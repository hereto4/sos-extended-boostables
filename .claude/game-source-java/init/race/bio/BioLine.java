package init.race.bio;

import game.faction.FACTIONS;
import game.faction.Faction;
import init.type.HTYPES;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.stats.STATS;
import snake2d.util.MATH;
import snake2d.util.file.Json;
import snake2d.util.sets.LISTE;
import snake2d.util.sprite.text.Str;
import util.data.GETTER_TRANS;
import util.text.INSERT;
import util.text.Inserter;

public class BioLine {

	CharSequence[] strings;
	protected boolean nl = false;
	protected final int index;
	private static Str str = new Str(256);
	final String key;
	public static final Inserter<Humanoid> insert = new Inserter<>(INSERT.human, "");
	static {
		insert.join(new Inserter<Humanoid>(INSERT.human, "FRIEND_"), new GETTER_TRANS<Humanoid, Humanoid>(){

			@Override
			public Humanoid get(Humanoid a) {
				ENTITY b = STATS.POP().FRIEND.get(a.indu());
				if (b instanceof Humanoid)
					return (Humanoid) b;
				
				int ri = STATS.RAN().get(a.indu(), 1);
				ENTITY[] es = SETT.ENTITIES().getAllEnts();
				
				if (es.length == 0)
					return a;
				
				for (int i = 0; i < es.length; i++) {
					ENTITY e = es[MATH.mod(i+ri, es.length)];
					if (e != a && e instanceof Humanoid) {
						return (Humanoid) e;
					}
				}
				
				return a;
			}
			
		});
		insert.join(INSERT.faction, new GETTER_TRANS<Humanoid, Faction>() {

			@Override
			public Faction get(Humanoid f) {
				return FACTIONS.player();
			}
		
		});
		insert.join(INSERT.player, new GETTER_TRANS<Humanoid, Integer>() {

			@Override
			public Integer get(Humanoid f) {
				return STATS.RAN().get(f.indu(), 21, 10);
			}
		
		});
	}
	
	BioLine(LISTE<BioLine> all, Json json, String key){
		if (json.has(key))
			strings = strings(json, key);
		else
			strings = new CharSequence[0];
		index = all.add(this);
		this.key = key;
	}
	
	BioLine(LISTE<BioLine> all, Json json, String key, CharSequence[] backup){
		if (json.has(key))
			strings = strings(json, key);
		else
			strings = backup;
		index = all.add(this);
		this.key = key;
	}

	protected CharSequence[] strings(Json json, String key) {
		CharSequence[] ll = json.texts(key);
		insert.check(ll);
		
		return ll;
	}

	protected boolean use(Humanoid a) {
		if (!a.indu().hType().player || a.indu().hType() == HTYPES.CHILD())
			return false;
		return true;
	}
	
	protected BioLine nlSet() {
		nl = true;
		return this;
	}
	
	public final CharSequence get(Humanoid a) {
		if (strings.length == 0)
			return null;
		if (!use(a))
			return null;
		int ran = STATS.RAN().get(a.indu(), 9 + index*5, 5);
		CharSequence s = strings[MATH.mod((int)ran, strings.length)];
		str.clear().add(s);
		
		Inserter.setRandom(STATS.RAN().getL(a.indu(), 0));
		insert.set(str, a);
		
		return str;
	}
	
	public boolean nl() {
		return nl;
	}
	
}