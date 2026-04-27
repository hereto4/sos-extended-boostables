package settlement.stats.disease;


import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;

public enum DiseaseStatus {
	NONE(false),
	INCUBATING(false),
	ISICK(true),
	IIMMUNE(false);
	
	public final boolean active;
	
	private DiseaseStatus(boolean active) {
		this.active = active;
	}
	
	public static final LIST<DiseaseStatus> ALL = new ArrayList<DiseaseStatus>(values());
}
