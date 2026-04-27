package util.colors;

import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.diplomacy.DIP;
import game.faction.npc.FactionNPC;
import init.paths.PATHS;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.color.ColorShifting;
import snake2d.util.file.Json;

public final class COLOR_MAP {
	
	private Json d = new Json(PATHS.SPRITE_UI().getLikeHell("Colors.txt")).json("MAP");
	
	public final COLOR DORMANT = new ColorImp(d, ("DORMANT"));
	public final COLOR BAD = new ColorImp(d, ("BAD"));
	public final COLOR SOSO  = new ColorImp(d, ("SOSO"));
	public final COLOR OK = new ColorImp(d, ("OK"));
	public final COLOR BETTER = new ColorImp(d, ("BETTER"));
	
	public final COLOR OK_2_BETTER = new ColorShifting(OK, BETTER);
	
	public final COLOR BEST = new ColorImp(d, ("BEST"));
	public final COLOR BEST_DARK = BEST.shade(0.75);
	
	public final COLOR JOB_DORMANT = DORMANT;
	public final COLOR JOB_ACTIVE = OK;
	public final COLOR JOB_RESERVED = BETTER;
	public final COLOR JOB_BLOCKED = JOB_ACTIVE.shade(0.75);
	
	public final COLOR BATTLE_DORMANT = DORMANT;
	public final COLOR BATTLE_OK = OK;
	
	public final COLOR OVERLAY_GOOD = new ColorImp(d, ("OVERLAY_GOOD"));
	public final COLOR OVERLAY_BAD = new ColorImp(d, ("OVERLAY_BAD"));
	
	public final COLOR F_PLAYER = new ColorImp(d, ("F_PLAYER"));
	public final COLOR F_ALLY = new ColorImp(d, ("F_ALLY"));
	public final COLOR F_NEAUTRAL = new ColorImp(d, ("F_NEAUTRAL"));
	public final COLOR F_ENEMY = new ColorImp(d, ("F_ENEMY"));
	public final COLOR F_REBEL = new ColorImp(d, ("F_REBEL"));
	
	public COLOR get(Faction f) {
		if (f == null)
			return F_REBEL;
		if (f == FACTIONS.player())
			return F_PLAYER;
		if (DIP.get((FactionNPC)f).ally)
			return F_ALLY;
		if (!DIP.WAR().is(FACTIONS.player(), f))
			return F_NEAUTRAL;
		else
			return F_ENEMY;
	}

	
}
