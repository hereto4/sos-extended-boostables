package game.save;

import java.nio.file.Path;
import java.util.Arrays;

import game.GameSpec;
import game.VERSION;
import init.paths.PATH;
import init.paths.PATHS;
import settlement.stats.STATS;
import snake2d.util.sprite.text.Str;
import util.text.D;
import util.text.DicTime;

public class SaveFile implements Comparable<SaveFile>{
	
	public final Path path;
	public final String name;
	public final CharSequence ago;
	public final CharSequence fullName;
	public final int version;
	public final int modHash;
	public final int pop;
	public final long t;
	
	private static CharSequence ¤¤Version = "¤This save is from a previous version and will probably not load!";
	private static CharSequence ¤¤Mod = "¤This save is made with a different mod configuration and will probably not load!";
	
	static {
		D.ts(SaveFile.class);
	}
	
	public SaveFile(Path path){
		this.path = path;
		String f =  path.getFileName().toString();
		if (f.lastIndexOf(".") > 0)
			f = f.substring(0, f.lastIndexOf("."));
		fullName = f;
		name = name(f);
		t = time(f);
		version = version(f);
		modHash = modHash(f);
		pop = pop(f);
		if (t > 0 && System.currentTimeMillis()-t > 0) {
			double tt = (System.currentTimeMillis() - t)/1000;
			double now = tt/(60*60*24*365);

			if (now < 1) {
				now = tt/(60*60*24);
				if (now < 1) {
					now = tt/(60*60);
					if (now < 1) {
						now = tt/60;
						if (now == 0) {
							ago = DicTime.setSeconds(new Str(8), tt);
						}else {
							ago = DicTime.setMinutes(new Str(8), now);
						}
						
						
					}else {
						ago = DicTime.setHours(new Str(8), now);
					}
					
				}else {
					ago = DicTime.setDays(new Str(8), now);
				}
			}else {
				ago = DicTime.setYears(new Str(8), now);
			}

		}else {
			ago = "???";
		}
	}
	

	public static SaveFile[] list(){
		return list( PATHS.local().save());
	}
	
	public static SaveFile[] list(PATH path){
		String[] ss = path.getFiles();
		
		SaveFile[] saves = new SaveFile[ss.length];
		for (int i = 0; i < ss.length; i++) {
			saves[i] = new SaveFile(path.get(ss[i]));
		}
		Arrays.sort(saves);
		return saves;
	}
	
	public static String name(String file) {
		return get(file, 4, false);
	}
	
	public static long time(String file) {
		String s = get(file, 3, true);
		try {
			return Long.parseLong(s, 16);
		}catch(Exception e) {
			return -1;
		}
	}
	
	private static int version(String file) {
		String s = get(file, 2, true);
		
		try {
			return (int) Long.parseLong(s, 16);
		}catch(Exception e) {
			return 0;
		}
	}
	
	private static int modHash(String file) {
		String s = get(file, 1, true);
		try {
			return (int) Long.parseLong(s, 16);
		}catch(Exception e) {
			return 0;
		}
	}
	
	private static int pop(String file) {
		String s = get(file, 0, true);
		try {
			return (int) Long.parseLong(s, 16);
		}catch(Exception e) {
			return 0;
		}
	}

	private static String get(String file, int part, boolean p) {
		while(part > 0) {
			int i = file.lastIndexOf('-');
			if (i <= 0)
				return "0";
			file = file.substring(0, i);
			part--;
		}
		int i = file.lastIndexOf('-');
		if (i <= 0 || !p)
			return file;
		return file.substring(i+1, file.length());
	}
	
	@Override
	public int compareTo(SaveFile arg0) {
//		if (VERSION.versionMajor(version) < VERSION.versionMajor(arg0.version)) {
//			return 1;
//		}
//		if (VERSION.versionMajor(version) > VERSION.versionMajor(arg0.version)) {
//			return -1;
//		}
		if (t < 0 && arg0.t >= 0)
			return 1;
		if (t >= 0 && arg0.t < 0)
			return -1;
		
		long ti = arg0.t - t;
		if (ti < 0)
			return -1;
		if (ti > 0)
			return 1;
		return name.compareTo(arg0.name);
	}
	
	public static String stamp(CharSequence savefile) {
		String t = Long.toHexString(System.currentTimeMillis());
		String v = Integer.toHexString(VERSION.VERSION);
		String mods = Integer.toHexString(PATHS.modHash());
		String pop = Integer.toHexString(STATS.POP().POP.data(null).get(null));
		
	
		String s = savefile + "-" + t + "-" + v + "-" + mods  + "-" + pop;
		return s;
	}
	
	public CharSequence problem() {
		if (VERSION.VERSION_MAJOR != VERSION.versionMajor(version)) {
			return ¤¤Version;
		}
		if (modHash != PATHS.modHash()) {
			return ¤¤Mod;
		}
		return null;
	}
	
	private GameSpec spec;
	
	public GameSpec spec() {
		if (spec == null) {
			spec = GameSpec.get(path);
		}
		return spec;
	}
	
	public boolean specReady() {
		return spec != null;
	}
	
}