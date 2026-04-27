package view.world.generator;

import game.faction.FACTIONS;
import init.race.RACES;
import init.race.Race;
import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import util.data.GETTER;
import util.gui.misc.GButt;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.table.GTextScroller;
import util.text.D;
import util.text.Dic;

class StagePickRace extends GuiSection{

	static CharSequence ¤¤title = "Select species";
	static CharSequence ¤¤desc = "All species have unique play styles, excel at different works, and have different likes and dislikes.";

	private static CharSequence ¤¤challenge = "Initial Challenge";
	static {
		D.ts(StagePickRace.class);
	}
	
	StagePickRace(WorldViewGenerator stages){
	
		stages.reset();
		
		int i = 0;
		for (Race r : RACES.all()) {
			if (r.playable) {
				addGrid(rButt(r), i++, 6, 4, 4);
			}
		}
		
		addRelBody(16, DIR.N, new GText(UI.FONT().M, ¤¤desc).setMaxWidth(600));
		//addRelBody(4, DIR.N, new GHeader(¤¤title));
		
		
		GETTER<CharSequence> cc = new GETTER<CharSequence>() {

			@Override
			public CharSequence get() {
				return FACTIONS.player().race().info.desc_long;
			}
			
		};
		
		addRelBody(16, DIR.S, new GTextScroller(UI.FONT().M, cc, 650, 150));
		
		addRelBody(16, DIR.S, new GStat() {

			@Override
			public void update(GText text) {
				text.add(FACTIONS.player().race().info.initialChallenge);
			}
			
		}.increase().hv(¤¤challenge));
		
		addRelBody(16, DIR.S, new RENDEROBJ.RenderImp(650, 180) {
			
			private final GText t = new GText(UI.FONT().M, 64);
			
			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				Race rr = FACTIONS.player().race();
				int x = body.x1();
				int y = body.y1();
				t.setMaxWidth(280);
				t.setMultipleLines(true);
				for (String s : rr.info.pros) {
					t.clear().add('+').s().add(s).adjustWidth();
					t.normalify2();
					t.render(r, x+16, y);
					y+= t.height();
				}
				y = body.y1();
				for (String s : rr.info.cons) {
					t.clear().add('-').s().add(s).adjustWidth();
					t.errorify();
					t.render(r, x+325, y);
					y+= t.height();
				}
				
			}
		});
		
		
		
		int p = 650-body().width();
		if (p > 0)
			pad(p/2, 0);
		
		addRelBody(16, DIR.S, new GButt.ButtPanel(Dic.¤¤confirm) {
			@Override
			protected void clickA() {
				stages.hasSeletedRace = true;
				stages.set();
			}
			
		}.hoverInfoSet(Dic.¤¤confirm));
		
		pad(0, 8);
		
		stages.dummy.add(this, ¤¤title);
		
	}

	private CLICKABLE rButt(Race r) {
		GButt.ButtPanel b = new GButt.ButtPanel(r.appearance().iconBig.huge) {
			
			@Override
			protected void clickA() {
				FACTIONS.player().setRace(r);
			}
			
			@Override
			protected void renAction() {
				selectedSet(FACTIONS.player().race() == r);
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				text.title(r.info.name);
				
				
			}
			
		};
		b.pad(10, 10);
		return b;
		
	}
	
	
	
}
