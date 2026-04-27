package view.ui.div;

import game.GAME;
import game.battle.div.Div;
import game.battle.util.DIV_SPEC;
import game.boosting.BOOSTABLES;
import game.boosting.BOOSTABLES.BDamage;
import game.boosting.BoostSpec;
import game.boosting.Boostable;
import game.boosting.Booster;
import game.faction.Faction;
import init.constant.C;
import init.race.RACES;
import init.race.Race;
import init.sprite.UI.UI;
import settlement.stats.STATS;
import settlement.stats.colls.StatsBattle.StatTraining;
import settlement.stats.equip.EquipBattle;
import settlement.stats.equip.EquipRange;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.gui.Hoverable.HOVERABLE.HoverableAbs;
import snake2d.util.sprite.SPRITE;
import util.gui.misc.GBox;
import util.gui.misc.GMeter;
import util.gui.misc.GMeter.GMeterCol;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.info.GFORMAT;
import util.text.D;
import util.text.Dic;

public final class UIDivStats {

	private static CharSequence ¤¤Config = "Configuration";

	static {
		D.ts(UIDivStats.class);
	}
	
	private static int width = 300;
	private static final int height = 20;
	
	private Div sdiv;
	
	private final DIV_SPEC spec = new DIV_SPEC() {
		
		@Override
		public Race race() {
			return sdiv.race();
		}
		
		@Override
		public double training(StatTraining tr) {
			return tr.stat.div().getD(sdiv);
		}
		
		@Override
		public int men() {
			return sdiv.menNrOf();
		}
		
		@Override
		public double equip(EquipBattle e) {
			return e.stat().div().getD(sdiv);
		}
		
		@Override
		public CharSequence name() {
			return sdiv.info.name();
		}
		
		@Override
		public Faction faction() {
			return sdiv.faction();
		}
		
		@Override
		public double experience() {
			return STATS.BATTLE().COMBAT_EXPERIENCE.div().getD(sdiv);
		}
		
		@Override
		public int bannerI() {
			return sdiv.info.bannerI();
		}
	};
	
	private DIV_SPEC div;
	
	private final GuiSection ss = new GuiSection();
	
	public UIDivStats(){

		pair(BOOSTABLES.BATTLE().OFFENCE, BOOSTABLES.BATTLE().DEXTERITY, BOOSTABLES.BATTLE().CHARGE, GMeter.C_YELLOW);
		pair(BOOSTABLES.BATTLE().DEFENCE, BOOSTABLES.BATTLE().PARRY, BOOSTABLES.BATTLE().FORMATION, GMeter.C_GREEN);
		
		ss.body().incrH(4);
		
		pair(BOOSTABLES.BATTLE().BLUNT_ATTACK, BOOSTABLES.BATTLE().BLUNT_DEFENCE_DIR, BOOSTABLES.BATTLE().BLUNT_DEFENCE);

		for (BDamage bb : BOOSTABLES.BATTLE().DAMAGES) {
			pair(bb.attack, bb.defenceDir, bb.defence);
		}
		
		icon(BOOSTABLES.BATTLE().MORALE.icon);
		ss.addRightC(4, new GaugeBo(BOOSTABLES.BATTLE().MORALE, GMeter.C_BLUE, width, height));
		
		
//		add(ss, jj);
//		
		{
			
			icon(UI.icons().s.bow);
			
			HoverableAbs aa = new HoverableAbs(width, height) {
				
				@Override
				protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
					double def = 0;
					double curr = 0;
					double max = 0;
					EquipRange rr = best(div);
					if (rr != null) {
						def = rr.projectile.range(0, rr.ref(0, 0));
						curr = rr.projectile.range(0, rr.ref(div.equip(rr), GAME.battle().boost(div, rr.boostable)));
						for (EquipRange e : STATS.EQUIP().RANGED()) {
							max = Math.max(max, e.projectile.range(0, rr.ref(1.0, rr.boostable.max(Div.class))));
						}
						def /=C.TILE_SIZE;
						curr/= C.TILE_SIZE;
						max /= C.TILE_SIZE;
					}
					GMeter.renderDelta(r, def/max, curr/max, body.x1(), body().x2(), body.y1(), body.cY(), GMeter.C_YELLOW);
					if (rr != null) {
						def = GAME.battle().power.range(rr, 0);
						curr = GAME.battle().power.range(rr, rr.ref(div.equip(rr), GAME.battle().boost(div, rr.boostable)));
					}
					max = GAME.battle().power.bestRangedPower()+1;
					GMeter.renderDelta(r, def/max, curr/max, body.x1(), body().x2(), body.cY(), body.y2(), GMeter.C_ORANGE);
				}
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					GBox box = (GBox) text;
					box.title(Dic.¤¤Ammunition);
					EquipRange b = best(div);
					
					if (b != null) {
						//box.add(GFORMAT.f0(box.text(), b.ref(div.equip(b), BattleBoosts.res().get(b.boostable))));
						b.projectile.hover(box, null, b.ref(div.equip(b), GAME.battle().boost(div, b.boostable)), 0);
					}
					
				}
			};
			ss.addRightC(4, aa);
			
		}
		
		GuiSection oo = new GuiSection();

