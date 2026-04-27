package settlement.stats.util;

import init.race.Race;
import init.sprite.SPRITES;
import init.type.HCLASS;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import settlement.stats.stat.SETT_STATISTICS;
import settlement.stats.stat.STAT;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.TextureCoords;
import util.colors.GCOLOR;
import util.gui.misc.GBox;
import util.gui.misc.GText;
import util.gui.table.GStaples;
import util.info.GFORMAT;
import util.text.D;

public class StatHoverer {

	private static CharSequence ¤¤Liked = "¤This is liked by your {0}. Higher value = more fulfillment.";
	private static CharSequence ¤¤Dislike = "¤This is disliked by your {0}. Higher value = less fulfillment.";
	private static CharSequence ¤¤DontCare = "¤Your {0} don't care about this and its value has no effect on fulfillment";
	
	private static CharSequence ¤¤ValueCurrent = "¤Current value: ";
	private static CharSequence ¤¤FulfillmentValue = "¤Current Fulfillment: ";
	
	private static CharSequence ¤¤HistoryValue = "¤History Value (days)";
	private static CharSequence ¤¤HistoryFulfillment = "¤History Fulfillment (days)";
	private static CharSequence ¤¤toReachMAx = "¤To reach max fulfillment, value needs to be : {0}%";
	
	static {
		D.ts(StatHoverer.class);
	}
	
	private static UtilGraph h1 = new UtilGraph();
	private static UtilGraph h2 = new UtilGraph();
	
	public static void hover(GUI_BOX text, STAT s) {
		GBox b = (GBox) text;
		b.title(s.info().name);
		b.text(s.info().desc);
		b.NL();
	}
	
	public static void hover(GUI_BOX text, STAT s, HCLASS cl, Race type) {

		GBox b = (GBox) text;
		double max = s.standing().max(cl, type);
		
		
		
		{
			
			double m = s.standing().get(cl, type, 0);
			double mm = s.standing().get(cl, type, 1);
			if (m == mm) {
				GText t = b.text();
				t.add(¤¤DontCare);
				t.insert(0, cl.names);
				b.add(t);
			}else if (m > mm) {
				b.add(SPRITES.icons().m.arrow_down);
				GText t = b.text();
				t.add(¤¤Dislike);
				t.insert(0, cl.names);
				t.errorify();
				b.add(t);
			}else {
				b.add(SPRITES.icons().m.arrow_up);
				GText t = b.text();
				t.add(¤¤Liked);
				t.insert(0, cl.names);
				t.normalify2();
				b.add(t);
			}
			
			
			
		}
		
		b.NL(8);
		
		b.textL(¤¤ValueCurrent);
		b.tab(7);

		b.add(format(b.text(), s, s.data(cl).getD(type), cl, type));
		if (s.info().isInt()) {
			b.add(b.text().add('(').add((int)(s.data(cl).getD(type)*100)).add('%').add(')'));
			
		}
		{
			double d = s.data(cl).getD(type)-s.data().getD(type, 1);
			b.tab(11);
			b.add(GFORMAT.percInc(b.text(), d));
		}
		
	
		
		
		b.NL();
		
		if (max > 0) {
			
			
			
			b.textL(¤¤FulfillmentValue);
			b.tab(7);
			b.add(GFORMAT.fofkInv(b.text(), s.standing().get(cl, type), max));
			double d = s.standing().get(cl, type) - s.standing().getHistoric(cl, type, 1);
			b.tab(11);
			b.add(GFORMAT.f0(b.text(), d));
			b.NL();
			
			if (type != null) {
				GText t = b.text();
				t.add(¤¤toReachMAx);
				if (!s.standing().definition(type).inverted) {
					double e = s.standing().definition(type).exp == null ? 1 :  s.standing().definition(type).exp.pow;
					e = Math.pow(1, -e)/s.standing().definition(type).mul;
					t.insert(0, (int)(100*e));
				}else
					t.insert(0, 0);
				b.add(t);
				b.NL();
			}
		}
		
		b.NL(16);
		b.textLL(¤¤HistoryValue);
		if (max > 0) {
			b.tab(8);
			b.textLL(¤¤HistoryFulfillment);
		}
		
		b.NL(4);
		b.add(h1.init(cl, s, s.info().isInt(), type, true));
		if (max > 0) {
			b.tab(8);
			b.add(h2.init(cl, s, s.info().isInt(), type, false));
		}
	}
	
