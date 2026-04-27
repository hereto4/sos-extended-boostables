package util.statistics;

import util.data.INT_O;

public interface HISTORY_COLLECTION<T> extends INT_O<T>{
	
	public HISTORY_INT history(T r);
	public HISTORY_INT total();
	
}

