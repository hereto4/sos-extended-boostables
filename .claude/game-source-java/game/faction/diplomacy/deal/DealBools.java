package game.faction.diplomacy.deal;

import game.GAME;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.diplomacy.DIP;
import game.faction.diplomacy.DipStance;
import game.faction.royalty.opinion.ROPINIONS;
import init.settings.S;
import init.sprite.UI.UI;
import settlement.stats.STATS;
import snake2d.LOG;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.DIR;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.text.Str;
import util.gui.misc.GBox;
import util.info.GFORMAT;
import util.text.D;
import util.text.Dic;
import world.WORLD;
import world.region.RD;

public final class DealBools {

	private static CharSequence ¤¤ally = "Alliance";
	private static CharSequence ¤¤allyD = "As allies, you will trade at an increased discount, respect each other's sovereignty, and also share your enemies. Armies can move freely in allied territory.";
	
	private static CharSequence ¤¤pact = "Become Colleagues";
	private static CharSequence ¤¤pactD = "As colleagues, you will trade at an increased discount, and also pledge not attack each other and respect each others sovereignty.";
	
	private static CharSequence ¤¤peace = "Peace";
	private static CharSequence ¤¤peaceD = "Peace Agreement. Will affect all allies and common enemies.";
	
	private static CharSequence ¤¤absorb = "Unite";
	private static CharSequence ¤¤absorbD = "The faction, and all of its territory joins you as your own territory.";
	
	private static CharSequence ¤¤vassal = "Become Vassal";
	private static CharSequence ¤¤vassalD = "You become a vassal to this faction. You will have no diplomatic relations with other factions. You will also pay a 10% tax at the end of each year, and in return you will be protected against attackers.";
	
	private static CharSequence ¤¤protector = "Become Protector";
	private static CharSequence ¤¤protectorD = "The faction becomes your vassal, and you its protector. At the end of each year you will receive a tribute of your choosing.";
	

	private static CharSequence ¤¤tradeCancel = "Cancel All Agreements";
	private static CharSequence ¤¤tradeCancelD = "Will cancel all agreements, including trade and make you neutral to each other";
	
	private static CharSequence ¤¤tradeD = "You and the faction become trade partners, which will allow you to automatically trade at better prices.";
	
	private static CharSequence ¤¤pWarAlready = "You are already at war.";
	private static CharSequence ¤¤pWarNot = "You are not at war.";
	private static CharSequence ¤¤pOther = "Can not be combined with other proposals.";
	private static CharSequence ¤¤pNoAgree = "You have no agreements to break.";
	private static CharSequence ¤¤pDistance = "This option requires the faction to be your neighbour.";
	private static CharSequence ¤¤pAlready = "You are already at this stance with each other.";
	private static CharSequence ¤¤pOpinion = "You are not liked enough by the faction for this stance to be considered. A minimum opinion of {0} is required.";
	private static CharSequence ¤¤pVassal = "As a vassal, you can not propose this agreement to this faction.";
	private static CharSequence ¤¤pVassalOther = "This faction is a puppet state of another faction, and is not allowed to make this agreement.";
	
	private static CharSequence ¤¤opReq = "Opinion Required";
	private static CharSequence ¤¤opTarif = "Tariff";
	
	static {
		D.ts(DealBools.class);
	}
	
	private final ArrayListGrower<DealBool> all = new ArrayListGrower<>();
	
	private final DealParty a;
	private final DealParty b;
	
	
	private boolean debug = false;
	
	DealBools(DealParty a, DealParty b){
		this.a = a;
		this.b = b;
	}
	

	
	public final DealBool WAR = new DealBool(all, Dic.¤¤DeclareWar, Dic.¤¤WarD, DIP.WAR().icon) {
		

		@Override
		public void execute() {
			ROPINIONS.STANCE().setNewStance(b.npc(), DIP.WAR(), player);
		}

		@Override
		public double value() {
			return 0;
		}

		@Override
		protected void pInit(DealParty a, DealParty b) {
			
		}

		@Override
		protected DipStance stance() {
			return DIP.WAR();
		}

		@Override
		public CharSequence problem() {
			if (DIP.WAR().is(a.f(), b.f()))
				return ¤¤pWarAlready;
			for (DealBool b : all()) {
				if (b != this && b.is())
					return ¤¤pOther;
			}
			return null;
		}
	};
	
