package settlement.stats.equip;

import init.resources.RESOURCE;
import settlement.stats.Induvidual;

public interface WearableResource {

	public RESOURCE resource(Induvidual i);
	public void wearOut(Induvidual i);
	public int max(Induvidual i);
	public int target(Induvidual i);
	public double wearPerYear(Induvidual i);
	public void set(Induvidual i, int am);
	public int get(Induvidual i);
	public int needed(Induvidual i);
	public default void inc(Induvidual i, int am) {
		set(i, get(i)+am);
	}
}
