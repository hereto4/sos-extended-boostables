package view.world.ui.army;

import java.util.Arrays;

import game.faction.FACTIONS;
import game.faction.Faction;
import init.constant.Config;
import init.race.RACES;
import init.resources.RESOURCE;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import snake2d.MButt;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.ArrayList;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.text.Str;
import snake2d.util.sprite.text.StringInputSprite;
import util.colors.GCOLOR;
import util.data.GETTER;
import util.data.INT.INTE;
import util.gui.common.UIPickerArmy;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GInput;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.slider.GSliderInt;
import util.gui.table.GScrollRows;
import util.info.GFORMAT;
import util.text.D;
import util.text.Dic;
import view.interrupter.ISidePanel;
import view.keyboard.KEYS;
import view.main.VIEW;
import view.sett.ui.army.UIArmyCitySendOut;
import view.ui.div.UIDivBannerEditor;
import view.ui.div.UIDivEditor;
import world.WORLD;
import world.army.AD;
import world.army.ADDiv;
import world.army.ADStats.ADStat;
import world.army.ADSupplies.ADArtillery;
import world.army.WDivRegional;
import world.entity.army.WArmy;
import world.region.RD;
import world.region.pop.RDRace;

final class Army extends ISidePanel{

	static WArmy army;
	private boolean[] selected = new boolean[Config.battle().DIVISIONS_PER_ARMY];
	private Button clicked;
	private boolean dragging;
	private ToolMove tool = new ToolMove();
	private int selectedAm;
	private UIDivBannerEditor banner = new UIDivBannerEditor() {
		
		@Override
		public void bannerISet(int bi) {
			super.bannerISet(bi);
			for (int i = 0; i < selected.length; i++) {
				if (selected[i]) {
					if (army == null || army.divs() == null || army.divs().get(i) == null)
						continue;
					army.divs().get(i).bannerSet(bi);
				}
			}
		};
		
	};
	
	private static CharSequence ¤¤RecruitD = "Recruit the local soldiers. These use up the conscripts pool of your realm, and once trained they will need to be supplied with resources from your capital through the 'military supply depot'.";
	private static CharSequence ¤¤RecruitDProb = "No conscripts are available in your realm.";
	private static CharSequence ¤¤RecruitSettD = "Have your city troops join this army.";
	private static CharSequence ¤¤DisbandArmy = "Disband entire army.";
	private static CharSequence ¤¤Sure = "Are you sure you wish to:";
	private static CharSequence ¤¤DisbandDiv = "Disband {0} divisions?";
	
	private final StringInputSprite name = new StringInputSprite(20, UI.FONT().H2) {
		@Override
		protected void change() {
			army.name.clear().add(text());
		};
	};
	
	public Army() {
		
		D.t(this);
		titleSet(Dic.¤¤Army);
		
		GETTER<WArmy> gg = new GETTER<WArmy>() {

			@Override
			public WArmy get() {
				return army;
			}
		};
		
		section = new GuiSection() {
			
			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				selectedAm = 0;
				for (boolean s : selected)
					if (s) {
						selectedAm ++;
					}
				super.render(r, ds);
				
				dragging &= MButt.LEFT.isDown();
			}
		};
		
		
		
		section.add(new GInput(name));
		
