package view.sett.ui.room.copy;

import settlement.job.Job;
import settlement.main.SETT;

final class Jobs {

	static Job get(int tx, int ty) {
		{
			Job j = SETT.JOBS().jobGetter.get(tx, ty);
			if (j != null && j.isConstruction ())
				return j;
			return null;
		}
		
	}
	
	
}
