package settlement.entity.humanoid.ai.subject;

import static settlement.main.SETT.PATH;

import init.constant.C;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AIModules;
import settlement.entity.humanoid.ai.main.AIPLAN;
import settlement.entity.humanoid.ai.main.AISTATES.Animation;
import settlement.entity.humanoid.ai.main.AISUB;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.entity.humanoid.ai.main.AISUBS;
import settlement.main.SETT;
import settlement.misc.util.FINDABLE;
import settlement.path.AVAILABILITY;
import settlement.room.main.RoomBlueprint;
import settlement.room.service.module.ROOM_ACTIVITY;
import settlement.room.service.module.ROOM_ACTIVITY.ROOM_ACTIVITY_HASER;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.rnd.RND;
import snake2d.util.sprite.text.Str;

class Activity extends AIPLAN.PLANRES{


	public final ROOM_ACTIVITY s;
	private final CharSequence verb;
	
	public Activity(ROOM_ACTIVITY_HASER t, CharSequence verb) {
		super("ACTIVITY_" + ((RoomBlueprint)t).key);
		this.s = t.spec();
		this.verb = verb;
	};

	@Override
	protected AISubActivation init(Humanoid a, AIManager d) {
		return walk.set(a, d);
	}

	private final R walk = new R() {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			
			COORDINATE c = s.service().reserve(a.tc(), 400);
			if (c == null)
				return null;
			d.planTile.set(c);
			c = s.getDestination(c);
			AISubActivation sub = AI.SUBS().walkTo.cooFull(a, d, c);
			if (sub == null) {
				can(a, d);
				return null;
			}
			return sub;
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			d.planByte1 = (byte) (8 + RND.rInt(8));
			return move.set(a, d);
		}
	};
	
	private final R move = new R() {
		
		private final int[] order = new int[] {0,1,2};
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			
			if (!shouldMove(a, a.tc().x(), a.tc().y()))
				return stand.set(a, d);
			
			
			DIR dd = DIR.get(a.body().cX(), a.body().cY(), s.lookAt(d.planTile.x(), d.planTile.y()));
			
			for (int i : order) {
				dd = dd.next(i);
				int dx = a.tc().x() + dd.x();
				int dy = a.tc().y() + dd.y();
				if (isSpot(dx, dy)) {
					if (!shouldMove(a, dx, dy)) {
						return AI.SUBS().walkTo.cooFull(a, d, dx, dy);
					}
				}
			}
			
			return stand.set(a, d);
		}
		
		private boolean shouldMove(Humanoid a, int cx, int cy) {
			for (ENTITY e : SETT.ENTITIES().getAtTile(cx, cy)){
				if (e != a && e instanceof Humanoid && e.speed.magnitude() == 0)
					return true;
			}
			return false;
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			return stand.set(a, d);
		}
	};
	
	private final R stand = new R() {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			if (!s.is(d.planTile.x(), d.planTile.y()))
				return null;
			
			if (d.planByte1 <= 0) {
				if (!AIModules.current(d).moduleCanContinue(a, d) || RND.oneIn(5)) {
					can(a, d);
					return null;
				}
				
			}else {
				FINDABLE f = s.service().getReserved(d.planTile.x(), d.planTile.y());
				if (f == null) {
					f = s.service().getReservable(d.planTile.x(), d.planTile.y());
					if (f == null) {
						d.planByte1 -= 3;
					}else
						f.findableReserve();
				}
				
				d.planByte1 --;
			}
			
			
			
				
			DIR dd = DIR.get(a.body().cX(), a.body().cY(), s.lookAt(d.planTile.x(), d.planTile.y()));
			if (RND.oneIn(5))
				dd = dd.next(RND.rInt0(1));
			a.speed.setDirCurrent(dd);
			
			if (s.shouldCheer(d.planTile.x(), d.planTile.y())) {
				return cheer.set(a, d);
			}
			
			if (s.shouldBoo(d.planTile.x(), d.planTile.y())) {
				return boo.set(a, d);
			}

			
			return AI.SUBS().STAND.activateTime(a, d, 10);
			
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			return move.set(a, d);
		}
	};
	
	private final R cheer = new R() {
		
		private Animation[] anima = new Animation[] {
			AI.STATES().anima.wave,
			AI.STATES().anima.box,
			AI.STATES().anima.lay,
			AI.STATES().anima.stand,
		};
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			
			return AI.SUBS().single.activate(a, d, anima[RND.rInt(anima.length)], 2+RND.rInt(4));
			
			
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			return stand.set(a, d);
		}
	};
	
	private final R boo = new R() {
		
		private final AISUB ss = new AISUBS.Throw(key+"specThrow") {
			
			@Override
			public int destY(Humanoid a, AIManager d) {
				if (s != null)
					return s.lookAt(d.planTile.x(), d.planTile.y()).y();
				return d.planTile.y()*C.TILE_SIZE+C.TILE_SIZEH;
			}
			
			@Override
			public int destX(Humanoid a, AIManager d) {
				if (s != null)
					return s.lookAt(d.planTile.x(), d.planTile.y()).x();
				return d.planTile.x()*C.TILE_SIZE+C.TILE_SIZEH;
			}
		};
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			if (RND.oneIn(5))
				return ss.activate(a, d);
			if (RND.oneIn(2))
				return AI.SUBS().single.activate(a, d, AI.STATES().anima.fist, 4);
			return AI.SUBS().STAND.activateTime(a, d, 4);
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			return stand.set(a, d);
		}
	};
	
	private boolean isSpot(int tx, int ty) {
		if (!SETT.IN_BOUNDS(tx, ty))
			return false;
		if (SETT.ROOMS().map.is(tx, ty))
			return false;
		AVAILABILITY av = PATH().availability.get(tx,ty);
		if (av.player >= 0 && av.player < AVAILABILITY.Penalty && av.from == 0) {
			return true;
		}
		return false;
	}
	

	
	private abstract class R extends Resumer {
		
		protected R() {
			super("");
		}

		@Override
		protected void name(Humanoid a, AIManager d, Str string) {
			if (s != null)
				string.add(verb);
		}

		@Override
		public boolean con(Humanoid a, AIManager d) {
			return true;
		}

		@Override
		public void can(Humanoid a, AIManager d) {

			FINDABLE f = s.service().getReserved(d.planTile.x(), d.planTile.y());
			if (f != null)
				f.findableReserveCancel();
			
		}
		
	}

}
