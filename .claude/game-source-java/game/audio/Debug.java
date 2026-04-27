package game.audio;

import java.util.Arrays;

import init.constant.C;
import snake2d.SPRITE_RENDERER;
import snake2d.SoundStream;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.LinkedList;
import util.data.INT.INTE;
import util.gui.misc.GButt;
import util.gui.misc.GHeader;
import util.gui.slider.GSliderInt;
import util.gui.table.GScrollRows;
import view.interrupter.IDebugPanel;
import view.main.VIEW;

final class Debug {

	Debug(){
		
		IDebugPanel.add("AUDION: MUSIC SHUFFLE", new ACTION() {
			@Override
			public void exe() {
				AUDIO.music().next();
			}
		});
		
		IDebugPanel.add("AUDIO: AMBIENCE", new ACTION() {
			
			@Override
			public void exe() {
				VIEW.inters().popup.show(ambience(), null);
			}
		});
		
		IDebugPanel.add("AUDIO: STREAMS", new ACTION() {
			
			@Override
			public void exe() {
				VIEW.inters().popup.show(streams(), null);
			}
		});
	}
	
	
	private GuiSection ambience() {
		
		
		//LinkedList<AAA> all = new LinkedList<>();
		
		LinkedList<RENDEROBJ> rows = new LinkedList<>();
		
		final double[] priority = new double[AUDIO.AMBI().all().size()];
		final double[] gains = new double[AUDIO.AMBI().all().size()];
		Arrays.fill(gains, 1.0);
		
		for (Ambiance a : AUDIO.AMBI().all()) {

			GuiSection row = new GuiSection();
			
			row.add(new GHeader(a.key()));
			
			INTE prio = new INTE() {
				
				@Override
				public int min() {

					return 0;
				}
				
				@Override
				public int max() {
					return 200;
				}
				
				@Override
				public int get() {
					return (int) (priority[a.index()]*100);
				}

				@Override
				public void set(int t) {
					priority[a.index()] = t/100.0;
				}
			};
			
			row.addRightCAbs(300, new GSliderInt(prio, 100, true));
			
			INTE gain = new INTE() {
				
				@Override
				public int min() {

					return 0;
				}
				
				@Override
				public int max() {
					return 200;
				}
				
				@Override
				public int get() {
					return (int) (gains[a.index()]*100);
				}

				@Override
				public void set(int t) {
					gains[a.index()] = t/100.0;
				}
			};
			
			row.addRightC(16, new GSliderInt(gain, 100, true));
			
			rows.add(row);
		}
		
		GuiSection s = new GuiSection() {
			
			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				AUDIO.AMBI_UP().debugPrio = priority;
				AUDIO.AMBI_UP().debugGain = gains;
				super.render(r, ds);
				
			};
		};
		
		s.add(new GScrollRows(rows, C.HEIGHT()-200).view());
		
		return s;
	}
	
	private GuiSection streams() {

		LinkedList<RENDEROBJ> rows = new LinkedList<>();
		
		final double[] priority = new double[AUDIO.AMBI().all().size()];
		final double[] gains = new double[AUDIO.AMBI().all().size()];
		Arrays.fill(gains, 1.0);
		
		for (String s : AUDIO.AMBI().factory.map().keysSorted()) {
			SoundStream st = AUDIO.AMBI().factory.map().get(s);
			
			rows.add(new GButt.ButtPanel(s) {
				@Override
				protected void clickA() {
					st.playOnce();
					super.clickA();
				}
				
			});
		}
		
		GuiSection s = new GuiSection() {
			
			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				AUDIO.AMBI_UP().debugPrio = priority;
				AUDIO.AMBI_UP().debugGain = gains;
				super.render(r, ds);
				
			};
		};
		
		s.add(new GScrollRows(rows, C.HEIGHT()-200).view());
		
		return s;
	}

}
