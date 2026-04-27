package game.time;

import snake2d.CORE;
import snake2d.util.color.RGB;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.gui.GuiSection;
import snake2d.util.light.AmbientLight;
import snake2d.util.misc.ACTION;
import util.data.DOUBLE;
import util.data.INT;
import util.gui.slider.GSliderInt;
import view.interrupter.IDebugPanel;
import view.main.VIEW;

public final class Light {

	private boolean isNight = false;
	private double partOf;
	private double partOfCircular;
	
	private final Ambient abient = new Ambient();
	public final LightShadows shadow = new LightShadows();
	private final AmbientLight room = new AmbientLight(0.7, 0.5, 0.3, 0, 20);
	private final Gui gui = new Gui();
	
	DOUBLE time = new DOUBLE() {
		
		@Override
		public double getD() {
			return TIME.days().bitPartOf();
		}
	};
	
	Light(){
		
		ACTION a = new ACTION() {
			
			@Override
			public void exe() {
				INT.IntImp in = new INT.IntImp(0, 300);
				time = in;
				GuiSection s = new GuiSection();
				s.add(new GSliderInt(in, 300, true));
				VIEW.inters().popup.show(s, s, true);
				
			}
		};
		IDebugPanel.add("Light Test", a);
		
	}
	
	public void bindRoom() {
		room.setTilt(20);
		room.setDir(180);
		double roomI = 1.0;
		if (dayIs()) {
			roomI = 1.0-partOfCircular();
			roomI *= roomI;
		}
		room.r(0.6*roomI);
		room.g(0.3*roomI);
		room.b(0.1*roomI);
		CORE.renderer().lightDepthSet((byte) 127);
		CORE.renderer().setTileLight(room);
	}
	
//	public void applyWorld(int x1, int x2, int y1, int y2, double season) {
//		moon.apply(x1, x2, y1, y2);
//		sun.apply(x1, x2, y1, y2, season);
//		
//	}
//	
//	public void applyWorld(RECTANGLE rec, double season) {
//		applyWorld(rec.x1(), rec.x2(), rec.y1(), rec.y2(), season);
//	}
	
	public void apply(RECTANGLE rec, RGB mask) {
		apply(rec.x1(), rec.x2(), rec.y1(), rec.y2(), mask);
	}
	
	public void apply(int x1, int x2, int y1, int y2, RGB mask) {
		abient.apply(x1, x2, y1, y2, mask);
	}
	
	public void applyGuiLight(float ds, RECTANGLE rec) {
		gui.register(ds, rec);
	}
	
	public void applyGuiLight(float ds, int x1, int x2, int y1, int y2) {
		gui.register(ds, x1, x2, y1, y2);
	}
	
	public boolean dayIs() {
		return !isNight;
	}
	
	public boolean nightIs() {
		return isNight;
	}
	
	public double partOf() {
		return partOf;
	}
	
	public double partOfCircular() {
		return partOfCircular;
	}
	

	void update(double ds) {

		double dayL = TIME.seasons().currentDay.dayLength();
		double nightL = 1.0-dayL;
		double now = time.getD();
		
		double dawn = nightL/2.0;
		double dusk = dawn + dayL;
		
		if (now <= dawn) {
			partOf = 0.5+0.5*now/dawn;
			isNight = true;
		}else if (now <= dusk) {
			partOf = (now-dawn)/dayL;
			isNight = false;
		}else {
			partOf = 0.5*(now-dusk)/(nightL/2);
			isNight = true;
		}
		
		if (partOf <= 0.5) {
			partOfCircular = partOf*2.0;
		}else {
			partOfCircular = 1.0 - (partOf-0.5)*2.0;
		}
		
		shadow.update(this);
		gui.update(this, ds);

	}
	
}
