package settlement.entity.humanoid.ai.main;

import static settlement.main.SETT.ENTITIES;
import static settlement.main.SETT.THINGS;

import java.io.IOException;

import game.GAME;
import init.constant.C;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.settings.S;
import init.type.CAUSE_ARRIVE;
import init.type.CAUSE_LEAVE;
import init.type.HTYPE;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.HEvent;
import settlement.entity.humanoid.HEvent.HEventData;
import settlement.entity.humanoid.HPoll.HPollData;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.Humanoid.HumanoidResource;
import settlement.entity.humanoid.ai.main.AI.AIElement;
import settlement.entity.humanoid.ai.main.AIPLAN.AiPlanActivation;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.entity.humanoid.spirte.HSprite;
import settlement.path.path.SPath;
import snake2d.LOG;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Coo;
import snake2d.util.datatypes.ShortCoo;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sprite.text.Str;
import util.gui.misc.GBox;

public final class AIManager extends HumanoidResource implements HAI {

	final long[] longs = new long[AI.data().longCount()];

	private AISTATE state = null;
	private AISUB sub = null;
	private AIPLAN plan = null;
	private AISUB subInter = null;
	private byte stateI = 0;

	/**
	 * Use internally by an AIPLAN. DO not touch
	 */
	byte planResumerByte;
	/**
	 * Free to use
	 */
	public byte planByte1;
	public byte planByte2;
	public byte planByte3;
	public byte planByte4;
	public final ShortCoo planTile = new ShortCoo();
	public int planObject;

	public float X;
	public float Y;

	private int otherEntity = -1;
	public float stateTimer = 0;
	public short subPathByte = 0;
	public byte subPathByte2;
	public byte subByte = 0;

	private byte interType = -1;

	private byte subByteI = 0;
	private short subPathByteI = 0;
	private byte subPathByte2I;

	private byte resource = -1;
	private byte resourceA = 0;
	
	public int lastCollision; 
	
	public final SPath path = new SPath();

	public AIManager(Humanoid h) {
		setPlan(AI.first().activate(h, this), h);
	}

	public AIManager(Humanoid h, FileGetter file) throws IOException {
		stateI = file.b();
		planResumerByte = file.b();
		planByte1 = file.b();
		planByte2 = file.b();
		planByte3 = file.b();
		planByte4 = file.b();
		planTile.load(file);
		planObject = file.i();
		X = file.f();
		Y = file.f();
		lastCollision = file.i();
		otherEntity = file.i();
		stateTimer = file.f();
		subPathByte2 = file.b();
		subByte = file.b();
		subPathByte = file.s();

		interType = file.b();
		subByteI = file.b();
		subPathByteI = file.s();
		subPathByte2I = file.b();

		resource = (byte) RESOURCES.map().loader().loadI(file);
		resourceA = file.b();
		if (resourceCarried() == null)
			resourceA = 0;
		path.load(file);

		if (!loadAI(file)) {
			setPlan(AI.first().activate(h, this), h);
		}
		
	}

	private boolean loadAI(FileGetter file) throws IOException {
		AI.data().loader().load(this, file);
		AIElement state = AI.load(file.i());
		AIElement sub = AI.load(file.i());
		AIElement plan = AI.load(file.i());
		AIElement subInter = AI.load(file.i());
		
		if (subInter instanceof AISUB) {
			this.subInter = (AISUB) subInter;
		}

		if (state != null && sub != null && plan != null) {
			if (state instanceof AISTATE && sub instanceof AISUB && plan instanceof AIPLAN) {
				this.state = (AISTATE) state;
				this.sub = (AISUB) sub;
				this.plan = (AIPLAN) plan;
				return true;

			}
		}
		return false;
	}

	@Override
	public void save(FilePutter file) {


		file.b(stateI);
		file.b(planResumerByte);
		file.b(planByte1);
		file.b(planByte2);
		file.b(planByte3);
		file.b(planByte4);
		planTile.save(file);
		file.i(planObject);
		file.f(X);
		file.f(Y);
		file.i(lastCollision);
		file.i(otherEntity);
		file.f(stateTimer);
		file.b(subPathByte2);
		file.b(subByte);
		file.s(subPathByte);

		file.b(interType);

		file.b(subByteI);
		file.s(subPathByteI);
		file.b(subPathByte2I);

		RESOURCES.map().saver().save(resourceCarried(), file);
		file.b(resourceA);
		path.save(file);
		
		AI.data().saver().save(this, file);
		file.i(AI.save(state));
		file.i(AI.save(sub));
		file.i(AI.save(plan));
		file.i(AI.save(subInter));
	}

