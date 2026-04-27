package view.ui.top;

import game.GAME;
import game.time.TIME;
import init.constant.C;
import init.constant.Config;
import init.settings.S;
import init.sprite.SPRITES;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import settlement.main.SETT;
import snake2d.KEYCODES;
import snake2d.MButt;
import snake2d.Renderer;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.color.OPACITY;
import snake2d.util.color.OpacityImp;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.ACTION;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.text.Text;
import util.colors.GCOLOR;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.text.D;
import util.text.Dic;
import view.interrupter.InterManager;
import view.interrupter.Interrupter;
import view.keyboard.KEYS;
import view.keyboard.KeyButt;
import view.main.VIEW;
import view.ui.advice.UIAdvice;
import view.ui.log.UILog;
import view.ui.wiki.WIKI;
import world.WORLD;

public class UIPanelTop extends Interrupter {

	public static final int WIDTH = C.WIDTH();
	public static final int HEIGHT = Icon.M*2+3;

	private final GuiSection section = new GuiSection();
	private final GuiSection time;
	private final GuiSection right = new GuiSection();
	private final GuiSection noti;
	
	private static CharSequence ¤¤bView = "Toggle Battle Mode";
	
	static {
		D.ts(UIPanelTop.class);
	}
	
	public UIPanelTop(InterManager manager) {

		this(manager, false, false);
	}

	public UIPanelTop(InterManager manager, boolean battleview, boolean battle) {

		pin();
		section.body().setDim(WIDTH, HEIGHT );
		section.body().moveX2(C.WIDTH());
		section.body().moveY1(0);

		time = SPRITES.specials().buildTimeThing(battleview);
		time.body().centerX(section.body());
		time.body().moveY1(battleview ? 6 : 0);


		if (!battleview && !battle) {
			noti = new UINotifications();

		}else {
			noti = new GuiSection();
		}
		right.addRightC(0, sep());

		

		if (S.get().developer) {
			right.addRightC(0, new Butt(SPRITES.icons().s.cog) {
				@Override
				protected void clickA() {
					VIEW.inters().debugpanel.show();
				}

				@Override
				protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected,
						boolean isHovered) {
					selectedSet(VIEW.inters().debugpanel.isActivated());
					super.render(r, ds, isActive, isSelected, isHovered);
				}
			}.hoverInfoSet("developer-tools"));
			
		}
		right.addRightC(0, new Butt(SPRITES.icons().l.book, 18) {
			@Override
			public void clickA() {
				VIEW.UI().wiki.activate();
			}
		}.hoverInfoSet(WIKI.¤¤name));
		right.addRightC(0, new Butt(SPRITES.icons().l.menu, 18) {
			@Override
			public void clickA() {
				VIEW.inters().menu.show();
			}
		}.hoverInfoSet(Dic.¤¤Menu));

		right.body().centerIn(section);
		right.body().moveX2(C.WIDTH() - 4);
		right.body().moveY1(1);

		section.add(right);
		
		noti.body().moveX1Y1(C.DIM().width()/2 + 100, HEIGHT);

		show(manager);
	}

//	public void addNoti() {
//		UINotifications noti = new UINotifications();
//		noti.body().moveX1Y1(section.body().cX() + 110, 0);
//		noti.body().centerY(0, HEIGHT);
//		section.add(noti);
//	}

	@Override
	public boolean render(Renderer r, float ds) {

		manager().viewPort().moveY1(section.body().y2());
		if (manager().viewPort().y2() > C.HEIGHT()) {
			manager().viewPort().setHeight(C.HEIGHT() - section.body().height());
		}

		GCOLOR.UI().panBG.render(r, section.body());
		section.render(r, ds);
		GCOLOR.UI().border(r, 0, C.WIDTH(), section.body().y2()-3, section.body().y2());
		//UI.PANEL().hollow.renderHorizontal(r, 0, C.WIDTH(), section.body().y2() - UI.PANEL().hollow.margin);
		time.render(r, ds);
		noti.render(r, ds);
		return true;
	}

	public void hide(boolean yes) {
		section.visableSet(yes);
	}

	@Override
	protected boolean hover(COORDINATE mCoo, boolean mouseHasMoved) {
		return section.hover(mCoo) | time.hover(mCoo) | noti.hover(mCoo) || mCoo.touchesRec(section);
	}

	@Override
	protected void mouseClick(MButt button) {
		if (button == MButt.LEFT) {
			section.click();
			time.click();
			noti.click();
		}
	}

	@Override
	protected void hoverTimer(GBox text) {
		section.hoverInfoGet(text);
		time.hoverInfoGet(text);
		noti.hoverInfoGet(text);
	}

	@Override
	protected boolean update(float ds) {

		return true;
	}

	public static int y2() {
		return 48 - 2;
	}

	public static class Butt extends GButt.ButtPanel{
		
		public Butt(SPRITE label) {
			super(label);
			body.setHeight(HEIGHT);
			body.setWidth(36);
		}
		
		public Butt(SPRITE label, int px) {
			super(label);
			body.setHeight(HEIGHT);
			body.setWidth(36+px);
		}
	}
	
	public static CLICKABLE messages() {
		CLICKABLE b = new Butt(SPRITES.icons().m.openscroll) {
			private Text nr = new Text(UI.FONT().M, 10);

			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected,
					boolean isHovered) {
				if (VIEW.b().isActive())
					return;
				isActive = VIEW.messages().size() > 0;
				
				isSelected = VIEW.messages().activated();

				if (VIEW.messages().unread() > 0) {
					bg(GCOLOR.UI().goodFlash());
					super.render(r, ds, isActive, isSelected, isHovered);
					nr.clear().add(VIEW.messages().unread()).adjustWidth();
					if (!isHovered && TIME.currentSecond() - VIEW.messages().currentSecond() < 3) {
						COLOR.WHITE2WHITE.bind();
						bg(COLOR.BLUE2BLUE);

					}

					int x = body().x1() + (body.width() - nr.width()) / 2;
					int y = body().y1() + (body.height() - nr.height()) / 2;

					COLOR.WHITE100.bind();
					nr.render(r, x - 1, y - 1);
					COLOR.RED50.bind();
					nr.render(r, x, y);
					COLOR.unbind();
				} else {
					bgClear();
					super.render(r, ds, isActive, isSelected, isHovered);
				}

			}

			@Override
			protected void clickA() {
				if (!VIEW.b().isActive())
					VIEW.inters().messages.activate();
			}
		};
		b.hoverInfoSet(Dic.¤¤Messages);
		return b;
	}
	
	public static CLICKABLE advice() {
		
		GButt.ButtPanel b = UIAdvice.make();
		b.setDim(36, HEIGHT);
		return b;
	}

	public void addLeft(GuiSection s) {
		s.body().moveX1(4);
		s.body().centerY(section.body());
		section.add(s);
	}
	
