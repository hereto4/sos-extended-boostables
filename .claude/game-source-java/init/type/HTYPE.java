package init.type;

import snake2d.util.color.COLOR;
import snake2d.util.sets.LISTE;
import snake2d.util.sprite.SPRITE;
import util.info.INFO;
import util.keymap.MAPPED;

public final class HTYPE extends INFO implements MAPPED {

	HTYPE(LISTE<HTYPE> all, String key, HCLASS c, CharSequence name, CharSequence names, CharSequence desc, boolean player, boolean works, boolean hostile, COLOR color, SPRITE icon){
		this(all, key, c, name, names, desc, player, works, hostile, player, color, icon);
	}
	
	HTYPE(LISTE<HTYPE> all, String key, HCLASS c, CharSequence name, CharSequence names, CharSequence desc, boolean player, boolean works, boolean hostile, boolean visible, COLOR color, SPRITE icon){
		super(name, names, desc, null);
		this.player = player;
		this.works = works;
		this.hostile = hostile;
		this.color = color;
		this.visible = visible;
		this.CLASS = c;
		this.key = key;
		this.icon = icon;
		index = all.add(this);
	}
	
	private final int index;
	public final String key;
	public final boolean player;
	public final boolean hostile;
	public final boolean works;
	public final boolean visible;
	public final COLOR color;
	public final HCLASS CLASS;
	public final SPRITE icon;
	
	@Override
	public String toString() {
		return ""+name;
	}
	
	@Override
	public int index() {
		return index;
	}

	@Override
	public String key() {
		return key;
	};
}
