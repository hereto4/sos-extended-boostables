package view.battle;

import game.GAME;
import game.battle.div.Div;
import game.battle.formation.DivFormationImp;
import game.battle.state.BattleState;
import game.battle.state.BattleStateExiter;
import game.battle.state.BattleStateResult;
import game.save.GameLoader;
import init.constant.C;
import init.constant.Config;
import init.paths.PATHS;
import init.settings.S;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import init.type.HCLASSES;
import init.type.HTYPES;
import menu.Menu;
import settlement.entity.EntityIterator;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.room.military.artillery.ROOM_ARTILLERY;
import snake2d.CORE;
import snake2d.CORE_STATE;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.misc.ACTION;
import snake2d.util.sprite.text.Str;
import snake2d.util.sprite.text.Text;
import util.colors.GCOLOR;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.info.GFORMAT;
import util.text.D;
import util.text.Dic;
import view.interrupter.ISidePanels;
import view.main.VIEW;
import view.subview.GameWindow;
import view.ui.top.UIPanelTop;
import world.battle.spec.BATTLE_RESULT;

public final class BattlePanel {
	
	private final UIPanelArtillery cards_cata;
	private final UIPanelUnitCards cards_player;
	private final UIPanelUnitCards cards_enemy;
	
	public static CharSequence ¤¤MusterDesc = "¤Muster Men. Calls to arm and have your subjects form up in their division and man artillery, or un-muster men and have them return to their civil duties.";
	public static CharSequence ¤¤MusterOneProblem = "¤One or more divisions do not have a position. Set a position by clicking the division, then click and drag on the ground where you want them.";
	public static CharSequence ¤¤MusterProblem = "¤The division do not have a position. Set a position by clicking the division, then click and drag on the ground where you want them.";
	public static CharSequence ¤¤notMustered = "¤Some of your available units are not mustered.";
	private static CharSequence ¤¤exp = "In order to deploy and use your army, press the muster button to the left. Click and drag to select an area of troops, or click the unit cards. You can use control to toggle unit card selection and shift to select several.";
	public static CharSequence ¤¤restart = "restart";
	private static CharSequence ¤¤restartD = "restart";
	
	private static CharSequence ¤¤retreat = "Retreat";
	private static CharSequence ¤¤retreatD = "Retreat and lose {0} soldiers.";
	private static CharSequence ¤¤retreatQ = "Are you sure you wish to retreat and lose {0} soldiers?";
	
	private static CharSequence ¤¤throne = "When enemies are standing by the throne, this timer will tick down, and once it reaches 0, the battle will be lost.";
	
	static {
		D.ts(BattlePanel.class);
	}
	