		{
			ADStat[] sss = new ADStat[] {
					AD.stats().wins,AD.stats().defeats,AD.stats().kills,AD.stats().losses,AD.stats().siegeWon
				};
			
			GuiSection sec = new GuiSection();
			int gi = 0;
			for (ADStat s : sss) {
				RENDEROBJ o = new GStat() {
					@Override
					public void update(GText text) {
						GFORMAT.iIncr(text, s.a().get(army));
					}
				}.hh(s.name, 180).hoverInfoSet(s.desc);
				sec.addGridD(o, gi++, 2, section.body().width()/2, 18, DIR.NW);
			}
			sec.body().setWidth(section.body().width());
			section.addRelBody(4, DIR.S, sec);
			
			section.addRelBody(4, DIR.S, new SPRITE.Imp(section.body().width(), 2) {
				
				@Override
				public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
					GCOLOR.UI().border().render(r, X1, X2, Y1, Y2-1);
					ColorImp.TMP.set(GCOLOR.UI().border()).shadeSelf(0.75).render(r, X1, X2, Y2-1, Y2);
				}
			});
		}
		
		{
		
			GuiSection s = new GuiSection();
			s.add(ArmyInfo.info(gg));
			
			
			
			s.addRelBody(8, DIR.S, ArmyInfo.supplies(gg));
			
			GuiSection ss = new GuiSection();
			ss.add(new GButt.ButtPanel(new SPRITE.Twin(SPRITES.icons().m.crossair, SPRITES.icons().m.anti)) {
				
				@Override
				protected void clickA() {
					army.stop();
				}
				
				@Override
				protected void renAction() {
					activeSet(army.path().moving(army.body()));
				}
				
			}.pad(6, 2).hoverTitleSet(Dic.¤¤Stop));
			
			ss.addRightC(0, new GButt.ButtPanel(SPRITES.icons().m.rebellion) {
				
				@Override
				protected void clickA() {
					army.raid(!army.raiding());
				}
				
				@Override
				protected void renAction() {
					activeSet(army.canRaid());
					selectedSet(army.raiding());
				}
				
			}.pad(6, 2).hoverTitleSet(Dic.¤¤Raiding).hoverInfoSet(Dic.¤¤RaidingD));
			
			ss.add(new GButt.ButtPanel(SPRITES.icons().m.cancel) {
				
				ACTION a = new ACTION() {
					
					@Override
					public void exe() {
						army.disband();
						last().remove(Army.this);
					}
				};
				
				@Override
				protected void clickA() {
					Str.TMP.clear().add(¤¤Sure).s().add(¤¤DisbandArmy);
					VIEW.inters().yesNo.activate(Str.TMP, a, ACTION.NOP, true);
				}
				
				@Override
				protected void renAction() {
					activeSet(AD.menTarget(null).get(army) == 0);
				}
				
			}.pad(6, 2).hoverTitleSet(Dic.¤¤Disband).hoverInfoSet(¤¤DisbandArmy), 0, ss.body().y2());
			
			s.addRelBody(16, DIR.E, ss);
			section.addRelBody(8, DIR.S, s);
		}



		
		{
			GuiSection ss = new GuiSection();
			int gi = 0;
			for (ADArtillery a : AD.supplies().arts()) {
				INTE ii = new INTE() {
					
					@Override
					public int min() {
						return 0;
					}
					
					@Override
					public int max() {
						return a.target.max(army);
					}
					
					@Override
					public int get() {
						return a.target.get(army);
					}
					
					@Override
					public void set(int t) {
						a.target.set(army, t);
					}
				};
				GuiSection s = new GuiSection() {
					
					@Override
					public void hoverInfoGet(GUI_BOX text) {
						super.hoverInfoGet(text);
						if (text.emptyIs()) {
							GBox b = (GBox) text;
							b.title(a.art.info.names);
							b.text(a.art.info.desc);
							b.NL();
							for (int i = 0; i < a.art.constructor().resources(); i++) {
								RESOURCE res = a.art.constructor().resource(i);
								int am = (int) Math.ceil(a.art.constructor().item(1).cost2(i, a.art.upgrades().max()));
								b.add(res.icon());
								b.add(GFORMAT.iIncr(b.text(), -am));
								b.NL();
							}
							b.NL();
							a.art.projectile.hover(b, title, 1.0, 0);
						}
					}
					
				};
				s.add(a.art.iconBig(), 0, 0);
				GSliderInt in = new GSliderInt(ii, 120, true);
				s.addRight(8, in);
				ss.addGridD(s, gi++, 4, 220, 48, DIR.NW);
			}
			section.addRelBody(8, DIR.S, ss);
		}
		
		
		section.addRelBody(4, DIR.S, new SPRITE.Imp(section.body().width(), 2) {
			
			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				GCOLOR.UI().border().render(r, X1, X2, Y1, Y2-1);
				ColorImp.TMP.set(GCOLOR.UI().border()).shadeSelf(0.75).render(r, X1, X2, Y2-1, Y2);
			}
		});
		
		{
			
			GuiSection ss = new GuiSection();
			
			COLOR bg = COLOR.ORANGE100.makeSaturated(0.25);
			
			ss.add(new GButt.ButtPanel(SPRITES.icons().m.for_muster) {
				
				private ArmyRecruit s = new ArmyRecruit();
				
				@Override
				protected void clickA() {
					VIEW.inters().popup.push(s, this);
				}
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					GBox b = (GBox) text;
					b.title(Dic.¤¤Recruit);
					b.text(¤¤RecruitD);
					b.NL(8);
					
					if (AD.conscripts().available(null).get(FACTIONS.player()) <= 0) {
						b.error(¤¤RecruitDProb);
						b.NL(8);
					}
					
					for (RDRace r : RD.RACES().all) {
						b.add(r.race.appearance().icon);
						b.add(GFORMAT.iIncr(b.text(), AD.conscripts().available(r.race).get(FACTIONS.player())));
						b.NL();
					}
					
				}
				
			}.bg(bg).pad(6, 2), 0, section.getLastY2()+8);
			
			ss.addRightC(0, new GButt.ButtPanel(SPRITES.icons().m.city) {
				
				final UIArmyCitySendOut city = new UIArmyCitySendOut();
				
				@Override
				protected void clickA() {
					city.init(army);
					VIEW.inters().popup.show(city, this);
				}
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					text.text(¤¤RecruitSettD);
					
					
				}
				
			}.bg(bg).pad(6, 2));
			
			
			ss.addRightC(0, new GButt.ButtPanel(SPRITES.icons().m.coins) {
				
				private UIMerenaries s = new UIMerenaries();
				
				@Override
				protected void clickA() {
					VIEW.inters().popup.show(s.get(), this);
				}
				
			}.bg(bg).pad(8, 2).hoverTitleSet(Dic.¤¤Mercenaries).hoverInfoSet(D.g("mercenaryD", "Hiring mercenaries is instant and they equip, feed and replenish themselves. They cost credits to hire and requires credits in upkeep.")));
			
			
			ss.addRightC(48, new GButt.Glow(SPRITES.icons().m.questionmark) {
				
				@Override
				protected void clickA() {
					
				}
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					Str tmp = Str.TMP.clear().add(Dic.¤¤Unitinfo);
					tmp.insert(0, KEYS.MAIN().UNDO.repr());
					tmp.insert(1, KEYS.MAIN().MOD.repr());
					text.text(tmp);
				};
				
				
			});
			
			ss.addRightC(4, new GButt.ButtPanel(SPRITES.icons().m.place_brush) {
				
				@Override
				protected void clickA() {
					if (activeIs())
						VIEW.inters().popup.show(banner.view(), this);
					
				}
				
				@Override
				protected void renAction() {
					activeSet(selectedAm > 0);
				};
				
				
			}.hoverTitleSet(Dic.¤¤Banner));
			
			ss.addRightC(4, new GButt.ButtPanel(SPRITES.icons().m.menu) {
				
				UIDivEditor pop = new UIDivEditor(0.75, false, false, true, RACES.playable());
				{
					pop.addRelBody(8, DIR.S, new GButt.ButtPanel(Dic.¤¤confirm){
						
						@Override
						protected void clickA() {
							
							for (int i = 0; i < army.divs().size(); i++) {
								if (selected[i] && army.divs().get(i) instanceof WDivRegional) {
									WDivRegional d = (WDivRegional) army.divs().get(i);
									pop.copyChanges(d.target);
								}
							}
							VIEW.inters().popup.close();

						}
						
					});
				}
				
				
				@Override
				protected void clickA() {
					for (int i = 0; i < selected.length; i++) {
						if (selected[i]  && army.divs().get(i) instanceof WDivRegional) {
							WDivRegional d = (WDivRegional) army.divs().get(i);
							pop.div().copyFrom(d.target);
							pop.clearChanges();
							VIEW.inters().popup.show(pop, this);
							return;
						}
					}
					
					
				}
				
				@Override
				protected void renAction() {
					activeSet(false);
					for (int i = 0; i < army.divs().size(); i++) {
						if (selected[i] && army.divs().get(i) instanceof WDivRegional) {
							activeSet(true);
						}
					}
				};
				
				
			}.hoverTitleSet(Dic.¤¤Edit));
			
			
			ss.addRightC(0, new GButt.ButtPanel(SPRITES.icons().m.arrow_right) {
				
				GETTER<Faction> g = new GETTER.GETTER_IMP<Faction>(FACTIONS.player());
				
				
				
				UIPickerArmy p = new UIPickerArmy(g, 400) {
					
					@Override
					protected void pick(WArmy a) {
						int off = 0;
						for (int i = 0; i < selected.length; i++) {
							if (selected[i]) {
								army.divs().get(i-off).reassign(a);
								off++;
								selected[i] = false;
							}
						}
						VIEW.inters().popup.close();
					}
					
					@Override
					protected boolean canBePicked(WArmy a) {
						
						return a != army && a.divs().size() + selectedAm < Config.battle().DIVISIONS_PER_ARMY && Math.abs(a.ctx()-army.ctx()) + Math.abs(a.cty()-army.cty()) < 4;
						
					}
				};
				
				@Override
				protected void renAction() {
					activeSet(false);
					if (selectedAm > 0) {
						for (WArmy a : FACTIONS.player().armies().all()) {
							if (a != army && Math.abs(a.ctx()-army.ctx()) + Math.abs(a.cty()-army.cty()) < 4) {
								activeSet(true);
								return;
							}
						}
					}
				};
				
				@Override
				protected void clickA() {
					VIEW.inters().popup.show(p, this);
				};
				
				
			}.hoverTitleSet(Dic.¤¤Reassign).hoverInfoSet(D.g("MoveD", "Move this division to another army. The other army must be within 3 tiles.")));
			
			ss.addRightC(0, new GButt.ButtPanel(SPRITES.icons().m.cancel) {
				
				ACTION a = new ACTION() {
					
					@Override
					public void exe() {
						int off = 0;
						for (int i = 0; i < selected.length; i++) {
							if (selected[i]) {
								if (army == null || army.divs() == null || army.divs().get(i-off) == null)
									continue;
								army.divs().get(i-off).disband();
								off++;
								selected[i] = false;
							}
						}
						clicked = null;
						dragging = false;
					}
				};
				
				@Override
				protected void clickA() {
					Str.TMP.clear().add(¤¤Sure).s().add(¤¤DisbandDiv).insert(0, selectedAm);
					VIEW.inters().yesNo.activate(Str.TMP, a, ACTION.NOP, true);
					clicked = null;
					dragging = false;
				}
				
				@Override
				protected void renAction() {
					activeSet(false);
					for (boolean s : selected)
						if (s) {
							activeSet(true);
							return;
						}
				};
				
				@Override
				public void hoverInfoGet(GUI_BOX box){
					box.title(Dic.¤¤Disband);
					Str.TMP.clear().add(¤¤DisbandDiv).insert(0, selectedAm);
					box.text(Str.TMP);
				}
				
				
			});
			
			

			
			section.addRelBody(8, DIR.S, ss);
			
		}
		
		{
			GuiSection ss = new GuiSection();
			section.addRelBody(8, DIR.S, ss);
		}
		
		
		int am = 8;
		
		ArrayList<GuiSection> rows = new ArrayList<GuiSection>((int)Math.ceil(Config.battle().DIVISIONS_PER_ARMY/(double)am));

		for (int i = 0; i < rows.max(); i++) {
			rows.add(new GuiSection());
		}
		
		
		for (int i = 0; i < Config.battle().DIVISIONS_PER_ARMY; i++) {
			
			GuiSection s = rows.get(i/am);
			s.addRightC(2, new Button(i));
		}
		
		
		section.addRelBody(8, DIR.S, new GScrollRows(rows, HEIGHT-section.getLastY2()-8).view());
		
	}
	

	
	
	ISidePanel get(WArmy army) {
		Army.army = army;
		name.text().clear().add(army.name);
		dragging = false;
		Arrays.fill(selected, false);
		clicked = null;
		VIEW.world().tools.place(tool, tool.config, false);
		return this;
	}
	
	@Override
	protected void update(float ds) {
		WORLD.OVERLAY().things.hover(army.body(), GCOLOR.MAP().get(army.faction()), false, M);
		if (!army.added() && last() != null && last().added(this)) {
			last().remove(this);
		}
	}
	
	private class Button extends CLICKABLE.ClickableAbs {
		
		private final int ii;
		
		Button(int ii){
			this.ii = ii;
			body.setDim(VIEW.UI().div.world);
		}

		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
			activeSet(ii < army.divs().size());
			if (ii >= army.divs().size())
				return;
			selectedSet(selected[ii]);
			GCOLOR.UI().border().render(r, body);
			ADDiv d = army.divs().get(ii);
			VIEW.UI().div.world.render(r, body().x1(), body().y1(), 1, d, d.men() > 0, isSelected, isHovered);
			
			if (dragging && isHovered && clicked != null && clicked != this && !KEYS.MAIN().UNDO.isPressed() && !KEYS.MAIN().MOD.isPressed()) {
				COLOR.GREEN100.render(r, body().x1()-2, body().x1()+2, body().y1(), body().y2());
				if (!MButt.LEFT.isDown()) {
					army.divs().insert(ii, clicked.ii);
					selected[clicked.ii] = selected[ii];
					selected[ii] = false;
				}
			}
			
			
			
		}

		@Override
		public void hoverInfoGet(GUI_BOX text) {
			if (ii >= army.divs().size())
				return;
			GBox b = (GBox) text;
			ADDiv d = army.divs().get(ii);
			VIEW.UI().div.world.hover(d, b);
		}
		
		@Override
		protected void clickA() {
			
			if (KEYS.MAIN().UNDO.isPressed() && clicked != null) {
				Arrays.fill(selected, false);
				if (clicked.ii < ii) {
					for (int i = clicked.ii; i <= ii; i++) {
						selected[i] = true;
					}
					
					
				}else {
					for (int i = ii; i <= clicked.ii; i++) {
						selected[i] = true;
					}
				}
				
				
			}else if(KEYS.MAIN().MOD.isPressed()) {
				selected[ii] = !selected[ii];
			}else {
				Arrays.fill(selected, false);
				selected[ii] = true;
				clicked = this;
				dragging = true;
			}
		}
		
	}
	
	
	
}
