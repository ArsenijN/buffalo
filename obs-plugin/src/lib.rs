//! buffalo OBS plugin -- v0 bare-bones milestone (SPECS.md 1.3).
//!
//! Verified against the actual obs-wrapper 0.4.1 source (not just docs snippets) plus a small
//! local patch (see vendor-obs-wrapper/) adding SourceContext::output_video_i420, which the
//! published crate doesn't expose for async video input sources.

const PLUGIN_VERSION: &str = concat!("1.0.0.build.", env!("BUFFALO_BUILD_NUMBER"));

mod control;
mod tcp_client;

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

use control::{ControlClient, ControlEvent};
use tcp_client::{DecodedFrame, TcpVideoClient};

const DEFAULT_VIDEO_PORT: i64 = 5757; // TBD per SPECS.md -- pick anything but 4747 (DroidCam)
const DEFAULT_CONTROL_PORT: i64 = 5758;

struct BuffaloSourceData {
    video_client: Option<TcpVideoClient>,
    control_client: Option<ControlClient>,
    latest_frame: Arc<Mutex<Option<DecodedFrame>>>,
    source: SourceContext,
    host: String,
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
        let host = settings
            .settings
            .get::<Cow<str>>("host")
            .map(|s| s.into_owned())
            .unwrap_or_else(|| "192.168.1.100".to_string());
        let video_port = settings.settings.get::<i64>("video_port").unwrap_or(DEFAULT_VIDEO_PORT) as u16;
        let control_port =
            settings.settings.get::<i64>("control_port").unwrap_or(DEFAULT_CONTROL_PORT) as u16;

        let mut data = BuffaloSourceData {
            video_client: None,
            control_client: None,
            latest_frame: Arc::new(Mutex::new(None)),
            source,
            host,
            video_port,
            control_port,
        };
        reconnect(&mut data);

        BuffaloSource { data: Mutex::new(data) }
    }
}

impl GetNameSource for BuffaloSource {
    fn get_name() -> ObsString {
        obs_string!("buffalo (phone camera)")
    }
}

/// Settings UI: IP + two port fields (SPECS.md 1.3). obs-wrapper doesn't currently expose a way
/// to add a literal "connect" button distinct from a settings-changed callback, so reconnecting
/// is driven by UpdateSource below -- same UX outcome (change a field, it takes effect), minus
/// a dedicated button.
impl GetPropertiesSource for BuffaloSource {
    fn get_properties(&mut self) -> Properties {
        let mut properties = Properties::new();

        // 1. Version display at the very top. The actual version text is set as this field's
        // default value in GetDefaultsSource below -- putting it in the *description* (as
        // before) makes it the field's label, not its content, which is why it rendered as an
        // empty editable text bar instead of showing the version.
        properties.add(
            obs_string!("plugin_version_label"),
            obs_string!("Plugin Version"),
            TextProp::new(TextType::Default),
        );

        // 2. Your existing fields with the fixed step sizes!
        properties.add(
            obs_string!("host"),
            obs_string!("Phone IP address"),
            TextProp::new(TextType::Default),
        );
        properties.add(
            obs_string!("video_port"),
            obs_string!("Video port"),
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
        settings.set_default::<Cow<str>>("plugin_version_label", PLUGIN_VERSION.into());
        settings.set_default::<Cow<str>>("host", "192.168.1.100".into());
        settings.set_default::<i64>("video_port", DEFAULT_VIDEO_PORT);
        settings.set_default::<i64>("control_port", DEFAULT_CONTROL_PORT);
    }
}

impl UpdateSource for BuffaloSource {
    fn update(&mut self, settings: &mut DataObj, _context: &mut GlobalContext) {
        let mut data = self.data.lock().unwrap();

        if let Some(host) = settings.get::<Cow<str>>("host") {
            data.host = host.into_owned();
        }
        if let Some(port) = settings.get::<i64>("video_port") {
            data.video_port = port as u16;
        }
        if let Some(port) = settings.get::<i64>("control_port") {
            data.control_port = port as u16;
        }

        reconnect(&mut data);
    }
}

fn reconnect(data: &mut BuffaloSourceData) {
    // Dropping first joins/stops the previous connections before we start new ones aimed at
    // the (possibly changed) host/ports.
    data.video_client = None;
    data.control_client = None;

    let latest_frame = data.latest_frame.clone();
    data.video_client = Some(TcpVideoClient::start(data.host.clone(), data.video_port, move |frame| {
        *latest_frame.lock().unwrap() = Some(frame);
    }));

    data.control_client = Some(ControlClient::start(
        data.host.clone(),
        data.control_port,
        |event| match event {
            ControlEvent::Hello { device, resolution } => {
                println!("[buffalo] connected to {device} ({resolution})");
            }
            ControlEvent::Connected => println!("[buffalo] control channel connected"),
            ControlEvent::Disconnected => println!("[buffalo] control channel disconnected"),
        },
    ));
}

/// Called every video tick by OBS. Pulls whatever the TCP thread last decoded and pushes it via
/// the patched output_video_i420. Decode happens off this thread (see tcp_client.rs) so this
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
