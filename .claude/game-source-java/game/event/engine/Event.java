package game.event.engine;

import java.io.IOException;

import game.GameDisposable;
import game.event.actions.EventAction;
import game.event.actions.EventActions;
import snake2d.util.file.Json;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LISTE;
import util.text.Dic;

public final class Event {

	static final ArrayListGrower<Event> all = new ArrayListGrower<>();
	static {
		new GameDisposable() {
			
			@Override
			protected void dispose() {
				all.clear();
			}
		};
	}
	
	int savedIndex;
	public final int allIndex;
	public final String key;
	
	public ETags tags;
	public EOccurence occurence;
	public EInfo info;
	public EDuration duration;
	
	public ESelection selection;
	public ECondition condition;
	public LIST<EChoice> choices;
	public LIST<EventAction> on_spawn;
	public LIST<ECondition> aborters;
	
	final ArrayListGrower<EventAction> allActions = new ArrayListGrower<>();
	
	Event(LISTE<Event> coll, String key, Json data, Json text) throws IOException {
		this.key = key;
		allIndex = all.add(this);
		if (allIndex >= Short.MAX_VALUE)
			throw new RuntimeException("Too many events!");
		savedIndex = allIndex;
		info = new EInfo(data, text);
		
		coll.add(this);
	}
	
	void read(Json data, Json text, EventActions actions, EventCollection engine) {
		
		on_spawn = EActions.actions("ON_SPAWN", this, null, actions, data, false);
		occurence = new EOccurence(data, engine, this);
		duration = new EDuration(data, actions, this);
		data.has("ICON");
		
		tags = new ETags(data);
		
		if (data.has("CHOICES")) {
			Json[] js = data.jsons("CHOICES");

			CharSequence[] names;
			if (js.length == 0)
				names = new CharSequence[0];
			else if (text == null || !text.has("CHOICES")) {
				if (js.length <= 2) {
					names = new CharSequence[] {
						Dic.¤¤Accept,
						Dic.¤¤Decline
					};
				}else {
					names = new CharSequence[0];
				}
			}else {
				names = text.texts("CHOICES");
			}
			
			ArrayList<EChoice> cs = new ArrayList<EChoice>(js.length);
			
			for (int i = 0; i < js.length; i++) {
				cs.add(new EChoice(this, i, actions, js[i], i < names.length ? names[i] : (""+i)));
			}
			choices = cs;
		}else {
			choices = new ArrayList<EChoice>(0);
		}
		if (data.has("CONDITION")) {
			
			condition = new ECondition("CONDITION", data, actions, this);
		}
		if (data.has("ABORTS")) {
			Json[] jj = data.jsons("ABORTS", 0);
			ArrayList<ECondition> aborters = new ArrayList<ECondition>(jj.length);
			for (Json j : jj) {
				aborters.add(new ECondition(null, j, actions, this));
			}
			this.aborters = aborters;
		}else {
			aborters = new ArrayList<ECondition>(0);
		}
		
		
		selection = new ESelection(this, actions, data);
		data.checkUnused();
	}
	
//	@Override
//	public int index() {
//		return index;
//	}
//
//	@Override
//	public String key() {
//		return key;
//	}
	
	public LIST<EventAction> actions(){
		return allActions;
	}
	
	
}
