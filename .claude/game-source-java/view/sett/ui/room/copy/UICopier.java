package view.sett.ui.room.copy;

import view.main.VIEW;

public class UICopier {

	private final Source source = new Source();
	private final Dest dest = new Dest(source);
	private final Second second = new Second(dest);
	private final First first = new First(source);
	private final FirstConfig config = new FirstConfig(source, second, first);
	
	public UICopier() {
		
	}
	
	public void activate() {
		source.init();
		second.rotSet(0);
		VIEW.s().tools.place(first, config);
	}
	
}
