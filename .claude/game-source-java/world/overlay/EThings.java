package world.overlay;

import init.sprite.SPRITES;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.datatypes.Rec;
import snake2d.util.sets.ArrayList;
import util.colors.GCOLOR;
import util.rendering.RenderData;
import util.rendering.ShadowBatch;
import world.entity.WEntity;

public final class EThings  {

	private final ArrayList<Thing> objects = new ArrayList<>(256);
	private int ai;
	
	EThings(){
		for (int i = 0; i < objects.max(); i++) {
			objects.add(new Thing());
		}
	}
	
	public void add(int x1, int y1, int w, int h, COLOR color, boolean thick) {
		if (ai >= objects.size())
			return;
		Thing t = objects.get(ai);
		t.rec.moveX1Y1(x1, y1).setDim(w, h);
		t.color = color;
		t.thick = thick;
		ai++;
	}
	
	public void hover(RECTANGLE body, COLOR color, boolean thick, int margin) {
		add(body.x1()-margin, body.y1()-margin, body.width()+margin*2, body.height()+margin*2, color, thick);
	}
	
	public void hover(int x1, int y1, int w, int h, COLOR color, boolean thick) {
		add(x1, y1, w, h, color, thick);
	}
	
	public void hover(WEntity e) {
		hover(e.body(), GCOLOR.MAP().get(e.faction()), true, 6);
	}
	
	public void render(Renderer r, ShadowBatch s, RenderData data) {
		
		s.setDistance2GroundUI(8);
		s.setHeightUI(2);
		s.setHard();
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
		s.setPrev();
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
