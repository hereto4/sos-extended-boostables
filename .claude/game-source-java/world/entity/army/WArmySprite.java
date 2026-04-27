package world.entity.army;

import java.io.IOException;

import game.GAME;
import init.constant.C;
import init.constant.Config;
import init.paths.PATHS;
import init.sprite.SPRITES;
import settlement.thing.pointlight.FireSparks;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.DIR;
import snake2d.util.misc.CLAMP;
import snake2d.util.sprite.TILE_SHEET;
import util.rendering.ShadowBatch;
import util.spritecomposer.ComposerDests;
import util.spritecomposer.ComposerSources;
import util.spritecomposer.ComposerThings.ITileSheet;
import util.spritecomposer.ComposerUtil;
import view.main.VIEW;
import world.WORLD;
import world.army.AD;

class WArmySprite {


	
	final TILE_SHEET sheet = (new ITileSheet(PATHS.SPRITE().getFolder("world").getFolder("entity").get("Army"), 136, 104) {
		@Override
		protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
			
			s.singles.init(0, 0, 2, 1, 2, 6, d.s8);
			s.singles.setVar(0);
			for (int i = 0; i < 7; i++) {
				s.singles.setSkip(i*2, 2);
				s.singles.paste(3, true);
			}
			s.singles.setVar(1);
			for (int i = 0; i < 7; i++) {
				s.singles.setSkip(i*2, 2);
				s.singles.paste(3, true);
			}
			
			
			return d.s8.saveGame();
			
		}
	}).get();
	
	static final int OFF = 8*7;
	static final int OFF_BOAT = 8*3;
	
	WArmySprite() throws IOException{
		
	}
	
	void render(WArmy a, Renderer r, ShadowBatch s, int x, int y, DIR dir) {
		
		COLOR color = a.faction() == null ? COLOR.WHITE50 : a.faction().banner().colorBG();
		
		
		
		color.bind();
		
		SPRITES.cons().BIG.dashed_hollow.renderBox(r, x+16, y+16, a.body().width()-32, a.body().height()-32);
		COLOR.unbind();
//		World.ENTITIES().armies.sprite.sMid.renderC(r, x+a.body().width()/2, y+a.body().height()/2);
//		COLOR.unbind();

		s.setHeight(1).setDistance2Ground(1);
		s.setHard();
		
		int d = 0;
		if (a.path().moving(a.body()))
			d = 8*GAME.intervals().get05() % 3;
		d*= 8;
		if (WORLD.WATER().has.is(a.ctx(), a.cty()))
			d += OFF_BOAT;
		else if(a.state() == WArmyState.fortified) {
			d = 2*OFF_BOAT;
		}
		
		
		
		
		double si = 2.0*AD.men(null).get(a)/Config.battle().MEN_PER_ARMY;
		si =  CLAMP.d(si, 0, 1);
		
		int am = (int) Math.ceil(si*16);
		int wi = (int) Math.ceil(si*4.0);
		int hi = (int) Math.ceil(1 + si*3.0);
		
		int y1 = (int) (y + C.TILE_SIZE -hi*C.TILE_SIZEH/2.0);
		for (int dy = 0; dy < hi; dy++) {
			int py = y1+dy*C.TILE_SIZEH;
			int w = am;
			w = CLAMP.i(w, 0, wi);
			am -= w;
			
			int px = x + C.TILE_SIZE;
			px -= C.TILE_SIZEH*w/2;
			for (int dx = 0; dx < w; dx++) {
				sheet.render(r, d+WArmySprite.OFF+dir.id(),px, py);
				color.bind();
				sheet.render(r, d+dir.id(), px, py);
				COLOR.unbind();
				sheet.render(s, d+WArmySprite.OFF+dir.id(), px, py);
				px+= C.TILE_SIZEH;
			}
			
		}
		
		if (a.raiding()) {
			FireSparks.render(VIEW.renderSecond()*26, x+a.body().width()/2, y+a.body().height()/2, 30, 1321342345, 0.5);
		}
		
	}
	
}
