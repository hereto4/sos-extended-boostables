package init.value;

import init.sprite.UI.Icon;
import snake2d.util.sprite.SPRITE;
import util.data.DOUBLE_O;

public class Value<T>{

	public final SPRITE icon;
	public final String key;
	public final CharSequence name;
	public final DOUBLE_O<T> d;
	public final boolean percentage;
	public final boolean isBool;
	
	Value(String key, SPRITE icon, CharSequence name, DOUBLE_O<T> d, boolean percentage, boolean isBool){
		this.icon = icon.resized(Icon.S);
		this.name = name;
		this.d = d;
		this.percentage = percentage;
		this.isBool = isBool;
		this.key = key;
	}
	
}
