package settlement.stats.stat;

import game.boosting.BoostSpecs;
import init.race.Race;
import init.sprite.UI.UI;
import init.type.HCLASS;
import init.type.POP_CL;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import settlement.stats.StatsInit;
import settlement.stats.standing.StatStanding;
import settlement.stats.util.StatHoverer;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.sets.INDEXED;
import util.data.INT_O.INT_OE;
import util.gui.misc.GBox;

public abstract class STAT implements INDEXED, SETT_STATISTICS {

	private StatDecree decree;
	private final int index;
	protected final String key;
	protected final StatInfo info;
	public StatStanding standing;
	public final BoostSpecs boosters;
	
	protected STAT(String key, StatsInit init, StatInfo info) {
		index = init.stats.add(this);
		init.coll.all.add(this);
		if (key != null) {
			key = init.coll.key + "_" + key;
			key = key.replace("__", "_");
		}
		this.key = key;
		if (info == null && key != null)
			info = new StatInfo(init.dText.json(key));
		if (key != null)
			init.statMap.put(key, this);
		if (info != null)
			this.info = new StatInfo(info);
		else {
			this.info = new StatInfo("no use","no use");
			this.info.setMatters(false, false);
		}
		boosters = new BoostSpecs(info == null ? "" : info.name, UI.icons().s.human, true);
		
	}
	
	abstract public INT_OE<Induvidual> indu();

	@Override
	public final StatInfo info() {
		return info;
	}

	public final StatStanding standing() {
		return standing;
	}

	public final String key() {
		return key;
	}

	public int pdivider(HCLASS c, Race r, int daysback) {
		return STATS.POP().POP.data(c).get(r, daysback);
	}

	public void addDecree(StatDecree d) {
		this.decree = d;
	}

	public StatDecree decree() {
		return decree;
	}

	public boolean hasIndu() {
		return false;
	}
	
	@Override
	public final int index() {
		return index;
	}
	

	
	public void hover(GUI_BOX text, HCLASS cl, Race type) {
		StatHoverer.hover(text, this);
		GBox b = (GBox) text;
		b.sep();
		StatHoverer.hover(text, this, cl, type);
		b.NL();
		if (boosters.all().size() > 0) {
			boosters.hover(text, POP_CL.clP(type, cl));
		}
		
	}
	
	public void hover(GUI_BOX text, Induvidual indu) {
		StatHoverer.hover(text, this);
		GBox b = (GBox) text;
		b.sep();
		StatHoverer.hover(text, this, indu);
		b.NL();
		if (boosters.all().size() > 0) {
			b.NL(8);
			boosters.hover(text, indu);
		}
	}

}
