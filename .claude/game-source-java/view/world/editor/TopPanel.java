package view.world.editor;

import game.GAME;
import game.save.SaveFile;
import game.time.TIME;
import init.constant.C;
import init.sprite.SPRITES;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import snake2d.MButt;
import snake2d.Renderer;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.STRING_RECIEVER;
import snake2d.util.sprite.SPRITE;
import util.colors.GCOLOR;
import util.data.INT.IntImp;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GText;
import util.gui.slider.GSliderInt;
import util.text.Dic;
import util.text.DicTime;
import view.interrupter.Interrupter;
import view.main.VIEW;
import view.subview.GameWindow;
import view.tool.PLACABLE;
import view.ui.top.UIPanelTop;
import view.world.generator.WorldViewGenerator;
import view.world.generator.tools.UIWorldGenerateTerrain;
import view.world.panel.UIMinimapW;
import world.WORLD;
import world.WORLD.WorldError;
import world.WORLD.WorldResource;

final class TopPanel extends Interrupter {

	private final GuiSection section = new GuiSection();
	private final GuiSection map;
	private final WorldViewEditor w;
	private final CLICKABLE.ClickSwitch current = new CLICKABLE.ClickSwitch(1200, 48);
	
	public TopPanel(WorldViewEditor w) {
		pin();
		current.setD(DIR.N);
		section.body().setDim(C.WIDTH(),UIPanelTop.HEIGHT + Icon.L+6);
		section.body().moveX2(C.WIDTH());
		section.body().moveY2(C.HEIGHT());
		map = new IMinimap(w.window);
		map.body().moveY1(0);
		this.w = w;

		GuiSection buttons = new GuiSection();
		
		{
			CLICKABLE gen = new G(WORLD.TERRAIN()) {

				@Override
				protected void clickA() {
					GuiSection s = new UIWorldGenerateTerrain(WORLD.GEN());
					s.addRelBody(8, DIR.S, new GButt.ButtPanel(Dic.¤¤Generate) {
						@Override
						protected void clickA() {
							WORLD.TERRAIN().saver().generate(WorldViewGenerator.loadPrint);
						}
					});
					VIEW.inters().popup.show(s, this);
				}
			};
			
			buttons.addRightC(0, topButt(WORLD.TERRAIN(), gen));
		}
		
		buttons.addRightC(0, topButt(WORLD.LANDMARKS(), null));
		buttons.addRightC(0, topButt(WORLD.BUILDINGS(), null));
		buttons.addRightC(0, topButt(WORLD.REGIONS(), null));
		buttons.addRightC(0, topButt(WORLD.ENTITIES(), null));
		buttons.addRightC(0, topButt(WORLD.ROADS(), null));
		buttons.addRightC(0, topButt(WORLD.PATH(), null));
		{
			CLICKABLE prime = new GButt.ButtPanel(new SPRITE.Resized(UI.icons().m.arrow_up, Icon.L)) {

				@Override
				protected void clickA() {
					WORLD.RD().prime();
				}
				
			}.hoverInfoSet("grow and build regions and factions");
			
			buttons.addRightC(0, topButt(WORLD.RD(), null, prime));
		}
		
		
		buttons.addRelBody(4, DIR.S, current);
		
		buttons.addRelBody(32, DIR.E, new GButt.ButtPanel(new SPRITE.Resized(UI.icons().m.crossair, Icon.L)) {

			@Override
			protected void clickA() {
				
				if (WORLD.REGIONS().player.active())
					VIEW.world().editor.window.centererTile.set(WORLD.REGIONS().player.cx(), WORLD.REGIONS().player.cy());
			}
			
		}.hoverInfoSet("go to player capitol"));
		
		
		buttons.addRelBody(32, DIR.E, new GButt.ButtPanel(new SPRITE.Resized(UI.icons().m.time, Icon.L)) {
			
			IntImp tt = new IntImp(0, 1000) {
				
				@Override
				public void set(int t) {
					super.set(t);
					TIME.set(get()*TIME.secondsPerDay());
				};
				
			};
			
			IntImp th = new IntImp(0, TIME.hoursPerDay()) {
				
				@Override
				public void set(int t) {
					super.set(t);
					TIME.set(tt.get()*TIME.secondsPerDay() + getD()*TIME.secondsPerDay());
				};
				
			};
			
			GuiSection s = new GuiSection();
			
			{
				s.add(new GSliderInt(tt, 100, true) {
					
					@Override
					public void hoverInfoGet(GUI_BOX text) {
						GBox b = (GBox) text;
						GText t = b.text();
						DicTime.setDate(t, (int) TIME.currentSecond());
						b.add(t);
					}
					
				});
				
				s.addDownC(4, new GSliderInt(th, 100, true) {
					
					@Override
					public void hoverInfoGet(GUI_BOX text) {
						GBox b = (GBox) text;
						GText t = b.text();
						DicTime.setDate(t, (int) TIME.currentSecond());
						b.add(t);
					}
					
				});
			}
			
			@Override
			protected void clickA() {
				
				VIEW.inters().popup.show(s, this);
			}
			
		}.hoverInfoSet("set time"));
		
		
		buttons.addRelBody(0, DIR.E, new GButt.ButtPanel(new SPRITE.Resized(UI.icons().m.arrow_right, Icon.L)) {

			@Override
			protected void clickA() {
				
				WorldError e = new WorldError();

				
				for (int ri = 0; ri < WORLD.RESOURCES().size(); ri++) {
					WorldResource r = WORLD.RESOURCES().get(ri);
					WorldViewGenerator.loadPrint.exe();
					r.saver().validateInit(e);
					if (e.problem != null) {
						
						VIEW.inters().yesNo.activate("ERROR: " + e.problem, ACTION.NOP, null, true);
						VIEW.world().editor.window.centererTile.set(e.coo);
						return;
					}
					
				}
				
				if (e.warning != null) {
					VIEW.inters().yesNo.activate("WARNING: " + e.warning, ACTION.NOP, null, true);
					VIEW.world().editor.window.centererTile.set(e.coo);
				}
			}
			
		}.hoverInfoSet("validate"));
		
		buttons.addRelBody(0, DIR.E, new GButt.ButtPanel(new SPRITE.Resized(UI.icons().m.menu2, Icon.L)) {

			@Override
			protected void clickA() {
				
				WorldError e = new WorldError();
				
				WORLD.GEN().playerX = -1;
				WORLD.GEN().hasGeneratedTerrain = false;
				
				for (int ri = 0; ri < WORLD.RESOURCES().size(); ri++) {
					WorldResource r = WORLD.RESOURCES().get(ri);
					WorldViewGenerator.loadPrint.exe();
					r.saver().validateInit(e);
					
					if (e.problem != null) {
						
						
						break;
						
					}else if (r == WORLD.TERRAIN()) {
						WORLD.GEN().hasGeneratedTerrain = true;
					}
					
					
				}
				
				if (e.problem != null) {
					if (WORLD.GEN().hasGeneratedTerrain) {
						STRING_RECIEVER ss = new STRING_RECIEVER() {
							
							@Override
							public void acceptString(CharSequence string) {
								if (string != null) {
									string = SaveFile.stamp(string);
									WORLD.GEN().isEditing = false;
									GAME.saver().save(""+string);
									WORLD.GEN().isEditing = true;
									VIEW.inters().yesNo.activate("The world terrain was saved. The other layers were invalid, so could not be saved.", ACTION.NOP, null, true);
								}
								
							}
						};
						VIEW.inters().input.requestInput(ss, "name save");
					}else {
						VIEW.inters().yesNo.activate("can't save: " + e.problem, ACTION.NOP, null, true);
						VIEW.world().editor.window.centererTile.set(e.coo);
						
						
						
						
					}
				}else {
					WORLD.GEN().playerX = WORLD.REGIONS().player.cx();
					WORLD.GEN().playerY = WORLD.REGIONS().player.cy();
					STRING_RECIEVER ss = new STRING_RECIEVER() {
						
						@Override
						public void acceptString(CharSequence string) {
							if (string != null) {
								string = SaveFile.stamp(string);
								WORLD.GEN().isEditing = false;
								GAME.saver().save(""+string);
								WORLD.GEN().isEditing = true;
								VIEW.inters().yesNo.activate("Save was a stunning success.", ACTION.NOP, null, true);
							}
							
						}
					};
					VIEW.inters().input.requestInput(ss, "name save");
				}
				
				
				
				
				
				
			}
			
		}.hoverInfoSet("save as playable map"));
		
		buttons.body().centerIn(section);
		section.add(buttons);
		
		
		show(w.uiManager);
	}

