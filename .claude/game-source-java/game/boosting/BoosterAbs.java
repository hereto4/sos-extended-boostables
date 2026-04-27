package game.boosting;

import snake2d.util.color.COLOR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.sprite.SPRITE;
import util.colors.GCOLOR;
import util.gui.misc.GBox;
import util.gui.misc.GText;
import util.info.GFORMAT;

public abstract class BoosterAbs<T> {

	public final BSourceInfo info;
	public final boolean isMul;
	private static final int htab = 7;
	
	public BoosterAbs(BSourceInfo info, boolean isMul){
		
		this.info = info;
		this.isMul = isMul;
	}
	
	public abstract double from();
	
	public abstract double to();
	
	public double min() {
		return Math.min(from(), to());
	}
	
	public double max() {
		return Math.max(from(), to());
	}
	
	public abstract double getValue(double input);
	
	public double get(T o) {
		return getValue(pget(o));
	}
	
	protected abstract double pget(T o);

//	public abstract boolean has(Class<?> b);
	
	public boolean isPositive(double input) {
		return (isMul && getValue(input) >= 1) || getValue(input) > 0;
	}
	

	
	public void hoverSpan(GUI_BOX box, T o) {
		hoverSpan(box, get(o));
	}
	
	public void hoverSpan(GUI_BOX box, double value) {
		hoverSpan(box, from(), to());
	}
	
	public void hover(GUI_BOX box, double value) {
		hover(box, value, 0);
	}
	
	public void hover(GUI_BOX box, double d, int tab) {
		hover(box, d, tab, info.icon, isMul, info.name);
	}
	
	public static void hover(GUI_BOX box, double d, int tab, SPRITE icon, boolean isMul, CharSequence name) {

		GBox b = (GBox) box;
		b.tab(tab * (htab + 2));
		b.add(icon);
		
		if (!isMul) {
			COLOR c = GCOLOR.T().INACTIVE;
			if (d < 0)
				c = GCOLOR.T().IBAD;
			else if (d > 0)
				c = GCOLOR.T().IGOOD;

			
			b.add(b.text().color(c).add(name));
			b.tab(tab * (htab + 2) + htab);

			GText t = b.text();
			if (d == (int) d)
				GFORMAT.iIncr(t, (int) d);
			else
				GFORMAT.f0(t, d);
			t.color(c);
			b.add(t);

		} else {
			COLOR c = GCOLOR.T().INACTIVE;
			if (d < 1)
				c = GCOLOR.T().IBAD;
			else if (d > 1)
				c = GCOLOR.T().IGOOD;
			b.add(b.text().color(c).add(name));
			b.tab(tab * (htab + 2) + htab);

			GText t = b.text();
			t.add('*');
			GFORMAT.f1(t, d);
			t.color(c);
			b.add(t);

		}
	}
	
	public static void hoverSpan(GUI_BOX box, double from, double to) {
		
		GBox b = (GBox) box;
		b.tab(9);
		
		GText t = b.text();
		t.color(COLOR.WHITE65);
		
		t.add('(');
		t.add(from);
		b.add(t);
		b.tab(11);
		
		t = b.text();
		t.color(COLOR.WHITE65);
		
		t.add('<').add('-').add('>').s();
		t.add(to);
		t.add(')');
		b.add(t);
		b.NL();
		
	}
	
	public GText format(GText t, double value) {
		return format(t, value, isMul);
	}
	
	public static GText format(GText t, double value, boolean isMul) {
		
		
		
		if (isMul) {
			t.add('*').s();
			GFORMAT.f1(t, value);
		}else {
			if (value == (int) value)
				GFORMAT.iIncr(t, (int)value);
			else
				GFORMAT.f0(t, value);
		}
		return t;
	}


	
}
