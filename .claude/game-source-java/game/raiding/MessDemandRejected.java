package game.raiding;

import init.sprite.UI.UI;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import util.gui.misc.GText;
import util.text.D;
import view.ui.message.MessageSection;

final class MessDemandRejected extends MessageSection{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static CharSequence ¤¤title = "Raider Rejected";
	private static CharSequence ¤¤body = "This is ill news for us indeed. Perhaps we should have paid the ransom. Now we must prepare for an attack.";
	
	static {
		D.ts(MessDemandRejected.class);
	}
	
	private final Raider raider;
	
	public MessDemandRejected(Raider raider) {
		super(¤¤title);
		this.raider = raider;
	}

	@Override
	protected void make(GuiSection section) {
		
		for (String s : raider.text.rejected)
			paragraph(s);
		
		section.addRelBody(16, DIR.S, new GText(UI.FONT().S, ¤¤body).lablifySub().setMaxWidth(WIDTH));
		
		section.addRelBody(32, DIR.N, new RaiderPortrait(4).set(raider));
	}

}
