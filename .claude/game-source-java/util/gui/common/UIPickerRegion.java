package util.gui.common;

import game.faction.FBanner;
import game.faction.Faction;
import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sprite.SPRITE;
import util.colors.GCOLOR;
import util.data.GETTER;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.table.GTableBuilder;
import util.gui.table.GTableBuilder.GRowBuilder;
import view.main.VIEW;
import world.WORLD;
import world.map.regions.Region;

public abstract class UIPickerRegion extends GuiSection{

	protected boolean active(Region reg) {
		return true;
	}
	protected boolean selected(Region reg) {
		return false;
	}
	protected abstract void toggle(Region reg);
	
	protected void hoverInfo(GBox b, Region reg) {
		VIEW.world().UI.regions.hover(reg, b);
	}
	
	private GETTER<? extends Faction> g;
	
	public UIPickerRegion(GETTER<? extends Faction> g, int height) {
		
		this.g = g;
		
		GTableBuilder builder = new GTableBuilder() {
			
			@Override
			public int nrOFEntries() {
				if (g.get() == null)
					return 0;
				
				return g.get().realm().regions();
			}
		};
		
		builder.column("", 264, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return new Button(ier);
			}
		});
		
		add(builder.createHeight(height, false));
		
		
	}
	
	private class Button extends ClickableAbs{

		private final GETTER<Integer> ier;
		
		public Button(GETTER<Integer> ier) {
			this.ier = ier;
			body.setWidth(264);
			body.setHeight(40);
		}
		
		private Region r() {
			return g.get().realm().region(ier.get());
		}

		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
			
			Region reg = r();
			if (reg == null)
				return;
			
			isSelected = selected(reg);
			GButt.ButtPanel.renderBG(r, isActive, isSelected, isHovered, body);
			
			SPRITE b = FBanner.rebel.MEDIUM;
			if (reg.faction() != null)
				b = reg.faction().banner().MEDIUM;
			
			b.renderCY(r, body().x1()+8, body().cY());
			
			GCOLOR.T().H1.bind();
			UI.FONT().H2.render(r, reg.info.name(), body().x1()+40, body().cY()-UI.FONT().H2.height()/2);
			
			if (!active(reg)) {
				OPACITY.O35.bind();
				COLOR.BLACK.render(r, body, -4);
				OPACITY.unbind();
			}
				
			GButt.ButtPanel.renderFrame(r, body);
			
		}
		
		@Override
		protected void clickA() {
			
			Region reg = r();
			if (reg == null)
				return;
			
			if (active(reg))
				toggle(reg);
		}
		
		@Override
		public boolean hover(COORDINATE mCoo) {
			if (super.hover(mCoo)) {
				Region reg = r();
				if (reg != null)
					WORLD.MINIMAP().hilight(reg);
				return true;
			}
			return false;
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			Region reg = r();
			if (reg == null)
				return;
			UIPickerRegion.this.hoverInfo((GBox) text, reg);
			
		}
		
	}
	
}
