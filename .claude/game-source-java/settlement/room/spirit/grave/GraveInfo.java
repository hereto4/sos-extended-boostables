package settlement.room.spirit.grave;

import init.race.RACES;
import init.race.Race;
import init.type.CAUSE_LEAVE;
import init.type.CAUSE_LEAVES;
import init.type.HTYPE;
import init.type.HTYPES;
import settlement.main.SETT;
import settlement.stats.STATS;
import settlement.thing.ThingsCorpses.Corpse;
import snake2d.util.bit.BitsLong;
import snake2d.util.misc.CLAMP;

public final class GraveInfo {

	private static final GraveInfo self = new GraveInfo();

	private BitsLong gender = 	new BitsLong	(0x000000000000000Fl);
	private BitsLong name = 	new BitsLong	(0x00000000000FFFF0l);
	private BitsLong nameC = 	new BitsLong	(0x0000000000F00000l);
	private BitsLong corpse = new BitsLong		(0x000000000000FFFFl);
	private BitsLong race = new BitsLong		(0x000000FF00000000l);
	private BitsLong type = new BitsLong		(0x00003F0000000000l);
	private BitsLong cause = new BitsLong		(0x000FC00000000000l);
	private BitsLong age = new BitsLong			(0x3FF0000000000000l);
	private BitsLong has = new BitsLong			(0x8000000000000000l);
	
	private GraveInstance ins;
	private int id;
	private long data;
	
	private GraveInfo() {
		
	}
	
	static GraveInfo get(GraveInstance instance, int id) {
		self.ins = instance;
		self.id = id;
		self.data = instance.datas[id];
		return self;
	}
	
	public CharSequence name() {
		return STATS.APPEARANCE().name(race(), type(), gender.get(data),name.get(data), nameC.get(data) );
	}

	boolean hasBody() {
		return has.get(data) > 0;
	}
	
	public Race race() {
		return RACES.all().get((int) race.get(data));
	}
	
	public HTYPE type() {
		return HTYPES.ALL().get((int)type.get(data));
	}
	
	public CAUSE_LEAVE cause() {
		return CAUSE_LEAVES.ALL().get((int)cause.get(data));
	}
	
	void clear() {
		data = has.set(data, 0);
		ins.datas[id] = data;
	}
	
	Corpse corpse() {
		if (has.get(data) > 0)
			return null;
		Corpse c = SETT.THINGS().corpses.getByIndex((short) corpse.get(data));
		if (c != null && c.findableReservedIs()) {
			if (race() == c.indu().race() && type() == c.indu().hType())
				return c;
		}
		return null;
	}
	
	public int years() {
		return (int) age.get(data);
	}
	
	void setCorpse(Corpse c) {
		data = corpse.set(data, c.index());
		data = type.set(data, c.indu().hType().index()); 
		data = race.set(data, c.indu().race().index);
		ins.datas[id] = data;
	}
	
	void bury() {
		Corpse c = corpse();
		data = has.set(data, 1);
		data = gender.set(data, STATS.APPEARANCE().gender.get(c.indu()));
		data = name.set(data, STATS.APPEARANCE().name.get(c.indu()));
		data = nameC.set(data, STATS.APPEARANCE().customName.get(c.indu()));
		data = type.set(data, c.indu().hType().index()); 
		data = race.set(data, c.indu().race().index);
		data = cause.set(data, c.cause().index());
		int a =  (int) Math.ceil(STATS.POP().age.years.getD(c.indu()));
		a = CLAMP.i(a, 0, (int) age.mask);
		data = age.set(data, a);
		ins.datas[id] = data;
	}
	
	
}
