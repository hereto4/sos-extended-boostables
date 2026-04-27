package settlement.room.law.court;

import settlement.misc.util.FSERVICE;
import settlement.room.main.RoomInstance;
import settlement.room.main.TmpArea;
import settlement.room.main.util.RoomInit;
import snake2d.Renderer;
import snake2d.util.datatypes.COORDINATE;
import util.rendering.RenderData;
import util.rendering.ShadowBatch;

final class CourtInstance extends RoomInstance {

	private static final long serialVersionUID = 1L;
	boolean autoEmploy = false;
	private short executions;
	private short workCurrent;
	private final short total;
	private final short[] cellsXY;
	private short cellI = 0;
	private short wI = 0;
	byte lastExecution = 0;
	
	protected CourtInstance(ROOM_COURT b, TmpArea area, RoomInit init) {
		super(b, area, init);
		
		int spots = 0;
		for (COORDINATE c : body()) {
			if (!is(c))
				continue;
			if (CourtStation.isJudge(c)) {
				spots ++;
			}
		}
		
		cellsXY = new short[spots*2];
		total = (short) spots;
		executions = 0;
		spots = 0;
		for (COORDINATE c : body()) {
			if (is(c)) {
				if (CourtStation.isJudge(c)) {
					cellsXY[spots++] = (short) c.x();
					cellsXY[spots++] = (short) c.y();
				}
			}
		}
		employees().maxSet(total);
		employees().neededSet(total);
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
				Service s = Service.init(c.x(), c.y());
				if (s != null) {
					s.activate();
				}
			}
		}
	}

	@Override
	protected void deactivateAction() {
		blueprintI().incPrisoners(-executions, -total);
		for (COORDINATE c : body()) {
			if (is(c)) {
				Service s = Service.init(c.x(), c.y());
				if (s != null) {
					s.deactivate();
				}
			}
		}
	}

	@Override
	protected void updateAction(double updateInterval, boolean day) {

	}

	
	@Override
	protected void dispose() {
		
		for (COORDINATE c : body()) {
			if (!is(c))
				continue;
			FSERVICE s = Service.init(c.x(), c.y());
			if (s != null && s.findableReservedCanBe())
				s.findableReserve();
		}
		
	}

	@Override
	public ROOM_COURT blueprintI() {
		return (ROOM_COURT) blueprint();
	}
	
	CourtStation reserveSpot() {
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
			CourtStation s = CourtStation.init(tx, ty);
			if (s.criminalReseveredCanBe()) {
				s.criminalReserve();
				return s;
			}
		}
		throw new RuntimeException();
	}
	
	CourtStation work() {
		if (workCurrent == 0)
			return null;
		for (int i = 0; i < cellsXY.length; i+=2) {
			wI += 2;
			if (wI >= cellsXY.length)
				wI = 0;
			int tx = cellsXY[wI];
			int ty = cellsXY[wI+1];
			CourtStation s = CourtStation.init(tx, ty);
			if (s.workReservedCanBe()) {
				s.workReserve();
				return s;
			}
		}
		throw new RuntimeException();
	}

}