	private boolean setPlan(AiPlanActivation p, Humanoid a) {
		if (p == null) {
			return false;
		}

		AIPLAN plan = p.plan();
		AISubActivation sub = p.sub();

		if (plan == null || sub == null) {
			throw new RuntimeException("" + plan + " " + sub);
		}

		this.plan = plan;
		setSub(sub);
		return true;
	}

	private boolean setSub(AISubActivation s) {
		if (s == null) {
			return false;
		}

		sub = s.get();
		state = s.state();
		if (state == null)
			throw new RuntimeException(sub.getClass().getName());

		return true;
	}

	public void interrupt(Humanoid a, HEvent.HEventData event) {

		this.interType = (byte) event.event.ordinal();
		if (this.subInter == null) {

			this.subInter = this.sub;
			this.subByteI = this.subByte;
			this.subPathByteI = this.subPathByte;
			this.subPathByte2I = this.subPathByte2;
			this.sub = null;
			this.state = null;
		} else {
			sub.cancel(a, this);
		}
	}

	public void overwrite(Humanoid a, AISubActivation sub) {
		if (!setSub(sub))
			debug(a, "nono");
	}

	public void changeType(Humanoid a, HTYPE t, CAUSE_LEAVE leave, CAUSE_ARRIVE arr) {
		AIPLAN plan = AI.plans().NOP;
		sub.cancel(a, this);
		if (subInter != null) {
			this.subByte = this.subByteI;
			this.subPathByte = this.subPathByteI;
			this.subPathByte2 = this.subPathByte2I;
			sub = subInter;
			sub.cancel(a, this);
		}
		this.plan.cancel(a, this);
		this.subInter = null;
		this.interType = -1;
		AiPlanActivation p = plan.activate(a, this);

		AI.modules().cancel(a, this);
		if (!setPlan(p, a)) {
			throw new RuntimeException();
		}
		HTYPE prev = a.indu().hType();
		a.indu().hTypeSet(a, t, leave, arr);
		AI.modules().init(a, this, prev, t);
	}

	public void overwrite(Humanoid a, AIPLAN plan) {
		sub.cancel(a, this);
		if (subInter != null) {
			this.subByte = this.subByteI;
			this.subPathByte = this.subPathByteI;
			this.subPathByte2 = this.subPathByte2I;
			sub = subInter;
			sub.cancel(a, this);
		}
		this.plan.cancel(a, this);
		this.subInter = null;
		this.interType = -1;
		AiPlanActivation p = plan.activate(a, this);

		if (!setPlan(p, a)) {
			newPlan(a);
		}

	}

	public AISubActivation resumeOtherPlan(Humanoid a, AIPLAN plan) {
		sub.cancel(a, this);
		if (subInter != null) {
			this.subByte = this.subByteI;
			this.subPathByte = this.subPathByteI;
			this.subPathByte2 = this.subPathByte2I;
			sub = subInter;
			sub.cancel(a, this);
		}
		this.plan.cancel(a, this);
		this.subInter = null;
		this.interType = -1;
		AiPlanActivation p = plan.activate(a, this);
		if (p == null) {
			p = AI.plans().NOP.activate(a, this);
			plan = AI.plans().NOP;
		}
		AISubActivation sub = p.sub();
		this.plan = plan;
		return sub;

	}
	
	public AISTATE resumeOtherPlanState(Humanoid a, AIPLAN plan) {
		sub.cancel(a, this);
		if (subInter != null) {
			this.subByte = this.subByteI;
			this.subPathByte = this.subPathByteI;
			this.subPathByte2 = this.subPathByte2I;
			sub = subInter;
			sub.cancel(a, this);
		}
		this.plan.cancel(a, this);
		this.subInter = null;
		this.interType = -1;
		AiPlanActivation p = plan.activate(a, this);
		if (p == null) {
			p = AI.plans().NOP.activate(a, this);
			plan = AI.plans().NOP;
		}
		AISubActivation sub = p.sub();
		this.plan = plan;
		this.sub = sub.get();
		this.state = sub.state();
		return this.state;

	}

	public void overwrite(Humanoid a, AISTATE state) {
		this.state = state;
		if (state == null) {
			throw new RuntimeException();
		}

	}

	boolean isInterrupted() {
		return subInter != null;
	}

	@Override
	protected void update(Humanoid a, double ds) {


		
		if (state == null) {
			debug(a, "State!");
		}

		state.sprite(a).tick(a, ds);
		
		
		if (state.update(a, this, ds)) {
			
			return;
		}
		// opti.time(a, this);

		setNextState(a, ds);
		// opti.flush(a);
		return;

	}

