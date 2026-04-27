package world.region;

import game.boosting.BOOSTING;
import game.boosting.BSourceInfo;
import game.boosting.BValue;
import game.boosting.BoostSpec;
import game.boosting.BoostSpecs;
import game.boosting.Boostable;
import game.boosting.BoostableCat;
import game.boosting.BoosterValue;
import game.faction.npc.FactionNPC;
import game.faction.player.Player;
import game.time.TIME;
import init.religion.Religion;
import init.religion.RELIGIONS;
import init.sprite.UI.UI;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LIST;
import util.data.INT_O.INT_OE;
import util.text.D;
import world.map.regions.Region;
import world.region.RD.RDInit;
import world.region.RD.RDUpdatable;
import world.region.pop.RDRace;

public class RDReligions {

	private final ArrayListGrower<RDReligion> all = new ArrayListGrower<>();
	public final INT_OE<Region> opposition;
	private static CharSequence ¤¤Opposition = "Religious differences";
	
	static {
		D.ts(RDReligions.class);
	}
	
	RDReligions(RDInit init){
		
		opposition = init.count.new DataByte("REL_OPPOSITION");
		
		for (Religion r : RELIGIONS.ALL()) {
			all.add(new RDReligion(init, r));
		}
		
		BOOSTING.connecter(new ACTION() {
			
			@Override
			public void exe() {

				
				RBooster lo = new RBooster(new BSourceInfo(¤¤Opposition, UI.icons().s.heat), 1.0, 0.75, true) {
					
					@Override
					public double get(Region t) {
						return opposition.getD(t);
					}
				};
				
				for (RDRace race : RD.RACES().all) {
					lo.add(race.loyalty.target);
				}
				
			}
		});
		
		init.upers.add(new RDUpdatable() {
			
			final double dt = 1.0/(TIME.secondsPerDay()*16);
			
			@Override
			public void update(Region reg, double time) {
				
				double min = min(reg);
				double tot = tot(reg)-min;
				
				for (RDReligion r : all) {
					double target = Math.round(0x0FF*(r.boost.get(reg)-min)/tot);
					double now = target + dt*(target-r.current.get(reg));
					now = CLAMP.d(now, Math.min(target, r.current.get(reg)), Math.max(target, r.current.get(reg)));
					r.current.set(reg, (int) now);
				}
				
				setop(reg);
				
			}
			
			@Override
			public void init(Region reg) {
				double min = min(reg);
				double tot = tot(reg)-min;
				for (RDReligion r : all) {
					double target = Math.round(0x0FF*(r.boost.get(reg)-min)/tot);
					r.current.set(reg, (int) target);
				}
				setop(reg);
			}
			

			
			private void setop(Region reg) {
				double op = 0;
				
				for (int ri = 0; ri < all.size(); ri++) {
					double vv = 0;
					RDReligion r = all.get(ri);
					for (int ri2 = 0; ri2 < all.size(); ri2++) {
						RDReligion r2 = all.get(ri2);
						double am = r2.current.getD(reg);
						am *= r.religion.opposition(r2.religion);
						vv += am;
					}
					op += vv*r.current.getD(reg);
				}
				
				op = CLAMP.d(op, 0, 1);
				opposition.setD(reg, op);
			}
		});
		
	}
	
	private double min(Region reg) {
		double mi = 0;
		for (RDReligion r : all) {
			mi = Math.min(mi, r.boost.get(reg));
		}
		return mi;
	}
	
	private double tot(Region reg) {
		double tot = 0;
		for (RDReligion r : all) {
			tot += Math.max(r.boost.get(reg), 0);
		}
		return tot;
	}
	
	public LIST<RDReligion> all(){
		return all;
	}
	
	public RDReligion get(Religion t) {
		return all.get(t.index());
	}
	
	
	public class RDReligion {
		
		public final Boostable boost;
		public final BoostSpecs boosts;
		public final Religion religion;
		public final INT_OE<Region> current;
		
		private RDReligion(RDInit init, Religion reg) {
			boosts = new BoostSpecs(reg.info.name, reg.icon, true);
			religion =  reg;
			current = init.count.new DataByte("REL" + reg.key);
			boost = BOOSTING.push("CONVERSION_" + reg.key, reg.inclination, reg.info.name, reg.info.desc, reg.icon, BoostableCat.ALL().WORLD_CIVICS);
			BOOSTING.connecter(new ACTION() {
				
				@Override
				public void exe() {
					
					
					for (BoostSpec s : reg.boosts.all()) {
						
						
						
						if ((s.boostable.cat.typeMask & BoostableCat.TYPE_WORLD) != 0) {
							BoosterValue b = new BoosterValue(new value(s.boostable), s.booster.info, s.booster.to(), s.booster.isMul);
							boosts.push(b, s.boostable);
						}
					}
					
					
				}
			});
		}
		
		public double target(Region reg) {
			double tot = 0;
			for (RDReligion r : all) {
				tot += r.boost.get(reg);
			}
			return boost.get(reg)/tot;
		}
		
		public class value extends BValue.BValueFaction{

			value(Boostable bo){
				super(bo);
			}
			
			@Override
			public double vGet(Region reg) {
				return current.getD(reg);
			}

			@Override
			public double vGet(Player f) {
				double d = 0;
				for (int i = 0; i < f.realm().regions(); i++) {
					Region reg = f.realm().region(i);
					d += current.getD(reg)*RD.RACES().population.get(reg);
				}
				d /= RD.RACES().population.faction().get(f);
				return d;
			}

			@Override
			public double vGet(FactionNPC f) {
				return 0;
			}
			
			
		}
		
	}

	

	
	
}
