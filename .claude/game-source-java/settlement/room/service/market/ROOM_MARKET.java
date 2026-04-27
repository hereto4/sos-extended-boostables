package settlement.room.service.market;

import java.io.IOException;
import java.util.Arrays;

import game.GAME;
import init.race.RACES;
import init.race.RaceResources.RaceResource;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
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
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.Bitmap1D;
import snake2d.util.sets.LISTE;
import view.sett.ui.room.UIRoomModule;

public final class ROOM_MARKET extends RoomBlueprintIns<MarketInstance> implements ROOM_EMPLOY_AUTO, ROOM_SERVICE_ACCESS_HASER{

	public static final String TYPE = "MARKET";
	final Constructor constructor;
	final Crate crate = new Crate(this);
	final RoomServiceAccess service;
	final long[] amounts = new long[RESOURCES.ALL().size()];
	long total;
	
	public ROOM_MARKET(String key, int index, RoomInitData data, RoomCategorySub cat) throws IOException {
		super(index, data, key, cat);
		
		constructor = new Constructor(this, data);
		
		service = new RoomServiceAccess(this, data, NEEDS.TYPES().SHOPPING) {
			
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
		service.saver.save(saveFile);
		saveFile.l(total);
		saveFile.lsE(amounts);
	}
	
	@Override
	protected void loadP(FileGetter saveFile) throws IOException{
		service.saver.load(saveFile);
		total = saveFile.l();
		saveFile.lsE(amounts);
	}
	
	@Override
	protected void clearP() {
		service.saver.clear();
		total = 0;
		Arrays.fill(amounts, 0l);
	}
	
	public long totalFood() {
		return total;
	}
	
	public long amount(RESOURCE e) {
		return amounts[e.index()];
	}
	
	@Override
	public void appendView(LISTE<UIRoomModule> mm) {
		mm.add(new Gui(this).make());
	}

	@Override
	public boolean autoEmploy(Room r) {
		return ((MarketInstance) r).autoE;
	}

	@Override
	public void autoEmploy(Room r, boolean b) {
		((MarketInstance) r).autoE = b;
	}

	@Override
	public RoomServiceAccess service() {
		return service;
	}
	
	
	public int buy(RaceResource res, int amount, int tx, int ty) {
		MarketInstance ins = getter.get(tx, ty);
		if (ins == null)
			return 0;;
		
		int am = amount;
		am = CLAMP.i(am, 0, ins.amount(res));
		
		if (am == 0)
			return 0;
		
		ins.consume(res, am, tx, ty);
		return am;
		
	}
	
	private final Bitmap1D ress = new Bitmap1D(RESOURCES.ALL().size(), false);
	private int ci = 0;
	
	public boolean uses(RaceResource rr) {
		if (ci == GAME.updateI())
			return ress.get(rr.res.index());
		ci = GAME.updateI();
		ress.clear();
		
		for (int ri = 0; ri < RACES.res().ALL.size(); ri++) {
			RaceResource r = RACES.res().ALL.get(ri);
			for (int i = 0; i < instancesSize(); i++) {
				MarketInstance ii = getInstance(i);
				if (ii.uses(r)) {
					ress.set(r.res.index(), true);
					break;
				}
			}
		}
		
		return ress.get(rr.res.index());
		
	}
	
	@Override
	public boolean registersEnvironment() {
		return true;
	}
	
}
