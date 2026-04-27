package view.world.ui.army;

import game.battle.util.DIV_SPEC;
import game.faction.FACTIONS;
import game.faction.FResources.RTYPE;
import init.constant.Config;
import init.race.RACES;
import init.resources.RESOURCES;
import init.resources.ResSupply;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import settlement.main.SETT;
import settlement.stats.STATS;
import settlement.stats.colls.StatsBattle.StatTraining;
import settlement.stats.equip.EquipBattle;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayList;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.info.GFORMAT;
import util.text.D;
import util.text.Dic;
import view.ui.div.UIDivCardWorld;
import view.ui.div.UIDivEditor;
import world.army.AD;
import world.army.WDivRegional;

class ArmyRecruit extends GuiSection{

	static CharSequence ¤¤Full = "¤Army unit limit reached!";
	static CharSequence ¤¤NoMapnpower = "¤Insufficient Conscripts available of selected race!";
	static CharSequence ¤¤Note = "¤NOTE: once a division has finished training, it will need to be supplied from your army supply depot. Without supplies, the men will desert you.";
	static CharSequence ¤¤Time = "¤The amount of days it will take to train this division to specification.";
	static CharSequence ¤¤Supplies = "¤You don't have enough essential supplies stored in your warehouses to recruit this unit.";
	static CharSequence ¤¤SuppliesW = "¤You do not have any army supply depots to deliver essential supplies to this division.";
	static{
		D.ts(ArmyRecruit.class);
	}
	
	private final UIDivEditor editor = new UIDivEditor(0.75, false, false, false, RACES.playable());
	
	ArmyRecruit() {
		
		
		add(new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.i(text, AD.conscripts().available(editor.div().race()).get(FACTIONS.player()));
			}
		}.hh(Dic.¤¤Conscripts));
		
		addRelBody(4, DIR.S, editor);
		
		{
			GuiSection row = new GuiSection();
			
			
			
			GStat ss = new GStat() {
				
				@Override
				public void update(GText text) {
					
					GFORMAT.i(text, (long) ti());
				}
				
				@Override
				public void hoverInfoGet(GBox b) {
					b.text(¤¤Time);
				}
				
				double ti() {
					
					int am = WDivRegional.DAYS_TO_TRAIN;
					
					for (StatTraining t : STATS.BATTLE().TRAINING_ALL) {
						am += WDivRegional.trainingDays(t, editor.div().training(t), FACTIONS.player());
					}
					
					return am;
					
				}
				
			};
			
			row.addRightC(48, ss.hh(SPRITES.icons().m.time));
			
			GButt b = new GButt.ButtPanel(Dic.¤¤Recruit) {
				
				@Override
				protected void clickA() {
					if (Army.army.divs().canAdd()) {
						WDivRegional d = AD.regional().create(editor.div().race(), (double)editor.div().men()/Config.battle().MEN_PER_DIVISION, Army.army);
						d.bannerSet(editor.div().bannerI());
						
						for (ResSupply s : RESOURCES.SUP().ALL) {
							if (s.health <= 0)
								continue;
							int am = s.amount(editor.div().race(), editor.div().men());
							am = CLAMP.i(am, 0, SETT.ROOMS().STOCKPILE.tally().amountReservable.get(s.resource));
							if (am > 0) {
								s.resource.remove(am, RTYPE.ARMY_SUPPLY);
								AD.supplies().get(s).current().inc(Army.army, am);
							}
						}
						
						for (StatTraining s: STATS.BATTLE().TRAINING_ALL) {
							d.target.trainingSet(s, editor.div().training(s));
						}
						
						for (EquipBattle s : STATS.EQUIP().BATTLE_ALL()) {
							d.target.equipSet(s, (editor.div().equip(s)));
						}
						
						
						
					}
				}
				
				@Override
				protected void renAction() {
					activeSet(problem() == null);
				}
				
				private final ArrayList<DIV_SPEC> li = new ArrayList<DIV_SPEC>(1);
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					li.clearSloppy();
					li.add(editor.div());
					UIDivCardWorld.hoverSendOut(li, text);
				}
				
			};
			
			row.addRightC(48, b);
			
			addRelBody(8, DIR.S, row);
			
		}
		
		add(new GStat() {
			
			@Override
			public void update(GText text) {
				text.setMaxWidth(body().width());
				text.setMultipleLines(true);
				CharSequence p = problem();
				if (p != null)
					text.errorify().add(p);
				else {
					p = warning();
					if (p != null)
						text.warnify().add(p);
				}
			}
		}, body().x1(), body().y2()+8);
		
		body().incrH(UI.FONT().S.height()*4);
		
	}
	
	private CharSequence problem() {
		
		if (!Army.army.divs().canAdd()) {
			return ¤¤Full;
		}
		
		if (editor.div().men() > AD.conscripts().available(editor.div().race()).get(FACTIONS.player())) {
			return ¤¤NoMapnpower;
		}
		
		if (UIDivCardWorld.supplyError(editor.div()) != null)
			return UIDivCardWorld.supplyError(editor.div());
		
		return null;
	}
	
	private CharSequence warning() {
		if (editor.div().men() > AD.conscripts().available(editor.div().race()).get(FACTIONS.player())) {
			return ¤¤NoMapnpower;
		}
		
		return null;
	}
}
