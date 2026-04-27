package settlement.entity.humanoid.ai.main;

import static settlement.main.SETT.PATH;

import init.constant.C;
import init.type.CAUSE_ARRIVE;
import init.type.CAUSE_ARRIVES;
import init.type.CAUSE_LEAVE;
import init.type.HTYPE;
import init.type.HTYPES;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.battle.AIModule_Battle;
import settlement.entity.humanoid.ai.consume.AIModule_Consumption;
import settlement.entity.humanoid.ai.crime.AIModule_Crime;
import settlement.entity.humanoid.ai.danger.AIModule_Danger;
import settlement.entity.humanoid.ai.home.AIModule_Home;
import settlement.entity.humanoid.ai.idle.AIModule_Idle;
import settlement.entity.humanoid.ai.main.AIPLAN.AiPlanActivation;
import settlement.entity.humanoid.ai.service.AIModule_Service;
import settlement.entity.humanoid.ai.subject.AIModule_Subject;
import settlement.entity.humanoid.ai.types.child.AIModule_Child;
import settlement.entity.humanoid.ai.types.insane.AIModule_Insane;
import settlement.entity.humanoid.ai.types.noble.AIModule_Noble;
import settlement.entity.humanoid.ai.types.prisoner.AIModule_Prisoner;
import settlement.entity.humanoid.ai.types.recruit.AIModule_Recruit;
import settlement.entity.humanoid.ai.types.retired.AIModule_Retired;
import settlement.entity.humanoid.ai.types.rioter.AIModule_Rioter;
import settlement.entity.humanoid.ai.types.slave.AIModule_Slave;
import settlement.entity.humanoid.ai.types.student.AIModule_Student;
import settlement.entity.humanoid.ai.types.tourist.AIModule_Tourist;
import settlement.entity.humanoid.ai.work.AIModule_Work;
import settlement.room.main.ROOMA;
import settlement.stats.STATS;
import snake2d.util.datatypes.COORDINATEE;
import snake2d.util.datatypes.DIR;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import snake2d.util.sets.Tree;
import util.data.INT_O.INT_OE;

public final class AIModules {
	
	private static AIDataModule data;
	{
		data = new AIDataModule(AI.data());
	}
	public final AIModule_Idle idle = new AIModule_Idle();

	final AIModule_Subject subject = new AIModule_Subject(idle);
	final AIModule_Danger danger = new AIModule_Danger();
	public final AIModule_Work work = new AIModule_Work();
	public final AIModule_Battle battle = new AIModule_Battle();
	final AIModule noble = new AIModule_Noble();
	final AIModule slave = new AIModule_Slave();
	final AIModule_Student student = new AIModule_Student();
	private final AIModule_Crime criminal = new AIModule_Crime();
	public final AIModule_Prisoner prisoner = new AIModule_Prisoner();
	private final AIModule_Recruit recruit = new AIModule_Recruit();
	private final AIModule_Child child = new AIModule_Child();
	private final AIModule_Rioter rioter = new AIModule_Rioter();
	public final AIModule_Home home = new AIModule_Home();
	private final AIModule_Retired retired = new AIModule_Retired();
	private final AIModule_Insane insane = new AIModule_Insane();
	private final AIModule_Tourist tourist = new AIModule_Tourist();
	
	public final AIModule_Service needs = new AIModule_Service();
	
	private final LIST<AIModule> std = new ArrayList<AIModule>(0).join(needs).join(new AIModule_Consumption()).join(danger.all);
	private final LIST<AIModule> pla = std.join(subject,home);
	
	private final AIModule[][] modules = new AIModule[HTYPES.ALL().size()][];
	/**
	 * Move to crime later
	 */
	
	public COORDINATEE coo(AIManager d) {
		return data.coo(d);
	}
	
	private final Sorter2 sorter;

