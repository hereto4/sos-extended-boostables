package view.ui.div;

import game.battle.util.DIV_SIMPLE;
import game.battle.util.DIV_SPEC;
import game.faction.Faction;
import init.race.Race;
import init.resources.RESOURCES;
import init.resources.ResSupply;
import init.sprite.SPRITES;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import settlement.main.SETT;
import settlement.stats.STATS;
import settlement.stats.colls.StatsBattle.StatTraining;
import settlement.stats.equip.EquipBattle;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.DIMENSION;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.Rec;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.text.Str;
import util.colors.GCOLOR;
import util.colors.GCOLOR_UI;
import util.gui.misc.GBox;
import util.gui.misc.GMeter;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.info.GFORMAT;
import util.text.D;
import util.text.Dic;
import world.army.AD;
import world.army.WDIV;
import world.army.WDivRegional;
import world.entity.army.WArmy;

public final class UIDivCardWorld implements DIMENSION{


	private static CharSequence ¤¤supHealth = "Supplies Health";
	private static CharSequence ¤¤supHealthD = "Supplies that are needed for general health. Initial supplies for 8 days of use must be available in warehouses, and will be sent to the army automatically.";
	private static CharSequence ¤¤supMorale = "Supplies Morale";
	private static CharSequence ¤¤supMoraleD = "Supplies that boosts morale. Will be sent if available automatically, but are not mandatory.";	
	private static CharSequence ¤¤supWarning = "Warning: you currently do not have any operational army supply depots for the needed resources! Once the supplies are gone, the unit might rout and be lost.";
	private static CharSequence ¤¤LowSupplies = "¤Not enough supplies to send out. Fill up your warehouses of essential army supplies.";
	


	private static CharSequence ¤¤NewConscripts = "¤Conscripts are training and will be ready in {0} days.";
	private static CharSequence ¤¤NewConscriptsProblem = "¤There are no conscripts to train for this division.";
	private static CharSequence ¤¤Training = "¤This division is currently training to reach the desired training level. Days left: {0}.";
	private static CharSequence ¤¤NotMustering = "¤This army is currently not mustering, and will not train conscripts.";
	
	static {
		D.ts(UIDivCardWorld.class);
	}
	
	private final GText tmp = new GText(UI.FONT().S, 5);
	
	private final Rec body = new Rec();
	private final int WIDTH;
	private final int HEIGHT;
	private final UIDiv m;

	private GuiSection sec = new GuiSection();
	private final UIDivStats stat = new UIDivStats();
	private WDIV current;
	
	private WDIV sd;
	private final DIV_SPEC stats = new DIV_SPEC() {
		
		@Override
		public double training(StatTraining tr) {
			return sd.target().training(tr);
		}
		
		@Override
		public double equip(EquipBattle e) {
			return sd.target().equip(e);
		}
		
		@Override
		public Race race() {
			return sd.race();
		}
		
		@Override
		public CharSequence name() {
			return sd.name();
		}
		
		@Override
		public int men() {
			return sd.menTarget();
		}
		
		@Override
		public Faction faction() {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public double experience() {
			return sd.experience();
		}
		
		@Override
		public int bannerI() {
			return sd.bannerI();
		}
	};
	
	UIDivCardWorld(UIDiv m) {
		this.m = m;
		WIDTH = m.WIDTH;
		HEIGHT =m.HEIGHT + 14;
		
		
		{
			GuiSection s = new GuiSection();
			
			for (EquipBattle e : STATS.EQUIP().BATTLE_ALL()) {
				SPRITE hh = new SPRITE.Imp(Icon.M) {
					
					@Override
					public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
						if (current.target().equip(e) == 0) {
							OPACITY.O50.bind();
						}
						e.resource.icon().render(r, X1, X2, Y1, Y2);
						OPACITY.unbind();
						
					}
				};
				
				RENDEROBJ o = new GStat() {
					
					@Override
					public void update(GText text) {
						if (current.target().equip(e) == 0) {
							text.color(COLOR.WHITE50).add('-');
						} else {
							double ee = (e.max()*10*(current.equip(e)))/10.0;
							int tar = current.target().equipI(e);
							if (ee == tar)
								GFORMAT.i(text, tar);
							else {
								GFORMAT.f(text, (e.max()*10*(current.equip(e)))/10.0, 1);
								text.add('/').add(current.target().equipI(e));
							}
							
						}
					}
				}.hh(hh);
				s.addGrid(o, e.indexMilitary(), 4, 48, 0);
				
			}
			
			GCOLOR.T().H1.bind();
			s.add(UI.icons().s.death, 0, s.body().y2()+2);
			
			s.addRightC(4, new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.percGood(text, ((int)100*(current.experience()))/100.0);
				}
			}.hh(Dic.¤¤Experience, 220));
			
