package settlement.entity.humanoid.ai.work;

import init.resources.RESOURCE;
import init.resources.RESOURCES;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.entity.humanoid.ai.work.WorkAbs.Works;
import settlement.main.SETT;
import settlement.room.infra.transport.ROOM_TRANSPORT;
import settlement.room.infra.transport.TransportInstance;
import settlement.room.main.RoomInstance;
import settlement.stats.STATS;
import snake2d.util.datatypes.Coo;

final class WorkTransporter extends PlanBlueprint {

	private final WorkDeliveryman deliveryman;
	private final WorkAbs work;

	protected WorkTransporter(AIModule_Work module, PlanBlueprint[] map, Works w) {
		super(module, SETT.ROOMS().TRANSPORT, map);
		map[SETT.ROOMS().TRANSPORT.index()] = null;
		deliveryman = new WorkDeliveryman(module, map, blueprint, false);
		map[SETT.ROOMS().TRANSPORT.index()] = null;
		work = new WorkAbs("WorkTransportExtra", module, blueprint, map, w);
		map[SETT.ROOMS().TRANSPORT.index()] = this;
	}

	@Override
	protected double transportAmount(Humanoid a, AIManager d) {
		if (getResumer(d) == go) {
			return (double)am(d)/ROOM_TRANSPORT.MAX_LOAD;
		}
		return -1;
	}

	@Override
	public AiPlanActivation activate(Humanoid a, AIManager d) {

		//go off
		AiPlanActivation p = super.activate(a, d);
		if (p != null)
			return p;

		//prep
		p = work.activate(a, d);

		if (p != null) {
			return p;
		}

		return deliveryman.activate(a, d);
		
		//needs to prep again
		
	}

	@Override
	protected AISubActivation init(Humanoid a, AIManager d) {

//		if (STATS.WORK().WORK_TIME.indu().getD(a.indu()) > 0.7)
//			return null;
		Coo[] job = w(a).getDeliveryJob();

		if (job == null)
			return null;

		AISubActivation s = AI.SUBS().walkTo.coo(a, d, job[0]);
		if (s == null) {
			SETT.ROOMS().STATION.reserveCancel(w(a).resource(), job[1].x(), job[1].y());
			w(a).deliveryJobCancel();
			return null;
		}

		d.planByte1 = w(a).resource().bIndex();
		d.planObject = 0;
		d.planTile.set(job[1]);

		start.set(a, d);
		return s;
	}

	@Override
	public boolean shouldReportWorkFailure(Humanoid a, AIManager d) {
		return STATS.WORK().WORK_TIME.indu().getD(a.indu()) <= 0.7;
	}

	private TransportInstance w(Humanoid a) {
		RoomInstance ins = STATS.WORK().EMPLOYED.get(a);
		if (ins != null && ins instanceof TransportInstance)
			return (TransportInstance) ins;
		return null;
	}

	private RESOURCE ress(AIManager d) {
		return RESOURCES.ALL().get(d.planByte1);
	}

	private int am(AIManager d) {
		return d.planObject;
	}

	private void unreserve(AIManager d, int tx, int ty) {
		SETT.ROOMS().STATION.reserveCancel(ress(d), tx, ty);
	}

	private final Resumer start = new Resumer(blueprint.employment().verb) {

		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			return null;
		}

		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			return go.set(a, d);
		}

		@Override
		public boolean con(Humanoid a, AIManager d) {
			return w(a) != null;
		}

		@Override
		public void can(Humanoid a, AIManager d) {
			if (w(a) != null) {
				w(a).deliveryJobCancel();
			}
			unreserve(d, d.planTile.x(), d.planTile.y());
			d.planObject = 0;
		}
	};

	private final Resumer go = new Resumer(blueprint.employment().verb) {

		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			d.planObject = w(a).doDeliveryJob();
			if (d.planObject == 0) {
				start.can(a, d);
				return null;
			}
			int dx = d.planTile.x();
			int dy = d.planTile.y();
			d.planTile.set(d.path.destX(), d.path.destY());
			AISubActivation s = AI.SUBS().walkTo.coo(a, d, dx, dy);
			
			if (s == null) {
				can(a, d);
				return null;
			}
			int ran = SETT.tileRan(d.planTile.x(), d.planTile.y());
			
			SETT.HALFENTS().transports.make(a, d.planTile.x(), d.planTile.y(), ress(d), (byte) ran, false);
			return s;
		}

		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {

			SETT.ROOMS().STATION.deliver(ress(d), am(d), d.path.x(), d.path.y());
			w(a).finishDeliveryJob(d.planObject);
			d.planObject = 0;
			return null;

		}

		@Override
		public boolean con(Humanoid a, AIManager d) {
			return w(a) != null;
		}

		@Override
		public void can(Humanoid a, AIManager d) {
			if (w(a) != null) {
				w(a).finishDeliveryJob(d.planObject);
			}
			unreserve(d, d.path.destX(), d.path.destY());
			if (am(d) > 0)
				SETT.THINGS().resources.create(a.tc(), ress(d), am(d));
			d.planObject = 0;
		}
	};

}
