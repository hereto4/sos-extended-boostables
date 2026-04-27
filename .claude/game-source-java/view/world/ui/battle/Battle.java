package view.world.ui.battle;

import init.constant.Config;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.gui.Hoverable.HOVERABLE.HoverableAbs;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.CLAMP;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.text.Str;
import util.colors.GCOLOR;
import util.data.GETTER;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GMeter;
import util.gui.misc.GMeter.GMeterCol;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.table.GTableBuilder;
import util.gui.table.GTableBuilder.GRowBuilder;
import util.info.GFORMAT;
import util.text.D;
import util.text.Dic;
import world.WORLD;
import world.army.AD;
import world.army.ADSupplies.ADArtillery;
import world.battle.spec.WBattleSide;
import world.battle.spec.WBattleSpec;
import world.battle.spec.WBattleUnit;
import world.map.landmark.WorldLandmark;
import world.map.regions.Region;

abstract class Battle {

	private static CharSequence ¤¤battleOf = "Battle of {0}";
	private static CharSequence ¤¤battle = "Battle";
	public static CharSequence ¤¤Annihilation = "¤Annihilation";
	public static CharSequence ¤¤Command = "¤Command";
	public static CharSequence ¤¤autoD = "¤Auto resolve this battle. The result will be {0}. You will lose about {1} men and inflict about {2} casualties on the enemy.";
	public static CharSequence ¤¤AutoResolve = "¤Auto";
	public static CharSequence ¤¤defence = "¤This unit is defending and is given extra power due to their defensive position.";
	
	static {
		D.ts(Battle.class);
	}
	
	private final int WIDTH = 300;
	
	private boolean hovRetreat = false;
	private boolean hovAuto = false;
	protected WBattleSpec g;
	
	private final GuiSection sec = new GuiSection() {
		@Override
		public void render(SPRITE_RENDERER r, float ds) {
			super.render(r, ds);
			hovRetreat = false;
			hovAuto = false;
			CharSequence title = title(g);
			int w = UI.FONT().H2.width(title);
			UI.PANEL().titleBoxes[1].renderCY(r, body().cX()-w/2, body().y1()-16, w);
			GCOLOR.T().H1.bind();
			UI.FONT().H2.renderC(r, body().cX(), body().y1()-16, title);
			COLOR.unbind();
		}
	};
	
	Battle(CharSequence desc){
		
		GETTER<WBattleSide> pg = new GETTER<WBattleSide>() {

			@Override
			public WBattleSide get() {
				return g.player;
			}
			
		};
		sec.add(side(pg, GMeter.C_REDGREEN));
		GETTER<WBattleSide> sg = new GETTER<WBattleSide>() {

			@Override
			public WBattleSide get() {
				return g.enemy;
			}
			
		};
		sec.addRightC(8, side(sg, GMeter.C_REDORANGE));
		
		sec.addRelBody(16, DIR.N, balance(pg, sg));
		sec.addRelBody(8, DIR.N, new RENDEROBJ.RenderDummy(10, 8));
		
		CharSequence[] descs = UI.FONT().M.getRows(desc, WIDTH*2);
		
		for (CharSequence d : descs) {
			GText t = new GText(UI.FONT().M, d);
			t.warnify();
			sec.addRelBody(4, DIR.S, t);
		}
		sec.addRelBody(8, DIR.S, buttons());
		
	}
	
	
	
	protected CharSequence title(WBattleSpec g) {
		WorldLandmark m = WORLD.LANDMARKS().setter.get(g.player.coo());
		if (m == null) {
			Region reg = WORLD.REGIONS().map.get(g.player.coo());
			if (reg != null)
				return Str.TMP.clear().add(¤¤battleOf).insert(0, reg.info.name());
			return ¤¤battle;
		}else
			return Str.TMP.clear().add(¤¤battleOf).insert(0, m.name);
	}
	
	
	protected abstract RENDEROBJ buttons();
	
	
	public static GuiSection balance(GETTER<WBattleSide> player, GETTER<WBattleSide> enemy) {

		GuiSection s = new GuiSection();

		RENDEROBJ gg = new HOVERABLE.HoverableAbs(200, Icon.M) {

			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {

				double d = player.get().powerBalance();
				if (d < 0.5)
					GMeter.render(r, GMeter.C_RED, d, body);
				else
					GMeter.render(r, GMeter.C_BLUE, d, body);
			}

			@Override
			public void hoverInfoGet(GUI_BOX text) {
				text.title(Dic.¤¤Balance);
			}
		};

		s.addDownC(4, gg);
		s.addC(UI.icons().l.rebel, s.body().cX(), s.body().cY());

		int y1 = s.body().cY() - 12;

		s.add(new GStat(UI.FONT().M) {

			@Override
			public void update(GText text) {
				
				GFORMAT.iBig(text, player.get().men());
				text.normalify2();
			}
		}.r(DIR.NE), s.body().x1() - 80, y1);

		s.add(new GStat(UI.FONT().M) {

			@Override
			public void update(GText text) {
				GFORMAT.iBig(text, enemy.get().men());
				text.warnify();
			}
		}.r(DIR.NW), s.body().x2() + 80, y1);

		return s;

	}
	
