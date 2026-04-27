package settlement.overlay;

import settlement.main.SETT;
import settlement.path.components.SComponent;
import settlement.path.components.SComponentChecker;
import settlement.path.components.SComponentEdge;
import settlement.room.main.RoomInstance;
import snake2d.PathTile;
import snake2d.Renderer;
import snake2d.util.datatypes.COORDINATE;
import util.GUTIL;
import util.rendering.RenderData;
import util.rendering.RenderData.RenderIterator;

final class RoomRadius extends Addable{

	private RoomInstance ins;
	private int radius;
	private final SComponentChecker comps;
	
	RoomRadius(SComponentChecker comps){
		super(null, null, null,null, true, false);
		exclusive = true;
		this.comps = comps;
	}
	
	public void add(RoomInstance ins, int radius) {
		
		this.ins = ins;
		this.radius = radius;
		super.add();
	}
	
	@Override
	public void initBelow(RenderData data) {
		GUTIL.flooder().init(this);
		
		for (COORDINATE c : ins.body()) {
			if (ins.is(c)) {
				//double dist = COORDINATE.tileDistance(c, ins.body().cX(), ins.body().cY());
				GUTIL.flooder().pushSloppy(c, 0);
			}
		}
		
		
		comps.init();
		while(GUTIL.flooder().hasMore()) {
			PathTile t = GUTIL.flooder().pollSmallest();
			if (t.getValue() > radius)
				continue;
			SComponent c = SETT.PATH().comps.zero.get(t);
			if (c == null)
				continue;
			comps.isSetAndSet(c);
			SComponentEdge e = c.edgefirst();
			
			while(e != null) {
				double dist = e.distance();
				GUTIL.flooder().pushSmaller(e.to().centreX(), e.to().centreY(), t.getValue()+dist);
				e = e.next();
			}
			
		}
		GUTIL.flooder().done();
	}

	@Override
	public boolean render(Renderer r, RenderIterator it) {

		return false;
	}
	
	@Override
	public void renderBelow(Renderer r, RenderIterator it) {
		SComponent c = SETT.PATH().comps.zero.get(it.tile());
		if (c != null && comps.isSet(c.index()))
			renderUnder(1, r, it, false);
		else
			renderUnder(0, r, it, false);
	}
	
	@Override
	public void finishBelow() {
		
	}
	
}
