package settlement.entity.humanoid.ai.subwalk;

import static settlement.main.SETT.PATH;
import static settlement.main.SETT.ROOMS;
import static settlement.main.SETT.THINGS;

import game.GAME;
import init.constant.C;
import init.resources.RBIT;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.resources.ResGroup;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AISTATE;
import settlement.entity.humanoid.ai.main.AISUB;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.misc.util.FINDABLE;
import settlement.path.AVAILABILITY;
import settlement.path.finders.SFinderFindable;
import settlement.path.finders.SFinderRoomService;
import settlement.room.main.Room;
import settlement.room.main.RoomInstance;
import settlement.room.service.module.RoomService;
import settlement.thing.DRAGGABLE;
import settlement.thing.DRAGGABLE.DRAGGABLE_HOLDER;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.rnd.RND;

public final class AISUB_walkTo {

	public AISubActivation pathRun(Humanoid a, AIManager d) {
		return run.activate(a, d);
	}
	
	public AISubActivation path(Humanoid a, AIManager d) {
		return vanilla.activate(a, d);
	}
	
	public AISubActivation pathFull(Humanoid a, AIManager d) {
		return vanilla_included.activate(a, d);
	}
	
	/**
	 * Finds a random place in a room. outposts will get a tile in its vicinity
	 * @param a
	 * @param d
	 * @param room
	 * @return
	 */
	public AISubActivation room(Humanoid a, AIManager d, final int rx, final int ry) {
		
		
		Room room = SETT.ROOMS().map.get(rx, ry);
		
		int w = room.width(rx, ry);
		int h = room.height(rx, ry);
		
		int r = w > h ? w:h;
		if (r <= 2) {
			COORDINATE c = PATH().finders.arround.find(rx, ry, 10, 10+RND.rInt(10));
			if (c != null) {
				if (d.path.request(a.physics.tileC(), c.x(), c.y()))
					return vanilla.activate(a, d);
			}
			return null;
		}
		
		if (SETT.PATH().comps.superComp.get(a.tc()) != SETT.PATH().comps.superComp.get(room.mX(rx, ry), room.mY(rx, ry)))
			return null;
			
		
		final int x1 = room.x1(rx, ry); 
		final int y1 = room.y1(rx, ry); 
		final int x2 = x1 + w; 
		final int y2 = y1 + h; 
		
		int x = x1+ RND.rInt(w);		
		int y = y1 + RND.rInt(h);
		
		int backupX = -1;
		int backupY = -1;
		
		for (int i = w*h; i >= 0; i--) {
			
			if (x >= x2) {
				y++;
				x = x1;
				if (y >= y2) {
					y = y1;
				}
			}
			
			if (room.isSame(rx, ry, x, y)) {
				AVAILABILITY av = PATH().availability.get(x,y);
				if (av.player >= 0 && av.player < AVAILABILITY.Penalty && av.from == 0) {
					d.path.requestFull(a.physics.tileC(), x, y);
					if (d.path.isSuccessful())
						return vanilla.activate(a, d);
					GAME.Notify("couldn't find path from: " + a.physics.tileC() + " to" + x + " " + y);
					break;
				}else if (backupX == -1 && av.player >= 0) {
					backupX = x;
					backupY = y;
				}
			}
			
			x++;
		}
		
		if (backupX != -1) {
			d.path.requestFull(a.physics.tileC(), backupX, backupY);
			if (d.path.isSuccessful())
				return vanilla.activate(a, d);
			GAME.Notify("couldn't find path from: " + a.physics.tileC() + " to" + backupX + " " + backupY);
		}
		
		GAME.Notify("Couldn't find place in room " + rx + " " + ry + " " + w + " " + h + " " + r + " " + room + " ");
		return null;
	}
	
	public AISubActivation room(Humanoid a, AIManager d, RoomInstance ins) {
		return room(a,d,ins.mX(), ins.mY());
	}
	
	public AISubActivation around(Humanoid a, AIManager d, int tx, int ty) {
		COORDINATE c = PATH().finders.arround.find(tx, ty, 10, 10+RND.rInt(10));
		if (c != null) {
			if (d.path.request(a.physics.tileC(), c.x(), c.y()))
				return vanilla.activate(a, d);
		}
		return null;
	}
	
