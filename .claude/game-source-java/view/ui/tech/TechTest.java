package view.ui.tech;



import game.boosting.BoostSpec;
import game.boosting.Boostable;
import game.faction.player.PTech;
import init.sprite.UI.UI;
import init.tech.TECH;
import init.tech.TECH.TechRequirement;
import init.value.Lock;
import init.tech.TECHS;
import init.tech.TechCost;
import settlement.main.SETT;
import settlement.room.industry.module.INDUSTRY_HASER;
import settlement.room.main.RoomBlueprint;
import settlement.room.main.RoomBlueprintImp;
import settlement.room.main.employment.RoomEquip.Target;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.VectorImp;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.Hoverable.HOVERABLE.HoverableAbs;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.Bitmap1D;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GText;
import util.info.GFORMAT;
import view.main.VIEW;

class TechTest extends HoverableAbs{

	public static GuiSection get() {
		GuiSection s = new GuiSection();
		TechTest t = new TechTest();
		int i = 0;
		int y1 = 0;
		int x1 = 0;
		for(Plot p : t.plots){
			GButt.Checkbox c = new GButt.Checkbox(p.blue.icon.small) {
				@Override
				protected void clickA() {
					p.show = !p.show;
				}
				@Override
				protected void renAction() {
					selectedSet(p.show);
				}
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					GBox b = (GBox) text;
					double bo = 0;
					double cost = 0;
					for (Line l : p.lines) {
						b.text(l.name);
						b.add(GFORMAT.f0(b.text(), l.bo));
						b.add(GFORMAT.iIncr(b.text(), (int)l.cost));
						bo += l.bo;
						cost += l.cost;
						text.NL();
						
					}
					b.add(GFORMAT.f0(b.text(), bo));
					b.add(GFORMAT.iIncr(b.text(), (int)cost));
				}
				
			};
			s.add(c, x1, y1);
			if (i++ > 10) {
				y1 = s.body().y2();
				i = 0;
				x1 = 0;
			}else
				x1 += c.body().width();
		}
		
		GuiSection ff = new GuiSection();
		ff.add(t);
		ff.addRelBody(80, DIR.E, new GText(UI.FONT().S, "y = boost, x = cost"));
		s.addRelBody(16, DIR.S, ff);
		
