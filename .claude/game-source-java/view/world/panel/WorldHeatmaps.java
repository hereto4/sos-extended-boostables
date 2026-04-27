package view.world.panel;

import init.sprite.SPRITES;
import snake2d.MButt;
import snake2d.util.gui.GuiSection;
import util.gui.misc.GButt;
import util.text.Dic;
import view.main.VIEW;
import world.WORLD;
import world.overlay.WorldOverlays.OverlayTileNormal;

final class WorldHeatmaps extends GButt.ButtPanel{

	private final GuiSection s;
	
	private OverlayTileNormal selected = null;
	
	WorldHeatmaps(){
		super(SPRITES.icons().s.eye);
		s = new GuiSection();
		
		hoverInfoSet(Dic.¤¤Overlays);
		
		{
			GButt.Checkbox c = new GButt.Checkbox(Dic.¤¤name) {
				
				@Override
				protected void clickA() {
					WORLD.OVERLAY().regNames.active.toggle();
				}
				
				@Override
				protected void renAction() {
					selectedSet(WORLD.OVERLAY().regNames.active.is());
				}
				
				
			};
			s.addDown(0, c);
		}
		
		for (OverlayTileNormal o : WORLD.OVERLAY().togglable) {
			GButt.ButtPanel c = new GButt.ButtPanel(o.info.name) {
				
				@Override
				protected void clickA() {
					if (selected == o)
						selected = null;
					else
						selected = o;
				}
				
				@Override
				protected void renAction() {
					selectedSet(selected == o);
				}
				
				
			};
			c.hoverSet(o.info);
			c.body().setWidth(200);
			s.addDown(0, c);
		}
			
		
	}
	
	@Override
	protected void clickA() {
		VIEW.inters().popup.show(s, this);
	}
	
	@Override
	protected void renAction() {
		if (hoveredIs() && MButt.RIGHT.consumeClick()) {
			selected = null;
		}
		
		if (selected != null)
			selected.add();
		
		
		selectedSet(selected != null);
	};
	
	
}
