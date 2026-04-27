package init.race.appearence;

import game.GAME;
import init.type.HTYPES;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import settlement.stats.stat.STAT;
import snake2d.util.MATH;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.file.Json;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.KeyMap;
import snake2d.util.sets.LIST;
import util.data.DOUBLE_O;
import util.keymap.MAPPED;
import util.keymap.RMAP;

public final class RColors {

	public static final int BLOOD_MASK = 64-1;
	public final COLOR blood;
	
	
	static final ColorCollection dummy = new ColorCollection(COLOR.WHITE100);
//	public final ColorCollection dead;
	private final COLOR[][] clothes;
//	private final COLOR[][] armour;
	private final ArrayList<ColorCollection> all;
	public static final COLOR grey = new ColorImp(170,170,170);
	public static final COLOR dead = new ColorImp(128,128,150);
	
	final RMAP<ColorCollection> collection;
	
	RColors(Json data){
		
		clothes = clothes("COLOR_CLOTHES", data, STATS.EQUIP().CLOTHES.stat().indu().max(null)+1, 16);
//		armour = armour("COLOR_ARMOUR_LEVELS", data, STATS.EQUIP().BATTLEGEAR.stat().indu().max(null)+1, 16);
		blood = new ColorImp(data, "COLOR_BLOOD");
		
		data = data.json("COLORS");
		all = new ArrayList<>(data.keys().size());
		
		KeyMap<ColorCollection> map = new KeyMap<>();
		
		for (String k : data.keys()) {
			map.put(k, new ColorCollection(all, data, k));
		}
		
//		skin = map.get("SKIN");
//		hair = map.get("HAIR");
//		leg = map.get("LEG");
//		{
//			COLOR[] dead = new COLOR[16];
//			for (int i = 0; i < dead.length; i++)
//				dead[i] = new ColorImp().interpolate(skin.get(i), COLOR.WHITE100, 0.3);
//			this.dead = new ColorCollection(dead);
//		}
		
		collection = new RMAP<ColorCollection>("COLORS", all);
	}
	
	public COLOR clothes(int level, int var) {
		return clothes[var&0x0F][level%clothes[0].length];
	}
	
//	public COLOR armour(int level, int var) {
//		return armour[var&0x0F][level%armour[0].length];
//	}
	
//	public COLOR skin(Induvidual indu) {
//		return skin.get((int) (indu.randomness2()>>((skin.ran)*8)));
//	}
//	
//	public COLOR hair(Induvidual indu) {
//		if (turngray && indu.hType() == HTYPE.RETIREE)
//			return grey;
//		return hair.get((int) (indu.randomness2()>>((hair.ran)*8)));
//	}
//	
//	public COLOR leg(Induvidual indu) {
//		return leg.get((int) (indu.randomness2()>>((leg.ran)*8)));
//	}
	
	public static class ColorCollection implements MAPPED{
		
		public static ColorCollection DUMMY = new ColorCollection(COLOR.WHITE100);
		
		final String key;
		protected final COLOR[] colors;
		private final int index;
		public final int ran;
		public boolean turnsGrayWhenOld;
		public boolean turnsWhiteWhenDead;
		public boolean addsSickColor;
		public final DOUBLE_O<Induvidual> statDerive;
		
