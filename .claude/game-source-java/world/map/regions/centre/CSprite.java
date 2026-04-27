package world.map.regions.centre;

import game.GAME;
import game.time.TIME;
import init.constant.C;
import snake2d.CORE;
import snake2d.util.color.OPACITY;
import snake2d.util.gui.GuiSection;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import snake2d.util.sprite.TILE_SHEET;
import util.data.INT;
import util.data.INT.INTE;
import util.gui.panel.GPanel;
import util.gui.slider.GSliderInt;
import view.main.VIEW;
import view.world.panel.IDebugPanelWorld;
import world.WORLD;
import world.WRenContext;
import world.map.regions.Region;
import world.region.RD;

public final class CSprite {
	
	private final Urban urban = new Urban();
	private final Capitol capitol = new Capitol();
	private GETTER g = new GETTER() {
		
		@Override
		public double pop(Region reg) {
			return RD.RACES().popSize(reg);
		}
		
		@Override
		public double fort(Region reg) {
			return RD.BUILDINGS().levelWall.get(reg);
		}
		
		@Override
		public double garrison(Region reg) {
			return RD.MILITARY().garrison(reg);
		}
	};
	
	CSprite() {
		
		GuiSection ss = new GuiSection();
		INTE pop = new INT.IntImp(0, 100);
		INTE fort = new INT.IntImp(0, 100);
		INTE bu = new INT.IntImp(0, 100);
		
		ss.addDown(4, new GSliderInt(pop, 200, false));
		ss.addDown(4, new GSliderInt(fort, 200, false));
		ss.addDown(4, new GSliderInt(bu, 200, false));
		
		ss.add(new GPanel(ss.body()));
		ss.moveLastToBack();
		
		ss.body().moveX1Y1(50, 50);
		
		
		GETTER g = new GETTER() {
			
			@Override
			public double pop(Region reg) {
				return pop.getD();
			}
			
			@Override
			public double fort(Region reg) {
				return fort.getD();
			}
			
			@Override
			public double garrison(Region reg) {
				return bu.getD();
			}
		};
		
		IDebugPanelWorld.add("Region Visuals", new ACTION() {
			
			@Override
			public void exe() {
				CSprite.this.g = g;
				VIEW.inters().section.activate(ss);
			}
		});
	}
	
	
	private interface GETTER {
		
		public double pop(Region reg);
		public double fort(Region reg);
		public double garrison(Region reg);
		
	}
	
	public void renderOnGround(WRenContext con, int dtx, int dty, Region reg, int ran, int x1, int y1) {
		if (reg.capitol())
			capitol.renderOnGround(con, dtx, dty, reg, ran, x1, y1);
		else
			urban.renderOnGround(con, dtx, dty, reg, ran, x1, y1);
	}
	
	public void renderAboveA(WRenContext con, int dtx, int dty, Region reg, int ran, int x1, int y1) {
		if (reg.capitol())
			capitol.renderAboveA(con, dtx, dty, reg, ran, x1, y1);
		else
			urban.renderAboveA(con, dtx, dty, reg, ran, x1, y1);
		
	}
	
	public void renderAboveB(WRenContext con, int dtx, int dty, Region reg, int ran, int x1, int y1) {
		if (reg.capitol())
			capitol.renderAboveB(con, dtx, dty, reg, ran, x1, y1);
		else
			urban.renderAboveB(con, dtx, dty, reg, ran, x1, y1);
		
	}
	
	public void renderAboveTerrain(WRenContext con, int dtx, int dty, Region reg, int ran, int x1, int y1) {
		
	}
	
	private class Capitol {
		

		private final int[][] mTownHouses = new int[][] {
			{ 0, 1, 2, 2, 2, 2, 1, 0},
			{ 1, 3, 4, 4, 4, 4, 3, 1},
			{ 2, 4, 6, 7, 7, 6, 4, 2},
			{ 2, 4, 7, 9, 9, 7, 4, 2},
			{ 2, 4, 7, 9, 9, 7, 4, 2},
			{ 2, 4, 6, 7, 7, 6, 4, 2},
			{ 1, 3, 4, 4, 4, 4, 3, 1},
			{ 0, 1, 2, 2, 2, 2, 1, 0},
		};
		
		private final int[][] mGarrison = new int[][] {
			{ 0, 0, 0, 0, 0, 0, 0, 0},
			{ 0, 0, 0, 0, 0, 0, 0, 0},
			{ 0, 0, 0, 0, 0, 0, 0, 0},
			{ 0, 0, 0, 1, 2, 0, 0, 0},
			{ 0, 0, 0, 3, 4, 0, 0, 0},
			{ 0, 0, 0, 0, 0, 0, 0, 0},
			{ 0, 0, 0, 0, 0, 0, 0, 0},
			{ 0, 0, 0, 0, 0, 0, 0, 0},
		};
		