	@Override
	protected void update(Humanoid a, int updateI, boolean newDay) {

		AIModules.update(a, this, newDay, HumanoidResource.byteDelta, (updateI & 0x0FF));

		if (state == null) {
			debug(a, "State!");
		}
		return;

	}

	private boolean setNextState(Humanoid a, double ds) {

		if (stateI != 30)
			stateI++;

		if (subInter != null) {
			return handleIterruption(a);
		}

		if (sub == null)
			LOG.ln(plan.className + " " + planResumerByte);

		state = sub.resume(a, this);

		if (plan == null)
			LOG.err("REMOVE this " + sub.name(a, this));

		if (state != null) {
			if (stateI == 30) {
				stateI = 0;
				if (!plan.shouldContinue(a, this)) {
//					st.clear();
//					plan.name(a, this, st);

//					if ((subPathByte & 0x0FF) < SFinderRoomService.all().size() && 
//							SFinderRoomService.get(subPathByte) != null 
//							&& SFinderRoomService.get(subPathByte).getReserved(path.destX(), path.destY()) != null 
//							&& SFinderRoomService.get(subPathByte).getReserved(path.destX(), path.destY()).findableReservedIs()) {
//						debug(a, SFinderRoomService.get(subPathByte) + " " + path.toDebugString());
//						LOG.ln("1: " + plan + " " + st + " " + a.id() + path.toDebugString());
//					}
					plan.cancel(a, this);
					sub.cancel(a, this);
					resourceDrop(a);
					newPlan(a);
				}

			}
		} else {
			if (plan == null) {
				LOG.ln(sub + " " + sub.name(a, this));
				debug(a, "WHAT?");
			}

//			if (plan instanceof AIPLAN.PLANRES && planResumerByte >= ((AIPLAN.PLANRES)plan).resumers.size()) {
//				LOG.ln(plan + " " + sub + " " + ((AIPLAN.PLANRES)plan).resumers.get(0) + " " + ((AIPLAN.PLANRES)plan).resumers.get(0).index);
//			}

			if (!sub.isSuccessful(a, this) || !plan.shouldContinue(a, this)) {
				if (S.get().developer && (plan.notifyIfSubFails() || !plan.shouldContinue(a, this)))
					debug(a, "hello " + " s:" + sub.isSuccessful(a, this) + " p:" + plan.shouldContinue(a, this));
				plan.cancel(a, this);
				sub.cancel(a, this);
				resourceDrop(a);
				newPlan(a);
			} else {
				AISubActivation s = plan.resume(a, this);
				if (!setSub(s)) {
					resourceDrop(a);
					newPlan(a);
				}

			}
		}

		return true;
	}

	private boolean handleIterruption(Humanoid a) {
		state = sub.resume(a, this);
		if (state != null)
			return true;

		sub = subInter;
		// inter = (HInterractor) AI.get(subInterInterIndex);
		subInter = null;
		subByte = subByteI;
		this.subPathByte = this.subPathByteI;
		this.subPathByte2 = this.subPathByte2I;
		byte it = interType;
		interType = -1;
		state = sub.resumeInterrupted(a, this, HEvent.all.get(it));

		if (state != null)
			return true;

		sub.cancel(a, this);
		if (setSub(plan.resumeFailed(a, this, HEvent.all.get(it))))
			return true;

		plan.cancel(a, this);
		sub = null;
		newPlan(a);
		return true;
	}

	private void newPlan(Humanoid a) {
		setPlan(AI.modules().getNextPlan(a, this), a);
	}

	public AIPLAN plan() {
		return plan;
	}

	public AISUB plansub() {
		return sub;
	}

	public AISTATE state() {
		return state;
	}

	@Override
	public RESOURCE resourceCarried() {
		if (resource >= 0)
			return RESOURCES.ALL().get(resource);
		return null;
	}

	public void resourceCarriedSet(RESOURCE r) {
		if (r == null) {
			resource = -1;
			resourceA = 0;
		} else {
			if (resource == r.bIndex()) {
				resourceA++;
			} else {
				resource = (byte) r.bIndex();
				resourceA = 1;
			}
		}
	}

	public void resourceAInc(int a) {
		if (resourceCarried() == null)
			throw new RuntimeException();
		resourceA += a;
		if (resourceA <= 0)
			resource = -1;
	}

	@Override
	public int resourceA() {
		return resourceA;
	}

	public void resourceDrop(Humanoid a) {
		if (resource >= 0 && resourceA > 0) {
			THINGS().resources.create(a.physics.tileC(), resourceCarried(), resourceA);
			resource = -1;
			resourceA = 0;
		}
	}

