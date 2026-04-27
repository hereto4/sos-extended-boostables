package view.world.ui.region;

import game.faction.Faction;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import util.data.GETTER.GETTER_IMP;
import util.gui.misc.GBox;
import util.gui.panel.GFrame;
import view.interrupter.ISidePanel;
import view.interrupter.ISidePanels;
import view.tool.ToolManager;
import world.WORLD;
import world.map.regions.Region;

final class Play implements RV{

	private GETTER_IMP<Region> g = new GETTER_IMP<>();
	private final ISidePanel panel;
	PlayToolAttack tool;
	private Faction ff;
	private final PlayMilitary mi;
	
	public Play(ToolManager m, ISidePanels p) {
		
		tool = new PlayToolAttack(m) {

			@Override
			boolean added() {
				return p.added(panel);
			}
			
		};
		
		GuiSection s = new GuiSection() {
			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				WORLD.OVERLAY().hover(g.get());
				if (ff != g.get().faction())
					panel.last().remove(panel);
				super.render(r, ds);
			}
		};
		int w = 600;
		int sep =580;
		s.body().setWidth(w);
		
		s.add(new PlayInfo(g, w), 0, s.getLastY2());
		s.addRelBody(0, DIR.S, GFrame.separator(sep));
		mi = new PlayMilitary(g, w);
		s.addRelBody(0, DIR.S, mi);
		s.addRelBody(0, DIR.S, GFrame.separator(sep));
		
		s.addRelBody(0, DIR.S, new PlayReligion(g, w));
		s.addRelBody(0, DIR.S, GFrame.separator(sep));
		
		s.addRelBody(0, DIR.S, new PlayPop(g, w, (ISidePanel.HEIGHT-s.body().height())/3));
		s.addRelBody(8, DIR.S, new PlayOutput(g, w));
		s.add(new PlayBuildings(g, w, ISidePanel.HEIGHT-s.body().height()-8), 0, s.getLastY2()+8);
		
		panel = new ISidePanel(s);
		
	}


	
	@Override
	public ISidePanel get(Region reg) {
		g.set(reg);
		tool.add(g.get());
		ff = reg.faction();
		panel.titleSet(reg.info.name());
		return panel;
	}

	@Override
	public void hover(GBox box, Region reg) {
		PlayHov.hover(reg, box);
	}
	

	@Override
	public boolean added(ISidePanels pans, Region reg) {
		return pans.added(panel) && reg == g.get();
	}

	@Override
	public void hoverGarrison(GBox box, Region reg) {
		box.title(reg.info.name());
		g.set(reg);
		box.add(mi);
	}
	
	
}
