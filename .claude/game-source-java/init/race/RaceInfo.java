package init.race;

import game.raiding.RaiderTextsRace;
import init.paths.PATHS;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import snake2d.util.file.Json;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.KeyMap;
import snake2d.util.sets.LIST;
import util.info.INFO;

public final class RaceInfo extends INFO {

	public final String namePosessive;
	public final String namePosessives;
	public final String desc_long;
	public final String initialChallenge;
	public final String[] pros;
	public final String[] cons;
	public final String[] raiderNames;
	public final RaiderTextsRace raiderMess;
	public final RaceWorldInfo winfo;
	private static KeyMap<RaceWorldInfo> wi = new KeyMap<>();
	private static KeyMap<String[]> ra = new KeyMap<>();
	private static KeyMap<RaiderTextsRace> ram = new KeyMap<>();
	public final LIST<String> armyNames;
	public final RPronoun pHE;
	public final RPronoun pHIS;
	public final RPronoun pHIMSELF;
	public final RPronoun pHIM;

	public final CharSequence[] sHello;
	public final CharSequence[] sGoodbye;
	public final CharSequence[] sCurse;
	public final CharSequence[] sInsult;
	public final CharSequence[] sInsulting;
	public final CharSequence[] sLord;
	public final CharSequence[] sCity;
	public final CharSequence[] sOthers;
	public final CharSequence[] sSelves;
	public final CharSequence[] sSelf;

	RaceInfo(Json json, Json text){
		super(text);
		namePosessive = text.text("POSSESSIVE");
		namePosessives = text.text("POSSESSIVES");
		desc_long = text.text("DESC_LONG");
		
		initialChallenge = text.text("CHALLENGE", "");
		
		pros = text.textsTry("PROS");
		cons = text.textsTry("CONS");
		
		pHE = new RPronoun("PRONOUN_HE", text);
		pHIS = new RPronoun("PRONOUN_HIS", text);
		pHIMSELF = new RPronoun("PRONOUN_HIMSELF", text);
		pHIM = new RPronoun("PRONOUN_HIM", text);
		
		armyNames = new ArrayList<>(text.texts("ARMY_NAMES", 1, 255));
		
		String f = json.value("WORLD_NAME_FILE");
		if (!wi.containsKey(f)) {
			wi.put(f, new RaceWorldInfo(f));
		}
		winfo = wi.get(f);
		
		f = json.value("RAID_TEXT_FILE");
		if (!ram.containsKey(f))
			ram.put(f, new RaiderTextsRace(new Json(PATHS.RACE().text.getFolder("raider").getFolder("message").get(f))));
		raiderMess = ram.get(f);
		
		f = json.value("RAIDER_NAME_FILE");
		if (!ra.containsKey(f)) {
			ra.put(f, new Json(PATHS.RACE().text.getFolder("raider").getFolder("name").get(f)).texts("NAMES"));
		}
		
		
		raiderNames = ra.get(f);
		//sInsult = text.text("INSULT");
		
		sHello = text.texts("HELLO");;
		sGoodbye = text.texts("GOODBYE");;
		sCurse = text.texts("CURSE");;
		sInsult = text.texts("INSULT");;
		sInsulting = text.texts("INSULTING");;
		sLord = text.texts("LORD");;
		sCity = text.texts("CITY");;
		sOthers = text.texts("OTHERS");;
		sSelves = text.texts("SELVES");;
		sSelf = text.texts("SELF");;
	}

	public final class RaceWorldInfo {

		public final String[] intros;
		public final String[] fNames;
		public final String[] rIntro;
		public final String[] rNames;

		RaceWorldInfo(String key) {
			Json json = new Json(PATHS.NAMES().getFolder("world").get(key));
			intros = json.texts("INTRO", 1, 128);
			fNames = json.texts("NAMES", 1, 512);
			rIntro = json.texts("RULER_INTRO", 1, 128);
			rNames = json.texts("RULER", 1, 512);

		}

	}

	public final static class RPronoun {

		public final CharSequence[] pronouns;
		public final CharSequence[] pronounsC;

		RPronoun(String key, Json text) {
			pronouns = text.texts(key);
			pronounsC = text.texts(key + "C");
		}

		public CharSequence get(Induvidual i, boolean cap) {
			int k = STATS.APPEARANCE().gender.get(i);
			CharSequence[] ll = pronouns;
			if (cap)
				ll = pronounsC;
			k = CLAMP.i(k, 0, ll.length - 1);
			return ll[k];
		}

	}

}
