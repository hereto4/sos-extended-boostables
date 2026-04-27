package init.type;

import game.battle.div.Div;
import game.boosting.BoostSpecs;
import game.faction.npc.FactionNPC;
import init.race.RACES;
import init.race.Race;
import init.race.bio.BioLine;
import init.sprite.UI.UI;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import settlement.stats.colls.StatsTraits.StatTrait;
import settlement.stats.util.StatBooster;
import snake2d.util.file.Json;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LISTE;
import util.info.INFO;
import util.keymap.MAPPED;
import util.text.D;

public final class TRAIT implements MAPPED{

	private static CharSequence ¤¤name = "Trait";
	
	static {
		D.ts(TRAIT.class);
	}
	
	private final String key;
	private final int index;
	public final INFO info;
	public final CharSequence rTitle;
	public final BoostSpecs boosters;
	public final CharSequence[] bios;
	final double[] occRaces = new double[RACES.all().size()];
	
	final ArrayListGrower<TRAIT> disables = new ArrayListGrower<>();
	
	TRAIT(LISTE<TRAIT> all, String key, Json data, Json jtext){
		this.key = key;
		this.index = all.add(this);
		info = new INFO(jtext);
		rTitle = jtext.text("TITLE");
		bios = BioLine.insert.check(jtext.texts("BIO_DESC"));
		RACES.map().readFill("DEFAULT_RACE_OCCURANCE", occRaces, data, 0, 1);
		boosters = new BoostSpecs(¤¤name + ": " + info.name, UI.icons().s.alert, true);
		
		boosters.read(data, new StatBooster() {

			@Override
			public double vGet(Induvidual indu) {
				return stat().getD(indu);
			}
			
			@Override
			public double vGet(Div div) {
				return stat().getD(div);
			}

			@Override
			public double vGet(FactionNPC f) {
				return vGet(f.court().king().roy().induvidual);
			}
			@Override
			public double vGet(PopTime popTime) {
				return stat().getD(popTime.pop.cl, popTime.pop.race);
			}
			
		});
		
	}
	
	@Override
	public int index() {
		return index;
	}

	@Override
	public String key() {
		return key;
	}
	
	public LIST<TRAIT> disables(){
		return disables;
	}
	
	public double get(Induvidual in) {
		return STATS.TRAITS().stat(this).getD(in);
	}
	
	public double occurance(Race race) {
		return occRaces[race.index];
	}
	
	public StatTrait stat() {
		return STATS.TRAITS().stat(this);
	}

	
}
