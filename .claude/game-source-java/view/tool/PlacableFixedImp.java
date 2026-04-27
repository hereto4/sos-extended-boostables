package view.tool;

import init.sprite.SPRITES;
import snake2d.util.sprite.SPRITE;
import util.gui.misc.GBox;

public abstract class PlacableFixedImp extends PlacableFixed{

	private final CharSequence name;
	private final CharSequence desc;
	private final SPRITE icon;
	private final PLACABLE undo;
	private final int rots;
	private final int sizes;
	
	public PlacableFixedImp(CharSequence name, int rots, int sizes){
		this(name, rots, sizes, null, null, null);
	}
	
	public PlacableFixedImp(CharSequence name, int rots, int sizes, CharSequence desc, SPRITE icon){
		this(name, rots, sizes, desc, icon, null);
	}
	
	public PlacableFixedImp(CharSequence name, int rots, int sizes, CharSequence desc, SPRITE icon, PLACABLE undo){
		this.name = name;
		this.desc = desc;
		if (icon == null)
			icon = SPRITES.icons().m.cancel;
		this.icon = icon;
		this.undo = undo;
		this.rots = rots;
		this.sizes = sizes;
	}
	
	@Override
	public CharSequence placableWhole(int tx1, int ty1) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public SPRITE getIcon() {
		return icon;
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
		if (name() != null)
			box.title(name());
		if (desc != null)
			box.text(desc);
	}
	
	@Override
	public final int rotations() {
		return rots;
	}
	@Override
	public final int sizes() {
		return sizes;
	}
	


}