	public Humanoid otherEntity() {
		ENTITY e = ENTITIES().getByID(otherEntity);
		if (e != null && e instanceof Humanoid)
			return (Humanoid) e;
		otherEntity = -1;
		return null;
	}

	public Humanoid otherEntitySet(Humanoid o) {
		if (o == null)
			otherEntity = -1;
		else
			otherEntity = o.id();
		return o;
	}

	@Override
	protected void cancel(Humanoid a) {
		sub.cancel(a, this);

		if (subInter != null) {
			sub = subInter;
			subByte = subByteI;
			this.subPathByte = this.subPathByteI;
			this.subPathByte2 = this.subPathByte2I;
			sub.cancel(a, this);
			subInter = null;
		}

		plan.cancel(a, this);
		plan.remove(a, this);

		AI.modules().cancel(a, this);
		resourceDrop(a);

		AiPlanActivation pa = AI.plans().dead.activate(a, this);
		plan = pa.plan();
		AISubActivation ac = pa.sub();
		sub = ac.get();
		state = ac.state();
	}

	public void muster(Humanoid a) {
		sub.cancel(a, this);

		if (subInter != null) {
			sub = subInter;
			subByte = subByteI;
			this.subPathByte = this.subPathByteI;
			this.subPathByte2 = this.subPathByte2I;
			sub.cancel(a, this);
			subInter = null;
		}

		plan.cancel(a, this);

		resourceDrop(a);

		newPlan(a);
	}

	public void debug(Humanoid a, CharSequence message) {
		if (S.get().developer) {
			String n = System.getProperty("line.separator");
			String res = message + n;
			res += "id: " + a.id() + " ctile: " + a.physics.tileC().x() + " " + a.physics.tileC().y() + n + " "
					+ ((int) (X) >> C.T_SCROLL) + " " + ((int) (Y) >> C.T_SCROLL);
			res += "Plan: ";
			if (plan == null)
				res += "null" + n;
			else
				res += plan.debug(a, this) + " plantile: " + planTile.x() + " " + planTile.y() + n;
			res += "Sub: ";
			if (sub == null)
				res += "null" + n;
			else
				res += sub + " " + subByte + n + " " + sub.name(a, this) + " " + sub.key;
			res += n + "inter: ";
			if (interType == -1)
				res += "null" + n;
			else
				res += HEvent.all.get(interType).name() + n;

			if (state != null)
				res += "State: " + state + " " + state.key + " " + n;
			else
				res += "No state! " + n;
			res += path.toDebugString();
			res += n + "remove: " + a.isRemoved();
			res += "isDead: " + dead;
			res += n + "res: " + resourceCarried();
			res += n + "inter: " + subInter;
			GAME.Notify(res);
		}

	}

	@Override
	public void getOccupation(Humanoid a, Str string) {
		if (subInter != null)
			string.add(sub.name(a, this));
		else
			plan().name(a, this, string);
	}

	private final static Coo dest = new Coo();

	@Override
	public COORDINATE getDestination() {
		if (!AI.SUBS().walkTo.isWalking(this))
			return null;
		dest.set(path.destX(), path.destY());
		return dest;
	}

	public SPath path() {
		return path;
	}

	public HSprite sprite(Humanoid h) {
		return state().sprite(h);
	}

	@Override
	public void hoverInfoSet(Humanoid a, GBox text) {

		if (S.get().developer) {
			text.NL();
			text.text(plan.className);
			if (plan instanceof AIPLAN.PLANRES) {
				text.text(((AIPLAN.PLANRES) plan).getResumer(this).getClass().getName());
				text.add(text.text().add(planResumerByte));
			} else
				LOG.ln("false");
			text.NL();
			text.text(sub.className);
			text.NL();
			text.text(state.className);
			text.NL();
			text.text(interType == -1 ? "" : HEvent.all.get(interType).name());
			text.NL();

			if (sub.name(a, this) != null)
				text.text(sub.name(a, this));
			text.text(state().name());
		}

	}

	public boolean event(Humanoid h, HEventData e) {
		if (subInter != null) {
			return sub.event(h, this, e);
		}
		return plan.event(h, this, e);
	}

	public double poll(Humanoid h, HPollData e) {
		if (subInter != null) {
			return sub.poll(h, this, e);
		}
		return plan.poll(h, this, e);
	}

	@Override
	protected void add(Humanoid h, CAUSE_ARRIVE a) {
		AI.modules().init(h, this, null, h.indu().hType());
		setPlan(AI.first().activate(h, this), h);

	}

}
