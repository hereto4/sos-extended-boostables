package game.battle.thread.order;

import static game.battle.thread.order.BattleOrderUpdater.Plan.div;

import java.io.IOException;
import java.util.Arrays;

import game.battle.Army;
import game.battle.div.Div;
import game.battle.formation.DivFormationImp;
import game.battle.formation.DivPositionCopyable;
import game.battle.thread.order.BattleOrderTask.DIVTASK;
import game.time.TIME;
import init.constant.Config;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.INDEXED;
import snake2d.util.sets.LISTE;
import snake2d.util.sprite.text.Str;
import util.data.DataOSimple;
import util.data.INT_O.INT_OE;

class BattleOrderUpdater implements SAVABLE{

	private final PlanData[] datas = new PlanData[Config.battle().DIVISIONS_PER_BATTLE];
	private final Tools tools = new Tools(datas);
	
	public final ArrayListGrower<Plan> all = new ArrayListGrower<Plan>();
	public final PlanWalkToDest walk_to_dest;
	public final PlanAttackDiv attack;
	public final PlanAttackTile attackTile;
	public final PlanCharge charge;
	public final PlanFireDiv range;
	public final PlanStop stop;
	
	public final Plan[] planmap = new Plan[DIVTASK.all.size()];
	
	BattleOrderUpdater(){

		Data d = null;
		int lc = 0;
		
		d = new Data();
		walk_to_dest = new PlanWalkToDest(tools, all, d);
		if (d.longCount() > lc)
			lc = d.longCount();
		
		d = new Data();
		attack = new PlanAttackDiv(tools, all, d);
		if (d.longCount() > lc)
			lc = d.longCount();
		
		d = new Data();
		range = new PlanFireDiv(tools, all, d);
		if (d.longCount() > lc)
			lc = d.longCount();
		
		d = new Data();
		stop = new PlanStop(tools, all, d);
		if (d.longCount() > lc)
			lc = d.longCount();
		
		d = new Data();
		attackTile = new PlanAttackTile(tools, all, d);
		if (d.longCount() > lc)
			lc = d.longCount();
		
		d = new Data();
		charge = new PlanCharge(tools, all, d);
		if (d.longCount() > lc)
			lc = d.longCount();
		
		
		for (int i = 0; i < datas.length; i++)
			datas[i] = new PlanData(lc);
		
		for (Plan p : all) {
			if (planmap[p.divtask.ordinal()] != null)
				throw new RuntimeException();
			planmap[p.divtask.ordinal()] = p;
		}
		
		for (Plan p : planmap) {
			if (p == null)
				throw new RuntimeException();
		}
			
	}
	
	@Override
	public void save(FilePutter file) {
		for (PlanData d : datas)
			d.save(file);
		
	}

	@Override
	public void load(FileGetter file) throws IOException {
		for (PlanData d : datas)
			d.load(file);
	}

	@Override
	public void clear() {
		for (PlanData d : datas)
			d.clear();
	}
	
	static int inter;
	
	public DivFormationImp update(Div div, BattleOrder o, DivFormationImp prev) {
		
		if (div.index() == 0)
			PlanWalkAbs.amountOfPaths = 0;
		
		o.dest.get(Plan.dest);
		if (!div.active()) {
			if (Plan.dest.deployed() == 0) {
				Plan.task.stop(div);
				o.task.set(Plan.task);
				Plan.dest.clear();
				return Plan.dest;
			}else {
				Plan.task.move(div);
				o.task.set(Plan.task);
				return Plan.dest;
			}
			
			
		}
		
		Plan.m = datas[div.index()];
		Plan.order = o;
		Plan.div = div;
		Plan.men = div.menNrOf();
		Plan.unreachable = div.reporter.unreachable();
		Plan.a = div.army();
		
		Plan.current.copyposition(div.current());
		o.path.get(Plan.path);
		o.task.get(Plan.task);
		Plan.prev.copy(prev);
		Plan.nextPos = null;
		Plan.charging = false;
		Plan.shouldBreak = false;
		Plan.chargeSpeed = false;
		
		Plan p = plan(Plan.m);
		
		
		if (p != Plan.m.plan()) {
			p.init();
			Plan.m.planI = p.index();
		}
		
		
		long now = (long) (TIME.currentSecond()*1000);
		now &= 0x00FFFFFFF;
		int millis = (int) (now - Plan.m.lastUpdate);
		
		
		
		Plan.m.lastUpdate = (int) now;
		p.update(millis);

		
		div.settings().charging = Plan.charging;
		div.settings().shouldbreak = Plan.shouldBreak;
		div.settings().chargeSpeed = Plan.chargeSpeed;
		inter++;
		
		
		
		return Plan.nextPos;
		
	}
	
