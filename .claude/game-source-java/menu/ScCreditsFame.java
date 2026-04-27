package menu;

import static menu.GUI.getNavButt;
import static menu.GUI.inner;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import init.constant.C;
import init.paths.PATHS;
import init.sprite.UI.UI;
import menu.GUI.COLORS;
import menu.GUI.Shadower;
import snake2d.CORE;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.light.PointLight;
import snake2d.util.misc.ACTION;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LinkedList;
import snake2d.util.sets.Tuple;
import snake2d.util.sets.Tuple.TupleImp;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.TextureCoords;
import snake2d.util.sprite.text.Text;
import util.text.D;
import view.menu.MenuScreen;

class ScCreditsFame extends Shadower implements SC{
	
	private final CLICKABLE next;
	private final CLICKABLE prev;
	private int currentScreen = 0;
	private final ArrayListGrower<Screen> all = new ArrayListGrower<>();
	
	private final Text sname = UI.FONT().H2.getText(200);
	private final Text stitles = UI.FONT().M.getText(200);
	
	static CharSequence ¤¤name = "¤hall of fame";
	static{
		D.ts(ScCreditsFame.class);
	}

	ScCreditsFame(Menu menu){
		D.gInit(this);
		
		MenuScreen screen = new MenuScreen(¤¤name, GUI.labelColor) {
			
			@Override
			protected void back() {
				menu.switchScreen(menu.main);
			}
		};
		
		add(screen);
		
		all.add(legends(menu));
		all.add(heroes(menu));
		all.add(others());
		
		prev = getNavButt("<<");
		prev.clickActionSet(new ACTION() {
			@Override
			public void exe() {
				if (currentScreen > 0)
					currentScreen --;
				prev.activeSet(currentScreen > 0);
				next.activeSet(currentScreen < all.size()-1);
				
			}
		});
		screen.addButt(prev);
		next = getNavButt(">>");
		next.clickActionSet(new ACTION() {
			@Override
			public void exe() {
				if (currentScreen < all.size()-1)
					currentScreen ++;
				prev.activeSet(currentScreen > 0);
				next.activeSet(currentScreen < all.size()-1);
			}
		});
		screen.addButt(next);

		
		body().centerX(C.DIM());
		
		prev.activeSet(currentScreen > 0);
		next.activeSet(currentScreen < all.size()-1);
		
		for (Screen s : all) {
			s.body().centerIn(MenuScreen.inner);
			s.body().moveY1(MenuScreen.inner.y1());
		}
	}
	
