package settlement.room.service.food.eatery;

import java.io.IOException;
import java.util.Arrays;

import game.GAME;
import init.resources.Meal;
import init.resources.RESOURCES;
import init.resources.ResG;
import init.resources.ResGEat;
import init.type.NEEDS;
import settlement.misc.util.FSERVICE;
import settlement.path.finders.SFinderRoomService;
import settlement.room.industry.module.Industry;
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

public final class ROOM_EATERY extends RoomBlueprintIns<EateryInstance> implements ROOM_EMPLOY_AUTO, ROOM_SERVICE_ACCESS_HASER{

	
	final Constructor constructor;
	final Crate crate = new Crate(this);
	final Industry industry;
	final RoomServiceAccess service;
	final long[] amounts = new long[RESOURCES.EDI().all().size()];
	long total;

	public ROOM_EATERY(String key, int index, RoomInitData data, RoomCategorySub cat) throws IOException {
		super(index, data, key, cat);
		constructor = new Constructor(this, data);
		industry = new Industry(this, 
				RESOURCES.EDI().makeArray(), new double[RESOURCES.EDI().all().size()], 
				null);
		service = new RoomServiceAccess(this, data, NEEDS.TYPES().HUNGER) {
			
			@Override
			public FSERVICE service(int tx, int ty) {
				return crate.service(tx, ty);
			}

		};
	}


	@Override
	protected void update(double ds) {
		
	}

	
	@Override
	public Furnisher constructor() {
		return constructor;
	}

	@Override
	public SFinderRoomService service(int tx, int ty) {
		return service.finder;
	}
	
	

	
	@Override
	protected void saveP(FilePutter saveFile){
		industry.save(saveFile);
		service.saver.save(saveFile);
	}
	
	@Override
	protected void loadP(FileGetter saveFile) throws IOException{
		industry.load(saveFile);
		service.saver.load(saveFile);
		Arrays.fill(amounts, 0l);
		total = 0;
		for (EateryInstance ins : all()) {
			for (ResGEat g : RESOURCES.EDI().all()) {
				amounts[g.index()] += ins.amount(g);
				total += ins.amount(g);
			}
		}
	}
	
	@Override
	protected void clearP() {
		industry.clear();
		service.saver.clear();
		total = 0;
		Arrays.fill(amounts, 0l);
	}
	
	public long totalFood() {
		return total;
	}
	
	public long amount(ResG e) {
		return amounts[e.index()];
	}
	
	@Override
	public void appendView(LISTE<UIRoomModule> mm) {
		mm.add(new Gui(this).make());
	}

	@Override
	public boolean autoEmploy(Room r) {
		return ((EateryInstance) r).autoE;
	}

	@Override
	public void autoEmploy(Room r, boolean b) {
		((EateryInstance) r).autoE = b;
	}

	public Industry industry() {
		return industry;
	}

	@Override
	public RoomServiceAccess service() {
		return service;
	}
	

	public int eat(LIST<ResG> prefs, int amount, int tx, int ty) {
		
		
		ResG pref = prefs.rnd();
		EateryInstance ins = getter.get(tx, ty);
		if (ins == null)
			return Meal.make(pref, 0, 0);
		FSERVICE f = crate.service(tx, ty);
		if (f == null)
			return Meal.make(pref, 0, 0);
		f.consume();
		
		
		
		int am = amount;
		if (amount > ins.amountTotal()-ins.serviceReserved()+1) {
			am = ins.amountTotal()-ins.serviceReserved()+1;
		}
		
		int ipref = 0;
		int iopref = 0;
		

		if (am <= 0) {
			GAME.Notify("here! " + am + " " + amount + " " + tx + " " + ty);
			return Meal.make(pref, 0, 0);
		}

		ResG ee = null;
		
		if (ins.amount(pref) > 0) {
			
			ee = pref;
			int a = Math.min(ins.amount(pref), am);
			am -= a;
			ipref += a;
			ins.consume(pref, a, tx, ty);
			
		}
		
		if (am > 0) {
			int ri = RND.rInt(prefs.size());
			for (int i = 0; i < prefs.size() && am > 0; i++) {
				ResG g = prefs.getC(i + ri);
				if (ins.amount(g) > 0) {
					if (ee == null)
						ee = g;
					int a = Math.min(ins.amount(g), am);
					am -= a;
					iopref += a;
					ins.consume(g, a, tx, ty);
				}
			}
		}
		
		if (am > 0) {
			int ri = RND.rInt(RESOURCES.EDI().all().size());
			for (int i = 0; i < RESOURCES.EDI().all().size() && am > 0; i++) {
				ResG g = RESOURCES.EDI().all().getC(i + ri);
				if (ins.amount(g) > 0) {
					if (ee == null)
						ee = g;
					int a = Math.min(ins.amount(g), am);
					am -= a;
					ins.consume(g, a, tx, ty);
				}
			}
		}
		
		if (ee == null)
			ee = RESOURCES.EDI().all().rnd();
		
		
		amount -= am;
		
		int pt = ipref + iopref;
		double pv = 0;
		if (pt > 0)
			pv = (ipref + 0.25*iopref)/pt;
		return Meal.make(ee, amount, pv);
		
	}
	
	private final Bitmap1D ress = new Bitmap1D(RESOURCES.ALL().size(), false);
	private int ci = 0;
	
	public boolean uses(ResG rr) {
		if (ci == GAME.updateI())
			return ress.get(rr.resource.index());
		ci = GAME.updateI();
		ress.clear();
		
		for (int ri = 0; ri < RESOURCES.EDI().all().size(); ri++) {
			ResGEat r = RESOURCES.EDI().all().get(ri);
			for (int i = 0; i < instancesSize(); i++) {
				EateryInstance ii = getInstance(i);
				if (ii.uses(r)) {
					ress.set(r.resource.index(), true);
					break;
				}
			}
		}
		
		return ress.get(rr.resource.index());
		
	}
	
	@Override
	public boolean registersEnvironment() {
		return true;
	}
	
}
