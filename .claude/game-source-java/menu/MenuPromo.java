package menu;


import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import init.INIT;
import init.constant.C;
import init.paths.PATHS;
import init.settings.S;
import init.sprite.UI.UI;
import snake2d.CORE;
import snake2d.CORE.GlJob;
import snake2d.CORE_STATE;
import snake2d.Displays.DisplayMode;
import snake2d.KEYCODES;
import snake2d.KeyBoard.KeyEvent;
import snake2d.MButt;
import snake2d.Renderer;
import snake2d.SETTINGS;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Coo;
import snake2d.util.file.Json;
import snake2d.util.file.JsonE;
import snake2d.util.file.SnakeImage;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE.ClickableAbs;
import snake2d.util.light.AmbientLight;
import snake2d.util.light.Fire;
import snake2d.util.light.PointLight;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.TILE_SHEET;
import snake2d.util.sprite.text.StringInputSprite;
import util.data.INT;
import util.data.INT.INTE;
import util.error.ErrorHandler;
import util.gui.misc.GButt;
import util.gui.slider.GTarget;
import util.spritecomposer.ComposerDests;
import util.spritecomposer.ComposerSources;
import util.spritecomposer.ComposerThings.ITileSheet;
import util.spritecomposer.ComposerUtil;
import util.spritecomposer.Initer;
import view.keyboard.KEYS;

public class MenuPromo extends CORE_STATE{
	

	private TILE_SHEET background;
	private final int ttX;
	private final int ttY;

	int offX = 0;
	int offY = 0;
	
	private final ArrayListGrower<PointLight> lights = new ArrayListGrower<>();
	private final ArrayListGrower<AmbientLight> alights = new ArrayListGrower<>();
	private final GuiSection sec;
	
	private final int scale = 1;
	
