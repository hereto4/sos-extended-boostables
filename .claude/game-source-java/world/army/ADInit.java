package world.army;

import java.util.LinkedList;

import game.faction.Faction;
import snake2d.util.misc.ACTION.ACTION_O;
import util.data.DataO;
import world.entity.army.WArmy;

public class ADInit {

	
	public final DataO<WArmy> dataA = new DataO<WArmy>("ADARMY") {
		@Override
		protected long[] data(WArmy t) {
			return t.divs().data;
		}		
	};
	public final  DataO<Faction> dataT = new DataO<Faction>("ADFACTION") {
		@Override
		protected long[] data(Faction t) {
			return AD.army(t).data;
		}
	};
	
	final LinkedList<Countable> countable = new LinkedList<>();
	final LinkedList<Register> registers = new LinkedList<>();
	final LinkedList<Updater> updaters = new LinkedList<>();
	final LinkedList<ACTION_O<Faction>> inits = new LinkedList<>();
	
	interface Countable {
		
		void count(WArmy a, int delta);
		
	}
	
	interface Register {
		void register(ADDiv div, int d);
	}
	
	interface Updater {
		
		void update(WArmy a, double timeSinceLast);
		void update(Faction f, double timeSinceLast);
		
	}
	
	ADInit(){
		
	}
	
}
