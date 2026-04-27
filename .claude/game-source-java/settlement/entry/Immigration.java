package settlement.entry;

import java.io.IOException;

import game.GAME;
import game.boosting.BOOSTABLES;
import game.faction.FACTIONS;
import game.time.TIME;
import init.race.RACES;
import init.race.Race;
import init.type.HCLASS;
import init.type.HCLASSES;
import init.type.HTYPES;
import init.type.POP_CL;
import settlement.entity.ENTETIES;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.stats.STATS;
import settlement.stats.standing.STANDINGS;
import settlement.stats.standing.StandingCitizen;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.misc.CLAMP;
import util.data.INT.INTE;
import util.gui.misc.GBox;
import util.info.GFORMAT;
import util.text.D;
import util.text.Dic;
import util.updating.IUpdater;
import world.WORLD;
import world.army.AD;

public class Immigration {

	public static final int MAX_POPULATION = ENTETIES.MAX - 5000;
	
	private final Immigrator[] imms = new Immigrator[RACES.all().size()]; 
	
	boolean killall = false;
	private int killAllI = 0;
	
	Immigration() {
		
		for (Race r : RACES.all())
			imms[r.index] = new Immigrator(r);
		
	}
	
	void update(double ds) {
		
		
		
		if (killall) {
			
			for (int k = 0; k < 10; k++) {
				int i = killAllI;
				ENTITY e = SETT.ENTITIES().getAllEnts()[i];
				if (e != null && e instanceof Humanoid) {
					Humanoid a = (Humanoid) e;
					SETT.THINGS().gore.cloud(SETT.ENTITIES().getAllEnts()[i], a.race().appearance().colors.blood);
					SETT.THINGS().gore.explode(SETT.ENTITIES().getAllEnts()[i], a.race().appearance().colors.blood);
					SETT.ENTITIES().getAllEnts()[i].helloMyNameIsInigoMontoyaYouKilledMyFatherPrepareToDie();
				}
				killAllI++;
				if (killAllI >= SETT.ENTITIES().getAllEnts().length)
					killAllI = 0;
			}
			return;
		}
	
		updater.update(ds);
		
	}
	
	private final IUpdater updater = new IUpdater(imms.length, 30) {
		
		@Override
		protected void update(int i, double timeSinceLast) {
			imms[i].update(timeSinceLast);
		}
	};
	
	public int admitted(Race race) {
		return SETT.ENTRY().onTheirWay(race, HTYPES.SUBJECT());
	}
	
	public int wanted(Race race) {
		if (race == null) {
			int im = 0;
			for (int ri = 0; ri < RACES.all().size(); ri++) {
				Race r = RACES.all().get(ri);
				im+= wanted(r);
			}
			return CLAMP.i(im, 0, im);
		}
		if (GAME.events().riot.shouldEmigrate(race))
			return 0;
		
		return CLAMP.i(imms[race.index].wanted()-SETT.ENTRY().onTheirWay(race, HTYPES.SUBJECT()), 0, 1000);
	}
	
	public final INTE auto(Race race) {
		return imms[race.index].auto;
	}
	
	public void admit(Race race, int amount) {
		SETT.ENTRY().add(race, HTYPES.SUBJECT(), amount);
		imms[race.index].timer = CLAMP.d(imms[race.index].timer-amount, 0, imms[race.index].timer);
	}
	
	public int maxPop(Race race) {
		return (int) (MAX_POPULATION*race.population().max);
	}
	
	public double secondsTillNext(Race race) {
		return imms[race.index].secondsTillNext();
	}
	
	public void setHigher(Race race, int am) {
		imms[race.index].timer = Math.max(imms[race.index].timer, am);
	}
	
	final SAVABLE saver = new SAVABLE() {
		
		@Override
		public void save(FilePutter file) {
			updater.save(file);
			RACES.map().saver().save(imms, file);
		}
		
		@Override
		public void load(FileGetter file) throws IOException {
			updater.load(file);
			RACES.map().loader().load(imms, file);
		}
		
		@Override
		public void clear() {
			for (Immigrator i : imms)
				i.clear();
			killall = false;
			killAllI = 0;
		}
	};
	
	public boolean shouldEmmigrate(Race race) {
		return imms[race.index].shouldEmmigrate();
	}

	public void clear() {
		for (Immigrator i : imms)
			i.clear();
	}
	
	public void clear(Race race) {
		imms[race.index()].clear();
	}
	

	private static final class Immigrator implements SAVABLE{

		private static double rate = 1.0/(TIME.secondsPerDay()*2);
		
		private final Race race;
		private int autoAdmit = 0;
		private double timer = 0;
		private double emmigrants = 0;
		Immigrator(Race race) {
			this.race = race;
		}
		
		private int wantedUltimately() {

			return getImmigrants(race)-SETT.ENTRY().onTheirWay(race, HTYPES.SUBJECT());
		}
		
		private double speed(int wanted) {
			if (wanted <= 0)
				return 0;
		
			double d = 1 + STATS.POP().POP.data(HCLASSES.CITIZEN()).get(race)/500.0;
			
			double speed = rate*d;
			if (WORLD.camps().available(race)) {
				speed *= WORLD.camps().replenishPerDay(FACTIONS.player(), race);
			}else {
				speed *= BOOSTABLES.CIVICS().IMMIGRATION.get(POP_CL.clP());
				double boost = 8.0;
				speed *= (1.0 + boost)*race.population().immigrantsPerDay*race.population().climate(SETT.ENV().climate());
			}
			
			return speed;
		}
		
