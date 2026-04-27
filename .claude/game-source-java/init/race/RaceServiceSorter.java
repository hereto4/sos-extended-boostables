package init.race;

import java.util.Arrays;
import java.util.Comparator;

import init.type.HCLASS;
import init.type.HCLASSES;
import settlement.stats.STATS;
import settlement.stats.colls.StatsBurial.StatGrave;
import settlement.stats.standing.StatStanding;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;

public final class RaceServiceSorter {
	

	public final LIST<LIST<StatGrave>> GRAVES;
	
	RaceServiceSorter(Race race) {
		

		
		
		
		Sorter<StatGrave> serG = new Sorter<StatGrave>() {

			@Override
			StatStanding standing(StatGrave t) {
				return t.standing();
			}
		};
		
		GRAVES = serG.sort(STATS.BURIAL().graves(), race);


		

		
	}
	
	private static abstract class Sorter<T> {
		
		@SuppressWarnings("unchecked") LIST<LIST<T>> sort(LIST<T> all, Race race) {
			ArrayList<LIST<T>> res = new ArrayList<>(HCLASSES.ALL().size());
			
			for (HCLASS dd : HCLASSES.ALL()) {
				
				HCLASS c = dd == HCLASSES.OTHER() ? HCLASSES.CITIZEN() : dd;
				
				
				int am = 0;
				for (T h : all) {
					if (standing(h).definition(race).get(c).max > 0) {
						am++;
					}
				}
				
				Object[] al = new Object[am];
				am = 0;
				for (T h : all) {
					if (standing(h).definition(race).get(c).max > 0) {
						al[am++] = h;
					}
				}
		
				Arrays.sort((T[])al, new Comparator<T>() {

					@Override
					public int compare(T o1, T o2) {
						double d = standing(o1).definition(race).get(c).max - standing(o2).definition(race).get(c).max;
						if (d < 0)
							return 1;
						if (d > 0)
							return -1;
						return 0;
					}
				});
				res.add(new ArrayList<T>((T[])al));
			}
			
			
			return res;

		}
		
		abstract StatStanding standing(T t);
		
	}
	

	
//	public LIST<RoomServiceAccess> services(HCLASS cl, NEED need){
//		return sorts.get(need.index()).res.get(cl.index());
//	}
	

	
//	public LIST<RoomService> services(Induvidual i, NEED need){
//		tmp.clearSloppy();
//
//		if (need == NEEDS.TYPES().TEMPLE) {
//			if (STATS.RELIGION().TEMPLE_TOTAL.standing().max(i.clas(), i.race()) > 0)
//				for (ROOM_TEMPLE t : SETT.ROOMS().TEMPLES.perRel.get(STATS.RELIGION().getter.get(i).religion.index()))
//					tmp.add(t.service());
//			return tmp;
//		}
//		
//		if (need == NEEDS.TYPES().SHRINE) {
//			if (STATS.RELIGION().SHRINE_TOTAL.standing().max(i.clas(), i.race()) > 0)
//				for (ROOM_SHRINE t : SETT.ROOMS().TEMPLES.perRelShrine.get(STATS.RELIGION().getter.get(i).religion.index()))
//					tmp.add(t.service());
//			return tmp;
//		}
//		
//		for (RoomServiceAccess n : sorts.get(need.index()).res.get(i.clas().index())) {
//			tmp.tryAdd(n);
//		}
//		return tmp;
//	}
	

}


