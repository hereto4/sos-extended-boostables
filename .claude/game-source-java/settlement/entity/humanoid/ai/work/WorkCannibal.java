package settlement.entity.humanoid.ai.work;
import static settlement.main.SETT.ROOMS;
import static settlement.main.SETT.THINGS;

import game.GAME;
import init.constant.C;
import init.resources.RESOURCE;
import init.resources.RES_AMOUNT;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.misc.job.SETT_JOB;
import settlement.room.food.cannibal.CannibalInstance;
import settlement.room.food.cannibal.ROOM_CANNIBAL;
import settlement.thing.ThingsCorpses.Corpse;
import snake2d.util.datatypes.DIR;

final class WorkCannibal extends PlanBlueprint {

	private final ROOM_CANNIBAL b = ROOMS().CANNIBAL;
	
	protected WorkCannibal(AIModule_Work module, PlanBlueprint[] map) {
		
		super(module, ROOMS().CANNIBAL, map);
		
	}
	
	@Override
	public AISubActivation init(Humanoid a, AIManager d) {
		CannibalInstance in = (CannibalInstance) work(a);
		
		SETT_JOB j = in.getWork();
		
		
		if (j == null) {
			GAME.Notify("Weird " + in.mX() + " " + in.mY());
			return null;
		}
		
		d.planTile.set(j.jobCoo());
		
		if (!SETT.PATH().finders.corpses.reserve(a.physics.tileC(), d.path, Integer.MAX_VALUE)) {
			return null;
		}
		in.getWork(d.planTile).jobReserve(null);
		Corpse prey  = SETT.PATH().finders.corpses.getResult();
		d.planObject = prey.index();
		b.setRace(d.planTile.x(), d.planTile.y(), prey.race());
		return fetch2.set(a,d);
		
	}

	final Resumer fetch2 = new Resumer(b.employment().verb) {
		
		@Override
		public AISubActivation setAction(Humanoid a, AIManager d) {
			AISubActivation ac = AI.SUBS().walkTo.path(a, d);
			if (ac != null)
				return ac;
			can(a, d);
			return null;
		}
		
		@Override
		public AISubActivation res(Humanoid a, AIManager d) {
			return drag_back2.set(a, d);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return SETT.THINGS().corpses.getByIndex((short)d.planObject) != null;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			
			Corpse prey  = SETT.THINGS().corpses.getByIndex((short)d.planObject);
			if (prey != null)
				prey.findableReserveCancel();
			d.planObject = -1;
			butcher2.can(a, d);
		}

	};
	
	final Resumer drag_back2 = new Resumer(b.employment().verb) {
		
		@Override
		public AISubActivation res(Humanoid a, AIManager d) {
			return butcher2.set(a, d);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			Corpse prey  = SETT.THINGS().corpses.getByIndex((short) d.planObject);
			return prey != null && prey.findableReservedIs() && butcher2.con(a, d);
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			Corpse prey  = SETT.THINGS().corpses.getByIndex((short) d.planObject);
			if (prey != null) {
				prey.findableReserveCancel();
			}
			d.planObject = -1;
			butcher2.can(a, d);
		}

		@Override
		public AISubActivation setAction(Humanoid a, AIManager d) {
			Corpse prey  = SETT.THINGS().corpses.getByIndex((short)d.planObject);
			return AI.SUBS().walkTo.drag(a, d, SETT.THINGS().corpses.draggable, prey.index(), d.planTile);
		}
	};
	
	final Resumer butcher2 = new Resumer(b.employment().verb) {
		
		@Override
		public AISubActivation res(Humanoid a, AIManager d) {
			
			Corpse prey  = SETT.THINGS().corpses.getByIndex((short)d.planObject);
			if (prey == null) {
				can(a, d);
				return null;
			}
			
			produce(prey, a, d);
			
			if (prey.isRemoved()) {
				can(a, d);
				return null;
			}
			b.employment().sound().rnd(a);
			return AI.SUBS().WORK_HANDS.activate(a, d, 5);
			
		}
		
		private void produce(Corpse corpse, Humanoid a,  AIManager d) {
			
			
			if (corpse.resLeft() <= 0) {
				if (corpse.indu().race().resources().size() > 0) {
					RES_AMOUNT rr = corpse.indu().race().resources().rnd();
					produce(rr.resource(), 1, a, d);
				}
				corpse.remove();
				return;
			}
			
			double dd = corpse.resLeft();
			if (dd > 0.25)
				dd = 0.25;
			
			for (RES_AMOUNT rr : corpse.indu().race().resources()) {
				int am = (int) (1 + rr.amount()*dd);
				produce(rr.resource(), am, a, d);
			}
			
			
			corpse.resRemove();
			
			if (corpse.resLeft() <= 0) {
				corpse.remove();
			}
		}
		
		private void produce(RESOURCE res, int am, Humanoid a,  AIManager d) {
			CannibalInstance in = (CannibalInstance) work(a);
			int kk = in.produce(res, am);

			in.gore(d.planTile);
			
			DIR dd = a.speed.dir().next(kk == 0 ? -1 : 1);
			THINGS().resources.create(a.tc().x()+dd.x(), a.tc().y()+dd.y(), res,am);
				
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			CannibalInstance in = (CannibalInstance) work(a);
			if (in == null)
				return false;
			if (in.getWork(d.planTile) == null || !in.getWork(d.planTile).jobReservedIs(null))
				return false;
			return true;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			Corpse prey  = SETT.THINGS().corpses.getByIndex((short) d.planObject);
			if (prey != null) {
				prey.findableReserveCancel();
				d.planObject = -1;
			}
			CannibalInstance in = (CannibalInstance) work(a);
			if (in == null)
				return;
			if (in.getWork(d.planTile) == null || !in.getWork(d.planTile).jobReservedIs(null))
				return;
			in.getWork(d.planTile).jobReserveCancel(null);
		}

		@Override
		public AISubActivation setAction(Humanoid a, AIManager d) {
			Corpse prey  = SETT.THINGS().corpses.getByIndex((short) d.planObject);
			prey.drag(DIR.N, d.planTile.x()*C.TILE_SIZE+C.TILE_SIZEH, d.planTile.y()*C.TILE_SIZE+C.TILE_SIZEH, 0);
			CannibalInstance in = (CannibalInstance) work(a);
			in.resetGore(d.planTile);
			return AI.SUBS().WORK_HANDS.activate(a, d, 12);
		}
	};

	@Override
	protected boolean shouldContinue(Humanoid a, AIManager d) {
		boolean be = super.shouldContinue(a, d);
		if (!be)
			GAME.Notify("here");
		return be;
	}
}