package settlement.entity.humanoid.ai.service;

import init.type.NEED;
import init.type.NEEDS;
import init.type.NEED_E;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AIPLAN.AiPlanActivation;
import settlement.main.SETT;
import settlement.room.service.module.ROOM_SPECTATOR.ROOM_SPECTATOR_HASER;
import settlement.room.service.module.RoomServiceAccess;
import settlement.room.service.module.RoomServiceAccess.ROOM_SERVICE_ACCESS_HASER;
import snake2d.LOG;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.ArrayListGrower;

class S_Plans {

	private final double[] usageI = new double[NEEDS.ALL().size()];
	
	public final ArrayListGrower<S_Plan> all = new ArrayListGrower<>();
	public final ArrayList<ArrayListGrower<S_Plan>> needMap = new ArrayList<>(NEEDS.ALL().size());


	
	public S_Plans() {
		
		while(needMap.hasRoom())
			needMap.add(new ArrayListGrower<S_Plan>());
		
		add(new M_PlanBarber());
		add(new M_PlanBath());
		add(new M_PlanBrothel());
		add(new M_PlanHearth());
		add(new M_PlanLavatory());
		add(new M_PlanPhysician());
		add(new M_PlanWell());
		
		add(new M_PlanSpectator("speak", SETT.ROOMS().SPEAKERS));
		add(new M_PlanSpectator("stage", SETT.ROOMS().STAGES));
		add(new M_PlanSpectator("arena", SETT.ROOMS().FIGHTPITS));
		add(new M_PlanSpectator("garena", SETT.ROOMS().GARENAS));
		
		{
			add(new S_PlanShrine());
			add(new S_PlanTemple());
		}

		
		{
			add(new PlanSkinny());
		}
		
		for (int i = 0; i < usageI.length; i++) {
			if (usageI[i] > 0)
				usageI[i] = 1.0/usageI[i];
		}
		
		for (NEED n : NEEDS.ALL()) {
			if (n instanceof NEED_E)
				continue;
			if (needMap.get(n.index()).size() == 0)
				LOG.err(n.key);
		}
		
	}
	

	
	
	private void add(MPlan<? extends ROOM_SERVICE_ACCESS_HASER> plan) {
		for (ROOM_SERVICE_ACCESS_HASER ss : plan.services) {
			RoomServiceAccess n = ss.service();
			S_Plan p = new S_Plan(n.stats(), n.usage) {
				
				@Override
				public boolean hasAccess(Humanoid a, AIManager d) {
					return n.stats().access().indu().get(a.indu()) > 0;
				}

				@Override
				public AiPlanActivation getPlan(Humanoid a, AIManager d) {
					return getPlan(a, d, n.radius());
				}

				@Override
				public boolean allowed(Humanoid a, AIManager d) {
					return n.stats().accessRequest(a) && n.finder.has(a.tc());
				}

				@Override
				public boolean goodTime(Humanoid a, AIManager d) {
					return n.isGoodTime();
				}

				@Override
				public AiPlanActivation getPlan(Humanoid a, AIManager d, int dist) {
					if (n.stats().accessRequest(a) && n.finder.has(a.tc())) {
						d.planByte3 = (byte) n.room().typeIndex();
						MPlan.dist = dist;
						AiPlanActivation p = plan.activate(a, d);
						if (p != null)
							return p;
					}
					n.clearAccess(a);
					return null;
				}

			};
			add(p);
		}
	}
	
	private void add(M_PlanSpectator plan) {
		for (ROOM_SPECTATOR_HASER s : plan.services) {
			add(new S_PlanEntertain(s.service().need, s, plan));
		}
	}
	
	private S_Plan add(S_Plan p) {
		usageI[p.need.index()] += p.usage;
		needMap.get(p.need.index()).add(p);
		all.add(p);
		return p;
	}

	
	public AiPlanActivation getPlan(Humanoid a, AIManager d) {
		
		double lacksAccessTot = 0;
		double allTot = 0;
		
		for (S_Plan p : all) {
			if (!p.allowed(a, d)) {
				p.service.clearAccess(a.indu());
				continue;
			}
				
			double v = p.need.rate.get(a.indu())*usageI[p.need.index()];
			if (p.goodTime(a, d) && !p.hasAccess(a, d)) {
				lacksAccessTot += v;
			}
			allTot += v;
		}

		if (lacksAccessTot > 0) {
			lacksAccessTot = RND.rFloat()*lacksAccessTot;
			for (S_Plan p : all) {
				
				if (p.allowed(a, d) && p.goodTime(a, d) && !p.hasAccess(a, d)) {
					double v = p.need.rate.get(a.indu())*usageI[p.need.index()];
					lacksAccessTot -= v;
					if (lacksAccessTot <= 0) {
						AiPlanActivation pp = p.getPlan(a, d);
						if (pp != null)
							return pp;
						p.service.clearAccess(a.indu());
						break;
					}
				}
				
			}
		}
		
		allTot = RND.rFloat()*allTot;
		for (S_Plan p : all) {
			if (p.allowed(a, d) && p.goodTime(a, d)) {
				double v = p.need.rate.get(a.indu())*usageI[p.need.index()];
				allTot -= v;
				if (allTot <= 0) {
					AiPlanActivation pp = p.getPlan(a, d);
					if (pp == null)
						p.service.clearAccess(a.indu());
					return pp;
				}
			}
			
			
			
		}
		
		return null;
	}
}
