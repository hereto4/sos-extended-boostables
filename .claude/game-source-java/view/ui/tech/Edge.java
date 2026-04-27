package view.ui.tech;

import init.sprite.SPRITES;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import init.tech.TechTree;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.renderable.RENDEROBJ.RenderImp;
import snake2d.util.sprite.SPRITE;
import util.gui.misc.GText;
import view.main.VIEW;

class Edge extends RenderImp {

	SPRITE sprite;
	
	int m = 0;
	int a = 0;
	int hm = 0;
	public int hoverI;
	
	Edge(int w, int h){
		body.setDim(w, h);
	}
	
	Edge(TechTree tree, int mw, int wi){
		super(wi, NodeCreator.DIM);
		final GText tt = new GText(UI.FONT().H2, tree.name).lablify();
		final int x1 = ((mw-1)*(Node.WIDTH+NodeCreator.DIM)+Node.WIDTH)/2 - tt.width()/2;
		final int w = (mw-1)*(Node.WIDTH+NodeCreator.DIM) + Node.WIDTH;
		final SPRITE bo = UI.decor().borderTop(w);
		
		sprite = new SPRITE.Imp() {
			
			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				tt.render(r, X1- x1-tt.width(), Y2-tt.height()-bo.height()-4-8);
				bo.render(r, X1-w, X1, Y2-bo.height()-8, Y2-8);
			}
		};

	}
	
	@Override
	public void render(SPRITE_RENDERER r, float ds) {
		
		render(r, COLOR.WHITE10, 3, m);
		render(r, COLOR.WHITE15, 2, m);
		
		if (hoverI == VIEW.renI) {
			render(r, COLOR.WHITE65, 3, hm);
			render(r, COLOR.WHITE85, 2, hm);
		}else {
		}
		
		for (DIR d : DIR.ORTHO) {
			if ((d.mask()& a) != 0) {
				if (hoverI == VIEW.renI && (d.mask()& hm) != 0)
					COLOR.WHITE85.bind();
				else
					COLOR.WHITE50.bind();
				int cx = body.cX()+d.x()*(body.width()-Icon.S-12)/2;
				int cy = body.cY()+d.y()*(body.height()-Icon.S-12)/2;
				SPRITES.icons().s.chevron(d).renderC(r, cx, cy);
			}
			
		}
		COLOR.unbind();
		
		if (sprite != null)
			sprite.render(r, body);
		//COLOR.GREEN100.renderFrame(r, body, -1, 1);
	}
	
	private void render(SPRITE_RENDERER r, COLOR c, int mm, int m) {
		if (m != 0) {
			c.render(r, body.cX()-mm, body.cX()+mm, body.cY()-mm, body.cY()+mm);
			for (DIR d : DIR.ORTHO) {
				
				if ((d.mask() & m) != 0) {
					int dd = (d.mask() & a) != 0 ? -1 : 0;
					int x1 = body.cX() + d.x()*(body.width()+dd)/2 +d.y()*mm;
					int y1 = body.cY() + d.y()*(body.height()+dd)/2 + d.x()*mm;
					int x2 = body.cX() - d.y()*mm;
					int y2 = body.cY() - d.x()*mm;
					c.render(r, x1, x2, y1, y2);
				}
			}
		}
	}
	
	void hover(int m) {
		if (hoverI != VIEW.renI+1)
			hm = 0;
		hm |= m;
		hoverI = VIEW.renI+1;
	}
	

	
}