package game.battle.state;

import game.battle.util.DivGeneration;
import init.constant.Config;
import snake2d.util.datatypes.Coo;
import snake2d.util.sets.ArrayList;
import world.army.AD;

public class BattleStateSpec{
	
	public SpecSide player = new SpecSide();
	public SpecSide enemy = new SpecSide();
	
	public static class SpecSide {
		public final Coo wCoo = new Coo();
		public final int[] artillery = new int[AD.supplies().arts().size()];
		public double moraleBase;
		public ArrayList<DivGeneration> divs = new ArrayList<DivGeneration>(Config.battle().DIVISIONS_PER_ARMY);
	}
}