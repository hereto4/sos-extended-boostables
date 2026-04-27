package game.events.citizen;

import java.io.IOException;

import game.events.EVENTS.EventResource;
import game.time.TIME;
import init.race.RACES;
import init.race.Race;
import init.type.HCLASSES;
import settlement.entity.humanoid.Humanoid;
import settlement.stats.STATS;
import settlement.stats.standing.STANDINGS;
import snake2d.LOG;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import util.text.D;
import view.sett.IDebugPanelSett;
import view.ui.message.MessageText;

public final class EventCitizen extends EventResource{

	public static final double breakPoint = 0.75;
	private static CharSequence ¤¤riotWarning = "¤Ungrateful plebs!";
	private static CharSequence ¤¤riotWarningD = "¤Rumour has it that your citizens are grinding their teeth in frustration over what they claim is your incompetent rule. If nothing is done in time, a riot might follow! Try to increase their loyalty immediately.";

	static {
		D.ts(EventCitizen.class);
	}
	
	private boolean hasSentWarning = false;
	private final double timerD = 15;
	private double timer = timerD;
	private double count = 5.0;
	private int warmup = 3;
	private final double countD = timerD/TIME.secondsPerDay();
	private boolean emigrate;
	private final EventCitizenEmmigrate emmi = new EventCitizenEmmigrate();
	private final EventCitizenStrike strike = new EventCitizenStrike();
	private final EventCitizenRiot riot = new EventCitizenRiot();
	private final EventCitizenRace brawl = new EventCitizenRace();
	private final EventCitizenRel rel = new EventCitizenRel();
	
	private final SMALL_EVENT[] all = new SMALL_EVENT[] {
		emmi,strike,brawl,rel
	};
	
	private final SMALL_EVENT[] tmp = new SMALL_EVENT[all.length];
	
	private final int[] amounts = new int[RACES.all().size()];
	
	public EventCitizen(){
		super("CITIZEN");
		clear();
		
		IDebugPanelSett.add("Event: Emmigration2", new ACTION() {
			
			@Override
			public void exe() {
				double total = 0;
				for (Race r : RACES.all()) {
					int a = getAmount(r);
					total += a;
					amounts[r.index()] = a;
				}
				
				
				double c = total/STATS.POP().POP.data(HCLASSES.CITIZEN()).get(null);
				
				if (c == 0) {
					LOG.ln("nay!");
				}else {
					Race r = getRace(total);
					emmi.event(amounts[r.index], r);
				}
			}
		});
		
	}
	
	@Override
	protected void save(FilePutter file) {
		file.d(timer);
		file.d(count);
		file.bool(emigrate);
		file.bool(hasSentWarning);
		file.i(warmup);
		for (SMALL_EVENT e : all) {
			e.save(file);
		}
		riot.save(file);
	}
	
	@Override
	protected void load(FileGetter file) throws IOException {
		timer = file.d();
		count = file.d();
		emigrate = file.bool(); 
		hasSentWarning = file.bool();
		warmup = file.i();
		for (SMALL_EVENT e : all) {
			e.load(file);
		}
		riot.load(file);
	}
	
	@Override
	protected void clear() {
		emigrate = true;
		hasSentWarning = false;
		timer = timerD;
		count = 2.0;
		for (SMALL_EVENT e : all) {
			e.clear();
		}
		warmup = 3;
		riot.clear();
		shuffleSmall();
	}
	
	private void shuffleSmall() {
		for (int i = 0; i < all.length; i++)
			tmp[i] = all[i];
		
		for (int i = 0; i < tmp.length; i++) {
			int ri = RND.rInt(tmp.length);
			SMALL_EVENT o = tmp[i];
			tmp[i] = tmp[ri];
			tmp[ri] = o;
		}
	}
	
	public boolean shouldEmigrate(Humanoid h) {
		return shouldEmigrate(h.race());
	}
	
	public boolean shouldEmigrate(Race r) {
		return emmi.shouldEmigrate(r);
	}
	
