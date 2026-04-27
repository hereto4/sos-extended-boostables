package world.battle;

import java.util.Arrays;

import game.GAME;
import game.battle.state.BattleState;
import game.battle.state.BattleStateExiter;
import game.battle.state.BattleStateResult;
import game.battle.state.BattleStateSpec;
import game.battle.util.DivGeneration;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.diplomacy.DIP;
import game.faction.npc.FactionNPC;
import game.faction.royalty.opinion.ROPINIONS;
import init.constant.Config;
import settlement.battle.invasion.InvasionSpec;
import settlement.main.SETT;
import settlement.stats.Induvidual;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.sets.ArrayListGrower;
import view.main.VIEW;
import world.army.AD;
import world.army.ADSupplies.ADArtillery;
import world.army.ADSupply;
import world.army.WDIV;
import world.battle.ResolverSide.RCount;
import world.battle.Side.SideUnit;
import world.battle.spec.BATTLE_RESULT;
import world.battle.spec.WBattleResult;
import world.battle.spec.WBattleSiege;
import world.battle.spec.WBattleSpec;
import world.map.regions.Region;
import world.region.RD;

class ResolverPlayer {
	
	private final RCount tmp = new RCount();
	private final RCount cEnemy = new RCount();
	private final RCount cPlayer = new RCount();
	private final Spec spec = new Spec();
	private final Conquer conq = new Conquer();
	
	ResolverPlayer(){

	}
	
	public void enemyWithdraws(ResolverSide pp, ResolverSide looser) {
		tmp.clear();
		
		
		double losses = Resolver.retreatValue(looser);
		looser.count(tmp, losses, false);
		Res rr = new Res(pp, looser, BATTLE_RESULT.VICTORY, tmp, 1.0) {
			
			@Override
			public void accept(int[] enslave, int[] resources) {
				BattleListener.notify(pp, looser);
				shipRetreat(enslave, resources);
				looser.extract(losses);
				
			}
		};
		VIEW.world().UI.battle.result(rr, true);
		
		
	}
	
	public void battle(ResolverSide pp, ResolverSide ee) {
		
		spec.init(pp, ee);	
		if (pp.us.get(0).unit.faction() == FACTIONS.player()) {
			VIEW.world().UI.battle.battle(spec);
		}else
			VIEW.world().UI.battle.assist(spec);
			//send
		
	}
	
	
	

	
	
	public void sallyOut(ResolverSide pp, ResolverSide ee) {
		
		pp.count(cPlayer.clear(), 0, true);
		
		WBattleSpec spec = new WBattleSpec() {
			
			{
				victory = pp.powerBalance > ee.powerBalance;
			}
			
			
			
			@Override
			public void retreat() {
				
			}
			
			@Override
			public void engage() {
				new Manual(pp.side, ee.side);
			}
			
			@Override
			public void auto() {
				tmp.clear();
				
				pp.count(cPlayer.clear(), Resolver.autoValue(pp), false);
				ee.count(cEnemy.clear(), Resolver.autoValue(ee), false);
				
				BATTLE_RESULT res = pp.powerBalance > ee.powerBalance ? BATTLE_RESULT.VICTORY : BATTLE_RESULT.DEFEAT;
				
				if (res == BATTLE_RESULT.DEFEAT) {
					pp.us.get(0).count(cPlayer.clear(), 1.0, false);
					
				}
				
				Res rr = new Res(pp, ee, res, res == BATTLE_RESULT.VICTORY ? cEnemy : cPlayer) {
					
					@Override
					public void accept(int[] enslave, int[] resources) {
						BattleListener.notify(res == BATTLE_RESULT.VICTORY ? pp : ee, res == BATTLE_RESULT.VICTORY ? ee : pp);
						shipRetreat(enslave, resources);
						pp.extract(Resolver.autoValue(pp));
						ee.extract(Resolver.autoValue(ee));
					}
				};
				
				VIEW.world().UI.battle.result(rr, false);
					
			}
			
		};
		spec.player = pp;
		spec.enemy = ee;
		
		
		
		VIEW.world().UI.battle.battleSally(spec);
	}
	
