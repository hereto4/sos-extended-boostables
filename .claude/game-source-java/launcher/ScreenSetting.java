
package launcher;

import init.constant.C;
import launcher.GUI.BSprite;
import launcher.GUI.BText;
import launcher.GUI.ScrollBox;
import launcher.LSettings.LSettingInt;
import snake2d.Displays;
import snake2d.Displays.DisplayMode;
import snake2d.SPRITE_RENDERER;
import snake2d.SoundDevices;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.misc.ACTION;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.text.Str;
import snake2d.util.sprite.text.Text;
import util.text.D;

class ScreenSetting extends GuiSection{

	{
		D.gInit(this);
	}
	
	private final Launcher l;
	
	private final GuiSection fullScreen = new GuiSection() {
		@Override
		public void render(SPRITE_RENDERER r, float ds) {
			visableSet(l.s.screenMode.get() == LSettings.screenModeFull);
			if (visableIs())
				super.render(r, ds);
		}
	};
	private final GuiSection windowed  = new GuiSection() {
		
		@Override
		public void render(SPRITE_RENDERER r, float ds) {
			visableSet(l.s.screenMode.get() == LSettings.screenModeWindowed);
			if (visableIs())
				super.render(r, ds);
		}
	};
	private final GuiSection borderless  = new GuiSection() {
		
		@Override
		public void render(SPRITE_RENDERER r, float ds) {
			visableSet(l.s.screenMode.get() == LSettings.screenModeBorderLess);
			if (visableIs())
				super.render(r, ds);
		}
	};
	
	private final ScrollBox content;
	
	private GuiSection message;
	private final GuiSection mFullScreens;
	
	private Str hoverInfo = new Str(200);
	
