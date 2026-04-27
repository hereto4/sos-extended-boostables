package game.boosting.tmp;

import game.boosting.BSourceInfo;
import game.boosting.BoostSpecs;
import snake2d.util.sprite.SPRITE;

public class TmpBoostSpec {
	
	public final CharSequence name;
	public final CharSequence desc;
	public final SPRITE icon;
	public final int index;
	public BoostSpecs spec;
	public final String key;
	
	public TmpBoostSpec(String key, CharSequence name, CharSequence desc, SPRITE icon){
		index = TmpBoosting.allTmp.add(this);
		int i = 1;
		String k = key;
		while(TmpBoosting.allMap.containsKey(k)) {
			k = key + i;
			i++;
		}
		
		TmpBoosting.allMap.put(k, this);
		this.key = key;
		this.name = name;
		this.desc = desc;
		this.icon = icon;
		spec = new BoostSpecs(new BSourceInfo(name, icon), false);
		
	}
	

}