		private final int[][] mTownFarms = new int[][] {
			{1,1,1,1,},
			{1,0,0,1,},
			{1,0,0,1,},
			{1,1,1,1,},
		};
		
		private final int[][] mTerrain = new int[][] {
			{0,1,1,2,},
			{3,4,4,5,},
			{3,4,4,5,},
			{6,7,7,8,},
		};

		
		void renderAboveA(WRenContext con, int dtx, int dty, Region reg, int ran, int xx1, int yy1) {
			
			renderPop(con, dtx, dty, reg, ran, xx1-C.TILE_SIZEH, yy1-C.TILE_SIZEH, mTownHouses, mGarrison);
			
		}
		
		void renderAboveB(WRenContext con, int dtx, int dty, Region reg, int ran, int xx1, int yy1) {
			
			renderOverlay(con, dtx, dty, reg, ran, xx1-C.TILE_SIZEH, yy1-C.TILE_SIZEH, mTownHouses);
			renderGarrison(con, dtx, dty, reg, ran, xx1-C.TILE_SIZEH, yy1-C.TILE_SIZEH, mGarrison);
			
			int tx = dtx;
			int ty = dty;
			
			double p = g.pop(reg);
			
			p = CLAMP.d((p-0.4)/0.15, 0, 1);
			
			if (tx == 1 && ty == 1) {
				
				double size = g.fort(reg);
				if (size > 0) {
					int dim = CLAMP.i(64+(int) (p*56), 64, 120);
					int cx = xx1 + C.TILE_SIZEH;
					int cy = yy1 + C.TILE_SIZEH;
					RD.RACES().visuals.cRace(reg).appearance().world.walls.render(con, size, dim, cx, cy);
				}
			}
			
//			if (tx >= 1 && ty >= 1){
//				
//				
//				int m = (int) Math.round(CLAMP.d(g.fort(reg), 0, 1)*5);
//				if (m > 0) {
//					m-= 1;
//					tx -= 1;
//					ty -= 1;
//					
//					int dx = tx -1;
//					int dy = ty -1;
//					
//					int d = (int) ((C.TILE_SIZEH/2)*p);
//					
//					int x = -C.TILE_SIZE + d*dx;
//					int y = -C.TILE_SIZE + d*dy;
//					
//					int t = tx+ty*3 + 9*m;
//					Wall sh = RD.RACES().visuals.cRace(reg).appearance().world.wall;
//					
//					sh.render(con, t, xx1+x, yy1+y);
//				}
//				
//			}
			
			renderSiege(con, dtx, dty, reg, ran, xx1, yy1);
			
		}
		
		void renderOnGround(WRenContext con, int dtx, int dty, Region reg, int ran, int x1, int y1) {
			
			x1 -= C.TILE_SIZEH;
			y1 -= C.TILE_SIZEH;
			
			renderGround(con, dtx, dty, reg, ran, x1, y1, mTerrain, mTownFarms);
		}
		
		
	}
	
	private class Urban {
		
		private final int[][] mTownHouses = new int[][] {
			{ 0, 1, 2, 2, 1, 0,-1,-1},
			{ 1, 2, 4, 4, 2, 1,-1,-1},
			{ 2, 4, 9, 9, 4, 2,-1,-1},
			{ 2, 4, 9, 9, 4, 2,-1,-1},
			{ 1, 2, 4, 4, 2, 1,-1,-1},
			{ 0, 1, 2, 2, 1, 0,-1,-1},
			{-1,-1,-1,-1,-1,-1,-1,-1},
			{-1,-1,-1,-1,-1,-1,-1,-1},
		};
		
		private final int[][] mGarrison = new int[][] {
			{ 0, 0, 0, 0, 0, 0, 0, 0},
			{ 0, 0, 0, 0, 0, 0, 0, 0},
			{ 0, 0, 1, 2, 0, 0, 0, 0},
			{ 0, 0, 3, 4, 0, 0, 0, 0},
			{ 0, 0, 0, 0, 0, 0, 0, 0},
			{ 0, 0, 0, 0, 0, 0, 0, 0},
			{ 0, 0, 0, 0, 0, 0, 0, 0},
			{ 0, 0, 0, 0, 0, 0, 0, 0},
		};
		
		private final int[][] mTownFarms = new int[][] {
			{1,1,1,-1},
			{1,0,1,-1},
			{1,1,1,-1},
			{-1,-1,-1,-1},
		};
		
