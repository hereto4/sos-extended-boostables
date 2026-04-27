package view.ui.util;

import java.util.ArrayList;

import init.sprite.UI.UI;
import init.value.GValueCat;
import init.value.Value;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.text.Str;
import snake2d.util.sprite.text.StringInputSprite;
import util.data.GETTER;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GInput;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.table.GScrollRows;
import view.main.VIEW;

public class UIValues<T> extends GuiSection{

	public UIValues(GValueCat<T> vv, GETTER<T> g){
		GInput filter = new GInput(new StringInputSprite(24, UI.FONT().S));
		add(filter);
		final LIST<Value<T>> all = vv.map().allSorted();
		ArrayList<RENDEROBJ> rows = new ArrayList<>(vv.all().size());
		for (Value<T> v : all) {
			GuiSection s = new GButt.BSection() {
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					text.title(v.name);
				}
			};
			s.add(v.icon, 0, 0);
			GText t =  new GText(UI.FONT().S, v.key);
			t.setMaxChars(20);
			s.addRightC(2, t);
			s.addCentredY(new GStat() {
				
				@Override
				public void update(GText text) {
					text.add(v.d.getD(g.get()));
				}
				
				@Override
				public void hoverInfoGet(GBox b) {
					b.add(v.icon);
					b.text(v.name);
				};
				
			}, 400);
			s.body().setWidth(s.getLastX2()+100);
			s.body().pad(4, 2);
			rows.add(s);
		}
		
		
		
		GScrollRows s = new GScrollRows(rows, 800) {
			
			@Override
			protected boolean passesFilter(int i, RENDEROBJ o) {
				if (filter.text().length() == 0)
					return true;
				if (Str.containsText(all.get(i).key, filter.text()) || Str.containsText(all.get(i).name, filter.text()))
					return true;
				return false;
			}
			
		};
		
		addDown(4, s.view());
	}
	
	public static <T>CLICKABLE butt(GValueCat<T> vv, GETTER<T> g) {
		UIValues<T> pop = new UIValues<T>(vv, g);
		return new GButt.ButtPanel(UI.icons().s.menu) {
			
			@Override
			protected void clickA() {
				VIEW.inters().popup2.show(pop, this);
			}
			
		};
	}
	
	
}
