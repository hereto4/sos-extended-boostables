package settlement.room.law.slaver;

import java.io.IOException;

import settlement.misc.util.FSERVICE;
import settlement.path.finders.SFinderRoomService;
import settlement.room.law.PUNISHMENT_SERVICE;
import settlement.room.main.Room;
import settlement.room.main.RoomBlueprintIns;
import settlement.room.main.category.RoomCategorySub;
import settlement.room.main.furnisher.Furnisher;
import settlement.room.main.job.ROOM_EMPLOY_AUTO;
import settlement.room.main.util.RoomInitData;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.rnd.RND;
import snake2d.util.sets.LISTE;
import view.sett.ui.room.UIRoomModule;

public final class ROOM_SLAVER extends RoomBlueprintIns<ExecutionInstance> implements ROOM_EMPLOY_AUTO, PUNISHMENT_SERVICE{

	public SFinderRoomService finder = new SFinderRoomService(this.info.name) {
		
		@Override
		public FSERVICE get(int tx, int ty) {
			// TODO Auto-generated method stub
			return null;
		}
	};
	final Constructor constructor;
	private int executions;
	private int total;
	
	public ROOM_SLAVER(RoomInitData init, RoomCategorySub block) throws IOException {
		super(0, init, "_SLAVER", block);
		
		constructor = new Constructor(this, init);

	}
	
	@Override
	protected void update(double ds) {
		// TODO Auto-generated method stub
		
	}
	

	@Override
	public Furnisher constructor() {
		return constructor;
	}

	@Override
	public int punishUsed() {
		return executions;
	}
	
	@Override
	public int punishTotal() {
		return total;
	}


	void incPrisoners(int current, int total){
		this.executions += current;
		this.total += total;
	}


	@Override
	protected void saveP(FilePutter f){
		f.i(executions);
		f.i(total);
	}
	
	@Override
	protected void loadP(FileGetter f) throws IOException{
		executions = f.i();
		total = f.i();
	}
	
	@Override
	protected void clearP() {
		executions = 0;
		total = 0;
	}

	@Override
	public SFinderRoomService service(int tx, int ty) {
		return finder;
	}
	
	@Override
	public void appendView(LISTE<UIRoomModule> mm) {
		mm.add(new Gui(this).make());
	}

	@Override
	public boolean autoEmploy(Room r) {
		return ((ExecutionInstance)r).autoEmploy;
	}

	@Override
	public void autoEmploy(Room r, boolean b) {
		((ExecutionInstance)r).autoEmploy = b;
	}
	
	public SlaverStation exectuionReserve() {
		if (executions == total)
			return null;
		int i = RND.rInt(instancesSize());
		for (int k = 0; k < instancesSize(); k++) {
			ExecutionInstance ins = getInstance((k+i)%instancesSize());
			if (ins.active() && ins.executions() < ins.total()) {
				return ins.reserveSpot();
			}
		}
		throw new RuntimeException();
	}
	
	public SlaverStation executionSpot(COORDINATE c) {
		if (is(c)) {
			return SlaverStation.init(c.x(), c.y());
		}
		return null;
	}
	
	public SlaverStation workReserve(Room r) {
		ExecutionInstance ins = (ExecutionInstance) r;
		return ins.work();
	}
	

}
