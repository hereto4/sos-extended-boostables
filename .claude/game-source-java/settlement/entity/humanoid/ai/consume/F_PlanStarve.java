package settlement.entity.humanoid.ai.consume;

import static settlement.main.SETT.TERRAIN;

import init.type.CAUSE_LEAVES;
import init.type.NEEDS;
import settlement.entity.animal.ANIMAL_ROOM_RUINER;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIData.AIDataSuspender;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AIPLAN;
import settlement.entity.humanoid.ai.main.AISUB;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.path.finders.SFinderMisc.FinderMiscWithoutDest;
import settlement.room.main.Room;
import settlement.stats.STATS;
import settlement.thing.THINGS.Thing;
import settlement.thing.ThingsCorpses.Corpse;
import settlement.tilemap.terrain.TGrowable;
import util.text.D;

final class F_PlanStarve extends AIPLAN.PLANRES{

	private final AISUB sub;
	private final AIDataSuspender suspender;
	
	private static CharSequence ¤¤cannibal = "Eating a corpse";
	private static CharSequence ¤¤starving = "Starving";
	private static CharSequence ¤¤eating = "Eating Dirt";
	
	static {
		D.ts(F_PlanStarve.class);
	}
	
	
	public F_PlanStarve(AISUB sub, AIDataSuspender suspender) {
		super("dangerStarve");
		this.sub = sub;
		this.suspender = suspender;
		// TODO Auto-generated constructor stub
	}

	
	public final FinderMiscWithoutDest edible = new FinderMiscWithoutDest(32) {
		
		@Override
		protected boolean has() {
			return SETT.WEATHER().growthRipe.cropsAreRipe();
		};
		
		@Override
		public boolean isTile(int tx, int ty) {
			Room r = SETT.ROOMS().map.get(tx, ty);
			if (r != null && r instanceof ANIMAL_ROOM_RUINER) {
				return ((ANIMAL_ROOM_RUINER)r).canBeGraced(tx, ty);
			}
			return (TERRAIN().get(tx, ty) instanceof TGrowable) && ((TGrowable)TERRAIN().get(tx, ty)).isEdible(tx, ty) && ((TGrowable)TERRAIN().get(tx, ty)).size.get(tx, ty) > 0;
		}
	};
	
	private Corpse corpse(int tx, int ty) {
		for (Thing t : SETT.THINGS().get(tx, ty))
			if (t instanceof Corpse) {
				Corpse c = (Corpse) t;
				if (c.hasMeat())
					return c;
			}
		return null;
	}
	
	public final FinderMiscWithoutDest corpses = new FinderMiscWithoutDest(32) {
		
		@Override
		protected boolean has() {
			return true;
		};
		
		@Override
		public boolean isTile(int tx, int ty) {
			return corpse(tx, ty) != null;
		}
	};

	@Override
	protected AISubActivation init(Humanoid a, AIManager d) {
		if (NEEDS.TYPES().HUNGER.stat().stat().indu().isMax(a.indu()))
			AIManager.dead = CAUSE_LEAVES.STARVED();
		
		if (!suspender.is(d)) {
			if (edible.find(a.physics.tileC(), d.path)) {
				return goEatTerrain.set(a, d);
			}
			if (corpses.find(a.physics.tileC(), d.path)) {
				return goEatCorpse.set(a, d);
			}
			suspender.suspend(d);
		}
		
		
		
		
		//misery
		return actCrazy.set(a, d);
	}
	
	private final Resumer goEatCorpse = new Resumer(¤¤cannibal) {
		
		@Override
		public AISubActivation setAction(Humanoid a, AIManager d) {
			return AI.SUBS().walkTo.pathRun(a, d);
		};
		
		@Override
		public AISubActivation res(Humanoid a, AIManager d) {
			return eatCorpse.set(a, d);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return corpse(d.path.destX(), d.path.destY()) != null;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {

		}
	};
	
	private final Resumer eatCorpse = new Resumer(¤¤cannibal) {
		
		@Override
		public AISubActivation setAction(Humanoid a, AIManager d) {
			return sub.activate(a, d);
		};
		
		@Override
		public AISubActivation res(Humanoid a, AIManager d) {
			Corpse c = corpse(d.path.destX(), d.path.destY());
			if (c != null) {
				SETT.ROOMS().CANNIBAL.reportCannibal();
				c.removeMeat();
				STATS.FOOD().eat(a, 0, 0);
				NEEDS.TYPES().HUNGER.stat().fix(a.indu());
				return null;
			}
			//kill other here
			return null;
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return true;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			
		}
	};
	
	private final Resumer goEatTerrain = new Resumer(¤¤eating) {
		
		@Override
		public AISubActivation setAction(Humanoid a, AIManager d) {
			return AI.SUBS().walkTo.pathRun(a, d);
		};
		
		@Override
		public AISubActivation res(Humanoid a, AIManager d) {
			return eatTerrain.set(a, d);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return edible.isTile(d.path.destX(), d.path.destY());
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {

		}
	};
	
	private final Resumer eatTerrain = new Resumer(¤¤eating) {
		
		@Override
		public AISubActivation setAction(Humanoid a, AIManager d) {
			
			return sub.activate(a, d);
		};
		
		@Override
		public AISubActivation res(Humanoid a, AIManager d) {
			if (edible.isTile(d.path.destX(), d.path.destY())) {
				STATS.FOOD().eat(a, 0, 0);
				NEEDS.TYPES().HUNGER.stat().fix(a.indu());
				TERRAIN().get(d.path.destX(), d.path.destY()).clearing().clear1(d.path.destX(), d.path.destY());
				Room r = SETT.ROOMS().map.get(d.path.destX(), d.path.destY());
				if (r != null && r.destroyTileCan(d.path.destX(), d.path.destY()))
					r.destroyTile(d.path.destX(), d.path.destY());
				
				return null;
			}
			//kill other here
			return null;
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return true;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			
		}
	};
	
	private final Resumer actCrazy = new Resumer(¤¤starving) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			return AI.SUBS().desperate.activate(a, d);
			
		};
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			return null;
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return true;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			
		}
	};
	
}
