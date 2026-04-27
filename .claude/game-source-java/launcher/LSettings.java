package launcher;

import game.VERSION;
import init.constant.C;
import init.paths.PATHS;
import snake2d.Displays;
import snake2d.Displays.DisplayMode;
import snake2d.util.file.Json;
import snake2d.util.file.JsonE;
import snake2d.util.misc.CLAMP;
import snake2d.util.misc.OS;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LIST;
import util.data.GETTER.GETTERE;
import util.data.INT.INTE;

public final class LSettings {

	private final ArrayListGrower<LSetting> all = new ArrayListGrower<LSetting>();
	public final LSettingInt otherJVM = new LSettingInt("JVM", 0, 1);
	public final LSettingInt debug = new LSettingInt("DEBUG", 0, 1);
	public final LSettingInt developer = new LSettingInt("DEVELOPER", 0, 1);
	public final LSettingInt rpc = new LSettingInt("RPC", PATHS.isSteam() && OS.get() != OS.MAC ? 1 : 0, 1);
	public final LSettingInt linear = new LSettingInt("LINEAR", 1, 1);
	public final LSettingInt shading = new LSettingInt("SHADING", 1, 1);
	public final LSettingInt vsync = new LSettingInt("VSYNC", 0, 1);
	public final LSettingInt vsyncadapt = new LSettingInt("VSYNC_ADAPTIVE", 0, 1);
	public final LSettingInt easy = new LSettingInt("EASY_FONT", 0, 1);
	public final LSettingInt version = new LSettingInt("VERSION", -1, Integer.MAX_VALUE-1);
	public final LSettingInt monitor = new LSettingInt("MONITOR", 0, Integer.MAX_VALUE-1) {
		
		@Override
		public int max() {
			return Displays.monitors()-1;
		};
	};
	
	public final LSettingInt screenMode = new LSettingInt("SCREEN_MODE", 0, 2);
	
	public final static int screenModeBorderLess = 0;
	public final static int screenModeFull = 1;
	public final static int screenModeWindowed = 2;
	public final LSettingInt fullScreenDisplay = new LSettingInt("FULL_DISPLAY", 0, Integer.MAX_VALUE-1) {
		
		@Override
		public int max() {
			LIST<DisplayMode> dis = Displays.available(monitor.get());
			if (dis == null || dis.size() == 0)
				return 0;
			return dis.size()-1;
		};
		
	};
	public final LSettingInt windowWidth = new LSettingInt("WINDOW_WIDTH", 15, 20) {
		@Override
		public int min() {
			return (int) (max()*(double)C.MIN_WIDTH/Displays.current(monitor.get()).width);
		};
	};
	public final LSettingInt windowHeight = new LSettingInt("WIDOW_HEIGHT", 15, 20) {
		@Override
		public int min() {
			return (int) (max()*(double)C.MIN_HEIGHT/Displays.current(monitor.get()).height);
		};
	};
	
	public final LSettingInt windowBorderLessScale = new LSettingInt("WIDOW_SCALE", 0, 100) {

		
		@Override
		public int max() {
			double dh = Displays.current(monitor.get()).height/(double)C.MIN_HEIGHT;
			double dv = Displays.current(monitor.get()).width/(double)C.MIN_WIDTH;
			double d = Math.min(dh, dv);
			d -= 1.0;
			return (int)CLAMP.d(d/0.05, 0, 100);
		};
		
		@Override
		public double getD() {
			return get()*0.05;
		};
		
	};
	
	public final LSettingInt decorated = new LSettingInt("WINDOW_DECORATE", 1, 1);
	public final LSettingInt forcedHD = new LSettingInt("WINDOW_FORCE_HD", 0, 1);
//	public final LSettingInt fill = new LSettingInt("WINDOW_FILL", 0, 1);
	
