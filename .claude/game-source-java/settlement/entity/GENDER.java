package settlement.entity;

import snake2d.util.rnd.RND;

public enum GENDER {

	MALE("male", 1f),FEMALE("female", 0.6f);
	
	public final String name;
	public final float weightReduction;
	
	private GENDER(String name, float weightRed){
		this.name = name;
		this.weightReduction = weightRed;
	}
	
	public static GENDER getRND(){
		return values()[RND.rInt(2)];
	}
	
}
