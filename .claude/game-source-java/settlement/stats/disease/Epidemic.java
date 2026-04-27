package settlement.stats.disease;

import java.io.IOException;

import game.boosting.BOOSTABLES;
import game.time.TIME;
import init.type.DISEASE;
import init.type.DISEASES;
import init.type.POP_CL;
import settlement.entity.ENTITY;
import settlement.entity.EntityIterator;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.stats.STATS;
import settlement.stats.StatsInit;
import settlement.stats.StatsInit.StatUpdatable;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import snake2d.util.misc.ACTION;
import snake2d.util.rnd.RND;
import view.sett.IDebugPanelSett;

class Epidemic implements StatUpdatable, SAVABLE{

	
	public DISEASE current;
	public double duration;
	
	public Epidemic(StatsInit init) {
		init.upers.add(this);
		init.savers.put("EPIDEMIC_UPDATER", this);
		
		IDebugPanelSett.add("disease: epidemic", new ACTION() {
			
			@Override
			public void exe() {
				outbreak(0.1+RND.rFloat()*0.5, DISEASES.randomEpidemic(RND.rFloat()));
			}
		});
		
		IDebugPanelSett.add("disease: CURE ALL", new ACTION() {
			
			@Override
			public void exe() {
				new EntityIterator.Humans() {
					
					@Override
					protected boolean processAndShouldBreakH(Humanoid h, int ie) {
						STATS.DISEASE().cure(h.indu(), false);
						return false;
					}
				}.iterate();;
				current = null;
				duration = 0;
			}
		});
	}

	@Override
	public void update(double ds) {
		
		duration -= ds;
		if (duration < 0) {
			current = null;
		}
		
	}

	public boolean outbreak(double spread, DISEASE strain) {
		int am = 0;
		double aveHealth = BOOSTABLES.PHYSICS().HEALTH.get(POP_CL.clP(null, null));
		Humanoid patienZero = null;;	
		
		ENTITY[] ee = SETT.ENTITIES().getAllEnts();
		for (int i = 0; i < ee.length; i++) {
			if (ee[i] != null && ee[i] instanceof Humanoid) {
				Humanoid a = (Humanoid) ee[i];
				if (a.indu().player()) {
					double c = spread*(BOOSTABLES.PHYSICS().HEALTH.get(a.indu())/aveHealth);
					if (RND.rFloat() < c) {
						STATS.DISEASE().incubate(a.indu(), strain);
						am ++;
						if (RND.oneIn(am))
							patienZero = a;
						
					}
				}
			}
		}
		
		if (am > 1) {
			current = strain;
			duration = TIME.secondsPerDay()*(strain.incubationDays + strain.length);
			STATS.DISEASE().infect(patienZero.indu(), strain);
			return true;
		}
		return false;
	}

	@Override
	public void save(FilePutter file) {
		file.d(duration);
		file.i(current == null ? -1 : current.index());
	}

	@Override
	public void load(FileGetter file) throws IOException {
		duration = file.d();
		int ci = file.i();
		current = ci < 0 ? null : DISEASES.all().getC(ci);
	}

	@Override
	public void clear() {
		duration = 0;
		current = null;
	}
	
}