	private final StringInputSprite in;
	public static void main(String[] args) {
		CORE.init(new ErrorHandler());
		
		SETTINGS setting = new SETTINGS() {
			
			@Override
			public boolean windowFloating() {
				return false;
			}
			
			@Override
			public boolean vsyncAdaptive() {
				return false;
			}
			
			@Override
			public String openALDevice() {
				return null;
			}
			
			@Override
			public int monitor() {
				return 0;
			}
			
			@Override
			public String getWindowName() {
				return "promo";
			}
			
			@Override
			public boolean getVSynchEnabled() {
				return false;
			}
			
			@Override
			public String getScreenshotFolder() {
				return ""+PATHS.local().SCREENSHOT.get() + File.separator;
			}
			
			@Override
			public int getRenderMode() {
				return 1;
			}
			
			@Override
			public int getPointSize() {
				return 1;
			}
			
			@Override
			public int getNativeWidth() {
				return 1920;
			}
			
			@Override
			public int getNativeHeight() {
				return 1080;
			}
			
			@Override
			public boolean getLinearFiltering() {
				return true;
			}
			
			@Override
			public String getIconFolder() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public boolean getFitToScreen() {
				return false;
			}
			
			@Override
			public DisplayMode display() {
				return new DisplayMode(getNativeWidth(), getNativeHeight(), 60, false);
			}
			
			@Override
			public boolean decoratedWindow() {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean debugMode() {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean autoIconify() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean windowFullFull() {
				// TODO Auto-generated method stub
				return false;
			}
		};
		S.get().make();
		CORE.create(setting);
		CORE.getInput().getMouse().showCusor(false);
		PATHS.init(new String[0], null, false);
		CORE.start(new CORE_STATE.Constructor() {
			@Override
			public CORE_STATE getState() {
				return new MenuPromo();
			}
		});
		
		
	}
	
	private MenuPromo() {
		
		final Path ss = new File(new File(System.getProperty("user.dir")).getParent().toString() + File.separator + "Tools" + File.separator + "mural" + File.separator + "Result.png").toPath(); 
		System.out.println(ss.toString());
		
		new GlJob() {
			@Override
			public void doJob() {
				new Initer() {
					
					
					
					
					@Override
					public void createAssets() throws IOException {
						new INIT();
						COORDINATE dim = SnakeImage.dim(ss);
						final int tx = (dim.x()-24)/(32*2);
						final int ty = (dim.y()-12)/(32);
						background = new ITileSheet(ss, dim.x(), dim.y()) {
							
							@Override
							protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
								s.full.init(0, 0, 1, 1, tx, ty, d.s32);
								s.full.paste(true);
								return d.s32.save(scale);
							}
						}.get();
						
					}
				}.get("menuPromo", PATHS.textureSize(), 0);
			
			}
		}.perform();
		KEYS.init();
		
		COORDINATE dim = SnakeImage.dim(ss);
		ttX = dim.x()/(32*2);
		ttY = dim.y()/(32);
		
		sec = new GuiSection();
		
		sec.add(new Sec());
		sec.addRightC(4, new Sec2());
		in = new StringInputSprite(100, UI.FONT().H1);
		
		Fire torch1 = new Fire(7);
		Fire torch2 = new Fire(7);
		PointLight torch3 = new PointLight();
		PointLight torch4 = new PointLight();
		
		
		torch1.set(-662, 921);
		torch1.setZ(130);
		torch1.setRadius(1902);
		torch1.setFalloff(2f);
		
		torch2.set(2188, 862);
		torch2.setZ(80);
		torch2.setRadius(2107);
		torch2.setFalloff(3f);
		
		torch3.set(864, -257);
		torch3.setZ(73);
		torch3.setRadius(1808);
		torch3.setFalloff(1.17f);
		torch3.setRed(1.5).setGreen(1.5).setBlue(1.5);
		
		torch4.set(904, 1660);
		torch4.setZ(-173);
		torch4.setRadius(3739);
		torch4.setFalloff(4.63f);
		torch4.setRed(3.5).setGreen(3.0).setBlue(2-0);

		torch4.set(974, 24);
		torch4.setZ(53);
		torch4.setRadius(1741);
		torch4.setFalloff(2.2f);
		torch4.setRed(1).setGreen(1).setBlue(1.5);
		
		
		lights.add(torch1);
//		lights.add(torch2);
//		lights.add(torch3);
//		lights.add(torch4);
		
		alights.add(ambient());
	}

	private static AmbientLight ambient() {
		return new AmbientLight().b(1).g(1).b(1);
	}
	
	private void save(int i) {
		JsonE[] jl = new JsonE[lights.size()];
		int ii = 0;
		
		for (PointLight l : lights) {
			JsonE e = new JsonE();
			e.add("X", l.x());
			e.add("Y", l.y());
			e.add("Z", l.cz());
			e.add("RA", l.getRadius());
			e.add("F", l.getFalloff());
			e.add("R", l.getRed());
			e.add("G", l.getGreen());
			e.add("B", l.getBlue());
			jl[ii++] = e;
		}
		
		JsonE[] ja = new JsonE[alights.size()];
		ii = 0;
		
		for (AmbientLight l : alights) {
			JsonE e = new JsonE();
			e.add("D", l.getDir());
			e.add("T", l.getTilt());
			e.add("R", l.r());
			e.add("G", l.g());
			e.add("B", l.b());
			ja[ii++] = e;
		}
		
		JsonE res = new JsonE();
		
		res.add("POINT", jl);
		res.add("AMBI", ja);
		
		
		Path p = PATHS.local().LOGS.get().resolve("PROMO" + i + ".txt");
		System.out.println(p);
		res.save(p);
	}
	
	private void load(int i) {

		if (!PATHS.local().LOGS.exists("PROMO" + i)) {
			System.out.println("nay");
			return;
		}
		
		Json jj = new Json(PATHS.local().LOGS.get("PROMO" + i));
		
		lights.clear();
		
		for (Json j : jj.jsons("POINT")) {
			PointLight p = new PointLight();
			p.xSet(j.d("X"));
			p.ySet(j.d("Y"));
			p.setZ((int) j.d("Z"));
			p.setRadius(j.i("RA"));
			p.setFalloff((float) j.d("F"));
			p.setRed(j.d("R"));
			p.setGreen(j.d("G"));
			p.setBlue(j.d("B"));
			lights.add(p);
		}
		
		alights.clear();
		for (Json j : jj.jsons("AMBI")) {
			AmbientLight p = new AmbientLight();
			p.setDir(j.d("D"));
			p.setTilt(j.d("T"));
			p.r(j.d("R"));
			p.g(j.d("G"));
			p.b(j.d("B"));
			alights.add(p);
		}
		
	}
	
	Coo old = new Coo();
	boolean pressed = false;
	int lastSavePress = -1;
	@Override
	public void update(float ds, double slow) {
		//in.listen();
		sec.hover(CORE.getInput().getMouse().getCoo());
		
		if (pressed) {
			pressed = MButt.RIGHT.isDown();
			if (pressed) {
				offX += CORE.getInput().getMouse().getCoo().x() - old.x();
				offY += CORE.getInput().getMouse().getCoo().y() - old.y();
				old.set((CORE.getInput().getMouse().getCoo()));
			}
			
		}else if (MButt.RIGHT.isDown()) {
			pressed = true;
			old.set((CORE.getInput().getMouse().getCoo()));
		}
		
		if (CORE.getInput().getKeyboard().isPressed(KEYCODES.KEY_ENTER)) {
			int ii = 0;
			for (PointLight l : lights) {
				System.out.println(ii++);
				System.out.println("x: " + l.x());
				System.out.println("y: " + l.y());
				System.out.println("z: " + l.cz());
				System.out.println("ra: " + l.getRadius());
				System.out.println("f: " + l.getFalloff());
				System.out.println("rgb: " + l.getRed() + " " + l.getGreen() + " " + l.getBlue());
			}
		}
		
		if (CORE.getInput().getKeyboard().isPressed(KEYCODES.KEY_ESCAPE)) {
			CORE.annihilate();
		}
		
		if (CORE.getInput().getKeyboard().isPressed(KEYCODES.KEY_LEFT_CONTROL)) {
			int i = 0;
			for (int k : KEYCODES.KEY_NUMS) {
				if (k != lastSavePress && CORE.getInput().getKeyboard().isPressed(k)) {
					save(i);
					lastSavePress = k;
				}
				i++;
			}
		}else if (CORE.getInput().getKeyboard().isPressed(KEYCODES.KEY_LEFT_SHIFT)){
			int i = 0;
			for (int k : KEYCODES.KEY_NUMS) {
				if (k != lastSavePress && CORE.getInput().getKeyboard().isPressed(k)) {
					load(i);
					lastSavePress = k;
				}
				i++;
			}
		}else {
			lastSavePress = -1;
		}
		
	}
	
	
	@Override
	protected void keyPush(LIST<KeyEvent> keys, boolean hasCleared) {
	
		
	}
	
	@Override
	public void render(Renderer r, float ds) {
		
		CORE.renderer().newLayer(false, 0);
		sec.render(r, ds);
		
		if (in.text().length() != 0) {
			
			
			
			AmbientLight.Strongmoonlight.register(0, 1920, 0, 1080);
			in.render(r, 100, 50);
			
			CORE.renderer().newLayer(false, 0);
			AmbientLight.full.register(C.DIM());
			COLOR.GREEN100.render(r, 100, 100+in.width(), 50, 50 + in.height());
			
		}
		
		CORE.renderer().newLayer(false, 0);
		
		CORE.renderer().shadeLight(true);
		CORE.renderer().shadowDepthDefault();
		
		byte full = -1;
		for (PointLight l : lights) {
			l.register(full, full, full, full);
		}
		
		for (AmbientLight l : alights) {
			l.register(0, 1920, 0, 1080);
		}

		int ti = 0;
		
		for (int y = 0; y < ttY; y++) {
			for (int x = 0; x < ttX; x++) {
				background.render(r, ti, x*32*scale+offX, y*32*scale+offY);
				ti++;
			}
		}
		
		
	}

	@Override
	public void mouseClick(MButt button) {
		sec.click();
	}
	
	
	private class Sec extends GuiSection {
		
		INT.IntImp current = new INT.IntImp() {
			@Override
			public int max() {
				return lights.size()-1;
			};
			
			@Override
			public int get() {
				return CLAMP.i(super.get(), min(), max());
			};
			
			@Override
			public int min() {
				return 0;
			};
		};
		
		public Sec() {
			
			add(new GButt.ButtPanel("new") {
				@Override
				protected void clickA() {
					PointLight l = new PointLight();
					l.setRed(1);
					l.setGreen(1);
					l.setBlue(1);
					l.set(C.DIM().cX(), C.DIM().cY());
					l.setZ(20);
					l.setRadius(100);
					lights.add(l);
					current.i = lights.size()-1;
				}
			});
			
			addDown (4, new GTarget(100, false, true, current));
			
			addDown(4, new Slider("X", new INTE() {
				
				@Override
				public int min() {
					return -2000;
				}
				
				@Override
				public int max() {
					return C.DIM().x2()+2000;
				}
				
				@Override
				public int get() {
					return lights.get(current.i).x();
				}
				
				@Override
				public void set(int t) {
					lights.get(current.i).set(t, lights.get(current.i).y());
				}
			}));
			
			addDown(4, new Slider("Y", new INTE() {
				
				@Override
				public int min() {
					return -2000;
				}
				
				@Override
				public int max() {
					return C.DIM().x2()+2000;
				}
				
				@Override
				public int get() {
					return lights.get(current.i).y();
				}
				
				@Override
				public void set(int t) {
					lights.get(current.i).set(lights.get(current.i).x(), t);
				}
			}));
			
			addDown(4, new Slider("Z", new INTE() {
				
				@Override
				public int min() {
					return -1000;
				}
				
				@Override
				public int max() {
					return 1000;
				}
				
				@Override
				public int get() {
					return (int) lights.get(current.i).cz();
				}
				
				@Override
				public void set(int t) {
					lights.get(current.i).setZ(t);
				}
			}));
			
			addDown(4, new Slider("FOFF", new INTE() {
				
				@Override
				public int min() {
					return 1;
				}
				
				@Override
				public int max() {
					return 1000;
				}
				
				@Override
				public int get() {
					return (int) (lights.get(current.i).getFalloff()*100);
				}
				
				@Override
				public void set(int t) {
					lights.get(current.i).setFalloff((float) (t/100.0));
				}
			}));
			
			addDown(4, new Slider("RAD", new INTE() {
				
				@Override
				public int min() {
					return 10;
				}
				
				@Override
				public int max() {
					return 10000;
				}
				
				@Override
				public int get() {
					return (int) (lights.get(current.i).getRadius());
				}
				
				@Override
				public void set(int t) {
					lights.get(current.i).setRadius(t);
				}
			}));
			
			addDown(4, new Slider("R", new INTE() {
				
				@Override
				public int min() {
					return 0;
				}
				
				@Override
				public int max() {
					return 1000;
				}
				
				@Override
				public int get() {
					return (int) (lights.get(current.i).getRed()*100);
				}
				
				@Override
				public void set(int t) {
					lights.get(current.i).setRed(t/100.0);
				}
			}));
			
			addDown(4, new Slider("G", new INTE() {
				
				@Override
				public int min() {
					return 0;
				}
				
				@Override
				public int max() {
					return 1000;
				}
				
				@Override
				public int get() {
					return (int) (lights.get(current.i).getGreen()*100);
				}
				
				@Override
				public void set(int t) {
					lights.get(current.i).setGreen(t/100.0);
				}
			}));
			
			addDown(4, new Slider("B", new INTE() {
				
				@Override
				public int min() {
					return 0;
				}
				
				@Override
				public int max() {
					return 1000;
				}
				
				@Override
				public int get() {
					return (int) (lights.get(current.i).getBlue()*100);
				}
				
				@Override
				public void set(int t) {
					lights.get(current.i).setBlue(t/100.0);
				}
			}));
			
			body().centerIn(C.DIM());
		}
		
		@Override
		public void render(SPRITE_RENDERER r, float ds) {
			
			if (CORE.getInput().getKeyboard().isPressed(KEYCODES.KEY_LEFT_SHIFT)) {
				AmbientLight.full.register(C.DIM());
				COLOR.WHITE10.render(r, body(), 8);
				super.render(r, ds);
				COLOR.GREEN100.render(r, CORE.getInput().getMouse().getCoo().x(), CORE.getInput().getMouse().getCoo().y());
				
			}
		}
		
	}
	

	
	private class Sec2 extends GuiSection {
		
		INT.IntImp current = new INT.IntImp() {
			@Override
			public int max() {
				return alights.size()-1;
			};
			
			@Override
			public int get() {
				return CLAMP.i(super.get(), min(), max());
			};
			
			@Override
			public int min() {
				return 0;
			};
		};
		
		public Sec2() {
			
			add(new GButt.ButtPanel("new") {
				@Override
				protected void clickA() {
					AmbientLight l = ambient();
					alights.add(l);
					current.i = lights.size()-1;
				}
			});
			
			addDown(4, new GButt.ButtPanel("delete") {
				@Override
				protected void clickA() {
					if (alights.size() > 0) {
						alights.remove(current.get());
						current.inc(-1);
					}
				}
			});
			
			addDown (4, new GTarget(100, false, true, current));
			
			addDown(4, new Slider("DEG", new INTE() {
				
				@Override
				public int min() {
					return 0;
				}
				
				@Override
				public int max() {
					return 360;
				}
				
				@Override
				public int get() {
					return (int) alights.get(current.i).getDir();
				}
				
				@Override
				public void set(int t) {
					
					alights.get(current.i).setDir(t);
				}
			}));
			
			addDown(4, new Slider("TILT", new INTE() {
				
				@Override
				public int min() {
					return -90;
				}
				
				@Override
				public int max() {
					return 90;
				}
				
				@Override
				public int get() {
					return (int) alights.get(current.i).getTilt();
				}
				
				@Override
				public void set(int t) {
					alights.get(current.i). setTilt(t);
				}
			}));
			
			addDown(4, new Slider("R", new INTE() {
				
				@Override
				public int min() {
					return 0;
				}
				
				@Override
				public int max() {
					return 1000;
				}
				
				@Override
				public int get() {
					return (int) (alights.get(current.i).r()*100);
				}
				
				@Override
				public void set(int t) {
					alights.get(current.i).set(t/100.0, alights.get(current.i).g(), alights.get(current.i).b());
				}
			}));
			
			addDown(4, new Slider("G", new INTE() {
				
				@Override
				public int min() {
					return 0;
				}
				
				@Override
				public int max() {
					return 1000;
				}
				
				@Override
				public int get() {
					return (int) (alights.get(current.i).g()*100);
				}
				
				@Override
				public void set(int t) {
					alights.get(current.i).set(alights.get(current.i).r(), t/100.0, alights.get(current.i).b());
				}
			}));
			
			addDown(4, new Slider("B", new INTE() {
				
				@Override
				public int min() {
					return 0;
				}
				
				@Override
				public int max() {
					return 1000;
				}
				
				@Override
				public int get() {
					return (int) (alights.get(current.i).b()*100);
				}
				
				@Override
				public void set(int t) {
					alights.get(current.i).set(alights.get(current.i).r(), alights.get(current.i).g(), t/100.0);
				}
			}));
			
			body().centerIn(C.DIM());
		}
		
		@Override
		public void render(SPRITE_RENDERER r, float ds) {
			
			if (CORE.getInput().getKeyboard().isPressed(KEYCODES.KEY_LEFT_CONTROL)) {
				AmbientLight.full.register(C.DIM());
				COLOR.WHITE10.render(r, body(), 8);
				super.render(r, ds);
				COLOR.GREEN100.render(r, CORE.getInput().getMouse().getCoo().x(), CORE.getInput().getMouse().getCoo().y());
				
			}
		}
		
	}
	
	private class Slider extends ClickableAbs {
		
		private INTE in;
		private final String name;
		Slider(String mame, INTE in){
			super(300, 32);
			this.in = in;
			this.name = mame;
			
		}
		
		@Override
		protected void clickA() {
			int x = CORE.getInput().getMouse().getCoo().x();
			x -= body().x1();
			double d = (double)x/body().width();
			
			d *= in.max()-in.min();
			int a = (int) (d+in.min());
			a = CLAMP.i(a, in.min(), in.max());
			
			in.set(a);
		}

		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
			if (isHovered && MButt.LEFT.isDown())
				clickA();
			(isHovered ? COLOR.WHITE10 : COLOR.WHITE20).render(r, body());
			UI.FONT().M.render(r, name, body.x1(), body.y1());
			int d = (int) body.width()*(in.get()-in.min())/(in.max()-in.min());
			COLOR.WHITE85.render(r, body.x1()+d-2, body.x1()+d+2, body.y1(), body.y2());
		}
		
	}
}
