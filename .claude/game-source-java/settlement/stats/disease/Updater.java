package settlement.stats.disease;

import game.boosting.BOOSTABLES;
import game.time.TIME;
import init.type.DISEASE;
import init.type.DISEASES;
import settlement.entity.humanoid.Humanoid;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import settlement.stats.StatsInit;
import settlement.stats.StatsInit.StatUpdatableI;
import settlement.stats.law.LAW;
import snake2d.util.rnd.RND;
import view.main.VIEW;

final class Updater implements StatUpdatableI{

	private final Data data;
	
	Updater(Data data, StatsInit init){
		this.data = data;
		init.updatable.add(this);
	}

	@Override
	public void update16(Humanoid h, int updateR, boolean day, int updateI) {
		Induvidual i = h.indu();
		if (VIEW.b().isActive())
			return;
		DiseaseStatus st = data.status(i);
		
		switch(st) {
		case IIMMUNE:
			if (!day)
				return;
			if (data.count.isMax(i)) {
				data.set(i, null, null);
			}else {
				data.count.inc(i, 1);
			}
			regular(h);
			break;
		case INCUBATING:
			DISEASE d = data.get(i);
			if (d == null || ((((STATS.RAN().get(i, 11, 16)+TIME.seasons().bitsSinceStart()) >>7)&0b11) == 0 && LAW.curfew().is())) {
				data.set(i, null, null);
			}else if (RND.oneIn(d.incubationDays*16)) {
				data.set(i, data.get(i), DiseaseStatus.ISICK);
			}
			break;
		case ISICK:
			if (!day)
				return;
			data.count.inc(i, 1);
			break;
		case NONE:
			if (day)
				regular(h);
			break;
		default:
			break;
	
	
	}
		
	}
	
	private void regular(Humanoid h) {
		Induvidual i = h.indu();
		DiseaseStatus st = data.status(i);
		if (shouldGetSickDay(h.indu())) {
			DISEASE d2 = DISEASES.randomRegular();
			if (d2 == null || (data.get(i) == d2 && st == DiseaseStatus.IIMMUNE)) {
				
			}else {
				data.set(i, d2, DiseaseStatus.ISICK);
			}
		}
	}

	public boolean isDone(Humanoid i, double treatment) {
		return time(i, treatment) <= 0;
	}
	
	public double time(Humanoid h, double treatment) {
		Induvidual i = h.indu();
		DiseaseStatus st = data.status(i);
		if (!st.active)
			return 0;
		if (data.get(i) == null)
			return 0;
		return data.get(i).length*(1-treatment) - (data.count.get(i)+h.partOfDay());
	}
	

	
	public static boolean shouldGetSickDay(Induvidual a) {

		double chance = DISEASES.regularDays()*(1+Math.max(BOOSTABLES.PHYSICS().HEALTH.get(a), 0));
		
		if (RND.oneIn((int)Math.ceil(chance))) {
			
			return true;
		}
		return false;
	}
}
