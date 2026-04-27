package view.world.panel;

import game.GAME;
import init.constant.C;
import init.sprite.SPRITES;
import snake2d.CORE;
import snake2d.MButt;
import snake2d.Renderer;
import snake2d.SPRITE_RENDERER;
import snake2d.SUPER_SCREENSHOT;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Rec;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import util.colors.GCOLOR;
import util.gui.common.SuperSc;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import view.interrupter.InterManager;
import view.interrupter.Interrupter;
import view.keyboard.KEYS;
import view.keyboard.KeyButt;
import view.main.VIEW;
import view.subview.GameWindow;
import view.ui.top.UIPanelTop;
import world.WORLD;

final class UIMinimap extends Interrupter{

	private final UIMinimapW map;
	private boolean expanded = true;
	private final GuiSection buttons = new GuiSection();
	private final GameWindow window;
	
	public UIMinimap(UIPanelTop top, InterManager m, GameWindow window, String saveKey){
		
		this.window = window;
		pin();
		map = new UIMinimapW(window);
		
		CLICKABLE b;
		
		if (top != null) {
			b = new WorldHeatmaps();
			buttons.addRight(0, b);
		}
		
		b = new GButt.ButtPanel(SPRITES.icons().s.camera) {
			@Override
			protected void clickA() {
				CORE.getGraphics().makeScreenShot();
			};
		};
		b = KeyButt.wrap(b, KEYS.MAIN().SCREENSHOT);
		buttons.addRight(0, b);
		
		b = new GButt.ButtPanel(SPRITES.icons().s.cameraBig) {
			
			private final SuperSc sst = new SuperSc("SUPER_WORLD", new SUPER_SCREENSHOT[] {new Shot(2, 2), new Shot(1, 2), new Shot(1, 1),}, saveKey);
			
			
			@Override
			protected void clickA() {
				VIEW.inters().popup.show(sst, this, true);
			};
		};
		b.hoverInfoSet(SuperSc.¤¤name);
		buttons.addRightC(8, b);
		
		b = new GButt.ButtPanel(SPRITES.icons().s.magnifier) {
			@Override
			protected void clickA() {
				if (window.zoomout() > 0)
					window.setZoomout(window.zoomout()-1);
				//inters.miniview.toggle();
			};
			@Override
			protected void renAction() {
				activeSet(window.zoomout() > 0);
			}
		};
		b = KeyButt.wrap(b, KEYS.MAIN().ZOOM_IN);
		buttons.addRightC(10, b);
		
		b = new GButt.ButtPanel(SPRITES.icons().s.minifier) {
			@Override
			protected void clickA() {
				if (window.zoomout() < 3)
					window.setZoomout(window.zoomout()+1);
				//inters.miniview.toggle();
			};
			@Override
			protected void renAction() {
				activeSet(window.zoomout() < 2);
			}
		};
		b = KeyButt.wrap(b, KEYS.MAIN().ZOOM_OUT);
		buttons.addRightC(0, b);

		b = new GButt.ButtPanel(SPRITES.icons().s.arrowUp) {
			@Override
			protected void clickA() {
				expanded = !expanded;
			};
			@Override
			protected void renAction() {
				selectedSet(expanded);
			}
		};
		buttons.addRightC(0, b);
		
		RENDEROBJ pan = new RENDEROBJ.RenderImp(map.body().width(), 32) {
			
			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				GCOLOR.UI().panBG.render(r, body);
				GCOLOR.UI().border(r, body.x1(), body.x1()+3, body.y1(), body.y2());
				GCOLOR.UI().border(r, body.x1(), body.x2(), body.y2(), body.y2()+3);
			}
		};
		
//		GuiSection pan = new GuiSection();
//		pan.add(UI.PANEL().panelL.get(DIR.N, DIR.E), 0, 0);
//		while(pan.body().width() <= map.body().width()+UI.PANEL().panelL.dim())
//			pan.addRightC(0, UI.PANEL().panelL.get(DIR.N, DIR.E, DIR.W));
		
		
		
		buttons.body().moveX2(C.WIDTH()-4);
		buttons.body().moveY1(0);
		pan.body().moveX2(C.WIDTH());
		pan.body().centerY(buttons);
		buttons.add(pan);
		buttons.moveLastToBack();
		buttons.body().moveY1(top == null ? 0 : UIPanelTop.HEIGHT);

		map.body().moveY1(buttons.body().y2());
		map.body().moveX2(C.DIM().width());
		show(m);
	}
	
	public void render(SPRITE_RENDERER r, GameWindow window) {
		
		buttons.render(r, 0);
		
		if (!expanded)
			return;
		
		map.render(r, 0);
		
		
	}

	@Override
	protected boolean hover(COORDINATE mCoo, boolean mouseHasMoved) {
		return buttons.hover(mCoo) | map.hover(mCoo);
	}

	@Override
	protected void mouseClick(MButt button) {
		if (button != MButt.LEFT)
			return;
		if (buttons.click())
			return;
		else if (expanded)
			map.click();
	}

	@Override
	protected boolean render(Renderer r, float ds) {
		render(r, window);
		return true;
	}

	@Override
	protected boolean update(float ds) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected void hoverTimer(GBox text) {
		buttons.hoverInfoGet(text);
	}
	
	private static class Shot extends SUPER_SCREENSHOT {
		
		private final int zoomout;
		private final int winW;
		private final int winH;
		private Rec current;
		
		Shot(int scale, int zoomout){
			super(scale);
			this.zoomout = zoomout;
			winW = (C.WIDTH())<<zoomout;
			winH = (C.HEIGHT())<<zoomout;
			current = new Rec(winW, winH);
		}
		
		@Override
		public boolean renderAndHasNext() {
			
			if (current.y1() >= WORLD.PHEIGHT())
				return false;
			
			WORLD.OVERLAY().hide();
			boolean t = WORLD.FOW().toggled.is();
			WORLD.FOW().toggled.set(false);
			GAME.world().render(CORE.renderer(), 0, zoomout, current, 0, 0);
			current.incrX(winW);
			if (current.x1() >= WORLD.PWIDTH()) {
				current.incrY(winH);
				current.moveX1(0);
			}
			WORLD.FOW().toggled.set(t);
			return true;
		}
		
		@Override
		public int getWidth() {
			return WORLD.PWIDTH()>>zoomout;
		}
		
		@Override
		public int getHeight() {
			return WORLD.PHEIGHT()>>zoomout;
		}

		@Override
		public void init() {
			current.set(0, winW, 0, winH);
			
		}
		
	}
	
}
