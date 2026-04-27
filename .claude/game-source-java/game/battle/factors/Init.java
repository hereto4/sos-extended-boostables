package game.battle.factors;

import game.battle.div.Div;
import game.boosting.BOOSTABLES;
import init.settings.S;
import init.sprite.UI.UI;
import settlement.stats.STATS;
import snake2d.util.misc.CLAMP;
import util.gui.misc.GBox;
import util.info.GFORMAT;
import util.text.D;
import util.text.Dic;

class Init {

	
	private static CharSequence ¤¤exhaustion = "Exhaustion";
	private static CharSequence ¤¤exhaustionDesc = "Running and fighting will exhaust a division, making them less effective in combat.";
	private static CharSequence ¤¤exhMess = "Exhaustion";
	
	private static CharSequence ¤¤formName = "Formation";
	private static CharSequence ¤¤formDesc = "A coherent formation of enough depth makes soldiers feel safe and helps them defend each other against attacks.";
	private static CharSequence ¤¤formMess = "Formation is Intact";
	private static CharSequence ¤¤inPos = "In Position";
	private static CharSequence ¤¤depth = "Formation depth";
	
	private static CharSequence ¤¤armyName = "Numbers";
	private static CharSequence ¤¤armyDesc = "The size of our army against the size of the enemy's army.";
	private static CharSequence ¤¤armyMess = "Numbers";
	private static CharSequence ¤¤armyPlayer = "Army Size";
	private static CharSequence ¤¤armyEnemy = "Enemy Army Size";
	
	private static CharSequence ¤¤suppliesName = "Supplies";
	private static CharSequence ¤¤suppliesDesc = "The army's supplies prior to engagement.";
	private static CharSequence ¤¤suppliesMess = "Supplied";
	
	private static CharSequence ¤¤casultiesName = "Casualties";
	private static CharSequence ¤¤casultiesDesc = "The amount of casualties sustained in this battle.";
	private static CharSequence ¤¤casultiesMess = "Taking Casulties";
	private static CharSequence ¤¤inUnit = "In Unit";
	private static CharSequence ¤¤inArmy = "In Army";
	
	private static CharSequence ¤¤routName = "Routing";
	private static CharSequence ¤¤routDesc = "The amount of soldiers that have routed.";
	private static CharSequence ¤¤routMess = "Routing";
	
	private static CharSequence ¤¤projectilesName = "Under Fire";
	private static CharSequence ¤¤projectilesDesc = "Nothing can be as demoralizing as being bombarded by projectiles. The effect lasts for some time.";
	private static CharSequence ¤¤projectilesMess = "Under Fire";
	
	private static CharSequence ¤¤situationName = "Situation";
	private static CharSequence ¤¤situationDesc = "The proximity, and amount of enemy troops and their quality.";
	private static CharSequence ¤¤situationMess = "Feeling Outmatched";
	private static CharSequence ¤¤enemiesNear = "Enemy Power Nearby";
	private static CharSequence ¤¤friendsNear = "Ally Power Nearby";
	
	private static CharSequence ¤¤wearinessName = "Battle Weariness";
	private static CharSequence ¤¤wearinessDesc = "While soldiers are often eager to do battle initially, extended periods of combat can make them think their warm beds back home.";
	private static CharSequence ¤¤wearinessMess = "Battle Weariness";
	private static CharSequence ¤¤weariness = "Weariness";
	private static CharSequence ¤¤wearinessDelta = "Weariness Increase";
	
	private static CharSequence ¤¤flanksName = "Flanks Compromized";
	private static CharSequence ¤¤flanksDesc = "There's nothing to ruin a soldier's day more than being attacked in the flank.";
	private static CharSequence ¤¤flanksMess = "Flanks Compromized";
	private static CharSequence ¤¤flanks = "Flanked Soldiers";
	private static CharSequence ¤¤flanksValue = "Flanked Value";
	
	private static CharSequence ¤¤surroundedName = "Surrounded";
	private static CharSequence ¤¤surroundedDesc = "Apart from flanks, being surrounded by an enemy force is the greatest deterrence to stand and fight.";
	private static CharSequence ¤¤surroundedMess = "Feeling Surrounded";
	private static CharSequence ¤¤surroundedThreat = "Surrounding Threats";
	
	static {
		D.ts(Init.class);
	}
	
