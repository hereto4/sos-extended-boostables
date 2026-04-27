package world.overlay;

import static world.WORLD.IN_BOUNDS;

import init.constant.C;
import init.sprite.SPRITES;
import snake2d.PathTile;
import snake2d.PathUtilOnline.Flooder;
import snake2d.Renderer;
import snake2d.util.MATH;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.sets.Bitmap1D;
import snake2d.util.sets.LIST;
import util.GUTIL;
import util.rendering.RenderData;
import util.rendering.ShadowBatch;
import view.main.VIEW;
import world.WORLD;
import world.map.pathing.WPATHING;
import world.map.pathing.WRegFinder;
import world.map.pathing.WRegFinder.RegDist;
import world.map.pathing.WRegFinder.Treaty;
import world.map.pathing.WRegSel;
import world.map.regions.Region;
import world.map.regions.WREGIONS;

public final class ERegion{

	private Region hovered;
	private final COLOR cNone = new ColorImp(100, 100, 100);
	private final ColorImp col = new ColorImp();
	private double shade;
	
	private final WRegFinder fin = new WRegFinder();
	
	public void add(Region r) {
		if (r != null) {
			hovered = r;
		}
	}

	public void renderAbove(Renderer r, ShadowBatch s, RenderData data) {
		if (hovered == null)
			return;
		renderAbove(hovered, r, s, data);
		renderPath(hovered, r, s, data);
		hovered = null;
		WORLD.OVERLAY().things.render(r, s, data);
	}
	
	public void renderAbove(Region hovered, Renderer r, ShadowBatch s, RenderData data) {
		s.setHeightUI(6);
		s.setDistance2GroundUI(10);
		s.setHard();
		shade = VIEW.renderSecond();
		shade = MATH.mod(shade, 2);
		shade = MATH.distanceC(shade, 1, 2);
		if (hovered.realm() == null)
			col.set(cNone);
		else
			col.set(hovered.faction().banner().colorBG());
		col.shadeSelf(0.5 + shade);
		col.bind();
		for (COORDINATE c : hovered.info.bounds()) {
			if (hovered.is(c)) {
				int m = 0;
				for (DIR d : DIR.ORTHO) {
					if (hovered.is(c, d) || !IN_BOUNDS(c, d)) {
						m |= d.mask();
					}
				}
				if (m != 0x0F) {
					int x = data.transformGX(c.x()*C.TILE_SIZE);
					int y = data.transformGY(c.y()*C.TILE_SIZE);
					SPRITES.cons().BIG.dashed_hollow.render(r, m, x, y);
					SPRITES.cons().BIG.dashed_hollow.render(s, m, x, y);
				}
			}
		}
		s.setPrev();
		
	}
	
	private Bitmap1D cc = new Bitmap1D(WREGIONS.MAX, false);
	
	public void renderPath(Region reg, Renderer r, ShadowBatch s, RenderData data) {
		LIST<RegDist> regs = fin.all(reg, Treaty.REG_NEIGHS, WRegSel.DUMMY());
		
		cc.clear();
		for (RegDist d : regs) {
			cc.set(d.reg.index(), true);
		}
		cc.set(reg.index(), true);
		Flooder f = GUTIL.flooder();
		f.init(this);
		f.pushSloppy(reg.cx(), reg.cy(), 0);
		while(f.hasMore()) {
			
			PathTile t = f.pollSmallest();
			Region rr = WORLD.PATH().regMap.get(t);
			
			if (rr != null && !cc.get(rr.index()))
				continue;
			if (rr != reg && rr != null && t.isSameAs(rr.cx(), rr.cy())) {
				cc.set(rr.index(), false);
				render(r, s, data, t);
				continue;
			}
			
			for (DIR d : DIR.ALL) {
				if (WORLD.PATH().map.can(t, d) && (rr == null || rr == reg || rr == WORLD.PATH().regMap.get(t, d))) {
					GUTIL.flooder().pushSmaller(t, d, t.getValue()+ WPATHING.cost(t.x(), t.y(), d)*d.tileDistance(), t);
					
				}
			}
			
		}
		
		GUTIL.flooder().done();
		COLOR.unbind();

	}
	
	private void render(Renderer r, ShadowBatch s, RenderData data, PathTile t) {
		Region reg = WORLD.REGIONS().map.get(t);
		
		
		
		if (reg != null)
			WORLD.OVERLAY().hoverBox(reg);
		if (t.getParent() == null)
			return;
		PathTile prev = t;
		t = t.getParent();
		while(t.getParent() != null) {

			DIR d = DIR.get(t, prev);
			int dd = C.TILE_SIZE/2;
			int x = data.transformGX(t.x()*C.TILE_SIZE);
			int y = data.transformGY(t.y()*C.TILE_SIZE);
			
			
			for (int i = 0; i < 2; i++) {
				SPRITES.cons().ICO.arrows2.get(d.id()).render(r, x+d.x()*dd*i, y+d.y()*dd*i);
				SPRITES.cons().ICO.arrows2.get(d.id()).render(s, x+d.x()*dd*i, y+d.y()*dd*i);
			}
			
//			SPRITES.cons().ICO.arrows2.get(d.id()).render(r, x, y);
//			SPRITES.cons().ICO.arrows2.get(d.id()).render(s, x, y);
			
			prev = t;
			t = t.getParent();
		}
	}

}
