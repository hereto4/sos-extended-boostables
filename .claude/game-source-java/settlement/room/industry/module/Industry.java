package settlement.room.industry.module;



import game.GAME;
import game.GameDisposable;
import game.boosting.Boostable;
import game.faction.FResources.RTYPE;
import game.faction.Faction;
import game.faction.npc.FactionNPC;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.value.GVALUES;
import init.value.Lockable;
import settlement.entity.humanoid.Humanoid;
import settlement.room.industry.module.FlatIndustries.FlatIndustry;
import settlement.room.industry.module.consumption.RoomConsumptionAbs;
import settlement.room.main.RoomBlueprintImp;
import settlement.room.main.RoomInstance;
import settlement.room.main.util.RoomInitData;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.Json;
import snake2d.util.file.SAVABLE;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.INDEXED;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.SPRITE;
import util.data.DOUBLE_O;
import util.data.DataOSimple;
import world.map.regions.Region;

public class Industry extends RoomConsumptionAbs implements SAVABLE, IndustryRate, INDEXED {

	private static ArrayListGrower<Industry> all = new ArrayListGrower<Industry>();
	
	static {
		new GameDisposable() {
			
			@Override
			protected void dispose() {
				all.clear();
			}
		};
	}
	
	public static LIST<Industry> all(){
		return all;
	}
	
	private final ArrayListGrower<IndustryResource> outs = new ArrayListGrower<IndustryResource>();
	
	protected IndustryResourceOut[] outMap = new IndustryResourceOut[RESOURCES.ALL().size()];	
	
	private final int index;
	public double AIMul = 1.0;
	
	IndustryRegion reg = null;
	
	Lockable<Faction> lockable = GVALUES.FACTION.LOCK.empty;
	
	public Industry(RoomBlueprintImp blue, RESOURCE out, double outRate, Boostable bonus) {
		super(blue, bonus);
		index = all.add(this);
		new IndustryResourceOut(data, out, outRate, outRate, outRate);
	}
	
	public Industry(RoomBlueprintImp blue, RESOURCE[] outs, double[] outRates, Boostable bonus) {
		super(blue, bonus);
		index = all.add(this);
		for (int i = 0; i < outs.length; i++) {
			RESOURCE out = outs[i];
			double outRate = outRates[i];
			new IndustryResourceIn(data, out, outRate, outRate, outRate);
		}
		
		
	}
	
	public Industry(RoomBlueprintImp blue, Json json, Boostable bonus) {
		super(blue, bonus);
		index = all.add(this);
		json = json.json("INDUSTRY");
		if (json.has("IN")){
			Json j = json.json("IN");
			for (String k : j.keys()) {
				RESOURCE res = RESOURCES.map().get(k, j);
				double rate = j.d(k, 0, 10000);
				new IndustryResourceIn(data, res, rate, rate, rate);
			}
			
		}
		if (json.has("OUT")) {
			Json j = json.json("OUT");
			for (String k : j.keys()) {
				RESOURCE res = RESOURCES.map().get(k, j);
				
				if (j.jsonIs(k)) {
					Json jj = j.json(k);
					double rate = jj.d("PLAYER", 0, 100000);
					double AI = jj.dTry("AI", 0, 100000, rate);
					double AISPEED = jj.dTry("AI_SPEED", 0, 100000, 1);
					new IndustryResourceOut(data, res, rate, AI, AISPEED);
				}else {
					double rate = j.d(k, 0, 10000);
					new IndustryResourceOut(data, res, rate, rate, rate);
				}
			}
		}
		
	}
	
	public Lockable<Faction> lockable(){
		return lockable;
	}
	

	
	public IndustryResource out(RESOURCE res) {
		return outMap[res.index()];
	}

	public LIST<IndustryResource> outs(){
		return outs;
	}
	
