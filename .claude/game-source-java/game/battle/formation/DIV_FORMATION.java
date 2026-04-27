package game.battle.formation;



import game.battle.div.Div;
import game.battle.util.DIV_SPEC;
import settlement.stats.STATS;
import settlement.stats.equip.EquipBattle;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;

public enum DIV_FORMATION {

	TIGHT(1.2),
	LOOSE(1.75),
	
	;
	
	public final static LIST<DIV_FORMATION> all = new ArrayList<>(values());
	
	private final double size;
	
	private DIV_FORMATION(double size) {
		this.size = size;
	}
	
	public int size(DIV_SPEC div) {
		int am = 0;
		for (int i = 0; i < STATS.EQUIP().BATTLE_ALL().size(); i++) {
			EquipBattle b = STATS.EQUIP().BATTLE_ALL().get(i);
			if (div.equipI(b) > 0)
				am = Math.max(b.formationAdd, am);
		}
		
		return (int) ((div.race().physics.hitBoxsize()+am)*size);
	}
	
	public int sizeH(DIV_SPEC div) {
		return size(div)/2;
	}
	
	public int size(Div div) {
		int am = 0;
		for (int i = 0; i < STATS.EQUIP().BATTLE_ALL().size(); i++) {
			EquipBattle b = STATS.EQUIP().BATTLE_ALL().get(i);
			if (b.stat().div().get(div) > 0)
				am = Math.max(b.formationAdd, am);
		}
		return (int) ((div.race().physics.hitBoxsize()+am)*size);
	}

}
