package init.value;

import game.GAME;
import game.boosting.BOOSTING;
import game.boosting.Boostable;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.diplomacy.DIP;
import game.faction.diplomacy.DipStance;
import game.faction.npc.FactionNPC;
import game.faction.royalty.NPCCourt;
import game.faction.royalty.Royalty;
import game.faction.royalty.opinion.ROPINIONS;
import init.sprite.UI.UI;
import init.type.TERRAIN;
import init.type.TERRAINS;
import settlement.stats.Induvidual;
import util.data.BOOLEANO;
import util.data.DOUBLE_O;
import util.text.D;
import util.text.Dic;
import world.map.regions.Region;
import world.map.regions.RegionInfo;

class GValuesInit {

	private static CharSequence ¤¤roySucc = "Succession Order";
	private static CharSequence ¤¤regFactionNo = "Free Lands";
	
	static {
		D.ts(GValuesInit.class);
	}
	
	public static void init() {
		faction();
		royalty();
		region();
	}
	
	private static void royalty() {
		GValueCat<Royalty> V = GVALUES.ROYALTY;
		
		for (Value<Faction> v : GVALUES.FACTION.map().allSorted()) {
			
			DOUBLE_O<Royalty> va = new DOUBLE_O<Royalty>() {

				@Override
				public double getD(Royalty t) {
					return v.d.getD(t.court.faction);
				}
				
			};
			Value<Royalty> roy = new Value<Royalty>("FACTION_" + v.key, v.icon, v.name, va, v.percentage, v.isBool);
			V.push(roy);
		}
		
		for (Value<Induvidual> v : GVALUES.INDU.map().allSorted()) {
			
			DOUBLE_O<Royalty> va = new DOUBLE_O<Royalty>() {

				@Override
				public double getD(Royalty t) {
					return v.d.getD(t.induvidual);
				}
				
			};
			Value<Royalty> roy = new Value<Royalty>("INDUVIDUAL_" + v.key, v.icon, v.name, va, v.percentage, v.isBool);
			V.push(roy);
		}
		
		for (int i = 0; i < NPCCourt.MAX; i++) {
			final int k = i;
			V.push("SUCCESSION_ORDER_" + i, ¤¤roySucc, UI.icons().s.noble, new BOOLEANO<Royalty>() {
				@Override
				public boolean is(Royalty t) {
					return t.successionI() == k;
				}
				
			});
		}
		
	}
	
	public static void region() {
		GValueCat<Region> V = GVALUES.REGION;
		
		for (Value<Faction> v : GVALUES.FACTION.map().allSorted()) {
			
			DOUBLE_O<Region> va = new DOUBLE_O<Region>() {

				@Override
				public double getD(Region t) {
					return t.faction() == null ? 0 : v.d.getD(t.faction());
				}
				
			};
			Value<Region> roy = new Value<Region>("FACTION_" + v.key, v.icon, v.name, va, v.percentage, v.isBool);
			V.push(roy);
		}
		
		V.push("FACTION_NONE", ¤¤regFactionNo, UI.icons().s.flag, new BOOLEANO<Region>() {
			@Override
			public boolean is(Region t) {
				return t.faction() == null;
			}
			
		});
		
		V.push("HAS_BOOST_PERM", "Has Boost", UI.icons().s.question, new BOOLEANO<Region>() {
			@Override
			public boolean is(Region t) {
				return GAME.BOOST().regions.any(t);
			}
			
		});
		
		V.push("IS_CAPITAL", Dic.¤¤Capitol, UI.icons().s.question, new BOOLEANO<Region>() {
			@Override
			public boolean is(Region t) {
				return t.capitol();
			}
			
		});
		
		{
			DOUBLE_O<Region> v = new DOUBLE_O<Region>() {

				@Override
				public double getD(Region t) {
					return  RegionInfo. vFer().get(t);
				}
				
			};
			GVALUES.REGION.push("PROP_FERTILTIY", Dic.¤¤Fertility, UI.icons().s.sprout, v);
		}
		{
			DOUBLE_O<Region> v = new DOUBLE_O<Region>() {

				@Override
				public double getD(Region t) {
					return RegionInfo.vArea().get(t);
				}
				
			};
			GVALUES.REGION.push("PROP_AREA", Dic.¤¤Area, UI.icons().s.expand, v);
		}
		for (TERRAIN t : TERRAINS.ALL()) {
			DOUBLE_O<Region> v = new DOUBLE_O<Region>() {

				@Override
				public double getD(Region r) {
					return RegionInfo.vTerrain(t).get(r);
				}
				
			};
			GVALUES.REGION.push("PROP_TERRAIN_" + t.key, t.name, t.icon(), v);
		}
		
		
		{
			DOUBLE_O<Region> v = new DOUBLE_O<Region>() {

				@Override
				public double getD(Region t) {
					return Math.max(RegionInfo.vTerrain(TERRAINS.WET()).get(t), RegionInfo.vTerrain(TERRAINS.OCEAN()).get(t));
				}
				
			};
			
			GVALUES.REGION.push("PROP_TERRAIN_WATER", Dic.¤¤Water, UI.icons().s.drop, v);
		}
		
	}
	
