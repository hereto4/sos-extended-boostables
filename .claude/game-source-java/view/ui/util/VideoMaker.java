package view.ui.util;

import java.io.File;

import game.GAME;
import init.paths.PATHS;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import snake2d.CORE;
import snake2d.LOG;
import snake2d.MButt;
import snake2d.Renderer;
import snake2d.VIDEO_MAKER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Coo;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.datatypes.Rec;
import snake2d.util.gui.GuiSection;
import snake2d.util.misc.ACTION;
import util.data.INT.IntImp;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GInputInt;
import util.gui.misc.GText;
import view.interrupter.IDebugPanel;
import view.interrupter.Interrupter;
import view.main.VIEW;
import view.sett.ui.minimap.UIMinimapSettConfig;
import view.subview.GameWindow;
import world.WORLD;

public class VideoMaker {
	
	private final Placer start = new Placer("start area");
	private final Placer end = new Placer("end area");
	private final GuiSection s = new GuiSection();
	private final Mode world;
	private final Mode sett;
	private final Mode battle;
	
	private final IntImp duration = new IntImp();
	
	private Mode current;
	
	public VideoMaker() {
		
		world = new Mode() {
			

			@Override
			public GameWindow window() {
				return VIEW.world().window;
			}

			@Override
			public void render(RECTANGLE bounds) {
				WORLD.OVERLAY().hide();
				boolean t = WORLD.FOW().toggled.is();
				WORLD.FOW().toggled.set(false);
				GAME.world().render(CORE.renderer(), 0, 0, bounds, 0, 0);
				WORLD.FOW().toggled.set(t);
			}
		};
		
		sett = new Mode() {
			
			@Override
			public GameWindow window() {
				return VIEW.s().getWindow();
			}
			
			@Override
			public void render(RECTANGLE bounds) {
				GAME.s().render(CORE.renderer(), 0, 0, bounds, 0, 0, UIMinimapSettConfig.NORMAL);
			}
		};
		
		battle = new Mode() {
			
			@Override
			public GameWindow window() {
				return VIEW.b().getWindow();
			}
			
			@Override
			public void render(RECTANGLE bounds) {
				GAME.s().render(CORE.renderer(), 0, 0, bounds, 0, 0, UIMinimapSettConfig.NORMAL);
			}
			
		};
		
		
		for (Placer p : new Placer[] {start, end}) {
			GuiSection rr = new GuiSection();
			
			rr.add(new GText(UI.FONT().S, p.name), 0, 0);
			
			rr.addRightCAbs(100, new GButt.ButtPanel(UI.icons().m.crossair) {
				
				@Override
				protected void clickA() {
					p.set();
				}
				
			});
			
			
			rr.addRightC(16, UI.FONT().S.getText("x1"));
			rr.addRightC(4, new GInputInt(p.x1));
			
			rr.addRightC(16, UI.FONT().S.getText("width"));
			rr.addRightC(4, new GInputInt(p.w));
			
			rr.addRightC(16, UI.FONT().S.getText("y1"));
			rr.addRightC(4, new GInputInt(p.y1));
			
			rr.addRightC(16, UI.FONT().S.getText("height"));
			rr.addRightC(4, new GInputInt(p.h));
			
			s.addDown(4, rr);
		}
		
		{
			GuiSection rr = new GuiSection();
			rr.addRightC(16, UI.FONT().S.getText("duration (ms)"));
			rr.addRightC(4, new GInputInt(duration));
			s.addDown(4, rr);
		}
		
		s.addRightC(32, new GButt.ButtPanel("action!") {
			
			@Override
			protected void clickA() {
				start.rec.moveX1Y1(start.x1.get(), start.y1.get()).setDim(start.w.get(), start.h.get());
				end.rec.moveX1Y1(end.x1.get(), end.y1.get()).setDim(end.w.get(), end.h.get());
				
				String f = ""+PATHS.local().VIDEO.get().toAbsolutePath() + File.separator + "frame";
				SPRITES.loader().init();
				new VIDEO_MAKER(start.rec, end.rec, duration.get(), f) {

					@Override
					public void render(RECTANGLE gamebounds) {
						current.render(gamebounds);
						
					}

					@Override
					public void renderProgress(int frame, int totFrames, double frameTime) {
						SPRITES.loader().print("frame " + (frame+1) + "/" + (totFrames+1));
						GAME.update(frameTime);
					}
					
			
				};
			}
			
		});
		
		IDebugPanel.add("video maker", new ACTION() {
			
			@Override
			public void exe() {
				
				current = null;
				if (VIEW.s().isActive())
					current = sett;
				else if (VIEW.b().isActive())
					current = battle;
				else if (VIEW.world().isActive())
					current = world;
				if (current == null) {
					LOG.ln("can't make video for view: " + VIEW.current());
				}
				VIEW.inters().popup.show(s, null);
			}
		});
		
	}
	
	private static abstract class Mode {
		
		Mode(){
			
		}

		public abstract GameWindow window();
		
		public abstract void render(RECTANGLE bounds);
	}

	
	private class Placer extends Interrupter {
		
		
		public final String name;
		private final Rec rec = new Rec();
		private final Rec tmp = new Rec();
		public final IntImp x1 = new IntImp();
		public final IntImp y1 = new IntImp();
		public final IntImp w = new IntImp();
		public final IntImp h = new IntImp();
		private boolean clicked = false;
		private Coo start = new Coo();
		
		public Placer(String name) {
			this.name = name;
		}
		
		void set() {
			clicked = false;
			VIEW.inters().manager.add(this);
		}
		
		@Override
		protected boolean hover(COORDINATE mCoo, boolean mouseHasMoved) {
			return true;
		}

		@Override
		protected void mouseClick(MButt button) {
			if (button == MButt.LEFT && !clicked) {
				clicked = true;
				start.set(current.window().pixel().x(), current.window().pixel().y());
				
			}else {
				hide();
			}
		}

		@Override
		protected void hoverTimer(GBox text) {
			// TODO Auto-generated method stub
			
		}

		@Override
		protected boolean render(Renderer r, float ds) {
			if (!clicked)
				return true;
			
			GameWindow w = current.window();
			
			
			int x1 = rec.x1();
			int y1 = rec.y1();
			
			x1 -= w.pixels().x1();
			y1 -= w.pixels().y1();
			
			x1 = x1 >> w.zoomout();
			y1 = y1 >> w.zoomout();
			
			x1 += w.viewWindow().x1();
			y1 += w.viewWindow().y1();
			
			tmp.moveX1Y1(x1, y1);
			tmp.setDim(rec.width()>>w.zoomout(), rec.height()>>w.zoomout());
			
			COLOR.GREEN100.renderFrame(r, tmp, 0, 3);
			
			return true;
		}

		@Override
		protected boolean update(float ds) {
			if (clicked) {
				rec.setDim(1);
				rec.moveX1Y1(start);
				rec.unify(current.window().pixel().x(), current.window().pixel().y());
				
				int cs = rec.cX();
				int cy = rec.cY();
				
				double d = (double)CORE.getGraphics().nativeHeight / CORE.getGraphics().nativeWidth;
				rec.setHeight(rec.width()*d);
				
				rec.moveC(cs, cy);
				
				if (!MButt.LEFT.isDown()) {
					clicked = false;
					
					x1.set(rec.x1());
					y1.set(rec.y1());
					w.set(rec.width());
					h.set(rec.height());
					
					
					hide();
					VIEW.inters().popup.show(s, null);
				}
			}
			
			
			
			
			
			return false;
		}
		
		
		
	}
	
}
