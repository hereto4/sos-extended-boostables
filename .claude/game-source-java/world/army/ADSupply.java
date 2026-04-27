package world.army;

import game.faction.Faction;
import game.time.TIME;
import init.resources.RESOURCE;
import init.resources.ResSupply;
import settlement.stats.equip.EquipBattle;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.INDEXED;
import util.data.INT_O.INT_OE;
import util.gui.misc.GBox;
import util.gui.misc.GText;
import util.info.GFORMAT;
import util.text.D;
import util.text.Dic;
import world.army.ADInt.ADIntImp;
import world.army.ADSupplies.ADArtillery;
import world.entity.army.WArmy;

public abstract class ADSupply implements INDEXED {

	public final static int STOCKPILE_DAYS = 6;
	
	public final CharSequence name; 
	public final RESOURCE res;
	
	
	private final INT_OE<WArmy> cached;
	private final INT_OE<Faction> cachedF;
	
	
	protected final ADIntImp current;
	protected final ADIntImp consumers;
	protected final ADIntImp consumersMax;
	protected final ADIntImp amountNeeded;
	protected final ADIntImp amountNeededMax;
	
	public final double baseMorale;
	public final double baseHealth;
	public final double consumptionPerItem;
	public final double consumptionPerUser;
	private final int index;

	ADSupply(int index, String KPrefix, ADInit init, RESOURCE res, CharSequence prefix, double consumptionPerUser, double consumptionPerItem, double morale, double health) {
		this.consumptionPerItem = consumptionPerItem;
		this.consumptionPerUser = consumptionPerUser;
		this.index = index;
		name = prefix + ": " + res.name;
		current = new ADIntImp(init, "SUPPLY_" + KPrefix + "_" +res.key, name, res.names) {
			
			@Override
			public void set(WArmy t, int i) {
				AD.power().mor(t);
				super.set(t, i);
			}
			
			@Override
			public int min(WArmy t) {
				return 0;
			}
			
		};
		consumers = new ADIntImp(init, "SUPPLY_NEEDED_" + KPrefix + "_" + res.key, name, res.names);
		consumersMax = new ADIntImp(init, "SUPPLY_CONSUMERTS_TARGET_" + KPrefix + "_" + res.key, name, res.names);
		amountNeeded = new ADIntImp(init, "SUPPLY_TARGET_" + KPrefix + "_" + res.key, name, res.names);
		amountNeededMax = new ADIntImp(init, "SUPPLY_TARGET_MAX_" + KPrefix + "_" + res.key, name, res.names);
		
		cached = init.dataA. new DataBit("SUPPLY_CACHE" + KPrefix + "_" +res.key);
		cachedF = init.dataT. new DataBit("SUPPLY_CACHE" + KPrefix + "_" +res.key);
		
		this.res = res;
		this.baseMorale = morale;
		this.baseHealth = health;
	}

	public void setChanged(WArmy a) {
		cached.set(a, 0);
		if (a.faction() != null)
			cachedF.set(a.faction(), 0);
	}
	
	@Override
	public int index() {
		return index;
	}
	
	public ADIntImp current() {
		return current;
	}
	
	
	/**
	 * consumed per user
	 * consumed per item
	 * minimum amount per user
	 * 
	 * 
	 * @param a
	 * @return
	 */
	
	public double consumedPerDayCurrent(WArmy a) {
		cache(a);
		return consumers.get(a)*consumptionPerUser + Math.min(current.get(a), minimumAmount(a))*consumptionPerItem;
	}
	
	public double consumedPerDayCurrent(Faction a) {
		cache(a);
		return consumers.faction(a)*consumptionPerUser + current.faction(a)*consumptionPerItem;
	}
	
	public double consumedPerDayTarget(WArmy a) {
		cache(a);
		return consumersMax.get(a)*consumptionPerUser + amountNeededMax.get(a)*consumptionPerItem;
	}
	
	public int targetAmount(WArmy a) {
		cache(a);
		double d = consumersMax.get(a)*consumptionPerUser*(STOCKPILE_DAYS+1);
		d += amountNeededMax.get(a) +  amountNeededMax.get(a)*consumptionPerItem*STOCKPILE_DAYS;
		return (int) Math.ceil(d);
	}
	
	public int targetAmount(Faction a) {
		cache(a);
		double d = consumersMax.faction(a)*consumptionPerUser*(STOCKPILE_DAYS+1);
		d += amountNeededMax.faction(a) + amountNeededMax.faction(a)*consumptionPerItem*STOCKPILE_DAYS;
		return (int) Math.ceil(d);
	}
	
	public int minimumAmount(WArmy a) {
		cache(a);
		double d = consumers.get(a)*consumptionPerUser;
		d += amountNeeded.get(a);
		return (int) Math.ceil(d);
	}
	
