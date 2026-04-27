package settlement.room.law.slaver;

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
	
	protected ExecutionInstance(ROOM_SLAVER b, TmpArea area, RoomInit init) {
		super(b, area, init);
		
		int spots = 0;
		
		for (COORDINATE c : body()) {
			if (!is(c))
				continue;
			if (SlaverStation.init(c.x(), c.y()) != null) {
				spots ++;
			}
		}
		
		cellsXY = new short[spots*2];
		total = (short) spots;
		executions = 0;
		spots = 0;
		for (COORDINATE c : body()) {
			if (is(c)) {
				if (SlaverStation.init(c.x(), c.y()) != null) {
					cellsXY[spots++] = (short) c.x();
					cellsXY[spots++] = (short) c.y();
				}
			}
		}
		
		employees().maxSet(total);
		employees().neededSet((int)Math.ceil(total/4.0));
		activate();
		
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
	}

	@Override
	protected void deactivateAction() {
		blueprintI().incPrisoners(-executions, -total);
	}

	
	@Override
	protected void dispose() {
		
		
	}

	@Override
	public ROOM_SLAVER blueprintI() {
		return (ROOM_SLAVER) blueprint();
	}
	
	SlaverStation reserveSpot() {
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
			SlaverStation s = SlaverStation.init(tx, ty);
			if (s.clientReseveredCanBe()) {
				s.clientReserve();
				return s;
			}
		}
		throw new RuntimeException();
	}
	
	SlaverStation work() {
		if (workCurrent == 0)
			return null;
		for (int i = 0; i < cellsXY.length; i+=2) {
			wI += 2;
			if (wI >= cellsXY.length)
				wI = 0;
			int tx = cellsXY[wI];
			int ty = cellsXY[wI+1];
			SlaverStation s = SlaverStation.init(tx, ty);
			if (s.workReservedCanBe()) {
				s.workReserve();
				return s;
			}
		}
		throw new RuntimeException();
	}

}
