package settlement.room.law.stocks;

import settlement.room.main.ROOMA;
import settlement.room.main.ROOMS;
import settlement.room.main.RoomBlueprint;
import settlement.room.main.RoomSingleton;
import snake2d.util.datatypes.COORDINATE;

final class Instance extends RoomSingleton {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	Instance(ROOMS m, RoomBlueprint p) {
		super(m, p);

	}

	@Override
	public ROOM_STOCKS blueprintI() {
		return (ROOM_STOCKS) blueprint();
	}

	@Override
	protected void addAction(ROOMA ins) {
		for (COORDINATE c : ins.body()) {
			if (ins.is(c) && blueprintI().constructor().service(c.x(), c.y())) {
				blueprintI().add(c.x(), c.y());
			}
		}
	}

	@Override
	protected void removeAction(ROOMA ins) {
		for (COORDINATE c : ins.body()) {
			if (ins.is(c) && blueprintI().constructor().service(c.x(), c.y())) {
				blueprintI().remove(c.x(), c.y());
			}
		}
		super.removeAction(ins);
	}

	
	


}