	public int minimumTarget(WArmy a) {
		cache(a);
		double d = consumersMax.get(a)*consumptionPerUser;
		d += amountNeededMax.get(a);
		return (int) Math.ceil(d);
	}
	
	public int minimumAmount(Faction a) {
		cache(a);
		double d = consumers.faction(a)*consumptionPerUser;
		d += amountNeeded.faction(a);
		return (int) Math.ceil(d);
	}
	
	public double daysStored(WArmy a) {
		return (current.get(a)-minimumAmount(a))/consumedPerDayCurrent(a);
	}
	
	public double amountValue(WArmy a) {
		return CLAMP.d((double)current.get(a)/minimumAmount(a), 0, 1);
	}
	
	public double needed(WArmy a) {
		return targetAmount(a) - current.get(a);
	}
	
	public double needed(Faction a) {
		return targetAmount(a) - current.faction(a);
	}
	
	public double moraleAdd(WArmy a) {
		cache(a);
		int men = AD.men(null).get(a);
		double d = (consumers.get(a)+1.0)/(men+1.0);
		return baseMorale*d*amountValue(a);
	}
	
	public double healthMul(WArmy a) {
		cache(a);
		int men = AD.men(null).get(a);
		double d = (consumers.get(a)+1.0)/(men+1.0);
		return 1-baseHealth*d*(1.0-amountValue(a));
	}
	
	private void cache(WArmy a) {

		if (cached.get(a) == 1)
			return;
		
		cached.set(a, 1);
		consumers.set(a, 0);
		consumersMax.set(a, 0);
		amountNeeded.set(a, 0);
		amountNeededMax.set(a, 0);
		add(a);
		
	}
	
	private void cache(Faction a) {

		if (cachedF.get(a) == 1)
			return;
		
		cachedF.set(a, 1);
		
		for (int ai = 0; ai < a.armies().all().size(); ai++) {
			cache(a.armies().all().get(ai));
		}
	}

	protected abstract void add(WArmy a);
	
	public abstract void transfer(WDIV div, WArmy old, WArmy current);
	
	private static CharSequence ¤¤affected = "consuming soldiers";
	private static CharSequence ¤¤ConsumtionRate = "Daily Consumption:";
	private static CharSequence ¤¤artD = "Needed to keep artillery functioning";
	private static CharSequence ¤¤days = "Days of Supply";
	
	static {
		D.ts(ADSupply.class);
	}
	
	public abstract void hover(GBox b, WArmy a);
	
	
	public static final class ADSupplyRes extends ADSupply {

		public final ResSupply res;
		
		ADSupplyRes(int index, ADInit init, ResSupply rs) {
			super(index, "SUPPLY", init, rs.resource, Dic.¤¤Supplies, rs.consumptionPerPersonday, rs.consumptionPerItemPerDay, rs.morale, rs.health);
			this.res = rs;
		}

		@Override
		protected void add(WArmy a) {
			for (int di = 0; di < a.divs().size(); di++) {
				ADDiv d = a.divs().get(di);
				if (d.needSupplies()) {
					this.consumers.inc(a, res.consumedMulPerDay(d.race())*d.men());
					this.consumersMax.inc(a, res.consumedMulPerDay(d.race())*d.menTarget());
					this.amountNeeded.inc(a, res.consumedMulPerDay(d.race())*res.wantedPerPerson*d.men());
					this.amountNeededMax.inc(a, res.consumedMulPerDay(d.race())*res.wantedPerPerson*d.menTarget());
				}
			}
		}

		@Override
		public void transfer(WDIV div, WArmy old, WArmy current) {

			double divAmount = res.consumedMulPerDay(div.race())*div.menTarget();
			double armyAmount = consumersMax.get(old);
			if (armyAmount == 0)
				return;
			int am = (int) (current().get(old)*(divAmount/armyAmount));
			if (am > 0) {
				current().inc(old, -am);
				current().inc(current, am);
			}
		}
		
		@Override
		public void hover(GBox b, WArmy a) {
			
			res.hover(b);
			
			b.sep();
			
			int m = 6;
			
			b.textL(¤¤affected);
			b.tab(m);
			b.add(GFORMAT.i(b.text(), consumers.get(a)));
			b.NL();
			
			b.textL(Dic.¤¤Minimum);
			b.tab(m);
			b.add(GFORMAT.i(b.text(), minimumAmount(a)));
			b.NL();
			
			b.textL(Dic.¤¤Max);
			b.tab(m);
			b.add(GFORMAT.i(b.text(), targetAmount(a)));
			b.NL();
			
			b.textL(Dic.¤¤Stored);
			b.tab(m);
			b.add(GFORMAT.iofkInv(b.text(), current().get(a), targetAmount(a)));
			b.NL();
			
			b.textL(¤¤ConsumtionRate);
			b.tab(m);
			b.add(GFORMAT.f0(b.text(), -consumedPerDayCurrent(a)));
			b.NL();
			
			b.textL(¤¤days);
			b.tab(m);
			b.add(GFORMAT.f0(b.text(), daysStored(a)));
			b.NL();
			
			if (baseMorale > 0) {
				b.textL(Dic.¤¤Morale);
				b.tab(m);
				b.add(GFORMAT.f0(b.text(), moraleAdd(a)));
				b.NL();
			}
			if (baseHealth > 0) {
				b.textL(Dic.¤¤Health);
				b.tab(m);
				GText t = b.text();
				t.add('*').s();
				b.add(GFORMAT.f1(t, healthMul(a)));
				b.NL();
			}
		}
		
		
	}
	