	public void besige(ResolverSide pp, ResolverSide ee) {
		
		
		
		
		if (ee.men() == 0) {
			conq.init(ee.us.get(0).unit.r(), pp.side, ee.side);
			VIEW.world().UI.battle.result(conq);
			return;
		}
		
		WBattleSiege spec = new WBattleSiege() {
			
			@Override
			public void auto() {
				tmp.clear();
				pp.count(cPlayer.clear(), Resolver.autoValue(pp), false);
				ee.count(cEnemy.clear(), Resolver.autoValue(ee), false);
				BATTLE_RESULT res = pp.powerBalance > ee.powerBalance ? BATTLE_RESULT.VICTORY : BATTLE_RESULT.DEFEAT;
				
				Res rr = new Res(pp, ee, res, res == BATTLE_RESULT.VICTORY ? cEnemy : cPlayer) {
					
					@Override
					public void accept(int[] enslave, int[] resources) {
						BattleListener.notify(res == BATTLE_RESULT.VICTORY ? pp : ee, res == BATTLE_RESULT.VICTORY ? ee : pp);
						shipRetreat(enslave, resources);
						pp.extract(Resolver.autoValue(pp));
						ee.extract(Resolver.autoValue(ee));
						
						if (res == BATTLE_RESULT.VICTORY) {
							conq.init(ee.us.get(0).unit.r(), pp.side, ee.side);
							VIEW.world().UI.battle.result(conq);
						}
					}
				};
				
				VIEW.world().UI.battle.result(rr, false);
				
			}

			@Override
			public void retreat() {
				for (SideUnit u : pp.side.us)
					if (u.a() != null && u.a().faction() == FACTIONS.player())
						u.a().stop();
				
				
			}
		};
		spec.besiged = ee.us.get(0).unit.r();
		spec.fortifications = RD.MILITARY().fort.getD(spec.besiged);
		spec.victory = pp.powerBalance > ee.powerBalance;
		spec.player = pp;
		spec.enemy = ee;
		VIEW.world().UI.battle.siege(spec);
		
	}

	
	
	public void invadeCapitol(ResolverSide a) {
		ArrayListGrower<DivGeneration> divs = new ArrayListGrower<>();
		Side winner = a.side;
		
		for (int i = 0; i < winner.divs(); i++) {
			divs.add(winner.div(i).generate());
		}
		
		
		Faction f = winner.us.get(0).faction();
		
		InvasionSpec spec = new InvasionSpec();
		if (f != null)
			spec.fi = f.index();
		spec.divs.clearSloppy();
		spec.divs.add(divs);
		spec.wx = winner.us.get(0).x();
		spec.wy = winner.us.get(0).y();
		
		for (SideUnit s : winner.us) {
			if (s.a() != null) {
				for (ADSupply su : AD.supplies().all) {
					spec.loot.add(su.res, su.current().get(s.a()));
				}
				for (ADArtillery art : AD.supplies().arts())
					spec.artillery[art.index()] += art.current(s.a());
			}
		}
		
		SETT.INVADOR().invade(spec, winner.us.get(0).a());
		
		for (SideUnit u : winner.us) {
			if (u.a() != null)
				u.a().disband();
		}
		return;
		
	}

	
	private static int artillery(ADArtillery a, Side s) {
		int am = 0;
		for (SideUnit u : s.us) {
			if (u.a() != null) {
				am += a.current(u.a());
			}
		}
		return am;
	}
	
	private static double morale(Side s) {
		double d = 0;
		double pop = 0;
		
		for (SideUnit u : s.us) {
			if (u.a() != null) {
				pop = AD.men(null).get(u.a());
				d += AD.morale(u.a())*AD.men(null).get(u.a());
			}
		}
		if (pop == 0)
			return 1;
		return d/pop;
	}
	
	
	
	private class Spec extends WBattleSpec {
		
		private ResolverSide pp;
		private ResolverSide ee;
		
		Spec(){
			
		}
		
		void init(ResolverSide pp, ResolverSide ee) {
			this.pp = pp;
			this.ee = ee;
			this.player = pp;
			this.enemy = ee;
			victory = pp.powerBalance > ee.powerBalance;
		}
		
