package settlement.tilemap.terrain;

import java.io.IOException;

import init.paths.PATHS;
import settlement.main.SETT;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.file.Json;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import util.spritecomposer.ComposerDests;
import util.spritecomposer.ComposerSources;
import util.spritecomposer.ComposerThings;
import util.spritecomposer.ComposerUtil;

public final class TColors {

	public final Tree tree;
	public final Minimap minimap;
	
	public final COLOR waternormal;
	public final COLOR waterWinter;
	
	public TColors() throws IOException {
		Json j = new Json(PATHS.CONFIG().get("SettColors"));
		tree = new Tree();
		minimap = new Minimap(j);
		j = j.json("WATER");
		waternormal = new ColorImp(j, "NORMAL");
		waterWinter = new ColorImp(j, "WINTER");
	}
	
	void update(double ds) {
		tree.update(ds);
	}
	
	void init() {
		tree.update(4);
	}
	
	final class Tree {
		
		private final ColorImp[] cols = new ColorImp[64];
		public final LIST<COLOR> fertile;
		private final LIST<COLOR> dry;
		private final LIST<COLOR> autumn;
		private final LIST<COLOR> winter;
		
		
		Tree() throws IOException{
			
			for (int i = 0; i < cols.length; i++) {
				cols[i] = new ColorImp();
			}
			
			new ComposerThings.IInit(PATHS.SPRITE_SETTLEMENT_MAP().get("TreeColors"), 536, 76);
			
			fertile = row(0);
			dry = row(1);
			autumn = row(2);
			winter = row(3);
		}
		
		private LIST<COLOR> row(int row) throws IOException {
			LIST<COLOR> cc = new ComposerThings.IColorSampler() {
				
				@Override
				protected COLOR next(int i, ComposerUtil c, ComposerSources s, ComposerDests d) {
					s.full.setSkip(1, row*16 + i);
					return s.full.sample();
				}
				
				@Override
				protected int init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					s.full.init(0, 0, 1, 1, 16, 4, d.s16);
					return 16;
				}
			}.getHalf();
			
			ArrayList<COLOR> nn = new ArrayList<>(cols.length);
			
			for (int i = 0; i < cols.length; i++) {
				COLOR c = cc.getC(i);
				if (i >= nn.size())
					c = new ColorImp(c).shadeSelf(RND.rFloat1(0.1));
				nn.add(c);
			}
			return nn;
			
			
		}
		
		private double time = 0;
		
		void update(double ds) {
			time -= ds;
			if (time > 0)
				return;
			time += 2;
			
			double moist = SETT.WEATHER().moisture.getD();
			if (moist < 0.5) {
				moist = moist/0.5;
			}else
				moist = 1.0;
			
			double winter = 1.0 - SETT.WEATHER().growth.getD();
			double autumn = 0;
			if (winter <= 0.5 && SETT.WEATHER().growth.isAutumn()) {
				autumn = Math.pow(winter*2.0, 0.5);
				winter = 0;
			}else if (winter > 0.5) {
				autumn = SETT.WEATHER().growth.isAutumn() ? 1.0 : 0;
				winter = (winter-0.5)*2.0;
				moist += winter;
			}
			for (int i = 0; i < cols.length; i++) {
				set(i, autumn, winter, 1.0-moist);
			}
		}
		
		private final ColorImp c1 = new ColorImp();
		private final ColorImp c2 = new ColorImp();
		
		private void set(int i, double autumn, double winter, double dry) {
			c1.interpolate(fertile.get(i), this.autumn.get(i), autumn);
			c2.interpolate(c1, this.winter.get(i), winter);
			cols[i].interpolate(c2, this.dry.get(i), dry);
		}

		public COLOR get(int ran) {
			return cols[ran&63];
		}
		
		public COLOR def() {
			return fertile.get(0);
		}
		
		public COLOR dry(int ran) {
			return dry.getC(ran);
		}
		
		public COLOR winter(int ran) {
			return winter.getC(ran);
		}
	}
	
	public final class Minimap {
		
		public final COLOR tree;
		public final COLOR water;
		public final COLOR water_deep;
		public final COLOR rock;
		public final COLOR growable;
		public final COLOR mountain;
		
		private Minimap(Json j ) throws IOException{
		
			j = j.json("MINIMAP");
			tree = new ColorImp(j, "TREE").shadeSelf(2.0);
			water = new ColorImp(j, "WATER").shadeSelf(2.0);
			water_deep = new ColorImp(j, "WATER_DEEP").shadeSelf(2.0);
			rock = new ColorImp(j, "ROCK").shadeSelf(2.0);
			growable = new ColorImp(j, "GROWABLE").shadeSelf(2.0);
			mountain = new ColorImp(j, "MOUNTAIN").shadeSelf(2.0);
		}
		
		
		
		
	}
	
	public final class Water {
		
		public final COLOR normal;
		public final COLOR winterMask;
		
		
		private Water(Json j) throws IOException{
			j = j.json("WATER");
			normal = new ColorImp(j, "NORMAL");
			winterMask = new ColorImp(j, "WINTER_MASK");
		}
		
		
		
		
	}

	
	
	

}
