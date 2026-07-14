//! buffalo OBS plugin -- v0 bare-bones milestone (SPECS.md 1.3).
//!
//! NOTE ON API STABILITY: obs-wrapper's own docs describe it as incomplete and prone to
//! breaking changes between versions. The trait/method names below (Sourceable,
//! GetNameSource, UpdateSource, VideoTickSource, GetPropertiesSource, and the exact
//! output-frame call on SourceContext) reflect the 0.4.x shape at the time this was written.
//! If `cargo build` complains about a missing trait or renamed method, that's this drift --
//! diff against whatever obs-wrapper version Cargo.lock actually resolves
//! (docs.rs/obs-wrapper/<version>) and adjust the trait impls below; the overall structure
//! (module -> source builder -> per-frame push) will still be right.

mod control;
mod tcp_client;

use std::sync::{Arc, Mutex};

use obs_wrapper::{
    obs_register_module, obs_string,
    prelude::*,
    source::*,
};

use control::{ControlClient, ControlEvent};
use tcp_client::{DecodedFrame, TcpVideoClient};

const DEFAULT_VIDEO_PORT: u32 = 5757; // TBD per SPECS.md -- pick anything but 4747 (DroidCam)
const DEFAULT_CONTROL_PORT: u32 = 5758;

struct BuffaloSourceData {
    video_client: Option<TcpVideoClient>,
    control_client: Option<ControlClient>,
    latest_frame: Arc<Mutex<Option<DecodedFrame>>>,
    host: String,
    video_port: u32,
    control_port: u32,
}

struct BuffaloSource {
    data: Mutex<BuffaloSourceData>,
}

impl Sourceable for BuffaloSource {
    fn get_id() -> ObsString {
        obs_string!("buffalo_source")
    }

    fn get_type() -> SourceType {
        SourceType::INPUT
    }

    fn create(settings: &mut CreatableSourceContext<Self>, _source: SourceContext) -> Self {
        let host = settings.settings().get_string("host").unwrap_or_else(|| "192.168.1.100".into());
        let video_port = settings.settings().get_int("video_port").unwrap_or(DEFAULT_VIDEO_PORT as i64) as u32;
        let control_port = settings.settings().get_int("control_port").unwrap_or(DEFAULT_CONTROL_PORT as i64) as u32;

        BuffaloSource {
            data: Mutex::new(BuffaloSourceData {
                video_client: None,
                control_client: None,
                latest_frame: Arc::new(Mutex::new(None)),
                host,
                video_port,
                control_port,
            }),
        }
    }
}

impl GetNameSource for BuffaloSource {
    fn get_name() -> ObsString {
        obs_string!("buffalo (phone camera)")
    }
}

/// Settings UI: IP + two port fields, matching SPECS.md 1.3 "Basic settings UI: IP/port
/// fields, connect button". obs-wrapper doesn't currently give a clean way to add a literal
/// "connect" button that isn't a property callback, so connection is instead driven by
/// "settings changed" (UpdateSource below) -- functionally the same UX, minus a dedicated
/// button, until we need something fancier.
impl GetPropertiesSource for BuffaloSource {
    fn get_properties(&mut self) -> Properties {
        let mut properties = Properties::new();
        properties.add(
            obs_string!("host"),
            obs_string!("Phone IP address"),
            StringType::Default,
        );
        properties.add_int(
            obs_string!("video_port"),
            obs_string!("Video port"),
            1024,
            65535,
            1,
        );
        properties.add_int(
            obs_string!("control_port"),
            obs_string!("Control port"),
            1024,
            65535,
            1,
        );
        properties
    }
}

impl UpdateSource for BuffaloSource {
    fn update(&mut self, settings: &mut DataObj, _context: &mut GlobalContext) {
        let mut data = self.data.lock().unwrap();

        data.host = settings.get_string("host").unwrap_or_else(|| data.host.clone());
        data.video_port = settings.get_int("video_port").unwrap_or(data.video_port as i64) as u32;
        data.control_port = settings.get_int("control_port").unwrap_or(data.control_port as i64) as u32;

        reconnect(&mut data);
    }
}

fn reconnect(data: &mut BuffaloSourceData) {
    // Drop existing connections (their Drop impls join the threads / stop the runtime) before
    // starting new ones with the updated host/ports.
    data.video_client = None;
    data.control_client = None;

    let latest_frame = data.latest_frame.clone();
    data.video_client = Some(TcpVideoClient::start(
        data.host.clone(),
        data.video_port as u16,
        move |frame| {
            *latest_frame.lock().unwrap() = Some(frame);
        },
    ));

    data.control_client = Some(ControlClient::start(
        data.host.clone(),
        data.control_port as u16,
        |event| match event {
            ControlEvent::Hello { device, resolution } => {
                println!("[buffalo] connected to {device} ({resolution})");
            }
            ControlEvent::Connected => println!("[buffalo] control channel connected"),
            ControlEvent::Disconnected => println!("[buffalo] control channel disconnected"),
        },
    ));
}

/// Called every video tick by OBS. Pull whatever the TCP thread last decoded and push it into
/// the source's async video output. Because decode happens on TcpVideoClient's own thread and
/// this runs on OBS's video thread, we hand off through `latest_frame` rather than decoding
/// here -- keeps this callback cheap and non-blocking, which matters since it runs at your OBS
/// canvas frame rate regardless of whether a new phone frame has actually arrived.
impl VideoTickSource for BuffaloSource {
    fn video_tick(&mut self, _seconds: f32) {
        let data = self.data.lock().unwrap();
        let frame = data.latest_frame.lock().unwrap().take();
        drop(data);

        if let Some(frame) = frame {
            // TODO: build an obs_source_frame from `frame.i420` (VIDEO_FORMAT_I420,
            // frame.width, frame.height) and call the equivalent of
            // obs_source_output_video2 on this source's SourceContext. The exact call on
            // obs-wrapper 0.4's SourceContext type needs to be checked against
            // docs.rs/obs-wrapper for the pinned version -- this is the one piece of the
            // v0 checklist ("Convert decoded AVFrame to obs_source_frame, push via
            // obs_source_output_video2") that's still a stub.
            let _ = frame;
        }
    }
}

struct BuffaloModule {
    context: ModuleRef,
}

impl Module for BuffaloModule {
    fn new(context: ModuleRef) -> Self {
        Self { context }
    }

    fn get_ctx(&self) -> &ModuleRef {
        &self.context
    }

    fn load(&mut self, load_context: &mut LoadContext) -> bool {
        let source = load_context
            .create_source_builder::<BuffaloSource>()
            .enable_get_name()
            .enable_get_properties()
            .enable_update()
            .enable_video_tick()
            .build();

        load_context.register_source(source);
        true
    }

    fn description() -> ObsString {
        obs_string!("buffalo -- streams a phone camera over Wi-Fi into OBS")
    }

    fn name() -> ObsString {
        obs_string!("buffalo")
    }

    fn author() -> ObsString {
        obs_string!("buffalo contributors")
    }
}

obs_register_module!(BuffaloModule);
