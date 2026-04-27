package view.battle;

import java.util.Arrays;

import game.battle.div.Div;
import game.battle.formation.DIV_FORMATION;
import game.battle.formation.DivFormationImp;
import game.battle.thread.order.BattleOrderTask;
import game.battle.thread.trajectory.BattleTrajectories;
import game.time.TIME;
import init.constant.C;
import init.sprite.SPRITES;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import settlement.main.SETT;
import settlement.room.military.artillery.ArtilleryInstance;
import settlement.stats.STATS;
import settlement.stats.equip.EquipRange;
import snake2d.MButt;
import snake2d.Renderer;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.misc.ACTION;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.text.Str;
import snake2d.util.sprite.text.Text;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GGrid;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.panel.GPanel;
import util.info.GFORMAT;
import util.text.D;
import util.text.Dic;
import view.interrupter.InterManager;
import view.interrupter.Interrupter;
import view.keyboard.KEYS;
import view.keyboard.KeyButt;
import view.main.VIEW;

public final class UISelection extends Interrupter{
	
	private final DivSelection selection;
	private final GuiSection section = new GuiSection();
	private final DivFormationImp tmp = new DivFormationImp();
	private int iFormationTight;
	private int iFormationLoose;
	private int iRunning;
	private int iCharging;
	private int iMustered;
	private int trajectories;

	private int iMopping;
	private int iRangedHasAny;
	private boolean hasRanged = false;
	private int[] iRangedHas = new int[STATS.EQUIP().RANGED().size()];
	private int[] iOutOfAmmo = new int[STATS.EQUIP().RANGED().size()];
	private int[] iRangedSelected = new int[STATS.EQUIP().RANGED().size()];
	private int iInGuard;
	private int ifiresAtWill;
	private int ifiresWait;
	private final BattleOrderTask task = new BattleOrderTask();
	private int men;
	
	private static CharSequence ¤¤dPosition = "¤To position your troops, left-click on the ground and hold.";
	private static CharSequence ¤¤dAdd = "¤To add troops to your selection, hold {0}, then click and drag over the additional troops.";
	private static CharSequence ¤¤dMove = "¤To reposition your troops, use the arrow keys ({0}).";
	private static CharSequence ¤¤dSpin = "¤To rotate your selection, hold  {0}, click and hold where the center should be.";
	private static CharSequence ¤¤dSelectAll = "¤To select all divisions, press {0}.";
	private static CharSequence ¤¤dStopAll = "¤Stop all divisions and clear targets ({0}).";
	private static CharSequence ¤¤dFireAtWill = "¤Toggle fire at will. Allows soldiers and artillery to fire at enemies within reach.";
	private static CharSequence ¤¤dFireStandGround = "¤When enabled, ranged units will stay put, and only attack targets that are within range, without attempting to walk into range first.";

	private static CharSequence ¤¤Attack = "¤To attack an enemy division, left click on it. If ranged, to attack an enemy division melee, hold {0} and left click.";
	private static CharSequence ¤¤MopUp = "¤When soldiers are in position, they'll break it and go chasing enemy soldiers or rioteers. Morale will be very weak while doing this.";
	private static CharSequence ¤¤Targets = "¤Potential targets";
	static {
		D.ts(UISelection.class);
	}
	