	AIModules() {
		
		//home, has none 7
		//work = 6
		//Service hi 5
		//early work 2
		//service low 3
		//subject activity = 2;
		//go home = 1
		//home (stay in home) = 4 (early work 2)
		
		modules[HTYPES.SUBJECT().index()] = make(pla.join(criminal, work, battle)); 
		modules[HTYPES.SLAVE().index()] = make(pla.join(work, slave));
		modules[HTYPES.RETIREE().index()] = make(pla.join(criminal, retired, battle));
		modules[HTYPES.RECRUIT().index()] = make(pla.join(criminal, recruit, battle)); 
		modules[HTYPES.STUDENT().index()] = make(pla.join(criminal, student, battle)); 
		modules[HTYPES.NOBILITY().index()] = make(pla.join(battle, noble));
		
		modules[HTYPES.TOURIST().index()] = make(std.join(tourist));
		
		modules[HTYPES.ENEMY().index()] = new AIModule[] {
			battle
		};
		modules[HTYPES.RIOTER().index()] = new AIModule[] {
			rioter,
		};
		modules[HTYPES.SOLDIER().index()] = new AIModule[] {
			battle
		};

		modules[HTYPES.PRISONER().index()] = new AIModule[] {
			prisoner,
		};
		
		modules[HTYPES.CHILD().index()] = make(danger.all.join(child));
		
		modules[HTYPES.DERANGED().index()] = make(danger.all.join(insane));
		sorter = new Sorter2();
		
		for (HTYPE t : HTYPES.ALL()) {
			
			for (AIModule m : modules[t.index()])
				m.hasType.set(t.index(), true);
			((AIModule)idle).hasType.set(t.index(), true);
		}
		
	}
	
	private AIModule[] make(LIST<AIModule> extra) {
		AIModule[] std = new AIModule[extra.size()];
		for (int i = 0; i < extra.size(); i++) {
			std[i] = extra.get(i);
		}
		return std;
	}

	public boolean isCriminal(Humanoid a) {
		return criminal.isCriminal(a);
	}
	
	public void makePrisoner(Humanoid h, AIManager m) {
		if (criminal.catchPrisoner(h))
			prisoner.makePrisoner(h, m);
	}

	void init(Humanoid a, AIManager d, HTYPE prev, HTYPE current) {
		for (AIModule m : modules[a.indu().hType().index()])
			m.init(a, d, prev, current);
	}
	
	void cancel(Humanoid a, AIManager d) {
		data.nextModule.set(d, 0);
		data.currentModule.set(d, 0);
		data.nextModulePrio.set(d, 0);
		for (AIModule m : modules[a.indu().hType().index()])
			m.cancel(a, d);
	}
	
	static void update(Humanoid a, AIManager d, boolean newDay, int byteDelta, int updateI) {
		
		AIModule next = null;
		int hprio = -1;
		
		
		AIModule[] modules = AI.modules().getModules(a, d);
		
		for (AIModule m : modules) {
			m.update(a, d, newDay, byteDelta, updateI);
			int prio = m.getPriority(a, d);
			if (prio > hprio) {
				next = m;
				hprio = prio;
			}
		}
		
		if (next != null)
			data.nextModule.set(d, next.index);
		else {
			data.nextModule.set(d, data.currentModule.get(d));
			hprio = 0;
		}
		data.nextModulePrio.set(d, hprio);
		
	}
	
	private AIModule[] getModules(Humanoid a, AIManager d) {
		return modules[a.indu().hType().index()];
	}
	