	public AISubActivation around(Humanoid a, AIManager d, int tx, int ty, int mindistance, int maxDistance) {
		COORDINATE c = PATH().finders.arround.find(tx, ty, mindistance, maxDistance);
		if (c != null) {
			if (d.path.request(a.physics.tileC(), c.x(), c.y()))
				return vanilla.activate(a, d);
		}
		return null;
	}
	
	/**
	 * Finds a place within the room the humanoid is standing. Why not use above is unclear.
	 * @param a
	 * @param d
	 * @return
	 */
	AISubActivation insideRoom(Humanoid a, AIManager d) {
		Room room = ROOMS().map.get(a.physics.tileC());
		final int rx = room.mX(a.physics.tileC().x(), a.physics.tileC().y());
		final int ry = room.mY(a.physics.tileC().x(), a.physics.tileC().y());
		int w = room.width(rx, ry);
		int h = room.height(rx, ry);
		
		int r = w > h ? w:h;
		if (r <= 2) {
			COORDINATE c = PATH().finders.arround.find(rx, ry, 10, 10+RND.rInt(10));
			if (c != null) {
				if (d.path.request(a.physics.tileC(), c.x(), c.y()))
					return vanilla.activate(a, d);
			}
			return null;
		}
				
		int sx = a.physics.tileC().x() + RND.rInt(w);
		int sy = a.physics.tileC().y() + RND.rInt(h);
		
		int i = 0;
		
		while(util.GUTIL.circle().radius(i++) <= r) {
			COORDINATE c = util.GUTIL.circle().get(i);
			int x = sx + c.x();
			int y = sy + c.y();
			if (room.isSame(rx, ry, x, y) && PATH().cost.get(x, y) > 0) {
				d.path.requestFull(a.physics.tileC(), x, y);
				if (d.path.isSuccessful())
					return vanilla.activate(a, d);
				break;
			}
		}
		
		GAME.Notify("Couldn't find place in room " + rx + " " + ry);
		return null;
	}
	
	public AISubActivation coo(Humanoid a, AIManager d, int dx, int dy) {
		d.path.request(a.physics.tileC(), dx, dy);
		if (d.path.isSuccessful())
			return vanilla.activate(a, d);
		return null;
	}
	
	public AISubActivation coo(Humanoid a, AIManager d, COORDINATE dest) {
		return coo(a, d, dest.x(), dest.y());
	}
	
	public AISubActivation drag(Humanoid a, AIManager d, DRAGGABLE_HOLDER h, int index, int dx, int dy) {
		
		d.planObject = index;
		d.subPathByte = h.index;
		
		if (h.draggable((short) d.planObject) == null) {
			d.debug(a, "Draggable gone! ");
			return null;
		}
		
		if (!h.draggable((short) d.planObject).canBeDragged()) {
			return null;
		}
		
		d.path.request(a.physics.tileC(), dx, dy);
		if (d.path.isSuccessful())
			return drag.activate(a, d);
		return null;
	}
	
	public AISubActivation drag(Humanoid a, AIManager d, DRAGGABLE_HOLDER h, int index, COORDINATE dest) {
		return drag(a, d, h, index, dest.x(), dest.y());
	}
	
	public AISubActivation cooFull(Humanoid a, AIManager d, COORDINATE dest) {
		d.path.requestFull(a.physics.tileC(), dest);
		if (d.path.isSuccessful())
			return vanilla_included.activate(a, d);
		return null;
	}
	
	public AISubActivation cooFull(Humanoid a, AIManager d, int dx, int dy) {
		d.path.requestFull(a.physics.tileC(), dx, dy);
		if (d.path.isSuccessful())
			return vanilla_included.activate(a, d);
		return null;
	}
	
	public AISubActivation resource(Humanoid a, AIManager d, ResGroup<?> r) {
		return resource(a,d,r.mask, 250);
	}
	
	public AISubActivation resource(Humanoid a, AIManager d, RBIT resBits, int maxDist ) {
		RESOURCE res = PATH().finders.resource.normal.reserve(a.physics.tileC(), resBits, d.path, maxDist);
		if (res == null) {
			d.subPathByte = -1;
			return null;
		}
		
		d.subPathByte = res.bIndex();
		
		return resource.activate(a, d);
	}
	
	public AISubActivation resource(Humanoid a, AIManager d, RBIT resBits) {
		return resource(a, d, resBits, 250);
	}
	
	public AISubActivation resource(Humanoid a, AIManager d, RESOURCE r, int maxDistance) {
		return resource(a, d, r.bit, maxDistance);
	}
	
