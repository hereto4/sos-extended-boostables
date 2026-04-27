package view.tool;

import init.sprite.SPRITES;
import snake2d.SPRITE_RENDERER;
import snake2d.util.sprite.SPRITE;
import util.gui.misc.GBox;

public abstract class PlacableSingle implements PLACABLE{

	private final CharSequence name;
	private final CharSequence desc;
	private final PLACABLE undo;
	PLACER_TYPE previous; 
	
	public PlacableSingle(CharSequence name){
		this(name, null, null);
	}
	
	public PlacableSingle(CharSequence name, CharSequence desc){
		this(name, desc, null);
	}
	
	public PlacableSingle(CharSequence name, CharSequence desc, PLACABLE undo){
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
		if (name != null)
		box.title(name);
		if (desc != null)
		box.text(desc);
	}
	
	public boolean expandsTo(int fromX, int fromY, int toX, int toY) {
		return false;
	}
	
	public abstract CharSequence isPlacable(int tx, int ty);
	public abstract void placeFirst(int tx, int ty);
	public void placeExpanded(int tx, int ty) {
		
	}
	
	public void renderPlaceHolder(SPRITE_RENDERER r, int mask, int x, int y, int tx, int ty, boolean isPlacable) {
		SPRITES.cons().BIG.dashedThick.render(r, mask, x, y);
	}
	
	public void placeInfo(GBox b, int tiles) {
		
	}

	protected void init(int tx, int ty) {
		
	}
	


}
