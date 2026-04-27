package settlement.entity.humanoid.spirte;

import game.GAME;
import init.constant.C;
import init.race.RACES;
import init.race.Race;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import snake2d.util.color.COLOR;
import snake2d.util.sprite.TILE_SHEET;

final class HSpriteConst {

	private HSpriteConst() {

	}
	
	private static int i = 0;
	private final static int NR = 8;
	final static int IFEET_NONE = i++ * NR;
	final static int IFEET_RIGHT = i++ * NR;
	final static int IFEET_RIGHT2 = i++ * NR;
	final static int IFEET_LEFT = i++ * NR;
	final static int IFEET_LEFT2 = i++ * NR;
	final static int ITUNIC = i++ * NR;
	final static int ITORSO_STILL = i++ * NR;
	final static int ITORSO_RIGHT = i++ * NR;
	final static int ITORSO_RIGHT2 = i++ * NR;
	final static int ITORSO_RIGHT3 = i++ * NR;
	final static int ITORSO_LEFT = i++ * NR;
	final static int ITORSO_LEFT2 = i++ * NR;
	final static int ITORSO_LEFT3 = i++ * NR;
	final static int ITORSO_CARRY = i++ * NR;
	final static int ITORSO_OUT = i++ * NR;
	final static int ITORSO_OUT2 = i++ * NR;

	final static int IHEAD = i++ * NR;
	final static int ISHADOW = i++ * NR;

	
	final static int[][] ITOOL = new int[][] {
		{0*NR, 1*NR, 2*NR},
		{0*NR, 1*NR, 2*NR},
	};
	
	final static int[][] IWEAPON = new int[][] {
		{0*NR, 1*NR, 2*NR},
		{0*NR, 1*NR, 2*NR},
	};
	
	static {
		i = 0;
	}
	
	final static int HAMMER1 = i++;
	final static int HAMMER2 = i++ * NR;
	final static int HAMMER3 = i++ * NR;
	final static int HAMMER4 = i++ * NR;
	final static int HAMMER5 = i++ * NR;
	final static int HAMMER6 = i++ * NR;
//	public final static int[] FILTH = new int[] {0, NR, 2 *NR, 3*NR,4*NR,5*NR,6*NR,7*NR};
//	public final static int[] BLOOD = new int[] {0, NR, 2 *NR, 3*NR,4*NR,5*NR,6*NR,7*NR};
//	final static int[] WATER = new int[] { 0, NR, 2 * NR, 3 * NR };
	final static int[] TROLLY = new int[] { 0, NR, 2 * NR, 3 * NR };
	final static int SWORD1 = 0;
	final static int SWORD2 = NR;
	final static int SWORD3 = 2 * NR;


	public static void filth(Induvidual indu, int torse, int x, int y) {
		double am = STATS.NEEDS().grime(indu); 
		texture(indu.race().appearance().sheet(indu).sheet.sheet, RACES.sprites().grit, am, STATS.RAN().get(indu, 0), torse, x, y);
	}
	
	public static void blood(Induvidual indu, int torse, int x, int y) {
		double am = STATS.NEEDS().INJURIES.COUNT.indu().getD(indu);
		indu.race().appearance().colors.blood.bind();
		texture(indu.race().appearance().sheet(indu).sheet.sheet, RACES.sprites().blood, am, STATS.RAN().get(indu, 0), torse, x, y);
		COLOR.unbind();
	}

	private static void texture(TILE_SHEET sheet, TILE_SHEET ex, double am, long ran, int torse, int x, int y) {
		if (am == 0)
			return;
		int i = (int)(am*0x07) * 8;
		i += ran&0x07;
		sheet.renderTextured(ex.getTexture(i), torse, x, y);
		
	}
	
	public static void water(Induvidual indu, int dir, int torso, int x, int y) {
		indu.race().appearance().sheet(indu).sheet.sheet.renderTextured(indu.race().appearance().extra.water.getTexture(CLAY.exWATER[GAME.intervals().get05() & 0b011] + dir),  torso, x, y);
	}

	
	public static class CLAY {
		static int i = 0;
		public static final int PANTS = i++ * NR;
		static final int TORSO = i++ * NR;
		static final int ARMS = i++ * NR;
		static final int HEAD = i++ * NR;
		static final int SHADOW = i++ * NR;
		static{i = 0;}
		private static final int[] exWATER = new int[] {0, NR, 2 *NR, 3 *NR};

		static final int off = (24 - 32) * C.SCALE / 2;
		static final int offC = 32 * C.SCALE / 2;
		
		public static void filth(Induvidual indu, int dir, int x, int y) {
			double am = STATS.NEEDS().grime(indu);
			texture(indu.race().appearance().sheet(indu).sheet.lay, RACES.sprites().Lgrit, am, STATS.RAN().get(indu, 0), dir, x, y);
		}
		
		public static void filth(Race race, boolean adult, double am, int dir, int ran, int x, int y) {
			texture(adult ? race.appearance().adult().sheet.lay : race.appearance().child().sheet.lay, RACES.sprites().Lgrit, am, ran, dir, x, y);
		}
		
		public static void blood(Induvidual indu, int dir, int x, int y) {
			double am = STATS.NEEDS().INJURIES.COUNT.indu().getD(indu);
			indu.race().appearance().colors.blood.bind();
			texture(indu.race().appearance().sheet(indu).sheet.lay, RACES.sprites().Lblood, am, STATS.RAN().get(indu, 0), dir, x, y);
			COLOR.unbind();
		}

		private static void texture(TILE_SHEET sheet, TILE_SHEET ex, double am, long ran, int dir, int x, int y) {
			if (am == 0)
				return;
			int i = (int)(am*0x07) *8;
			i += ran&0x07;
			sheet.renderTextured(ex.getTexture(i), SHADOW+dir, x, y);
			
		}
		
		public static void water(Induvidual indu, int dir, int x, int y) {
			indu.race().appearance().sheet(indu).sheet.lay.renderTextured(indu.race().appearance().extra.Lwater.getTexture(CLAY.exWATER[GAME.intervals().get05() & 0b011] + dir),  CLAY.SHADOW+dir, x, y);
		}
		
		
	}
	

}
