package view.world.ui.region;

import snake2d.SPRITE_RENDERER;
import snake2d.util.gui.GuiSection;
import util.data.GETTER.GETTER_IMP;
import util.gui.misc.GBox;
import util.text.Dic;
import view.interrupter.ISidePanel;
import view.interrupter.ISidePanels;
import view.tool.ToolManager;
import world.WORLD;
import world.map.regions.Region;

final class PlayCapitol implements RV{

	private GETTER_IMP<Region> g = new GETTER_IMP<>();
	private final ISidePanel panel;
	PlayToolAttack tool;
	
	public PlayCapitol(ToolManager m, ISidePanels p) {
		
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
				super.render(r, ds);
			}
		};
		
		s.add( MiscMore.garrison(g, 256));
		
		
		panel = new ISidePanel(s);
		
	}


	
	@Override
	public ISidePanel get(Region reg) {
		g.set(reg);
		tool.add(g.get());
		
		panel.titleSet(Dic.¤¤Capitol);
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
		PlayHov.hover(reg, box);
	}
	
	
}
