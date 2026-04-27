package settlement.room.service.food.tavern;

import java.io.IOException;
import java.util.Arrays;

import game.GAME;
import game.faction.FResources.RTYPE;
import init.resources.Meal;
import init.resources.RESOURCES;
import init.resources.ResG;
import init.resources.ResGDrink;
import init.type.NEEDS;
import settlement.misc.util.FSERVICE;
import settlement.path.finders.SFinderRoomService;
import settlement.room.main.Room;
import settlement.room.main.RoomBlueprintIns;
import settlement.room.main.category.RoomCategorySub;
import settlement.room.main.furnisher.Furnisher;
import settlement.room.main.job.ROOM_EMPLOY_AUTO;
import settlement.room.main.util.RoomInitData;
import settlement.room.service.module.RoomServiceAccess;
import settlement.room.service.module.RoomServiceAccess.ROOM_SERVICE_ACCESS_HASER;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.rnd.RND;
import snake2d.util.sets.Bitmap1D;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LISTE;
import view.sett.ui.room.UIRoomModule;

public final class ROOM_TAVERN extends RoomBlueprintIns<TavernInstance> implements ROOM_SERVICE_ACCESS_HASER, ROOM_EMPLOY_AUTO{
	

	public final RoomServiceAccess serviceData;
	final Job tile;
	final Constructor constructor;
	final long[] amounts = new long[RESOURCES.DRINKS().all().size()];
	long total;
	
	public ROOM_TAVERN(String key, int index, RoomInitData data, RoomCategorySub cat) throws IOException {
		super(index, data, key, cat);
		tile = new Job(this);
		constructor = new Constructor(this, data);
		serviceData = new RoomServiceAccess(this, data, NEEDS.TYPES().THIRST) {
			
			@Override
			public FSERVICE service(int tx, int ty) {
				return tile.servive(tx, ty);
			}
			
		};
		
	}

	@Override
	public void appendView(LISTE<UIRoomModule> mm) {
		mm.add(new Gui(this).make());
	}
	
	@Override
	public Furnisher constructor() {
		return constructor;
	}
	
	@Override
	protected void update(double ds) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public SFinderRoomService service(int tx, int ty) {
		return serviceData.finder;
	}

	@Override
	protected void saveP(FilePutter saveFile){
		serviceData.saver.save(saveFile);
	}
	
	@Override
	protected void loadP(FileGetter saveFile) throws IOException{
		serviceData.saver.load(saveFile);
		Arrays.fill(amounts, 0l);
		total = 0;
		for (TavernInstance ins : all()) {
			for (ResGDrink g : RESOURCES.DRINKS().all()) {
				amounts[g.index()] += ins.amount(g);
				total += ins.amount(g);
			}
		}
	}
	
	@Override
	protected void clearP() {
		serviceData.saver.clear();
		Arrays.fill(amounts, 0l);
		total = 0;
	}
	
	@Override
	public RoomServiceAccess service() {
		return serviceData;
	}
	
	@Override
	public boolean autoEmploy(Room r) {
		return ((TavernInstance)r).auto;
	}

	@Override
	public void autoEmploy(Room r, boolean b) {
		((TavernInstance)r).auto = b;
	}
	
	public int grab(LIST<ResGDrink> prefs, int amount, int tx, int ty) {
		
		ResGDrink pref = prefs.rnd();
		TavernInstance ins = getter.get(tx, ty);
		if (ins == null)
			return Meal.make(pref, 0, 0);
		FSERVICE f = tile.servive(tx, ty);
		if (f == null)
			return Meal.make(pref, 0, 0);
		
		int max = ins.amount(null)-(ins.service.reserved() + ins.service().available()-1);
		
		if (amount > max) {
			amount = max;
		}
		int am = amount;
		

		
		
		
		if (am <= 0) {
			GAME.Notify("here! " + am + " " + tx + " " + ty + " " + ins.amount(null) + " " + (ins.service.reserved() + " " + ins.service().available()) + " " + ins.tablesreserved);
			return Meal.make(pref, 0, 0);
		}
		
		int ipref = 0;
		int iopref = 0;
		
		ResGDrink ee = null;
		
		if (ins.amount(pref) > 0) {
			ee = pref;
			int a = Math.min(ins.amount(pref), am);
			am -= a;
			ipref += a;
			ins.amountInc(ee, -a);
			GAME.player().res().inc(pref.resource, RTYPE.CONSUMED, -a);
			
			
		}
		
		if (am > 0) {
			int ri = RND.rInt(prefs.size());
			for (int i = 0; i < prefs.size() && am > 0; i++) {
				ResGDrink g = prefs.getC(ri+1);
				if (ins.amount(g) > 0) {
					if (ee == null)
						ee = g;
					int a = Math.min(ins.amount(g), am);
					am -= a;
					iopref += a;
					ins.amountInc(g, -a);
					GAME.player().res().inc(g.resource, RTYPE.CONSUMED, -a);
				}
			}
		}
		
		if (am > 0) {
			int ri = RND.rInt(RESOURCES.DRINKS().all().size());
			for (int i = 0; i < RESOURCES.DRINKS().all().size() && am > 0; i++) {
				ResGDrink g = RESOURCES.DRINKS().all().getC(ri+i);
				if (ins.amount(g) > 0) {
					if (ee == null)
						ee = g;
					int a = Math.min(ins.amount(g), am);
					am -= a;
					ins.amountInc(g, -a);
					GAME.player().res().inc(g.resource, RTYPE.CONSUMED, -a);
				}
			}
		}
		
		if (ee == null)
			ee = RESOURCES.DRINKS().all().rnd();
		
		tile.sActiveDrink.set(ins, ee.index()+1);
		
		

		amount -= am;
	
		double pv = (ipref + 0.25*iopref)/(amount);
		return Meal.make(ee, amount, pv);
		
	}
	
	public void anotherRound(int tx, int ty) {
		FSERVICE f = tile.servive(tx, ty);
		if (f != null) {
			tile.sUsedDrinks.inc(getter.get(tx, ty), 1);
		}
	}
	
	private final Bitmap1D ress = new Bitmap1D(RESOURCES.ALL().size(), false);
	private int ci = 0;
	
	public boolean uses(ResG rr) {
		if (ci == GAME.updateI())
			return ress.get(rr.resource.index());
		ci = GAME.updateI();
		ress.clear();
		
		for (int ri = 0; ri < RESOURCES.DRINKS().all().size(); ri++) {
			ResGDrink r = RESOURCES.DRINKS().all().get(ri);
			for (int i = 0; i < instancesSize(); i++) {
				TavernInstance ii = getInstance(i);
				if (ii.use.has(r.resource)) {
					ress.set(r.resource.index(), true);
					break;
				}
			}
		}
		
		return ress.get(rr.resource.index());
		
	}
}