		return s;
		
	}
	
	static int width = 600;
	static int height = 600;
	private static final double COSTI = 1.0/1000.0;
	private static final double BI = 1.0/6.0;

	private final ArrayListGrower<Plot> plots = new ArrayListGrower<>();
	

	private TechTest(){
		
		for (RoomBlueprint b : SETT.ROOMS().all()) {
			if (b instanceof INDUSTRY_HASER) {
				INDUSTRY_HASER i = (INDUSTRY_HASER) b;
				if (i.industries().get(0).outs().size() > 0) {
					Plot p = new Plot(i.industries().get(0).blue);
					if (p.prevCost > 0)
						plots.add(p);
				}
				
			}
		}
		
		body().setDim(width, height);
	}
	
	@Override
	protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
		for (Plot p : plots)
			p.render(r);
		GButt.ButtPanel.renderFrame(r, body);
	}
	
	@Override
	public void hoverInfoGet(GUI_BOX text) {
		int dx = VIEW.mouse().x()-body.x1();
		int dy = height-VIEW.mouse().y()-body.y1();
		GBox b = (GBox) text;
		b.add(b.text().add('x').add(dx/COSTI/width));
		b.add(b.text().add('y').add(dy/BI/height));
	}

	private class Plot {

		private final RoomBlueprintImp blue;
		private double prevBo = 0;
		private double prevCost = 0;
		private double bo = 0;
		private double cost = 0;
		private final COLOR col;
		String tt = "";
		private final ArrayListGrower<Line> lines = new ArrayListGrower<>();
		private boolean show = false;
		
		Plot(RoomBlueprintImp blue){
			
			this.blue = blue;
			col = COLOR.UNIQUE.getC(blue.index());
			Boostable bo = blue.bonus();
			if (bo == null)
				return;
			
			Bitmap1D map = new Bitmap1D(TECHS.ALL().size(), false);
			Bitmap1D mapN = new Bitmap1D(TECHS.ALL().size(), false);
			boolean has = true;
			int ii = 0;
			while (has) {
				mapN.clear();
				flush();
				has = false;
				outer:
				for (TECH t : TECHS.ALL()) {
					if (map.get(t.index()))
						continue;
					for (TechRequirement re : t.requires()) {
						if (!map.get(re.tech.index()))
							continue outer;
					}
					has = true;
					mapN.set(t.index(), true);
					add(t, ii);
				}
				for (TECH t : TECHS.ALL()) {
					if (mapN.get(t.index()))
						map.set(t.index(), true);
				}
				ii++;
			}
		}
		
		private void add(TECH t, int ii) {
			if (t.AIAmount == 0)
				return;
			
			double b = boost(t) + tool(t) + upgrade(t);
			
			boolean ll = false;
			for (Lock<?> l : t.lockers.all()) {
				if (l.lockable.key.equals("ROOM_" + blue.key)) {
					ll = true;
				}
			}
			
			if (b > 0 || ll) {
				if (tt.length() > 0)
					tt += " + ";
				else
					tt += " ";
					
				tt += " " + t.name() + "(" + cost(t) + "," + (int)(b*100)/100.0 + ")";
				bo += b;
				cost += cost(t);
			}
			
		}
		
		private double cost(TECH t) {
			double c = 0;
			for (TechCost tc : t.costs) {
				c += PTech.costTotal(tc, t, t.levelMax);
			}
			return c;
		}
		
		private double boost(TECH t) {
			for (BoostSpec s : t.boosters.all()) {
				if (SETT.ROOMS().bonus.get(s.boostable) == blue) {
					return s.booster.to()*t.levelMax;
				}
			}
			
			return 0;
		}
		
		private double tool(TECH t) {
			for (BoostSpec s : t.boosters.all()) {
				if (SETT.ROOMS().employment.equip.boostToTarget(s.boostable) != null) {
					Target ta = SETT.ROOMS().employment.equip.boostToTarget(s.boostable);
					if (ta.blue == blue) {
						return 0.25*0.75*t.levelMax*s.booster.to();
					}
				}
			}
			return 0;
		}
		
		private double upgrade(TECH t) {
			for (Lock<?> l : t.lockers.all()) {
				String n = l.lockable.key;
				
				if (n.contains(blue.key + "_UPGRADE_")) {
					
					String[] nn = n.split("_UPGRADE_");
					if (nn.length == 2) {
						RoomBlueprintImp b = (RoomBlueprintImp) SETT.ROOMS().collection.tryGet(nn[0].replace("ROOM_", ""));
						if (b == blue) {
							int lev = Integer.parseInt(nn[1]);
							
							if (lev == 1)
								return 0.8;
							if (lev == 2)
								return 0.5;
							return 0.3;
						}
						return 0;
					}

				}
			}
			return 0;
		}
		
		private void flush() {
			if ((bo == 0 && cost == 0) || (bo == prevBo || cost == prevCost))
				return;
			
			double startX = prevCost*COSTI*width;
			double startY = prevBo*BI*height;
			
			Line l = new Line(tt, startX, startY, bo, cost);
			tt = "";
			lines.add(l);
			
			prevBo += bo;
			prevCost+= cost;
			bo = 0;
			cost = 0;
		}
		
		
		private void render(SPRITE_RENDERER r) {
			if (!show)
				return;
			for (Line l : lines)
				l.render(r, body.x1(), body.y2(), col);
		}
	}
	
	private static class Line {
		private final VectorImp vec = new VectorImp();
		private final int mag;
		private final double startX;
		private final double startY;
		private final CharSequence name;
		public final double bo;
		public final double cost;
		
		Line(CharSequence name, double startX, double startY, double bo, double cost){
			this.name = ""+name;
			this.startX = startX;
			this.startY = startY;
			mag = (int) Math.ceil(vec.set(cost*COSTI*width, bo*BI*height));
			this.bo = bo;
			this.cost = cost;
		}
		
		private void render(SPRITE_RENDERER r, int x1, int y1, COLOR col) {
			
			
			for (int i = 0; i < mag; i++) {
				int x = (int) (x1 + startX + vec.nX()*i);
				int y = (int) (y1 - (startY + vec.nY()*i));
				if (i == 0) {
					col.render(r, x, x+3, y, y+3);
				}else
					col.render(r, x, x+1, y, y+1);
			}
		}
		
	}
	
	
	
}
