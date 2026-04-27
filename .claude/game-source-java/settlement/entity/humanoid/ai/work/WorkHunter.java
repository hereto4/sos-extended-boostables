package settlement.entity.humanoid.ai.work;
import static settlement.main.SETT.PATH;
import static settlement.main.SETT.ROOMS;
import static settlement.main.SETT.THINGS;

import game.GAME;
import init.constant.C;
import settlement.entity.animal.AnimalSpecies;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AIModules;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.room.food.hunter.ROOM_HUNTER;
import settlement.room.main.RoomInstance;
import settlement.stats.STATS;
import settlement.thing.ThingsCadavers.Cadaver;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.rnd.RND;
import snake2d.util.sprite.text.Str;

final class WorkHunter extends PlanBlueprint {

	private final ROOM_HUNTER b;
	
	protected WorkHunter(ROOM_HUNTER b, AIModule_Work module, PlanBlueprint[] map) {
		
		super(module, b, map);
		this.b = b;
	}
	
	@Override
	public AISubActivation init(Humanoid a, AIManager d) {
		RoomInstance in = work(a);
		
		if (!PATH().finders.entryPoints.anyHas(a.tc().x(), a.tc().y()))
			return null;
		

		COORDINATE j = b.reserveWork(in, a);
		
		if (j == null) {
			GAME.Notify("Weird " + in.mX() + " " + in.mY());
			return null;
		}
		d.planTile.set(j);
		
		if (getCadaver(a, d) != null) {
			AISubActivation s = walk.set(a, d);
			
			if (s == null)
				b.workFinish(d.planTile);
			else
				return s;
		}
		
		if (STATS.WORK().WORK_TIME.indu().getD(a.indu()) < 0.5) {
			AISubActivation s = leave.set(a, d);
			return s;
			
		}
		
		
		AISubActivation s = walk.set(a, d);
		
		if (s == null)
			b.workFinish(d.planTile);
		return s;
		
	}
	
	final Resumer walk = new Resumer() {
		
		@Override
		public AISubActivation setAction(Humanoid a, AIManager d) {
			STATS.WORK().proximityStart(a);
			return AI.SUBS().walkTo.coo(a, d, d.planTile);
		}
		
		@Override
		public AISubActivation res(Humanoid a, AIManager d) {
			STATS.WORK().proximityEnd(a);
			return butcher.set(a, d);
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			b.workFinish(d.planTile);
		}

		@Override
		public boolean con(Humanoid a, AIManager d) {
			return work(a) != null && work(a).blueprint() == b;
		}

	};
	
	final Resumer leave = new Resumer() {
		
		@Override
		public AISubActivation setAction(Humanoid a, AIManager d) {
			if (PATH().finders.entryPoints.any(a.tc().x(), a.tc().y(), d.path, Integer.MAX_VALUE)) {
				b.reportSkill(work(a), a);
				return AI.SUBS().walkTo.pathFull(a, d);
			}
			return null;
		}
		
		@Override
		public AISubActivation res(Humanoid a, AIManager d) {
			return hunt.set(a, d);
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			b.workFinish(d.planTile);
		}

		@Override
		public boolean con(Humanoid a, AIManager d) {
			return work(a) != null && work(a).blueprint() == b;
		}

	};
	
	final Resumer hunt = new Resumer() {
		
		@Override
		public AISubActivation setAction(Humanoid a, AIManager d) {
			SETT.ENTITIES().moveIntoTheTheUnknown(a);
			a.speed.magnitudeInit(0);
			return AI.SUBS().STAND.activate(a, d);
		}
		
		@Override
		public AISubActivation res(Humanoid a, AIManager d) {
			b.reportSkill(work(a), a);
			if (STATS.WORK().WORK_TIME.indu().getD(a.indu()) > 0.5 || !AIModules.current(d).moduleCanContinue(a, d)) {
				can(a, d);
				return drag.set(a, d);
			}
			return AI.SUBS().STAND.activate(a, d);
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			SETT.ENTITIES().returnFromTheTheUnknown(a);
			b.workFinish(d.planTile);
		}

		@Override
		public boolean con(Humanoid a, AIManager d) {
			return work(a) != null && work(a).blueprint() == b;
		}

	};
	