	public UISelection(InterManager m, DivSelection s, boolean muster) {
		this.selection = s;
		
		section.add(new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.i(text, men);
			}
		}.decrease().hh(SPRITES.icons().s.sword));
		
		
		section.addRightC(64, new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.i(text, s.artillery.selection().size());
			}
		}.decrease().hh(SETT.ROOMS().ARTILLERY.get(0).icon.small));
		
		
		section.body().incrW(C.SG*60);
		
		section.addRelBody(C.SG*5, DIR.S, makeCommands(muster));
		
		GPanel f = new GPanel();
		f.inner().set(section);
		
		f.body().centerX(C.DIM());
		f.body().moveY1(C.SG*80);
		section.body().centerIn(f.inner());
		f.setButt();
		section.add(f);
		section.moveLastToBack();
		
		ACTION a = new ACTION() {
			
			@Override
			public void exe() {
				selection.clear();
			}
		};
		f.clickActionSet(a);
		
		pin();
		show(m);
	}

	@Override
	protected boolean hover(COORDINATE mCoo, boolean mouseHasMoved) {
		if ((selection.isClear() || men == 0) && selection.artillery.isClear())
			return false;
		return section.hover(mCoo);
	}

	@Override
	protected void mouseClick(MButt button) {
		if ((selection.isClear() || men == 0) && selection.artillery.isClear())
			return;
		if (button == MButt.LEFT)
			section.click();
	}

	@Override
	protected void hoverTimer(GBox text) {
		if ((selection.isClear() || men == 0) && selection.artillery.isClear())
			return;
		section.hoverInfoGet(text);
	}

	@Override
	protected boolean render(Renderer r, float ds) {
		if ((selection.isClear() || men == 0) && selection.artillery.isClear())
			return true;
		section.render(r, ds);
		return true;
	}

	@Override
	protected boolean update(float ds) {
		iFormationLoose = 0;
		iFormationTight = 0;
		iRunning = 0;
		iCharging = 0;
		iMustered = 0;

		iInGuard = 0;
		ifiresAtWill = 0;
		ifiresWait = 0;
		iMopping = 0;
		trajectories = 0;
		men = 0;
		hasRanged = false;
		iRangedHasAny = 0;
		Arrays.fill(iOutOfAmmo, 0);
		Arrays.fill(iRangedHas, 0);
		Arrays.fill(iRangedSelected, 0);
		
		for (Div d : selection.selection()) {
			d.order().task.get(task);
			if (d.settings().running)
				iRunning ++;
			if (d.settings().formation == DIV_FORMATION.LOOSE)
				iFormationLoose++;
			if (d.settings().formation == DIV_FORMATION.TIGHT)
				iFormationTight++;
//			if (!d.charging() && d.orders().canCharge())
//				iCanCharge++;
			if (task.task() == BattleOrderTask.DIVTASK.CHARGE)
				iCharging ++;
			if (d.settings().mustering())
				iMustered++;
			if (d.settings().moppingUp())
				iMopping++;
			if (d.settings().guard)
				iInGuard ++;
			for (EquipRange a : STATS.EQUIP().RANGED()) {
				if (a.stat().div().get(d) > 0) {
					
					if (d.settings().ammo() == a)
						iRangedSelected[a.tIndex] ++;
					
					if (a.ammoD(d) == 0) {
						iOutOfAmmo[a.tIndex] ++;
					}else {
						iRangedHas[a.tIndex] ++;
						iRangedHasAny ++;
						hasRanged = true;
						if (d.settings().fireAtWill) {
							ifiresAtWill ++;
						}
						
						if (d.settings().shouldNotMoveToFire)
							ifiresWait ++;
					}
				}
				
				
				
			}
			trajectories += BattleTrajectories.trajectories(d);
			
			men += d.menNrOf();
		}
		for (ArtilleryInstance c : selection.artillery.selection()) {
			if (c.mustered())
				iMustered ++;
			if (c.fireAtWill()) {
				ifiresAtWill ++;
			}
			iRangedHasAny ++;
				
		}
		
		if (KEYS.BATTLE().FORM_LOOSE.consumeClick())
			loose();
		
		if (KEYS.BATTLE().FORM_TIGHT.consumeClick())
			ltight();
		
		if (KEYS.BATTLE().GUARD.consumeClick())
			guard();
		
		if (KEYS.MAIN().ROTATE.consumeClick())
			run();
		
		if (KEYS.BATTLE().CHARGE.consumeClick())
			charge();
		
		return true;
	}
	
	private void loose() {
		for (Div d : selection.selection())
			d.settings().formation = DIV_FORMATION.LOOSE;
	}
	
	private void ltight() {
		for (Div d : selection.selection())
			d.settings().formation = DIV_FORMATION.TIGHT;
	}
	
	private void guard() {
		boolean charge = iInGuard < selection.selection().size();
		for (Div d : selection.selection()) {
			d.settings().guard = charge;
		}
	}
	
	protected void run() {
		if (iRunning == selection.selection().size()) {
			for (Div d : selection.selection())
				d.settings().running = false;
		}else {
			for (Div d : selection.selection())
				d.settings().running = true;
		}
		
		
	};
	
	protected void charge() {
		
		
		
		for (Div d : selection.selection()) {
			if (iCharging > 0)
				task.stop(d);
			else
				task.charge(d);
			d.order().task.set(task);
		}
	};
	
	private GuiSection makeCommands(boolean muster) {

		GuiSection sec = new GuiSection();
		sec.body().setWidth(6*40);
		GGrid g = new GGrid(sec, 6);
		

		g.add(KeyButt.wrap(new BB(SPRITES.icons().m.b_for_loose) {
			@Override
			protected void clickA() {
				loose();
			};
			
			@Override
			protected void renAction(){
				activeSet(selection.selection().size() > 0);
				selectedSet(activeIs() && iFormationLoose == selection.selection().size());
			}
			
			
		
		}, KEYS.BATTLE().FORM_LOOSE));
		g.add(KeyButt.wrap(new BB(SPRITES.icons().m.b_for_tight) {
			@Override
			protected void clickA() {
				ltight();
			};
			
			@Override
			protected void renAction(){
				activeSet(selection.selection().size() > 0);
				selectedSet(activeIs()&& iFormationTight == selection.selection().size());
			}
		}, KEYS.BATTLE().FORM_TIGHT));
		
		g.skip();
		
		g.add(KeyButt.wrap(new BB(SPRITES.icons().m.b_guard) {
			@Override
			protected void clickA() {
				guard();
			};
			
			@Override
			protected void renAction(){
				activeSet(selection.selection().size() > 0);
				selectedSet(activeIs() && iInGuard == selection.selection().size());
			}
		}, KEYS.BATTLE().GUARD));
		
		g.add(new BB(SPRITES.icons().m.b_run) {
			@Override
			protected void clickA() {
				run();
			};
			
			@Override
			protected void renAction(){
				activeSet(selection.selection().size() > 0);
				selectedSet(activeIs() && iRunning == selection.selection().size());
			}
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				Text t = text.text();
				t.insert(0, KEYS.MAIN().ROTATE.repr());
				text.add(t);
			};
		});
		
		g.add(new BB(SPRITES.icons().m.b_stop) {
			
			
			@Override
			protected void clickA() {
				for (Div d : selection.selection()) {
					task.stop(d);
					d.order().task.set(task);
				}
				for (ArtilleryInstance c : selection.artillery.selection()) {
					c.clearTarget();
				}
			};
			
			@Override
			protected void renAction(){
				activeSet(true);
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				Text t = text.text();
				t.add(¤¤dStopAll);
				t.insert(0, KEYS.MAIN().BACKSPACE.repr());
				text.add(t);
			};
			
		});

		
		g.NL();
		
		
		g.add(new BB(SPRITES.icons().m.b_chase) {
			@Override
			protected void clickA() {
				boolean shouldMuster = iMopping < selection.selection().size();
				for (Div d : selection.selection())
					if (d.menNrOf() > 0 && d.position().deployed() > 0)
						d.settings().moppingSet(shouldMuster);
			};
			
			@Override
			protected void renAction(){
				activeSet(false);
				if (selection.selection().size() > 0) {
					activeSet(true);
					boolean mustered = iMopping == selection.selection().size();
					selectedSet(mustered);
				}
				
			}
			
		
		}.hoverInfoSet(¤¤MopUp));
		
		
		
		g.add(KeyButt.wrap(new BB(SPRITES.icons().m.b_charge) {
			@Override
			protected void clickA() {
				charge();
			};
			
			@Override
			protected void renAction(){
				activeSet(selection.selection().size() > 0);
				selectedSet(activeIs() && iCharging == selection.selection().size());
			}
		}, KEYS.BATTLE().CHARGE));
		
		g.skip();
		
		g.add(new BB(SPRITES.icons().m.b_fire) {
			@Override
			protected void clickA() {
				
				
				boolean charge = ifiresAtWill == 0;
				for (Div d : selection.selection()) {
					d.settings().fireAtWill = charge;
				}
				for (ArtilleryInstance c : selection.artillery.selection()) {
					c.fireAtWill(charge);
				}
			};
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				GBox b = (GBox) text;
				text.text(¤¤dFireAtWill);
				b.NL(8);
				b.textL(¤¤Targets);
				b.add(GFORMAT.i(b.text(), trajectories));
			};
			
			@Override
			protected void renAction(){
				activeSet(hasRanged);
				selectedSet(ifiresAtWill > 0);
			}
		});
		
		g.add(new BB(SPRITES.icons().m.b_fire_stop) {
			@Override
			protected void clickA() {
				
				
				boolean charge = ifiresWait == 0;
				for (Div d : selection.selection()) {
					d.settings().shouldNotMoveToFire = charge;
				}
			};
			
			@Override
			protected void renAction(){
				activeSet(iRangedHasAny > 0);
				selectedSet(ifiresWait > 0);
			}
		}.hoverInfoSet(¤¤dFireStandGround));
		
		{
			
			GuiSection s = new GuiSection();
			for (EquipRange a : STATS.EQUIP().RANGED()) {
				
				
				s.addDownC(0, new BB(a.resource.icon()) {
					@Override
					protected void clickA() {
						for (Div d : selection.selection()) {
							if (a.stat().div().get(d) > 0)
								d.settings().ammoI = a.tIndex;
						}
						VIEW.inters().popup.close();
					};
					
					@Override
					protected void renAction(){
						activeSet(iRangedHas[a.tIndex] > 0);
						selectedSet(activeIs() && iRangedSelected[a.tIndex] > 0);
					
					}
					
					@Override
					protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
						super.render(r, ds, isActive, isSelected, isHovered);
						if (iOutOfAmmo[a.tIndex] > 0) {
							OPACITY.O0To25.bind();
							COLOR.BLACK.render(r, body);
							OPACITY.unbind();
							
						}
					};
					
					@Override
					public void hoverInfoGet(GUI_BOX text) {
						
						a.hover(text);
						int ammo = 0;
						
						for (Div d : selection.selection()) {
							
							ammo += a.ammoPerMan(d);
							
						}
						
						GBox b = (GBox) text;
						
						b.textLL(Dic.¤¤Ammunition);
						b.tab(6);
						b.add(GFORMAT.i(b.text(), ammo));
						
						if (iOutOfAmmo[a.tIndex] > 0) {
							Str.TMP.clear().add(Dic.¤¤ReloadingXX);
							Str.TMP.insert(0, a.ammoReplenishHours*TIME.secondsPerHour(), 4);
							text.text(Str.TMP);
						}
					};
					
					
				}.hoverInfoSet(a.resource.name));
				
			}
			
			SPRITE sp = new SPRITE.Imp(Icon.M) {
				
				@Override
				public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
					EquipRange bb = null;
					for (EquipRange a : STATS.EQUIP().RANGED()) {
						if (iRangedSelected[a.tIndex] > 0 && (bb == null || iRangedSelected[a.tIndex] > iRangedSelected[bb.tIndex]))
							bb = a;
					}
					if (bb != null) {
						UI.icons().s.chevron(DIR.S).renderCX(r, X1+(X2-X1)/2, Y2);
						bb.resource.icon().render(r, X1, X2, Y1, Y2);
					}
					
				}
			};
			
			g.add(new BB(sp) {
				@Override
				protected void clickA() {
					VIEW.inters().popup.show(s, this);
					super.clickA();
				}
				
				@Override
				protected void renAction() {
					EquipRange bb = null;
					for (EquipRange a : STATS.EQUIP().RANGED()) {
						if (iRangedSelected[a.tIndex] > 0 && (bb == null || iRangedSelected[a.tIndex] > iRangedSelected[bb.tIndex]))
							bb = a;
					}
					activeSet(bb != null);
				}
			});
			
			
		}

		{
			GuiSection s = new GuiSection();
			s.addRightC(0, new HOVERABLE.Sprite(SPRITES.icons().m.questionmark) {
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					text.text(¤¤dPosition);
					text.NL(4);
					Text t;
					
					t = text.text();
					t.add(¤¤dAdd);
					t.insert(0, KEYS.MAIN().UNDO.repr());
					text.add(t);
					text.NL(4);
					
					t = text.text();
					t.add(¤¤dMove);
					t.insert(0, KEYS.BATTLE().UP.repr());
					text.add(t);
					text.NL(4);
					
					t = text.text();
					t.add(¤¤dSpin);
					t.insert(0, KEYS.MAIN().MOD.repr());
					text.add(t);
					text.NL(4);
					
					t = text.text();
					t.add(¤¤Attack);
					t.insert(0, KEYS.MAIN().UNDO.repr());
					text.add(t);
					text.NL(4);
					
					t = text.text();
					t.add(¤¤dSelectAll);
					t.insert(0, KEYS.BATTLE().SELECT_ALL.repr());
					text.add(t);
					text.NL(4);
					
				};
				
			
			});
			
			
			
			if (muster) {
				s.addDownC(0, new BB(SPRITES.icons().m.b_muster) {
					@Override
					protected void clickA() {
						
						boolean shouldMuster = iMustered < selection.allSelected();
						for (Div d : selection.selection())
							if (shouldMuster && d.menNrOf() > 0 && d.position().deployed() > 0)
								d.settings().musteringSet(shouldMuster);
							else if (!shouldMuster)
								d.settings().musteringSet(false);
						for (ArtilleryInstance c : selection.artillery.selection()) {
							c.muster(shouldMuster);
						}
					};
					
					@Override
					protected void renAction(){
						boolean mustered = iMustered == selection.allSelected();
						selectedSet(mustered);
						if (!mustered) {
							activeSet(positions() == 0 || selection.artillery.selection().size() > 0);
						}else {
							activeSet(true);
						}
					}
					
					@Override
					public void hoverInfoGet(GUI_BOX text) {
						GBox b = (GBox)text;
						b.title(Dic.¤¤Muster);
						b.text(BattlePanel.¤¤MusterDesc);
						b.NL(8);
						
						int p = positions();
						if (p > 0) {
							if (selection.selection().size() == 1)
								b.error(BattlePanel.¤¤MusterProblem);
							else
								b.error(BattlePanel.¤¤MusterOneProblem);
								
						}
						
					}
					
					private int positions() {
						int i = 0;
						for (Div d : selection.selection()) {
							if (d.menNrOf() > 0) {
								d.order().dest.get(tmp);
								if (tmp.deployed() == 0)
									i++;
							}
						}
						return i;
					}
				
				});
			}
			g.section.addRelBody(8, DIR.W, s);
		}
		

		
		return g.section;
	}

	static class BB extends GButt.ButtPanel {

		public BB(SPRITE label) {
			super(label);
			setDim(40, 40);
		}
		
		
	}
	
}