	public static void faction() {
		GValueCat<Faction> V = GVALUES.FACTION;
		
		for (Boostable b : BOOSTING.ALL()) {
			V.push("BOOST_" + b.key, b.name, b.icon, new DOUBLE_O<Faction>() {
				@Override
				public double getD(Faction t) {
					return b.get(t);
				}
			}, false);
		}
		
		V.push("HAS_BOOST_PERM", "Has Boost", UI.icons().s.question, new BOOLEANO<Faction>() {
			@Override
			public boolean is(Faction t) {
				return GAME.BOOST().factions.any(t);
			}
			
		});
		
		V.push("IS_PLAYER", "Is Player", UI.icons().s.question, new BOOLEANO<Faction>() {
			@Override
			public boolean is(Faction t) {
				return t == FACTIONS.player();
			}
			
		});
		
		dip(DIP.ALLY());
		dip(DIP.NEUTRAL());
		dip(DIP.OVERLORD());
		dip(DIP.PACT());
		dip(DIP.TRADE());
		dip(DIP.VASSAL());
		dip(DIP.WAR());
		
		V.push("STACE_IS_TRADING", "Trading", UI.icons().s.question, new BOOLEANO<Faction>() {
			@Override
			public boolean is(Faction t) {
				if (t instanceof FactionNPC) {
					return DIP.get((FactionNPC) t).trades;
				}
				return false;
			}
			
		});
		
		V.push("STACE_IS_ALLY", "Trading", UI.icons().s.question, new BOOLEANO<Faction>() {
			@Override
			public boolean is(Faction t) {
				if (t instanceof FactionNPC) {
					return DIP.get((FactionNPC) t).ally;
				}
				return false;
			}
			
		});
		
		V.push("OPINION_ABS", "Opinion", UI.icons().s.question, new DOUBLE_O<Faction>() {

			@Override
			public double getD(Faction t) {
				if (t instanceof FactionNPC) {
					Royalty roy = ((FactionNPC) t).court().king().roy();
					return ROPINIONS.BOOST().get(roy);
				}
				return 0;
			}
			
		});
		
		V.push("OPINION_REL", "Opinion", UI.icons().s.question, new DOUBLE_O<Faction>() {

			@Override
			public double getD(Faction t) {
				if (t instanceof FactionNPC) {
					return ROPINIONS.peaceValue((FactionNPC) t);
				}
				return 0;
			}
			
		});
		
		V.push("RIVALRY", "Rivalry", UI.icons().s.question, new DOUBLE_O<Faction>() {

			@Override
			public double getD(Faction t) {
				if (t instanceof FactionNPC) {
					return ROPINIONS.rivalry((FactionNPC) t);
				}
				return 0;
			}
			
		});
	}
	
	private static void dip(DipStance dip) {
		GVALUES.FACTION.push("STANCE_" + dip.key(), dip.name, dip.icon, new BOOLEANO<Faction>() {
			@Override
			public boolean is(Faction t) {
				if (t instanceof FactionNPC) {
					return dip.is((FactionNPC) t);
				}
				return false;
			}
			
		});
	}
	
}
