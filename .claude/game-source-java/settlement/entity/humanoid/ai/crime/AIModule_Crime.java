package settlement.entity.humanoid.ai.crime;

import game.GAME;
import game.boosting.BOOSTABLES;
import game.time.TIME;
import init.sprite.UI.UI;
import init.type.HTYPE;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.HEvent;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AIModule;
import settlement.entity.humanoid.ai.main.AIModules;
import settlement.entity.humanoid.ai.main.AIPLAN;
import settlement.entity.humanoid.ai.main.AIPLAN.AiPlanActivation;
import settlement.main.SETT;
import settlement.stats.STATS;
import settlement.stats.law.LAW;
import settlement.stats.law.PRISONER_TYPE;
import settlement.stats.law.PRISONER_TYPE.CRIME;
import snake2d.util.bit.Bits;
import snake2d.util.rnd.RND;
import util.data.INT_O.INT_OE;
import util.text.D;

public final class AIModule_Crime extends AIModule{

	public final AIPLAN theft = new Theft("CrimeTheft", this);
	public final AIPLAN murder = new Murder("CrimeMurder", this);
	public final AIPLAN vandal = new Vandalism("CrimeVandal", this);
	public final AIPLAN flash = new Flasher("crimeFlash", this);
	public final AIPLAN disrespect = new Disrespect("crimeDisres", this);
	
	public final AIPLAN serial = new SerialKiller("crimeSerial");
	
	
	private final INT_OE<AIManager> ctimer = new wrap(new Bits(0b0000_0011));
	private final INT_OE<AIManager> ccrimes = new wrap(new Bits(0b0011_1100));
	private final INT_OE<AIManager> crimesToCommit = new wrap(new Bits(0b1100_0000));
	
	private static CharSequence ¤¤name = "Mischief";
	private static CharSequence ¤¤desc = "Commit crimes";
	static {
		D.ts(AIModule_Crime.class);
	}
	
	public AIModule_Crime() {
		super(UI.icons().s.law, ¤¤name, ¤¤desc);
		for (CRIME c : PRISONER_TYPE.CRIMES)
			getPlan(c);
		
	}
	
	@Override
	public AiPlanActivation getPlan(Humanoid a, AIManager d) {
		
		if (GAME.events().killer.theKiller() == a && GAME.events().killer.theKillerShouldKill())
			return serial.activate(a, d);
		
		CRIME crime = PRISONER_TYPE.RND(a.race());
		
		return getPlan(crime).activate(a, d);
	}
	
	private AIPLAN getPlan(CRIME crime) {
		
		if (crime == CRIME.THEFT)
			return theft;
		if (crime == CRIME.DISRESPECT)
			return disrespect;
		if (crime == CRIME.FLASHING)
			return flash;
		if (crime == CRIME.MURDER)
			return murder;
		if (crime == CRIME.VANDALISM)
			return vandal;
		throw new RuntimeException(crime.name+"");
	}

//	private static final double di = 1.0/16;
	
//	private int day = -1;
//	private double acc = 0;
//	private int ii = 0;
//	private int kk = 0;
//	private int max = 0;
	
	
	@Override
	protected void update(Humanoid a, AIManager d, boolean newDay, int byteDelta, int updateOfDay) {
		
		if (LAW.curfew().isSetForADay()) {
			crimesToCommit.set(d, 0);
		}else {
			double r = BOOSTABLES.BEHAVIOUR().LAWFULNESS.get(a.indu());
			if (r < 0) {
				r = 0;
			}
			r *= 32*10;
			r += 16;
			if (STATS.POP().POP.data(null).get(null) < 250)
				r *= 100;
			
			if (RND.oneIn(r)) {
				crimesToCommit.set(d, crimesToCommit.max(d));
//				kk++;
//				max = Math.max(crimesToCommit.get(d), max);
			}
		}


		
		if (newDay) {

			int c = ccrimes.get(d);
			if (c > 0) {
				
				int t = ctimer.get(d);
				if (t == 0) {
					for (int i = 0; i < ccrimes.get(d); i++) {
						LAW.process().arrests.inc(a.race(), false);
						ccrimes.set(d, 0);
					}
				}else {
					ctimer.inc(d, -1);
				}
				
			}
		}
		if ((updateOfDay & 0b011) == 0 && LAW.process().prosecute.allowed.is(a.race()) || STATS.MULTIPLIERS().PROSECUTION.markIs(a)) {
			SETT.ROOMS().GUARD.reportCriminal(a, true);
		}
	}
	
