package game.faction.diplomacy.deal;

import game.faction.FACTIONS;
import game.faction.diplomacy.deal.DealRegs.DealReg;
import game.faction.npc.FactionNPC;
import game.faction.royalty.opinion.ROPINIONS;
import init.race.RACES;
import init.race.Race;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import util.gui.misc.GBox;
import view.main.VIEW;

public final class Deal {
	
	public final DealParty player;
	public final DealParty npc;
	public final DealBools bools;
	public boolean clearDeal;

	public Deal(){
		DealRegs.RegData data = new DealRegs.RegData();
		player = new DealParty(this, data);
		npc = new DealParty(this, data);
		bools = new DealBools(player, npc);
	}
	
	public void setFactionAndClear(FactionNPC faction) {
		setFactionAndClear(faction, true);
	}
	
	public void setFactionAndClear(FactionNPC faction, boolean clearDeal) {
		this.clearDeal = clearDeal;
		player.init(FACTIONS.player(), faction, faction);
		npc.init(faction, FACTIONS.player(), faction);
		bools.init(true, clearDeal);
		dupI = -1;
	}
	
	public boolean canBeAccepted() {
		return hasDeal() && ((int)valueCredits() >= 0 || can);
	}

	public double execute(boolean changeOpinion) {
		
		double v = opinionChange();
		player.execute();
		npc.execute();
		bools.execute();
		
		if (changeOpinion && player.f() == FACTIONS.player())
			ROPINIONS.GIFTS().makeDeal(npc.npc(), v);
		if (npc.npc().isActive())
			setFactionAndClear(npc.npc(), clearDeal);
		
		return v;
	}
	
	int dupI = -1;
	private int cvalue;
	private boolean can = false;
	
	public double valueCredits() {
		
		
		
//		if (dupI == VIEW.RI())
//			return cvalue;
		
		can = false;
		dupI = VIEW.RI();
		
		cvalue = (int) bools.value();
		cvalue += player.value();
		cvalue -= npc.value();
		
		return cvalue;
	}
	
	public boolean hasDeal() {
		for (DealBool b : bools.all())
			if (b.is())
				return true;
		return has(npc) || has(player);
	}
	
	public boolean has(DealParty p) {
		if (p.credits.get() != 0)
			return true;
		for (DealReg r : player.regs.all()) {
			if (r.is())
				return true;
		}
		for (RESOURCE r : RESOURCES.ALL())
			if (p.resources.get(r) != 0)
				return true;
		for (Race r : RACES.all())
			if (p.slaves.get(r) != 0)
				return true;
		return false;
	}
	
	public double opinionChange() {
		return opinionChangeD();
	}
	
	public double opinionChangeD() {
		
		double c = 25*valueCredits();
		c /= npc.selfWorth();
		
		return c;
	}
	
	public double getWorthOfOpinion(double opinion) {
		return opinion * npc.selfWorth()/25.0;
	}
	
	public double betrayal() {
		return bools.betrayal();
	}

	public void hoverBetrayal(GBox b) {
		bools.betrayalHover(b);
		
	}

	
}
