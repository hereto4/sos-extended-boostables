package init.type;

import snake2d.util.sets.LISTE;
import util.info.INFO;
import util.keymap.MAPPED;

public final class CAUSE_ARRIVE extends INFO implements MAPPED{

	private final int index;
	public boolean fromoutside;
	private final String key;
	
	CAUSE_ARRIVE(LISTE<CAUSE_ARRIVE> all, String key, CharSequence name, CharSequence desc, boolean fromOutside) {
		super(name, desc);
		index = all.add(this);
		this.fromoutside = fromOutside;
		this.key = key;
	}

	@Override
	public int index() {
		return index;
	}

	@Override
	public String key() {
		return key;
	}
	
	
}