	public AISubActivation resource(Humanoid a, AIManager d, RESOURCE r) {
		return resource(a, d, r.bit, 250);
	}
	
	public AISubActivation resourceAlreadyReserved(Humanoid a, AIManager d, RESOURCE r) {
		d.subPathByte = r.bIndex();
		return resource.activate(a, d);
	}
	
	public RESOURCE targetResource(Humanoid a, AIManager d) {
		return RESOURCES.ALL().get(d.subPathByte);
	}
	
	public AISubActivation deposit(Humanoid a, AIManager d, RESOURCE r) {
		return deposit(a, d, r, Integer.MAX_VALUE);	
	}
	
	public AISubActivation deposit(Humanoid a, AIManager d, RESOURCE r, int maxTiles) {
		
		if (PATH().finders.storage.reserve(a.physics.tileC(), r, d.path, maxTiles)) {
			d.subPathByte = r.bIndex();
			d.resourceCarriedSet(r);
			return storage.activate(a, d);
		}else {
			THINGS().resources.create(a.physics.tileC(), r, 1);
			return null;
		}
		
	}
	
	public AISubActivation depositInited(Humanoid a, AIManager d, RESOURCE r) {
		
		d.subPathByte = r.bIndex();
		d.resourceCarriedSet(r);
		return storage.activate(a, d);
		
	}
	
	public AISubActivation service(Humanoid a, AIManager d, RoomService r) {
		return service(a, d, r.finder, r.radius);
		
	}
	
	public AISubActivation service(Humanoid a, AIManager d, SFinderFindable r, int distance) {
		
		if (!r.reserve(a.physics.tileC(), d.path, distance)) {
			return null;
		}
		d.subPathByte = r.index;
		return service.activate(a, d);
		
	}
	
	AISubActivation service(Humanoid a, AIManager d, SFinderFindable r, COORDINATE c) {
		FINDABLE f = r.getReservable(c.x(), c.y());
		if (f == null)
			return null;
		d.subPathByte = r.index;
		d.path.request(a.physics.tileC().x(), a.physics.tileC().y(), c);
		if (!d.path.isSuccessful())
			return null;
		f.findableReserve();
		
		return service.activate(a, d);
	}
	
	public AISubActivation serviceInclude(Humanoid a, AIManager d, RoomService r) {
		return serviceInclude(a, d, r.finder, r.radius);	
	}
	
	public AISubActivation serviceInclude(Humanoid a, AIManager d, RoomService r, int dist) {
		return serviceInclude(a, d, r.finder, dist);	
	}
	
	public AISubActivation serviceInclude(Humanoid a, AIManager d, SFinderFindable r, int dist) {
		d.subPathByte = r.index;
		if (!r.reserve(a.physics.tileC(), d.path, dist)) {
			return null;
		}
		
		return serviceInclude.activate(a, d);
		
	}
	
	public AISubActivation serviceMax(Humanoid a, AIManager d, SFinderFindable r) {
		d.subPathByte = r.index;
		if (!r.reserve(a.physics.tileC(), d.path, Integer.MAX_VALUE)) {
			GAME.Notify("oh no!");
			return null;
		}
		
		return service.activate(a, d);
		
	}
	
	public AISubActivation follow(Humanoid a, AIManager d, ENTITY target, boolean run, byte trials) {
		if (run)
			return follow_run.activate(a, d, target, trials);
		return follow.activate(a, d, target, trials);
	}
	
	public boolean followSucess(Humanoid a, AIManager d) {
		return follow_run.isSuccess(a, d);
	}
	
	public AISubActivation flee(Humanoid a, AIManager d, ENTITY other) {
		return flee.activate(a, d, other);
	}
	
	public AISubActivation run_arround_crazy(Humanoid a, AIManager d, int iterations) {
		return flee.activate(a, d, iterations);
	}
	
	
	
	private final SubFlee flee = new SubFlee();
	private final AISub_follow follow = new AISub_follow("walk2Follow", AI.STATES().WALK2, "following");
	private final AISub_follow follow_run = new AISub_follow("walk2Chase", AI.STATES().RUN2, "chasing");
	

	
	private final AISUB vanilla = new PathWalker("walkPath", "walking along path") {

		@Override
		public void abort(Humanoid a, AIManager d) {
			
			
		}

		@Override
		public void arrive(Humanoid a, AIManager d) {
			
		}

		@Override
		public boolean hasFailed(Humanoid a, AIManager d) {
			return false;
		}
	};
	
