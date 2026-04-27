package view.sett.ui.minimap;

import static settlement.main.SETT.PIXEL_BOUNDS;

import game.GAME;
import init.constant.C;
import settlement.main.SETT;
import snake2d.MButt;
import snake2d.Renderer;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Rec;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import util.gui.misc.GBox;
import view.interrupter.InterManager;
import view.interrupter.Interrupter;
import view.keyboard.KEYS;
import view.subview.GameWindow;

final class UIMiniMapSettView extends Interrupter {

	private final Rec absBounds = new Rec(C.DIM());

	private final GuiSection ss = new GuiSection();
	private final GameWindow window = new GameWindow(C.DIM(), SETT.PIXEL_BOUNDS, 0).setzoomoutMax(6);
	
	boolean hovered = false;
	
	private final UIMiniMapSettViewMap mini;
	
	private final GameWindow c;
	private final InterManager manager;
	
	public UIMiniMapSettView(UIMinimapSett m, InterManager i, GameWindow w, UIMinimapSettConfig config) {
		this.manager = i;
		persistantSet();
		int zoomout = 4;
		while ((PIXEL_BOUNDS.width() >> zoomout) > C.WIDTH() || (PIXEL_BOUNDS.height() >> zoomout) > C.HEIGHT())
			zoomout++;
		if (zoomout > 6)
			zoomout = 6;
		this.c = w;
		mini = new UIMiniMapSettViewMap(config);
		
		
		
		
		window.setZoomout(4);
		window.setzoomoutMax(zoomout);
		
		
		config.addButtons(ss, w, m);
		ss.body().moveY1(30);
		ss.body().moveX2(C.WIDTH()-50);
		
	}

	public void addButt(CLICKABLE c) {
		ss.addRightC(0, c);
		ss.body().moveY1(30);
		ss.body().moveX2(C.WIDTH()-50);
	}
	
	@Override
	protected void hoverTimer(GBox text) {
		ss.hoverInfoGet(text);
	}

	@Override
	protected boolean render(Renderer r, float ds) {
		ss.render(r, ds);
		mini.render(r, ds, window, absBounds, window.pixel(), hovered);
		hovered = false;

		return false;
	}
	
	
	public void showMin() {
		window.setZoomout(4);
		show();
	}
	
	public void show() {
		
		if (window.zoomout() < 4) {
			window.setZoomout(4);
			hide();
		}
		window.setFromOther(c);
		
		super.show(manager);
	}
	
	public void showFull() {
		window.setZoomout(window.zoomoutmax());
		window.centerAt(c.pixels().cX(), c.pixels().cY());
		up();
		
		super.show(manager);
	}
	
	@Override
	public void hide() {
		super.hide();
	}
	
	
	@Override
	protected void mouseClick(MButt button) {
		if (button == MButt.LEFT) {
			if (hovered) {
				window.zoomByMouse(4);
				c.setFromOther(window);
				hide();
			}else {
				ss.click();
			}
			
		}else if(button == MButt.RIGHT) {
			hide();
		}
	}
	
	@Override
	protected boolean hover(COORDINATE mCoo, boolean mouseHasMoved) {
		
		ss.hover(mCoo);
		hovered = window.pixel().isWithinRec(SETT.PIXEL_BOUNDS) && ! ss.hoveredIs();
		if (!ss.hoveredIs())
			window.hover();
		
		return true;
	}

	@Override
	protected void deactivateAction() {
		
	}
	
	private void up() {
		
		mini.update();
		
	}
	
	@Override
	protected boolean update(float ds) {
		
		if (window.zoomout() < 4) {
			c.setFromOther(window);
			hide();
		}

		if (KEYS.MAIN().MINIMAP.consumeClick()) {
			hide();
			return true;
		}

		
		
		
		GAME.SPEED.poll();
		
		window.update(ds);
		up();

		return true;
	}

	

}
