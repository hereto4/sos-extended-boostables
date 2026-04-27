package view.sett.ui.army;

import game.GAME;
import game.battle.Armies;
import game.battle.div.Div;
import init.constant.Config;
import snake2d.util.datatypes.DIR;
import snake2d.util.sets.ArrayList;
import util.text.Dic;
import view.interrupter.ISidePanel;
import view.sett.IDebugPanelSett;

public final class UIArmy extends ISidePanel{

	public UIArmy(Armies m){
		titleSet(Dic.¤¤Conscripts);
		
		ArrayList<Div> selection = new ArrayList<>(Config.battle().DIVISIONS_PER_ARMY);
		
		this.section.add(new Info());
		this.section.addRelBody(8, DIR.S, new Actions(selection));
		this.section.addRelBody(8, DIR.S, new DivList(HEIGHT-this.section.body().height()-16, selection));
		
		IDebugPanelSett.add(new FormationDebugPlacer(GAME.ARMIES().player()));
		IDebugPanelSett.add(new FormationDebugPlacer(GAME.ARMIES().enemy()));
		
		
	}

}