//		oo.addDown(4, simple(BOOSTABLES.BATTLE().FORMATION));
//		oo.addDown(4, simple(BOOSTABLES.BATTLE().CHARGE));
		oo.addDown(4, simple(BOOSTABLES.PHYSICS().SPEED));
		oo.addDown(4, simple(BOOSTABLES.PHYSICS().MASS));
		oo.addDown(4, simple(BOOSTABLES.PHYSICS().STAMINA));
		oo.addDown(4, new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.f0(text, GAME.battle().power.get(div));
			}
			
			@Override
			public void hoverInfoGet(GBox b) {
				GAME.battle().power.hover(b, div);
			};
			
		}.hh(UI.icons().s.fist));
		
		
		ss.addRelBody(16, DIR.E, oo);
		ss.body().incrW(64);
//		
//		for (Boostable bo : BattleBoosts.OTHERS()) {
//			if (bo == BOOSTABLES.BATTLE().OFFENCE || bo == BOOSTABLES.BATTLE().DEFENCE)
//				continue;
//			
//			RENDEROBJ jj = new Pair(bo.icon, dwidth, new GaugeBo(bo, GMeter.C_BLUE));
//			add(ss, jj);
//		}
	}
	
	public GuiSection get(DIV_SPEC div) {
		sdiv = null;
		this.div = div;
		return ss;
	}
	
	public GuiSection get(Div div) {
		this.sdiv = div;
		this.div = spec;
		return ss;
	}
	
	private static EquipRange best(DIV_SPEC div) {
		double max = 0;
		EquipRange b = null;
		for (EquipRange rr : STATS.EQUIP().RANGED()) {
			if (div.equip(rr) > 0) {
				double m = GAME.battle().power.range(rr, rr.ref(div.equip(rr), GAME.battle().boost(div, rr.boostable)));
				if (m > max) {
					max = m;
					b = rr;
				}
			}
				
		}
		return b;
	}
	
	private void pair(Boostable a, Boostable b, Boostable c, GMeterCol col) {
		
		icon(a.icon);
		ss.addRight(4, new GaugeBo(a, col, width/3, height));
		ss.addRight(0, new GaugeBo(b, col, width/3, height));
		ss.addRight(0, new GaugeBo(c, col, width/3, height));
	}
	
	private void pair(Boostable a, Boostable b, Boostable c) {
		
		icon(a.icon);
		ss.addRight(4, new GaugeBo(a, GMeter.C_YELLOW, width/3, height));
		ss.addRight(0, new GaugeBo(b, GMeter.C_GREEN, width/3, height));
		ss.addRight(0, new GaugeBo(c, GMeter.C_GREEN, width/3, height));
	}
	
	private void icon(SPRITE icon) {
		ss.add(icon, 0,  ss.body().y2());
	}
	
	private HOVERABLE simple(Boostable bo) {
		return new GStat() {
			
			@Override
			public void update(GText text) {
				double v = GAME.battle().boost(div, bo);
				if (sdiv != null)
					v = bo.get(sdiv);
				GFORMAT.fRel(text, v, div.race().bvalue(bo));
			}
			
			@Override
			public void hoverInfoGet(GBox b) {
				hoverI(div, bo, b);
			};
			
		}.hh(bo.icon);
	}
	
	public void hoverI(DIV_SPEC st, Boostable bo, GUI_BOX text) {
		
		
		
		GBox b = (GBox) text;
		b.title(bo.name);
		b.text(bo.desc);
		b.sep();
		
		if (sdiv != null) {
			bo.hoverDetailed(text, sdiv, bo.name, true);
		}else {
			
			double base = bo.baseValue;
			double tot = GAME.battle().boost(st, bo);
			double withoutRace = tot;
			
			for (BoostSpec s : st.race().boosts.all()) {
				if (s.boostable == bo) {
					if (s.booster.isMul)
						withoutRace /= s.booster.to();
				}
			}
			for (BoostSpec s : st.race().boosts.all()) {
				if (s.boostable == bo) {
					if (!s.booster.isMul)
						withoutRace -= s.booster.to();
				}
			}
			
			double withOutFaction = withoutRace;
			for (Booster ss : bo.fGlobal) {
				if (ss.isMul)
					withOutFaction /= ss.get(st.faction());
			}
			
			for (Booster ss : bo.fGlobal) {
				if (!ss.isMul)
					withOutFaction -= ss.get(st.faction());
			}
			
			double race = tot-withoutRace;
			double fac = withoutRace-withOutFaction;
			double con = tot-race-fac-base;
			b.textLL(Dic.¤¤Base);
			b.tab(7);
			b.add(GFORMAT.f(b.text(), base));
			b.NL(2);
			
			b.textLL(RACES.name());
			b.tab(7);
			b.add(GFORMAT.f0(b.text(), race));
			b.NL(2);
			b.textLL(Dic.¤¤Faction);
			b.tab(7);
			b.add(GFORMAT.f0(b.text(), fac));
			b.NL(2);
			b.textLL(¤¤Config);
			b.tab(7);
			b.add(GFORMAT.f0(b.text(), con));
			b.NL(8);
			b.textLL(Dic.¤¤Total);
			b.tab(7);
			b.add(GFORMAT.f0(b.text(), tot));
		}
		
		
		
	}
	
	private class GaugeBo extends Gauge {

		private final Boostable bo;
		
		GaugeBo(Boostable bo, GMeterCol col, int width, int height){
			super(col, width, height);
			this.bo = bo;
		}
		

		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
			double max = bo.max(Div.class);
			double rr = div.race().bvalue(bo);
			double d = GAME.battle().boost(div, bo);
			if (sdiv != null)
				d = bo.get(sdiv);
			
			render(r, rr, d, max);
		}


		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			hoverI(div, bo, text);
		}

		
		
	}
	


	

	private static abstract class Gauge extends HoverableAbs {
		
		public final GMeterCol col;
		private final GText tt = new GText(UI.FONT().S, 4);
		
		Gauge(GMeterCol col, int width, int height){
			super(width, height);
			this.col = col;
		}

		protected void render(SPRITE_RENDERER r, double def, double current, double max) {
			
			int X1 = body.x1();
			int X2 = body.x2();
			int Y1 = body.y1();
			int Y2 = body.y2();
			GMeter.renderDelta(r, def/max, current/max, X1, X2, Y1, Y2, col);
			
			tt.clear();
			GFORMAT.f(tt, current, 1);
			tt.adjustWidth();
			OPACITY.O35.bind();
			
			X2 -= 4;
			X1 = X2-tt.width()-8;
			Y1 += ((Y2-Y1)-tt.height())/2;
			Y2 = Y1 + tt.height();
			
			COLOR.BLACK.render(r, X1, X2, Y1, Y2);
			OPACITY.unbind();
			tt.render(r, X1+4, Y1);
			
		}

		
		
	}
	
	


	
}