	AiPlanActivation getNextPlan(Humanoid a, AIManager d) {

		data.nextModule.set(d, 0);
		

		AIModule current = current(d);
		current.finish(a, d);
		AiPlanActivation p = untrap(a, d);
		if (p != null)
			return p;
		
		
		switchType(a, d);
		
		boolean hasCurrent = false;
		int rrr = data.timesResumed.get(d);
		data.timesResumed.set(d, 0);
		
		sorter.init(a, d, getModules(a, d));
		
		AIModule n = sorter.poll();
		while(n != null) {
			
			hasCurrent |= n == current;
			
			p = n.getPlan(a, d);
			if (p != null) {
				data.currentModule.set(d, n.index);
				data.nextModule.set(d, n.index);
				data.nextModulePrio.set(d, 0);
				return p;
			}
			n = sorter.poll();
		}
		
		if (current != null && hasCurrent) {
			p = current.resume(a, d, rrr);
			if (p != null) {
				data.timesResumed.set(d, CLAMP.i(rrr+1, 0, data.timesResumed.max(d)));
				data.currentModule.set(d, current.index);
				data.nextModule.set(d, current.index);
				data.nextModulePrio.set(d, 0);
				return p;
			}
		}
		
		p = idle.getPlan(a, d);
		data.currentModule.set(d, ((AIModule)idle).index);
		data.nextModule.set(d, ((AIModule)idle).index);
		data.nextModulePrio.set(d, 0);
		return p;
	}
	
	private AiPlanActivation untrap(Humanoid a, AIManager d) {
		if (!PATH().connectivity.is(a.physics.tileC())) {
				
			if (PATH().comps.zero.get(a.tc()) == null) {
				for (int di = 0; di < DIR.ORTHO.size(); di++) {
					DIR dd = DIR.ORTHO.get(di);
					if (PATH().connectivity.is(a.physics.tileC(), dd)){
						int x = (a.tc().x()+dd.x())*C.TILE_SIZE + C.TILE_SIZEH;
						int y = (a.tc().y()+dd.y())*C.TILE_SIZE + C.TILE_SIZEH;
						a.physics.body().moveC(x, y);
						return null;
					}
				}
				return AI.plans().unreachable.activate(a, d);
			}
			
			if (RND.oneIn(5) && a.indu().hType() != HTYPES.ENEMY())
				return AI.plans().unreachable.activate(a, d);
			
		}else {
			STATS.POP().TRAPPED.indu().set(a.indu(), 0);
			if (a.division() != null)
				a.division().reporter.reportReachable(a, true);
		}
		return null;
	}
	
	private void switchType(Humanoid a, AIManager d) {
		
		if (STATS.WORK().incap.stat.indu().get(a.indu()) == 1)
			STATS.WORK().incap.stat.indu().set(a.indu(), 0);
	
		if (a.indu().hType() == HTYPES.SUBJECT()) {
			if(STATS.WORK().RET.shoudRetire(a.indu())) {
				switchType(a, d, HTYPES.RETIREE(), null, null);
			}else if(recruit.canBecome(a, d)) {
				switchType(a, d, HTYPES.RECRUIT(), null, null);
			}else if(student.tryInit(a, d))
				switchType(a, d, HTYPES.STUDENT(), null, null);
		}else if (a.indu().hType() == HTYPES.RETIREE()) {
			if(!STATS.WORK().RET.shoudRetire(a.indu())) {
				switchType(a, d, HTYPES.SUBJECT(), null, null);
			}
		}else if (a.indu().hType() == HTYPES.RECRUIT()) {
			if (!recruit.setEmploy(a, d)) {
				switchType(a, d, HTYPES.SUBJECT(), null, null);
			}
			if(STATS.WORK().RET.shoudRetire(a.indu())) {
				switchType(a,d, HTYPES.RETIREE(), null, null);
			}
		}else if (a.indu().hType() == HTYPES.CHILD()) {
			if (STATS.POP().age.isAdult(a.indu())) {
				switchType(a,d, HTYPES.SUBJECT(), null, CAUSE_ARRIVES.BORN());
				STATS.RELIGION().setChildReligion(a);
			}
		}else if (a.indu().hType() == HTYPES.STUDENT()) {
			recruit.updateNonRecruit(a, d);
			if (!AIModule_Student.shouldContinue(a, d)) {
				switchType(a,d, HTYPES.SUBJECT(), null, null);
			}
			
			
		}
	}
	
