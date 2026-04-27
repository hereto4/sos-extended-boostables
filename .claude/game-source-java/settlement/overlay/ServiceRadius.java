package settlement.overlay;

import settlement.main.SETT;
import settlement.path.components.SComponent;
import settlement.path.components.SComponentChecker;
import settlement.path.components.SComponentEdge;
import settlement.room.main.RoomBlueprintIns;
import settlement.room.main.RoomInstance;
import settlement.room.service.module.RoomFinderHaser;
import snake2d.PathTile;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.Rec;
import util.GUTIL;
import util.colors.GCOLOR;
import util.rendering.RenderData;
import util.rendering.RenderData.RenderIterator;

final class ServiceRadius extends Addable{

	private final SComponentChecker check = new SComponentChecker(SETT.PATH().comps.zero);
	private RoomFinderHaser ser;
	private final Rec tiles = new Rec();
	ServiceRadius(){
		super(null, null, null,null, true, false);
		exclusive = true;
	}

	public void add(RoomFinderHaser ser) {
		super.add();
		this.ser = ser;
	}
	
	@Override
	public void initBelow(RenderData data) {
		
		check.init();
		GUTIL.flooder().init(this);
		
		
		int radius = ser.radius();
		
		if (ser instanceof RoomBlueprintIns<?>) {
			RoomBlueprintIns<?> b = ((RoomBlueprintIns<?>)(ser));
			for (RoomInstance ins : b.all()) {

				for (COORDINATE c : ins.body()) {
					if (ins.is(c) && b.service(c.x(), c.y()) != null) {
						for (DIR d : DIR.ORTHO)
							GUTIL.flooder().pushSloppy(c,d, 0);
					}
				}
			}
		}else {
			
			tiles.setDim(data.tBounds());
			tiles.incrW(radius*2+2);
			tiles.incrH(radius*2+2);
			tiles.centerIn(data.tBounds());
			
			for (COORDINATE c : tiles) {
				if (SETT.ROOMS().map.blueprint.get(c) == ser){
					for (DIR d : DIR.ORTHO)
						GUTIL.flooder().pushSloppy(c,d, 0);
				}
			}
		}
		
		
		
		while(GUTIL.flooder().hasMore()) {
			PathTile t = GUTIL.flooder().pollSmallest();
			if (t.getValue() > radius)
				continue;
			SComponent c = SETT.PATH().comps.zero.get(t);
			if (c != null) {
				check.isSetAndSet(c);
				SComponentEdge e = c.edgefirst();
				while(e != null) {
					GUTIL.flooder().pushSmaller(e.to().centreX(), e.to().centreY(), t.getValue()+e.distance(), t);
					e = e.next();
				}
			}

		}
		GUTIL.flooder().done();
		
	}
	
	@Override
	public void renderBelow(Renderer r, RenderIterator it) {
		
		double radius = ser.radius();
		
		COLOR c = COLOR.WHITE10;
		SComponent comp = SETT.PATH().comps.zero.get(it.tile());
		
		if (comp != null && check.is(comp)) {
			PathTile t = GUTIL.flooder().get(comp.centreX(), comp.centreY());
			if (t != null) {
				double v = t.getValue();
				v /= radius;
				v = 1.0-v;
				
				if (ser.finder().map.has(it.tx(), it.ty())) {
					if (ser.finder().map.fail(it.tx(), it.ty())) {
						c = ColorImp.TMP.interpolate(COLOR.WHITE25, GCOLOR.MAP().OVERLAY_BAD, v);
					}else {
						c = ColorImp.TMP.interpolate(COLOR.WHITE25, GCOLOR.MAP().OVERLAY_GOOD, v);
					}
					
				}else {
					c = ColorImp.TMP.interpolate(COLOR.WHITE25, COLOR.WHITE85, v);
				}
			}
			
		}
		
		renderUnder(c, r, it);
		
	}
	
	@Override
	public void finishBelow() {
		
	}
	
}
