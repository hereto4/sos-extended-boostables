package view.sett.ui.bottom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.misc.Dictionary;
import snake2d.util.sets.LinkedList;
import snake2d.util.sprite.text.StringInputSprite;
import util.data.INT.INTE;
import util.gui.misc.GHeader;
import util.gui.misc.GInput;
import util.gui.slider.GTarget;
import util.text.Dic;
import view.main.VIEW;

final class SearchToolPanel extends SPanel{

	static LinkedList<Holder> all;
	private final GInput input;
	private final Holder[] nonFiltered ;
	private final ArrayList<Holder> filtered;
	private int page = 0;
	private GuiSection content = new GuiSection();
	
	private final int width = 2;
	private final int height = 10;
	
	static CLICKABLE add(CLICKABLE c, CharSequence name, CharSequence desc) {
		all.add(new Holder(c, name + " " + desc));
		return c;
	}
	
	public SearchToolPanel() {
		
		nonFiltered = new Holder[all.size()];
		int i = 0;
		for (Holder rr : all)
			nonFiltered[i++] = rr;
		filtered = new ArrayList<>(nonFiltered.length);
		Arrays.sort(nonFiltered, new Comparator<Holder>() {

			@Override
			public int compare(Holder o1, Holder o2) {
				return Dictionary.compare(o1.name, o2.name);
			}
		});
		
		input = new GInput(new StringInputSprite(20, UI.FONT().M) {
			@Override
			protected void change() {
				filter(text());
				super.change();
			}
		});
		add(new GHeader(Dic.¤¤Filter));
		addRightC(16, input);
		
		content.body().setDim((BButt.WIDTH+8)*width, (BButt.HEIGHT+2)*height);
		
		addRelBody(16, DIR.S, content);
		
		GTarget t = new GTarget(64, false, true, new INTE() {
			
			@Override
			public int min() {
				return 0;
			}
			
			@Override
			public int max() {
				return filtered.size()/(width*height);
			}
			
			@Override
			public int get() {
				return page;
			}
			
			@Override
			public void set(int t) {
				page = t;
				build();
			}
		});
		addRelBody(8, DIR.S, t);
		
		filter("");
		
		pad(8, 8);
	}
	
	
	
	void open(CLICKABLE c, Inter inter) {
		inter.set(c, this);
		input.focus();
	}
	
	private void filter(CharSequence filter) {
		String f = (""+filter).toUpperCase();
		filtered.clear();
		for (Holder h : nonFiltered) {
			if (h.name.contains(f))
				filtered.add(h);
		}
		page = 0;
		build();
	}
	
	private void build() {
		int x1 = content.body().x1();
		int y1 = content.body().y1();
		content.clear();
		int s = page*(width*height);
		for (int i = 0; s < filtered.size() && i < width*height; i++) {
			Holder h = filtered.get(s);
			s++;
			
			content.add(h, (i%width)*(BButt.WIDTH+8), (i/width)*(BButt.HEIGHT+2));
			
		}
		content.body().moveX1Y1(x1, y1);
	}
	
	private static class Holder extends ClickableAbs {

		private final CLICKABLE other;
		private final String name;
		
		Holder(CLICKABLE other, CharSequence name){
			body.set(other);
			this.other = other;
			this.name = (""+name).toUpperCase();
		}
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
			
			int x1 = other.body().x1();
			int y1 = other.body().y1();
			other.body().moveX1Y1(body().x1(), body().y1());
			other.render(r, ds);
			other.body().moveX1Y1(x1, y1);
		}
		
		@Override
		public boolean hover(COORDINATE mCoo) {
			int x1 = other.body().x1();
			int y1 = other.body().y1();
			other.body().moveX1Y1(body().x1(), body().y1());
			other.hover(mCoo);
			other.body().moveX1Y1(x1, y1);
			return super.hover(mCoo);
		}
		
		@Override
		public boolean click() {
			if (super.click()) {
				VIEW.inters().popup.close();
				other.click();
				return true;
			}
			return false;
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			other.hoverInfoGet(text);
		}
		
	}
	
}