	public final DealBool PEACE = new DealBool(all, ¤¤peace, ¤¤peaceD, UI.icons().s.sprout.createColored(new ColorImp(0, 128, 128))) {
		
		private double value;

		@Override
		public void execute() {
			ROPINIONS.STANCE().setNewStance(b.npc(), DIP.NEUTRAL(), player);
		}

		@Override
		public double value() {
			return value;
		}

		@Override
		protected void pInit(DealParty a, DealParty b) {
			value = pactChange(DIP.NEUTRAL());
		}

		@Override
		protected DipStance stance() {
			return DIP.NEUTRAL();
		}
		
		@Override
		public CharSequence problem() {
			if (!DIP.WAR().is(a.f(), b.f()))
				return ¤¤pWarNot;
			for (DealBool b : all()) {
				if (b != this && b.is())
					return ¤¤pOther;
			}
			return null;
		}
	};
	
	public final DealBool CANCEL_ALL = new DealBool(all, ¤¤tradeCancel, ¤¤tradeCancelD, DIP.TRADE().icon.twin(UI.icons().s.cancel.createColored(COLOR.REDISH), DIR.C, 1)) {
		
		private double value;
		
		@Override
		public double value() {
			return value;
		}
		
		@Override
		public void execute() {
			ROPINIONS.STANCE().setNewStance(b.npc(), DIP.NEUTRAL(), player);
		}

		@Override
		protected void pInit(DealParty a, DealParty b) {
			value = pactChange(DIP.NEUTRAL());
			
		}
		
		@Override
		protected DipStance stance() {
			return DIP.NEUTRAL();
		}

		@Override
		public CharSequence problem() {
			if (!DIP.get(a.f(), b.f()).trades)
				return ¤¤pNoAgree;
			for (DealBool b : all()) {
				if (b != this && b.is())
					return ¤¤pOther;
			}
			return null;
		}
	};
	
	public final DealBool TRADE = new DealStance(Dic.¤¤Trade, ¤¤tradeD, DIP.TRADE());
	
	public final DealBool PACT = new DealStance(¤¤pact, ¤¤pactD, DIP.PACT());
	
	public final DealBool ALLY = new DealStance(¤¤ally, ¤¤allyD, DIP.ALLY());
		
	public final DealBool VASSAL = new DealStance(¤¤protector, ¤¤protectorD, DIP.OVERLORD());

	public final DealBool OVERLORD = new DealStance(¤¤vassal, ¤¤vassalD, DIP.VASSAL());
	
	
	public final DealBool ABSORB = new DealBool(all, ¤¤absorb, ¤¤absorbD, UI.icons().s.flags.createColored(new ColorImp(128, 128, 0))) {
		
		private double value;

		@Override
		public void execute() {
			while(b.npc().realm().regions() > 1) {
				b.npc().realm().region(1).fationSet(a.f(), true);
			}
			if (b.f().realm().regions() > 0)
				b.f().realm().region(0).fationSet(a.f(), true);
			FACTIONS.remove(b.npc(), true);
			GAME.count().UNITES.inc(1);
		}

		@Override
		public double value() {
			return value;
		}

		@Override
		protected void pInit(DealParty a, DealParty b) {

			value = pactChange(DIP.VASSAL());
			value -= b.selfWorth();;
			
			if (DIP.WAR().is(a.f(), b.f()))
				value -= b.selfWorth();
			
		}
		
		@Override
		protected DipStance stance() {
			return DIP.get(a.f(), b.f());
		}

		@Override
		public CharSequence problem() {
			if (a.f() == FACTIONS.player() && !RD.DIST().reachable(b.f()))
				return ¤¤pDistance;
			for (DealBool b : all()) {
				if (b != this && b.is())
					return ¤¤pOther;
			}
			
			
			return null;
		}
	};
	
	private class DealStance extends DealBool {

		private double vv;
		private final DipStance ss;
		