			for (StatTraining tt : STATS.BATTLE().TRAINING_ALL) {
				s.add(tt.room.icon.small, 0, s.body().y2()+2);
				s.addRightC(4, new GStat() {
					
					@Override
					public void update(GText text) {
						
						int target = ((int)Math.round(100*current.target().training(tt)));
						int cu = (int) Math.round(100*current.training(tt));
						
						text.add(cu).add('/').add(target).add('%');
						if (target > 0)
							text.color(ColorImp.TMP.interpolate(GCOLOR.T().IBAD, GCOLOR.T().IGOOD, (double)cu/target));
						else
							text.color(GCOLOR.T().INACTIVE);
					}
				}.hh(tt.stat.info().name, 200));
				
			}
			
			s.add(UI.icons().s.sword, 0, s.body().y2()+8);
			s.addRightC(4, new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.iofk(text, current.men(), current.menTarget());
					
				}
			}.hh(Dic.¤¤Deployable, 200));
			
			
			sec.add(s);
			
			sec.addRelBody(8, DIR.W, new RENDEROBJ.RenderImp(WIDTH*2, HEIGHT*2) {
				
				@Override
				public void render(SPRITE_RENDERER r, float ds) {
					UIDivCardWorld.this.render(r, body.x1(), body.y1(), 2, current, true, false, false);
				}
			});
		}
	}
	
	@Override
	public int width() {
		return WIDTH;
	}

	@Override
	public int height() {
		return HEIGHT;
	}
	
	public void render(SPRITE_RENDERER r, int x1, int y1, int scale, WDIV d, boolean isActive, boolean isSelected, boolean isHovered) {
		
		
		body.set(x1,x1+width()*scale, y1, y1+height()*scale);
		
		GCOLOR_UI.color(d.color(), isActive, isSelected, isHovered).render(r, body, -1);
		GCOLOR.UI().bg(isActive, isSelected, isHovered).render(r, body, -3);
		
		//GButt.ButtPanel.renderBG(r, isActive, isSelected, isHovered, body);
		
		
		sd = d;
		m.renderBasics(r, x1, y1, scale, stats);

		WArmy a = d.army();
		if (d.needConscripts()) {
			if (d.army() != null && d.army().recruiting() && d.men() < d.menTarget()) {
				if (d.men() < d.menTarget()) {
					SPRITES.icons().s.time.renderC(r, body.x2()-6, body.y1()+8);
					tmp.clear();
					if ((a != null && !a.recruiting()) || d.menTarget() == 0)
						tmp.errorify().add('-');
					else
						tmp.normalify().add(d.daysUntilMenArrives());
					tmp.adjustWidth();
					tmp.render(r, body.x2()-2-tmp.width(), body.y1()+16);
				}
			}

		}
		
		int cx = body.cX();
		
		double men = d.menTarget();
		double n = d.men();
	
		double nn = (d.army() != null && d.army().recruiting()) && d.daysUntilMenArrives() >= 0 ? d.menTarget() : d.men();
		if (men == 0)
			GMeter.renderDelta(r, 0, 0, body);
		else
			GMeter.renderDelta(r, n/men, nn/men, body.x1()+4*scale, body.x2()-4*scale, body.y2()-22*scale, body.y2()-6*scale);

		
		UI.FONT().S.renderC(r, cx, body.y2()-14*scale, Str.TMP.clear().add(d.men()), scale);
		
		
		GCOLOR.UI().border().renderFrame(r, body, 0, 1);
		
		
		
	}
	

	
	public void hover(WDIV d, GUI_BOX box) {
		GBox b = (GBox) box;
		b.title(d.name());

		current = d;
		b.add(sec);
		b.NL();

		b.add(stat.get(d));

		b.NL(8);
		b.sep();
		
		if (d.costPerMan() > 0) {
			b.textL(Dic.¤¤InitialCost);
			b.tab(3);
			b.add(GFORMAT.i(b.text(), 4 * d.costPerMan() * d.menTarget()));
			b.NL();

			b.textL(Dic.¤¤Upkeep);
			b.tab(3);
			b.add(GFORMAT.i(b.text(), d.costPerMan() * d.menTarget()));
			b.NL();
		}

		if (d.needConscripts()) {
			if (d.army().recruiting()) {
				if (d.men() < d.menTarget()) {
					if (!AD.conscripts().canTrain(d.race(), d.faction())) {
						b.error(¤¤NewConscriptsProblem);
					} else {
						GText te = b.text();
						te.add(¤¤NewConscripts);
						te.insert(0, d.daysUntilMenArrives());
						te.normalify2();
						b.add(te);
					}
				} else {
					int tt = trainingTime(d);
					if (tt > 0) {
						GText te = b.text();
						te.add(¤¤Training);
						te.insert(0, tt);
						te.normalify2();
						b.add(te);

					}

				}
			} else if (d.men() < d.menTarget() || trainingTime(d) > 0) {
				b.error(¤¤NotMustering);
			}
		}
	}

	static int trainingTime(WDIV div) {
		int m = 0;
		for (StatTraining tr : STATS.BATTLE().TRAINING_ALL) {
			m += WDivRegional.trainingDays(tr, div.target().training(tr) - div.training(tr), div.faction());
		}
		return m;
	}
	
	public static CharSequence supplyError(DIV_SIMPLE div) {	
		
		for (ResSupply s : RESOURCES.SUP().ALL) {
			if (s.health > 0 && s.amount(div.race(), div.men()) > SETT.ROOMS().STOCKPILE.tally().amountReservable.get(s.resource))
				return ¤¤LowSupplies;
		}
		return null;
	}
	
	public static void hoverSendOut(LIST<? extends DIV_SIMPLE> divs, GUI_BOX box) {
		GBox b = (GBox) box;

		
		boolean sup = true;
		
		b.textLL(¤¤supHealth);
		b.NL();
		b.text(¤¤supHealthD);
		b.NL();
		for (ResSupply s : RESOURCES.SUP().ALL) {
			if (s.health <= 0)
				continue;
			int need = 0;
			for (DIV_SIMPLE div : divs) {
				need += s.amount(div.race(), div.men());
			}
			
			int available = SETT.ROOMS().STOCKPILE.tally().amountReservable.get(s.resource);
			
			b.add(s.resource.icon());
			GText t = b.text();
			GFORMAT.i(t, -need);
			if (need <= available)
				t.normalify2();
			else
				t.errorify();
			b.add(t);
			
			
			b.tab(3);
			b.add(SETT.ROOMS().STOCKPILE.icon.small);
			b.add(GFORMAT.i(b.text(), available));
			
			b.tab(6);
			
			if (SETT.ROOMS().SUPPLY.has(s.resource))
				b.add(SETT.ROOMS().SUPPLY.icon.small);
			else
				b.add(UI.icons().s.cancel, GCOLOR.UI().BAD.hovered);
			
			b.NL();
			if (need > 0 && !SETT.ROOMS().SUPPLY.has(s.resource))
				sup = false;
			
		}
		
		b.textLL(¤¤supMorale);
		b.NL();
		b.text(¤¤supMoraleD);
		b.NL();
		for (ResSupply s : RESOURCES.SUP().ALL) {
			if (s.morale <= 0)
				continue;
			int need = 0;
			for (DIV_SIMPLE div : divs) {
				need += s.amount(div.race(), div.men());
			}
			
			int available = SETT.ROOMS().STOCKPILE.tally().amountReservable.get(s.resource);
			
			b.add(s.resource.icon());
			GText t = b.text();
			GFORMAT.i(t, -need);
			if (need <= available)
				t.normalify2();
			else
				t.warnify();
			b.add(t);
			
			b.tab(3);
			b.add(SETT.ROOMS().STOCKPILE.icon.small);
			b.add(GFORMAT.i(b.text(), available));
			
			b.tab(6);
			
			if (SETT.ROOMS().SUPPLY.has(s.resource))
				b.add(SETT.ROOMS().SUPPLY.icon.small);
			else
				b.add(UI.icons().s.cancel, GCOLOR.UI().BAD.hovered);
			
			b.NL();
			if (need > 0 && !SETT.ROOMS().SUPPLY.has(s.resource))
				sup = false;
			
		}
		
		if (!sup) {
			b.add(SETT.ROOMS().SUPPLY.icon);
			b.warn(¤¤supWarning);
			b.NL();
		}
		
	}
}