	private final AISUB vanilla_included = new PathWalker("walkVanilla", "walking along path") {

		@Override
		public AISTATE init(Humanoid a, AIManager d) {
			if (a.tc().isSameAs(d.path.destX(), d.path.destY()) && a.physics.isWithinTile())
				return wait.set(a, d);
			return super.init(a, d);
			
		};
		
		@Override
		public void abort(Humanoid a, AIManager d) {
			
			
		}

		@Override
		public void arrive(Humanoid a, AIManager d) {
			
		}

		@Override
		public boolean hasFailed(Humanoid a, AIManager d) {
			return false;
		}
		
		private final Resumer last = new Resumer() {

			@Override
			protected AISTATE setAction(Humanoid a, AIManager d) {
				return AI.STATES().WALK2.free(a, d, d.path.destX()*C.TILE_SIZE+C.TILE_SIZEH, d.path.destY()*C.TILE_SIZE+C.TILE_SIZEH);
			}
			
			@Override
			protected AISTATE res(Humanoid a, AIManager d) {
				return wait.set(a, d);
			}
		};
		
		@Override
		protected AISTATE setLast(Humanoid a, AIManager d) {
			return last.set(a, d);
		};
	};
	
	private final AISUB drag = new PathWalker("walkDrag", AI.STATES().DRAG, "dragging something") {

		@Override
		public void abort(Humanoid a, AIManager d) {
			
			
		}

		@Override
		public void arrive(Humanoid a, AIManager d) {
			
		}

		@Override
		public boolean hasFailed(Humanoid a, AIManager d) {
			DRAGGABLE c = DRAGGABLE.DRAGGABLE_HOLDER.all().get(d.subPathByte).draggable((short) d.planObject);
			boolean ret = c == null || !c.canBeDragged();
			
			return ret;
		}
	};
	
	private final AISUB run = new PathWalker("walkRun", AI.STATES().RUN2, "running") {

		@Override
		public void abort(Humanoid a, AIManager d) {
			
			
		}
		
		@Override
		protected AISTATE setLast(Humanoid a, AIManager d) {
			return null;
		};

		@Override
		public void arrive(Humanoid a, AIManager d) {
			
		}

		@Override
		public boolean hasFailed(Humanoid a, AIManager d) {
			return false;
		}
	};
	
	private AISUB resource = new PathWalker("walkRes", "walking to resource"){
		
		private final Resumer pickedup = new Resumer() {
			
			@Override
			public AISTATE res(Humanoid a, AIManager d) {
				return null;
			}

			@Override
			public boolean success(Humanoid a, AIManager d) {
				return true;
			}

			@Override
			public AISTATE setAction(Humanoid a, AIManager d) {
				return null;
			}
			@Override
			public void can(Humanoid a, AIManager d) {
				d.resourceDrop(a);
			}
		};
		
		@Override
		public void abort(Humanoid a, AIManager d) {
			PATH().finders.resource.unreserve(RESOURCES.ALL().get(d.subPathByte), d.path.destX(), d.path.destY(), 1);
		}

		@Override
		public void arrive(Humanoid a, AIManager d) {
			int x = d.path.destX();
			int y = d.path.destY();
			RESOURCE r = RESOURCES.ALL().get(d.subPathByte);
			PATH().finders.resource.pickup(r, x, y, 1);
			d.resourceCarriedSet(r);
			if (r == null)
				d.debug(a, RESOURCES.ALL().get(d.subPathByte).name);
			pickedup.set(a, d);
		}

		@Override
		public boolean hasFailed(Humanoid a, AIManager d) {
			int x = d.path.destX();
			int y = d.path.destY();
			RESOURCE r = RESOURCES.ALL().get(d.subPathByte);
			if (!PATH().finders.resource.isReservedAndAvailable(r, x, y)) {
				return PATH().finders.resource.scattered.reserveExtra(r, x, y, 1) == 0;
			}
			return false;
		}
		
	};
	
