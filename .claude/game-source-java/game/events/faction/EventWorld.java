package game.events.faction;

import game.events.faction.player.EventDiplomacy;

public final class EventWorld {
	
	public final EventFactionExpand factionExpand = new EventFactionExpand();
	public final EventFactionCollapse factionBreak = new EventFactionCollapse();
	public final EventFactionPopup popup = new EventFactionPopup();
	public final EventFactionWar war = new EventFactionWar();
	public final EventFactionPeace warPeace = new EventFactionPeace();
	public final EventDiplomacy dip = new EventDiplomacy();
}
