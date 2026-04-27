package game.raiding;

import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import util.text.D;
import view.ui.message.MessageSection;

final class MessVictory extends MessageSection{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static CharSequence ¤¤title = "Raider Raids";
	
	static {
		D.ts(MessVictory.class);
	}
	
	private final Raider raider;
	private String[] mm;
	
	public MessVictory(Raider raider) {
		super(¤¤title);
		this.raider = raider;
		mm = new String[raider.text.afterRaid.size()];
		int mi = 0;
		for (String s : raider.text.afterRaid)
			mm[mi++] = s;
	}

	@Override
	protected void make(GuiSection section) {
		for (String s : mm)
			paragraph(s);
		
		section.addRelBody(32, DIR.N, new RaiderPortrait(4).set(raider));
		
	}

}