	private Plan plan(PlanData d) {
		if (div.status().isFighting() && tools.div.isCloseToFighting() && !Plan.task.orderedWhenFighting()) {
			if (!planmap[Plan.task.task().ordinal()].continueWhenFighting()) {
				Plan.task.stop(div);
				Plan.order.task.set(Plan.task);
			}
		}
		
		return planmap[Plan.task.task().ordinal()];
	}
	
	
	static class Data extends DataOSimple<PlanData> {

		@Override
		protected long[] data(PlanData t) {
			return t.data;
		}
		
	}
	
	static abstract class Plan implements INDEXED{

		public final Tools t;
		private final int index;
		
		public final DIVTASK divtask;
		
		public static PlanData m;
		public static BattleOrder order;
		public static Div div;
		public static int men;
		public static int unreachable;
		public static Army a;
		
		public static final DivPositionCopyable current = new DivPositionCopyable();
		public static final DivFormationImp dest = new DivFormationImp();
		public static final BattleOrderTask task = new BattleOrderTask();
		public static final BattleOrderPath path = new BattleOrderPath();
		public static final DivFormationImp prev = new DivFormationImp();
		public static DivFormationImp nextPos;
		
		public static boolean charging;
		public static boolean shouldBreak;
		public static boolean chargeSpeed;
		
		
		private final INT_OE<PlanData> stateI;
		private final ArrayListGrower<STATE> states = new ArrayListGrower<>();
		
		public Plan(Tools tools, LISTE<Plan> all, Data data, DIVTASK task) {
			this.t = tools;
			index = all.add(this);
			stateI = data. new DataByte();
			this.divtask = task;
		}
		
		@Override
		public int index() {
			return index;
		}
		
		abstract void init();
		abstract void update(int gameMillis);
		
		abstract boolean continueWhenFighting();
		
		protected STATE state(PlanData m) {
			return states.get(stateI.get(m));
		}
		
		abstract class STATE {
			
			final int index = states.add(this);
			public final String name;
			
			STATE(String name){
				this.name = name;
			}
			
			boolean set() {
				stateI.set(m, index);
				return setAction();
			}
			
			abstract boolean setAction();
			
			abstract void update(int gameMillis);
			
			void debugInfo(Div div, PlanData m, Str text) {
				
			}
			
		}
		
	}
	
	final class PlanData implements SAVABLE{

		final long[] data;
		private int planI = -1;
		private int lastUpdate;
		
		PlanData(int size){
			this.data = new long[size];
		}
		
		public Plan plan() {
			if (planI == -1)
				return null;
			return all.get(planI);
		}

		@Override
		public void save(FilePutter file) {
			file.i(planI);
			file.lsE(data);
			file.i(lastUpdate);
		}

		@Override
		public void load(FileGetter file) throws IOException {
			planI = file.i();
			file.lsE(data);
			lastUpdate = file.i();
		}
		
		@Override
		public void clear() {
			Arrays.fill(data, 0);
			planI = -1;
			lastUpdate = 0;
		}
		
	}

	private int iii = 0;
	private String prevState;
	public void debug(Div div, Str text) {
		PlanData d = datas[div.index()];
		Plan p = d.plan();
		if (p == null)
			return;
		text.add(d.plan().getClass().getSimpleName());
		text.s();
		text.add('>');
		text.s();
		if (iii++ > 60 || prevState == null) {
			prevState = d.plan().state(d).name;
			iii = 0;
		}
		text.add(prevState);
		text.s();
		text.add(d.plan().state(d).name);
		
		
		text.NL();
		d.plan().state(d).debugInfo(div, d, text);
	}


	
}
