package init.type;

import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LIST;
import util.keymap.RMAPS;
import util.text.D;

public final class CAUSE_ARRIVES {

	private final ArrayListGrower<CAUSE_ARRIVE> all = new ArrayListGrower<>();
	
	{
		D.gInit(this);
	}
	
	private final CAUSE_ARRIVE BORN = new CAUSE_ARRIVE(all, 
			"BORN",
			D.g("Born"),
			D.g("BornD", "Subjects that have been born in your city."), false);
	private final CAUSE_ARRIVE IMMIGRATED = new CAUSE_ARRIVE(all, 
			"IMMIGRATED",
			D.g("Immigrated"),
			D.g("ImmigratedD", "Subjects that have immigrated to your city."), true);
	private final CAUSE_ARRIVE EMANCIPATED = new CAUSE_ARRIVE(all, 
			"EMANCIPATED",
			D.g("Emancipated"),
			D.g("EmancipatedD", "Subjects that are freed slaves."), false);
	private final CAUSE_ARRIVE PAROLE = new CAUSE_ARRIVE(all, 
			"PAROLE",
			D.g("Parole"),
			D.g("ParoleD", "Subjects that have been prisoners and are now pardoned and free citizens."), false);
	private final CAUSE_ARRIVE SOLDIER_RETURN = new CAUSE_ARRIVE(all, 
			"SOLDIER",
			D.g("Soldiers"),
			D.g("SoldiersD", "Soldiers that have returned from campaigning."), true);
	private final CAUSE_ARRIVE CURED = new CAUSE_ARRIVE(all, 
			"CURED",
			D.g("Readjusted"),
			D.g("ReadjustedD", "Subjects that have been readjusted in the asylum and cured of insanity."), false);
	
	private final RMAPS<CAUSE_ARRIVE> map = new RMAPS<>("CAUSE_ARRIVE", all);
	
	private static CAUSE_ARRIVES self;
	
	
	
	public static LIST<CAUSE_ARRIVE> ALL(){
		return self.all;
	}
	public static CAUSE_ARRIVE BORN() {
		return self.BORN;
	}
	public static CAUSE_ARRIVE IMMIGRATED() {
		return self.IMMIGRATED;
	}
	public static CAUSE_ARRIVE EMANCIPATED() {
		return self.EMANCIPATED;
	}
	public static CAUSE_ARRIVE PAROLE() {
		return self.PAROLE;
	}
	public static CAUSE_ARRIVE SOLDIER_RETURN() {
		return self.SOLDIER_RETURN;
	}
	public static CAUSE_ARRIVE CURED() {
		return self.CURED;
	}
	public static RMAPS<CAUSE_ARRIVE> MAP(){
		return self.map;
	}
	
	
	CAUSE_ARRIVES(){
		self = this;
	}

}