	public BattlePanel(ISidePanels p, GameWindow w, UIPanelTop top, DivSelection selection, boolean battleview){
		
		CLICKABLE b;

		GuiSection butts = new GuiSection();
		
		b = new UIPanelTop.Butt(SPRITES.icons().m.sword, 8) {
			@Override
			protected void clickA() {
				if (p.added(cards_player))
					p.remove(cards_player);
				else
					p.add(cards_player, false, true);
				
			};
			@Override
			protected void renAction() {
				selectedSet(p.added(cards_player));
			}
		};
		b.hoverInfoSet(Dic.¤¤Army);
		butts.addRight(0, b);
		
		b = new UIPanelTop.Butt(SETT.ROOMS().ARTILLERY.get(0).iconBig(), 8) {
			@Override
			protected void clickA() {
				if (p.added(cards_cata))
					p.remove(cards_cata);
				else
					p.add(cards_cata, false, true);
				
			};
			@Override
			protected void renAction() {
				selectedSet(p.added(cards_cata));
			}
		};
		b.hoverInfoSet(Dic.¤¤Artillery);
		butts.addRight(0, b);
		
		
		
		if (S.get().developer){
			
			b = new UIPanelTop.Butt(SPRITES.icons().m.sword, 8) {
				@Override
				protected void clickA() {
					if (p.added(cards_enemy))
						p.remove(cards_enemy);
					else
						p.add(cards_enemy, true, true);
					
				};
				@Override
				protected void renAction() {
					selectedSet(p.added(cards_enemy));
				}
			};
			b.hoverInfoSet("armyEnemy");
			butts.addRight(0, b);
		}
		
		if (!battleview) {
			b = new UIPanelTop.Butt(SPRITES.icons().m.for_muster) {
				
				private DivFormationImp tmp = new DivFormationImp();
				boolean shouldmuster;
				boolean problem;
				
				@Override
				protected void clickA() {
					for (Div d : GAME.ARMIES().player().divisions())
						d.settings().musteringSet(shouldmuster);
					for (ROOM_ARTILLERY c : SETT.ROOMS().ARTILLERY) {
						for (int i = 0; i < c.instancesSize(); i++) {
							if (c.getInstance(i).army() == GAME.ARMIES().player())
								c.getInstance(i).muster(shouldmuster);
							
						}
					}
				};
				@Override
				protected void renAction() {
					shouldmuster = false;
					problem = false;
					for (Div d : GAME.ARMIES().player().divisions()) {
						
						if (d.menNrOf() > 0) {
							shouldmuster |= d.menNrOf() > 0 && !d.settings().mustering();
							if (!problem) {
								d.order().dest.get(tmp);
								if (tmp.deployed() == 0)
									problem = true;
							}
							
						}
					}
						
					for (ROOM_ARTILLERY c : SETT.ROOMS().ARTILLERY) {
						for (int i = 0; i < c.instancesSize(); i++) {
							shouldmuster |= c.getInstance(i).army() == GAME.ARMIES().player() && !c.getInstance(i).mustered();
						}
					}
					selectedSet(!shouldmuster); 
				}
				
				@Override
				protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected,
						boolean isHovered) {
					
					super.render(r, ds, isActive, isSelected, isHovered);
					if (problem) {
						GCOLOR.UI().BAD.hovered.bind();
						UI.icons().s.alert.render(r, body.x2()-16, body.y1());
					}else if (shouldmuster) {
						GCOLOR.UI().SOSO.hovered.bind();
						UI.icons().s.alert.render(r, body.x2()-16, body.y1());
					}
					COLOR.unbind();
				}
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					GBox b = (GBox)text;
					b.title(Dic.¤¤Muster);
					b.text(¤¤MusterDesc);
					b.sep();
					if (problem) {
						b.error(¤¤MusterOneProblem);
					}
					if (shouldmuster) {
						b.warn(¤¤notMustered);
					}
						
				}
				
				
			};
			butts.addRight(C.SG*4, b);
		}
		

		
		b = new GButt.BStat2(SPRITES.icons().s.standard, new GStat() {

			@Override
			public void update(GText text) {
				GFORMAT.perc(text, GAME.ARMIES().armies().get(0).morale());
			}
		}.decrease()) {
			@Override
			protected void clickA() {
				
			}
			
			@Override
			protected void renAction() {
				
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				GBox b = (GBox) text;
				b.title(Dic.¤¤Morale);
				b.text(Dic.¤¤MoraleD);
				b.NL(8);
				
//				for (DOUBLE_O<Army> o : MORALE.ARMY().factors) {
//					b.textL(o.info().name);
//					b.tab(6);
//					b.add(GFORMAT.f1(b.text(), o.getD(GAME.ARMIES().player())));
//					if (o instanceof INT_OE<?>) {
//						INT_OE<Army> oo = (INT_OE<Army>) o;
//						b.tab(8);
//						b.add(GFORMAT.i(b.text(), oo.get(GAME.ARMIES().player())));
//					}
//					b.NL();
//					b.text(o.info().desc);
//					b.NL(4);
//				}
			}
		};
		butts.addRightC(C.SG*4, b);
		
		if (S.get().developer) {
			b = new GButt.BStat2(SPRITES.icons().s.standard, new GStat() {

				@Override
				public void update(GText text) {
					GFORMAT.perc(text, GAME.ARMIES().armies().get(0).morale());
				}
			}.decrease()) {
				@Override
				protected void clickA() {
					
				}
				
				@Override
				protected void renAction() {
					
				}
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					GBox b = (GBox) text;
					b.title(Dic.¤¤Morale);
					b.text(Dic.¤¤MoraleD);
					b.NL(8);
					
//					for (DOUBLE_O<Army> o : MORALE.ARMY().factors) {
//						b.textL(o.info().name);
//						b.tab(6);
//						b.add(GFORMAT.f1(b.text(), o.getD(GAME.ARMIES().enemy())));
//						if (o instanceof INT_OE<?>) {
//							INT_OE<Army> oo = (INT_OE<Army>) o;
//							b.tab(8);
//							b.add(GFORMAT.i(b.text(), oo.get(GAME.ARMIES().enemy())));
//						}
//						b.NL();
//						b.text(o.info().desc);
//						b.NL(4);
//					}
				}
			};
			butts.addRightC(C.SG*4, b);
		}
		
		b = new GButt.BStat2(SPRITES.icons().s.human, new GStat() {

			@Override
			public void update(GText text) {
				GFORMAT.i(text, GAME.ARMIES().armies().get(1).men());
				text.errorify();
			}
		}) {
			
			int di = 0;
			@Override
			protected void clickA() {
				if (GAME.ARMIES().armies().get(1).men() > 0) {
					
					
					for (int i = 0; i < Config.battle().DIVISIONS_PER_ARMY; i++) {
						di++;
						
						di %= Config.battle().DIVISIONS_PER_ARMY;
						Div d = GAME.ARMIES().armies().get(1).divisions().get(di);
						if (d.menNrOf() > 0) {
							w.centerer.set(d.centre().cUnitX(), d.centre().cUnitY());
							return;
						}
					}
					
				}
			}
			
			@Override
			protected void renAction() {
				
			}
		}.hoverInfoSet(HTYPES.ENEMY().names);
		butts.addRightC(C.SG*4, b);

		if (!battleview) {
			b = new GButt.Panel(SPRITES.icons().m.questionmark).hoverInfoSet(¤¤exp);
			butts.addRelBody(8, DIR.E, b);
		}
		
		

		
		cards_cata = new UIPanelArtillery(GAME.ARMIES().player(), selection.artillery);
		cards_player = new UIPanelUnitCards(GAME.ARMIES().player(), selection);
		cards_enemy = new UIPanelUnitCards(GAME.ARMIES().enemy(), selection);
		
		
		

		top.addLeft(butts);
		
		p.add(cards_player, true, true);
		
		if (battleview){
			GuiSection r = new GuiSection();
			GButt bb = new UIPanelTop.Butt(SPRITES.icons().m.rotate, 8) {
				
				private ACTION a = new ACTION() {
					
					@Override
					public void exe() {
						VIEW.b().state().reloadBattle();
					}
				};
				
				@Override
				protected void clickA() {
					VIEW.inters().yesNo.activate(¤¤restartD, a, null, true);
				}
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					text.title(¤¤restart);
				}
				
			};
			r.addRightC(0, bb);
			bb = new UIPanelTop.Butt(SPRITES.icons().m.flag, 8) {
				
				private ACTION a = new ACTION() {
					
					@Override
					public void exe() {
						VIEW.b().state().liveRetreat();
					}
				};
				
				@Override
				protected void clickA() {
					Str.TMP.clear().add(¤¤retreatQ).insert(0, VIEW.b().state().liveRetreatLosses());
					VIEW.inters().yesNo.activate(Str.TMP, a, null, true);
				}
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					text.title(¤¤retreat);
					Text t = text.text();
					t.add(¤¤retreatD).insert(0, VIEW.b().state().liveRetreatLosses());
					text.add(t);

				}
				
			};
			r.addRightC(0, bb);
			
			r.addRightC(8, new GStat() {
				
				@Override
				public void update(GText text) {
					if (VIEW.b().state() == null)
						return;
					
					int tt = (int)VIEW.b().state().throneTimer();
					GFORMAT.iBig(text, (int)VIEW.b().state().throneTimer());
					
					if (tt < BattleState.throneMax/4) {
						text.color(GCOLOR.UI().badFlash());
					}else if (tt < BattleState.throneMax)
						text.color(GCOLOR.UI().SOSO.normal);
					else
						text.color(GCOLOR.UI().GOOD.normal);
					
				}
				
				@Override
				public void hoverInfoGet(GBox b) {
					// TODO Auto-generated method stub
					super.hoverInfoGet(b);
				}
			}.hh(SPRITES.icons().m.noble).hoverInfoSet(¤¤throne));
			
			if (S.get().developer) {
				
				r.addRightC(48, new GButt.ButtPanel("win") {
				
				
				
					@Override
					protected void clickA() {
						new EntityIterator.Humans() {
							
							@Override
							protected boolean processAndShouldBreakH(Humanoid h, int ie) {
								if (h.indu().clas() == HCLASSES.OTHER()) {
									h.helloMyNameIsInigoMontoyaYouKilledMyFatherPrepareToDie();
								}
								return false;
							}
						}.iterate();;
					}
				
				});
				
				BattleStateExiter exit = new BattleStateExiter() {
					
					@Override
					public void exit(BATTLE_RESULT res, int plosses, int elosses) {
						CORE.setCurrentState(new CORE_STATE.Constructor() {
							@Override
							public CORE_STATE getState() {
								return Menu.make();
							}
						});
					}

					@Override
					public void afterExit(BattleStateResult res) {

					}
				};
				
				r.addRightC(0, new GButt.ButtPanel(UI.icons().m.arrow_up.twin(UI.icons().s.star, DIR.NE, 1)) {
					
					
					
					@Override
					protected void clickA() {
						if (PATHS.local().save().exists(BattleState.debugLoad)) {
							new GameLoader(PATHS.local().save().get(BattleState.debugLoad)) {
								@Override
								public void doAfterSet() {
									BattleState.setLoaded(exit, PATHS.local().save().get(BattleState.debugLoad), true);
								};
							}.set();;
							
						}
					}
				});
				
				r.addRightC(0, new GButt.ButtPanel(UI.icons().m.arrow_up) {
					
					
					
					@Override
					protected void clickA() {
						new GameLoader(PATHS.local().save().get(BattleState.debugLoad)) {
							@Override
							public void doAfterSet() {
								BattleState.setLoaded(exit, PATHS.local().save().get(BattleState.debugLoad), false);
							};
						}.set();;
					}
				
				});
				
				r.addRightC(0, new GButt.ButtPanel(UI.icons().m.arrow_down) {
					
					
					
					@Override
					protected void clickA() {
						GAME.saver().save(BattleState.debugLoad);
					}
				
				});
				
			}
			
			top.addRight(r);
		}
		
		
		
		
		
	}

	
	
	
}
