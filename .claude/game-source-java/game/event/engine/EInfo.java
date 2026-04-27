package game.event.engine;

import java.io.IOException;

import init.sprite.SPRITES;
import init.sprite.UI.UI;
import snake2d.util.file.Json;
import snake2d.util.sprite.SPRITE;

public final class EInfo {
	
	public CharSequence name = "";
	public CharSequence[] messages = new CharSequence[0];
	public CharSequence desc = "";
	public CharSequence subject = "";
	public SPRITE icon = UI.icons().l.event;
	public final boolean showRemaining;
	
	EInfo()  {
		showRemaining = true;
	}
	
	public EInfo(Json data, Json text) throws IOException {

		if (text != null) {
			name = text.text("NAME", "");
			desc = text.text("DESC", "");
			messages = text.textsTry("MESSAGE");
			subject = text.text("SUBJECT", "");
			text.has("CHOICES");
			text.checkUnused();
		}
		
		

		if (data.has("ICON"))
			icon = SPRITES.icons().get(data);
		showRemaining = data.bool("SHOW_TIME", true);
	
		EContext.insert.check(desc);
		EContext.insert.check(messages);
		
		
		
	}
	
}
