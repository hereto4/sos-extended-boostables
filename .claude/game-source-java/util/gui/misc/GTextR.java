package util.gui.misc;

import snake2d.util.datatypes.DIR;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.sprite.text.Font;

public class GTextR extends HOVERABLE.Sprite{

	private final GText text;

	public GTextR(GText text){
		this(text, DIR.C);
	}
	
	public GTextR(Font f, CharSequence text){
		this(new GText(f, text));
	}
	
	public GTextR(Font f, int width){
		this(new GText(f, width));
	}
	
	public GTextR(Font f, int width, DIR replacementStrat){
		this(new GText(f, width), replacementStrat);
	}
	
	public GTextR(GText text, DIR replacementStrat){
		super(text);
		setAlign(replacementStrat);
		this.text = text;
		text.adjustWidth();
		
	}
	
	public GText text(){
		return text;
	}
	
	@Override
	public void adjust() {
		super.adjust();
	}
	
	@Override
	public GTextR setAlign(DIR d) {
		super.setAlign(d);
		return this;
	}


}
