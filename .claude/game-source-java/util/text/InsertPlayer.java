package util.text;

import game.faction.FACTIONS;
import game.faction.Faction;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.room.main.RoomBlueprintIns;
import settlement.stats.STATS;
import snake2d.util.MATH;
import snake2d.util.sprite.text.Str;
import world.WORLD;
import world.map.regions.Region;
import world.map.regions.WREGIONS;
import world.region.RD;

final class InsertPlayer extends Inserter<Integer> {
	

	
	public InsertPlayer() {
		
		new II("PLAYER_RND_REGION_CLOSE") {

			@Override
			public void set(Integer t, Str str) {
				Region res = FACTIONS.player().capitolRegion();
				int ri = t;
				for (int i = 0; i < WREGIONS.MAX; i++) {
					Region reg = WORLD.REGIONS().all().getC(ri+i);
					if (reg.active() && reg != FACTIONS.player().capitolRegion() && RD.DIST().distance().get(reg) < 200) {
						res = reg;
						break;
					}
				}
				
				if (res != null)
					str.add(res.info.name());
				
			}
			
		};
		
		new II("PLAYER_RND_REGION_FAR") {

			@Override
			public void set(Integer t, Str str) {
				Region res = FACTIONS.player().capitolRegion();
				int ri = t + 10;
				for (int i = 0; i < WREGIONS.MAX; i++) {
					Region reg = WORLD.REGIONS().all().getC(ri+i);
					if (reg.active() && reg != FACTIONS.player().capitolRegion() && RD.DIST().distance().get(reg) > 200) {
						res = reg;
						break;
					}
				}
				
				if (res != null)
					str.add(res.info.name());
				
			}
			
		};
		
		new II("PLAYER_RND_FACTION_CLOSE") {

			@Override
			public void set(Integer t, Str str) {
				Faction res = null;
				int ri = (int) t;
				for (int i = 0; i < FACTIONS.MAX(); i++) {
					Faction f = FACTIONS.getByIndex(MATH.mod(ri+i, WREGIONS.MAX));
					if (f != null && f.isActive() && f != FACTIONS.player() && RD.DIST().distance().get(f.capitolRegion()) < 200) {
						res = f;
						break;
					}
				}
				
				if (res != null)
					str.add(res.name);
				else
					str.add("Empire of Sand");
				
			}
			
		};
		
		new II("PLAYER_RND_FACTION_FAR") {

			@Override
			public void set(Integer t, Str str) {
				Faction res = null;
				int ri = (int) t + 10;
				for (int i = 0; i < FACTIONS.MAX(); i++) {
					Faction f = FACTIONS.getByIndex(MATH.mod(ri+i, WREGIONS.MAX));
					if (f != null && f.isActive()&& f != FACTIONS.player() && RD.DIST().distance().get(f.capitolRegion()) > 200) {
						res = f;
						break;
					}
				}
				
				if (res != null)
					str.add(res.name);
				else
					str.add("Empire of Sand");
				
			}
			
		};
		
		for (int i = 0; i < 2; i++) {
			final int kk = i;
			new II("PLAYER_CITY_RND_NAME_RACE_" + (i+1)) {
				
				@Override
				public void set(Integer t, Str str) {
					str.add("Bob");
					int ri = Integer.MAX_VALUE&t;
					int skip = kk;
					ENTITY[] ee = SETT.ENTITIES().getAllEnts();
					int f = ri%ee.length;
					for (int k = 0; k < ee.length; k++) {
						f++;
						if (f >= ee.length)
							f = 0;
						if (ee[f] instanceof Humanoid) {
							Humanoid a = (Humanoid) ee[f];
							if (a.indu().player() && a.race() == FACTIONS.player().race()) {
								skip--;
								if (skip < 0) {
									str.clear().add(STATS.APPEARANCE().name(a.indu()));
									return;
								}
									
							}
						}
					}
					
				}
				
			};
		}
		
		new II("PLAYER_CITY_ROOM_RND") {

			@Override
			public void set(Integer t, Str str) {
				int tot = 0;
				for (RoomBlueprintIns<?> in : SETT.ROOMS().ins()) {
					if (in instanceof RoomBlueprintIns<?>)
						tot += in.instancesSize();
				}
				
				tot &= t & Integer.MAX_VALUE;
				
				for (RoomBlueprintIns<?> in : SETT.ROOMS().ins()) {
					if (in instanceof RoomBlueprintIns<?>) {
						if (tot >= in.instancesSize())
							tot -= in.instancesSize();
						else {
							str.add((in.getInstance(tot).name()));
							return;
						}
					}
					if (tot <= 0) {
						
					}
				}
				str.add("no rooms");
				
			}
			
		};
		
	}
	
}
