package settlement.entity.humanoid.ai.work;
import static settlement.main.SETT.THINGS;

import init.constant.C;
import init.type.HTYPES;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.room.law.execution.ExecutionStation;
import settlement.room.law.execution.ROOM_EXECTUTION;
import settlement.thing.ThingsCorpses;
import settlement.thing.ThingsCorpses.Corpse;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.rnd.RND;

final class WorkExecutioner extends PlanBlueprint {

	private final ROOM_EXECTUTION b = SETT.ROOMS().EXECUTION;
	
	protected WorkExecutioner(AIModule_Work module, PlanBlueprint[] map) {
		super(module, SETT.ROOMS().EXECUTION, map);
	}
	
	@Override
	protected AISubActivation init(Humanoid a, AIManager d) {
		ExecutionStation s = b.workReserve(work(a));
		if (s == null)
			return null;
		d.planTile.set(s.coo());
		return walk.set(a, d);
	}
	
	final Resumer walk = new Resumer(blueprint.employment().verb) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			AISubActivation s = AI.SUBS().walkTo.coo(a, d, d.planTile);
			if (s == null) {
				can(a, d);
				return null;
			}
			return s;
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			return init.set(a, d);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			ExecutionStation s = b.executionSpot(d.planTile);
			return hasEmployment(a, d) && s != null && s.workReserved();
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			ExecutionStation s = b.executionSpot(d.planTile);
			if (s != null)
				s.workCancel();
		}
	};
	
	private final Resumer init = new Resumer(blueprint.employment().verb) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			return AI.SUBS().STAND.activateTime(a, d, 1 + RND.rInt(5));
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			
			ExecutionStation s = b.executionSpot(d.planTile);
			
			if (s.clientIsUsing()) {
				return execute.set(a, d);
			}else {
				can(a, d);
				return null;
			}
			
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return walk.con(a, d);
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			walk.can(a, d);
		}
	};
	
	private final Resumer execute = new Resumer(blueprint.employment().verb) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			ExecutionStation s = b.executionSpot(d.planTile);
			ENTITY o = SETT.ENTITIES().getAtTileSingle(d.planTile.x(), d.planTile.y());
			if (o == null || !(o instanceof Humanoid) || ((Humanoid)o).indu().hType() != HTYPES.PRISONER()) {
				can(a, d);
				return handleCorpseWait.set(a, d);
			}
			
			if (s.isChop()) {
				return AI.SUBS().single.activate(a, d, AI.STATES().anima.work.activate(a, d));
			}else {
				return AI.SUBS().single.activate(a, d, AI.STATES().anima.grab.activate(a, d));
			}
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			
			ExecutionStation s = b.executionSpot(d.planTile);
			
			if (s.workExecute()) {
				return set(a, d);
			}else {
				can(a, d);
				return handleCorpseWait.set(a, d);
			}
			
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			ExecutionStation s = b.executionSpot(d.planTile);
			return s != null;
			
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			walk.can(a, d);
		}
	};
	
	private final Resumer handleCorpseWait = new Resumer(blueprint.employment().verb) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			return AI.SUBS().STAND.activateTime(a, d, 1+RND.rInt(2));
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			return handleCorpse.set(a, d);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return true;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			
		}
	};
	
	private final Resumer handleCorpse = new Resumer(blueprint.employment().verb) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			
			Corpse c = SETT.PATH().finders.corpses.getReservable(d.planTile.x(), d.planTile.y());
			if (c == null)
				return null;
			if (c.findableReservedIs())
				return null;
			if (!c.findableReservedCanBe())
				return null;
			if (work(a) == null)
				return null;
			COORDINATE coo = b.dumpCorpe(work(a));
			if (coo == null)
				return null;
			d.planTile.set(coo);
			c.findableReserve();
			d.planObject = -1;
			AISubActivation s = AI.SUBS().walkTo.drag(a, d, SETT.THINGS().corpses.draggable, c.index(), d.planTile);
			if (s == null) {
				c.findableReserveCancel();
			}
			return s;
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			ThingsCorpses.Corpse c = THINGS().corpses.getByIndex((short) d.planObject);
			c.drag(DIR.ALL.rnd(), d.planTile.x()*C.TILE_SIZE+C.TILE_SIZEH, d.planTile.y()*C.TILE_SIZE+C.TILE_SIZEH, 4);
			c.findableReserveCancel();
			return null;	
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			ThingsCorpses.Corpse c = THINGS().corpses.getByIndex((short) d.planObject);
			return c != null && c.findableReservedIs();
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			ThingsCorpses.Corpse c = THINGS().corpses.getByIndex((short) d.planObject);
			if (c != null && c.findableReservedIs())
				c.findableReserveCancel();
		}
	};



}