		private final int[][] mTerrain = new int[][] {
			{0,1,2,-1},
			{3,4,5,-1},
			{6,7,8,-1},
			{-1,-1,-1,-1},
		};
		
		void renderAboveA(WRenContext con, int dtx, int dty, Region reg, int ran, int xx1, int yy1) {
			
			renderPop(con, dtx, dty, reg, ran, xx1, yy1, mTownHouses, mGarrison);
			
		}
		
		void renderAboveB(WRenContext con, int dtx, int dty, Region reg, int ran, int xx1, int yy1) {
			
			renderOverlay(con, dtx, dty, reg, ran, xx1, yy1, mTownHouses);
			renderGarrison(con, dtx, dty, reg, ran, xx1, yy1, mGarrison);
			
			int tx = dtx;
			int ty = dty;
			
			double p = g.pop(reg);
			p = CLAMP.d((p-0.4)/0.15, 0, 1);
			
			if (tx == 1 && ty == 1) {
				
				double size = g.fort(reg);
				if (size > 0) {
					int dim = CLAMP.i(64+(int) (p*36), 64, 100);
					int cx = xx1 + C.TILE_SIZEH;
					int cy = yy1 + C.TILE_SIZEH;
					RD.RACES().visuals.cRace(reg).appearance().world.walls_village.render(con, size, dim, cx, cy);
				}
			}
			
//			int w = wall[dtx][dty];
//			
//			if (w > 0){
//				double p = g.pop(reg);
//				w-= 1;
//				int m = (int) Math.ceil(7*g.fort(reg));
//				m = CLAMP.i(m, 0, 7);
//				
//				if (m > 0) {
//					m -= 1;
//					int tile = 4*m + w;
//					
//					int d = (int) (p*24);
//					
//					WallVillage sh = RD.RACES().visuals.cRace(reg).appearance().world.wall_village;
//					
//					
//					
//					con.s.setHard();
//					con.s.setHeight(3).setDistance2Ground(0);
//					int x = xx1-32+d*wallDirs[w].x();
//					int y = yy1-32+d*wallDirs[w].y();
//					sh.render(con, tile, x, y);
//				}
//				
//				
//				
//			}
			
			renderSiege(con, dtx, dty, reg, ran, xx1, yy1);
			
		}
		
		void renderOnGround(WRenContext con, int dtx, int dty, Region reg, int ran, int x1, int y1) {
			
			renderGround(con, dtx, dty, reg, ran, x1, y1, mTerrain, mTownFarms);
			
		}
		
		
			
		
	}
	
	void renderGround(WRenContext con, int dtx, int dty, Region reg, int ran, int x1, int y1, int[][] mTerrain, int[][] mTownFarms) {
		
		{
			WorldRaceSheet.Terrain os = RD.RACES().visuals.cRace(reg).appearance().world.terrain;
			WorldRaceSheet.Farm farm = RD.RACES().visuals.cRace(reg).appearance().world.farm;
			if (mTerrain[dty][dtx] >= 0)
				os.render(con, mTerrain[dty][dtx], ran, x1, y1);
			
			int t = mTownFarms[dtx][dty];
			if (t >= 0) {
				t = (int) (mTownFarms[dtx][dty] + 2 * RD.RACES().popSize(reg) - (ran&0x011));
				if (t > 0) {
					OPACITY.O50.bind();
					farm.render(con, ran, x1, y1);
					OPACITY.unbind();
				}
			}
		}
		
		
	}
	
	private void renderGarrison(WRenContext con, int dtx, int dty, Region reg, int ran, int xx1, int yy1, int[][] mGarrison) {
		
		double v = g.garrison(reg);
		if (v <= 0)
			return;
		
		con.s.setHard();
		con.s.setHeight(6).setDistance2Ground(0);
		

		
		for (int y = 0; y < 2; y++) {
			for (int x = 0; x < 2; x++) {
				int ix = x+dtx*2;
				int iy = y+dty*2;
				if (!isGarrison(reg, ix, iy, ran, mGarrison)) 
					continue;
				
				int x1 = xx1+x*C.TILE_SIZEH;
				int y1 = yy1+y*C.TILE_SIZEH;
				TILE_SHEET sh = WORLD.BUILDINGS().sprites.garrison;
				int t = (int) Math.round(v * (7));
				int rot = (ran >>9)&0b011;
				sh.render(con.r, t+rot*8, x1, y1);
				return;
			}
		}
		
	}
	
	private boolean isGarrison(Region reg, int dx, int dy, int ran, int[][] mGarrison) {
		return g.garrison(reg) > 0 && mGarrison[dy][dx]-1 == ((reg.index())&0b11);
	}
	
