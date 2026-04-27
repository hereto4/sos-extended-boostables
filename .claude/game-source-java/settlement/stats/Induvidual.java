package settlement.stats;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import game.GAME;
import game.battle.Army;
import game.battle.div.Div;
import game.boosting.BOOSTABLE_O;
import game.boosting.BValue;
import game.faction.FACTIONS;
import game.faction.Faction;
import init.race.RACES;
import init.race.Race;
import init.type.CAUSE_ARRIVE;
import init.type.CAUSE_LEAVE;
import init.type.CAUSE_LEAVES;
import init.type.HCLASS;
import init.type.HTYPE;
import init.type.HTYPES;
import init.type.POP_CL;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.Humanoid.HumanoidResource;
import settlement.main.SETT;
import settlement.stats.law.PRISONER_TYPE;
import settlement.stats.stat.STAT;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.Tuple;

public final class Induvidual extends HumanoidResource implements Serializable, BOOSTABLE_O{

	private static final long serialVersionUID = 1L;
	long[] data = new long[STATS.count().longCount()];
	private byte race;
	private byte type;
	
	private boolean added = false;
	
	public Induvidual(HTYPE type, Race race){
		 
		this.race = (byte) race.index;
		this.type = (byte) type.index();
		
		if (type == HTYPES.SLAVE()) {
			GAME.count().ENSLAVED.inc(1);
		}else if (type == HTYPES.PRISONER())
			STATS.LAW().prisonerType.set(this, PRISONER_TYPE.WAR);
		STATS.get().construct(this);
		
	}
	
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    	in.defaultReadObject();
    	
    	long[] oldData = data;
    	data = new long[STATS.count().longCount()];
    	STATS.count().loader().wash(this, oldData);
    	
    	
    }
	
	public Induvidual(FileGetter p) throws IOException{
		
		STATS.count().loader().load(this, p);
		
		race = (byte) RACES.map().loader().loadB(p, FACTIONS.player().race()).index;
		type = (byte) HTYPES.MAP().loader().loadB(p, HTYPES.SUBJECT()).index();
		added = p.bool();
		if (added) {
			STATS.get().add(this);
		}
		
		
	}
	
	@Override
	public void save(FilePutter p) {
		STATS.count().saver().save(this, p);
		RACES.map().saver().save(race(), p);
		HTYPES.MAP().saver().save(hType(), p);


		p.bool(added);
	}
	
	public void copyFrom(Induvidual other) {
		STATS.get().copy.copy(this, other);
	}
	
	public void copyFromHard(Induvidual other) {
		if (added)
			throw new RuntimeException();
		
		STATS.POP().COUNT.reg(other, CAUSE_LEAVES.OTHER());

		
		STATS.get().remove(this);
		this.race = other.race;
		this.type = other.type;
		STATS.get().add(this);

		STATS.get().copy.copy(this, other);
	}
	
	@Override
	protected void add(Humanoid h, CAUSE_ARRIVE a) {
		if (added)
			return;
		added = true;
		for (Tuple<STAT, Double> t : race().stats().arrivalStats()) {
			t.a().indu().setD(this, CLAMP.d(RND.rFloat0(0.2)*t.b(), 0, 1));
		}
		STATS.get().add(this);
		
		STATS.POP().COUNT.reg(this, a);
	}
	
	@Override
	protected void cancel(Humanoid h) {
		if (!added)
			return;
		STATS.get().cancel(h);
		added = false;
	}
	
	public boolean added() {
		return added;
	}
	
	public HTYPE hType() {
		return HTYPES.ALL().get(type&0x0FF);
	}
	
	public HCLASS clas() {
		return HTYPES.ALL().get(type&0x0FF).CLASS;
	}
	
	public POP_CL popCL() {
		return HTYPES.ALL().get(type&0x0FF).CLASS.get(race());
	}
	
	/**
	 * May only be called
	 * @param h
	 * @param t
	 * @param leave
	 * @param arr
	 */
	public void hTypeSet(Humanoid h, HTYPE t, CAUSE_LEAVE leave, CAUSE_ARRIVE arr) {

		if (t != hType()) {
			HTYPE old = hType();
			STATS.POP().COUNT.reg(h.indu(), leave);
			SETT.PATH().finders.entity.report(h, -1);
			STATS.WORK().EMPLOYED.set(h, null);
			STATS.HOME().dump(h);
			STATS.HOME().GETTER.set(h, null);
			STATS.BATTLE().ROUTING.indu().set(this, 0);
			Div d = STATS.BATTLE().DIV.get(h);
			STATS.BATTLE().DIV.set(h, null);
			
			STATS.get().remove(this);
			
			this.type = (byte) t.index();
			STATS.get().add(this);
			
			SETT.PATH().finders.entity.report(h, 1);
			if (keepDiv(old) && keepDiv(t)) {
				h.setDivision(d);
			}
			STATS.POP().COUNT.reg(h.indu(), arr);
			if (t == HTYPES.SLAVE()) {
				GAME.count().ENSLAVED.inc(1);
			}else if (old == HTYPES.SLAVE() && t.player)
				GAME.count().FREED_SLAVES.inc(1);
		}
		
	}
	
	public void raceSet(Humanoid h, Race race, CAUSE_LEAVE leave, CAUSE_ARRIVE arr) {
		if (this.race != race.index()) {
			STATS.POP().COUNT.reg(h.indu(), leave);
			SETT.PATH().finders.entity.report(h, -1);
			STATS.WORK().EMPLOYED.set(h, null);
			STATS.HOME().dump(h);
			STATS.HOME().GETTER.set(h, null);
			STATS.BATTLE().ROUTING.indu().set(this, 0);
			STATS.BATTLE().DIV.set(h, null);
			
			STATS.get().remove(this);
			this.race = (byte) race.index();
			STATS.get().add(this);
			
			SETT.PATH().finders.entity.report(h, 1);
			STATS.POP().COUNT.reg(h.indu(), arr);
		}
	}
	
	private boolean keepDiv(HTYPE t) {
		return t == HTYPES.STUDENT() || t == HTYPES.SUBJECT() || t == HTYPES.RECRUIT();
	}
	
	public Race race() {
		return RACES.all().get(race);
	}
	
	public boolean i2sDead() {
		
		return false;
	}

	@Override
	protected void update(Humanoid h, int updateI, boolean newDay) {
		STATS.update(h, updateI, newDay);
	}

	@Override
	protected void update(Humanoid h, double ds) {
		throw new RuntimeException();
	}
	
	public boolean player() {
		return hType().player;
	}
	
	public boolean hostile() {
		return hType().hostile;
	}
	
	public Army army() {
		return hType().hostile ? GAME.ARMIES().enemy() : GAME.ARMIES().player();
	}
	
	public Div division() {
		return STATS.BATTLE().DIV.get(this);
	}
	
	public Faction faction() {
		if (hType() != HTYPES.ENEMY())
			return FACTIONS.player();
		return FACTIONS.otherFaction();
	}

	@Override
	public double boostableValue(BValue v) {
		return v.vGet(this);
	}


	
	
}