//	public void addRightDown(GuiSection s) {
//		s.body().moveX2(C.WIDTH()-4);
//		s.body().moveY1(right.body().y2());
//		section.add(s);
//	}

	public void addRight(GuiSection s) {
		s.body().moveX1(time.body().x2() + 32);
		s.body().centerY(section.body());
		section.add(s);
	}
	
	public void addRightRight(GuiSection s) {
		
		s.body().moveX2(right.body().x1());
		s.body().centerY(right.body());
		right.addRelBody(0, DIR.W, s);
	}
	
	public static RENDEROBJ sep() {
		
		return new RENDEROBJ.RenderImp(12, 38) {
			
			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				GCOLOR.UI().border().render(r, body().cX()-1, body().cX(), body().y1(), body().y2());
				COLOR.WHITE05.render(r, body().cX(), body().cX()+1, body().y1(), body().y2());
			}
		};
		
	}
	
	public static RENDEROBJ bToggle() {
		ACTION a = new ACTION() {
			
			@Override
			public void exe() {
				if (VIEW.s().isActive())
					VIEW.s().battle.activate();
				else if (VIEW.s().battle.isActive())
					VIEW.s().activate();
			}
		};
		
		final OpacityImp opa = new OpacityImp(0);
		COLOR normal = new ColorImp(47, 20, 0).shadeSelf(1.2);
		COLOR active = new ColorImp(127, 40, 20);
		
		CLICKABLE c = new GButt.ButtPanel(SPRITES.icons().l.battle) {
			int di = 0;
			boolean blink = false;
			boolean nextBlink = false;
			@Override
			protected void clickA() {
				a.exe();
			}

			@Override
			protected void renAction() {
				selectedSet(VIEW.s().battle.isActive());
			};
			
			@Override
			public void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
				super.render(r, ds, isActive, isSelected, isHovered);
				
				if (di >= Config.battle().DIVISIONS_PER_ARMY) {
					blink = nextBlink;
					nextBlink = false;
					di= 0;
				}
				
				if (GAME.ARMIES().player().divisions().get(di).men() > 0 && GAME.ARMIES().player().divisions().get(di).settings().mustering()) {
					blink = true;
					nextBlink = true;
				}
				
				di++;
				
				if (blink || (!isHovered && ! isSelected && GAME.ARMIES().enemy().men() > 0 || SETT.INVADOR().invading())) {
					opa.set(0.25 + VIEW.renderSecond()%0.75);
					opa.bind();
					active.render(r, body, -3);
					OPACITY.unbind();
				}
			};
			
		}.setDim(40, 48).bg(normal).hoverInfoSet(Dic.¤¤Battle);
		
		c = KeyButt.wrap(a, c, KEYS.MAIN(), "enablebattle", ¤¤bView, ¤¤bView, KEYCODES.KEY_LEFT_SHIFT, KEYCODES.KEY_B);
		
		return c;
	}
	
	public static RENDEROBJ wLog() {
		
		Butt b = new Butt(SPRITES.icons().m.factions) {
			int current = WORLD.LOG().all().size();
			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected,
					boolean isHovered) {
				if (VIEW.b().isActive())
					return;
				isActive = WORLD.LOG().all().size() > current;
				
				isSelected = VIEW.UI().log.isActivated();
				super.render(r, ds, isActive, isSelected, isHovered);

			}

			@Override
			protected void clickA() {
				if (!VIEW.b().isActive()) {
					VIEW.UI().log.activate();
					current = WORLD.LOG().all().size();
				}
			}
		};
		b.setDim(40, 48);
		b.hoverInfoSet(UILog.¤¤name);
		return b;
		
	}
	
	public static RENDEROBJ vToggle() {
		
		COLOR cw = new ColorImp(0, 47, 20);
		COLOR cc = new ColorImp(0, 47, 47);
		
		ACTION a = new ACTION() {
			
			@Override
			public void exe() {
				if (VIEW.s().isActive())
					VIEW.world().activate();
				else if (VIEW.world().isActive())
					VIEW.s().activate();
			}
		};
		
		Butt b = new Butt(SPRITES.icons().l.city, 18) {
			@Override
			protected void clickA() {
				a.exe();
			}

			@Override
			protected void renAction() {
				replaceLabel(VIEW.s().isActive() ? SPRITES.icons().l.world : SPRITES.icons().l.city, DIR.C);
				bg(VIEW.s().isActive() ? cc : cw);
				selectedSet(false);
			};
		};
		
		
		
		CLICKABLE c = KeyButt.wrap(b, KEYS.MAIN().SWAP);
		return c;
	}
	
	public static RENDEROBJ junk() {
		return VIEW.UI().manager.butt();
	}

}
