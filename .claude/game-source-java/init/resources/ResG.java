package init.resources;

import util.keymap.MAPPED;

public class ResG implements MAPPED{

	public final RESOURCE resource;
	private final int index;
	private final String key;
	
	ResG(int index, String key, RESOURCE r) {
		this.index = index;;
		resource = r;
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
