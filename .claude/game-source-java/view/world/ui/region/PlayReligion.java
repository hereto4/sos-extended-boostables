package view.world.ui.region;

import init.religion.Religion;
import init.religion.RELIGIONS;
import init.sprite.UI.UI;
import settlement.stats.STATS;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sprite.SPRITE;
import util.data.GETTER.GETTER_IMP;
import util.gui.misc.GBox;
import util.gui.misc.GMeter;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.info.GFORMAT;
import util.text.Dic;
import world.map.regions.Region;
import world.region.RD;
import world.region.RDReligions.RDReligion;

final class PlayReligion extends GuiSection {

	private GETTER_IMP<Region> g;

	
	PlayReligion(GETTER_IMP<Region> g, int W) {
		
		this.g = g;
		int i = 0;
		
		int w = 90;
		int cols = W/w;

		for (RDReligion reg : RD.RELIGION().all())
			addGridD(rel(reg), i++, cols, w, 34, DIR.W);
		
		i++;
		addGridD(new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.percInv(text, RD.RELIGION().opposition.getD(g.get()));
			}
			
			@Override
			public void hoverInfoGet(GBox b) {
				STATS.RELIGION().OPPOSITION.info().hover(b);
			}
		}.hh(UI.icons().m.cancel), i++, cols, w, 34, DIR.W);
		
		pad((W-body().width())/2, 0);

		
		
	}

	
	private RENDEROBJ rel(RDReligion rel) {
		
		GuiSection s = new GuiSection() {
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				rel.religion.info.hover(text);
				GBox b = (GBox) text;
				b.sep();
				rel.boost.hover(b, g.get(), Dic.¤¤Spread, false);
				b.sep();
				
				
				rel.boosts.hover(text, g.get());
				
				b.sep();
				b.textSLL(STATS.RELIGION().OPPOSITION.info().name);
				b.NL();
				int tab = 0;
				for (Religion o : RELIGIONS.ALL()) {
					b.tab(tab);
					b.text(o.info.name);
					b.tab(tab+6);
					b.add(GFORMAT.percInv(b.text(), rel.religion.opposition(o)));
					tab+= 8;
					if (tab > 8) {
						b.NL();
						tab = 0;
					}
					
				}
				
				super.hoverInfoGet(text);
			}
			
		};
		
		
		
		s.add(new SPRITE.Imp(48, 14) {
			
			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				double now = rel.current.getD(g.get());
				double t = rel.target(g.get());
				GMeter.render(r, GMeter.C_REDPURPLE, now, t, X1, X2, Y1, Y2);
			}
		},0, 0);
		
		s.addCentredY(rel.religion.icon, -30);
		
		return s;
		
	}
	
}