	private void switchType(Humanoid a, AIManager d, HTYPE type, CAUSE_LEAVE leave, CAUSE_ARRIVE arr) {
		cancel(a, d);
		HTYPE prev = a.indu().hType();
		a.indu().hTypeSet(a, type, leave, arr);
		init(a, d, prev, type);
	}
	
	public static AIDataModule data() {
		return data;
	}
	
	public static AIModule next(AIManager d) {
		return AIModule.all.get(data.nextModule.get(d));
	}
	
	public static int nextPrio(AIManager d) {
		return data.nextModulePrio.get(d);
	}
	
	public static AIModule current(AIManager d) {
		AIModule m = AIModule.all.get(data.currentModule.get(d));
		if (m == null)
			return AI.modules().idle;
		return m;
	}
	
	public void evictFromRoom(Humanoid a, AIManager d, ROOMA r) {
		for (AIModule m : getModules(a, d))
			m.evictFromRoom(a, d, r);
	}
	
	private final static class Sorter2 {
		
		private final Tree<Node> sorter = new Tree<Node>(AIModule.all.size()) {

			@Override
			protected boolean isGreaterThan(Node current,
					Node cmp) {
				return current.prio < cmp.prio;
			}
			
		};
		
		private final Node[] nodes = new Node[AIModule.all.size()];
		
		Sorter2() {
			for (int i = 0; i < nodes.length; i++)
				nodes[i] = new Node();
		}
		
		void init(Humanoid a, AIManager d, AIModule[] modules) {
			sorter.clear();
			int ri = RND.rInt(modules.length);
			for (int i = 0; i < modules.length; i++) {
				AIModule m = modules[(i+ri)%modules.length];
				int prio = m.getPriority(a, d);
				if (prio <= 0)
					continue;
				Node n = nodes[i];
				n.m = m;
				n.prio = prio;
				sorter.add(n);
			}
			
		}
		
		AIModule poll() {
			if (sorter.hasMore())
				return sorter.pollSmallest().m;
			return null;
		}
	
		static class Node {
			
			int prio;
			AIModule m;
			
		}
		
	}

	public class AIDataModule {
		
		private AIManager cooD;
		public final INT_OE<AIManager> byte1;
		public final INT_OE<AIManager> byte2;
		public final INT_OE<AIManager> byte3;
		public final INT_OE<AIManager> x;
		public final INT_OE<AIManager> y;
		
		public final INT_OE<AIManager> nextModule;
		public final INT_OE<AIManager> currentModule;
		private final INT_OE<AIManager> nextModulePrio;
		private final INT_OE<AIManager> timesResumed;

		private AIDataModule(AIData data) {
			byte1 = data.new DataByte("ModuleB1");
			byte2 = data.new DataByte("ModuleB2");
			byte3 = data.new DataByte("ModuleB3");
			x = data.new DataShort("moduleX");
			y = data.new DataShort("moduleY");
			nextModule = data.new DataByte("ModuleNext");
			currentModule = data.new DataByte("ModuleCurrent");
			nextModulePrio = data.new DataByte("ModulePrio");
			timesResumed = data.new DataNibble("ModuleTimes");
		}
		
		private final COORDINATEE coo = new COORDINATEE.Abs() {
			
			@Override
			public int y() {
				return (short)y.get(cooD);
			}
			
			@Override
			public int x() {
				return (short)x.get(cooD);
			}
			
			@Override
			public void ySet(double dy) {
				y.set(cooD, (int) dy&0x0FFFF);
			}
			
			@Override
			public void xSet(double dx) {
				x.set(cooD, (int) dx&0x0FFFF);
			}
		};
		
		public COORDINATEE coo(AIManager d) {
			cooD = d;
			return coo;
		}
		
	}

	public LIST<AIModule> ALL(){
		return AIModule.all;
	}
	
}
