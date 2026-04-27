package util.gui.misc;

import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.DIR;
import snake2d.util.sprite.text.Font;
import snake2d.util.sprite.text.Text;
import util.colors.GCOLOR;

public class GText extends Text{

	private ColorImp color = new ColorImp(GCOLOR.T().NORMAL);

	public GText(Font f, CharSequence text){
		super(f, text);
	}
	
	public GText(Font f, int length){
		super(f, length);
	}
	
	@Override
	public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
		color.bind();
		super.render(r, X1, X1+maxWidth, Y1, Y2);
		COLOR.unbind();
	}
	
	public COLOR color(){
		return color;
	}
	
	@Override
	public GText clear() {
		super.clear();
		return this;
	}
	
	public GText color(COLOR c){
		this.color.set(c);
		return this;
	}
	
	public GText lablify(){
		this.color.set(GCOLOR.T().H1);
		return this;
	}
	
	public GText lablifySub(){
		this.color.set(GCOLOR.T().H2);
		return this;
	}
	
	public GText normalify(){
		this.color.set(GCOLOR.T().NORMAL);
		return this;
	}
	
	public GText normalify2(){
		this.color.set(GCOLOR.T().NORMAL2);
		return this;
	}
	
	public GText selectify(){
		this.color.set(GCOLOR.T().HOVER_SELECTED);
		return this;
	}
	
	public GText hoverify(){
		this.color.set(GCOLOR.T().HOVERED);
		return this;
	}
	
	public GText clickify(){
		this.color.set(GCOLOR.T().CLICKABLE);
		return this;
	}
	
	public GText errorify() {
		this.color.set(GCOLOR.T().ERROR);
		return this;
	}
	
	public GText warnify() {
		this.color.set(GCOLOR.T().WARNING);
		return this;
	}
	
	public GText decrease() {
		setFont(UI.FONT().S);
		return this;
	}
	
	public GText increase() {
		setFont(UI.FONT().M);
		return this;
	}
	

	
	@Override
	public GText setMaxWidth(int max) {
		super.setMaxWidth(max);
		return this;
	}
	
	public final GTextR r(DIR alignment) {
		return new GTextR(this, alignment);
	}
	
	@Override
	public GText toCamel() {
		super.toCamel();
		return this;
	}

	@Override
	public GText toLower() {
		super.toLower();
		return this;
	}

	@Override
	public GText toUpper() {
		super.toUpper();
		this.adjustWidth();
		return this;
	}


}
