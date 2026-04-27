package view.sett.ui.subject;

import init.constant.C;
import init.race.RACES;
import init.race.Race;
import init.race.appearence.RPortrait;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.stats.STATS;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.ACTION;
import util.gui.misc.GButt;
import view.main.VIEW;
import view.sett.IDebugPanelSett;

final class SPortraitsDebug extends GuiSection{
	

	private int start = 0;
	private int race;
	private COLOR bg = COLOR.BLACK;
	
	SPortraitsDebug() {
		
		body().setWidth(C.WIDTH());
		body().setHeight(C.HEIGHT());
		
		RENDEROBJ r;
		
		r = new GButt.Glow("randomize") {
			@Override
			protected void clickA() {
				start += 50;
				
			};
		};
		
		r.body().moveX1Y1(20, 20);
		add(r);
		
		r = new GButt.Glow("racify") {
			@Override
			protected void clickA() {
				race++;
			};
		};
		
		addRightC(20, r);
		
		r = new GButt.Glow("green") {
			@Override
			protected void clickA() {
				SPortraitsDebug.this.bg = COLOR.GREEN100;
			};
		};
		
		addRightC(20, r);
		
		IDebugPanelSett.add("Portraits", new ACTION() {
			
			@Override
			public void exe() {
				VIEW.inters().section.activate(SPortraitsDebug.this);
			}
		});
		
	}
	
	@Override
	public void render(SPRITE_RENDERER r, float ds) {
		bg.render(r, body());
		super.render(r, ds);
		ENTITY[] ents = SETT.ENTITIES().getAllEnts();
		
		int m = 20;
		int y = 60;
		int x = m;
		int w = RPortrait.P_WIDTH*4;
		int h = RPortrait.P_HEIGHT*4;
		
		Race ra = RACES.all().get(race%RACES.all().size());
	
		for (int i = 0; i < ents.length; i++) {
			int k = (i+start)%ents.length;
			if (!(ents[k] instanceof Humanoid))
				continue;
			Humanoid hu = (Humanoid) ents[k];
			
			if (hu.race() != ra)
				continue;
			
			STATS.APPEARANCE().portraitRender(r, hu.indu(), x, y, 4);
			
			x += w + m;
			if (x + w > C.WIDTH()) {
				y += h + m;
				if (y + h > C.HEIGHT())
					break;
				x = m;
			}
		}
	}
	
}
