package game.event.actions;

import game.boosting.BSourceInfo;
import game.boosting.superb.SuperSpec;
import game.boosting.superb.SuperSpec.SuperSpecImp;
import game.event.engine.EChoice;
import game.event.engine.EContext;
import game.event.engine.Event;
import game.faction.FACTIONS;
import game.faction.npc.FactionNPC;
import game.faction.royalty.Royalty;
import game.faction.royalty.opinion.ROPINIONS;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.file.Json;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.LISTE;
import util.gui.misc.GBox;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.info.GFORMAT;
import util.text.D;

public class _OPINION extends EventActionConstructor{

	private static CharSequence ¤¤sTitle = "Affected Royalties";
	
	static {
		D.ts(_OPINION.class);
	}
	
	_OPINION() {
		super("OPINION");
	}
	
	@Override
	public EventAction action(Data data) {
		return new Imp(key, data.parent, data.choice, data.json, data.all);
	}
	
	public final class Imp extends EventAction  {

		private final SuperSpecImp<Royalty> spec;
		
		
		Imp(String key, Event parent, EChoice choice, Json data, LISTE<EventAction> all) {
			super(key, all);
			
			BSourceInfo info = new BSourceInfo(parent.info.name, parent.info.icon);
			String desc = "" + info.name;
			if (choice != null)
				desc += " - " + choice.name + ". ";
			
			double time = data.dTry("LENGHT_DAYS", 0, 1000, -1);
			double value = data.d("VALUE", -1000, 1000);
			boolean isMul = data.bool("IS_MUL", false);
			double increase = data.dTry("INCREASE_PER_DAY", -100, 100, 0);
			String k = "EVENT_" + key;
			if (increase < 0) {
				spec = new SuperSpec.Downer<Royalty>(-increase, ROPINIONS.BOOST(), k, info, desc, value, isMul, time);
			}else if (increase > 0) {
				spec = new SuperSpec.Uper<Royalty>(increase, time, ROPINIONS.BOOST(), k, info, desc, value, isMul);
			}else if (time > 0){
				spec = new SuperSpec.TimeLimit<Royalty>(time, ROPINIONS.BOOST(), k, info, desc, value, isMul);
			}else {
				spec = new SuperSpec.Permanent<Royalty>(ROPINIONS.BOOST(), k, info, desc, value, isMul);
			}
			spec.hidden = true;
			data.checkUnused();
		}

		@Override
		public void exe(Event e, EContext data) {
		
			for (FactionNPC f : FACTIONS.NPCs()) {
				for (Royalty r : f.court().all()) {
					if (r.event()) {
						spec.activate(r, true);
					}
				}
			}
			super.exe(e, data);
		}
		
		
		@Override
		public void hover(GBox b, Event event, EContext context) {
			
		}
		
		@Override
		public void addToMessageBody(LISTE<RENDEROBJ> rows, Event event, EContext context, RECTANGLE messBody) {
			
			int am = context.royalty.am;
			
			rows.add(new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.i(text, am);
				}
			}.hh(¤¤sTitle));
			
			
			
			rows.add(new GStat() {
				
				@Override
				public void update(GText text) {
					if (spec.isMul) {
						text.add('*').s();
						GFORMAT.f1(text, spec.to());
					}else {
						GFORMAT.f0(text, spec.to());
					}
				}
			}.hh(ROPINIONS.BOOST().bo.name));
			
		}
	}
	
}
