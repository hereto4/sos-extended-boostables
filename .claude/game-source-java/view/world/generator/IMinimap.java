package view.world.generator;

import init.constant.C;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import snake2d.MButt;
import snake2d.Renderer;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import view.interrupter.Interrupter;
import view.world.panel.UIMinimapW;

class IMinimap extends Interrupter{

	private final UIMinimapW map;
	private final GuiSection buttons = new GuiSection();
	private final WorldViewGenerator v;
	
	public IMinimap(WorldViewGenerator v){
		this.v = v;
		map = new UIMinimapW(v.window);
		pin();
		CLICKABLE b;

		b = new GButt.Panel(SPRITES.icons().m.citizen) {
			@Override
			protected void clickA() {
				v.reset();
				new StagePickRace(v);
			};
			
			@Override
			protected void renAction() {
				activeSet(v.canSelectRace);
			};
		}.hoverInfoSet(StagePickRace.¤¤title);
		buttons.addRight(0, b);
		
		b = new GButt.Panel(SPRITES.icons().m.city) {
			@Override
			protected void clickA() {
				v.reset();
				new StageVisuals(v);
			};
		}.hoverInfoSet(StageVisuals.¤¤title);
		buttons.addRight(8, b);
		
		b = new GButt.Panel(SPRITES.icons().m.arrow_up) {
			
			@Override
			protected void clickA() {
				v.reset();
				new StagePickTitles(v);
			};
		}.hoverInfoSet(StagePickTitles.¤¤title);
		buttons.addRightC(8, b);
		
		b = new GButt.Panel(SPRITES.icons().m.terrain) {
			@Override
			protected void clickA() {
				new StageTerrain(v);
			};
		}.hoverInfoSet(StageTerrain.¤¤title);
		buttons.addRightC(8, b);
		
		b = new GButt.Panel(SPRITES.icons().m.plus) {
			@Override
			protected void clickA() {
				if (v.window.zoomout() > 0)
					v.window.setZoomout(v.window.zoomout()-1);
				//inters.miniview.toggle();
			};
			@Override
			protected void renAction() {
				activeSet(v.window.zoomout() > 0);
			}
		};
		buttons.addRightC(32, b);
		
		b = new GButt.Panel(SPRITES.icons().m.minus) {
			@Override
			protected void clickA() {
				if (v.window.zoomout() < 3)
					v.window.setZoomout(v.window.zoomout()+1);
				//inters.miniview.toggle();
			};
			@Override
			protected void renAction() {
				activeSet(v.window.zoomout() < 3);
			}
		};
		buttons.addRightC(0, b);

		RENDEROBJ pan = new RENDEROBJ.RenderImp(map.body().width(), 32) {
			
			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				UI.PANEL().butt.render(r, body, 0, DIR.S, DIR.W);
			}
		};
		
//		GuiSection pan = new GuiSection();
//		pan.add(UI.PANEL().panelL.get(DIR.N, DIR.E), 0, 0);
//		while(pan.body().width() <= map.body().width()+UI.PANEL().panelL.dim())
//			pan.addRightC(0, UI.PANEL().panelL.get(DIR.N, DIR.E, DIR.W));
		
		buttons.body().moveX2(C.WIDTH()-4);
		buttons.body().moveY1(0);
		pan.body().moveX2(C.WIDTH());
		buttons.add(pan);
		buttons.moveLastToBack();
		

		map.body().moveY1(buttons.body().y2());
		map.body().moveX2(C.DIM().width());
		show(v.uiManager);
	}

	@Override
	protected boolean hover(COORDINATE mCoo, boolean mouseHasMoved) {
		return buttons.hover(mCoo) | map.hover(mCoo) | mCoo.isWithinRec(map.body());
	}

	@Override
	protected void mouseClick(MButt button) {
		if (button != MButt.LEFT)
			return;
		if (buttons.click())
			return;
		else
			map.click();
	}

	@Override
	protected boolean render(Renderer r, float ds) {
		
		buttons.render(r, 0);
		map.render(r, 0);
		
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
	
	@Override
	protected void hide() {
		super.hide();
	}
	
	public void show() {
		show(v.uiManager);
	}
	
}