	public static void hover(GUI_BOX text, STAT s, Induvidual indu) {

		
		HCLASS cl = indu.clas();
		Race type = indu.race();
		GBox b = (GBox) text;
		
		b.NL(8);
		
		double max = s.standing().max(cl, type);
		
		{
			double m = s.standing().get(cl, type, 0);
			double mm = s.standing().get(cl, type, 1);
			
			if (m == mm) {
				GText t = b.text();
				t.add(¤¤DontCare);
				t.insert(0, cl.names);
				b.add(t);
			}else if (m > mm) {
				b.add(SPRITES.icons().m.arrow_down);
				GText t = b.text();
				t.add(¤¤Dislike);
				t.insert(0, cl.names);
				t.errorify();
				b.add(t);
			}else {
				b.add(SPRITES.icons().m.arrow_up);
				GText t = b.text();
				t.add(¤¤Liked);
				t.insert(0, cl.names);
				t.normalify2();
				b.add(t);
			}
			
		}
		
		b.NL(8);
		
		b.textL(¤¤ValueCurrent);
		b.tab(7);
		if (s.info().isInt()) {
			b.add(format(b.text(), s, s.indu().getD(indu), cl, indu.race()));
			b.add(b.text().add('(').add((int)(s.indu().getD(indu)*100)).add('%').add(')'));
			
		}else {
			b.add(format(b.text(), s, s.indu().getD(indu), cl, indu.race()));
			if (s.standing.definition(indu.race()).mul != 1) {
				
				GText t = b.text();
				t.add('-').add('>').s();
				t.add(s.standing.definition(indu.race()).mul).s().add('x').add(s.indu().getD(indu), 2).s().add('=').s();
				t.add(s.standing.definition(indu.race()).mul*s.indu().getD(indu), 2);
				b.add(t);
				
			}
		}
		
		b.NL();
		
		if (max > 0) {
			b.textL(¤¤FulfillmentValue);
			b.tab(7);
			b.add(GFORMAT.fofkInv(b.text(), s.standing().get(indu), max));
			b.NL();
		}
		
	}
	
	static GText format(GText t, STAT s, double v, HCLASS cl, Race race) {
		if (race == null) {
			if (s.info().isInt()) {
				return GFORMAT.f(t, v*s.dataDivider());
			}else {
				return GFORMAT.perc(t, v).normalify();
			}
		}else {
			
			double d = race.stats().def(s.standing()).get(cl).to - race.stats().def(s.standing()).get(cl).from;
			if (s.info().isInt()) {
				double m = s.dataDivider();
				double n = (double)v*s.dataDivider();
				
				if (d>0) {
					return GFORMAT.f0(t, n, m);
				}else if(d<0) {
					return GFORMAT.f0Inv(t, n, m);
				}else {
					return GFORMAT.f(t, n);
				}
			}else {
				if (d>0)
					return GFORMAT.perc(t, v);
				else if (d < 0) {
					return GFORMAT.percInv(t, v);
				}else {
					return GFORMAT.perc(t, v).normalify();
				}
			}
		}
	}
	
	public static SPRITE chart(HCLASS cl, SETT_STATISTICS s, Race race, boolean isInt, boolean isValue) {
		h1.init(cl, s, isInt, race, isValue);
		return h1;
	}
	
	private static class UtilGraph implements SPRITE {

		private final GStaples staples = new GStaples(STATS.DAYS_SAVED) {
			
			@Override
			protected void hover(GBox box, int stapleI) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			protected double getValue(int stapleI) {
				int fromZero = STATS.DAYS_SAVED-stapleI-1;
				if (!valuev && global instanceof STAT) {
					
					STAT s = (STAT) global;
					double m = s.standing.max(c, race);
					if (m <= 0)
						return 0;
					if (s.standing().max(c, race, fromZero) > 0) {
						return s.standing().get(c, race, global.data(c).getD(race, fromZero))/m;
					}
				}
				
				return global.data(c).getD(race, fromZero);
			}
			
			@Override
			protected void setColor(ColorImp col, int stapleI, double value) {
				int fromZero = STATS.DAYS_SAVED-stapleI-1;
				
				if (!valuev && global instanceof STAT) {
					STAT s = (STAT) global;
					if (s.standing().max(c, race, fromZero) > 0) {
						col.set(GCOLOR.UI().NEUTRAL.normal);
						return;
					}
				}
				col.set(COLOR.WHITE65);
			};
			
			@Override
			protected void setColorBg(ColorImp col, int stapleI, double value) {
				
				col.set(COLOR.WHITE05);
			};
			
		};
		private SETT_STATISTICS global;
		private HCLASS c;
		private Race race;
		private boolean valuev;
		
		UtilGraph(){
			
			staples.body().setDim(250, 64);
			staples.normalize(false);
		}
		
		SPRITE init(HCLASS c, SETT_STATISTICS global, boolean isInt, Race race, boolean isValue){
			valuev = isValue;
			this.c = c;
			this.global = global;
			this.race = race;
			return this;
		}

		@Override
		public int width() {
			return staples.body().width();
		}

		@Override
		public int height() {
			return staples.body().height();
		}

		@Override
		public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
			staples.body().moveX1Y1(X1, Y1);
			staples.render(r, 0);
		}

		@Override
		public void renderTextured(TextureCoords texture, int X1, int X2, int Y1, int Y2) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
}
