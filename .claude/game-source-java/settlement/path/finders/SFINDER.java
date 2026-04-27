package settlement.path.finders;

import settlement.path.components.finder.SCompFinder.SCompPatherFinder;

public interface SFINDER extends SCompPatherFinder{

	boolean isTile(int tx, int ty, int tileNr);
	
}
