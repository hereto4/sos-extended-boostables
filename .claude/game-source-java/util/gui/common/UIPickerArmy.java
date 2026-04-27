package util.gui.common;

import game.faction.Faction;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import util.colors.GCOLOR;
import util.data.GETTER;
import util.gui.misc.GButt;
import util.gui.table.GTableBuilder;
import util.gui.table.GTableBuilder.GRowBuilder;
import view.world.ui.WorldHoverer;
import world.entity.army.WArmy;

public abstract class UIPickerArmy extends GuiSection{

	protected abstract boolean canBePicked(WArmy a);
	protected abstract void pick(WArmy a);
	
	private final GETTER<? extends Faction> g;
	
	public UIPickerArmy(GETTER<? extends Faction> g, int height) {
		
		this.g = g;
		GTableBuilder builder = new GTableBuilder() {
			
			@Override
			public int nrOFEntries() {
				if (g.get() == null)
					return 0;
				return g.get().armies().all().size();
			}
		};
		
		builder.column("", 250, new GRowBuilder() {
			
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
			body.setWidth(250);
			body.setHeight(40);
		}
		
		private WArmy a() {
			return g.get().armies().all().get(ier.get());
		}

		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
			
			GButt.ButtPanel.renderBG(r, true, false, isHovered, body);
			SPRITES.icons().s.sword.renderCY(r, body().x1()+6, body().cY());
			
			GCOLOR.T().H1.bind();
			
			UI.FONT().H2.render(r, a().name, body().x1()+24, body().cY()-UI.FONT().H2.height()/2);
			if (!canBePicked(a())) {
				OPACITY.O35.bind();
				COLOR.BLACK.render(r, body, -4);
				OPACITY.unbind();
			}
			GButt.ButtPanel.renderFrame(r, body);
				
		}
		
		@Override
		protected void clickA() {
			if (canBePicked(a()))
				pick(a());
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			UIPickerArmy.this.hover(text, a());
		}
		
		
		
	}
	
	public void hover(GUI_BOX text, WArmy a) {
		WorldHoverer.hover(text, a);
	}
	
}