		public double secondsTillNext() {
			double rem = 1.0 - (timer-(int)timer);
			double speed = speed(wantedUltimately());
			if (speed == 0)
				return Double.NaN;
			return rem/speed;
		}

		
		void update(double ds) {
		
			int wanted = wantedUltimately();
			if (wanted < 0) {
				emmigrants += -ds*wanted/(2.0*TIME.secondsPerDay());
				timer = 0;
				return;
			}
			emmigrants = 0;
			
			timer += speed(wanted)*ds;
			
			timer = CLAMP.d(timer, 0, wanted);
			
			int a = (int) (auto.get());
			a -= STATS.POP().POP.data(HCLASSES.CITIZEN()).get(race);
			a -= SETT.ENTRY().onTheirWay(race, HTYPES.SUBJECT());
			a -= STATS.POP().pop(HTYPES.RIOTER());
			int w =  wanted();
			
			
			if (a > 0 && w > 0) {
				int am = CLAMP.i(w, 0, a);
				SETT.ENTRY().add(race, HTYPES.SUBJECT(), am);
				timer -= am;
			}

		}
		
		public boolean shouldEmmigrate() {
			if (emmigrants > 1) {
				emmigrants --;
				return true;
			}
			return false;
		}


		@Override
		public void save(FilePutter file) {
			file.d(timer);
			file.i(autoAdmit);
			file.d(emmigrants);
		}

		@Override
		public void load(FileGetter file) throws IOException {
			timer = file.d();
			autoAdmit = file.i();
			emmigrants = file.d();
		}
		
		@Override
		public void clear() {
			timer = 0;
			autoAdmit = 0;
		}
		
		public int wanted() {
			return CLAMP.i((int)timer, 0, wantedUltimately());
		}
		
		public final INTE auto = new INTE() {

			@Override
			public int get() {
				return race.population().max == 0 ? ENTETIES.MAX : autoAdmit;
			}

			@Override
			public int min() {
				return 0;
			}

			@Override
			public int max() {
				return ENTETIES.MAX;
			}

			@Override
			public void set(int t) {
				autoAdmit = t;
			}
		};

		
	}
	
	private static int getImmigrants(final Race r) {
		
		
		HCLASS cl = HCLASSES.CITIZEN();
		double pop = STATS.POP().POP.data(cl).get(r, 0)+AD.cityDivs().total(r);
		
		if (WORLD.camps().available(r)) {
			return Math.max(WORLD.camps().current(FACTIONS.player(), r)-AD.cityDivs().total(r), 0)-STATS.POP().POP.data().get(r);
		}
		
		if (pop == 0) {
			double hap = BOOSTABLES.BEHAVIOUR().HAPPI.get(POP_CL.clP(r, HCLASSES.CITIZEN()));
			return (int) Math.ceil(hap-0.1);
		}
		
		{
			double ful = STANDINGS.CITIZEN().fullfillment.getD(r);
			double exp = STANDINGS.CITIZEN().expectation(r, pop, STATS.POP().POP.data(null).get(null, 0)-pop);
			double hap = ful/exp;
			hap*= BOOSTABLES.BEHAVIOUR().HAPPI.get(POP_CL.clP(r, HCLASSES.CITIZEN()));
			hap = CLAMP.d(hap, 0, 2);

			hap -= threshold; 
			if (hap <= 0)
				return (int) (pop*hap/threshold);
			hap*= 0.5;
			double am = hap*r.population().max*pop;
			if (am > 1) {
				double d = am / (am+pop);
				am = am*(1-d) + d*Math.pow(am, 1/STANDINGS.CITIZEN().fullPow(r));
			}
			
			int res = (int) Math.ceil(am);
			
			return (int) res;
		}

	}
	
	private static double threshold = 0.9;
	private static CharSequence ¤¤immigrants = "Immigrants";
	private static CharSequence ¤¤camp = "Havens Max";
	private static CharSequence ¤¤immigrantD = "Immigrants are subjects from either your regional population, or camps that have joined your cause. These subjects will be attracted by your current happiness.";
	private static CharSequence ¤¤admitted = "Admitted";
	private static CharSequence ¤¤speed = "Attracted/day";

	static {
		D.ts(Immigration.class);
	}
	
	public void hoverImmigrants(GUI_BOX box, Race r) {
		if (r == null)
			return;
		GBox b = (GBox) box;
		StandingCitizen st = STANDINGS.CITIZEN();
		
		b.textLL(¤¤immigrants);
		b.tab(7);
		b.add(GFORMAT.iBig(b.text(), wanted(r)));
		b.NL();
		
		b.text(¤¤immigrantD);
		b.NL(4);
		
		if (WORLD.camps().available(r)) {
			b.textL(¤¤camp);
			b.tab(7);
			b.add(GFORMAT.iBig(b.text(), WORLD.camps().current(FACTIONS.player(), r)));
			b.NL();
		}else {
			b.textL(¤¤speed);
			b.tab(7);
			b.add(GFORMAT.f0(b.text(), TIME.secondsPerDay()*imms[r.index].speed(1)));
			b.NL();
			b.textL(Dic.¤¤Available);
			b.tab(7);
			b.add(GFORMAT.f0(b.text(), imms[r.index].wantedUltimately()));
			b.NL();
			
		}
		
		
		b.textL(¤¤admitted);
		b.tab(7);
		b.add(GFORMAT.iBig(b.text(), admitted(r)));
		b.NL();
		
		b.textL(st.happiness.info().name);
		b.tab(7);
		b.add(GFORMAT.perc(b.text(), st.happiness.getD(r)));
		
		b.NL(8);
		
		
	}

	public void setWanted(Race race, int am) {
		imms[race.index()].timer = am;
	}

	
}
