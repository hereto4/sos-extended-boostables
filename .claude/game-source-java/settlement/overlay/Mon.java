package settlement.overlay;

import settlement.main.SETT;
import settlement.room.infra.monument.ROOM_MONUMENT;
import settlement.room.main.RoomBlueprint;
import settlement.room.main.furnisher.FurnisherItem;
import snake2d.Renderer;
import snake2d.util.misc.CLAMP;
import util.rendering.RenderData;
import util.rendering.RenderData.RenderIterator;

class Mon extends Addable{

	public ROOM_MONUMENT m;
	
	public FurnisherItem it;
	public int radius;
	public int x1, y1;
	
	Mon() {
		super(null, null, null, null, true, false);
		
	}
	
	void set(ROOM_MONUMENT m, FurnisherItem it, int x1, int y1, int radius) {
		this.m = m;
		this.it = it;
		this.radius = radius;
		this.x1 = x1;
		this.y1 = y1;
		add();
	}
	
	void set(ROOM_MONUMENT m) {
		this.m = m;
		this.it = null;
	}
	
	@Override
	public void initBelow(RenderData data) {
		if (it != null)
			SETT.ENV().map.MONUMENT.addExtra(m, it, x1, y1);
		super.initBelow(data);
	}
	
	@Override
	public void renderBelow(Renderer r, RenderIterator it) {
		int d = 0;
		RoomBlueprint b = SETT.ROOMS().map.blueprint.get(it.tile());
		if (b == null || b.registersEnvironment()) {
			d = m.mapData.get(it.tx(), it.ty());
			if (this.it != null)
				d = CLAMP.i(d+SETT.ENV().map.MONUMENT.extra(x1, y1, it.tx(), it.ty()), 0, m.maxEnv());
		}
		
		renderUnder(d/(double)m.maxEnv(), r, it);
		
		
	}

	@Override
	public void finishBelow() {
		it = null;
		super.finishBelow();
	}
	

	
}
