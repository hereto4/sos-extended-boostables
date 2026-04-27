package settlement.entity.humanoid.ai.battle;

import game.audio.AUDIO;
import game.audio.SoundRace;
import game.battle.div.Div;
import init.sprite.UI.UI;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AIModule;
import settlement.entity.humanoid.ai.main.AIPLAN;
import settlement.entity.humanoid.ai.main.AIPLAN.AiPlanActivation;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.stats.STATS;
import util.text.D;

public final class AIModule_Battle extends AIModule{
	
	private final AIPLAN march = new MarchPlan("BattleMarch");
	private final ManPlan planMan = new ManPlan("BattleMan");
	final PlanEscape escape = new PlanEscape();
	final MarchSubCutTo subCutTo = new MarchSubCutTo();
	final SubFight fight = new SubFight("battleFight");
	final MarchSoftCollision subSoft = new MarchSoftCollision();
	final PlanAttackTile tile = new PlanAttackTile("BAttleTile");
	final AIPLAN dessert = new PlanRout("BattleRout");
	
	public final SoundRace soundSword = AUDIO.race("SWORD");
	
	private static CharSequence ¤¤name = "Battle";
	private static CharSequence ¤¤desc = "Joining of mustered divisions and fighting.";
	static {
		D.ts(AIModule_Battle.class);
	}
	
	public AIModule_Battle() {
		super(UI.icons().s.sword, ¤¤name, ¤¤desc);
	}
	
	@Override
	public AiPlanActivation getPlan(Humanoid a, AIManager d) {
		if (STATS.BATTLE().ROUTING.indu().get(a.indu()) == 1) {
			return dessert.activate(a, d);
		}
	
		if (a.indu().hostile()) {
			AiPlanActivation p = march.activate(a, d);
			if (p == null)
				return planMan.activate(a, d);
			return p;
		}
		else {
			Div div = a.division();
			if (div != null) {
				
				AiPlanActivation p = march.activate(a, d);
				if (p != null)
					return p; 
			}
			return planMan.activate(a, d);
		}
	}

	public AISubActivation fight(Humanoid a, AIManager d, ENTITY h){
		d.otherEntitySet((Humanoid) h);
		return fight.activate(a, d);
	}
	
	public boolean breakTile(Humanoid a, AIManager d, int tx, int ty) {
		if (tile.shouldattackTile(d, a, tx, ty)) {
			tile.init(d, a, tx, ty);
			d.overwrite(a, tile);
			return true;
		}
		return false;
	}
	
	public AIPLAN interrrupt(Humanoid a, AIManager d){
		
		return march;
	}

	@Override
	protected void update(Humanoid a, AIManager ds, boolean newDay, int byteDelta, int updateI) {
		
		if (a.division() != null) {
			
			if (a.division().info.men() < a.division().men()) {
				STATS.BATTLE().DIV.set(a, null);
			}
			
		}
		
	}



	
	@Override
	public int getPriority(Humanoid a, AIManager ds) {
		if (STATS.BATTLE().ROUTING.indu().get(a.indu()) == 1)
			return 11;
		
		if (a.indu().hostile())
			return 11;
		
		Div d = a.division();
		if (d != null && d.settings().mustering() && d.deployed() > 0) {
			return 9;
		}
		
		if (planMan.shouldMan(a, ds)) {
			return 8;
		}
		
		return 0;
	}



}
