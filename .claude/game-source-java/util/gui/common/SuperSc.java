package util.gui.common;

import java.io.File;

import game.save.PROP;
import game.time.TIME;
import init.paths.PATH;
import init.paths.PATHS;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import snake2d.MButt;
import snake2d.Renderer;
import snake2d.SUPER_SCREENSHOT;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.FileManager;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.sprite.text.Str;
import util.data.INT.IntImp;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GHeader;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.slider.GSliderInt;
import util.info.GFORMAT;
import util.text.D;
import util.text.Dic;
import view.interrupter.Interrupter;
import view.main.VIEW;

public final class SuperSc extends GuiSection{


	public static CharSequence ¤¤name = "Super screenshot";
	private static CharSequence ¤¤desc = "A super screenshot generates a large image of the current view. It takes some time. Screenshots are saved in your local files. You can find them through the game launcher.";
	private static CharSequence ¤¤time = "Generate a screenshot every {0} in-game day. Older screenshots will be overwritten by new based on how many screenshots you keep";
	private static CharSequence ¤¤keep = "Save and keep {0} screenshot-files. If file amount is exceeded, they fill be overwritten.";
	private static CharSequence ¤¤sc = "¤Super Screenshot";
	
	private final String fn;
	private final SUPER_SCREENSHOT[] shot;
	
	static {
		D.ts(SuperSc.class);
	}
	
	private final double[] day = new double[] {
		-1,
		16,
		8,
		4,
		2,
		1,
		0.5,
	};
	
	private final IntImp iday;
	private final IntImp saved;
	private final IntImp quality;
	
	double old = 0;
	
	public SuperSc(String fn, SUPER_SCREENSHOT[] shot, String saveKey) {
		
		this.fn = fn;
		this.shot = shot;
		add(new GHeader(¤¤name));
		addRelBody(4, DIR.S, new GText(UI.FONT().M, ¤¤desc).setMaxWidth(400).r(DIR.N));
		
		addRelBody(4, DIR.S, new GButt.ButtPanel(Dic.¤¤Generate + " 1") {
			
			@Override
			protected void clickA() {
				take();
			}
			
		});
		
		iday = new II(saveKey, "DAY", 0, day.length-1, 0);
		saved = new II(saveKey, "SAVED", 0, 400, 100);
		quality = new II(saveKey, "QUALITY", 0, shot.length-1, (shot.length-1)/2);
		
		
		{
			addRelBody(16, DIR.S, new GHeader(Dic.¤¤Quality));
			GuiSection ss = new GuiSection();
			GSliderInt sl = new GSliderInt(quality, 100, false);
			ss.add(sl);
			ss.addRightC(40, new GStat() {
				
				@Override
				public void update(GText text) {
					double s = shot[quality.get()].fileSizeMB();
					text.add('~');
					GFORMAT.f(text, s, 1);
					text.add('M').add('b');
				}
			});
			addRelBody(3, DIR.S, ss);
		}
		
		if (saveKey != null) {
			addRelBody(16, DIR.S, new GHeader(Dic.¤¤Timer));
			
			
			
			GuiSection sl = new GuiSection() {
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					GBox b = (GBox) text;
					
					if (iday.get() > 0) {
						Str.TMP.clear().add(¤¤time).insert(0, day[iday.get()], 1);
						b.text(Str.TMP);
					}else {
						b.text(Dic.¤¤Deactivated);
					}
				}
				
			};
			sl.add(UI.icons().s.clock, 0, 0);
			sl.addRightC(8, new GSliderInt(iday, 200, true));
			addRelBody(4, DIR.S, sl);
			
			
			saved.set(100);
			
			sl = new GuiSection() {
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					GBox b = (GBox) text;
					Str.TMP.clear().add(¤¤keep).insert(0, saved.get());
					b.text(Str.TMP);
				}
				
			};
			sl.add(UI.icons().s.storage, 0, 0);
			sl.addRightC(8, new GSliderInt(saved, 200, false));
			addDown(2, sl);
			
			Interrupter in = new Interrupter(true, true) {
				

				@Override
				protected boolean update(float ds) {
					double dday = day[iday.get()];
					if (dday < 0)
						return true;
					
					double d = TIME.secondsPerDay() *dday;
					
					double day = (TIME.currentSecond()/d)%1.0;
					if (old < 0.5 && day >= 0.5) {
						take();
					}
					old = day;
					
					return true;
				}
				
				@Override
				protected boolean render(Renderer r, float ds) {
					return true;
				}
				
				@Override
				protected void mouseClick(MButt button) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				protected void hoverTimer(GBox text) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				protected boolean hover(COORDINATE mCoo, boolean mouseHasMoved) {
					return false;
				}
			};
			
			VIEW.inters().manager.add(in);
			

		}
		

		
		
	}
	
	private class II extends IntImp {
		
		private final String key;
		
		II(String key, String k2, int min, int max, int def){
			super(min, max);
			if (key != null)
				key = "SUPER_SCREENSHOT_" + key + "_" + k2;
			this.key = key;
			if (this.key != null) {
				def = PROP.propI(key, def);
			}
			super.set(def);
		}
		
		@Override
		public void set(int t) {
			if (key != null && t != get()) {
				
				PROP.propISet(key, t);
			}
			
			super.set(t);
			double dday = day[iday.get()];
			double d = TIME.secondsPerDay() *dday;
			old = (TIME.currentSecond()/d)%1.0;
		}
	}
	
	
	private void take() {
		SPRITES.loader().init();
		SPRITES.loader().print(¤¤sc);
		
		String smallest = null;
		int am = 0;
		long lastM = Long.MAX_VALUE;
		PATH p = PATHS.local().SCREENSHOT_S;
		for (String s : p.getFiles()) {
			if (s.startsWith(fn)) {
				am++;
				long m = p.get(s).toFile().lastModified();
				if (m < lastM) {
					lastM = m;
					smallest = s;
				}
			}
		}
		if (am >= saved.get()) {
			p.delete(smallest);
		}
		
		String f = ""+p.get().toAbsolutePath() + File.separator + fn;
		f = FileManager.NAME.timeStampString(f) + ".jpg";
		
		shot[quality.get()].perform(f);
	}
	
	
}
