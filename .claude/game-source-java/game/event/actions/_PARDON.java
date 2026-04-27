package game.event.actions;

import game.GAME;
import game.event.engine.EContext;
import game.event.engine.Event;
import init.sprite.UI.UI;
import init.type.HTYPES;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.types.prisoner.AIModule_Prisoner;
import settlement.main.SETT;
import settlement.stats.STATS;
import settlement.stats.law.LAW;
import snake2d.util.MATH;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.file.Json;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.LISTE;
import util.gui.misc.GBox;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.info.GFORMAT;

final class _PARDON extends EventActionConstructor{
	
	_PARDON() {
		super("PARDON");
	}
	
	@Override
	public EventAction action(Data data) {
		return new Imp(key, data.json, data.all);
	}
	
	public final class Imp extends EventAction  {

		private final boolean useSelection;
		private final int amount;
		
		
		Imp(String key, Json data, LISTE<EventAction> all) {
			super(key, all);
			amount = data.i("PRISONER_TARGET", 1, 10000);
			useSelection = data.bool("USE_SELECTION", false);
			data.checkUnused();
		}

		@Override
		public void exe(Event event, EContext data) {

			ENTITY[] es = SETT.ENTITIES().getAllEnts();
			int ri = GAME.updateI();
			int am = 0;
			for (int i = 0; i < es.length; i++) {
				int ei = i+ri;
				ei = MATH.mod(ei, es.length);
				
				ENTITY e = es[ei];
				if (e instanceof Humanoid) {
					Humanoid a = (Humanoid) e;
					if (useSelection && !STATS.EVENT().has(a.indu()))
						continue;
					if (a.indu().hType() == HTYPES.PRISONER() && AIModule_Prisoner.DATA().punishmentSet.get(a.ai()) != LAW.process().pardoned) {
						AIModule_Prisoner.DATA().punishmentSet.set(a.ai(), LAW.process().pardoned);
						am++;
					}
					if (am >= amount || (useSelection && am >= data.indu.am))
						break;
				}
			}
			
		}
		
		@Override
		public void addToMessageBody(LISTE<RENDEROBJ> rows, Event event, EContext data, RECTANGLE messBody) {
			
			rows.add(new GStat() {
				
				@Override
				public void update(GText text) {
					int am = 0;
					if (useSelection) {
						am = STATS.EVENT().stat().data().get(null);
					}else {
						am = amount;
					}
					GFORMAT.i(text, -am);
				}
			}.hh(UI.icons().s.slave));
		}
		
		@Override
		public void hover(GBox b, Event event, EContext context) {
			
			int am = 0;
			if (useSelection) {
				am = STATS.EVENT().stat().data().get(null);
			}else {
				am = amount;
			}
			b.add(UI.icons().s.slave);
			b.add(GFORMAT.i(b.text(), -am));
			b.NL();
			
		}
		
		@Override
		public CharSequence problem(Event event, EContext context) {
			return null;
		}
		
	}

	
}
