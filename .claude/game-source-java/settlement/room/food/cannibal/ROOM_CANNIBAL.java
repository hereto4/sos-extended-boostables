package settlement.room.food.cannibal;

import java.io.IOException;
import java.util.Arrays;

import game.time.TIME;
import init.race.RACES;
import init.race.Race;
import init.resources.RBIT.RBITImp;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.resources.RES_AMOUNT;
import settlement.main.SETT;
import settlement.path.finders.SFinderRoomService;
import settlement.room.main.RoomBlueprintIns;
import settlement.room.main.category.RoomCategorySub;
import settlement.room.main.furnisher.Furnisher;
import settlement.room.main.util.RoomInitData;
import settlement.stats.STATS;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.LISTE;
import util.statistics.HistoryInt;
import view.sett.ui.room.UIRoomModule;

public class ROOM_CANNIBAL extends RoomBlueprintIns<CannibalInstance>{

	final Job job;
	final int[] produced = new int[RESOURCES.ALL().size()];
	private int year = -1;
	double cannibalism;
	private final HistoryInt cannHistory = new HistoryInt(STATS.DAYS_SAVED, TIME.days(), false);
	final Constructor constructor;
	
	private RESOURCE[] resources;
	
	public ROOM_CANNIBAL(RoomInitData init, RoomCategorySub cat) throws IOException {
		super(0, init, "_CANNIBAL", cat);
		
		constructor = new Constructor(this, init);
		job = new Job(this);

	}
	
	RESOURCE[] resources() {
		
		RBITImp m = new RBITImp();
		
		if (resources == null) {
			int am = 0;
			for (Race race : RACES.all()) {
				for (RES_AMOUNT r : race.resources()) {
					if (!m.has(r.resource())) {
						am++;
						m.or(r.resource());
					}
				}
			}
			RESOURCE[] res = new RESOURCE[am];
			m.clear();
			am = 0;
			for (Race race : RACES.all()) {
				for (RES_AMOUNT r : race.resources()) {
					if (!m.has(r.resource())) {
						res[am++] = r.resource();
						m.or(r.resource());
					}
				}
			}
			resources = res;
		}
		
		return resources;
	}
	
	@Override
	protected void update(double ds) {
		if (year != TIME.years().bitsSinceStart()) {
			Arrays.fill(produced, 0);
			year = TIME.years().bitsSinceStart();
		}
		
		double d = cannibalism;
		if (d < 1)
			d = 1;
		
		cannibalism -= d*ds/(TIME.years().bitSeconds()*2);
		cannibalism = CLAMP.d(cannibalism, 0, 1.5);
		
		cannHistory.setD(cannibalism());
		
	}

	@Override
	public SFinderRoomService service(int tx, int ty) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void saveP(FilePutter f){
		f.isE(produced);
		f.i(year);
		f.d(cannibalism);
		cannHistory.save(f);
	}
	
	@Override
	protected void loadP(FileGetter f) throws IOException{
		f.isE(produced);
		year = f.i();
		cannibalism = f.d();
		cannHistory.load(f);
	}
	
	@Override
	protected void clearP() {
		year = -1;
		Arrays.fill(produced, 0);
		cannibalism = 0;
		cannHistory.clear();
	}
	
	@Override
	public Furnisher constructor() {
		return constructor;
	}
	
	@Override
	public void appendView(LISTE<UIRoomModule> mm) {
		
	}
	
	public void reportCannibal() {
		cannibalism += 100.0/STATS.POP().POP.data().get(null);
	}
	
	public void reportCannibal2() {
		cannibalism += 50.0/STATS.POP().POP.data().get(null);
	}
	
	public double cannibalism() {
		return CLAMP.d(cannibalism, 0, 1);
	}
	
	public HistoryInt cannHistory() {
		return cannHistory;
	}
	
	public void setRace(int tx, int ty, Race race) {
		CannibalInstance ins = get(tx, ty);
		if (ins != null) {
			int d = SETT.ROOMS().data.get(tx, ty);
			d = Job.race.set(d, race.index());
			SETT.ROOMS().data.set(ins, tx, ty, d);
		}
			
	}
	

}