	private class G extends GButt.ButtPanel {
		
		WorldResource res;
		
		G(WorldResource res){
			super(UI.icons().m.rotate.resized(Icon.L));
			hoverTitleSet("Generate: " + res.name);
			this.res = res;
		}
		
		final ACTION a = new ACTION() {

			@Override
			public void exe() {
				res.saver().clear();
				res.saver().generate(WorldViewGenerator.loadPrint);
			}
			
		};
		
		@Override
		protected void clickA() {
			VIEW.inters().yesNo.activate("Are you sure you wish to remove progress and randomly generate layer?",a, ACTION.NOP, true);
		}
		
	}
	
	private class V extends GButt.ButtPanel {
		
		WorldResource res;
		
		V(WorldResource res){
			super(UI.icons().m.arrow_right.resized(Icon.L));
			hoverTitleSet("validate: " + res.name);
			this.res = res;
		}
		
		@Override
		protected void clickA() {
			
			WorldError e = new WorldError();
			
			res.saver().validateInit(e);
			
			if (e.problem != null) {
				error(e);
			}else if (e.warning != null){
				VIEW.inters().yesNo.activate("WARNING: " + e.warning, ACTION.NOP, ACTION.NOP, true);
			}
			else {
				VIEW.inters().yesNo.activate(res.name + " is OK!", ACTION.NOP, ACTION.NOP, true);
			}
		}
		
	}
	