	ScreenSetting(Launcher l){
		
		this.l = l;
		final int x1 = 40;
		
		CharSequence sback = D.g("Back");
		
		{
			
			GuiSection s = new GuiSection();
			int i = 0;
			int cols = 5;
			int wi = 170;
			int hi = 52;
			
			CLICKABLE c = new CheckBox(l.s.debug, D.g("Debug"), D.g("debugD", "Starts the game in debug mode and will print extra information and diagnostics when enabled at the cost of performance. Helpful when modding."));
			s.addGridD(c, i++, cols, wi, hi, DIR.NW);
			
			c = new CheckBox(l.s.developer, D.g("Developer"), D.g("DeveloperD", "Enables powerful tools in game that can be used to test things, or cheat."));
			s.addGridD(c, i++, cols, wi, hi, DIR.NW);
			

			c = new CheckBox(l.s.linear, D.g("Linear"), D.g("LinearD", "Enables linear filtering when the game is scaled."));
			s.addGridD(c, i++, cols, wi, hi, DIR.NW);
			
//			if (OS.get() != OS.MAC) {
//				c = new CheckBox(l.s.rpc, D.g("RPC"), D.g("RPCD", "Enables rich presence, that will send out information to other applications such as discord about your game for others to see."));
//				s.addGridD(c, i++, cols, wi, hi, DIR.NW);
//			}
			
			c = new CheckBox(l.s.shading, D.g("Shading"), D.g("ShadingD", "Use normal maps and dynamic lightening"));
			s.addGridD(c, i++, cols, wi, hi, DIR.NW);
			
			c = new CheckBox(l.s.vsync, D.g("VSync"), D.g("VSyncD", "Enables vsync to reduce screen tearing. Can cause conflicts with NVidia GSync in which case it's recommended to turn that off."));
			s.addGridD(c, i++, cols, wi, hi, DIR.NW);
			
			c = new CheckBox(l.s.vsyncadapt, D.g("VSync-Adapt"), D.g("VSyncAD", "Enables vsync that adapts to the current refresh rate. Only supported on some GPUs"));
			s.addGridD(c, i++, cols, wi, hi, DIR.NW);
			
			c = new CheckBox(l.s.winIconi, D.g("Iconify"), D.g("IconifyD", "If full screen is selected, allow the window to iconify if it loses focus. When using multiple monitors, this should be turned off."));
			s.addGridD(c, i++, cols, wi, hi, DIR.NW);
			
			c = new CheckBox(l.s.winFullFull, D.g("W-Fix"), D.g("MFixD", "A possible fix for borderless window and tabbing out if you have multiple monitors."));
			s.addGridD(c, i++, cols, wi, hi, DIR.NW);
			
			c = new CheckBox(l.s.winFoat, D.g("W-Float"), D.g("WFloatD", "Determines how the window floats on top of other applications. Might work different on different configurations."));
			s.addGridD(c, i++, cols, wi, hi, DIR.NW);
			
			c = new CheckBox(l.s.easy, D.g("UI-Easy"), D.g("UI-EasyD", "Only works with the default english language. Replaces the fonts with open sans, and tweaks UI colors to make things more clear."));
			s.addGridD(c, i++, cols, wi, hi, DIR.NW);
			
			CharSequence none = D.g("none");
			CharSequence def = D.g("default");
			
			SPRITE ss = new SPRITE.Imp(250, l.res.font.height()+12) {
				Text t = new Text(l.res.font, 64);
				CharSequence a = D.g("Audio");
				
				
				@Override
				public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
					
					t.clear().add(a).add(':').s();
					t.add(get());
					t.setMaxWidth(250);
					t.setMultipleLines(false);
					t.adjustWidth();
					t.renderC(r, X1+(X2-X1)/2, Y1+(Y2-Y1)/2);
				}
				
				private CharSequence get() {
					String a = l.s.audiodevice.get();
					if (a == null) {
						return none;
					}else if(a.isEmpty()) {
						return def;
					}else {
						for (String s : SoundDevices.get()) {
							if (s.equalsIgnoreCase(a)) {
								return s;
							}
						}

						if (SoundDevices.get().size() > 0)
							return SoundDevices.get().get(0);
						
						return none;
					}

					
				}
			};
			
			c = new GUI.Button(ss) {
				
				@Override
				protected void clickA() {
					GuiSection mFullScreens = new GuiSection();
					ScrollBox content = new ScrollBox(Sett.HEIGHT-100);
					
					CLICKABLE up = new BSprite(l.res.arrowUpDown[0]).clickActionSet(new ACTION() {
						@Override
						public void exe() {
							content.scrollUp();
						}
					});
					mFullScreens.add(up);
					
					
					CLICKABLE down = new BSprite(l.res.arrowUpDown[1]).clickActionSet(new ACTION() {
						@Override
						public void exe() {
							content.scrollDown();
						}
					});
					down.body().moveY2(Sett.HEIGHT-120);
					
					mFullScreens.add(down);
					content.addNavButts(up, down);
					content.body().moveX1Y1(30, 0);
					
					content.add(new BText(l.res, none).clickActionSet(new ACTION() {
						@Override
						public void exe() {
							l.s.audiodevice.set(null);
							message = null;
						}
					}));
					
					content.add(new BText(l.res, def).clickActionSet(new ACTION() {
						@Override
						public void exe() {
							l.s.audiodevice.set("");
							message = null;
						}
					}));
					
					for (String s : SoundDevices.get()) {
						content.add(new BText(l.res, s).clickActionSet(new ACTION() {
							@Override
							public void exe() {
								l.s.audiodevice.set(s);
								message = null;
							}
						}));
					}
					
					mFullScreens.add(content);
					
					CLICKABLE can = new BText(l.res, D.g("Back")) {
						@Override
						protected void clickA() {
							message = null;
						};
					};
					
					
					can.body().moveX1(80);
					can.body().moveY1(mFullScreens.getLastY2()+10);
					mFullScreens.add(can);
					mFullScreens.body().centerX(0, Sett.WIDTH);
					mFullScreens.body().centerY(0, Sett.HEIGHT);
					
					message = mFullScreens;
				}
			};
			c.hoverInfoSet(D.g("audioD", "What audio device to use. If empty, the game can not find a device with openal support. Try fiddling with headphones and jacks if you have a problem."));
			
			s.addGridD(c, i++, cols, wi, hi, DIR.NW);
			i++;
			
			
			
			
			
			add(s, 0, 0);
			

			
		}
		
