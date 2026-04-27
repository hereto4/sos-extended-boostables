package view.sett.ui.subject;

import init.sprite.UI.Icons.S.IconS;
import init.sprite.UI.UI;
import settlement.stats.STATS;
import settlement.stats.stat.STAT;
import settlement.stats.stat.StatCollection;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.LinkedList;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.text.Str;
import snake2d.util.sprite.text.StringInputSprite;
import util.gui.misc.GHeader;
import util.gui.misc.GInput;
import util.gui.misc.GMeter;
import util.gui.misc.GText;
import util.gui.table.GScrollRows;
import util.info.GFORMAT;
import util.text.Dic;

final class SStats {
	
	private final AInfo a;
	private final GuiSection section = new GuiSection();
	
	SStats(AInfo a, int height) {
		this.a = a;
		section.addRelBody(8, DIR.S, makeStats(height-16));
	}
	
	private RENDEROBJ makeStats(int height) {
		
		LinkedList<RENDEROBJ> rows = new LinkedList<>();
		
		GText work = new GText(UI.FONT().S, 32);


		for (StatCollection h : STATS.COLLECTIONS()) {

			LinkedList<STAT> stats = new LinkedList<>();
			for (STAT s : h.all()) {
				if (s.key() == null)
					continue;
				if (s.standing() == null)
					continue;
				stats.add(s);
				
//				outer:
//				for (Race r : RACES.all()) {
//					for (HCLASS c : HCLASSES.ALL())
//					if (s.standing().max(c, r) != 0) {
//						stats.add(s);
//						break outer;
//					}
//				}
			}
			
			if (stats.size() == 0)
				continue;
		
			rows.add(new GHeader(h.info.name).hoverInfoSet(h.info.desc));
			for (STAT s : stats) {
			
				CLICKABLE c = new Row(s, work); 				
				rows.add(c);
			}
		}
	
		GuiSection s = new GuiSection();
		
		GInput in = new GInput(new StringInputSprite(32, UI.FONT().S).placeHolder(Dic.¤¤Search));
		
		s.add(in);
		
		GScrollRows sc = new GScrollRows(rows, height-s.body().height()-4-s.body().height(), 0) {
			@Override
			protected boolean passesFilter(int i, RENDEROBJ o) {
				if (in.text() == null || in.text().length() == 0)
					return true;
				if (o instanceof Row) {
					return Str.containsText(((Row)o).s.info().name, in.text());
				}
				return false;
			}
		};
		
		
		
		s.addDown(4, sc.view());
		
		return s;
	}
	

	
	private class Row extends CLICKABLE.ClickableAbs {
		
		private final GText work;
		private final STAT s;
		private final SPRITE icon;
		Row(STAT stat, GText text){
			this.work = text;
			this.s = stat;
			body.setDim(480, 32);
			if (stat.info().icon != null) {
				icon = stat.info().icon.resized(IconS.M);
			}else
				icon = null;
			
		}
		
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
			if (isHovered) {
				COLOR.BLUEDARK.render(r, body());
			}
			
			if (icon != null) {
				icon.renderCY(r, body().x1(), body().cY());
			}
			
			work.setFont(UI.FONT().S);
			work.clear();
			work.add(s.info().name);
			work.setMaxWidth(220);
			work.setMultipleLines(false);
			work.lablifySub();
			work.renderCY(r, body().x1()+32, body().cY());
			
			work.setFont(UI.FONT().S);
			work.clear();
			
			if (s.indu().max(a.a.indu()) == 1 && s.info().isInt()) {
				GFORMAT.bool(work, s.indu().get(a.a.indu()) == 1);
			}else if (s.info().isInt()) {
				
				GFORMAT.i(work, s.indu().get(a.a.indu()));
			}else {
				GFORMAT.perc(work, s.indu().getD(a.a.indu()));
			}
			work.normalify();
			work.renderCY(r, body().x1()+260, body().cY());
			
			double now = s.standing().get(a.a.indu());
			double max = s.standing().max(a.a.indu().clas(), a.a.race());
			int w = (int) (150*s.standing().normalized(a.a.indu().clas(), a.a.race()));
			if (w > 0) {
				if (w < 20)
					w = 20;
				GMeter.render(r, GMeter.C_REDGREEN, now/max, body().x1()+330, body().x1()+330+w, body().y1()+8, body().y2()-8);
			}
			
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			s.hover(text, a.a.indu());
			
			
		}
		
		@Override
		protected void clickA() {
			if (s.indu() != null)
				SDebugInput.activate(s.indu(), a.a);
		}
		
	}
	
	GuiSection activate() {
		return section;
	}

}
