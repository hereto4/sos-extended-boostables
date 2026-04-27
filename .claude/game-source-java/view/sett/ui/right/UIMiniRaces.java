package view.sett.ui.right;

import game.faction.FACTIONS;
import game.time.TIME;
import init.constant.C;
import init.race.RACES;
import init.race.Race;
import init.sprite.SPRITES;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import init.type.HCLASSES;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.stats.STATS;
import settlement.stats.standing.STANDINGS;
import snake2d.MButt;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.text.Str;
import util.colors.GCOLOR;
import util.data.INT.INTE;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GMeter;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.table.GScrollRows;
import util.info.GFORMAT;
import util.text.D;
import view.main.VIEW;
import view.sett.ui.right.UIPanelRightSett.Expansion;

final class UIMiniRaces extends Expansion{

	private final INTE t;
	
	public UIMiniRaces(int index, int y1){
		super(index);
		
		RENDEROBJ[] rows = new RENDEROBJ[RACES.all().size()];
		for (int i = 0; i < RACES.all().size(); i++){			
			rows[i] = noble(i);
		}
		
		int width = rows[0].body().width();
		body().setDim(width+6,C.HEIGHT()-y1);
		
		
		RENDEROBJ c;
		c = new GButt.Glow(UI.decor().up) {
			@Override
			protected void renAction() {
				activeSet(t.get() > 0);
			}
			@Override
			protected void clickA() {
				t.inc(-1);
			}
		};
		c.body().moveCX(body().cX());
		c.body().moveY1(body().y1()+3);
		add(c);
		
		GScrollRows sc = new GScrollRows(rows, C.HEIGHT()-y1-(c.body().height()+3)*2, 0, false);
		addDownC(0, sc.view());
		
		c = new GButt.Glow(UI.decor().down) {
			@Override
			protected void renAction() {
				activeSet(t.get() != t.max());
			}
			@Override
			protected void clickA() {
				t.inc(1);
			}
		};
		addDownC(0, c);
		body().moveY1(y1);
		t = sc.target;
		

		
	}
	

	
	
	
	@Override
	public void render(SPRITE_RENDERER r, float ds) {
		if (visableIs()) {
			GCOLOR.UI().panBG.render(r, body());
			GCOLOR.UI().borderH(r, body(), 0);
			super.render(r, ds);
		}
		if (!MButt.LEFT.isDown())
			clickI = -1;
	}
	
	private static CharSequence ¤¤Desc = "¤An assortment of opinions from your citizens:";
	private static CharSequence ¤¤Double = "¤Double click to grant access to all immigrants. Right click to open immigration settings.";
	
	static {
		D.ts(UIMiniRaces.class);
	}
	
	static int clickI = -1;
	
	
	
	private static class RaceUI extends GuiSection {
		
		private double viewI = -60*5;
		private int cache = 0;
		private int old;
		private final int ri;
		private final ArrayList<Str> tmp = new ArrayList<>(
				new Str(128),
				new Str(128),
				new Str(128),
				new Str(128)
				);
		
		RaceUI(int ri){
			this.ri = ri;
			
			body().setWidth(Icon.M*2);
			addDownC(2, new SPRITE.Imp(Icon.M, Icon.M) {
				
				@Override
				public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
					Race res = FACTIONS.player().races.get(ri);
					if (STATS.POP().POP.data().get(res) == 0)
						COLOR.WHITE65.bind();;
					res.appearance().icon.render(r, X1, Y1);
					COLOR.unbind();
					
					if (VIEW.renderSecond() - viewI > 5) {
						old = cache;
						cache = SETT.ENTRY().immi().wanted(res);
						viewI = VIEW.renderSecond();
					}
				
					if (cache == 0)
						return;
					
					int am = CLAMP.i(cache/5, 1, 4);
					
					COLOR.BLACK.bind();
					for (int i = 0; i < am; i++)
						SPRITES.icons().s.arrow_right.render(r, X1-10+12*i, Y1-2);
					COLOR.YELLOW100.bind();
					
					if (cache > old)
						OPACITY.O25TO100.bind();
					for (int i = 0; i < am; i++)
						SPRITES.icons().s.arrow_right.render(r, X1-12+12*i, Y1-4);
					COLOR.unbind();
					OPACITY.unbind();
				}
			});
			
