package init.race;

import java.io.IOException;

import init.paths.PATH;
import init.paths.PATHS;
import init.race.appearence.RaceSprites;
import snake2d.Errors;
import snake2d.util.file.Json;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import util.keymap.RMAPS;
import util.text.D;

public class RACES {

	private static RACES i;
	
	private final ArrayList<Race> all;
	private final ArrayList<Race> playable;
	private final RMAPS<Race> map;
	private RaceServiceSorter service;
	
	private static CharSequence ¤¤name = "¤Species";
	
	private RaceSprites sprites;
	private final RaceBoosts boosts;
	private RaceResources resources;
	static {
		D.ts(RACES.class);
	}
	
	public RACES() {
		
		i = this;
		PATH p = PATHS.INIT().getFolder("race");
		PATH pt = PATHS.TEXT().getFolder("race");
		String[] files = p.getFiles();
		all = new ArrayList<Race>(files.length);
		
		
		if (files.length == 0) {
			throw new Errors.DataError("no races defined!", p.get());
		}
		
		for (String s : files) {
			new Race(s, new Json(p.get(s)), new Json(pt.get(s)), all);
		}

		int pl = 0;
		for (Race r : all) {
			if (r.playable)
				pl++;
		}
		this.map = new RMAPS<Race>("RACES", all);
		
		
		playable = new ArrayList<>(pl);
		for (Race r : all) {
			if (r.playable)
				playable.add(r);
		}

		boosts = new RaceBoosts();
		
		if (false) {
			//gathimi carry capacity
		}
	}
	
	public static void expand() throws IOException {
		
		ExpandInit init = new ExpandInit();
		
		for (Race r : i.all) {
			r.expand(init);
		}
		RacePreferrence.init();
		
		i.sprites = new RaceSprites();
		i.resources = new RaceResources(i.all);
	}
	
	public static RaceResources res(){
		return i.resources;
	}
	
	public static LIST<Race> all(){
		return i.all;
	}
	
	public static LIST<Race> playable(){
		return i.playable;
	}
	
	public static RMAPS<Race> map(){
		return i.map;
	}
	
	public static RaceServiceSorter SERVICE() {
		return i.service;
	}
	
	public static CharSequence name() {
		return ¤¤name;
	}
	
	public static RaceSprites sprites() {
		return i.sprites;
	}
	
	public static RaceBoosts boosts() {
		return i.boosts;
	}
	


	
}
