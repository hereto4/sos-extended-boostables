package world.region;

import game.boosting.BOOSTING;
import game.boosting.BSourceInfo;
import game.faction.npc.FactionNPC;
import game.time.TIME;
import init.resources.RESOURCES;
import init.sprite.UI.UI;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import util.data.INT_O.INT_OE;
import util.text.D;
import world.map.regions.Region;
import world.region.RD.RDInit;
import world.region.RD.RDUpdatable;
import world.region.RDOutputs.RDOutput;
import world.region.pop.RDRace;

public class RDDevastation {

	private static CharSequence ¤¤Name = "¤Devastation";
	private static CharSequence ¤¤Desc = "¤Devastation comes from military actions. Devastated regions produce less, and have slower population growth. Devastation takes 2 years to subside.";
	
	static {
		D.ts(RDDevastation.class);
	}
	
	private static final double dTime = 1.0/(TIME.secondsPerDay()*32);
	public final INT_OE<Region> current;
	
	
	RDDevastation(RDInit init){
		current = init.count.new DataShort("DEVASTATION", ¤¤Name, ¤¤Desc);
		

		BOOSTING.connecter(new ACTION() {
			
			@Override
			public void exe() {
				RBooster b = new RBooster(new BSourceInfo(¤¤Name, UI.icons().s.heat), 0.25, 1.0, true) {

					@Override
					public double get(Region t) {
						return 1.0 - current.getD(t);
					}
				
				};
				b.add(RD.RACES().capacity);
				
				for (RDRace r : RD.RACES().all)
					b.add(r.pop.growth);
				
				
				for (RDOutput o : RD.OUTPUT().ALL) {
					b.add(o.boost);
					b.add(o.boostYearlyPart);
				}
				
			}
		});
		
		init.upers.add(new RDUpdatable() {
			
			@Override
			public void update(Region reg, double time) {
				if (reg.faction() != null)
					current.incFraction(reg, -current.max(reg)*time*dTime);
			}
			
			@Override
			public void init(Region reg) {
				current.set(reg, 0);
			}
		});
		
	}
	
	public int raidCredits(Region reg) {
		double pop = RD.RACES().population.get(reg);
		double d = pop*RESOURCES.ALL().size();
		if (reg.faction() != null && reg.faction() instanceof FactionNPC) {
			FactionNPC f = (FactionNPC) reg.faction();
			d *= 1 + CLAMP.d(f.credits().credits()/RD.RACES().population.faction().get(f), 0, 100);
		}
		return (int) (d * (1.0-current.getD(reg)));
		
	}


	
}