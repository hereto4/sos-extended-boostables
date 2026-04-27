package util.gui.misc;

import snake2d.MButt;
import snake2d.Mouse;
import snake2d.SPRITE_RENDERER;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.sprite.text.Str;
import snake2d.util.sprite.text.StringInputSprite;
import util.colors.GCOLOR;
import view.main.VIEW;

public class GInput extends CLICKABLE.ClickableAbs {

	private final StringInputSprite input;
	private boolean dragging = false;
	
	public GInput(StringInputSprite input){
		this.input = input;
		
		int w = input.font().maxCWidth*(input.text().length() + input.text().spaceLeft()) ;
		body.setWidth(w+12);
		body.setHeight(input.height()+12);
		//input.text().clear();
	}
	
	@Override
	protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
		
		GCOLOR.UI().bg(isActive, isSelected, isHovered).render(r, body);
		
		
		input.renAction();
		if (Mouse.currentClicked == this)
			input.listen();
		
		if (isHovered || Mouse.currentClicked == this) {
			GCOLOR.UI().NORMAL.hovered.render(r, body());
		}
		
		int x1 = body().x1()+6;
		int y1 = body().y1() + (body().height()-input.height())/2;
		
		dragging &= MButt.LEFT.isDown();
		
		if (dragging) {
			input.select(VIEW.mouse().x()-x1);
		}
		
		input.render(r, x1, y1);
		
		GCOLOR.UI().border().renderFrame(r, body, 0, 2);

	}
	
	@Override
	public boolean click() {
		if (super.click()) {
			Mouse.currentClicked = this;
			if (!input.listening() || MButt.LEFT.isDouble()) {
				input.listen();
				input.selectAll();
				dragging = false;
			}else {
				dragging = true;
			
				input.click(VIEW.mouse().x()-body().x1()-6);
				return true;
			}
			
			
			
		}
		return false;
	}
	
	public void focus() {
		Mouse.currentClicked = this;
		input.listen();
		input.selectAll();
	}
	
	public void listen() {
		Mouse.currentClicked = this;
		input.listen();
	}
	
	public Str text() {
		return input.text();
	}
	

	

}
