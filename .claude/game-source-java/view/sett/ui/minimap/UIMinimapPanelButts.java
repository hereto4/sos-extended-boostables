package view.sett.ui.minimap;

import java.util.Comparator;

import game.GAME;
import init.constant.C;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import init.sprite.UI.Icons.S.IconS;
import settlement.main.SETT;
import settlement.overlay.Addable;
import snake2d.CORE;
import snake2d.MButt;
import snake2d.SPRITE_RENDERER;
import snake2d.SUPER_SCREENSHOT;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.Rec;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.ArrayList;
import util.colors.GCOLOR;
import util.data.GETTER.GETTER_IMP;
import util.gui.common.SuperSc;
import util.gui.misc.GButt;
import util.text.D;
import util.text.Dic;
import view.keyboard.KEYS;
import view.keyboard.KeyButt;
import view.main.VIEW;
import view.sett.ui.minimap.UIMinimapSett.Butt;
import view.subview.GameWindow;

public class UIMinimapPanelButts {

	final GuiSection section = new GuiSection() {
		@Override
		public void render(SPRITE_RENDERER r, float ds) {
			GCOLOR.UI().panBG.render(r, body());
			super.render(r, ds);
			GCOLOR.UI().borderH(r, body(), 0);
		};
	};
	private final GuiSection pbuttons = new GuiSection();
	
	private static CharSequence ¤¤HideUI = "¤Cinematic mode + Hide UI. Cancel by right click or ESC.";
	private static CharSequence ¤¤ToggleOverlay = "¤Toggle Overlay: ";
	
	static {
		D.ts(UIMinimapPanelButts.class);
	}
	
	UIMinimapPanelButts(UIMiniMapSettView view, UIMinimapPanel panel, GameWindow w){
		section.body().setDim(panel.body().width(), 36);
		pbuttons.body().centerIn(section.body());
		section.add(pbuttons);
		section.body().moveX2(C.WIDTH());
		section.body().moveY1(panel.body().y2());
		
		GButt b;
		
		b = new Butt(SPRITES.icons().s.minimap) {
			@Override
			protected void clickA() {
				view.show();
			};
		};
		padd(KeyButt.wrap(b, KEYS.MAIN().MINIMAP));
		
		b = new Butt(SPRITES.icons().s.minifier) {
			@Override
			protected void clickA() {
				if (w.zoomout() < w.zoomoutmax())
					w.setZoomout(w.zoomout()+1);
				
			};
			@Override
			protected void renAction() {
				activeSet(w.zoomout() < w.zoomoutmax());
			}
		};
		padd(KeyButt.wrap(b, KEYS.MAIN().ZOOM_OUT));
		
		b = new Butt(SPRITES.icons().s.magnifier) {
			@Override
			protected void clickA() {
				if (w.zoomout() > 0)
					w.setZoomout(w.zoomout()-1);
			};
			@Override
			protected void renAction() {
				activeSet(w.zoomout() > 0);
			}
		};
		padd(KeyButt.wrap(b, KEYS.MAIN().ZOOM_IN));
		
		
		

		
		
	}
	
	public void addScreenshot(String savekey) {

		GButt b;
		b = new Butt(SPRITES.icons().s.camera) {
			@Override
			protected void clickA() {
				CORE.getGraphics().makeScreenShot();
			};
			
		};
		padd(KeyButt.wrap(b, KEYS.MAIN().SCREENSHOT));
		
		b = new Butt(SPRITES.icons().s.cameraBig) {
			
			private final SuperSc sst = new SuperSc("SUPER_CITY", new SUPER_SCREENSHOT[] {new Shot(4, 2), new Shot(2, 2), new Shot(2, 1)}, savekey);
			
			
			@Override
			protected void clickA() {
				VIEW.inters().popup.show(sst, this, true);
			};
		};
		b.hoverInfoSet(SuperSc.¤¤name);
		padd(b);
		
		b = new Butt(SPRITES.icons().s.cancel) {
			
			@Override
			protected void clickA() {
				VIEW.hide();
			};
		};
		b.hoverInfoSet(¤¤HideUI);
		padd(b);
	}
	
	public GETTER_IMP<Addable> addOverlays() {
		GuiSection s = new GuiSection();
		
		GETTER_IMP<Addable> thing = new GETTER_IMP<Addable>();
		
		int i = 0;
		ArrayList<Addable> aa = new ArrayList<>(SETT.OVERLAY().all());
		aa.sort(new Comparator<Addable>() {
			
			@Override
			public int compare(Addable arg0, Addable arg1) {
				return (""+arg0.name).compareTo(""+arg1.name);
			}
		});
		for (Addable a : aa) {
			if (a.key != null) {
				CLICKABLE cc = ontop(a, thing);
				s.add(cc, (i%2)*cc.body().width(), (i/2)*cc.body().height());
				i++;
			}
			
		}
		

		CLICKABLE c = new Butt(SPRITES.icons().s.eye) {
			@Override
			protected void clickA() {
				VIEW.inters().popup.show(s, this);
			}
			
			@Override
			protected void renAction() {
				if (hoveredIs() && MButt.RIGHT.consumeClick()) {
					thing.set(null);
				}
				
				if (thing.get() != null) {
					thing.get().add();
				}
				
				selectedSet(thing.get() != null);
			};
		}.hoverInfoSet(Dic.¤¤Overlays);
		
		padd(c);
		return thing;
	}
	
	private CLICKABLE ontop(Addable add, GETTER_IMP<Addable> thing) {
		 ACTION a = new ACTION() {
			
			@Override
			public void exe() {
				if (thing.get() == add)
					thing.set(null);
				else
					thing.set(add);
				
			}
		};
		GButt.ButtPanel c = new GButt.ButtPanel(UI.FONT().H2.getText(add.name)) {
			@Override
			protected void clickA() {
				a.exe();
			};
			
			@Override
			protected void renAction() {
				selectedSet(thing.get() == add);
			};
			
		};
		c.setDim(250, 30).align(DIR.W).hoverTitleSet(add.name).hoverInfoSet(add.desc);
		c.icon(add.icon.resized(IconS.L));
		return KeyButt.wrap(a, c, KEYS.SETT(), "TOGGLE_OVERLAY_" + add.key, add.name, ¤¤ToggleOverlay + " " + add.name);
	}
	
	private static class Shot extends SUPER_SCREENSHOT {
		
		private final int zoomout;
		private final int winW ;
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
			
			if (current.y1() >= SETT.PHEIGHT)
				return false;
			
			GAME.s().render(CORE.renderer(), 0, zoomout, current, 0, 0, UIMinimapSettConfig.NORMAL);
			current.incrX(winW);
			if (current.x1() >= SETT.PWIDTH) {
				current.incrY(winH);
				current.moveX1(0);
			}
			return true;
		}
		
		@Override
		public int getWidth() {
			return SETT.PWIDTH>>zoomout;
		}
		
		@Override
		public int getHeight() {
			return SETT.PHEIGHT>>zoomout;
		}

		@Override
		public void init() {
			current.set(0, winW, 0, winH);
		}
		
	}
	

	
	public void padd(CLICKABLE cl) {
		pbuttons.addRelBody(0, DIR.W, cl);
		pbuttons.body().moveX1(section.body().x1()+4);
		pbuttons.body().moveCY(section.body().cY());
	}
	
	
}
