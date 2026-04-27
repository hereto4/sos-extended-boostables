package game.events.killer;

import snake2d.util.file.Json;

class KillerType {
	
	public final CharSequence name;
	public final CharSequence method;
	public final CharSequence[] messages;
	
	KillerType(Json json){
		name = json.text("NAME");
		method = json.text("METHOD");
		messages = json.texts("MESSAGES", 2, 100);
	}
	
}