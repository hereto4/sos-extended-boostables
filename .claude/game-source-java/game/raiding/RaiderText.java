package game.raiding;

import java.io.Serializable;

import game.faction.FACTIONS;
import game.faction.Faction;
import settlement.stats.Induvidual;
import snake2d.util.rnd.RND;
import snake2d.util.sets.LinkedList;
import snake2d.util.sprite.text.Str;
import util.data.GETTER_TRANS;
import util.text.INSERT;
import util.text.Inserter;

public final class RaiderText implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public LinkedList<String> demandBody = new LinkedList<>();
	public LinkedList<String> rejected = new LinkedList<>();
	public LinkedList<String> payed = new LinkedList<>();
	public LinkedList<String> afterRaid = new LinkedList<>();
	
	public static final Inserter<Raider> insert = new Inserter<>();
	static {
		insert.new II("RAIDER_NAME") {
			
			@Override
			public void set(Raider t, Str str) {
				str.add(t.name);
			}
		};
		insert.join(INSERT.indu, new GETTER_TRANS<Raider, Induvidual>() {

			@Override
			public Induvidual get(Raider f) {
				return f.indu;
			}
			
		});
		insert.join(INSERT.faction, new GETTER_TRANS<Raider, Faction>() {

			@Override
			public Faction get(Raider f) {
				return FACTIONS.player();
			}
			
		});
		insert.join(INSERT.player, new GETTER_TRANS<Raider, Integer>() {

			@Override
			public Integer get(Raider f) {
				return RND.rInt();
			}
			
		});
		
	}
	
	public RaiderText(){
		
	}
	
	public void set(Raider raider, boolean first) {
		RaiderTextsRace tt = raider.indu.race().info.raiderMess;
		if (first)
			insert(demandBody, raider, tt.greetings.rnd(), tt.mids.rnd(), tt.bodies.rnd(), tt.ends.rnd());
		else
			insert(demandBody, raider, tt.rgreetings.rnd(), tt.rmids.rnd(), tt.rbodies.rnd(), tt.rends.rnd());
		insert(payed, raider, tt.payed.rnd());
		insert(rejected, raider, tt.rejected.rnd());
		insert(afterRaid, raider, tt.afterRaid.rnd());
	}
	
	public void insert(LinkedList<String> res, Raider raider, CharSequence... sources) {
		res.clear();
		for (CharSequence s : sources) {
			Str.TMP.clear();
			Str.TMP.add(s);
			
			
			insert.set(Str.TMP, raider);
			res.add(""+Str.TMP);
		}
		
	}

	
}