		private ColorCollection(ArrayList<ColorCollection> all, Json json, String key) {
			this.key = key;
			json = json.json(key);
			this.index = all.add(this);
			turnsGrayWhenOld = json.bool("TURNS_GRAY_WHEN_OLD", false);
			turnsWhiteWhenDead = json.bool("TURNS_WHITE_WHEN_DEAD", false);
			addsSickColor = json.bool("TURNS_SICKLY", false);
			ran = index&15;
			
			if (json.has("PICK_BY_STAT")) {
				DOUBLE_O<Induvidual> statDerive = null;
				STAT s = STATS.STAT(json.value("PICK_BY_STAT"));
				if (s != null) {
					statDerive = s.indu();
				}
				this.statDerive = statDerive;
			}else {
				this.statDerive = null;
			}
			
			LIST<ColorImp> lcols = ColorImp.cols(json, "VALUES");
			COLOR[] cols = new COLOR[16];
			
			int k = 0;
			for (ColorImp c : lcols) {
				cols[k++] = c;
			}
			
			if (json.has("GENERATE_RANDOMIZE")) {
				double d = json.d("GENERATE_RANDOMIZE");
				for (int i = k; i < 16; i++) {
					cols[i] = new ColorImp(cols[i%lcols.size()]).shade(1.0-d*(i/16.0));
				}
			}else {
				COLOR[] nn = new COLOR[lcols.size()];
				for (int i = 0; i < lcols.size(); i++) {
					nn[i] = lcols.get(i);
				}
				cols = nn;
			}
			
			
			
			this.colors = cols;
			
		}
		
		private ColorCollection(COLOR color) {
			this.key = "";
			index = -1;
			this.colors = new COLOR[16];
			for (int i = 0; i < 16; i++) {
				this.colors[i] = color;
			}
			ran = 0;
			this.statDerive = null;
		}
		
		ColorCollection(COLOR[] color) {
			this.key = "";
			index = -1;
			this.colors = color;
			ran = 0;
			this.statDerive = null;
		}

		@Override
		public int index() {
			return index;
		}
		
		public COLOR get(int i) {
			return colors[MATH.mod(i, colors.length)];
		}
		
		public COLOR get(Induvidual in, boolean dead) {

			if (turnsGrayWhenOld && in.hType() == HTYPES.RETIREE())
				return grey;
			
			COLOR col = null;
			
			if (statDerive != null) {
				col = get((int)(statDerive.getD(in)*15));	
			}else {
				col = get((int) (STATS.RAN().get(in, 64) >> (ran*4)));
			}
			if (turnsWhiteWhenDead && dead)
				return ColorImp.TMP.interpolate(col, RColors.dead, 0.3);
			if (addsSickColor) {
				COLOR b = STATS.DISEASE().color(in);
				if (b != null)
					return colorAdd(col, b);
				b = GAME.EVENT().color(in);
				if (b != null)
					return colorAdd(col, b);
			}
			return col;
			
		}

		private final ColorImp color = new ColorImp();
		private double ci = 1.0/127.0;
		
		private COLOR colorAdd(COLOR ca, COLOR cb) {
			int r = ca.red()&0x0FF;
			int r2 = cb.red()&0x0FF;
			if (r2 > 127)
				r += r2-127;
			else
				r *= r2*ci;
			if (r > 0x0FF)
				r = 0x0FF;
			
			int g = ca.green()&0x0FF;
			int g2 = cb.green()&0x0FF;
			if (g2 > 127)
				g += g2-127;
			else
				g *= g2*ci;
			if (g > 0x0FF)
				g = 0x0FF;
			
			int b = ca.blue()&0x0FF;
			int b2 = cb.blue()&0x0FF;
			if (b2 > 127)
				b += b2-127;
			else
				b *= b2*ci;
			if (b > 0x0FF)
				b = 0x0FF;
			
			color.set(r, g, b);
			return color;
		}
		
		@Override
		public String key() {
			return key;
		}
		
	}
	
	private COLOR[][] clothes(String key, Json json, int levels, int vars) {
		Json[] is = json.jsons(key, 1, vars);
		COLOR[][] cols = new COLOR[vars][levels];
		for (int i = 0; i < cols.length; i++) {
			if (i >= is.length) {
				cols[i][levels-1] = new ColorImp(is[i%(is.length)]);
			}else {
				cols[i][levels-1] = new ColorImp(is[i]);
			}
		}
		
		for (int v = 0; v < vars; v++) {
			for (int s = levels-2; s >= 0; s--) {
				double d = (s+1.0)/(levels-1);
				cols[v][s] = cols[v][levels-1].makeSaturated(d);
			}
			
		}
		
		return cols;
	}
	
}
