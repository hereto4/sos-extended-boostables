package world.battle;

import game.battle.state.BattleStateExiter;
import game.battle.state.BattleStateResult;
import game.battle.state.BattleStateSpec;
import game.battle.state.BattleStateSpec.SpecSide;
import game.battle.state.BattleState;
import game.battle.util.DivGeneration;
import game.faction.FACTIONS;
import init.race.RACES;
import init.race.Race;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.sprite.UI.UI;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Coo;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.SPRITE;
import util.text.Dic;
import view.main.VIEW;
import view.tool.PlacableSimpleTile;
import view.world.panel.IDebugPanelWorld;
import world.WORLD;
import world.army.AD;
import world.army.ADSupplies.ADArtillery;
import world.battle.spec.BATTLE_RESULT;
import world.battle.spec.WBattleResult;
import world.battle.spec.WBattleSide;
import world.battle.spec.WBattleSiege;
import world.battle.spec.WBattleSpec;
import world.battle.spec.WBattleUnit;
import world.map.regions.Region;
import world.region.RD;

class Tests {
	
	
	public Tests(){
		
		IDebugPanelWorld.add(new PlacableSimpleTile("battle create") {
			
			@Override
			public void place(int tx, int ty) {
				WBattleSpec s = spec(tx, ty, null, null); 
				VIEW.world().UI.battle.battle(s);
				
			}
			
			@Override
			public CharSequence isPlacable(int tx, int ty) {
				return WORLD.IN_BOUNDS(tx, ty) ? null : E;
			}
		});
		
		IDebugPanelWorld.add(new PlacableSimpleTile("battle assist") {
			
			@Override
			public void place(int tx, int ty) {
				WBattleSpec s = spec(tx, ty, null, null); 
				VIEW.world().UI.battle.assist(s);
				
			}
			
			@Override
			public CharSequence isPlacable(int tx, int ty) {
				return WORLD.IN_BOUNDS(tx, ty) ? null : E;
			}
		});
		
		IDebugPanelWorld.add(new PlacableSimpleTile("battle lastStand") {
			
			@Override
			public void place(int tx, int ty) {
				
				Region reg = WORLD.REGIONS().map.get(tx, ty);
				
				WBattleUnit ss = new WBattleUnit() {
					
					@Override
					public CharSequence name() {
						return reg.info.name();
					}
					
					@Override
					public int men() {
						return RD.MILITARY().garrison.get(reg);
					}
					
					@Override
					public int lossesRetreat() {
						return men();
					}
					
					@Override
					public int losses() {
						return men();
					}
					
					@Override
					public SPRITE icon() {
						return UI.icons().m.building;
					}
					
					@Override
					public void hover(GUI_BOX box) {
						box.title(Dic.¤¤Region);
					}
					
					@Override
					public double defences() {
						return RD.MILITARY().fort.getD(reg);
					}
				};
				
				WBattleSpec s = spec(tx, ty, ss, null); 
				VIEW.world().UI.battle.lastStand(s);
				
			}
			
			@Override
			public CharSequence isPlacable(int tx, int ty) {
				return WORLD.REGIONS().centreTile().is(tx, ty) ? null : E;
			}
		});
		
		IDebugPanelWorld.add(new PlacableSimpleTile("battle sally") {
			
			@Override
			public void place(int tx, int ty) {
				
				Region reg = WORLD.REGIONS().map.get(tx, ty);
				
				WBattleUnit ss = new WBattleUnit() {
					
					@Override
					public CharSequence name() {
						return reg.info.name();
					}
					
					@Override
					public int men() {
						return RD.MILITARY().garrison.get(reg);
					}
					
					@Override
					public int lossesRetreat() {
						return 0;
					}
					
					@Override
					public int losses() {
						return (int) (men()*0.5);
					}
					
					@Override
					public SPRITE icon() {
						return UI.icons().m.building;
					}
					
					@Override
					public void hover(GUI_BOX box) {
						box.title(Dic.¤¤Region);
					}
					
					@Override
					public double defences() {
						return 0;
					}
				};
				
				WBattleSpec s = spec(tx, ty, ss, null); 
				VIEW.world().UI.battle.battleSally(s);
				
			}
			
			@Override
			public CharSequence isPlacable(int tx, int ty) {
				return WORLD.REGIONS().centreTile().is(tx, ty) ? null : E;
			}
		});
		
		IDebugPanelWorld.add(new PlacableSimpleTile("battle siege") {
			
			@Override
			public void place(int tx, int ty) {
				
				Region reg = WORLD.REGIONS().map.get(tx, ty);
				
				WBattleUnit ss = new WBattleUnit() {
					
					@Override
					public CharSequence name() {
						return reg.info.name();
					}
					
					@Override
					public int men() {
						return RD.MILITARY().garrison.get(reg);
					}
					
					@Override
					public int lossesRetreat() {
						return 0;
					}
					
					@Override
					public int losses() {
						return (int) (men()*0.5);
					}
					
					@Override
					public SPRITE icon() {
						return UI.icons().m.building;
					}
					
					@Override
					public void hover(GUI_BOX box) {
						box.title(Dic.¤¤Region);
					}
					
					@Override
					public double defences() {
						return 20;
					}
				};
				
				WBattleSiege s = new WBattleSiege() {
					
					@Override
					public void retreat() {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void auto() {
						// TODO Auto-generated method stub
						
					}
				};
				
				s.besiged = reg;
				s.victory = RND.rBoolean();
				s.player = side(RND.rFloat(), tx, ty, null);
				s.enemy = side(RND.rFloat(), tx, ty, ss);
				s.fortifications = 20;
				VIEW.world().UI.battle.siege(s);
				
			}
			
			@Override
			public CharSequence isPlacable(int tx, int ty) {
				return WORLD.REGIONS().centreTile().is(tx, ty) ? null : E;
			}
		});
		
		IDebugPanelWorld.add(new PlacableSimpleTile("battle conquer") {
			
			@Override
			public void place(int tx, int ty) {
				
				Region reg = WORLD.REGIONS().map.get(tx, ty);
				
				WBattleSiege.Result res = new WBattleSiege.Result() {
					
					@Override
					public void puppet(double devastation, double death, int[] enslave, int[] resources) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void occupy(double devastation, double death, int[] enslave, int[] resources) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void abandon(double devastation, double death, int[] enslave, int[] resources) {
						// TODO Auto-generated method stub
						
					}
				};
				res.besiged = reg;
				
				VIEW.world().UI.battle.result(res);
				
			}
			
			@Override
			public CharSequence isPlacable(int tx, int ty) {
				return WORLD.REGIONS().centreTile().is(tx, ty) ? null : E;
			}
		});

	}
	
	private WBattleSpec spec(int tx, int ty, WBattleUnit p, WBattleUnit e) {

		WBattleSpec s = new WBattleSpec() {
			
			@Override
			public void retreat() {
				WBattleResult res = new WBattleResult() {

					@Override
					public void accept(int[] enslave, int[] resources) {
						// TODO Auto-generated method stub
						
					}
				
				};
				
				res.player = player;
				res.enemy = enemy;
				res.result = BATTLE_RESULT.RETREAT;
				init(res, player, -1);
				VIEW.world().UI.battle.result(res, false);
				
			}
			
			@Override
			public void engage() {
				BattleStateSpec s = new BattleStateSpec();
				s.player = new SpecSide();
				s.player.divs = new ArrayList<>(1+ RND.rInt(25));
				for (int i = 0; i < s.player.artillery.length; i++) {
					s.player.artillery[i] = player.artillery(AD.supplies().arts().get(i));
				}
				
				s.player.moraleBase = 1.0;
				s.player.wCoo.set(player.coo());
				for (int i = 0; i < s.player.divs.max(); i++)
					s.player.divs.add(DivGeneration.rnd());
				s.enemy = new SpecSide();
				s.enemy.divs = new ArrayList<>(1+RND.rInt(25));
				for (int i = 0; i < s.player.artillery.length; i++) {
					s.player.artillery[i] = player.artillery(AD.supplies().arts().get(i));
				}
				
				for (int i = 0; i < s.player.artillery.length; i++) {
					s.enemy.artillery[i] = enemy.artillery(AD.supplies().arts().get(i));
				}
				
				s.enemy.moraleBase = 1.0;
				s.enemy.wCoo.set(enemy.coo());
				while(s.enemy.divs.hasRoom())
					s.enemy.divs.add(DivGeneration.rnd());
				
				BattleStateExiter res = new BattleStateExiter() {
					
					@Override
					public void afterExit(BattleStateResult res) {
						// TODO Auto-generated method stub
						
					}
				};
				
				BattleState.setGenerate(res, s);
			}
			
			@Override
			public void auto() {
				WBattleResult res = new WBattleResult() {


					@Override
					public void accept(int[] enslave, int[] resources) {
						// TODO Auto-generated method stub
						
					}
				
				};
				if (!victory)
					init(res, player, -1);
				else
					init(res, enemy, 1);
				res.player = player;
				res.enemy = enemy;
				res.result = RND.rBoolean() ? BATTLE_RESULT.VICTORY : (RND.rBoolean() ? BATTLE_RESULT.DEFEAT : BATTLE_RESULT.RETREAT);
				VIEW.world().UI.battle.result(res, RND.rBoolean());
				
			}
		};
		
		double power = RND.rFloat();
		s.player = side(power, tx, ty, p);
		s.enemy = side(1.0-power, tx+1, ty, e);
		s.victory = power >= 0.5;
		return s;
	}
	
	private WBattleSide side(double power, int tx, int ty, WBattleUnit first) {
		
		ArrayList<WBattleUnit> us = new ArrayList<WBattleUnit>(1+RND.rInt(10)+1);
		
		int tloss = 0;
		int tlossRet = 0;
		int tmen = 0;
		
		if (first != null)
			us.add(first);
		
		while(us.hasRoom()) {
			
			CharSequence name = ""+RACES.all().rnd().info.armyNames.rnd();
			int men = 1 + RND.rInt(20000);
			int losses = 1+ RND.rInt(men);
			int lossesRetreat = RND.rInt(losses);
			int ri = RND.rInt();
			tloss += losses;
			tlossRet += lossesRetreat;
			tmen += men;
			
			SPRITE icon = FACTIONS.NPCs().getC(ri).banner().MEDIUM;
			WBattleUnit u = new WBattleUnit() {
				
				@Override
				public void hover(GUI_BOX box) {
					box.text(Dic.¤¤Babies);
					box.NL();
					box.add(box.text().add(men));
				}

				@Override
				public CharSequence name() {
					return name;
				}

				@Override
				public int men() {
					return men;
				}

				@Override
				public int losses() {
					return losses;
				}

				@Override
				public int lossesRetreat() {
					return lossesRetreat;
				}

				@Override
				public SPRITE icon() {
					return icon;
				}

				@Override
				public double defences() {
					return 0;
				}
			};
			us.add(u);
			
		}
		final int men = tmen;
		final int loss = tloss;
		final int lossR = tlossRet;
		WBattleSide a = new WBattleSide() {

		
		
			Coo coo = new Coo(tx, ty);
			
			@Override
			public COORDINATE coo() {
				return coo;
			}

			@Override
			public int men() {
				return men;
			}

			@Override
			public int losses() {
				return loss;
			}

			@Override
			public int lossesRetreat() {
				return lossR;
			}

			@Override
			public LIST<WBattleUnit> units() {
				return us;
			}

			@Override
			public int artillery(ADArtillery a) {
				return 2;
			}

			@Override
			public double powerBalance() {
				return power;
			}
			
		};

		return a;
		
		
		
	}
	
	private void init(WBattleResult a, WBattleSide s, int i) {
		int captured = RND.rInt(1 + s.men());
		
		for (Race race : RACES.all()) {
			if (captured == 0)
				break;
			a.capturedRaces[race.index()] = i*RND.rInt(captured);
			captured -= a.capturedRaces[race.index()];
		}
		
		for (RESOURCE res : RESOURCES.ALL()) {
			if (RND.oneIn(5)) {
				a.lostResources[res.index()] = i*RND.rInt(5000);
			}
		}
	}
	

	
}
