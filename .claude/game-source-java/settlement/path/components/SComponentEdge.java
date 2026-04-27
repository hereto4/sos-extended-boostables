package settlement.path.components;

import snake2d.util.sets.ArrayList;

public final class SComponentEdge {

	private SComponentEdge next;
	private SComponent to;
	private float cost;
	private float distance;
	private static ArrayList<SComponentEdge> chache = new ArrayList<>(2048);
	private static int count = 0;
	
	private SComponentEdge(){
		
	}

	
	public SComponent to(){
		return to;
	}
	
	public double cost2(){
		return cost;
	}
	
	public double distance(){
		return distance;
	}
	
	void retire() {
		next = null;
		to = null;
		count--;
		if (chache.hasRoom())
			chache.add(this);
	}
	
	static SComponentEdge create(SComponent to, double cost, double distance, SComponentEdge next) {
		count++;
		SComponentEdge e = null;
		if (!chache.isEmpty())
			e = chache.removeLast();
		else
			e = new SComponentEdge();
		e.to = to;
		e.cost = (float) cost;
		e.distance = (float) distance;
		e.next = next;
		return e;
	}
	
	static int count() {
		return count;
	}

	void setNext(SComponentEdge e) {
		this.next = e;
	}

	public SComponentEdge next() {
		return next;
	}
	
}