		{
			
			GuiSection s = new GuiSection();
			
			
			CLICKABLE c = new Multi(l.s.screenMode, D.g("Screen"), D.g("ScreedD", "The type of display to be created for the game."), 200) {
				int i = -1;
				
				@Override
				public void render(SPRITE_RENDERER r, float ds) {
					if (i != l.s.monitor.get()) {
						i = l.s.monitor.get();
						settResolutions();
					}
					super.render(r, ds);
				}
				
				private CharSequence[] vs = new CharSequence[] {
					D.g("Borderless"),
					D.g("Full"),
					D.g("Windowed")
				};
				
				@Override
				public void setValue(Text v, LSettingInt s) {
					v.add(vs[s.get()]);
				}
				
			};

			s.add(c);
			
			c = new Multi(l.s.monitor, D.g("Monitor"), D.g("MonitorD", "Which monitor to start the game in.")) {
				int i = -1;
				
				@Override
				public void render(SPRITE_RENDERER r, float ds) {
					if (i != l.s.monitor.get()) {
						i = l.s.monitor.get();
						settResolutions();
					}
					super.render(r, ds);
				}
				
			};
			s.addRightC(16, c);
			
			s.addRelBody(8, DIR.S, new SPRITE.Imp(800, 2) {
				
				@Override
				public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
					COLOR.WHITE65.render(r, x1, X2, Y1, Y2);
				}
			});
			