	public final LSettingInt shadows = new LSettingInt("SHADOWS", 2, 2);
	public final LSettingInt particles = new LSettingInt("PARICLES", 2, 2);
	public final LSettingInt gore = new LSettingInt("GORE", 2, 2);
	public final LSettingInt volumeMaster = new LSettingInt("VOLUME_MASTER", 70, 100);
	public final LSettingInt volumeSound = new LSettingInt("VOLUME_SOUND", 100, 100);
	public final LSettingInt volumeMusic = new LSettingInt("VOLUME_MUSIC", 70, 100);
	public final LSettingInt volumeAmbience = new LSettingInt("VOLUME_AMBIENCE", 70, 100);
	public final LSettingInt focusMute = new LSettingInt("FOCUS_MUTE", 0, 1);
	public final LSettingInt brightness = new LSettingInt("BRIGHTNESS22", 50, 100);
	public final LSettingInt autoSaveInterval = new LSettingInt("AUTO_SAVE_TIME", 9, 10);
	public final LSettingInt autoSaveFiles = new LSettingInt("AUTO_SAVE_FILES", 5, 10);
	public final LSettingInt edgeScroll = new LSettingInt("EDGE_SCROLL", 0, 1);
	public final LSettingInt detail = new LSettingInt("GRAPHIC_DETAIL", 1, 1);
	public final LSettingInt lightCycle = new LSettingInt("LIGHT_CYCLE", 1, 1);
	public final LSettingInt uiLightCycle = new LSettingInt("UI_LIGHT_CYCLE", 1, 1);
	public final LSettingInt downpour = new LSettingInt("DOWNPOUR", 1, 1);
	public final LSettingInt winIconi = new LSettingInt("WIN_AUTO_ICONIFY", 1, 1);
	public final LSettingInt winFoat = new LSettingInt("WINDOW_FLOAT", 0, 1);
	public final LSettingInt winFullFull = new LSettingInt("WINDOW_FULL_FULL", 0, 1);
	public final SString alternateJVM = new SString("PATH_JAVA", "");
	public final SString lang = new SString("LANGUAGE", "");
	public final SString audiodevice = new SString("OPENAL", "");
	public final SStrings mods = new SStrings("MODS", new String[] {});
	public final SStrings jvmArguments = new SStrings("JVM_ARGS2", new String[] {
		"-Xms512m",
		"-Xmx4096m",
		"-XX:+UseCompressedOops",
		"-Dfile.encoding=UTF-8",
		"-server",
		"-Dfml.earlyprogresswindow=false",
		"-XX:+UseSerialGC"
	});
	
//	public String alternateJVM = "";
//	private String[] mods = new String[0];
//	public String lang = "";
//	public String audiodevice = "";
	
	public void setDefault() {
		
		for (LSetting s : all)
			s.setDefault();
	}
	
	public LSettings() {
		try {
			Json json = new Json(PATHS.local().SETTINGS.get("LauncherSettings"));
			for (LSetting s : all) {
				if (json.has(s.key))
					s.read(json);
				else
					s.setDefault();
			}
			
		} catch (Exception e) {
			e.printStackTrace(System.out);
			setDefault();
			save();
		}
	}
	
	boolean check() {
		
		return false;
		
	}
	
	public void save() {
		try {
			JsonE json = new JsonE();
			version.v = VERSION.VERSION;
			for (LSetting s : all) {
				s.write(json);
			}
			json.save(PATHS.local().SETTINGS.create("LauncherSettings"));
			
			
		} catch (Exception e) {
			e.printStackTrace(System.out);
			setDefault();
		}
	}
	
	
	public abstract class LSetting {

		public final String key;
		
		private LSetting(String key){
			this.key = key;
			all.add(this);
		}
		
		protected abstract void setDefault();
		protected abstract void read(Json json);
		protected abstract void write(JsonE json);
				
		
	}
	
	public class LSettingInt extends LSetting implements INTE {

		protected int v;
		public final int defaultValue;
		private final int max;
		
		private LSettingInt(String key, int defaultValue, int max){
			super(key);
			this.defaultValue = defaultValue;
			this.max = max;
		}
		
		@Override
		public int get() {
			return CLAMP.i(v, 0, max());
		}

		@Override
		public int min() {
			return 0;
		}

		@Override
		public int max() {
			return max;
		}

		@Override
		public void set(int t) {
			v = CLAMP.i(t, 0, max());
			save();
		}

		@Override
		protected void setDefault() {

			v = defaultValue;
		}

		@Override
		protected void read(Json json) {
			v = json.i(key, -Integer.MAX_VALUE, Integer.MAX_VALUE, defaultValue);
		}

		@Override
		protected void write(JsonE json) {
			json.add(key, v);
		}
		
		
	}
	
	public final class SString extends LSetting implements GETTERE<String> {
		
		public final String def;
		public String current;
		
		SString(String key, String def){
			super(key);
			this.def = def;
		}

		@Override
		public String get() {
			return current;
		}

		@Override
		protected void setDefault() {
			current = def;
		}

		@Override
		protected void read(Json json) {
			current = json.text(key);
			if (current.equals("null"))
				current = null;
		}

		@Override
		protected void write(JsonE json) {
			json.addString(key, current == null ? "null" : current);
		}

		@Override
		public void set(String t) {
			current = t;
		}
		
	}
	
	public final class SStrings extends LSetting implements GETTERE<String[]>{
		
		public final String key;
		public final String[] def;
		public String[] current;
		
		SStrings(String key, String[] def){
			super(key);
			this.key = key;
			this.def = def;
		}

		@Override
		public String[] get() {
			if (current == null)
				return def;
			return current;
		}

		@Override
		protected void setDefault() {
			current = def;
		}

		@Override
		protected void read(Json json) {
			current = json.texts(key);
			
		}

		@Override
		protected void write(JsonE json) {
			json.addStrings(key, current);
		}

		@Override
		public void set(String[] t) {
			current = t;
		}
		
	}
	
}