		@Override
		public void retreat() {
			tmp.clear();
			double losses = Resolver.retreatValue(pp);
			pp.us.get(0).count(tmp.clear(), losses, false);
			Res rr = new Res(pp, ee, BATTLE_RESULT.RETREAT, tmp) {
				
				@Override
				public void accept(int[] enslave, int[] resources) {
					BattleListener.notify(ee, pp);
					shipRetreat(enslave, resources);
					if (pp.retreatCoo.x() >= 0 && pp.side.us.get(0).a() != null) {
						pp.side.us.get(0).a().teleport(pp.retreatCoo.x(), pp.retreatCoo.y());
					}
					pp.extract(losses);
					
				}
			};
			VIEW.world().UI.battle.result(rr, false);
		}
		
		@Override
		public void engage() {
			new Manual(pp.side, ee.side);
		}
		
		@Override
		public void auto() {
			tmp.clear();
			pp.clear().count(cPlayer.clear(), Resolver.autoValue(pp), false);
			ee.clear().count(cEnemy.clear(), Resolver.autoValue(ee), false);
			
			BATTLE_RESULT res = pp.powerBalance > ee.powerBalance ? BATTLE_RESULT.VICTORY : BATTLE_RESULT.DEFEAT;
			
			Res rr = new Res(pp, ee, res, res == BATTLE_RESULT.VICTORY ? cEnemy : cPlayer) {
				
				@Override
				public void accept(int[] enslave, int[] resources) {
					BattleListener.notify(res == BATTLE_RESULT.VICTORY ? pp : ee, res == BATTLE_RESULT.VICTORY ? ee : pp);
					shipRetreat(enslave, resources);
					
					pp.clear().extract(Resolver.autoValue(pp));
					ee.clear().extract(Resolver.autoValue(ee));
				}
			};
			
			VIEW.world().UI.battle.result(rr, false);
				
		}
		
	}
	
	
	private static final class Manual {

		private final Side savedPlayer;
		private final Side savedEnemy;
		
		
		Manual(Side player, Side enemy){
			this.savedPlayer = player.copy();
			this.savedEnemy = enemy.copy();
			BattleStateExiter res = new BattleStateExiter() {
				
				@Override
				public void afterExit(BattleStateResult res) {
					afterBattle(res);
				}
			};
			
			BattleState.setGenerate(res, new BattleSpecc(player, enemy));
		}
		
		void afterBattle(BattleStateResult res) {
			
			RCount ecount = new RCount();
			RCount pcount = new RCount();
			
			ResolverSide ee = new ResolverSide();
			ee.init(savedEnemy, 0);
			ResolverSide pp = new ResolverSide();
			pp.init(savedPlayer, 0);
			
			
			
			if (res.result == BATTLE_RESULT.VICTORY && ee.us.get(0).unit.a() != null) {
				COORDINATE c = Util.retTile(ee.us.get(0).unit.a());
				if (c != null) {
					ee.retreatCoo.set(c);
				}else {
					Arrays.fill(res.enemySurvivors, 0);
				}
			}else if (res.result != BATTLE_RESULT.VICTORY && pp.us.get(0).unit.a() != null) {
				COORDINATE c = Util.retTile(pp.us.get(0).unit.a());
				if (c != null) {
					ee.retreatCoo.set(c);
				}else {
					for (int i = 0; i < res.playerSurvivors.length; i++) {
						res.playerSurvivors[i] = new Induvidual[0];
					}
				}
			}
			
			int[] losses = new int[Config.battle().DIVISIONS_PER_ARMY];
			for (int i = 0; i < savedEnemy.divs(); i++) {
				losses[savedEnemy.ui(i)] += savedEnemy.div(i).men() - res.enemySurvivors[i];
			}
			
			for (int ui = 0; ui < savedEnemy.us.size(); ui++) {
				ee.us.get(ui).count(ecount, losses[ui], false);
			}
			
			
			Arrays.fill(losses, 0);
			for (int di = 0; di < savedPlayer.divs(); di++) {
				if (savedPlayer.div(di) != null)
					losses[savedPlayer.ui(di)] += savedPlayer.div(di).men()-res.playerSurvivors[di].length;
			}
			
			for (int ui = 0; ui < savedPlayer.us.size(); ui++) {
				pp.us.get(ui).count(pcount, losses[ui], false);
			}
			
			
			WBattleResult rr = new Res(pp, ee, res.result, res.result == BATTLE_RESULT.VICTORY ? ecount : pcount) {
				
				@Override
				public void accept(int[] enslave, int[] resources) {
					BattleListener.notify(res.result == BATTLE_RESULT.VICTORY ? pp : ee, res.result == BATTLE_RESULT.VICTORY ? ee : pp);
					shipRetreat(enslave, resources);			
					
					for (int i = 0; i < savedEnemy.divs(); i++) {
						WDIV d = savedEnemy.div(i);
						d.resolve(res.enemySurvivors[i]);
					}
					
					for (int i = 0; i < savedPlayer.divs(); i++) {
						WDIV d = savedPlayer.div(i);
						if (d != null)
							d.resolve(res.playerSurvivors[i]);
					}
					
				}
			};
			
			
			
			VIEW.world().UI.battle.result(rr, false);
		}
		

		
		private class BattleSpecc extends BattleStateSpec{
			
