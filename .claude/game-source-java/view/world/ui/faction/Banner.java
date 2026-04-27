package view.world.ui.faction;

import game.faction.diplomacy.DIP;
import game.faction.npc.FactionNPC;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import util.data.GETTER;
import util.gui.misc.GBox;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.text.Dic;
import world.region.RD;

final class Banner extends GuiSection{

	
	Banner(GETTER<FactionNPC> f, int width){
		
		body().setWidth(700);
		
		addRelBody(2, DIR.S, new GStat(new GText(UI.FONT().M, 32)) {
			
			@Override
			public void update(GText text) {
				text.add(f.get().nameIntro);
			}
		}.r(DIR.N));
		
		addRelBody(6, DIR.S, new GStat(new GText(UI.FONT().H1, 32)) {
			
			@Override
			public void update(GText text) {
				text.add(f.get().name);
				text.lablify();
			}
		}.r(DIR.N));
		
		addRelBody(4, DIR.S, new GStat() {
			
			@Override
			public void update(GText text) {
				if (!RD.DIST().reachable(f.get())) {
					if (RD.DIST().factionCanAttackPlayerAllies(f.get()))
						text.add(Dic.¤¤FactionBorder);
					else
						text.add(Dic.¤¤Distant);
				}
				else
					text.add(DIP.get(f.get()).name);
				
			}
			
			@Override
			public void hoverInfoGet(GBox b) {
				DIP.get(f.get()).hover(b);
			};
			
		}.r(DIR.N));
		
		RENDEROBJ rr = ban(f);
		rr.body().moveX1(0).moveCY(body().cY());
		add(rr);
		
		rr = ban(f);
		rr.body().moveX2(700).moveCY(body().cY());
		add(rr);
		
		RENDEROBJ info = Hoverer.facts(f, 1000, 90);
		addRelBody(4, DIR.S, info);
		
	}
	
	
	private RENDEROBJ ban(GETTER<FactionNPC> f) {
		return new RENDEROBJ.RenderImp(Icon.L*2) {
			
			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				f.get().banner().HUGE.render(r, body());
				
			}
		};
	}
	
	
	
}
