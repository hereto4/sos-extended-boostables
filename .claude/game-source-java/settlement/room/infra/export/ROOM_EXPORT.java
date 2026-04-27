package settlement.room.infra.export;

import java.io.IOException;
import java.util.Arrays;

import init.resources.RESOURCE;
import init.resources.RESOURCES;
import settlement.main.SETT;
import settlement.path.finders.SFinderRoomService;
import settlement.room.main.Room;
import settlement.room.main.RoomBlueprintIns;
import settlement.room.main.category.RoomCategorySub;
import settlement.room.main.furnisher.Furnisher;
import settlement.room.main.job.ROOM_EMPLOY_AUTO;
import settlement.room.main.job.ROOM_RADIUS.ROOM_RADIUSE;
import settlement.room.main.util.RoomInitData;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sets.LISTE;
import view.sett.ui.room.UIRoomModule;

public final class ROOM_EXPORT extends RoomBlueprintIns<ExportInstance> implements ROOM_RADIUSE, ROOM_EMPLOY_AUTO{

	public final ExportTally tally = new ExportTally();
	public final ExportFetcher FETCHER = new ExportFetcher(this, tally);
	final Constructor constructor;
	
	private final Crate crate = new Crate(this);
	
	private final double[] prioFetchLims = new double[RESOURCES.ALL().size()];
	
	public ROOM_EXPORT(RoomInitData  data, RoomCategorySub cat) throws IOException {
		super(0, data, "_EXPORT", cat);
		
		constructor = new Constructor(this, data);
		Arrays.fill(prioFetchLims, 0.25);
		
		if (false) {
			//have a checkbox to not export over the trade cap.
		}
	}

	@Override
	protected void update(double ds) {
		if (ds > 0)
			FETCHER.update(ds);
//		up -= ds;
//		if (up <= 0) {
//			for (RESOURCE r : RESOURCE.ALL()) {
//				if (tally.toBeFetched.get(r) > 0) {
//					fetch(r);
//				}
//			}
//		}
//		
		
	}
	
	
	public Crate crate(int tx, int ty) {
		return crate.get(tx, ty);
	}
	
	@Override
	public Furnisher constructor() {
		return constructor;
	}

	@Override
	public SFinderRoomService service(int tx, int ty) {
		return null;
	}
	
	@Override
	protected void saveP(FilePutter saveFile){
		tally.saver.save(saveFile);
		saveFile.dsE(prioFetchLims);
	}
	
	@Override
	protected void loadP(FileGetter saveFile) throws IOException{
		tally.saver.load(saveFile);
		saveFile.dsE(prioFetchLims);
	}
	
	@Override
	protected void clearP() {
		tally.saver.clear();
		Arrays.fill(prioFetchLims, 0.25);
	}
	
	
	
	@Override
	public void appendView(LISTE<UIRoomModule> mm) {
		mm.add(new Gui(this).make());
	}

	@Override
	public boolean autoEmploy(Room r) {
		return ((ExportInstance) r).auto;
	}

	@Override
	public void autoEmploy(Room r, boolean b) {
		if (r instanceof ExportInstance) {
			((ExportInstance) r).auto = b;
		}
	}


	
	@Override
	public ROOM_RADIUS_INSTANCE radiusInstance(Room t) {
		return (ExportInstance) t;
	}
	
	public double prioFetch(RESOURCE res) {
		return prioFetchLims[res.index()];
	}
	
	public void prioFetch(RESOURCE res, double v) {
		prioFetchLims[res.index()] = v;
	}
	
	public int prioFetchAmount(RESOURCE res) {
		int cap = SETT.ROOMS().STOCKPILE.tally().space.total(res);
		return (int) (cap*prioFetch(res));
	}
	
	public int prioFetchAvailable(RESOURCE res) {
		return SETT.ROOMS().STOCKPILE.tally().amountReservable.get(res)-prioFetchAmount(res);
	}

}
