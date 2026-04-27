package init.race.home;

import java.io.IOException;

import game.GAME;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.resources.RES_AMOUNT;
import init.sprite.game.SheetType;
import settlement.main.SETT;
import settlement.room.home.HOME;
import settlement.tilemap.floor.Floors.Floor;
import snake2d.util.file.Json;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;

public final class RaceHomeClass {
	
	private final LIST<RES_AMOUNT> amounts;
	private final int[] ramounts = new int[RESOURCES.ALL().size()];
	private final int amountTotal;
	
	public final RaceHomeSheet bedTop;
	public final RaceHomeSheet bedBottom;
	public final RaceHomeSheet carpet;
	public final RaceHomeSheet table;
	public final RaceHomeSheet nightStand;
	public final RaceHomeSheet storage;
	public final RaceHomeSheet chair;
	public final RaceHomeSheet nick1;
	public final RaceHomeSheet nickTop1;
	public final RaceHomeSheet nick2;
	public final RaceHomeSheet mat;
	public final RaceHomeSheet masterBed;
	public final RaceHomeSheet statue;
	
	private final int[][] fneeded;
	private Floor[] floors; 
	
	RaceHomeClass() throws IOException{
		

		amounts = new ArrayList<RES_AMOUNT>(0);
		amountTotal = 0;
		
		bedTop = new RaceHomeSheet();
		bedBottom = new RaceHomeSheet();
		carpet = new RaceHomeSheet();
		table = new RaceHomeSheet();
		nightStand = new RaceHomeSheet();
		storage = new RaceHomeSheet();
		chair = new RaceHomeSheet();
		nick1 = new RaceHomeSheet();
		nickTop1 = new RaceHomeSheet();
		nick2 = new RaceHomeSheet();
		mat = new RaceHomeSheet();
		masterBed = new RaceHomeSheet();
		statue = new RaceHomeSheet();
		floors = new Floor[0];
		fneeded = new int[0][0];
		
	}
	
	RaceHomeClass(Json json) throws IOException{
		
		ArrayList<RESOURCE> resses = new ArrayList<>(RESOURCES.ALL().size());
		for (String key : json.keys()) {
			Json[] js = json.jsons(key); 
			for (Json jj : js) {
				Json j = jj.json(RESOURCES.KEYS);
				for (String k : j.keys()) {
					RESOURCE res = RESOURCES.map().tryGet(k);
					if (res == null)
						GAME.WarnLight(j.errorGet("No resource with this key! ", k));
					else {
						int am = j.i(k, 1, 15);
						
						if (ramounts[res.index()] == 0) {
							resses.add(res);
						}
						if (am > ramounts[res.index()])
							ramounts[res.index()] = am;
						
					}
				}
			}
			
			
		}
		
		if (resses.size() > 8) {
			json.error("Only 8 distinct resources are allowed", "");
		}
		
		ArrayList<RES_AMOUNT> ams = new ArrayList<>(resses.size());
		int tot = 0;
		for (int ri = 0; ri < resses.size(); ri++){
			ams.add(new RES_AMOUNT.Abs(resses.get(ri), ramounts[resses.get(ri).index()]));
			tot += ramounts[resses.get(ri).index()];
		}
		
		amounts = ams;
		amountTotal = tot;
		
		bedTop = new RaceHomeSheet(amounts, json, "BED_1x1_TOP", SheetType.s1x1);
		bedBottom = new RaceHomeSheet(amounts, json, "BED_1x1_BOTTOM", SheetType.s1x1);
		carpet = new RaceHomeSheet(amounts, json, "CARPET_COMBO", SheetType.sCombo);
		table = new RaceHomeSheet(amounts, json, "TABLE_COMBO", SheetType.sCombo);
		nightStand = new RaceHomeSheet(amounts, json, "NIGHTSTAND_1x1", SheetType.s1x1);
		storage = new RaceHomeSheet(amounts, json, "STORAGE_1x1", SheetType.s1x1);
		chair = new RaceHomeSheet(amounts, json, "CHAIR_1x1", SheetType.s1x1);
		nick1 = new RaceHomeSheet(amounts, json, "NICKNACK_A_1x1", SheetType.s1x1);
		nickTop1 = new RaceHomeSheet(amounts, json, "NICKNACK_A_ONTOP_1x1", SheetType.s1x1);
		nick2 = new RaceHomeSheet(amounts, json, "NICKNACK_B_1x1", SheetType.s1x1);
		mat = new RaceHomeSheet(amounts, json, "MAT_1x1", SheetType.s1x1);
		masterBed = new RaceHomeSheet(amounts, json, "BED_MASTER_2x2", SheetType.s2x2);
		statue = new RaceHomeSheet(amounts, json, "STATUE_2x2", SheetType.s2x2);
		
		if (json.has("FLOORS")) {
			Json[] jsons = json.jsons("FLOORS");
			floors = new Floor[jsons.length];
			fneeded = new int[jsons.length][ams.size()];
			
			for (int i = 0; i < jsons.length; i++) {
				Json j = jsons[i];
				RaceHomeSheet.addResource(ams, j, i, fneeded);
				floors[i] = SETT.FLOOR().map.read(j);
			}
		}else {
			floors = new Floor[0];
			fneeded = new int[0][0];
		}
		
		
	}
	
	public LIST<RES_AMOUNT> resources(){
		return amounts;
	}
	
	public int amount(RESOURCE res){
		return ramounts[res.index()];
	}
	
	public int amountTotal() {
		return amountTotal;
	}
	
	public Floor floor(HOME data) {
		outer:
			for (int ai = floors.length-1; ai >= 0; ai--) {
				int[] amounts = fneeded[ai];
				for (int i = 0; i < amounts.length; i++) {
					if (data.resourceAm(i) < amounts[i])
						continue outer;
				}
				return floors[ai];
			}
			return null;
	}
	
}