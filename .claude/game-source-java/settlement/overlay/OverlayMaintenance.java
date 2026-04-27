package settlement.overlay;

import init.constant.C;
import init.resources.RESOURCE;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import settlement.main.SETT;
import settlement.path.components.SComponent;
import settlement.path.components.SComponentChecker;
import settlement.path.components.SComponentEdge;
import settlement.room.infra.janitor.ROOM_JANITOR;
import settlement.room.main.RoomInstance;
import snake2d.PathTile;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.sets.Bitmap1D;
import util.GUTIL;
import util.colors.GCOLOR;
import util.rendering.RenderData;
import util.rendering.RenderData.RenderIterator;
import util.text.D;
import util.text.Dic;

public final class OverlayMaintenance extends Addable{

	private final SComponentChecker check = new SComponentChecker(SETT.PATH().comps.zero);
	private Bitmap1D checkS = new Bitmap1D(1024, false);
	private RoomInstance special;
	private static CharSequence ¤¤desc = "Highlights which tiles need maintenance.";
	
	static {
		D.ts(OverlayMaintenance.class);
	}
	
	OverlayMaintenance(){
		super(UI.icons().s.degrade, "MAINTENANCE", Dic.¤¤Maintenance, ¤¤desc, true, true);
		exclusive = true;
		if (false) {
			//something is wrong here. Radius does not show.
		}
	}

	public void add(RoomInstance ins) {
		super.add();
		this.special = ins;
	}
	
	@Override
	public void initBelow(RenderData data) {
		
		check.init();
		GUTIL.flooder().init(this);

		int radius = ROOM_JANITOR.radius;
		
		for (RoomInstance ins : SETT.ROOMS().JANITOR.all()) {

			if (ins.active()) {
				GUTIL.flooder().pushSloppy(ins.mX(), ins.mY(), 0);
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
					GUTIL.flooder().setValue2(e.to().centreX(), e.to().centreY(), 0);
					e = e.next();
				}
			}

		}
		GUTIL.flooder().done();
		
		if (special == null)
			return;
		
		if (checkS.size() < SETT.PATH().comps.zero.componentsMax())
			checkS = new Bitmap1D(SETT.PATH().comps.zero.componentsMax(), false);
		checkS.clear();
		GUTIL.flooder().init(this);
		GUTIL.flooder().pushSloppy(special.mX(), special.mY(), 0);
		
		while(GUTIL.flooder().hasMore()) {
			PathTile t = GUTIL.flooder().pollSmallest();
			if (t.getValue() > radius)
				continue;
			SComponent c = SETT.PATH().comps.zero.get(t);
			if (c != null) {
				checkS.set(c.index(), true);
				SComponentEdge e = c.edgefirst();
				while(e != null) {
					GUTIL.flooder().pushSmaller(e.to().centreX(), e.to().centreY(), t.getValue()+e.distance(), t);
					GUTIL.flooder().setValue2(e.to().centreX(), e.to().centreY(), 0);
					e = e.next();
				}
			}

		}
		GUTIL.flooder().done();
		
	}
	
	@Override
	public void renderBelow(Renderer r, RenderIterator it) {
		COLOR c = COLOR.WHITE10;
		
		SComponent comp = SETT.PATH().comps.zero.get(it.tile());
		if (comp == null) {
			
		}else {
			if (SETT.MAINTENANCE().disabled.is(it.tile())) {
				 c = GCOLOR.MAP().SOSO;
			}else if (!SETT.MAINTENANCE().needs.is(it.tx(), it.ty())) {
				c = COLOR.WHITE50;
			}else if (SETT.MAINTENANCE().degrade.get(it.tx(), it.ty()) > 0) {
				c = ColorImp.TMP.interpolate(GCOLOR.MAP().OVERLAY_GOOD, GCOLOR.MAP().OVERLAY_BAD, SETT.MAINTENANCE().degrade.get(it.tx(), it.ty()));
			}else {
				c = GCOLOR.MAP().OVERLAY_GOOD;
			}
			
			ColorImp.TMP.set(c);
			if (checkS != null && checkS.get(comp.index()))
				ColorImp.TMP.shadeSelf(1.25);
			else if (!check.is(comp))
				ColorImp.TMP.shadeSelf(0.75);
			c = ColorImp.TMP;
				
			
		}
		
		
		
		
		renderUnder(c, r, it);
		
	}
	
	@Override
	public boolean render(Renderer r, RenderIterator it) {
		if (SETT.MAINTENANCE().isser.is(it.tile())) {
			COLOR c = GCOLOR.MAP().BAD;
			if (SETT.MAINTENANCE().disabled.is(it.tile())) {
				c = COLOR.WHITE50;
			}else if (SETT.MAINTENANCE().reserved.is(it.tx(), it.ty()))
				c = GCOLOR.MAP().BEST_DARK;
			else if (SETT.MAINTENANCE().degrade.get(it.tx(), it.ty()) > 0) {
				c = GCOLOR.MAP().BAD;
			}else {
				c = GCOLOR.MAP().SOSO;
			}

			c.bind();
			SPRITES.cons().BIG.outline.render(r, 0, it.x(), it.y());
			COLOR.unbind();
			RESOURCE res = SETT.MAINTENANCE().resource.get(it.tx(), it.ty());
			if (res != null) {
				res.icon().renderScaled(r, it.x()+C.TILE_SIZEH/4, it.y()+C.TILE_SIZEH/4, 2);
			}
			return true;
		}
		return false;
	}
	
	@Override
	public void finishBelow() {
		special = null;
	}
	
}
