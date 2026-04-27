package game.raiding;

import game.faction.FACTIONS;
import game.faction.FCredits.CTYPE;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.sprite.text.Str;
import util.text.D;
import view.ui.message.MessageSection;

final class MessDefeated extends MessageSection{

	private static CharSequence ¤¤title = "Raider Defeated";
	private static CharSequence ¤¤desc = "¤The once mighty {0} lies dead at your feet, oh mighty one. This will surely send a powerful message throughout all of Syx.";
	private static CharSequence ¤¤loot = "¤In addition, {HIS} personal treasury has been found and looted by our men. A total of {0} denari was found and has been transported to our treasury.";
	static {
		D.ts(MessDefeated.class);
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final Raider raider;
	
	public MessDefeated(Raider raider) {
		super(¤¤title);
		this.raider = raider;
		if (raider.bounty > 0)
			FACTIONS.player().credits().inc(raider.bounty, CTYPE.MISC);
	}

	@Override
	protected void make(GuiSection section) {
		
		paragraph(Str.TMP.clear().add(¤¤desc).insert(0, raider.name));
		if (raider.bounty > 0) {
			Str.TMP.clear().add(¤¤loot);
			Str.TMP.insert("HIS", raider.indu.race().info.pHIS.get(raider.indu, false));
			Str.TMP.insert(0, Str.TMP2.clear().add((long)raider.bounty, true));
			paragraph(Str.TMP);
		}
		
		section.addRelBody(32, DIR.N, new RaiderPortrait(4).set(raider).dead(true));
		
	}

}