	private void error(WorldError e) {
		if (e.problem != null) {
			VIEW.inters().yesNo.activate("ERROR: " + e.problem, ACTION.NOP, null, true);
			VIEW.world().editor.window.centererTile.set(e.coo);
		}		
	}
	
	private class Clear extends GButt.ButtPanel {
		
		WorldResource res;
		
		Clear(WorldResource res){
			super(UI.icons().m.cancel.resized(Icon.L));
			hoverTitleSet("clear everything: " + res.name);
			this.res = res;
		}
		
		final ACTION a = new ACTION() {

			@Override
			public void exe() {
				res.saver().clear();
			}
			
		};
		
		@Override
		protected void clickA() {
			VIEW.inters().yesNo.activate("Are you sure you wish to completely clear this layer?",a, ACTION.NOP, true);
		}

		
	}
	
	private class B extends GButt.ButtPanel {
		
		private final PLACABLE p;
		
		B(PLACABLE p){
			super(p.getIcon().resized(Icon.L));
			this.p = p;
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			text.title(p.name());
		}
		
		@Override
		protected void renAction() {
			selectedSet(w.tools.placer.isActivated() && (w.tools.placer.getCurrent() == p));
		}
		
		@Override
		protected void clickA() {
			w.tools.place(p);
		}
		
	}
	