			BattleSpecc(Side player, Side enemy){
				
				set(player, this.player);
				set(enemy, this.enemy);
			}

			private void set(Side side, SpecSide spec) {
				spec.wCoo.set(side.us.get(0).x(), side.us.get(0).y());
				for (int i = 0; i < AD.supplies().arts().size(); i++) {
					spec.artillery[i] = artillery(AD.supplies().arts().get(i), side); 
				}
				spec.moraleBase = morale(side);
				
				for (int di = 0; di < side.divs(); di++) {
					spec.divs.add(side.div(di).generate());
				}
				
			}


		}

		
	}
	
	private static abstract class Res extends WBattleResult {
		
		private final ResolverSide pp;
		private final ResolverSide ee;
		
		Res(ResolverSide pp, ResolverSide ee, BATTLE_RESULT res, RCount ecount){
			this(pp, ee, res, ecount, 1.0);
		}
		
		Res(ResolverSide pp, ResolverSide ee, BATTLE_RESULT res, RCount ecount, double spoilV){
			this.pp = pp;
			this.ee = ee;
			this.player = pp;
			this.enemy = ee;
			this.result = res;
			this.capturedRaces = ecount.dead;
			this.lostResources = ecount.res;
			for (int i = 0; i < ecount.dead.length; i++) {
				capturedRaces[i] = (int) (ecount.dead[i]*0.2*spoilV);
			}
			for (int i = 0; i < ecount.res.length; i++) {
				lostResources[i] = (int) (ecount.res[i]*0.4*spoilV);
			}
		}
		
		void shipRetreat(int[] enslave, int[] resources) {
			ResolverSide looser = ee;
			
			if (result == BATTLE_RESULT.VICTORY) {
				Util.ship(pp.side, ee.side, enslave, resources);
			}else {
				Util.ship(ee.side, pp.side, capturedRaces, lostResources);
				looser = pp;
			}
			
			if (looser.retreatCoo.x() >= 0) {
				looser.us.get(0).unit.a().teleport(looser.retreatCoo.x(), looser.retreatCoo.y());
			}

			stop(pp.side);
			stop(ee.side);
			
		}

		private void stop(Side toSide) {
			for (SideUnit u : toSide.us) {
				if (u.a() != null && u.a().besieging() == null) {
					u.a().stop();
				}
			}
		}
		
	}
	
	private static class Conquer extends WBattleSiege.Result {

		Side player;
		Side enemy;
		
		void init(Region reg, Side player, Side enemy) {
			this.player = player;
			this.besiged = reg;
			this.enemy = enemy;
		}
		
		@Override
		public void occupy(double devastation, double death, int[] enslave, int[] resources) {
			Util.ship(player, enemy, enslave, resources);
			Util.conquer(player, devastation, death, besiged, FACTIONS.player());
			
		}

		@Override
		public void abandon(double devastation, double death, int[] enslave, int[] resources) {
			Util.ship(player, enemy, enslave, resources);
			Util.conquer(player, devastation, death, besiged, null);
			
		}

		@Override
		public void puppet(double devastation, double death, int[] enslave, int[] resources) {
			
			Util.ship(player, enemy, enslave, resources);
			Util.conquer(player, devastation, death, besiged, null);
			
			FactionNPC f = FACTIONS.activateNext(besiged, null, true);
			f.generate(RD.RACES().get(FACTIONS.player().race()), true);
			ROPINIONS.OTHER().liberate(f);
			DIP.VASSAL().set(f, FACTIONS.player());
			GAME.events().world.dip.dismissWelcome(f);
			
		}
		
	}

}
