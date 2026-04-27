package init.constant;

import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.datatypes.Rec;

public class C {

	private C(){}
	
	public static final int STEAM_ID = 1162750;

	public static final int MAX_SPRITES = 65536;
	public static final String NAME = "Songs of Syx";
	public final static int SCALE = 4;
	public final static int SCALE_NORMAL = 2;
	/**
	 * SCALE GUI
	 */
	public final static int SG = 1;
	public final static int GM = 24*SG;
	public static final int T_PIXELS = 16;
	public static final int T_SCROLL = 6;
	public static final int TILE_SIZE = T_PIXELS*SCALE;
	public static final double ITILE_SIZE = 1.0/TILE_SIZE;
	public static final int TILE_SIZEH = TILE_SIZE/2;
	public static final int T_MASK = TILE_SIZE-1; 
	private static int WIDTH = 1280;
	private static int HEIGHT = 768;
	
	public static final int MIN_WIDTH = 1280;
	public static final int MIN_HEIGHT = 768;


	public static final int MAX_SCREEN_AREA = 2500*1080;
	
	
	private static final Rec DIM = new Rec(0, WIDTH, 0, HEIGHT);
//	public static final int TWIDTH = 768;
//	public static final RECTANGLE SETTLE_TDIM = new Rec(0, TWIDTH, 0, TWIDTH);
	public static final double SQR2 = Math.sqrt(2.0);
	public static final double SQR2I = 1.0/Math.sqrt(2.0);
	public static final String WEB_PAGE = "https://songsofsyx.com";
	public static final String BUG_MAIL = "info@songsofsyx.com";
	
	public static int WIDTH() {
		return WIDTH;
	}

	public static int HEIGHT() {
		return HEIGHT;
	}
	
	public static RECTANGLE DIM() {
		return DIM;
	}

	public static void init(int width, int height) {
		WIDTH = width;
		HEIGHT = height;
		DIM.set(0, WIDTH, 0, HEIGHT);
	}
	
	
}
