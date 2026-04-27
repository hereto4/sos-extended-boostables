package world.region;

import game.boosting.BOOSTING;
import game.boosting.BSourceInfo;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.npc.FactionNPC;
import game.time.TIME;
import init.sprite.UI.UI;
import snake2d.util.misc.ACTION;
import util.data.INT_O.INT_OE;
import util.text.D;
import world.map.regions.Region;
import world.region.RD.RDInit;
import world.region.RD.RDUpdatable;
import world.region.pop.RDRace;

public class RDOwner implements RDUpdatable {

	private static CharSequence ¤¤Affiliation = "¤Support";
	private static CharSequence ¤¤AffiliationD = "¤Support towards your majesty. Low support increases the chance of rebellion. When a region is controlled, support will increase with time. For other regions, emissaries can be sent to increase support.";
	
	static {
		D.ts(RDOwner.class);
	}
	
	private static final double dTime = 1.0/(TIME.secondsPerDay()*8);
	public final INT_OE<Region> affiliation;
	
	private final INT_OE<Region> prevOwner;
	private final INT_OE<Region> prevOwnerII;
	public final INT_OE<Region> ownerI;
	
	RDOwner(RDInit init){
		affiliation = init.count.new DataByte("OWNER", ¤¤Affiliation, ¤¤AffiliationD);
		prevOwner = init.count.new DataShort("PREVOWVER");
		prevOwnerII = init.count.new DataNibble("PREVOWNER2");
		ownerI = init.count.new DataByte("OWNERI");
		init.upers.add(this);
		
		
		
		BOOSTING.connecter(new ACTION() {
			
			@Override
			public void exe() {
				RBooster b = new RBooster(new BSourceInfo(¤¤Affiliation, UI.icons().s.happy), 0, 1, true) {

					@Override
					public double get(Region t) {
						if (t.faction() == FACTIONS.player())
							return affiliation.getD(t);
						return 1;
					}
				
				};
				
				for (RDRace r : RD.RACES().all) {
					b.add(r.loyalty.target);
				}
			}
		});
		
	}

	@Override
	public void update(Region reg, double time) {
		int tar = 255;
		double d = 255.0*time*dTime;
		if (reg.faction() == FACTIONS.player()) {
			tar = 255;
			affiliation.moveTo(reg, d, tar);
		}else if (reg.faction() != FACTIONS.player()) {
			double dd = FACTIONS.player().emissaries.assimilate.getD(reg)*FACTIONS.player().emissaries.penaltyMul();;
			if (dd <= 0) {
				affiliation.moveTo(reg, d*0.25, 0);
			}else
				affiliation.moveTo(reg, 255*time*dTime*dd, 255);
			
		}
		
		
		if (prevOwner(reg) == null) {
			Faction ff = reg.faction();
			if (ff != null) {
				prevOwner.set(reg, ff.index()+1);
				if (ff instanceof FactionNPC)
					prevOwnerII.set(reg, ((FactionNPC) ff).iteration()&0x0F);
			}
			
		}
	}
	
	@Override
	public void init(Region reg) {
		// TODO Auto-generated method stub
		
	}
	
	public Faction prevOwner(Region reg) {
		int i = prevOwner.get(reg);
		if (i != 0) {
			Faction f = FACTIONS.getByIndex(i-1);
			if (f == null || !f.isActive() || (f instanceof FactionNPC && prevOwnerII.get(reg) != (((FactionNPC) f).iteration()&0x0F))) {
				prevOwner.set(reg, 0);
				return null;
			}
				
			return f;
		}
		return null;
	}


	
}