package util.gui.common;

import init.race.RACES;
import init.race.Race;
import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.MATH;
import snake2d.util.color.COLOR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.Hoverable.HOVERABLE.HoverableAbs;
import snake2d.util.sets.LIST;
import util.colors.GCOLOR;
import util.gui.misc.GBox;
import util.gui.misc.GButt;

public class UIPickerRace {

	public final GuiSection section = new GuiSection();
	private final LIST<Race> all;
	private int current = 0;
	
	public UIPickerRace(){
		this(RACES.all());
	}
	
	public UIPickerRace(LIST<Race> races){
		this.all = races;
		
		GButt.ButtPanel b = new GButt.ButtPanel(UI.icons().m.arrow_left) {
			@Override
			protected void clickA() {
				set(current-1);
				super.clickA();
			}
		};
		b.body.setHeight(46);
		b.pad(1, 0);
		section.add(b);
		
		section.addRightC(0, new HoverableAbs(80, 46) {
			
			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
				

				GCOLOR.UI().border().render(r, body);
				GCOLOR.UI().bg().render(r, body, -1);
				COLOR.WHITE35.bind();
				
				all.getC(current-1).appearance().icon.renderC(r, body.cX()-18, body.cY());
				all.getC(current+1).appearance().icon.renderC(r, body.cX()+18, body.cY());
				COLOR.unbind();
				all.getC(current).appearance().iconBig.renderC(r, body.cX(), body.cY());
				
				
				
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				UIPickerRace.this.hover((GBox) text, all.getC(current));
			}
			
		});
		
		
		b = new GButt.ButtPanel(UI.icons().m.arrow_right) {
			@Override
			protected void clickA() {
				set(current+1);
				super.clickA();
			}
		};
		b.body.setHeight(46);
		b.pad(1, 0);
		section.addRightC(0, b);
	}
	
	public void set(Race race) {
		for (int i = 0; i < all.size(); i++) {
			if (all.get(i) == race)
				current = i;
		}
	}
	
	public void set(int ri) {
		current = ri;
		current = MATH.mod(current, all.size());
	}
	
	public void hover(GBox b, Race race) {
		b.title(race.info.names);
		b.text(race.info.desc);
		b.NL();
	}
	
	public Race race() {
		return all.getC(current);
	}
	
	
}
