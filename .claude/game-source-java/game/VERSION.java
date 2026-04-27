package game;

public final class VERSION {

	public static final int VERSION_MAJOR = 70;
	public static final int VERSION_MINOR = 33;
	public static final int VERSION = version(VERSION_MAJOR, VERSION_MINOR);
	public static final String VERSION_STRING = versionString(VERSION);

	private VERSION() {

	}

	public static boolean versionIsBefore(int major, int minor) {
		return GAME.version() < version(major, minor);
	}
	
	public static boolean versionIs(int major, int minor) {
		return GAME.version() == version(major, minor);
	}
	
	public static int version(int major, int minor) {
		return (major << 16) | minor;
	}

	public static String versionString(int version) {
		int m = versionMajor(version);
		int n = versionMinor(version);
		
		return "0." + m + "." + n;
	}

	public static int versionMajor(int version) {
		return (version >> 16) & 0x0FFFF;
	}

	public static int versionMinor(int version) {
		return (version) & 0x0FFFF;
	}

}
