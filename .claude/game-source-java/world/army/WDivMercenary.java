package world.army;

import java.io.IOException;

import game.battle.util.DivType;
import game.GAME;
import game.battle.util.DIV_SETTING;
import game.battle.util.DivGeneration;
import game.faction.FACTIONS;
import game.time.TIME;
import init.constant.Config;
import init.race.RACES;
import init.race.Race;
import init.resources.RESOURCES;
import init.resources.ResSupply;
import init.type.HTYPES;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import settlement.stats.colls.StatsBattle.StatTraining;
import settlement.stats.equip.EquipBattle;
import snake2d.util.color.COLOR;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import world.entity.army.WArmy;
import world.region.RD;
import world.region.pop.RDRace;

public final class WDivMercenary extends ADDiv {
	
	static final int type = 2;
	private static final COLOR col = COLOR.ORANGE100.makeSaturated(0.5).shade(0.75);
	private byte race = 0;
	private short men;
	private short menTarget;
	private final DIV_SETTINGImp spec = new DIV_SETTINGImp();
	private float exp;
	private int costPerMan;
	private short nameI;
	private short bannerI;
	private Induvidual indu;
	float disbandTime = 0;
	byte missedPayments;
			
	WDivMercenary(int index) {
		super(index);
	}
	
	void randomize() {
		report(-1);
		
		
		
		race = (byte) RACES.all().rnd().index;
		
		double am = 0;
		for (RDRace r : RD.RACES().all) {
			am += (0.1 + r.race.physics.raiding)*r.pop.faction().get(FACTIONS.player()) +1;
		}
		double ri = RND.rFloat()*am;
		for (RDRace r : RD.RACES().all) {
			ri -= (0.1 + r.race.physics.raiding)*r.pop.faction().get(FACTIONS.player())+1;
			if (ri <= 0) {
				race = (byte) r.race.index;
				break;
			}
		}

		int dmen = 15;
		int ma = (int) Math.ceil((double)Config.battle().MEN_PER_DIVISION/dmen);
		men = (short) CLAMP.i(dmen * RND.rInt(ma), dmen, Config.battle().MEN_PER_DIVISION);
		menTarget = men;
		
		exp = (float) CLAMP.d(Math.pow(RND.rFloat(), 1.5), 0, 1);
		
		
		nameI = (short) RND.rInt(race().info.armyNames.size());
		
		DivType type = GAME.battle().types.rnd(race(), null, RND.rFloat());
		
		spec.copySettings(type, menTarget, 0.5 + 0.5*RND.rFloat(), 0.1 + 0.9*RND.rFloat());
		
		bannerSet(RND.rShort());
		
		{
			
			double costBase = race().physics.adultAt*FACTIONS.PRICE().edible();
			for (StatTraining t : STATS.BATTLE().TRAINING_ALL) {
				costBase += t.room.TRAINING_DAYS*FACTIONS.PRICE().edible()*spec.training(t);
			}
			costBase *= 0.1;
			costBase *= 0.5 + exp;
			
			double costEquip = 0;
			
			for (EquipBattle e : STATS.EQUIP().BATTLE_ALL()) {
				
				costEquip += (e.wearRate()/16.0)*spec.equipI(e)*FACTIONS.PRICE().get(e.resource);
			}
			
			double cost = costBase+costEquip;
			

			for (ResSupply s : RESOURCES.SUP().ALL) {
				
		
				cost += 1.25*FACTIONS.PRICE().get(s.resource)*s.consumptionPerPersonday*s.consumedMulPerDay(race())*s.consumptionPerPersonday;
			}
			
			costPerMan = (int) Math.ceil(cost);
			
		}
		
		//costPerMan = (short) (2 + 0.5*RND.rFloat1(0.2)*100*RESOURCES.ALL().size()*((double)provess()/(100.0*men)));
		
		indu = new Induvidual(HTYPES.SOLDIER(), race());
		STATS.NEEDS().DIRTINESS.setD(indu, RND.rExpo()*0.5);
		
		report(1);
	}

	@Override
	protected void armyChange(WArmy old, WArmy newW) {
		if (newW == null) {
			disbandTime = TIME.secondsPerDay()*16*2;
		}else if (old == null && newW != null && newW.faction() == FACTIONS.player())
			;missedPayments = -1; //lastPaymentTime = (int) TIME.currentSecond();
		super.armyChange(old, newW);
	}
	
	public boolean disbanded() {
		return disbandTime > 0;
	}
	
	@Override
	public void save(FilePutter file) {
		super.save(file);
		file.b(race);
		file.s(men);
		file.s(menTarget);
		file.f(exp);
		file.f(disbandTime);
		file.i(costPerMan);
		file.s(nameI);
		file.s(bannerI);
		spec.save(file);
		file.bool(indu != null);
		if (indu != null)
			indu.save(file);
		file.b(missedPayments);
	}

	@Override
	public void load(FileGetter file) throws IOException {
		super.load(file);
		race = file.b();
		men = file.s();
		menTarget = file.s();
		exp = file.f();
		disbandTime = file.f();
		costPerMan = file.i();
		nameI = file.s();
		bannerI = file.s();
		spec.load(file);
		if (file.bool())
			indu = new Induvidual(file);
		missedPayments = file.b();
	}

	public Induvidual cheif() {
		return indu;
	}


	@Override
	public int men() {
		return men;
	}
	
	@Override
	public void menSet(int m) {
		report(-1);
		men = (short) CLAMP.i(m, 0, menTarget());
		report(1);
	}
	
	@Override
	public void resolve(Induvidual[] hs) {
		double exp = 0;
		for (Induvidual i : hs)
			exp += STATS.BATTLE().COMBAT_EXPERIENCE.indu().getD(i);
		if (hs.length > 0)
			exp /= hs.length;
		resolve(hs.length, exp);
	}
	
	@Override
	public void resolve(int surviviors, double experiencePerMan) {
		menSet(surviviors);
		report(-1);
		this.exp = (float) CLAMP.d(experiencePerMan, 0, 1);
		report(1);
	}
	
	@Override
	public int menTarget() {
		return menTarget;
	}
	
	@Override
	public double training(StatTraining tr) {
		return spec.training[tr.tIndex];
	}
	
	@Override
	public double experience() {
		return exp;
	}
	
	@Override
	public Race race() {
		return RACES.all().get(race & 0x0FF);
	}

	@Override
	public int daysUntilMenArrives() {
		return 1;
	}
	
	@Override
	public int costPerMan() {
		return costPerMan;
	}
	
	@Override
	public int type() {
		return type;
	}

	
	@Override
	public CharSequence name() {
		return race().info.armyNames.get(nameI);
	}

	@Override
	public double equip(EquipBattle e) {
		return spec.equip[e.indexMilitary()];
	}

	@Override
	public boolean needSupplies() {
		return false;
	}


	@Override
	public DivGeneration generate() {
		return new DivGeneration(this, target);
	}

	@Override
	public int bannerI() {
		return bannerI;
	}

	@Override
	public void bannerSet(int bi) {
		this.bannerI = (short) bi;
	}

	@Override
	public COLOR color() {
		return col;
	}

	private final DIV_SETTING target = new DIV_SETTING() {
		
		@Override
		public double training(StatTraining tr) {
			return WDivMercenary.this.training(tr);
		}
		
		@Override
		public int men() {
			return menTarget;
		}
		
		@Override
		public double equip(EquipBattle e) {
			return WDivMercenary.this.equip(e);
		}
	};


	@Override
	public DIV_SETTING target() {
		return target;
	}
	



	
}