	private CLICKABLE topButt(WorldResource res, CLICKABLE gen,  CLICKABLE... extra) {
		
		GuiSection row = new GuiSection();
		
		
		PLACABLE pfirst = null;
		for (PLACABLE p : res.saver().makePlacers(w.tools)) {
			if (pfirst == null)
				pfirst = p;
			row.addRightC(0, new B(p));
		}
		
		if (gen == null) {
			gen = new G(res);
		}
		
		row.addRightC(16, gen);
		
		row.addRightC(0, new V(res));
		row.addRightC(0, new Clear(res));
		
		if (current.current() == null)
			current.set(row);
		final PLACABLE pp = pfirst;
		CLICKABLE cc = new GButt.ButtPanel(res.name) {
			
			@Override
			protected void renAction() {
				selectedSet(current.current() == row);
				if (selectedIs())
					res.saver().addDebugView();
			}
			
			@Override
			protected void clickA() {
				current.set(row);
				VIEW.world().editor.tools.place(pp);
			}
		};
		return cc;
		
	}

	@Override
	public boolean render(Renderer r, float ds) {
//
//		manager().viewPort().moveY1(section.body().y2());
		if (manager().viewPort().y2() >= C.HEIGHT() - section.body().height()) {
			manager().viewPort().setHeight(C.HEIGHT() - section.body().height());
		}

		map.render(r, ds);
		GCOLOR.UI().panBG.render(r, section.body());
		section.render(r, ds);
		
		GCOLOR.UI().border(r, 0, C.WIDTH(), section.body().y1(), section.body().y1()+3);
		return true;
	}

	public void hide(boolean yes) {
		section.visableSet(yes);
	}

	@Override
	protected boolean hover(COORDINATE mCoo, boolean mouseHasMoved) {
		return section.hover(mCoo) | map.hover(mCoo) || mCoo.touchesRec(section);
	}

	@Override
	protected void mouseClick(MButt button) {
		if (button == MButt.LEFT) {
			section.click();
			map.click();
		}
	}

	@Override
	protected void hoverTimer(GBox text) {
		section.hoverInfoGet(text);
		map.hoverInfoGet(text);;
	}

	@Override
	protected boolean update(float ds) {

		return true;
	}

	public static int y2() {
		return 48 - 2;
	}
	
	private static class IMinimap extends GuiSection{

		private final UIMinimapW map;
		private final GuiSection buttons = new GuiSection();
		
		public IMinimap(GameWindow w){
			
			map = new UIMinimapW(w);
			
			CLICKABLE b;

			
			
			
			
			b = new GButt.Panel(SPRITES.icons().m.plus) {
				@Override
				protected void clickA() {
					if (w.zoomout() > 0)
						w.setZoomout(w.zoomout()-1);
					//inters.miniview.toggle();
				};
				@Override
				protected void renAction() {
					activeSet(w.zoomout() > 0);
				}
			};
			buttons.addRightC(32, b);
			
			b = new GButt.Panel(SPRITES.icons().m.minus) {
				@Override
				protected void clickA() {
					if (w.zoomout() < 3)
						w.setZoomout(w.zoomout()+1);
					//inters.miniview.toggle();
				};
				@Override
				protected void renAction() {
					activeSet(w.zoomout() < 3);
				}
			};
			buttons.addRightC(0, b);

			RENDEROBJ pan = new RENDEROBJ.RenderImp(map.body().width(), 32) {
				
				@Override
				public void render(SPRITE_RENDERER r, float ds) {
					UI.PANEL().butt.render(r, body, 0, DIR.S, DIR.W);
				}
			};
			
//			GuiSection pan = new GuiSection();
//			pan.add(UI.PANEL().panelL.get(DIR.N, DIR.E), 0, 0);
//			while(pan.body().width() <= map.body().width()+UI.PANEL().panelL.dim())
//				pan.addRightC(0, UI.PANEL().panelL.get(DIR.N, DIR.E, DIR.W));
			
			buttons.body().moveX2(C.WIDTH()-4);
			buttons.body().moveY1(0);
			pan.body().moveX2(C.WIDTH());
			buttons.add(pan);
			buttons.moveLastToBack();
			
			add(buttons);

			map.body().moveY1(buttons.body().y2());
			map.body().moveX2(C.DIM().width());
			add(map);
		}

		
	}
	
}