	void commitCrime(Humanoid a, AIManager d, boolean notify, CRIME crime) {
		LAW.crimes().register(a.race(), crime);
		PRISONER_TYPE old = STATS.LAW().prisonerType.get(a.indu());
		if (old instanceof CRIME) {
			if (crime.rarty() < ((CRIME)old).rarty())
				STATS.LAW().prisonerType.set(a.indu(), crime);
		}else {
			STATS.LAW().prisonerType.set(a.indu(), crime);
		}
		
		if (notify)
			AIModule_Crime.notify(a);
		SETT.ROOMS().GUARD.reportCriminal(a, false);
		if (ccrimes.isMax(d)) {
			LAW.process().arrests.inc(a.race(), false);
		}else
			ccrimes.inc(d, 1);
		ctimer.set(d, 2);
		crimesToCommit.inc(d, -16);
	}
	

	public boolean catchPrisoner(Humanoid a) {
		AIManager d = (AIManager) a.ai();
		if (a.indu().hType().player) {
			if (ccrimes.get(d) > 0) {
				LAW.process().arrests.inc(a.race(), true);
				for (int i = 0; i < ccrimes.get(d)-1; i++) {
					LAW.process().arrests.inc(a.race(), false);
				}
				ccrimes.set(d, 0);
				return true;
			}
			if (LAW.process().prosecute.allowed.is(a.race()) || STATS.MULTIPLIERS().PROSECUTION.markIs(a)) {
				STATS.LAW().prisonerType.set(a.indu(), PRISONER_TYPE.PLEASURE);
				LAW.process().prosecute.inc(a.race(), true);
				ccrimes.set(d, 0);
				return true;
			}
			return false;
		}else {
			return true;
		}
		
		
		
		
	}

	@Override
	public int getPriority(Humanoid a, AIManager d) {
		if (GAME.events().killer.theKiller() == a) {
			if (GAME.events().killer.theKillerShouldKill()) {
				return TIME.light().nightIs()  ? 4 : 0;
			}
			return 0;
		}
		if (crimesToCommit.isMax(d)) {
			return 6;
		}
		return 0;
	}
	
	public static void notify(Humanoid criminal) {
		for (ENTITY e : SETT.ENTITIES().getInProximity(criminal, 5)) {
			if (e instanceof Humanoid) {
				HEvent.Handler.notifyCrime((Humanoid) e, criminal);
			}
		}
	}

	public boolean isCriminal(Humanoid a) {
		AIManager d = (AIManager) a.ai();
		return ccrimes.get(d) > 0 || a.indu().hostile() || LAW.process().prosecute.allowed.is(a.race()) || STATS.MULTIPLIERS().PROSECUTION.markIs(a);
	}
	
	@Override
	protected void init(Humanoid a, AIManager d, HTYPE prev, HTYPE current) {
		crimesToCommit.set(d, RND.rInt(crimesToCommit.max(d)));
		ccrimes.set(d, 0);
	}
	
	@Override
	protected void cancel(Humanoid a, AIManager d) {
		for (int i = 0; i < ccrimes.get(d); i++) {
			LAW.process().arrests.inc(a.race(), false);
			ccrimes.set(d, 0);
		}
			
		super.cancel(a, d);
	}
	
	private static class wrap implements INT_OE<AIManager> {

		private final Bits bits;
		
		wrap(Bits bits){
			this.bits = bits;
		}
		
		@Override
		public int get(AIManager t) {
			return bits.get(AIModules.data().byte1.get(t));
		}

		@Override
		public int min(AIManager t) {
			return 0;
		}

		@Override
		public int max(AIManager t) {
			return bits.mask;
		}

		@Override
		public void set(AIManager t, int i) {
			int d = bits.set(AIModules.data().byte1.get(t), i);
			AIModules.data().byte1.set(t, d);
		}
		
		
	}

}
