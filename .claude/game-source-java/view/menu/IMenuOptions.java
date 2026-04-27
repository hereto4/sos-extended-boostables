package view.menu;

import init.constant.C;
import init.settings.S;
import init.settings.S.Setting;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.text.Font;
import snake2d.util.sprite.text.Str;
import util.colors.GCOLOR;
import util.data.INT.INTE;
import util.gui.slider.GSliderInt;
import util.gui.table.GScrollRows;
import util.text.Dic;

class IMenuOptions extends GuiSection{
	
	IMenuOptions(IMenu m, Font font, Font small) {
		
		MenuScreen sc = new MenuScreen(Dic.¤¤OPTIONS, GCOLOR.T().H1) {
			
			@Override
			protected void back() {
				m.setMain();
			}
		};
		
		add(sc);
		
		int am = S.get().all().size();
		
		RENDEROBJ[] rs = new RENDEROBJ[am];
		am = 0;
		for (Setting s : S.get().all()){
			
			
			rs[am++] = new OptionLine(s, small);
		}
		
		RENDEROBJ r = new GScrollRows(rs, 300, 0).view();
		
		r.body().centerIn(this.body());
		add(r);
		
		

	}
	
	private class OptionLine extends GuiSection{
		
		private final SPRITE label;
		private final GSliderInt sl;
		private final Setting sett;
		private final Font font;
		
		OptionLine(Setting s, Font font) {
			
			this.sett = s;
			this.font = font;
			body().setWidth(600);
			
			label = font.getText(s.name);
			
			INTE ii = new INTE() {
				
				@Override
				public int min() {
					return 0;
				}
				
				@Override
				public int max() {
					return s.max();
				}
				
				@Override
				public int get() {
					return s.get();
				}
				
				@Override
				public void set(int t) {
					s.set(t);
					S.get().applyRuntimeConfigs();
				}
			};
			
			sl = new GSliderInt(ii, 135, false);
			add(sl, body().cX()-sl.body().width()/2, 0);
			pad(0, 4);
			
		}
		
		@Override
		public void render(SPRITE_RENDERER r, float ds) {
			super.render(r, ds);
			GCOLOR.T().NORMAL.bind();
			Str.TMP.clear();
			sett.getValue(Str.TMP);
			font.render(r, Str.TMP, body().x1() + 400, body().y1());
			GCOLOR.T().H1.bind();
			label.renderCY(r, body().cX() - C.SCALE*20 - label.width(), body().cY());
			COLOR.unbind();
		}
		
		
		
	}
	
}
