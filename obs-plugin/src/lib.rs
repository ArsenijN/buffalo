//! buffalo OBS plugin -- v0 bare-bones milestone (SPECS.md 1.3).
//!
//! Verified against the actual obs-wrapper 0.4.1 source (not just docs snippets) plus a small
//! local patch (see vendor-obs-wrapper/) adding SourceContext::output_video_i420, which the
//! published crate doesn't expose for async video input sources.
//!
//! ARCHITECTURE: the phone is the client on both channels (video TCP and control WebSocket);
//! this plugin LISTENS, it doesn't dial out. This was flipped from an earlier design where both
//! sides tried to dial each other -- which can never connect, since nobody was listening. Phone-
//! as-client was chosen over the reverse because mobile carriers use CGNAT: a phone can't be
//! reached as a server from outside its own LAN under any circumstances, while a PC (this side)
//! can be port-forwarded if remote access is ever wanted. It also means the phone has zero open
//! ports -- no attack surface to discover or port-scan, only outbound connections it initiates.

mod control;
mod tcp_server;

use std::sync::{Arc, Mutex};

use obs_wrapper::{
    data::DataObj,
    module::{LoadContext, Module, ModuleContext},
    obs_register_module, obs_string,
    properties::{NumberProp, Properties, TextProp, TextType},
    source::{
        CreatableSourceContext, GetDefaultsSource, GetNameSource, GetPropertiesSource,
        GlobalContext, SourceContext, SourceType, Sourceable, UpdateSource, VideoTickSource,
    },
    string::ObsString,
};

use control::{ControlEvent, ControlServer};
use tcp_server::{DecodedFrame, TcpVideoServer};

const DEFAULT_VIDEO_PORT: i64 = 5757; // TBD per SPECS.md -- pick anything but 4747 (DroidCam)
const DEFAULT_CONTROL_PORT: i64 = 5758;

/// Live connection status, shared with the background listener threads so
/// GetPropertiesSource can show a snapshot of it. See the long comment on get_properties()
/// below for why this can only ever be a snapshot-on-open, not a live tick -- obs-wrapper's
/// GetPropertiesSource hook has no settings-write access, unlike UpdateSource.
#[derive(Default)]
struct StreamStatus {
    video_connected: bool,
    frames_received: u64,
    control_connected: bool,
    device: Option<String>,
    resolution: Option<String>,
}

impl StreamStatus {
    fn summary_line(&self) -> String {
        let video = if self.video_connected {
            format!("video connected ({} frames received)", self.frames_received)
        } else {
            "waiting for phone to connect...".to_string()
        };
        let control = match (&self.device, &self.resolution) {
            (Some(d), Some(r)) => format!("phone identified as {d} ({r})"),
            _ if self.control_connected => "control channel connected, no hello yet".to_string(),
            _ => "control channel idle".to_string(),
        };
        format!("Status: {video} | {control}")
    }
}

struct BuffaloSourceData {
    video_server: Option<TcpVideoServer>,
    control_server: Option<ControlServer>,
    latest_frame: Arc<Mutex<Option<DecodedFrame>>>,
    status: Arc<Mutex<StreamStatus>>,
    source: SourceContext,
    video_port: u16,
    control_port: u16,
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

    fn create(settings: &mut CreatableSourceContext<Self>, source: SourceContext) -> Self {
        let video_port = settings.settings.get::<i64>("video_port").unwrap_or(DEFAULT_VIDEO_PORT) as u16;
        let control_port =
            settings.settings.get::<i64>("control_port").unwrap_or(DEFAULT_CONTROL_PORT) as u16;

        let mut data = BuffaloSourceData {
            video_server: None,
            control_server: None,
            latest_frame: Arc::new(Mutex::new(None)),
            status: Arc::new(Mutex::new(StreamStatus::default())),
            source,
            video_port,
            control_port,
        };
        start_listening(&mut data);

        BuffaloSource { data: Mutex::new(data) }
    }
}

/// Version is embedded directly in the source's displayed name (shown in the Sources panel and
/// the Add Source dialog) instead of a Properties field. This used to be a TextProp, which is
/// ALWAYS editable in this crate/libobs version -- there's no read-only "info" text type
/// available in the bindings this is built against, checked directly against the raw C
/// bindings, not just the safe wrapper. Rather than fake read-only and let someone type over
/// the version number, the name is a field OBS genuinely never lets you edit.
impl GetNameSource for BuffaloSource {
    fn get_name() -> ObsString {
        obs_string!(concat!("buffalo (phone camera) v", "1.0.0.build.", env!("BUFFALO_BUILD_NUMBER")))
    }
}