	public double aiBonus(FactionNPC f, FlatIndustry i) {
		return bonus().get(f)*AIMul;
	}
	
	
	private final class IndustryResourceOut extends IndustryResource{
		
		
		IndustryResourceOut(DataOSimple<ROOM_IDATA_INSTANCE> data, RESOURCE res, double rate, double AI, double AIRate) {
			super(data, outs.size(), res, rate, AI, AIRate);
			outs.add(this);
			outMap[resource.index()] = this;
			allRes.add(this);
		}
		
		@Override
		public int inc(ROOM_IDATA_INSTANCE r, double amount, boolean record) {
			if (!Double.isFinite(amount)) {
				GAME.Warn(""+amount);
				return 0;
			}
			if (!Double.isFinite(day.getD(r))) {
				day.setD(r, 0);
			}
			
			int old = (int) day.getD(r);
			day.incD(r, amount);
			int now = (int) day.getD(r);
			int d = now-old;
			if (d != 0) {
				if (record)
					GAME.player().res().inc(resource, RTYPE.PRODUCED, d);
				year.inc(r, d);
				history.inc(d);
				GAME.count().CRAFTED.inc(1);
			}
			return d;
		}
		
		@Override
		protected double getEffort(Humanoid skill, ROOM_IDATA_INSTANCE r, double workSeconds) {
			return IndustryUtil.calcProductionRate(rateSeconds*workSeconds, skill, Industry.this, (RoomInstance)r);
		}

	}
	
	public IndustryRegion reg() {
		return reg;
	}

	@Override
	public int index() {
		return index;
	}

	public static LIST<Industry> createIndustries(RoomBlueprintImp blue, RoomInitData init, RoomBoost[] boosts,
			Boostable bonus, DOUBLE_O<Region> regBonus) {

		Json[] js = init.data().jsons("INDUSTRIES", 1);

		ArrayList<Industry> res = new ArrayList<>(js.length);
		for (Json j : js) {
			Industry i = new Industry(blue, j, bonus);
			if (i.outs().size() == 0)
				j.error(blue.key + " has no out resources declared. This can be due to an outdated mod.", "INDUSTRIES");
			for (RoomBoost b : boosts) {
				i.roomBoosts.add(b);
			}
			
			res.add(i);
		}
		if (res.size() > 1) {
			int ii = 0;
			for (int i = 0; i < res.size(); i++) {
				RESOURCE u =  unique(res.get(i), res);
				SPRITE icon = blue.iconBig().twin(u.icon().scaled(0.5), DIR.SE, 1);
				String desc = "";
				for (IndustryResource ir : res.get(i).ins()) {
					if (desc.length() > 0)
						desc += " + ";
					desc += ir.resource.name;
				}
				desc += " -> " + res.get(i).outs().get(0).resource.name;
				res.get(i).lockable = GVALUES.FACTION.LOCK.push("ROOM_" + blue.key + "_RECIPE_" + ii, blue.info.name + ": " + unique(res.get(i), res).name, desc, icon);
				ii++;
				
			}
			
			
		}
		
		return res;

	}
	
	public static LIST<Industry> createIndustries(RoomBlueprintImp blue, RoomInitData init, RoomBoost[] boosts,
			Boostable bonus) {

		DOUBLE_O<Region> rr = new DOUBLE_O<Region>() {

			@Override
			public double getD(Region t) {
				return 1.0;
			}
			
		};
		return createIndustries(blue, init, boosts, bonus, rr);
	}
	
	private static RESOURCE unique(Industry ins, LIST<Industry> others) {
		RESOURCE res = ins.outs().get(0).resource;
		boolean unique = true;
		for (Industry i : others) {
			if (i != ins && i.outs().get(0).resource == res)
				unique = false;
		}
		if (unique)
			return res;
		for (IndustryResource r : ins.ins()) {
			unique = true;
			for (Industry i : others) {
				if (i != ins) {
					for (IndustryResource or : i.ins()) {
						if (or.resource == r.resource) {
							unique = false;
						}
					}
				}
			}
			if (unique)
				return r.resource;
		}
		return res;
	}
	
}
