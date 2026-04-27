package game.event.actions;

import game.event.actions.EventAction.CInt;
import game.event.engine.EContext;
import game.event.engine.Event;
import init.race.RACES;
import init.race.Race;
import init.type.HTYPE;
import init.type.HTYPES;
import settlement.main.SETT;
import settlement.stats.STATS;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.file.Json;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LISTE;
import util.gui.misc.GBox;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.table.GRows;
import util.info.GFORMAT;

final class _SUBJECTS_ADD extends EventActionConstructor{
	
	_SUBJECTS_ADD() {
		super("SUBJECTS_ADD");
	}
	
	@Override
	public EventAction action(Data data) {
		return new Imp(key, data.json, data.all);
	}
	
	public final class Imp extends EventAction  {

		private final HTYPE immType; 
		private final ArrayListGrower<RAmount> datas = new ArrayListGrower<>();
		
		
		Imp(String key, Json data, LISTE<EventAction> all) {
			super(key, all);
			immType = HTYPES.MAP().readTry("IMMIGRANT_TYPE", data);
			RACES.map().new KJson("AMOUNTS", data) {
				
				@Override
				protected void process(Race s, Json j, String key, boolean isWeak) {
					RAmount d = new RAmount(s, new CInt(s.key + "_AMOUNT"));
					d.read(j.json(key), 0);
					datas.add(d);
				}
			};
			
			
			data.checkUnused();
		}

		@Override
		public void setContext(Event event, EContext data) {
			for (RAmount d : datas) {
				d.set(event, data, STATS.POP().pop(d.t, immType));
			}
		}
		

		@Override
		public void exe(Event event, EContext data) {

			for (RAmount d : datas) {
				int am = d.amount.get(event, data);
				if (am > 0)
					SETT.ENTRY().add(d.t, immType, am);
			}
		}
		
		@Override
		public void addToMessageBody(LISTE<RENDEROBJ> rows, Event event, EContext data, RECTANGLE messBody) {
			
			GRows rr = new GRows(6).setMin(100);
			for (RAmount d : datas) {
				
				rr.add(new GStat() {
					@Override
					public void update(GText text) {
						GFORMAT.i(text, d.amount.get(event, data));
					}
					
					@Override
					public void hoverInfoGet(GBox b) {
						
						b.title(b.text().add(d.t.info.names).s().add('(').add(immType.names).add(')'));
						b.add(GFORMAT.iIncr(b.text(), d.amount.get(event, data)));
						b.NL();
						
					};
					
				}.hh(d.t.appearance().icon));
			}
			rows.add(rr.rows());
		}
		
		@Override
		public void hover(GBox b, Event event, EContext context) {
			
			int t = 0;
			for (RAmount d : datas) {
				
				if (t > 5) {
					t = 0;
					b.NL();
				}
				b.tab(t*3);
				t++;
				b.add(d.t.appearance().icon);
				b.add(GFORMAT.iIncr(b.text(), d.amount.get(event, context)));
			}
		}
		
		@Override
		public CharSequence problem(Event event, EContext context) {
			return null;
		}
		
	}
	
	
	private static class RAmount extends Amount{

		public final Race t;
		
		RAmount(Race res, CInt amount) {
			super(amount);
			this.t = res;
		}
		
		
	}
	
}
