package settlement.stats.disease;

import game.GAME;
import game.boosting.BOOSTABLES;
import game.time.TIME;
import init.type.DISEASE;
import init.type.DISEASES;
import init.type.POP_CL;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import settlement.stats.StatsInit;
import settlement.stats.StatsInit.StatUpdatable;
import settlement.stats.stat.STAT;
import settlement.stats.stat.StatCollection;
import snake2d.util.color.COLOR;
import util.statistics.HistoryInt;
import util.text.D;
import view.sett.IDebugPanelSett;
import view.tool.PlacableSimple;

public class StatsDisease extends StatCollection{

	private final Data data;
	private final Updater updater;
	private final Epidemic epidemic;
	public final HistoryInt healthHistory = new HistoryInt(STATS.DAYS_SAVED, TIME.days(), true) {
		
		@Override
		public double getD(int fromZero) {
			return get(fromZero)/1024.0;
		};
		
		@Override
		public int get(int fromZero) {
			if (fromZero == 0)
				return (int) (BOOSTABLES.PHYSICS().HEALTH.get(POP_CL.clP())*1024);
			return super.get(fromZero);
		};
		
	};
	
	private static CharSequence ¤¤name = "Disease";
	private static CharSequence ¤¤desc = "Disease stats.";
	public static CharSequence ¤¤low = "The poor heath in our settlement is a serious cause for concern. Lots of people are sick, and if not improved, there will be serious outbreaks.";
	public static CharSequence ¤¤high = "Health in your settlement is good. There is no risk of outbreaks, but health can always be improved additionally to have less sick people to take care of.";
	
	static {
		D.ts(StatsDisease.class);
	}
	
	public StatsDisease(StatsInit init) {
		super(init, "DISEASE", ¤¤name, ¤¤desc);
		data = new Data(init);
		updater = new Updater(data, init);
		epidemic = new Epidemic(init);
		new BoostsHealth();
		
		init.upers.add(new StatUpdatable() {
			
			@Override
			public void update(double ds) {
				healthHistory.set((int) (BOOSTABLES.PHYSICS().HEALTH.get(POP_CL.clP())*1024));
			}
		});
		
		init.savers.put("D_HEALTH_HISTORY", healthHistory);
		
		IDebugPanelSett.add(new PlacableSimple("Disease Infect") {
			
			@Override
			public void place(int x, int y) {
				for (ENTITY e : SETT.ENTITIES().getAtPointL(x, y)) {
					if (e instanceof Humanoid) {
						Humanoid a = (Humanoid) e;
						infect(a.indu(), DISEASES.randomRegular());
					}
						
				}
			}
			
			@Override
			public CharSequence isPlacable(int x, int y) {
				return SETT.ENTITIES().getAtPoint(x, y) != null ? null : E;
			}
		});
		
		IDebugPanelSett.add(new PlacableSimple("Disease Cure") {
			
			@Override
			public void place(int x, int y) {
				for (ENTITY e : SETT.ENTITIES().getAtPointL(x, y)) {
					if (e instanceof Humanoid) {
						Humanoid a = (Humanoid) e;
						cure(a.indu(), false);
					}
						
				}
			}
			
			@Override
			public CharSequence isPlacable(int x, int y) {
				return SETT.ENTITIES().getAtPoint(x, y) != null ? null : E;
			}
		});
	}
	
	public int cases(POP_CL pop, DISEASE d) {
		return data.cases(pop, d);
	}
	
	public boolean shouldHospital(Humanoid i) {
		if (!STATS.SERVICE().hospital.accessRequest(i))
			return false;
		if (shouldDie(i))
			return true;
		if (updater.time(i, 0) > 1.0)
			return true;
		return false;
	}
	
	public boolean shouldDie(Humanoid i) {
		DISEASE d = data.get(i.indu());
		if (d == null)
			return false;
		return data.die.get(i.indu()) == 1;


	}
	
	public boolean diseaseIsDone(Humanoid i, double treatment) {
		return updater.isDone(i, treatment); 
	}
	
	public double diseaseTime(Humanoid i, double treatment) {
		return updater.time(i, treatment); 
	}
	
	public STAT sick() {
		return data.infected;
	}
	
	public STAT incubating() {
		return data.incubating;
	}
	
	public DISEASE get(Induvidual i) {
		return data.get(i);
	}
	
	public DISEASE currentEpidemic() {
		return epidemic.current;
	}
	
	public DiseaseStatus status(Induvidual i) {
		return data.status(i);
	}
	
	public COLOR color(Induvidual i) {
		if (get(i) != null && status(i).active) {
			return get(i).color;
		}
		return null;
	}
	
	public void infect(Induvidual a, DISEASE d) {
		data.set(a, d, DiseaseStatus.ISICK);
	}
	
	public void incubate(Induvidual a, DISEASE d) {
		data.set(a, d, DiseaseStatus.INCUBATING);
	}
	
	public void cure(Induvidual a, boolean hospital) {
		if (get(a) != null && status(a) != DiseaseStatus.IIMMUNE) {
			if (hospital)
				GAME.count().CURED.inc(1);
			data.set(a, get(a), DiseaseStatus.IIMMUNE);
		}
		
	}

	public boolean outbreak(double d, DISEASE currentStrain) {
		return epidemic.outbreak(d, currentStrain);
	}
	
}
