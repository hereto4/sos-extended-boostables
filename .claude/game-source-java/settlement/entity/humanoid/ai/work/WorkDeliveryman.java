package settlement.entity.humanoid.ai.work;

import static settlement.main.SETT.ROOMS;

import settlement.entity.humanoid.HPoll;
import settlement.entity.humanoid.HPoll.HPollData;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.entity.humanoid.ai.util.AIPlanResourceMany;
import settlement.main.SETT;
import settlement.misc.util.RESOURCE_TILE;
import settlement.misc.util.TILE_STORAGE;
import settlement.room.infra.logistics.MoveJob;
import settlement.room.infra.logistics.MoveJob.ROOM_MOVEJOBBER;
import settlement.room.main.Room;
import settlement.room.main.RoomBlueprintIns;
import settlement.room.main.RoomInstance;
import snake2d.LOG;
import snake2d.util.misc.CLAMP;
import util.text.D;

final class WorkDeliveryman extends PlanBlueprint {

	private final boolean standAround;
	
	private static CharSequence ¤¤storing = "Storing Goods";
	private static CharSequence ¤¤waiting = "Waiting for orders";
	
	static {
		D.ts(WorkDeliveryman.class);
	}
	
	protected WorkDeliveryman(AIModule_Work module, PlanBlueprint[] map, RoomBlueprintIns<?> b, boolean standAround) {
		super("work_delivery_"+b.key, module, b, map);
		this.standAround = false;
	}

	@Override
	public AISubActivation init(Humanoid a, AIManager d) {
		
		
		
		RoomInstance i = (RoomInstance) work(a);
		ROOM_MOVEJOBBER jobber = (ROOM_MOVEJOBBER) i;
		
		MoveJob j = jobber.moveJob(a);

		if (j == null) {
			if (standAround)
				return standing.set(a, d);
			return null;
		}

		if (!d.path.request(a.tc(), j.source)) {
			LOG.ln("NAY " + a.tc() + " " + j.source);
			return null;
		}
		
		Room room = SETT.ROOMS().map.get(j.dest);
		if (room == null) {
			fuckup(j, a);
		}
		TILE_STORAGE st = room.storage(j.dest.x(), j.dest.y());
		if (st == null) {
			fuckup(j, a);
		}
		if (st.resource() != j.res) {
			System.err.println(st.resource() + " " + j.res);
			fuckup(j, a);
		}
		if (j.maxAm <= 0) {
			fuckup(j, a);
		}
		if (st.storageReservable() < j.maxAm) {
			fuckup(j, a);
		}
		
		j.maxAm = CLAMP.i(j.maxAm, 0, 0b0011_1111);
		
		st.storageReserve(j.maxAm);
		
		if (RESOURCE_TILE.GETTER.reserve(j.stored, j.prio, j.res, j.source.x(), j.source.y(), 1) == 0) {
			fuckup(j, a);
		}
		

		d.planByte1 = (byte) j.maxAm;
		d.planTile.set(j.dest.x(), j.dest.y());
		AISubActivation s = fetch.activateFound(a, d, j.res, d.planByte1, j.stored, j.prio);
		if (s == null) {
			unreserve(a, d);
		}
		return s;
		
	}
	
	private void fuckup(MoveJob j, Humanoid a) {
		System.err.println(j.res);
		System.err.println(j.source);
		System.err.println(j.dest);
		System.err.println(j.maxAm);
		throw new RuntimeException(""+work(a));
	}
	
	private final AIPlanResourceMany fetch = new AIPlanResourceMany(this, 48) {
		
		@Override
		public AISubActivation next(Humanoid a, AIManager d) {
			d.planByte2 = resource(a, d).bIndex();
			return return_resource.set(a, d);
		}
		
		@Override
		public void cancel(Humanoid a, AIManager d) {
			
			unreserve(a, d);
		}
	};
	
	private void unreserve(Humanoid a, AIManager d) {
		TILE_STORAGE c = targetStorage(a, d);
		if (c != null) {
			int i = CLAMP.i(d.planByte1, 0, c.storageReserved());
			c.storageUnreserve(i);
		}
	}
	
	private TILE_STORAGE targetStorage(Humanoid a, AIManager d) {
		Room r = ROOMS().map.get(d.planTile);
		if (r != null)
			return r.storage(d.planTile.x(), d.planTile.y());
		return null;
	}
	
	private final Resumer return_resource = new Resumer(¤¤storing) {

		@Override
		public AISubActivation setAction(Humanoid a, AIManager d) {
			if (!con(a, d)) {
				can(a, d);
				return WAIT_AND_EXIT.set(a, d);
			}
			return AI.SUBS().walkTo.coo(a, d, d.planTile);
		}
		
		@Override
		public AISubActivation res(Humanoid a, AIManager d) {
			
			if (!con(a, d)) {
				can(a, d);
				return WAIT_AND_EXIT.set(a, d);
			}
			TILE_STORAGE c = targetStorage(a, d);
			int am = d.resourceA();
			am = CLAMP.i(am, 0, c.storageReserved());
			c.storageDeposit(am);
			
			int res = d.planByte1-am;
			if (res > 0)
				c.storageUnreserve(res);
			
			if (d.resourceCarried() != null)
				d.resourceAInc(-am);
			d.resourceDrop(a);
			
			int i = d.resourceA()-am;
			if (i > 0)
				d.resourceDrop(a);
			d.resourceCarriedSet(null);
			return WAIT_AND_EXIT.set(a, d);

		}

		@Override
		public boolean con(Humanoid a, AIManager d) {
			if (work(a) != null) {
				TILE_STORAGE c = targetStorage(a, d);
				
				if (c != null && c.storageReserved() > 0 && c.resource() != null && c.resource().bIndex() == d.planByte2) {
					return true;
				}
			}
			return false;
		}

		@Override
		public void can(Humanoid a, AIManager d) {
			unreserve(a, d);
			d.resourceDrop(a);
		}


	};
	
	private final Resumer standing = new Resumer(¤¤waiting) {

		@Override
		public AISubActivation setAction(Humanoid a, AIManager d) {
			if (work(a).is(a.tc()))
				return AI.SUBS(). STAND.activateRndDir(a, d, 5);
			return AI.SUBS().walkTo.room(a, d, work(a));
		}
		
		@Override
		public AISubActivation res(Humanoid a, AIManager d) {
			return null;
		}

		@Override
		public boolean con(Humanoid a, AIManager d) {
			return true;
		}

		@Override
		public void can(Humanoid a, AIManager d) {
			
		}
		
		@Override
		public double poll(Humanoid a, AIManager d, HPollData e) {
			if (e.type == HPoll.WORKING)
				return 0;
			return super.poll(a, d, e);
		}


	};
	
	@Override
	public double poll(Humanoid a, AIManager d, HPollData e) {
		if (e.type == HPoll.WORKING) {
			return getResumer(d) == standing ? 0 : 1;
		}
		return super.poll(a, d, e);
	}
	
}