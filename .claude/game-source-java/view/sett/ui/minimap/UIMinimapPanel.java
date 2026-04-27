package view.sett.ui.minimap;

import static settlement.main.SETT.ENTITIES;

import game.time.TIME;
import init.constant.C;
import settlement.entity.ENTITY;
import settlement.main.SETT;
import snake2d.CORE;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.Coo;
import snake2d.util.datatypes.Rec;
import snake2d.util.gui.clickable.CLICKABLE.ClickableAbs;
import snake2d.util.light.AmbientLight;
import snake2d.util.misc.CLAMP;
import util.colors.GCOLOR;
import util.gui.panel.GFrame;
import view.main.VIEW;
import view.subview.GameWindow;

final class UIMinimapPanel extends ClickableAbs{

	private final int M = GFrame.MARGIN;
	private final int HEIGHT = 128;
	final static int WIDTH = 256;
	private final Rec tiles = new Rec(WIDTH-6, HEIGHT-6);

	private final Rec ents = new Rec(WIDTH-6, HEIGHT-6);
	private final GameWindow w;
	private final Coo lastClick = new Coo();
	private final UIMinimapSettConfig config;
	
	public UIMinimapPanel(GameWindow w, UIMinimapSettConfig config) {
		this.w = w;
		this.config = config;
		body.setDim(WIDTH, HEIGHT);
	}
	
	@Override
	protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {

		
		
//		if (isHovered && MButt.LEFT.isDown() && lastClick.set(VIEW.mouse())) {
//			clickA();
//		}
		
		int x1,y1;
		
		int cx = w.tiles().cX();
		int cy = w.tiles().cY();
		
		x1 = cx-WIDTH/2;
		y1 = cy-HEIGHT/2;
		
//		if (x1 < 0)
//			x1 = 0;
//		if (y1 < 0)
//			y1 = 0;
//		
//		if (x1+WIDTH >= TWIDTH)
//			x1 = TWIDTH-WIDTH;
//		if (y1+HEIGHT >= THEIGHT)
//			y1 = THEIGHT-HEIGHT;
		
		tiles.moveX1Y1(x1, y1);
		
		ents.moveX1Y1(x1, y1);
		
		CORE.renderer().newLayer(false, 0);
		AmbientLight.full.register(body);
		
		
		for (int ty = ents.y1(); ty < ents.y2(); ty++) {
			for (int tx = ents.x1(); tx < ents.x2(); tx++) {
				
				ENTITY e = ENTITIES().getAtTileSingle(tx, ty);
				if (e != null) {
					COLOR c = config.col(e);
					if (c != null) {
						c.bind();
						int x = tx -ents.x1() + body().x1() + M;
						int y = ty -ents.y1() + body().y1() + M;
						CORE.renderer().renderParticle(x, y);
						
					}
					tx = (tx+4)&~3;
					
				}
			}
		}
		
//		for (COORDINATE c : ents) {
//			ENTITY e = ENTITIES().getAtTileSingle(c.x(), c.y());
//			if (e != null) {
//				funk.get(e).bind();
//				int x = c.x() -ents.x1() + frame.body().x1() + M;
//				int y = c.y() -ents.y1() + frame.body().y1() + M;
//				r.renderParticle(x, y);
//			}
//				
//		}
		COLOR.unbind();
		
		CORE.renderer().newLayer(true, 0);
		SETT.MINIMAP().render(r, body().x1() + M, body().y1() + M, tiles);
		config.shade().bind();
		COLOR.BLACK.render(r, body);
		OPACITY.unbind();
		

		int w = this.w.tiles().width();
		int h = this.w.tiles().height(); 
		

		
		x1 = CLAMP.i(cx- w/2+body().x1() + M-tiles.x1(), body().x1()+M, body().x2()-M);
		y1 = CLAMP.i(cy - h/2+body().y1() + M-tiles.y1(), body().y1()+M, body().y2()-M);
		int x2 = CLAMP.i(x1+w, body().x1()+M, body().x2()-M);
		int y2 = CLAMP.i(y1+h, body().y1()+M, body().y2()-M);
		
		OPACITY.O25.bind();
		COLOR.WHITE100.render(r, x1, x2,y1, y2);
		OPACITY.unbind();
		
		CORE.renderer().newLayer(false, 0);
		

		TIME.light().applyGuiLight(ds, C.DIM());
		GCOLOR.UI().borderH(r, body(), 0);
	}

	@Override
	protected void clickA() {
		if (visableIs()) {
			int tx = VIEW.mouse().x()-M-body().x1()+tiles.x1();
			int ty = VIEW.mouse().y()-M-body().y1()+tiles.y1();
			w.centerAtTile(tx, ty);
			lastClick.set(VIEW.mouse());
		}
	}
	
}