	void setCas(boolean hovRetreat, boolean hovFight) {
		this.hovRetreat = hovRetreat;
		this.hovAuto = hovFight;
	}
	
	GuiSection get(WBattleSpec spec) {
		this.g = spec;
		return sec;
		
	}

	
	private GuiSection side(GETTER<WBattleSide> g, GMeterCol col) {
		GuiSection s = new GuiSection();
		
		{
			HoverableAbs h = new HoverableAbs(WIDTH, Icon.S) {
				
				@Override
				protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
					int am = 0;
					for (int i = 0; i < AD.supplies().arts().size(); i++) {
						am +=  g.get().artillery(AD.supplies().arts().get(i));
					}
					
					if (am <= 0)
						return;
					
					int d = (body().width()-50)/am;
					d = CLAMP.i(d, 1, 16);

					int i = 0;
					for (ADArtillery a : AD.supplies().arts()) {
						for (int k = 0; k < g.get().artillery(a); k++) {
							a.art.icon.small.render(r, body.x1()+i*d, body().y1());
							i++;
						}
					}
					
				}
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					GBox b = (GBox) text;
					for (int i = 0; i < AD.supplies().arts().size(); i++) {
						ADArtillery a = AD.supplies().arts().get(i);
						int am = g.get().artillery(a);
						if (am > 0) {
							b.add(AD.supplies().arts().get(i).art.icon.small);
							b.text(a.art.info.names);
							b.tab(7);
							b.add(GFORMAT.i(b.text(), am));
							b.NL();
						}
					}
				}
			};
			
			s.addDownC(0, h);
		}
		
		s.addDown(4, new Row(g, null, col));
		
		GTableBuilder bu = new GTableBuilder() {
			
			@Override
			public int nrOFEntries() {
				return g.get().units().size()-1;
			}
		};

		bu.column(null, WIDTH, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return new Row(g, ier, col);
			}
		});
		
		s.addDown(8, bu.create(4, false));
		
		SPRITE frame = GCOLOR.UI().border().makeFrame(s.body().width()+12, s.body().height()+12, 1);
		
		s.addC(frame, s.body().cX(), s.body().cY());
		
		return s;
		
	}
	
	private class Row extends HoverableAbs{
		private final GETTER<WBattleSide> g;
		private final GETTER<Integer> ier;
		private final GMeterCol col;
		Row(GETTER<WBattleSide> g, GETTER<Integer> ier, GMeterCol col){
			super(WIDTH, Icon.M);
			this.g = g;
			this.ier = ier;
			this.col = col;
		}
		
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
			WBattleUnit u = u();
			if (u == null)
				return;
			u.icon().renderCY(r, body.x1(), body.cY());
			
			int X1 = body.x1()+Icon.M + 4;
			int WI = body.x2() - X1-8;
			
			
			double dmen = Math.sqrt((double) u.men() / Config.battle().MEN_PER_ARMY);
			int X2 = (int) (X1 + WI * dmen);

			int losses = 0;
			
			if (hovRetreat)
				losses = u.lossesRetreat();
			if (hovAuto) {
				losses = u.losses();		
			}
			
			double d = (double) (u.men() - losses) / u.men();
			
			GMeter.render(r, col, d, X1, X2, body.y1()+2, body.y2()-2);
			
			GMeter.renderDelta(r, 1.0, d, X1, X2, body.y1()+2, body.y2()-2, col);
			
//			if (u.defences() >= 1) {
//				UI.icons().s.degrade.renderCY(r, X1+WI, body.cY());
//				Str.TMP.clear().add((int)u.defences());
//				UI.FONT().S.renderCY(r, X1+WI+18, body.cY(), Str.TMP);
//			}
			
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			WBattleUnit u = u();
			if (u == null)
				return;
			u.hover(text);
			super.hoverInfoGet(text);
			
			if (u.defences() >= 1) {
				GBox b = (GBox) text;
				b.sep();
				b.text(¤¤defence);
			}
			
		}
		
		private WBattleUnit u() {
			WBattleSide s = g.get();
			if (s == null)
				return null;
			int ui = ier == null ? 0: ier.get()+1;
			WBattleUnit u = s.units().get(ui);
			if (u == null)
				return null;
			return u;
		}
		
	}
	
	
	static class Butt extends GButt.ButtPanel {

		public Butt(SPRITE icon, CharSequence label) {
			super(label);
			icon(UI.icons().s.arrow_left);
			body.setWidth(200);
		}
	}
	
}
