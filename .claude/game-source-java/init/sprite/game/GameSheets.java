package init.sprite.game;

import java.io.IOException;

import game.GAME;
import init.paths.PATH;
import init.paths.PATHS;
import snake2d.util.file.Json;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.KeyMap;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.TILE_SHEET;

public class GameSheets {
	
	private final Sheet[] overlays = new Sheet[SheetType.ALL.size()];
	private final LIST<KeyMap<LIST<Sheet>>> gsheets;
	private final LIST<KeyMap<LIST<TILE_SHEET>>> raws;
	final KeyMap<SheetType> imap = new KeyMap<>();
	
//	public final Textures textures;
	
	public GameSheets() throws IOException {
		ArrayList<KeyMap<LIST<Sheet>>> m = new ArrayList<>(SheetType.ALL.size());
		ArrayList<KeyMap<LIST<TILE_SHEET>>> r = new ArrayList<>(SheetType.ALL.size());
		gsheets = m;
		raws = r;
		for (SheetType t : SheetType.ALL) {
			imap.put(t.path, t);
			m.add(new KeyMap<LIST<Sheet>>());
			r.add(new KeyMap<LIST<TILE_SHEET>>());
		}
		for (SheetType t : SheetType.ALL) {
			if (t == SheetType.sCombo)
				continue;
			if (t == SheetType.sBox)
				continue;
			if (t == SheetType.sTex)
				continue;
			this.overlays[t.index()] = sheets(t, "_OVERLAY", null).get(0);
		}
	}
	
	public LIST<TILE_SHEET> raws(SheetType t, String file, Json error) throws IOException {
		
		if (raws.get(t.index()).containsKey(file)) {
			return raws.get(t.index()).get(file);
		}
		LIST<TILE_SHEET> sh = t.make(file, error);
		raws.get(t.index()).put(file, sh);
		
		ArrayList<Sheet> res = new ArrayList<Sheet>(sh.size());
		for (TILE_SHEET s : sh)
			res.add(new Sheet.Imp(t, s, true));
		
		gsheets.get(t.index()).put(file, res);
		
		return sh;
		
	}
	
	public TILE_SHEET raw(SheetType t, Json json) throws IOException {
		json = json.json("GAME_TEXTURE");
		String file = json.value("FILE");
		int row = json.i("ROW");
		return raw(t, file, row, json);
		
	}
	
	public TILE_SHEET raw(SheetType t, String key, Json json) throws IOException {
		json = json.json(key);
		String file = json.value("FILE");
		int row = json.i("ROW");
		return raw(t, file, row, json);
		
	}
	
	public TILE_SHEET raw(SheetType t, String file, int row, Json error) throws IOException{
		
		LIST<TILE_SHEET> li = raws(t, file, error);
		
		if (row >= li.size()) {
			if (error != null)
				GAME.WarnLight(row + "is outside of the available sprites. In + file " + error.path());
			return SheetType.DUMMY;
		}
		return li.get(row);
	}

	
	public Sheet overlay(SheetType t) {
		return overlays[t.index()];
	}
	
	public LIST<Sheet> sheets(SheetType t, String file, Json error) throws IOException {
		if (gsheets.get(t.index()).containsKey(file))
			return gsheets.get(t.index()).get(file);
		raws(t, file, error);
		LIST<Sheet> ss = gsheets.get(t.index()).get(file);
		
		return ss;
		
	}
	
	public void add(SheetType t, LIST<Sheet> sh, String key) {
		if (gsheets.get(t.index()).containsKey(key)) {
			throw new RuntimeException(key);
		}
		gsheets.get(t.index()).put(key, sh);
	}
	
	private boolean[] adump = new boolean[SheetType.ALL.size()];
	
	public LIST<SheetPair> sheets(SheetType type, Json json) throws IOException {
		String[] ss = json.values("FRAMES");
		
		SheetData[] datas = new SheetData[ss.length];
		{
			SheetData odata = new SheetData(json);
			for (int i = 0; i < datas.length; i++) {
				datas[i] = odata;
			}
			if (json.has("OVERWRITE")) {
				Json[] js = json.jsons("OVERWRITE");
				for (int i = 0; i < datas.length && i < js.length; i++) {
					datas[i] = new SheetData(odata, js[i]);
				}
			}
		}
		
		
		
		int i = 0;
		ArrayList<SheetPair> sheets = new ArrayList<>(ss.length);
		
		for (String k : ss) {
			
			SheetData data = datas[i];
			
			if (k.equals("-")) {
				sheets.add(new SheetPair(type.dummy(), data));
				i++;
				continue;
			}
				
			String[] chops = k.split(":");

			if (chops.length != 2) {
				json.error("malformatted frame. Format is FILENAME:ROW Current is: "+k, k);
			}
			
			String file = chops[0].trim();
			String snr = chops[1].trim();
			
			PATH p = PATHS.SPRITE_GAME().getFolder(type.path);
			
			if (!gsheets.get(type.index()).containsKey(file)) {
				
				
				if (!p.exists(file)) {
					String a = "";
					if (!adump[type.index()]) {
						a = System.lineSeparator() + "Available: ";
						adump[type.index()] = true;
						for (String ke : gsheets.get(type.index()).keys()) {
							a += System.lineSeparator() + ke;
						}
						for (String ke : p.getFiles()) {
							if (!gsheets.get(type.index()).containsKey(ke)) {
								a += System.lineSeparator() + ke;
							}
						}
					}
					
					GAME.WarnLight(p.get() + "/" + file + " does not exist and will be ignored. Refrenced from: " + json.path() + a);
					continue;
				}
			}
			LIST<Sheet> shs = sheets(type, file, json);
			
			int nr = 0;
			try {
				nr = Integer.parseInt(snr);
			}catch(Exception e) {
				json.error("malformatted Row. Format is FOLDER:FILENAME:ROW", k);
				continue;
			}
			
			if (nr < 0 || nr >= shs.size()) {
				GAME.WarnLight("ROW: " + nr + " in file: " + p.get(file) + " is out of bounds. must specify a row of the image: " + json.path());
				continue;
			}

			
			sheets.add(new SheetPair(shs.get(nr), data));
		}
		
		return new ArrayList<>(sheets);
		
		
	}
	
}
