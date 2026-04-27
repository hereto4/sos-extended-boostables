package settlement.maintenance;

import init.resources.RESOURCE;

abstract class MType {

	public abstract boolean validate(int tx, int ty);
	
	public abstract boolean degrade(int tx, int ty, int tile, double rate);
	public abstract void maintain(int tx, int ty);
	public abstract void vandalize(int tx, int ty);
	public abstract boolean shouldPlace(int tx, int ty, boolean was);
	public abstract int shouldPlaceResource(int tx, int ty);
	

	
	public abstract RESOURCE res(int tx, int ty, int ri);
	public abstract double resRate(int tx, int ty, int ri);
	
	public abstract double degrade(int tx, int ty);
	
}
