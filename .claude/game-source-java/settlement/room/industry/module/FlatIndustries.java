package settlement.room.industry.module;

import game.boosting.BOOSTING;
import game.boosting.Boostable;
import game.boosting.BoostableCat;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.sprite.UI.Icon;
import settlement.room.main.ROOMS;
import settlement.room.main.RoomBlueprintImp;
import snake2d.util.datatypes.DIR;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.INDEXED;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LinkedList;
import snake2d.util.sprite.text.Str;
import util.text.D;

public class FlatIndustries {

	private final ArrayListGrower<FlatIndustry> all = new ArrayListGrower<>();
	
	private final IInBoost[] boos = new IInBoost[RESOURCES.ALL().size()];
	
	private static CharSequence ¤¤desc = "Decreases the consumption of this resource in industries:";
	
	static {
		D.ts(FlatIndustries.class);
	}
	
	FlatIndustries(ROOMS r, RoomIndustries indus){
		make(new LinkedList<INDUSTRY_HASER>(r.FARMS));
		make(new LinkedList<INDUSTRY_HASER>(r.ORCHARDS));
		make(new LinkedList<INDUSTRY_HASER>(r.PASTURES));
		make(new LinkedList<INDUSTRY_HASER>(r.FISHERIES));
		make(new LinkedList<INDUSTRY_HASER>(r.MINES));
		make(new LinkedList<INDUSTRY_HASER>(r.REFINERS));
		make(new LinkedList<INDUSTRY_HASER>(r.WORKSHOPS));
		make(r.WOOD_CUTTER);
		
		ArrayListGrower<ArrayListGrower<RoomBlueprintImp>> iiis = new ArrayListGrower<>();
		for (@SuppressWarnings("unused") RESOURCE res : RESOURCES.ALL())
			iiis.add(new ArrayListGrower<RoomBlueprintImp>());
		
		for (Industry ins : indus.all) {
			
			for (IndustryResource ii : ins.ins()) {
				RESOURCE res = ii.resource;
				if (iiis.get(res.index()).contains(ins.blue))
					continue;
				iiis.get(res.index()).add(ins.blue);
			}
			
		}
		for (RESOURCE res : RESOURCES.ALL()) {
			if (iiis.get(res.index()).size() > 0) {
				boos[res.index()] = new IInBoost(res, iiis.get(res.index()));
			}
		}
	}
	
	public LIST<FlatIndustry> all(){
		return all;
	}
	
	public IInBoost inBoost(RESOURCE res) {
		return boos[res.index()];
	}
	
	private LIST<FlatIndustry> make(LIST<INDUSTRY_HASER> li) {
		
		ArrayListGrower<FlatIndustry> res = new ArrayListGrower<>();
		for (INDUSTRY_HASER h : li) {
			res.add(make(h));
		}
		return res;
	}
	
	private LIST<FlatIndustry> make(INDUSTRY_HASER blue) {
		
		INDUSTRY_HASER hh = (INDUSTRY_HASER) blue;
		ArrayListGrower<FlatIndustry> res = new ArrayListGrower<>();
		for (int ii = 0; ii < hh.industries().size(); ii++) {
			Industry ins = hh.industries().get(ii);
			FlatIndustry fi = new FlatIndustry(all.size(), ins, hh);
			res.add(fi);
			all.add(fi);
		}
		return res;
		
	}
	
	public static class FlatIndustry implements INDEXED{
		
		public final String key;
		public final CharSequence name;
		public final CharSequence desc;
		public final Industry industry;
		public final RoomBlueprintImp blue;
		public final Icon icon;
		public final int index;
		
		
		FlatIndustry(int index, Industry ins, INDUSTRY_HASER hs) {
			this.index = index;
			
			
			
			blue = ins.blue;
			this.industry = ins;
			this.desc = ins.blue.info.desc;
			
			String key = ins.blue.key;
			String name = ""+blue.info.name;
			Icon icon = blue.iconBig();
			
			RESOURCE in = uniqueResource(ins, hs);
			if (in != null) {
				key += "I_" + in.key;
				name += "(" + in.name + ")";
				icon = icon.twin(in.icon().scaled(0.5), DIR.SE, 2);
			}
			
			this.key = key;
			this.name = name;
			this.icon = icon;
		}

		@Override
		public int index() {
			return index;
		}
		
	}

	
	private static RESOURCE uniqueResource(Industry ins, INDUSTRY_HASER hs) {
		if (hs.industries().size() == 1)
			return null;
		
		if (ins.outs().size() > 0) {
			RESOURCE res = ins.outs().get(0).resource;
			boolean unique = true;
			for (Industry io : hs.industries()) {
				if (io == ins)
					continue;
				if (io.outs().get(0).resource == res)
					unique = false;
				
			}
			if (unique)
				return res;
		}
		
		for (Industry io : hs.industries()) {
			if (io == ins)
				continue;
			for (IndustryResource in : ins.ins()) {
				boolean contains = false;
				for (IndustryResource ino : io.ins()) {
					if (ino.resource == in.resource)
						contains = true;
				}
				if (!contains)
					return in.resource;
			}
		}
		
		return null;
		
	}

	public static class IInBoost {
		
		public final Boostable bo;
		public final LIST<RoomBlueprintImp> blues;
		
		IInBoost(RESOURCE res, LIST<RoomBlueprintImp> blues){
			this.blues = blues;
			Str.TMP.clear().add(¤¤desc);
			for (int bi = 0; bi < blues.size(); bi++) {
				Str.TMP.s();
				Str.TMP.add(blues.get(bi).info.names);
				if (bi < blues.size()-1)
					Str.TMP.add(',');
				else
					Str.TMP.add('.');
			}
			
			String desc = "" + Str.TMP;
			
			bo = BOOSTING.push(res.key, 1, res.name, desc, res.icon(), BoostableCat.ALL().RES_CONSUMPTION);
		}
		
		
	}
	
}
