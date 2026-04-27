package view.tool;

import init.sprite.SPRITES;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.sprite.SPRITE;
import util.colors.GCOLOR;
import util.gui.misc.GBox;
import view.subview.GameWindow;

public abstract class PlacableSimple implements PLACABLE{

	private final CharSequence name;
	private final CharSequence desc;
	private final PLACABLE undo;
	
	public PlacableSimple(CharSequence name){
		this(name, null, null);
	}
	
	public PlacableSimple(CharSequence name, CharSequence desc){
		this(name, desc, null);
	}
	
	public PlacableSimple(CharSequence name, CharSequence desc, PLACABLE undo){
		this.name = name;
		this.desc = desc;
		this.undo = undo;
	}
	
	@Override
	public SPRITE getIcon() {
		return SPRITES.icons().m.cancel;
	}

	@Override
	public CharSequence name() {
		return name;
	}

	@Override
	public PLACABLE getUndo() {
		return undo;
	}
	
	@Override
	public void hoverDesc(GBox box) {
		box.title(name);
		box.text(desc);
	}
	
	public abstract CharSequence isPlacable(int x, int y);
	public abstract void place(int x, int y);
	
	public void renderPlaceHolder(SPRITE_RENDERER r, int cx, int cy, boolean isPlacable) {
		if (!isPlacable)
			GCOLOR.MAP().OK.bind();
		else
			GCOLOR.MAP().BAD.bind();
		SPRITES.cons().BIG.dashedThick.get(0).renderC(r, cx, cy);
		COLOR.unbind();
	}


	public void renderOverlay(int x, int y, SPRITE_RENDERER r, float ds, GameWindow window) {
		
	}

	public void placeInfo(GBox hoverBox, int cx, int cy) {
		
		
	}
	
	public void renderAction(int cx, int cy) {
		
	}
	


}
