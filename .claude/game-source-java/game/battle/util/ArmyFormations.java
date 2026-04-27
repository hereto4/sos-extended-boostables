package game.battle.util;

import game.GAME;
import game.battle.util.DIV_SPEC.DIV_SPECImp;
import game.boosting.BOOSTING;
import game.boosting.Boostable;
import init.paths.PATH;
import init.paths.PATHS;
import settlement.stats.STATS;
import settlement.stats.equip.EquipBattle;
import snake2d.util.file.Json;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LIST;

public class ArmyFormations {

	public final ArrayListGrower<ArmyFormation> all = new ArrayListGrower<ArmyFormations.ArmyFormation>();
	public final ArmyFormation player;

	ArmyFormations(){
		PATH p = PATHS.INIT().getFolder("battle").getFolder("formation");
		player = new ArmyFormation(new Json(p.get("_player")));
		for (String f : p.getFiles()) {
			all.add(new ArmyFormation(new Json(p.get(f))));
		}
		
	}
	
	
	
	
	public static class ArmyFormation {
		
		private final LIST<Pair> CENTRE;
		private final LIST<Pair> FLANK;
		private final LIST<Pair> FRONT;
		private final LIST<Pair> REAR;
		
		private final LIST<PairE> CENTRE_E;
		private final LIST<PairE> FLANK_E;
		private final LIST<PairE> FRONT_E;
		private final LIST<PairE> REAR_E;
		
		ArmyFormation(Json json){
			CENTRE = boost("PRIORITY_CENTRE", json);
			FLANK = boost("PRIORITY_FLANK", json);
			FRONT = boost("PRIORITY_FRONT", json);
			REAR = boost("PRIORITY_REAR", json);
			
			CENTRE_E = boostE("PRIORITY_CENTRE_E", json);
			FLANK_E = boostE("PRIORITY_FLANK_E", json);
			FRONT_E = boostE("PRIORITY_FRONT_E", json);
			REAR_E = boostE("PRIORITY_REAR_E", json);
		}
		
		private LIST<Pair> boost(String key, Json json) {
			
			ArrayListGrower<Pair> boosts = new ArrayListGrower<Pair>();
			BOOSTING.connecter(new ACTION() {
				
				@Override
				public void exe() {
					BOOSTING.MAP().new KJson(key, json) {
						
						@Override
						protected void process(Boostable s, Json j, String key, boolean isWeak) {
							
							for (Pair pp : boosts) {
								if (pp.bo == s) {
									pp.value = j.d(key);
									return;
								}
							}
							Pair p = new Pair(s);
							p.value = j.d(key);
							boosts.add(p);
						}
					};
				}
			});
			return boosts;
		}
		
		private LIST<PairE> boostE(String key, Json json) {
			
			ArrayListGrower<PairE> boosts = new ArrayListGrower<PairE>();
			BOOSTING.connecter(new ACTION() {
				
				@Override
				public void exe() {
					STATS.EQUIP().militaryColl.new KJson(key, json) {
						
						@Override
						protected void process(EquipBattle s, Json j, String key, boolean isWeak) {
							
							for (PairE pp : boosts) {
								if (pp.bo == s) {
									pp.value = j.d(key);
									return;
								}
							}
							PairE p = new PairE(s);
							p.value = j.d(key);
							boosts.add(p);
						}
					};
				}
			});
			return boosts;
		}
		
		public ArrayList<ArmyFormationDiv> getFirstRow(LIST<DivGeneration> all) {
			
			ArrayList<ArmyFormationDiv> li = new ArrayList<ArmyFormationDiv>(all.size());
			
			int di = 0;
			for (DivGeneration g : all) {
				
				DIV_SPECImp s = g.makeSpec();
				ArmyFormationDiv d = new ArmyFormationDiv(g, di);
				
				for (Pair p : CENTRE) {
					d.centre += p.value*GAME.battle().boost(s, p.bo)/(1+p.bo.baseValue);		
				}
				for (Pair p : FLANK) {
					
					d.flank += p.value*GAME.battle().boost(s, p.bo)/(1+p.bo.baseValue);		
				}
				for (Pair p : FRONT) {
					d.front += p.value*GAME.battle().boost(s, p.bo)/(1+p.bo.baseValue);					
				}
				for (Pair p : REAR) {
					d.rear += p.value*GAME.battle().boost(s, p.bo)/(1.0+p.bo.baseValue);					
				}
				
				for (PairE p : CENTRE_E) {
					d.centre += p.value*s.equip(p.bo);		
				}
				for (PairE p : FLANK_E) {
					d.flank += p.value*s.equip(p.bo);
				}
				for (PairE p : FRONT_E) {
					d.front += p.value*s.equip(p.bo);
				}
				for (PairE p : REAR_E) {
					d.rear += p.value*s.equip(p.bo);				
				}
				
				
				li.add(d);
				di++;
			}
			return li;
			
		}
		
		public ArmyFormationDiv get(LIST<ArmyFormationDiv> all, double centre, double flank, double front, double rear) {
			
			ArmyFormationDiv res = null;
			double value = Double.MIN_VALUE;
			
			for (ArmyFormationDiv d : all) {
				
				
				double v = d.value(centre, flank, front, rear);
				if (res == null || v > value) {
					res = d;
					value = v;
				}
			}
			
			return res;
		}
		

		
		
	}
	

	
	private static class Pair {
		
		public final Boostable bo;
		public double value;
		public Pair(Boostable bo) {
			this.bo = bo;
		}
		
	}
	
	private static class PairE {
		
		public final EquipBattle bo;
		public double value;
		public PairE(EquipBattle bo) {
			this.bo = bo;
		}
		
	}
	
	public static class ArmyFormationDiv {
		
		public double centre;
		public double flank;
		public double front;
		public double rear;
		
		public DivGeneration g;
		public final int divID;
	
		ArmyFormationDiv(DivGeneration g, int divID){
			this.g = g;
			this.divID = divID;
		}
		
		public double value(double centre, double flank, double front, double rear) {
			return this.centre*centre + this.flank*flank + this.front*front + this.rear*rear;
		}
		
		
	}
	
	
}