			addDownC(-2, new GStat() {
				
				@Override
				public void update(GText text) {
					Race res = FACTIONS.player().races.get(ri);
					GFORMAT.iBig(text, STATS.POP().POP.data(HCLASSES.CITIZEN()).get(res, 0)+SETT.ENTRY().immi().admitted(res));
				}
			}.bg().decrease().r(DIR.C));
			
			
			
			pad(0, 4);
		}
		
		int li = -1;
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			viewI = -5;
			GBox b = (GBox) text;
			Race res = FACTIONS.player().races.get(ri);
			b.title(b.text().add(HCLASSES.CITIZEN().names).add(':').s().add(res.info.names));
			b.text(res.info.desc);
			b.NL();
			
			b.add(b.text().lablifySub().add(STATS.POP().POP.info().name));
			b.add(b.text().add(STATS.POP().POP.data(HCLASSES.CITIZEN()).get(res, 0)));
			b.NL();
			b.add(b.text().lablifySub().add(STANDINGS.CITIZEN().info().name));
			b.add(GFORMAT.perc(b.text(), STANDINGS.CITIZEN().loyalty.getD(res)));
			b.NL(8);
			
			b.NL();
			SETT.ENTRY().immi().hoverImmigrants(b, res);
			
			b.NL(4);
			
			b.text(¤¤Double);
			
			b.NL(8);
			
			if (STATS.POP().POP.data(HCLASSES.CITIZEN()).get(res) > 0) {
				b.textL(¤¤Desc);
				b.NL();
				
				if (li < TIME.currentSecond()) {
					li = 60 + (int) TIME.currentSecond();
					ENTITY[] ee = SETT.ENTITIES().getAllEnts();
					int ei = RND.rInt(ee.length);
					for (int i = 0; i < ee.length; i++) {
						ENTITY e = ee[(ei+i)%ee.length];
						if (e instanceof Humanoid) {
							Humanoid a = (Humanoid) e;
							if (a.indu().player() && a.indu().clas() == HCLASSES.CITIZEN() && a.race() == res) {
								res.bio().opinions(tmp, a);
							}
						}
					}
					
					
				}

				for (Str s : tmp) {
					if (s.length() > 0) {
						b.add(b.text().warnify().add('\'').add(s).add('\''));
						b.NL(4);
					}
				}
			}
			
			super.hoverInfoGet(text);
		}
		
		@Override
		public void render(SPRITE_RENDERER r, float ds) {
			Race res = FACTIONS.player().races.get(ri);
			
			GCOLOR.UI().border().render(r, body().x1(), body().x2(), body().y1(), body().y1()+1);
			COLOR.WHITE05.render(r, body(),-1);
			double dfrom = STANDINGS.CITIZEN().loyalty.getD(res);
			double dto = STANDINGS.CITIZEN().loyalty.getD(res);
			
			
			
			if (hoveredIs())
				COLOR.WHITE30.render(r, body(), -3);
			else if (STATS.POP().POP.data(HCLASSES.CITIZEN()).get(res) > 0) {
				GMeter.renderDelta(r, dfrom, dto, body().x1()+1, body().x2()-1, body().y1()+2, body().y2()-1);
			}else {
				GMeter.renderDelta(r, dfrom, dto, body().x1()+1, body().x2()-1, body().y1()+2, body().y2()-1, GMeter.C_GRAY);
			}
			
			if (hoveredIs() && MButt.RIGHT.consumeClick()) {
				VIEW.s().ui.standing.open(res);
			}
			
			if (hoveredIs() && clickI >= 0 && clickI != ri) {
				
				COLOR.GREEN100.render(r, body().x1(), body().x2(), body().y1()-1, body().y1()+2);
				if (!MButt.LEFT.isDown()) {
					FACTIONS.player().races.order(FACTIONS.player().races.get(clickI), ri);
					clickI = -1;
				}
			}
			
			super.render(r, ds);
		}
		
		@Override
		public boolean click() {
			if (MButt.LEFT.isDouble()) {
				Race res = FACTIONS.player().races.get(ri);
				int am = SETT.ENTRY().immi().wanted(res);
				if (am > 0)
				SETT.ENTRY().immi().admit(res, am);
			}
			clickI = ri;
			
			return super.click();
		}
		
	}

	
	private static RENDEROBJ noble(int ri) {
		return new RaceUI(ri);
		
	}

}
