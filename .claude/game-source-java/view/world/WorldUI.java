package view.world;

import view.interrupter.ISidePanels;
import view.interrupter.InterManager;
import view.tool.ToolManager;
import view.world.ui.army.UIArmies;
import view.world.ui.battle.UIWBattlePrompt;
import view.world.ui.camps.UICampList;
import view.world.ui.faction.UIFactions;
import view.world.ui.region.UIRegions;

public class WorldUI {

	public final UIRegions regions;
	public final UIArmies armies = new UIArmies();
	public final UICampList camps = new UICampList();
	public final UIWBattlePrompt battle = new UIWBattlePrompt();
	public final UIFactions factions = new UIFactions();
	
	WorldUI(InterManager m, ISidePanels panels, ToolManager tools){
		regions = new UIRegions(panels, tools);
	}
}