	final Resumer drag = new Resumer() {
		
		@Override
		public AISubActivation setAction(Humanoid a, AIManager d) {
			
			AnimalSpecies ss = spe();
			Cadaver c = SETT.THINGS().cadavers.normal(a.tc().x(), a.tc().y(), ss.mass()*RND.rFloat1(1.1), 1, ss, 2);
			if (c == null)
				return null;
			
			d.planObject = c.index();
			RoomInstance in = work(a);
			
			COORDINATE j = b.reserveWork(in, a);
			
			if (j == null) {
				GAME.Notify("Weird " + in.mX() + " " + in.mY());
				return null;
			}
			d.planTile.set(j);
			
			AISubActivation ac = AI.SUBS().walkTo.drag(a, d, THINGS().cadavers.draggable, c.index(), d.planTile);
			if (ac != null)
				return ac;
			
			can(a, d);
			return null;
			
		}
		
		private Cadaver getCadaver(Humanoid a, AIManager d) {
			if (d.planObject == -1)
				return null;
			Cadaver e = THINGS().cadavers.getByIndex(d.planObject);
			
			if (e == null || e.isRemoved() || !e.resHas()) {
				d.planObject = -1;
				return null;
			}
			return e;
		}
		
		@Override
		public AISubActivation res(Humanoid a, AIManager d) {
			Cadaver old = THINGS().cadavers.tGet.get(d.planTile);
			if (old != null)
				old.remove();
			Cadaver c = getCadaver(a, d);
			c.drag(DIR.ORTHO.get(ROOMS().fData.item.get(d.planTile).rotation), (d.planTile.x()<<C.T_SCROLL)+C.TILE_SIZEH, (d.planTile.y()<<C.T_SCROLL)+C.TILE_SIZEH, 0);
			return butcher.set(a, d);
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			b.workFinish(d.planTile);
		}

		@Override
		public boolean con(Humanoid a, AIManager d) {
			return work(a) != null && work(a).blueprint() == b && getCadaver(a, d) != null;
		}

		private AnimalSpecies spe() {
			double tot = 0;
			for (AnimalSpecies s : SETT.ANIMALS().sett()) {
				tot += s.occurence(SETT.WORLD_AREA().climate());
			}
			tot *= RND.rFloat();
			for (AnimalSpecies s : SETT.ANIMALS().sett()) {
				tot -= s.occurence(SETT.WORLD_AREA().climate());
				if (tot <= 0)
					return s;
			}
			return SETT.ANIMALS().sett().rnd();
		}
		
	};
	
	final Resumer butcher = new Resumer() {
		
		@Override
		public AISubActivation res(Humanoid a, AIManager d) {
			
			if (AIModules.current(d).moduleCanContinue(a, d)) {
				Cadaver prey = getCadaver(a, d);
				if (prey != null) {
					b.work(work(a), d.planTile, a, true);
					can(a,d);
					if (prey.resHas())
						prey.resRemove();
					b.employment().sound().rnd(a);
					return AI.SUBS().WORK_HANDS.activate(a, d, 20);
				}else {
					b.work(work(a), d.planTile, a, false);
					return AI.SUBS().STAND.activateRndDir(a, d);
				}
			}
			
			b.workFinish(d.planTile);
			return null;
			
						
		}
		

		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return work(a) != null && work(a).blueprint() == b;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			b.workFinish(d.planTile);
		}

		@Override
		public AISubActivation setAction(Humanoid a, AIManager d) {
			b.reportSkill(work(a), a);
			return AI.SUBS().STAND.activate(a, d);
		}
	};

	private Cadaver getCadaver(Humanoid a, AIManager d) {
		return THINGS().cadavers.tGet.get(d.planTile);
	}

	@Override
	protected void name(Humanoid a, AIManager d, Str string) {
		string.add(b.employment().verb);
	}


}