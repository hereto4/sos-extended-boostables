package init.resources;

import java.util.Arrays;

import init.resources.RBIT.RBITImp;
import snake2d.Errors;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import util.keymap.RMAPS;

public class ResGroup<T extends ResG> {

	public final RBIT mask;
	private final LIST<T> all;
	private final LIST<RESOURCE> resources;
	private int[] indexMap;
	public final String key;
	public final RMAPS<T> MAP;
	
	ResGroup(String key, LIST<T> resses) {
		this.key = key;
		all = new ArrayList<T>(resses);
		if (all.size() == 0)
			throw new Errors.DataError("not enough " + key + " resources have been declared");
		ArrayList<RESOURCE> ress = new ArrayList<>(resses.size());
		indexMap = new int[RESOURCES.ALL().size()];
		Arrays.fill(indexMap, -1);
		RBITImp m = new RBITImp();
		for (T  r : resses) {
			if (m.has(r.resource.bit))
				throw new Errors.DataError("Several " + key + " is mapping to the same resource, and that doesn't work");
			ress.add(r.resource);
			m.or(r.resource.bit);
			indexMap[r.resource.index()] = r.index();
		}
		this.resources = ress;
		mask = m;		
		MAP = new RMAPS<T>(key, resses);
	}
	
	public LIST<RESOURCE> res(){
		return resources;
	}
	
	public boolean is(RESOURCE res) {
		return mask.has(res); 
	}
	
	public LIST<T> all(){
		return all;
	}
	
	public T get(RESOURCE res) {
		if (mask.has(res.bit)) {
			return all.get(indexMap[res.bIndex()]);
		}
		return null;
	}
	
	public RESOURCE[] makeArray() {
		RESOURCE[] rr = new RESOURCE[all.size()];
		for (ResG e : all())
			rr[e.index()] = e.resource;
		return rr;
	}
	

	
}
