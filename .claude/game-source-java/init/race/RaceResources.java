package init.race;

import init.resources.RBIT;
import init.resources.RBIT.RBITImp;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.resources.RES_AMOUNT;
import init.type.HCLASS;
import init.type.HCLASSES;
import init.type.POP_CL;
import settlement.stats.STATS;
import settlement.stats.equip.EquipCivic;
import settlement.stats.equip.WearableResource;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.INDEXED;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LISTE;
import snake2d.util.sets.LinkedList;

public class RaceResources {

	public final LIST<RaceResource> ALL;
	public final RBIT BIT;
	private final RaceResource[] map = new RaceResource[RESOURCES.ALL().size()];
	
	private final ArrayList<ArrayListGrower<WearableResource>> perCl = new ArrayList<>(POP_CL.ALL().size());
	private final LIST<LIST<RES_AMOUNT>> homeres;
	private final LIST<RES_AMOUNT> homeresAll;
	
	RaceResources(LIST<Race> races){
		
		{
			ArrayList<LIST<RES_AMOUNT>> ress = new ArrayList<>(HCLASSES.ALL().size());
			RES_AMOUNT.Imp[] all = new RES_AMOUNT.Imp[RESOURCES.ALL().size()];
			
			for (HCLASS c : HCLASSES.ALL()) {
				ArrayList<RES_AMOUNT> rr = new ArrayList<>(RESOURCES.ALL().size());
				for (RESOURCE res : RESOURCES.ALL()) {
					int am = 0;
					for (Race r : RACES.all()) {
						am = Math.max(am, r.home().clas(c).amount(res));
					}
					if (am > 0) {
						rr.add(new RES_AMOUNT.Abs(res, am));
						if (all[res.index()] != null) {
							all[res.index()].set(Math.max(all[res.index()].amount(), am));
						}else {
							all[res.index()] = new RES_AMOUNT.Imp(res, am);
						}
					}
				}
				ress.add(new ArrayList<RES_AMOUNT>(rr));
			}
			homeres = ress;
			
			LinkedList<RES_AMOUNT> tm = new LinkedList<>();
			for (RES_AMOUNT a : all) {
				if (a != null)
					tm.add(a);
			}
			
			homeresAll = new ArrayList<RES_AMOUNT>(tm);
		}
		
		
		
	
		
		ArrayListGrower<RaceResource> all = new ArrayListGrower<>();
		RBITImp bit = new RBITImp();
		
		for (Race r : races) {
			for (HCLASS cl : HCLASSES.ALL()) {
				int i = 0;
				for (RES_AMOUNT a : r.home().clas(cl).resources()) {
					if (map[a.resource().index()] == null) {
						RaceResource res = new RaceResource(all, a.resource());
						map[res.res.index()] = res;
						bit.or(res.res);
					}
					map[a.resource().index()].map.get(cl.get(r).index).add(STATS.HOME().furniture(i));
					i++;
				}
			}
		}
		
		for (EquipCivic e : STATS.EQUIP().civics()) {
			if (map[e.resource.index()] == null) {
				RaceResource res = new RaceResource(all, e.resource);
				map[res.res.index()] = res;
				bit.or(res.res);
			}
			for (Race r : races) {
				for (HCLASS cl : HCLASSES.ALL()) {
					map[e.resource().index()].map.get(cl.get(r).index).add(e);
				}
			}
			
			
		}
		
		{
			while(perCl.hasRoom())
				perCl.add(new ArrayListGrower<WearableResource>());
			for (POP_CL cl : POP_CL.ALL()) {
				for (RaceResource r : all) {
					perCl.get(cl.index).add(r.map.get(cl.index));
				}
			}
		}
		
		this.ALL = all;
		this.BIT = bit;
		
	}
	
	public LIST<RES_AMOUNT> homeResMax(HCLASS c){
		if (c == null)
			return homeresAll;
		return homeres.get(c.index());
	}
	
	
	public LIST<WearableResource> get(POP_CL cl, RESOURCE res){
		return map[res.index()].map.get(cl.index);
	}

	public RaceResource get(RESOURCE res){
		return map[res.index()];
	}
	
	public LIST<WearableResource> all(POP_CL cl){
		return perCl.get(cl.index);
	}
	
	public static class RaceResource implements INDEXED {
		
		public final RESOURCE res;
		private final int index;
		private final ArrayList<ArrayListGrower<WearableResource>> map = new ArrayList<>(POP_CL.ALL().size());
		
		RaceResource(LISTE<RaceResource> all, RESOURCE res){
			this.res = res;
			this.index = all.add(this);
			while(map.hasRoom())
				map.add(new ArrayListGrower<WearableResource>());
			
		}

		@Override
		public int index() {
			return index;
		}
		
		
	}
}
