package settlement.misc.job;

import game.audio.SoundRace;
import init.resources.RBIT;
import init.resources.RESOURCE;
import settlement.entity.humanoid.Humanoid;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;

public interface SETT_JOB {
	
	
	public abstract void jobReserve(RESOURCE r);
	public abstract boolean jobReservedIs(RESOURCE r);
	public abstract void jobReserveCancel(RESOURCE r);
	public abstract boolean jobReserveCanBe();

	
	
	/**
	 * 
	 * @return 0 if no resource, else a bitmask of resources
	 */
	public RBIT jobResourceBitToFetch();
	public default int jobResourcesNeeded(Humanoid skill) {
		return 4;
	}
	public double jobPerformTime(Humanoid a);
	public void jobStartPerforming();
	public RESOURCE jobPerform(Humanoid skill, RESOURCE r, int rAm);
	
	
	public COORDINATE jobCoo();
	public default DIR jobStandDir() {
		return null;
	}
	
	public CharSequence jobName();

	public boolean jobUseTool();
	public default boolean jobUseHands() {
		return true;
	}
	public SoundRace jobSound();

	public default boolean longFetch() {
		return false;
	}
	
	
}