	private AISUB storage = new PathWalker("walkStore", "walking to stockpile"){

		private final Resumer finished = new Resumer() {
			
			@Override
			public AISTATE res(Humanoid a, AIManager d) {
				return null;
			}

			@Override
			public boolean success(Humanoid a, AIManager d) {
				return true;
			}

			@Override
			public AISTATE setAction(Humanoid a, AIManager d) {
				return null;
			}
			@Override
			public void can(Humanoid a, AIManager d) {
				
			}
		};
		
		@Override
		public void abort(Humanoid a, AIManager d) {
			PATH().finders.storage.cancelReservation(d.path.destX(), d.path.destY(), d.subPathByte);
		}

		@Override
		public void arrive(Humanoid a, AIManager d) {
			int x = d.path.destX();
			int y = d.path.destY();
			PATH().finders.storage.deposit(x,y,d.subPathByte);
			d.resourceCarriedSet(null);
			finished.set(a, d);
		}

		@Override
		public boolean hasFailed(Humanoid a, AIManager d) {
			int x = d.path.destX();
			int y = d.path.destY();
			return !PATH().finders.storage.isReservedAndAvailable(x, y,d.subPathByte); 
		}
		
	};
	
	private AISUB service = new PathWalker("WalkSer", "walking to service"){

		@Override
		public void arrive(Humanoid a, AIManager d) {
			finished.set(a, d);
			return;
		}

		@Override
		public void abort(Humanoid a, AIManager d) {
			FINDABLE s = service(d);
			if (s != null)
				s.findableReserveCancel();
		}

		@Override
		public boolean hasFailed(Humanoid a, AIManager d) {
			FINDABLE s = service(d);
			
			if (s == null || !s.findableReservedIs()) {
				return true;
			}
			
			return false;
		}
		
		private FINDABLE service(AIManager d) {
			int x = d.path.destX();
			int y = d.path.destY();
			SFinderFindable s = SFinderRoomService.get(d.subPathByte);
			if (s == null)
				return null;
			return s.getReserved(x, y);
		}
		
		private final Resumer finished = new Resumer() {
			
			@Override
			public AISTATE res(Humanoid a, AIManager d) {
				return null;
			}

			@Override
			public boolean success(Humanoid a, AIManager d) {
				return true;
			}

			@Override
			public AISTATE setAction(Humanoid a, AIManager d) {
				return null;
			}
			@Override
			public void can(Humanoid a, AIManager d) {
				abort(a, d);
			}
		};
		
	};
	
	private AISUB serviceInclude = new PathWalker("walkSer2", "walking to service"){

		private final Resumer movingLast = new Resumer() {
			
			@Override
			public AISTATE setAction(Humanoid a, AIManager d) {
				
				AISTATE s =  AI.STATES().WALK2.tile(a, d, d.path.destX(), d.path.destY());
//				GAME.Notify(a.tc() + " " + a.speed.dir());
				return s;
			}
			
			@Override
			public AISTATE res(Humanoid a, AIManager d) {
				if (!a.physics.tileC().isSameAs(d.path.destX(), d.path.destY())) {
					d.debug(a, "weird");
				}
				a.speed.magnitudeInit(0);
				return wait.set(a, d);
			}
			@Override
			public void can(Humanoid a, AIManager d) {
				abort(a, d);
			};
		};
		
		private final Resumer finished = new Resumer() {
			
			@Override
			public AISTATE res(Humanoid a, AIManager d) {
				return null;
			}

			@Override
			public boolean success(Humanoid a, AIManager d) {
				return true;
			}

			@Override
			public AISTATE setAction(Humanoid a, AIManager d) {
				return null;
			}
			@Override
			public void can(Humanoid a, AIManager d) {
				abort(a, d);
			}
		};
		
		@Override
		public void arrive(Humanoid a, AIManager d) {
			finished.set(a, d);
		}
		
		@Override
		public AISTATE setLast(Humanoid a, AIManager d) {

			return movingLast.set(a, d);
		};
		
		@Override
		public void abort(Humanoid a, AIManager d) {
			FINDABLE s = service(d);
			if (s != null)
				s.findableReserveCancel();
		}

		@Override
		public boolean hasFailed(Humanoid a, AIManager d) {
			FINDABLE s = service(d);
			if (s == null || !s.findableReservedIs()) {
				return true;
			}
			
			return false;
		}
		
		private FINDABLE service(AIManager d) {
			int x = d.path.destX();
			int y = d.path.destY();
			SFinderFindable s = SFinderRoomService.get(d.subPathByte);
			if (s == null) {
				return null;
			}
			
			return s.getReserved(x, y);
		}
		
	};

	public boolean isWalking(AIManager a) {
		return a.plansub() instanceof PathWalker;
	}


	

	

	
}
