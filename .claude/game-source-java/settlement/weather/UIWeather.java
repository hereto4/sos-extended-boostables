package settlement.weather;

import snake2d.util.misc.ACTION;
import util.data.INT.INTE;
import util.gui.misc.GHeader;
import util.gui.slider.GSliderInt;
import view.interrupter.ISidePanel;
import view.main.VIEW;
import view.sett.IDebugPanelSett;

final class UIWeather extends ISidePanel{

	public UIWeather(SWEATHER w){
		
		titleSet("weather");
		
		for (WeatherThing ww : w.all())
			add(ww);
		
		
		IDebugPanelSett.add("weather", new ACTION() {
			
			@Override
			public void exe() {
				VIEW.s().panels.add(UIWeather.this, true);
			}
		});
		
	}
	
	private void add(WeatherThing t) {
		section.add(new GHeader(t.info().name), 0, section.body().y2()+2);
		INTE in = new INTE() {
			
			@Override
			public int min() {
				return 0;
			}
			
			@Override
			public int max() {
				return 100;
			}
			
			@Override
			public int get() {
				return (int) Math.round(t.getD()*100);
			}
			
			@Override
			public void set(int ti) {
				t.setD(ti/100.0);
			}
		};
		section.add(new GSliderInt(in, 120, 16, false), 220, section.getLastY1());
	}
	
}
