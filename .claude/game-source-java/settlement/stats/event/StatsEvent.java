package settlement.stats.event;


import game.GAME;
import settlement.stats.Induvidual;
import settlement.stats.StatsInit;
import settlement.stats.StatsInit.StatInitable;
import settlement.stats.StatsInit.StatUpdatable;
import settlement.stats.stat.STAT;
import settlement.stats.stat.STATData;
import settlement.stats.stat.StatCollection;
import util.data.INT_O.INT_OE;

public class StatsEvent extends StatCollection{
	
	private final STATData stat;
	boolean hasChange = true;
	public INT_OE<Induvidual> mark;
	
	
	public StatsEvent(StatsInit init) {
		super(init, "EVENT", "", "");
		
		stat = new STATData("EVENT", "EVENTD", init, init.count.new DataBit("EVENT_STATUS"));
		mark = init.count.new DataShort("EVENT_MARK");
		
		init.onArrival.add(new StatInitable() {
			
			@Override
			public void init(Induvidual h) {
				if (GAME.EVENT().shouldSet(h)) {
					stat.indu().set(h, 1);
				}
			}
		});
		
		init.upers.add(new StatUpdatable() {
			
			@Override
			public void update(double ds) {
				if (hasChange) {
					
				}
				
			}
		});

	}
	
	public STAT stat() {
		return stat;
	}
	
	public boolean has(Induvidual t) {
		return stat.indu().get(t) == 1;
	}

	public void set(Induvidual t, boolean has) {
		stat.indu().set(t, has ? 1 : 0);
	}

	
}