		DealStance(CharSequence name, CharSequence desc, DipStance stance) {
			super(all, name, desc, stance.icon);
			ss = stance;
		}

		@Override
		public double value() {
			return vv;
		}

		@Override
		public void execute() {
			ROPINIONS.STANCE().setNewStance(b.npc(), ss, player);
		}

		@Override
		protected void pInit(DealParty a, DealParty b) {
			vv = pactChange(ss);
		}

		@Override
		protected DipStance stance() {
			return ss;
		}

		@Override
		public CharSequence problem() {
			if (ss.is(a.f(), b.f()) || ss.is(b.f(), a.f()))
				return ¤¤pAlready;
			
//			if (DIP.get(a.f(), b.f()).minLoyalty > 0 && DIP.get(a.f(), b.f()).minLoyalty > ss.minLoyalty)
//				return ¤¤pDowngrade;
//			
			if (ROPINIONS.current(b.npc()) < ss.minLoyalty && !S.get().developer) {
				Str.TMP.clear().add(¤¤pOpinion).insert(0, ss.minLoyalty, 1);
				return Str.TMP;
			}
			
			if (this != TRADE) {
				if (DIP.overlord(a.f()) != null)
					return ¤¤pVassal;
				
				if (DIP.overlord(b.f()) != null)
					return ¤¤pVassalOther;
			}
			

			
			if (a.f() == FACTIONS.player() && !RD.DIST().reachable(b.f()))
				return ¤¤pDistance;
			
			for (DealBool b : all()) {
				if (b != this && b.is())
					return ¤¤pOther;
			}
			return null;
		}
		
		@Override
		public void hover(GBox b) {
			super.hover(b);
			b.textL(¤¤opReq);
			b.tab(7);
			b.add(GFORMAT.f0(b.text(), ss.minLoyalty));
			b.NL();
			
			b.textL(¤¤opTarif);
			b.tab(7);
			b.add(GFORMAT.f0(b.text(), ss.tarif));
			b.NL();
		}
		
	}

	
	private double pactChange(DipStance newStance) {
		DipStance old = DIP.get(a.f(), b.f());
		if (old == newStance)
			return 0;
		
		double value = 0;
		
		
		
		if (old == DIP.WAR()) {
			value += peaceValue;
		}
		
		if (newStance.tarif < old.tarif) {
			double d = DIP.TRADE().tarif-newStance.tarif;
			value += tradeValue + 20.0*tradeValue*d; 
		}
		
		if (!old.ally && newStance.ally) {
			value += allyValue;
		}
		
		if (debug){
			LOG.ln(old.name + "->" + newStance.name);
			LOG.ln("1 " + value);
		}		
		
		if (old == DIP.VASSAL()) {
			value -= 0.5*a.offerableWorth()*opinionMul;
		}else if (newStance == DIP.VASSAL()) {
			value = tradeValue;
			value += CLAMP.d(0.25*a.selfWorth()*(1-opinionMul), 0, b.offerableWorth());
		}else {
			double be = betrayal(newStance);
			
//			if (be > 0)
//				//value -= be*0.05*opinionMul*b.selfWorth();
//			else
//			if (be < 0)
//				value += be*0.0025*b.selfWorth();
			
			if (debug) {
				LOG.ln("2 " + be + " " + value);
			}
		}
		
		if (debug) {
			LOG.ln("3 " + value);
		}
		
		
		if (old == DIP.OVERLORD()) {
			value += b.offerableWorth()*0.1*opinionMul;
		}else if (newStance == DIP.OVERLORD()) {
			value -= b.selfWorth()*0.5*(1-opinionMul);
		}
		
		if (debug) {
			LOG.ln("4 " + value);
		}
		
		return value;

	}
	
	private double betrayal(DipStance newStance) {
		if (a.f() != FACTIONS.player())
			return 0;
		
		return ROPINIONS.STANCE().betrayal(b.npc(), newStance);
		
	}
	
