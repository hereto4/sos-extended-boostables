package view.ui.div;

import game.GAME;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sprite.SPRITE;
import util.gui.common.BitmapSpriteEditor;
import util.gui.misc.GButt;
import util.gui.misc.GColorPicker;
import util.text.Dic;
import view.main.VIEW;

public class UIDivBannerEditor {
	
	private int bannerI = 0;
	GuiSection pop = new GuiSection();
	
	RENDEROBJ prevPop;
	CLICKABLE prevPopC;
	
	public UIDivBannerEditor(){
		
		
		BitmapSpriteEditor ee = new BitmapSpriteEditor();
		ee.spriteSet(GAME.ARMIES().banners.get(0).sprite);
		
		for (int i = 0; i < GAME.ARMIES().banners.size(); i++) {
			final int k = i;
			
			GButt.ButtPanel bu = new GButt.ButtPanel(GAME.ARMIES().banners.get(i)){
				
				@Override
				protected void clickA() {
					bannerISet(k);
					ee.spriteSet(GAME.ARMIES().banners.get(k).sprite);
				}
				
				@Override
				protected void renAction() {
					selectedSet(bannerI == k);
				}
				
			};
			bu.pad(2, 0);
			pop.add(bu, (i%10)*bu.body.width(), (i/10)*bu.body.height());
		}
		
		pop.addRelBody(8, DIR.S, ee);
		
		GuiSection c = new GuiSection();
		
		c.addRelBody(8, DIR.S, new GColorPicker(false) {
			
			@Override
			public ColorImp color() {
				return GAME.ARMIES().banners.get(bannerI).col;
			}
		});
		
		c.addRelBody(8, DIR.E, new GColorPicker(false) {
			
			@Override
			public ColorImp color() {
				return GAME.ARMIES().banners.get(bannerI).bg;
			}
		});	
		
		pop.addRelBody(8, DIR.S, c);
		
		pop.addRelBody(8, DIR.S, new GButt.ButtPanel(Dic.¤¤Accept) {
			@Override
			protected void clickA() {
				VIEW.inters().popup.pop();
			}
		});
	}
	
	public int bannerI() {
		return bannerI;
	}
	
	public void bannerISet(int i) {
		bannerI = i;
	}
	
	public RENDEROBJ view() {
		return pop;
	}
	

	public CLICKABLE butt() {
		SPRITE sp = new SPRITE.Imp(32) {
			
			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				GAME.ARMIES().banners.get(bannerI()).render(r, X1+2, Y1+2);
				
			}
		};
		
		
		return new GButt.ButtPanel(sp) {
			
			@Override
			protected void clickA() {
				VIEW.inters().popup.push(view(), this);
			}
			
		}.hoverTitleSet(Dic.¤¤Banner);
	}
	
}