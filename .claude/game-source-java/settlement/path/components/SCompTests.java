package settlement.path.components;

import init.sprite.SPRITES;
import settlement.main.ON_TOP_RENDERABLE;
import settlement.path.components.finder.SCompFinder.SCompPath;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import snake2d.util.misc.ACTION;
import util.rendering.RenderData;
import util.rendering.RenderData.RenderIterator;
import util.rendering.ShadowBatch;
import view.main.VIEW;
import view.sett.IDebugPanelSett;
import view.tool.PlacableSimpleTile;

class SCompTests {

	private final SCOMPONENTS comps;
	
	SCompTests(SCOMPONENTS comps){
		this.comps = comps;
		IDebugPanelSett.add("Path Comp", new Placer());
	}
	
	private class Placer implements ACTION{
		
		int sx,sy;
		SCompPath res = null;
		
		ON_TOP_RENDERABLE ren = new ON_TOP_RENDERABLE() {
			
			@Override
			public void render(Renderer r, ShadowBatch shadowBatch, RenderData data, double ds) {
				if (res == null)
					return;
				RenderIterator it = data.onScreenTiles();
				while(it.has()) {
					if (res.is(it.tile())) {
						if (it.tx() == sx && it.ty() == sy)
							COLOR.GREEN100.bind();
						SPRITES.cons().BIG.dots.render(r, 0, it.x(), it.y());
						COLOR.unbind();
					}
					it.next();
				}
				
			}
		};
		
		PlacableSimpleTile p1 = new PlacableSimpleTile("set start") {
			
			@Override
			public void place(int tx, int ty) {
				sx= tx;
				sy = ty;
				VIEW.s().tools.place(p2);
				
			}
			
			@Override
			public CharSequence isPlacable(int tx, int ty) {
				if (comps.zero.get(tx, ty) == null)
					return E;
				return null;
			}
		};
		
		PlacableSimpleTile p2 = new PlacableSimpleTile("set dest") {
			
			@Override
			public void place(int tx, int ty) {
				res = comps.pather.findDest(sx, sy, tx, ty);
				ren.add();
			}
			
			@Override
			public CharSequence isPlacable(int tx, int ty) {
				if (comps.zero.get(tx, ty) == null)
					return E;
				return null;
			}
			
			
		};

		@Override
		public void exe() {
			res = null;
			VIEW.s().tools.place(p1);
		}
		
	}
	
}
