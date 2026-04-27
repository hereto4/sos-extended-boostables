package world.overlay;

import init.sprite.SPRITES;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.Rec;
import snake2d.util.sets.ArrayList;
import util.rendering.RenderData;
import util.rendering.ShadowBatch;

class OverlayThings {

	private final ArrayList<Thing> objects = new ArrayList<>(256);
	private int ai;
	
	OverlayThings(){
		for (int i = 0; i < objects.max(); i++) {
			objects.add(new Thing());
		}
	}
	
	void add(int x1, int y1, int w, int h, COLOR color, boolean thick) {
		Thing t = objects.get(ai);
		t.rec.moveX1Y1(x1, y1).setDim(w, h);
		t.color = color;
		t.thick = thick;
		ai++;
	}
	
	void render(Renderer r, ShadowBatch s, RenderData data) {
		
		s.setDistance2GroundUI(8);
		for (int i = 0; i < ai; i++) {
			Thing t = objects.get(i);
			t.color.bind();
			Rec e = t.rec;
			if (t.thick) {
				SPRITES.cons().BIG.outline.renderBox(r, e.x1() - data.offX1(), e.y1() - data.offY1(), e.width(), e.height());
				SPRITES.cons().BIG.outline.renderBox(s, e.x1() - data.offX1(), e.y1() - data.offY1(), e.width(), e.height());
			}else {
				SPRITES.cons().BIG.outline.renderBox(r, e.x1() - data.offX1(), e.y1() - data.offY1(), e.width(), e.height());
				SPRITES.cons().BIG.outline.renderBox(s, e.x1() - data.offX1(), e.y1() - data.offY1(), e.width(), e.height());
			}
		}
		
		ai = 0;
		
		
	}
	
	void clear() {
		ai = 0;
	}
	
	private static class Thing {
		
		final Rec rec = new Rec();
		COLOR color;
		boolean thick;
		
	}
}