	public void emigrate(Humanoid h) {
		emmi.emigrate(h);
	}
	
	public boolean onStrike(Humanoid h) {
		return strike.isStriking(h);
	}
	
	public boolean shouldBrawl(Humanoid a, Humanoid b) {
		if (a.indu().clas() == HCLASSES.CITIZEN() && b.indu().clas() == HCLASSES.CITIZEN())
			return brawl.isAtOdds(a, b) || rel.isAtOdds(a, b);
		return false;
	}
	
	@Override
	protected void update(double ds) {
		
		for (SMALL_EVENT e : all) {
			e.update(ds);
		}
		riot.update(ds);
		
		timer -= ds;
		if (timer > 0)
			return;
		
		timer += timerD;
		
		if (STATS.POP().POP.data().get(null) < 15)
			return;
		
		double total = 0;
		double biggest = 0;
		for (Race r : RACES.all()) {
			int a = getAmount(r);
			total += a;
			amounts[r.index()] = a;
			int pop = STATS.POP().POP.data(HCLASSES.CITIZEN()).get(r);
			if (pop > 0)
				biggest = Math.max(biggest, (double)a/pop);
		}
		
		
		
		
		if (total == 0 || biggest <= 0) {
			if (count < 1.0) {
				count += countD;
				count = CLAMP.d(count, 0, 1.0);
			}
		}else {
			
			double c = biggest;
			
			double old = count;
			biggest = Math.pow(c, 0.8);
			count -= c*countD;
			if (count >= 0.25)
				return;
			
			if (emigrate) {
				if (old > 0.25) {
					emigrate = !RND.oneIn(3);
					Race r = getRace(total);
					shuffleSmall();
					for (SMALL_EVENT e : tmp) {
						if (e.event(amounts[r.index], r)) {
							addCount(0.2 + 0.5*total/(1+STATS.POP().POP.data(HCLASSES.CITIZEN()).get(null)));
							STANDINGS.CITIZEN().buff.execute(HCLASSES.CITIZEN(), 2*TIME.secondsPerDay());
							return;
						}
					}
					emigrate = false;
					new MessageText(¤¤riotWarning, ¤¤riotWarningD).send();
				}
				
			}else if (count <= 0) {
				emigrate = !RND.oneIn(3);
				riot.riot(amounts);
				addCount(1.0 + total/(1+STATS.POP().POP.data(HCLASSES.CITIZEN()).get(null)));
				
				return;
			}
		}
		
		
		
	}
	
	private void addCount(double am){
		warmup --;
		if (warmup < 1)
			warmup = 1;
		count += warmup*am;
		
	}
	
	private Race getRace(double total) {
		
		total = total*RND.rFloat();
		for (int i = 0; i < RACES.all().size(); i++) {
			total -= amounts[i];
			if (total <= 0 && amounts[i] > 0)
				return RACES.all().get(i);
		}
		for (int i = 0; i < RACES.all().size(); i++) {
			if (amounts[i] > 0)
				return RACES.all().get(i);
		}
		return RACES.all().get(0);
		
	}
	
	private int getAmount(Race r) {
		
		double m = Math.max(STANDINGS.CITIZEN().loyalty.getD(r), STANDINGS.CITIZEN().loyaltyTarget.getD(r));
		if (m >= breakPoint) {
			return 0;
		}
		
		m = 1.0 - m/breakPoint;
		
		double dPop = STATS.POP().POP.data().get(null);
		dPop = 0.1 + 0.9*CLAMP.d(dPop/600.0, 0, 1);
		
		
		m *= dPop;
		int pop = STATS.POP().POP.data(HCLASSES.CITIZEN()).get(r);
		
		int rebels = (int) (pop*m);
		
		return CLAMP.i(rebels, 0, pop);
	}
	

	interface SMALL_EVENT extends SAVABLE{
		
		boolean event(int am, Race race);
		void update(double ds);
		
	}
	


	
}