	public static final class ADSupplyEquip extends ADSupply {

		public final EquipBattle equip;
		
		ADSupplyEquip(int index, ADInit init, EquipBattle rs) {
			super(index, "EQUIPMENT", init, rs.resource, Dic.¤¤Equipment, 0, rs.wearRate()/16.0, 0, 0);
			this.equip = rs;
		}

		@Override
		protected void add(WArmy a) {
			for (int di = 0; di < a.divs().size(); di++) {
				ADDiv div = a.divs().get(di);
				if (div.needSupplies()) {
					amountNeededMax.inc(div.army(), div.menTarget()*div.target().equipI(equip));
					amountNeeded.inc(div.army(), div.men()*div.target().equipI(equip));
				}
			}
		}

		@Override
		public void transfer(WDIV div, WArmy old, WArmy current) {
			double divAmount = div.menTarget()*div.target().equipI(equip);
			double armyAmount = amountNeededMax.get(old);
			int am = (int) (current().get(old)*(divAmount/armyAmount));
			if (am > 0) {
				current().inc(old, -am);
				current().inc(current, am);
			}
		}
		
		@Override
		public void hover(GBox b, WArmy a) {
			b.title(name);
			b.text(equip.resource.desc);
			
			b.sep();
			
			b.textL(Dic.¤¤Minimum);
			b.tab(6);
			b.add(GFORMAT.i(b.text(), minimumAmount(a)));
			b.NL();
			
			b.textL(Dic.¤¤Max);
			b.tab(6);
			b.add(GFORMAT.i(b.text(), targetAmount(a)));
			b.NL();
			
			b.textL(Dic.¤¤Stored);
			b.tab(6);
			b.add(GFORMAT.iofkInv(b.text(), current().get(a), targetAmount(a)));
			b.NL();
			
			b.textL(¤¤ConsumtionRate);
			b.tab(6);
			b.add(GFORMAT.f0(b.text(), -consumedPerDayCurrent(a)));
			b.NL();
			
			b.textL(¤¤days);
			b.tab(6);
			b.add(GFORMAT.f0(b.text(), daysStored(a)));
			b.NL();
			
		}
		
		
	}
	
	public static final class ADSupplyArt extends ADSupply {

		public final ADArtillery art;
		public final int ri;
		
		ADSupplyArt(int index, ADInit init, ADArtillery art, RESOURCE res, int ri) {
			super(index, "ART_" + art.art.key + "_" + res.key, init, res,  art.art.info.name, 0, 0.2/TIME.years().bitConversion(TIME.days()), 0, 0);
			this.art = art;
			this.ri = ri;
		}

		@Override
		protected void add(WArmy a) {
			
			int am = (int) Math.ceil(art.target.get(a)*art.art.constructor().item(1).cost2(ri, art.art.upgrades().max()));
			amountNeeded.set(a, am);
			amountNeededMax.set(a, am);
			
		}

		@Override
		public void transfer(WDIV div, WArmy old, WArmy current) {
			
		}
		
		@Override
		public void hover(GBox b, WArmy a) {
			b.title(name);
			b.text(¤¤artD);
			
			b.sep();
			
			b.textL(Dic.¤¤Minimum);
			b.tab(6);
			b.add(GFORMAT.i(b.text(), minimumAmount(a)));
			b.NL();
			
			b.textL(Dic.¤¤Max);
			b.tab(6);
			b.add(GFORMAT.i(b.text(), targetAmount(a)));
			b.NL();
			
			b.textL(Dic.¤¤Stored);
			b.tab(6);
			b.add(GFORMAT.iofkInv(b.text(), current().get(a), targetAmount(a)));
			b.NL();
			
			b.textL(¤¤ConsumtionRate);
			b.tab(6);
			b.add(GFORMAT.f0(b.text(), -consumedPerDayCurrent(a)));
			b.NL();
			
			b.textL(¤¤days);
			b.tab(6);
			b.add(GFORMAT.f0(b.text(), daysStored(a)));
			b.NL();
			
			
			
		}
		
	}
	
}