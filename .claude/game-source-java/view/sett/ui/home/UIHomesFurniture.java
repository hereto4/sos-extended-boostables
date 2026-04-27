package view.sett.ui.home;


import init.race.RACES;
import init.race.Race;
import init.resources.RESOURCE;
import init.resources.RES_AMOUNT;
import init.type.HCLASS;
import init.type.HCLASSES;
import settlement.stats.STATS;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.LinkedList;
import util.gui.misc.GBox;
import util.gui.misc.GHeader;
import util.gui.misc.GMeter;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.table.GScrollRows;
import util.info.GFORMAT;
import util.text.D;
import util.text.Dic;

final class UIHomesFurniture extends GuiSection{

	private static CharSequence ¤¤Yearly = "¤{0} per item per year, estimation: -{1} in total per year.";
	
	private static CharSequence ¤¤CurrentlyUsed = "¤Currently Used";
	private static CharSequence ¤¤CurrentTarget = "¤Current Target";
	private static CharSequence ¤¤CurrentMax = "¤Current Maximum";
	private static CharSequence ¤¤manage = "¤Limits regarding what resources that can be used, is set in the subject panels respectively.";

	static {
		D.ts(UIHomesFurniture.class);
	}
	
	
	public UIHomesFurniture(int height) {
		
		
		
		LinkedList<RES_AMOUNT> all = new LinkedList<RES_AMOUNT>(RACES.res().homeResMax(null));
		LinkedList<RENDEROBJ> rows = new LinkedList<>();
		
		while(!all.isEmpty()) {
			
			int am = 0;
			RES_AMOUNT r = all.removeFirst();
			GuiSection s = new GuiSection();
			rows.add(s);
			s.add(new REN(r.resource()));
			
			while(!all.isEmpty() && am++ < 4) {
				r = all.removeFirst();
				s.addRightC(8, new REN(r.resource()));
			}
			
		}
		
		GScrollRows r = new GScrollRows(rows, height);
		
		add(r.view());
		
		addRelBody(8, DIR.N, new GHeader(STATS.HOME().materials.info().name));
		
		
	}
	
	
	private static class REN extends HOVERABLE.HoverableAbs {
		
		private final RESOURCE res;
		private final int[][] ti = new int[HCLASSES.ALL().size()][RACES.all().size()];
		private final GStat s;
		
		REN(RESOURCE res){
			this.res = res;
			body.setDim(100, 32);
			for (HCLASS c : HCLASSES.ALL()) {
				for (Race r : RACES.all()) {
					ti[c.index()][r.index] = -1;
					for (int i = 0; i < r.home().clas(c).resources().size(); i++) {
						if (r.home().clas(c).resources().get(i).resource() == res) {
							ti[c.index()][r.index] = i;
							break;
						}
						
					}
				}
			}
			
			s = new GStat() {
				
				@Override
				public void update(GText text) {
					int i = getCurrent();
					GFORMAT.i(text, i);
				}
				
				@Override
				public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
					OPACITY.O50.bind();
					COLOR.BLACK.render(r, X1-4, X2+4, Y1-2, Y2+2);
					OPACITY.unbind();
					super.render(r, X1, X2, Y1, Y2);
				}
			};
			
			
		}
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
			double c = getCurrent();
			double max = max();
			
			if (max == 0) {
				GMeter.render(r, GMeter.C_GRAY, 1.0, body());
			}else {
				double d = c/max();
				GMeter.render(r, GMeter.C_REDGREEN, d, body());
			}
			
			res.icon().renderCY(r, body().x1()+8, body().cY());
			
			s.adjust();
			
			s.renderCY(r, body().x1()+40, body().cY());
			
		}
		
		private int getTarget() {
			int am = 0;
			for (HCLASS c : HCLASSES.ALL()) {
				for (Race r : RACES.all()) {
					if (ti[c.index()][r.index] == -1)
						continue;
					am += STATS.HOME().target(c, r, res)*STATS.HOME().GETTER.stat().data(c).get(r);
				}
			}
			return am;
		}
		
		private int max() {
			int am = 0;
			for (HCLASS c : HCLASSES.ALL()) {
				for (Race r : RACES.all()) {
					if (ti[c.index()][r.index] == -1)
						continue;
					am += STATS.HOME().max(c, r, res)*STATS.HOME().GETTER.stat().data(c).get(r);
				}
			}
			return am;
		}
		
		private int getCurrent() {
			int am = 0;
			for (HCLASS c : HCLASSES.ALL()) {
				
				for (Race r : RACES.all()) {
					if (ti[c.index()][r.index] == -1)
						continue;
					am += STATS.HOME().current(c, r, ti[c.index()][r.index]);
				}
			}
			return am;
		}

		@Override
		public void hoverInfoGet(GUI_BOX text) {
			GBox b = (GBox) text;
			b.title(res.name);
			b.text(STATS.HOME().materials.info().desc);
			b.NL(4);
			b.text(¤¤manage);
			
			b.NL(8);
			
			int current = getCurrent();
			
			b.textL(¤¤CurrentlyUsed);
			b.tab(5);
			b.add(GFORMAT.i(b.text(), current));
			b.NL();
			
			b.textL(¤¤CurrentTarget);
			b.tab(5);
			b.add(GFORMAT.i(b.text(), getTarget()));
			b.NL();
			
			b.textL(¤¤CurrentMax);
			b.tab(5);
			b.add(GFORMAT.i(b.text(), max()));
			b.NL();
			
			
			b.textL(Dic.¤¤ConsumptionRate);
			b.NL();
			GText t = b.text();
			t.add(¤¤Yearly);
			t.insert(0, STATS.HOME().rate(null, null), 2);
			t.insert(1, (int)(STATS.HOME().rate(null, null)*current));
			b.add(t);
			super.hoverInfoGet(text);
		}
		
		
	}

	
	
}