	public Init(DivFactors ff){

		new DivFactor(¤¤formName, ¤¤formDesc, UI.icons().s.shield, ¤¤formMess, 0) {

			@Override
			public double getD(Div div) {
				if (!div.position().isCoherent())
					return 0;
				double men = div.men();
				if (men == 0)
					return 0;
				double d = (double)div.centre().inFormation()/div.men();
				d -= 0.35;
				d /= (1.0-0.35);
				double w = men/(div.position().width()/(div.position().formation().size(div)));
				w = CLAMP.d(w/5.0, 0, 1);
				return d*w;
			}
			
			@Override
			public void phover(Div div, GBox b) {
				
				b.textLL(¤¤inPos);
				b.tab(6);
				b.add(GFORMAT.iofkInv(b.text(), div.centre().inFormation(), div.men()));
				b.NL();
				b.textLL(¤¤depth);
				b.tab(6);
				int d = (div.position().width()/div.position().formation().size(div));
				if (d == 0)
					d = 1;
				b.add(GFORMAT.iofkInv(b.text(), div.men()/d, 5));
				b.NL();				
			};
		}.
		boost(BOOSTABLES.BATTLE().MORALE, 1, 1.25, true).
		boost(BOOSTABLES.BATTLE().FORMATION, 0, 1, true);
		
		new DivFactor(¤¤exhaustion, ¤¤exhaustionDesc, UI.icons().s.shield, ¤¤exhMess, 1) {

			@Override
			public double getD(Div div) {
				return 1.0-STATS.NEEDS().EXHASTION.div().getD(div);
			}
			
			@Override
			public void phover(Div div, GBox b) {
				
			};
			
			@Override
			protected double induValue(settlement.stats.Induvidual indu) {
				return 1.0-STATS.NEEDS().EXHASTION.indu().getD(indu);
			};
			
		}.
		boost(BOOSTABLES.PHYSICS().SPEED, 0.25,1, true).
		boost(BOOSTABLES.BATTLE().OFFENCE, 0.5,1, true).
		boost(BOOSTABLES.BATTLE().DEFENCE, 0.5,1, true);

		new DivFactor(¤¤armyName, ¤¤armyDesc, UI.icons().s.typeSoldier, ¤¤armyMess, 0.5) {

			@Override
			public double getD(Div div) {
				
				double m = div.army().men();
				double e = div.army().enemy().men();
				
				if (e == 0)
					return 0.5;
				return m/e-0.5;
			}
			
			@Override
			public void phover(Div div, GBox b) {
				
				b.textLL(¤¤armyPlayer);
				b.tab(6);
				b.add(GFORMAT.i(b.text(), div.army().men()));
				b.NL();
				b.textLL(¤¤armyEnemy);
				b.tab(6);
				b.add(GFORMAT.i(b.text(), div.army().enemy().men()));
				b.NL();				
			};
		}.
		boost(BOOSTABLES.BATTLE().MORALE, 0.75, 1.25, true);
		
		new DivFactor(¤¤suppliesName, ¤¤suppliesDesc, UI.icons().s.storage, ¤¤suppliesMess, 0.5) {

			@Override
			public double getD(Div div) {
				return ff.supplies.getD(div.army())+0.5;
			}
			
			@Override
			public void phover(Div div, GBox b) {
				if (S.get().developer) {
					b.add(b.text().add(ff.supplies.getD(div.army())));
					b.NL();
					b.add(b.text().add(getD(div)));
				}
			};
		}.
		boost(BOOSTABLES.BATTLE().MORALE, 0.25, 1.75, true);
		
		new DivFactor(¤¤casultiesName, ¤¤casultiesDesc, UI.icons().s.death, ¤¤casultiesMess, 1) {

			@Override
			public double getD(Div div) {
				
				if (div.army().men() == 0)
					return 1;
				if (div.men() == 0)
					return 1;
				
				double a = ff.casulties.army.getD(div.army());
				a = a / (a+div.army().men());
				
				double d = ff.casulties.getD(div);
				d = 2.0*d / (d + div.men());
				
				return CLAMP.d(1-0.5*a-d, 0, 1);
			}
			
			@Override
			public void phover(Div div, GBox b) {
				
				b.textLL(¤¤inUnit);
				b.tab(6);
				b.add(GFORMAT.i(b.text(), (int)ff.casulties.getD(div)));
				b.NL();
				b.textLL(¤¤inArmy);
				b.tab(6);
				b.add(GFORMAT.i(b.text(), (int) ff.casulties.army.getD(div.army())));
				b.NL();				
			};
		}.
		boost(BOOSTABLES.BATTLE().MORALE, 0, 1, true);
		
		new DivFactor(¤¤routName, ¤¤routDesc, UI.icons().s.flag, ¤¤routMess, 1) {

			@Override
			public double getD(Div div) {
				if (div.army().men() == 0)
					return 1;
				if (div.men() == 0)
					return 1;
				
				double a = ff.routing.army.getD(div.army());
				a = a / (a+div.army().men());
				
				double d = ff.routing.getD(div);
				d = 2.0*d / (d + div.men());
				
				return CLAMP.d(1-a*d, 0, 1);
			}
			
			@Override
			public void phover(Div div, GBox b) {
				
				b.textLL(¤¤inUnit);
				b.tab(6);
				b.add(GFORMAT.i(b.text(), (int)ff. routing.getD(div)));
				b.NL();
				b.textLL(¤¤inArmy);
				b.tab(6);
				b.add(GFORMAT.i(b.text(), (int) ff.routing.army.getD(div.army())));
				b.NL();				
			};
		}.
		boost(BOOSTABLES.BATTLE().MORALE, 0, 1, true);
		
		new DivFactor(¤¤projectilesName, ¤¤projectilesDesc, UI.icons().s.arrow_left, ¤¤projectilesMess, 1) {

			@Override
			public double getD(Div div) {
				if (div.men() == 0)
					return 1;
				return 1.0-CLAMP.d(ff.projectiles.getD(div)/(div.men()*4), 0, 1);
			}
			
			@Override
			public void phover(Div div, GBox b) {
				
				b.textLL(Dic.¤¤Projectiles);
				b.tab(6);
				b.add(GFORMAT.percInv(b.text(), 1.0-getD(div)));
			};
		}.
		boost(BOOSTABLES.BATTLE().MORALE, 0.5, 1, true).
		boost(BOOSTABLES.BATTLE().DEFENCE, 0.75, 1, true).
		boost(BOOSTABLES.BATTLE().OFFENCE, 0.75, 1, true);
		
		new DivFactor(¤¤situationName, ¤¤situationDesc, UI.icons().s.eye, ¤¤situationMess, 1) {

			@Override
			public double getD(Div div) {
				double ee = div.status().ajacentEnemiesPower();
				if (ee == 0)
					return 1;
				
				double f = div.status().ajacentFriendsPower();
				
				
				double d = (ee-f)/((ee + f)*2.0);
				
				return 1.0-CLAMP.d(d, 0, 1);
			}
			
			@Override
			public void phover(Div div, GBox b) {
				
				b.textLL(¤¤enemiesNear);
				b.tab(6);
				b.add(GFORMAT.f0(b.text(), -div.status().ajacentEnemiesPower()));
				b.NL();
				b.textLL(¤¤friendsNear);
				b.tab(6);
				b.add(GFORMAT.f0(b.text(), div.status().ajacentFriendsPower()));
			};
		}.
		boost(BOOSTABLES.BATTLE().MORALE, 0, 1, true);
		
		new DivFactor(¤¤wearinessName, ¤¤wearinessDesc, UI.icons().s.happy, ¤¤wearinessMess, 1) {

			@Override
			public double getD(Div div) {
				return 1.0 - ff.weariness.getD(div)/10000;
			}
			
			@Override
			public void phover(Div div, GBox b) {
				
				b.textLL(¤¤weariness);
				b.tab(6);
				b.add(GFORMAT.f0(b.text(), -ff.weariness.getD(div)));
				b.NL();
				b.textLL(¤¤wearinessDelta);
				b.tab(6);
				b.add(GFORMAT.f0(b.text(), -(double)div.status().engagements()/(1+div.men())));
			};
		}.
		boost(BOOSTABLES.BATTLE().MORALE, -10000, 0, false);
		
		new DivFactor(¤¤flanksName, ¤¤flanksDesc, UI.icons().s.expand, ¤¤flanksMess, 1) {

			@Override
			public double getD(Div div) {
				return 1.0 - v(div);
			}
			
			private double v(Div div) {

				double f = div.status().flanks();
				f/= div.men();
				f-= 0.3;
				f*=4;
				f = CLAMP.d(f, 0, 1);
				return f;
			}
			
			@Override
			public void phover(Div div, GBox b) {
				
				b.textLL(¤¤flanks);
				b.tab(6);
				b.add(GFORMAT.i(b.text(), (long) div.status().flanks()));
				b.NL();
				b.textLL(¤¤flanksValue);
				b.tab(6);
				b.add(GFORMAT.percInv(b.text(), v(div)));
				b.NL();
				
			};
		}.
		boost(BOOSTABLES.BATTLE().DEFENCE, 0.2, 1, true).
		boost(BOOSTABLES.BATTLE().MORALE, -1, 0, false);
		
		new DivFactor(¤¤surroundedName, ¤¤surroundedDesc, UI.icons().s.circle, ¤¤surroundedMess, 1) {

			@Override
			public double getD(Div div) {
				double d = div.status().encirclementPower();
				d /= (div.status().ajacentFriendsPower()+1)*10.0;
				
				
				return 1.0 - d;
			}
			
			@Override
			public void phover(Div div, GBox b) {
				b.textLL(¤¤surroundedThreat);
				b.tab(6);
				b.add(GFORMAT.f0(b.text(), -div.status().encirclementPower()));
				b.NL();
				b.textLL(¤¤friendsNear);
				b.tab(6);
				b.add(GFORMAT.f0(b.text(), div.status().ajacentFriendsPower()));
				
			};
		}.
		boost(BOOSTABLES.BATTLE().MORALE, -15, 0, false);
		
	}
	

	
}