/// Settings UI: two ports, plus a status line built fresh every time this dialog opens.
///
/// Why the status line is only a snapshot-on-open, not a live tick: this hook's signature is
/// `fn get_properties(&mut self) -> Properties` -- no `&mut DataObj` parameter, unlike
/// UpdateSource::update(). Properties::add() only lets us set a field's TYPE and LABEL
/// (description), not inject a fresh runtime VALUE into settings; DataObj::set_default (via
/// GetDefaultsSource) only backfills a key that has no value yet, it doesn't force-overwrite an
/// existing one on every open. So the only channel we have for "something computed right now"
/// is the field's label text, which is why the status text lives there instead of in the
/// field's content. Close and reopen this dialog to refresh it.
impl GetPropertiesSource for BuffaloSource {
    fn get_properties(&mut self) -> Properties {
        let data = self.data.lock().unwrap();
        let status_line = data.status.lock().unwrap().summary_line();
        drop(data);

        let mut properties = Properties::new();

        properties.add(
            obs_string!("status_info"),
            ObsString::from(status_line),
            TextProp::new(TextType::Default),
        );

        properties.add(
            obs_string!("video_port"),
            obs_string!("Video port (point the phone app at this machine's IP + this port)"),
            NumberProp::new_int()
                .with_range(1024i64..=65535i64)
                .with_step(1i64),
        );
        properties.add(
            obs_string!("control_port"),
            obs_string!("Control port"),
            NumberProp::new_int()
                .with_range(1024i64..=65535i64)
                .with_step(1i64),
        );
        properties
    }
}

impl GetDefaultsSource for BuffaloSource {
    fn get_defaults(settings: &mut DataObj) {
        settings.set_default::<i64>("video_port", DEFAULT_VIDEO_PORT);
        settings.set_default::<i64>("control_port", DEFAULT_CONTROL_PORT);
    }
}

impl UpdateSource for BuffaloSource {
    fn update(&mut self, settings: &mut DataObj, _context: &mut GlobalContext) {
        let mut data = self.data.lock().unwrap();

        if let Some(port) = settings.get::<i64>("video_port") {
            data.video_port = port as u16;
        }
        if let Some(port) = settings.get::<i64>("control_port") {
            data.control_port = port as u16;
        }

        start_listening(&mut data);
    }
}

fn start_listening(data: &mut BuffaloSourceData) {
    // Dropping first stops any previous listeners before binding new ones on the (possibly
    // changed) ports.
    data.video_server = None;
    data.control_server = None;

    let latest_frame = data.latest_frame.clone();
    let status_for_video = data.status.clone();
    data.video_server = Some(TcpVideoServer::start(
        data.video_port,
        move |frame| {
            {
                let mut s = status_for_video.lock().unwrap();
                s.video_connected = true;
                s.frames_received += 1;
            }
            *latest_frame.lock().unwrap() = Some(frame);
        },
        {
            let status = data.status.clone();
            move |connected| {
                status.lock().unwrap().video_connected = connected;
            }
        },
    ));

    let status_for_control = data.status.clone();
    data.control_server = Some(ControlServer::start(data.control_port, move |event| {
        let mut s = status_for_control.lock().unwrap();
        match event {
            ControlEvent::Hello { device, resolution } => {
                println!("[buffalo] phone identified itself: {device} ({resolution})");
                s.device = Some(device);
                s.resolution = Some(resolution);
            }
            ControlEvent::Connected => {
                println!("[buffalo] control channel connected");
                s.control_connected = true;
            }
            ControlEvent::Disconnected => {
                println!("[buffalo] control channel disconnected");
                s.control_connected = false;
                s.device = None;
                s.resolution = None;
            }
        }
    }));
}

/// Called every video tick by OBS. Pulls whatever the TCP thread last decoded and pushes it via
/// the patched output_video_i420. Decode happens off this thread (see tcp_server.rs) so this
/// stays a cheap lock+take regardless of network jitter -- a late phone frame just means OBS
/// keeps showing the last one instead of this callback blocking on a socket read.
impl VideoTickSource for BuffaloSource {
    fn video_tick(&mut self, _seconds: f32) {
        let mut data = self.data.lock().unwrap();
        let frame = data.latest_frame.lock().unwrap().take();

        if let Some(frame) = frame {
            let timestamp_ns = std::time::SystemTime::now()
                .duration_since(std::time::UNIX_EPOCH)
                .map(|d| d.as_nanos() as u64)
                .unwrap_or(0);
            data.source
                .output_video_i420(&frame.i420, frame.width, frame.height, timestamp_ns);
        }
    }
}

struct BuffaloModule {
    context: ModuleContext,
}

impl Module for BuffaloModule {
    fn new(context: ModuleContext) -> Self {
        Self { context }
    }

    fn get_ctx(&self) -> &ModuleContext {
        &self.context
    }

    fn load(&mut self, load_context: &mut LoadContext) -> bool {
        let source = load_context
            .create_source_builder::<BuffaloSource>()
            .enable_get_name()
            .enable_get_properties()
            .enable_get_defaults()
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
