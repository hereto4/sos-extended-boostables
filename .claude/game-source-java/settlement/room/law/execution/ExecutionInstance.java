package settlement.room.law.execution;

import settlement.misc.util.FSERVICE;
import settlement.room.main.RoomInstance;
import settlement.room.main.TmpArea;
import settlement.room.main.util.RoomInit;
import snake2d.Renderer;
import snake2d.util.datatypes.COORDINATE;
import util.rendering.RenderData;
import util.rendering.ShadowBatch;

final class ExecutionInstance extends RoomInstance {

	private static final long serialVersionUID = 1L;
	boolean autoEmploy = false;
	private short executions;
	private short workCurrent;
	private final short total;
	private final short[] cellsXY;
	private short cellI = 0;
	private short wI = 0;
	byte lastExecution = 0;
	
	protected ExecutionInstance(ROOM_EXECTUTION b, TmpArea area, RoomInit init) {
		super(b, area, init);
		
		int spots = 0;
		for (COORDINATE c : body()) {
			if (!is(c))
				continue;
			if (ExecutionStation.init(c.x(), c.y()) != null) {
				spots ++;
			}
		}
		
		cellsXY = new short[spots*2];
		total = (short) spots;
		executions = 0;
		spots = 0;
		for (COORDINATE c : body()) {
			if (is(c)) {
				if (ExecutionStation.init(c.x(), c.y()) != null) {
					cellsXY[spots++] = (short) c.x();
					cellsXY[spots++] = (short) c.y();
				}
			}
		}
		
		loadFix();
		
		employees().maxSet(total);
		employees().neededSet((int)Math.ceil(total/4.0));
		activate();
		
	}
	
	@Override
	protected void loadFix() {
		super.loadFix();
	}
	
	public int total() {
		return total;
	}
	
	public int executions() {
		return executions;
	}
	
	void inc(int executions, int workCurrent) {
		this.executions += executions;
		this.workCurrent += workCurrent;
		if (active()) {
			blueprintI().incPrisoners(executions, 0);
		}
	}
	
	
	@Override
	protected boolean render(Renderer r, ShadowBatch shadowBatch, RenderData.RenderIterator it) {
		it.lit();
		return super.render(r, shadowBatch, it);
	}

	@Override
	protected void activateAction() {
		blueprintI().incPrisoners(executions, total);
		for (COORDINATE c : body()) {
			if (is(c)) {
				
				FSERVICE s = ExecutionStation.service(c.x(), c.y());
				while(s != null && s.findableReservedIs())
					s.findableReserveCancel();
			}
		}
		
	}

	@Override
	protected void deactivateAction() {
		blueprintI().incPrisoners(-executions, -total);
		for (COORDINATE c : body()) {
			if (is(c)) {
				FSERVICE s = ExecutionStation.service(c.x(), c.y());
				while(s != null && s.findableReservedCanBe()) {
					s.findableReserve();
					
				}
			}
		}
	}

	@Override
	protected void updateAction(double updateInterval, boolean day) {

	}

	
	@Override
	protected void dispose() {

	}

	@Override
	public ROOM_EXECTUTION blueprintI() {
		return (ROOM_EXECTUTION) blueprint();
	}
	
	ExecutionStation reserveSpot() {
		if (executions == total)
			throw new RuntimeException();
		if (!active())
			throw new RuntimeException();
		for (int i = 0; i < cellsXY.length; i+=2) {
			cellI += 2;
			if (cellI >= cellsXY.length)
				cellI = 0;
			int tx = cellsXY[cellI];
			int ty = cellsXY[cellI+1];
			ExecutionStation s = ExecutionStation.init(tx, ty);
			if (s.clientReseveredCanBe()) {
				s.clientReserve();
				return s;
			}
		}
		throw new RuntimeException();
	}
	
	ExecutionStation work() {
		if (workCurrent == 0)
			return null;
		for (int i = 0; i < cellsXY.length; i+=2) {
			wI += 2;
			if (wI >= cellsXY.length)
				wI = 0;
			int tx = cellsXY[wI];
			int ty = cellsXY[wI+1];
			ExecutionStation s = ExecutionStation.init(tx, ty);
			if (s.workReservedCanBe()) {
				s.workReserve();
				return s;
			}
		}
		throw new RuntimeException();
	}
	

}