	private void renderPop(WRenContext con, int dtx, int dty, Region reg, int ran, int xx1, int yy1, int[][] mTownHouses, int[][] mGarrison) {
		int tx = dtx;
		int ty = dty;
		double p = g.pop(reg);
		
		con.s.setSoft();
		con.s.setHeight(4);
		int ttt = ran;
		{
			WorldRaceSheet.Town sh = RD.RACES().visuals.cRace(reg).appearance().world.town;
			for (int y = 0; y < 2; y++) {
				for (int x = 0; x < 2; x++) {
					int ix = x+tx*2;
					int iy = y+ty*2;
					if (isGarrison(reg, ix, iy, ttt, mGarrison)) 
						continue;
					if (mTownHouses[iy][ix] < 0)
						continue;
					int t = mTownHouses[iy][ix]-9;
					t += (int)(p*11) + (ran & 0b011);
					ran = ran >> 2;
					if (t < 0)
						continue;
					t = (int) Math.round((double)WorldRaceSheet.Town.maxSize*t/10.0);
					
					if (t > WorldRaceSheet.Town.maxSize)
						t = WorldRaceSheet.Town.maxSize;
					int x1 = xx1+x*C.TILE_SIZEH;
					int y1 = yy1+y*C.TILE_SIZEH;
					sh.render(con.r, con.s, t, ran, x1, y1);
					ran = ran >> 4;
					if (TIME.light().nightIs() && (TIME.light().partOfCircular()*4 > (ran&0x07))) {
						int lx = reg.cx()-dtx;
						int ly = reg.cy()-dty;
						if (!WORLD.WATER().coversTile.is(lx, ly) && !WORLD.MOUNTAIN().coversTile(lx, ly)) {
							x1 += C.TILE_SIZEH/2+(GAME.intervals().get05()+ran & 0b11);
							y1 += C.TILE_SIZEH/2+(GAME.intervals().get05()+(ran>>4) & 0b11);
							CORE.renderer().renderUniLight(x1, y1, 2, 64);
						}
						
					}
					ran = ran >> 3;
					
				}
			}
			
		}
		
		if (RD.HEALTH().outbreak.get(reg) == 1){
			ran = ttt;
			for (int y = 0; y < 2; y++) {
				for (int x = 0; x < 2; x++) {
					int ix = x+tx*2;
					int iy = y+ty*2;
					if (mTownHouses[iy][ix] <= 0)
						continue;

					int x1 = xx1+x*C.TILE_SIZEH;
					int y1 = yy1+y*C.TILE_SIZEH;
					Sparks.render(x1, y1, mTownHouses[iy][ix], ran);
					ran = ran >> 3;
					
				}
			}
		}
		
		
		
	}
	
	private void renderOverlay(WRenContext con, int dtx, int dty, Region reg, int ran, int xx1, int yy1, int[][] mTownHouses) {
		int tx = dtx;
		int ty = dty;
		double p = g.pop(reg);
		
		con.s.setSoft();
		con.s.setHeight(4);
		
		ran = ran >> 1;
		{
			WorldRaceSheet.Overlay os = RD.RACES().visuals.cRace(reg).appearance().world.overlay;
			
			for (int y = 0; y < 2; y++) {
				for (int x = 0; x < 2; x++) {
					if (mTownHouses[y+ty*2][x+tx*2] < 0)
						continue;
					int t = (int) (mTownHouses[y+ty*2][x+tx*2]*p-(ran&0b0111));
					if (t > 0) {
						int x1 = xx1+x*C.TILE_SIZEH;
						int y1 = yy1+y*C.TILE_SIZEH;
						os.render(con, ran, x1, y1);
						
					}
					ran = ran >> 7;
					
				}
			}
		}
	}
	
	private void renderSiege(WRenContext con, int dtx, int dty, Region reg, int ran, int x1, int y1) {

		if (!reg.besieged())
			return;
		int tx = dtx;
		int ty = dty;
		
		
		if (tx >= 1 && ty >= 1){
			
			tx -= 1;
			ty -= 1;
			
			int dx = tx -1;
			int dy = ty -1;
			
			int d = (int) ((C.TILE_SIZEH/2 + 16));
			
			int x = -C.TILE_SIZE + d*dx;
			int y = -C.TILE_SIZE + d*dy;
			
			
			int t = tx+ty*3;
			WORLD.BUILDINGS().sprites.siege.render(con.r, t, x1+x, y1+y);
			con.s.setHard();
			con.s.setHeight(4).setDistance2Ground(0);
			WORLD.BUILDINGS().sprites.siege.render(con.s, t, x1+x, y1+y);
			
			
		}
	}
	


	
}