	private LIST<Screen> legends(Menu menu){
		String[] names = new String[] {
			"Jake",
			"Natalia Jasinska",
			"Gianluca Borg",
			"Superwutz",
			"ProRt",
			"Connor Bryant",
			"JollyWarhammer",
			"Bendigeidfran",
		};
		String[] descs = new String[] {
			"Supreme Developer, Creator of worlds, Bringer of Syxians",
			"Mistress of soundtracks",
			"High Councelor, Spokesman of the Plebs, Guardian of History",
			"Sacred voice of modability, Father of the Agonosh, He whose name is hard to remember",
			"First knighted, Finder of bugs",
			"Generous benefactor",
			"Warrior Monk",
			"Champion of Art",
		};
		
		LinkedList<Screen> screens = new LinkedList<>();
		Screen current = null;
		final PointLight light = new PointLight();
		light.setRadius(200);
		light.setZ(200);
		double ii = 1.5;
		light.setRed(ii).setGreen(ii).setBlue(ii);
		
		for (int i = 0; i < names.length; i++) {
			if (i%4 == 0) {
				current = new Screen();
				screens.add(current);
			}
			
			String name = names[i];
			String desc = descs[i];
			SPRITE frame = menu.res.s().creditsBigFrame;
			SPRITE ps = menu.res.s().creditsBig[i];
			
			current.addRightC(16, new HOVERABLE.HoverableAbs(frame) {
				
				@Override
				protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
				
					if (isHovered) {
						light.set(body.cX(), body.cY());
						light.register();
					}else {
						
					}

					ps.render(r, body.x1(), body.y1());
					frame.render(r, body.x1(), body.y1());
					
				}
				
				@Override
				public boolean hover(COORDINATE mCoo) {
					
					sname.clear();
					if (super.hover(mCoo)) {
						sname.set(name);
						stitles.clear().set(desc);
						return true;
					}
					return false;
				}
			});
			
			
			
		}
		return screens;
		
	}
	
	private LIST<Screen> heroes(Menu menu){
		

		String[] names = new String[] {
			"Laki 95",
			"Dr. Kelloggs",
			"Licher",
			"Qbjik",
			"Mathedarius & Daniella",
			"Sparrow",
			"Mathias Dietrich",
			"Felix Ungman",
		};
		String[] descs = new String[] {
			"Bringer of Suggestions",
			"Titles...",
			"The King of the People and Protector of Syx",
			"The Lazy Panda, Lord of Quokkas",
			"The Wise, The Wolves, Breaker of Chains",
			"The Sweet, the Chonk, Purrveyor of Mews",
			"Beacon of the Free",
			"Shogun",
		};
		
		LinkedList<Screen> screens = new LinkedList<>();
		Screen current = null;
		final PointLight light = new PointLight();
		light.setRadius(150);
		light.setZ(150);
		light.setRed(1).setGreen(1).setBlue(1);

		for (int i = 0; i < names.length; i++) {
			if (i%10 == 0) {
				current = new Screen();
				screens.add(current);
			}
			
			String name = names[i];
			String desc = descs[i];
			SPRITE frame = menu.res.s().creditsSmallFrame;
			SPRITE ps = menu.res.s().creditsSmall[i];
			
			int x = (i%10)%5;
			int y = (i%10)/5;
			
			current.add(new HOVERABLE.HoverableAbs(frame) {
				
				@Override
				protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
				
					if (isHovered) {
						light.set(body.cX(), body.cY());
						light.register();
					}else {
						
					}

					ps.render(r, body.x1(), body.y1());
					frame.render(r, body.x1(), body.y1());
					
				}
				
				@Override
				public boolean hover(COORDINATE mCoo) {
					
					sname.clear();
					if (super.hover(mCoo)) {
						sname.set(name);
						stitles.clear().set(desc);
						return true;
					}
					return false;
				}
			}, x*(frame.width()+20), y*(frame.height()+8));
			
			
			
		}
		return screens;
		
	}
	
	private LIST<Screen> others(){
		
		final PointLight light = new PointLight();
		double in = 1.0;
		light.setRed(in).setGreen(in).setBlue(in);
		light.setRadius(900);
		light.setZ(200);
		
		ArrayListGrower<TupleImp<String, String[]>> all = new ArrayListGrower<Tuple.TupleImp<String,String[]>>();
		
		try {
			all.add(new TupleImp<String, String[]>(""+D.g("nobility"), new String(Files.readAllBytes(PATHS.BASE().DATA.get("Nobles")),StandardCharsets.UTF_8).split(System.lineSeparator())));
			all.add(new TupleImp<String, String[]>(""+D.g("knights"), new String(Files.readAllBytes(PATHS.BASE().DATA.get("Knights")), StandardCharsets.UTF_8).split(System.lineSeparator())));
			all.add(new TupleImp<String, String[]>(""+D.g("citizens"), new String(Files.readAllBytes(PATHS.BASE().DATA.get("Citizens")), StandardCharsets.UTF_8).split(System.lineSeparator())));
		}catch(Exception e) {
			
		}
		
		
		
		LinkedList<Screen> screens = new LinkedList<>();

		for (TupleImp<String, String[]> t : all) {
			
			int x1 = inner.x1();
			int x2 = inner.x2()+100;
			
			
			
			
			
			int scale = 2;
			
			int margin = 20;
			int height = UI.FONT().H2.height()*scale;
			
			int y2 = inner.y2()-height;
			
			String[] names = t.b;
			String title = t.a;
			
			int i = 0;
			while (i < names.length) {
				
				Screen s = new Screen() {
					@Override
					public void render(SPRITE_RENDERER r, float ds) {
						light.set(inner.cX(), inner.cY());
						light.register();
						OPACITY.O99.bind();
						super.render(r, ds);
						OPACITY.unbind();
						
					}
				};
				s.add(new RENDEROBJ.Sprite(UI.FONT().H2.getText((Object)title).setScale(2)));
				s.body().moveY1(inner.y1());
				s.body().centerX(inner);
				int y1 = s.body().y2()+10;
				int x = x1;
				while(i < names.length) {
					x += RND.rInt(20);
					RENDEROBJ.Sprite o = new RENDEROBJ.Sprite(new Name(names[i]));
					i++;
					o.setColor(COLOR.WHITE100);
					if (x + o.body().width() > x2) {
						y1 += height;
						if (y1 > y2)
							break;
						x = x1 +RND.rInt(30);
					}
					o.body().moveX1(x).moveY1(y1+RND.rInt(20));
					x+= o.body().width() + margin;
					s.add(o);
					
				}
				screens.add(s);
				
			}
			
			
		}

		return screens;
		
	}
	
	private static final class Name implements SPRITE {

		private final int width;
		private final CharSequence name;
		
		Name(CharSequence s){
			this.name = s;
			width = UI.FONT().H2.getDim(s).x()*2;
		}
		
		@Override
		public int width() {
			return width;
		}

		@Override
		public int height() {
			return UI.FONT().H2.height()*2;
		}

		@Override
		public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
			
			UI.FONT().H2.render(r, name, X1, Y1, 2);
		}

		@Override
		public void renderTextured(TextureCoords texture, int X1, int X2, int Y1, int Y2) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	@Override
	public void render(SPRITE_RENDERER r, float ds) {
		super.render(r, ds);
		if (sname.length() != 0) {
			
			COLORS.label.bind();
			sname.renderC(r, body().cX(), inner.y2());
			COLORS.copper.bind();
			stitles.renderC(r, body().cX(), inner.y2()+sname.height()+4);
			COLOR.unbind();
			sname.clear();
			stitles.clear();
			
		}
	}
	
	@Override
	public void renderBackground(Background back, float ds, COORDINATE mCoo) {
		
		back.renderFame(CORE.renderer(), ds, mCoo, all.get(currentScreen).ran);
		all.get(currentScreen).render(CORE.renderer(), ds);
	}

	
	@Override
	public boolean hover(COORDINATE mCoo) {
		all.get(currentScreen).hover(mCoo);
		return super.hover(mCoo);
	}
	
	@Override
	public boolean back(Menu menu) {
		menu.switchScreen(menu.main);
		return true;
	}

	private static class Screen extends GuiSection{
		
		final double ran = RND.rFloat();
		
		
	}
	
	
	
}