	private double tradeValue() {
		double d = WORLD.PATH().distance(a.f().capitolRegion(), b.f().capitolRegion());
		if (d == 0)
			return 0;
		else {
			double dist = 1 + d/512.0;
			double cs = 0.0005*FACTIONS.WORTH().faction(b.npc());
			cs *= (1 + CLAMP.d(STATS.POP().POP.data().get(null)/2500.0, 0, 1));
			if (debug) {
				LOG.ln("  trade: " + dist + " " + cs + " " + cs*dist);
			}
			return -dist * cs;
			
			
		}
			
	}
	
	private double peaceValue() {
		
		double pv = 0.2 - DIP.WAR().peaceValue(a.f());
		if (pv < 0) {
			return 0;
		}else if (pv > 0) {
			return -pv*0.75*a.offerableWorth();
		}
		return 0;
		
	}
	
	private double allyValue() {
		
		double powA = 0;
		double powB = 0;
		double enemiesA = 0;
		double enemiesB = 0;

		for (Faction f : FACTIONS.all()) {
			if (f == a.f() || DIP.get(a.f(), f).ally) {
				powA += f.offensivePower();
			}else if (f != b.f() && DIP.WAR().is(a.f(), f)) {
				if (!DIP.WAR().is(b.f(), f))
					enemiesA += f.offensivePower();
			}else if(f == b.f() || DIP.get(b.f(), f).ally) {
				powB += f.offensivePower();
			}else if (f != a.f() && DIP.WAR().is(b.f(), f)) {
				enemiesB -= f.offensivePower();
			}
		}
		
		double before = (powB+100) / (enemiesB+100);
		double after = (powA + powB + 100) / (enemiesA + enemiesB+100);
		
		//faction has problems
		if (before < 1) {
			if (after > before) {
				//you save them
				return b.selfWorth()*CLAMP.d((after-before)*0.25, 0, 0.25);
			}else {
				//You make things worse
				return -b.selfWorth();
			}
		}
		
		double d = 0.1*before/after;
		return -b.selfWorth()*d;
	
	}
	
	private double allyValue;
	private double peaceValue;
	private double tradeValue;
	private double opinionMul;
	private boolean player;
	
	void init(boolean player, boolean clear) {
		this.player = player;
		opinionMul = 0.5;
		if (a.f() == FACTIONS.player()) {
			double op = ROPINIONS.peaceValue(b.npc());
			opinionMul = 0.5 + -op/10.0;
			opinionMul = CLAMP.d(opinionMul, 0.05, 1.0);
			
		}
		
		allyValue = allyValue();
		peaceValue = peaceValue();
		tradeValue = tradeValue();
		if (debug){
			LOG.ln(b.f().name);
			LOG.ln("worth " + b.selfWorth());
			LOG.ln("player " +  a.offerableWorth());
			LOG.ln("opinion " + opinionMul);
			LOG.ln("ally " + allyValue);
			LOG.ln("peace " + peaceValue);
			LOG.ln("trade " + tradeValue);
		}


		for (DealBool bool : all) {
			bool.pInit(a, b);
			if (clear)
				bool.set(false);
		}

	}
	
	public double value() {
		double v = 0;
		for (DealBool bool : all)
			if (bool.is())
				v += bool.value();
		
		if (ABSORB.is()) {
			v -= b.selfWorth()*0.5*(1-opinionMul);
			v -= b.regs.worth();
		}
		
		return v;
	}
	
	public double betrayal() {
		if (a.f() != FACTIONS.player())
			return 0;
		
		for (DealBool bool : all)
			if (bool.is()) {
				return Math.max(0, ROPINIONS.STANCE().betrayal(b.npc(), bool.stance()));
				
			}
		return 0;
		
	}
	
	void execute() {
		double be = betrayal();
		for (DealBool bo : all) {
			if (bo.is()) {
				if (a.f() == FACTIONS.player() && be > 0)
					ROPINIONS.STANCE().betrayal(b.npc(), DIP.get(b.npc()), bo.stance());
				bo.execute();
			}
		}
	}
	
	public LIST<DealBool> all(){
		return all;
	}

	public void betrayalHover(GBox box) {
		if (a.f() != FACTIONS.player())
			return;
		
		for (DealBool bool : all)
			if (bool.is()) {
				ROPINIONS.STANCE().betrayalHover(box, b.npc(), bool.stance());
				return;
			}
		
	}

}