			addRelBody(8, DIR.S, s);
		}
		
		{
		
			SPRITE ss = new SPRITE.Imp(400, l.res.font.height()+12) {
				Text t = new Text(l.res.font, 64);
				CharSequence a = D.g("Resolution");
				
				@Override
				public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
					
					t.clear().add(a).add(':').s();

					DisplayMode d = Displays.available(l.s.monitor.get()).get(l.s.fullScreenDisplay.get());
					t.add(d.width).add(' ').add('x').add(' ').add(d.height).add(' ').add('@').add(d.refresh).add('H').add('z');
					t.adjustWidth();
					t.renderC(r, X1+(X2-X1)/2, Y1+(Y2-Y1)/2);
				}
			};
			
			CLICKABLE b = new GUI.Button(ss) {

				@Override
				protected void clickA() {
					message = mFullScreens;
				}
			};
			b.hoverInfoSet(D.g("ResolutionD", "What resolution to use in full screen mode"));
			

			fullScreen.addRightC(32, b);
			
			
			fullScreen.body().moveY1(getLastY2()+16);
			fullScreen.body().moveX1(x1);
			
			add(fullScreen);
			
			mFullScreens = new GuiSection();
			content = new ScrollBox(Sett.HEIGHT-100);
			
			CLICKABLE up = new BSprite(l.res.arrowUpDown[0]).clickActionSet(new ACTION() {
				@Override
				public void exe() {
					content.scrollUp();
				}
			});
			mFullScreens.add(up);
			
			
			CLICKABLE down = new BSprite(l.res.arrowUpDown[1]).clickActionSet(new ACTION() {
				@Override
				public void exe() {
					content.scrollDown();
				}
			});
			down.body().moveY2(Sett.HEIGHT-120);
			
			mFullScreens.add(down);
			content.addNavButts(up, down);
			content.body().moveX1Y1(30, 0);
			mFullScreens.add(content);
			
			CLICKABLE can = new BText(l.res, sback) {
				@Override
				protected void clickA() {
					message = null;
				};
			};
			
			
			can.body().moveX1(80);
			can.body().moveY1(mFullScreens.getLastY2()+10);
			mFullScreens.add(can);
			mFullScreens.body().centerX(0, Sett.WIDTH);
			mFullScreens.body().centerY(0, Sett.HEIGHT);
			
			
		}
		
		
		
		{
			CLICKABLE c = new Multi(l.s.windowWidth, D.g("Width"), D.g("widthD", "The width of the window")) {
				
				@Override
				public void setValue(Text v, LSettingInt s) {
					v.add((int)(s.getD()*100));
					v.add('%');
				}
			};
			windowed.add(c);
			c = new Multi(
					l.s.windowHeight, D.g("Height"), D.g("HeightD", "The height of the window")) {
				@Override
				public void setValue(Text v, LSettingInt s) {
					v.add((int)(s.getD()*100));
					v.add('%');
				}
			};
			windowed.addDown(0, c);
			
			CLICKABLE d;
//			CLICKABLE d = new CheckBox(l.s.fill, "Fill", "Stretches the defined window over the whole screen.");
//			d.body().moveX1(3*200);
//			windowed.add(d);
			windowed.body().moveY1(getLastY1());
			windowed.body().moveX1(x1);
			
			d = new CheckBox(l.s.decorated, D.g("Borders"), D.g("BorderD", "Use borders and system decoration on the window"));
			windowed.addRelBody(16, DIR.E, d);
			
			d = new CheckBox(l.s.forcedHD, "HD", "Forced HD resolution"){
				
				@Override
				protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected,
						boolean isHovered) {
					if (l.s.developer.get() == 0)
						return;
					super.render(r, ds, isActive, isSelected, isHovered);
				}
				
			};
			windowed.addRelBody(16, DIR.E, d);
			
			
			add(windowed);
		}
		
		{
			
			CLICKABLE c = new Multi(l.s.windowBorderLessScale, D.g("Scale"), D.g("ScaleD", "The scale of the game. Choose a bigger scale if the game is too small for you.")) {
				
				@Override
				public void setValue(Text v, LSettingInt s) {
					v.add((int)(100 + s.getD()*100));
					v.add('%');
				}
			};
			borderless.add(c);
			
			borderless.body().moveY1(getLastY1());
			borderless.body().moveX1(x1);
			
			
			add(borderless);
		}

		{
			GuiSection s = new GuiSection();
			s.body().setWidth(Sett.WIDTH);

			CLICKABLE b;
			b = new BText(l.res, sback).clickActionSet(new ACTION() {
				@Override
				public void exe() {
					exit();
				}
			});
			b.body().moveX2(Sett.WIDTH-16);
			s.add(b);
			
			b = new BText(l.res, D.g("Reset")).clickActionSet(new ACTION() {
				@Override
				public void exe() {
					l.s.setDefault();
				}
			});
			b.body().moveX2(s.getLastX1()-8);
			s.add(b);
			s.addCentredY(new GUI.Header(l.res, D.g("Settings")), 16);
			
			
			addRelBody(16, DIR.N, s);
			
			
		}
		
		
		
		
		body().centerIn(0, Sett.WIDTH, 0, Sett.HEIGHT);
		body().moveY1(16);
		settResolutions();
		
	}

	
	private CLICKABLE resButt(int i) {
		
		
		
		DisplayMode d = Displays.available(l.s.monitor.get()).get(i);
		
		COLOR c = COLOR.WHITE100;
		if (d.width < C.MIN_WIDTH) {
			c = COLOR.RED100;
		}else if (d.height < C.MIN_HEIGHT) {
			c = COLOR.RED100;
		}else {
			double a = d.width*d.height;
			if (a > C.MAX_SCREEN_AREA) {
				c = COLOR.ORANGE100;
			}
		}
		final COLOR c2 = c;
		
		CLICKABLE b = new BText(l.res, Displays.available(l.s.monitor.get()).get(i).toString()) {
			
			@Override
			protected void clickA() {
				l.s.fullScreenDisplay.set(i);
				message = null;
			}
			
			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected,
					boolean isHovered) {
				c2.bind();
				super.render(r, ds, isActive, isSelected, isHovered);
			}
		};
		
		return b;
		
	}
	
	private void settResolutions() {
		content.clear();
		for (int i = 0; i < Displays.available(l.s.monitor.get()).size(); i++) {
			CLICKABLE bb = resButt(i);
			content.add(bb);
		}
	}
	
	private void exit(){
		message = null;
		
		l.setMain();
	}
	
	@Override
	public void render(SPRITE_RENDERER r, float ds) {
		OPACITY.O75.bind();
		COLOR.BLACK.render(r, 0, Sett.WIDTH, 0, Sett.HEIGHT);
		OPACITY.unbind();
		super.render(r, ds);
		if (hoverInfo.length() != 0) {
			GUI.c_label.bind();
			l.res.font.render(r, hoverInfo, 16, 360, Sett.WIDTH-32, 1);
			hoverInfo.clear();
		}
		if (message != null){
			OPACITY.O75.bind();
			COLOR.BLACK.render(r, 0, Sett.WIDTH, 0, Sett.HEIGHT);
			OPACITY.unbind();
			message.render(r, ds);
		}
		COLOR.unbind();
	};
	
	@Override
	public boolean hover(COORDINATE mCoo) {
		if (message != null)
			return message.hover(mCoo);
		else {
			return super.hover(mCoo);
		}
	};
	
	@Override
	public boolean click() {
		if (message != null)
			return message.click();
		else
			return super.click();
		
	};
	
	private class CheckBox extends BText{

		private final LSettingInt b;
		CheckBox(LSettingInt b, CharSequence name, CharSequence desc) {
			super(l.res, name, 150);
			this.b = b;
			hoverInfoSet(desc);
		}
		
		@Override
		protected void clickA() {
			b.set((b.get()+1)&0b01);
		}
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive,
				boolean isSelected, boolean isHovered) {
			
			isSelected = b.get() == 1;
			super.render(r, ds, isActive, isSelected, isHovered);
			
		}
		
		@Override
		public boolean hover(COORDINATE mCoo) {
			if (super.hover(mCoo) && this.hoverInfo != null) {
				ScreenSetting.this.hoverInfo.add(this.hoverInfo);
				return true;
			}
			return false;
		}

	}
	
	private class Multi extends GuiSection{

		private final SPRITE s;
		private final Text value = new Text(l.res.font, 100);
		private final CLICKABLE left = new BSprite(l.res.arrowLR[0]) {
			@Override
			protected void clickA() {
				b.inc(-1);
			};
			
			@Override
			protected void renAction() {
				activeSet(b.get() > b.min());
			};
		};
		private final CLICKABLE right = new BSprite(l.res.arrowLR[1]) {
			@Override
			protected void clickA() {
				b.inc(1);
			};
			
			@Override
			protected void renAction() {
				activeSet(b.get() < b.max());
			};
		};
		
		private final LSettingInt b;
		private int width;
		private CharSequence desc;
		
		Multi(LSettingInt b, CharSequence name, CharSequence desc) {
			this(b, name, desc, 100);
		}
		
		Multi(LSettingInt b, CharSequence name, CharSequence desc, int w) {
			this.b = b;
			s = new snake2d.util.sprite.text.Text(l.res.font, name);
			add(s, 0, 0);
			hoverInfoSet(desc);
			this.desc = desc;
			width = 180;
			addRightCAbs(120,left);
			addRightC(width, right);
		}
		
		@Override
		public void render(SPRITE_RENDERER r, float ds) {
			value.clear();
			setValue(value, b);
			value.adjustWidth();
			int dx = (width-value.width())/2;
			value.renderCY(r, left.body().x2()+dx, body().cY());
			super.render(r, ds);
		}
		
		public void setValue(Text v, LSettingInt s) {
			v.add(s.get());
		}
		
		@Override
		public boolean hover(COORDINATE mCoo) {
			if (super.hover(mCoo)) {
				ScreenSetting.this.hoverInfo.add(desc);
				return true;
			}
			return false;
		}

	}
	
	
	
	
}
