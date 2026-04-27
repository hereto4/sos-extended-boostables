package view.battle.editor;

import game.GAME;
import game.battle.util.DIV_SPEC.DIV_SPECImp;
import game.faction.FACTIONS;
import game.faction.npc.FactionNPC;
import init.constant.Config;
import init.race.RACES;
import init.sprite.UI.UI;
import settlement.stats.STATS;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.rnd.RND;
import util.data.GETTER.GETTER_IMP;
import util.gui.misc.GButt;
import util.text.D;
import util.text.Dic;
import view.ui.div.UIDivEditor;
import world.region.RD;

class Army extends GuiSection{

	private final GETTER_IMP<ArmySide> current;
	static CharSequence ¤¤name = "armies";
	private static CharSequence ¤¤generate = "Generate";
	private static CharSequence ¤¤add = "Add Unit";
	
	static {
		D.ts(Army.class);
	}
	
	Army(ArmySide player, ArmySide enemy){

		current = new GETTER_IMP<ArmySide>(player);
		
		FACTIONS.player().setRace(RACES.playable().get(0));
		((FactionNPC)FACTIONS.all().get(1)).generate(RD.RACES().all.rnd(), false);
		
		{
			GuiSection s = new GuiSection();
			s.add(UI.icons().l.battle, 0, 0);
			s.addDownC(6, new GButt.ButtPanel(UI.icons().m.rotate) {
				
				@Override
				protected void clickA() {
					
					double pow = 1000 + Config.battle().MEN_PER_ARMY*RND.rFloat()*RND.rFloat()*RND.rFloat()*GAME.battle().power.HIGH_POWER;
					
					player.generate(pow);
					enemy.generate(pow);
					
					super.clickA();
				}
				
			}.hoverInfoSet(¤¤generate));
			
			s.addRelBody(8, DIR.W, new ArmyFactionButt(FACTIONS.player(), player, current));
			s.addRelBody(8, DIR.E, new ArmyFactionButt(FACTIONS.all().get(1), enemy, current));
			
			addRelBody(8, DIR.S, s);
			addRelBody(8, DIR.S, UI.decor().borderTop(800));
		}
		
		
		{
			UIDivEditor editor = new UIDivEditor(STATS.BATTLE().TRAINING_ALL.size(), true, true, false, RACES.all());
			editor.div().raceSet(RACES.playable().get(0));
			
			GuiSection s = new GuiSection();
			s.add(editor);
			
			{
				GuiSection butts = new GuiSection();
				butts.addRightC(0, new GButt.ButtPanel(¤¤add) {
					
					@Override
					protected void clickA() {
						current.get().divs.add(new DIV_SPECImp().copyFrom(editor.div()));
					}
					
					@Override
					protected void renAction() {
						activeSet(current.get().divs.hasRoom());
					}
					
				}.pad(32, 0).repetativeSet(true));
				butts.addRightC(32, new GButt.ButtPanel(UI.icons().m.rotate) {
					
					@Override
					protected void clickA() {
						editor.div().generate();
					}
					
				}.hoverInfoSet(Dic.¤¤Generate));
				
				s.addRelBody(8, DIR.S, butts);
			}
			
			
			
		
			s.addRelBody(8, DIR.W, new ArmyDivs(current, editor));
			addRelBody(8, DIR.S, s);
		}
		
		addRelBody(8, DIR.S, UI.decor().borderBottom(800));
		
		
		
	}
	
}
