package game.faction.diplomacy;

import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.diplomacy.DIP.DipActivityListener;
import init.sprite.UI.UI;
import snake2d.util.sprite.text.Str;
import util.text.D;
import view.ui.message.MessageText;
import world.WORLD;
import world.region.RD;

final class Listener extends DipActivityListener{

	private static CharSequence ¤¤warDeclared = "The realm of {0} has declared war on {1}.";
	private static CharSequence ¤¤warPeace = "{0} and {1} have agreed to a truce.";
	
	private static CharSequence ¤¤mTitle = "Distant war.";
	private static CharSequence ¤¤mBody = "One of your neighbours have gone to war. This could be an opportunity to snatch a cheap alliance, or join one of the sides to take part of the spoils.";
	
	private static CharSequence ¤¤trade = "{FACTION_A} and {FACTION_B} are now trade partners.";
	
	static {
		D.ts(Listener.class);
	}
	
	@Override
	public void change(Faction faction, Faction other, DipStance old, DipStance nn) {
		if (nn == DIP.WAR() || old == DIP.WAR()) {
			Str.TMP.clear().add(nn == DIP.WAR() ? ¤¤warDeclared : ¤¤warPeace);
			Str.TMP.insert(0, faction.name);
			Str.TMP.insert(1, other.name);
			WORLD.LOG().log(faction, other, UI.icons().s.sword, Str.TMP, other.cx(), other.cy());
			if (other != FACTIONS.player() && faction != FACTIONS.player() && RD.DIST().factionHasRegionBorderingPlayer(other) || RD.DIST().factionHasRegionBorderingPlayer(faction)) {
				new MessageText(¤¤mTitle).paragraph(¤¤mBody).send();
			}
		}else if (!old.trades && nn.trades) {
			Str.TMP.clear().add(¤¤trade);
			Str.TMP.insert(0, faction.name);
			Str.TMP.insert(1, other.name);
			if (other.capitolRegion() != null)
				WORLD.LOG().log(faction, other, UI.icons().s.trade, Str.TMP, other.cx(), other.cy());
		}
		
	}

}
