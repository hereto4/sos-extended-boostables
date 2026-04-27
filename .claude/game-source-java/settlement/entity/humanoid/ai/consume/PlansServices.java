package settlement.entity.humanoid.ai.consume;

import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AIPLAN.AiPlanActivation;
import settlement.room.service.module.RoomServiceAccess;
import settlement.room.service.module.RoomServiceAccess.ROOM_SERVICE_ACCESS_HASER;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.Tree;

public class PlansServices {

	private final ArrayList<Node> plans;
	private Tree<Node> sort;
	
	
	PlansServices(SPlanAbs<?> ... plans){
		int am = 0;
		for (SPlanAbs<?> p : plans) {
			am += p.services.size();
		}
		
		this.plans = new ArrayList<Node>(am);
		
		for (SPlanAbs<?> p : plans) {
			for (ROOM_SERVICE_ACCESS_HASER s : p.services) {
				this.plans.add(new Node(p, s.service()));
			}
		}
		
		sort = new Tree<PlansServices.Node>(this.plans.size()) {
			
			@Override
			protected boolean isGreaterThan(Node current, Node cmp) {
				return current.v > cmp.v;
			}
		};
	}

	public boolean worthTrying(Humanoid a, AIManager d) {
		
		for (Node n : plans) {
			for (ROOM_SERVICE_ACCESS_HASER s : n.plan.services) {
				RoomServiceAccess b = s.service();
				if (b.accessRequest(a) && b.finder.has(a.tc()))
					return true;
			}
		}
		return false;
	}
	
	
	public AiPlanActivation getPlan(Humanoid a, AIManager d) {

		sort.clear();
		for (Node n : plans) {
			if (n.b.accessRequest(a) && n.b.finder.has(a.tc())) {
				n.v = RND.rFloat()*n.b.usage;
				if (!n.b.stats().access(a)) {
					sort.add(n);
				}
			}
		}
		
		while(sort.hasMore()) {
			Node n = sort.pollGreatest();
			d.planByte3 = (byte)n.b.room().typeIndex();
			AiPlanActivation p = n.plan.activate(a, d);
			if (p != null)
				return p;
			
		}
		
		for (Node n : plans) {
			if (n.b.accessRequest(a) && n.b.finder.has(a.tc())) {
				if (n.b.stats().access(a)) {
					sort.add(n);
				}
			}
		}
		
		while(sort.hasMore()) {
			Node n = sort.pollGreatest();
			d.planByte3 = (byte)n.b.room().typeIndex();
			AiPlanActivation p = n.plan.activate(a, d);
			if (p != null)
				return p;
			
		}
		
		for (Node n : plans) {
			n.b.clearAccess(a);
		}
		
		return null;
	}

	
	private static class Node {
		private final SPlanAbs<?> plan;
		private final RoomServiceAccess b;
		private double v;
		
		Node(SPlanAbs<?> plan, RoomServiceAccess b){
			this.plan = plan;
			this.b = b;
		}
		
	}
	
	
}
