package game.raiding;

import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.sprite.text.Str;
import util.text.D;
import view.ui.message.MessageSection;

final class MessGoingAway extends MessageSection{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static CharSequence ¤¤title = "Raider Retreats";
	private static CharSequence ¤¤desc = "Due to unknown reasons, {0} has turned back {1} army and left our lands. Lets pray {2} never returns.";
	
	static {
		D.ts(MessGoingAway.class);
	}
	
	private final Raider raider;
	
	public MessGoingAway(Raider raider) {
		super(¤¤title);
		this.raider = raider;
	}

	@Override
	protected void make(GuiSection section) {
		Str s = Str.TMP.clear();
		s.add(¤¤desc);
		s.insert(0, raider.name);
		s.insert(1, raider.indu.race().info.pHIS.get(raider.indu, false));
		s.insert(2, raider.indu.race().info.pHE.get(raider.indu, false));
		paragraph(s);
		
		section.addRelBody(32, DIR.N, new RaiderPortrait(4).set(raider));
		
	}

}
