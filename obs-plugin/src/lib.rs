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

const PLUGIN_VERSION: &str = concat!("1.0.0.build.", env!("BUFFALO_BUILD_NUMBER"));

mod control;
mod tcp_server;

use std::borrow::Cow;
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

struct BuffaloSourceData {
    video_server: Option<TcpVideoServer>,
    control_server: Option<ControlServer>,
    latest_frame: Arc<Mutex<Option<DecodedFrame>>>,
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
            source,
            video_port,
            control_port,
        };
        start_listening(&mut data);

        BuffaloSource { data: Mutex::new(data) }
    }
}

impl GetNameSource for BuffaloSource {
    fn get_name() -> ObsString {
        obs_string!("buffalo (phone camera)")
    }
}

/// Settings UI: just the two ports now -- no "Phone IP address" field, since this side listens
/// rather than dialing out (see the architecture note at the top of this file). Point the
/// phone's own settings at whatever IP this machine has on your LAN instead.
impl GetPropertiesSource for BuffaloSource {
    fn get_properties(&mut self) -> Properties {
        let mut properties = Properties::new();

        // Version display. The actual version text is set as this field's default value in
        // GetDefaultsSource below -- putting it in the *description* instead makes it the
        // field's label, not its content, which is why it previously rendered as an empty
        // editable text bar instead of showing the version.
        properties.add(
            obs_string!("plugin_version_label"),
            obs_string!("Plugin Version"),
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
        settings.set_default::<Cow<str>>("plugin_version_label", Cow::Borrowed(PLUGIN_VERSION));
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
    data.video_server = Some(TcpVideoServer::start(data.video_port, move |frame| {
        *latest_frame.lock().unwrap() = Some(frame);
    }));

    data.control_server = Some(ControlServer::start(
        data.control_port,
        |event| match event {
            ControlEvent::Hello { device, resolution } => {
                println!("[buffalo] phone identified itself: {device} ({resolution})");
            }
            ControlEvent::Connected => println!("[buffalo] control channel connected"),
            ControlEvent::Disconnected => println!("[buffalo] control channel disconnected"),
        },
    ));
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
