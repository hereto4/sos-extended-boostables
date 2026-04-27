package game.event.actions;

import game.event.actions.EventAction.CInt;
import game.event.engine.EContext;
import game.event.engine.Event;
import game.faction.FResources.RTYPE;
import game.faction.trade.ITYPE;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import settlement.main.SETT;
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
import util.text.D;
import util.text.Dic;

final class _RESOURCES extends EventActionConstructor{

	private static CharSequence ¤¤noEnough = "¤You don't have enough resources available.";
	
	static {
		D.ts(_RESOURCES.class);
	}
	
	_RESOURCES() {
		super("RESOURCES");
	}
	
	@Override
	public EventAction action(Data data) {
		return new Imp(key, data.json, data.all);
	}
	
	public final class Imp extends EventAction  {

		private final ArrayListGrower<RAmount> datas = new ArrayListGrower<>();
		
		Imp(String key, Json data, LISTE<EventAction> all) {
			super(key, all);
			RESOURCES.map().new KJson("AMOUNTS", data) {


				@Override
				protected void process(RESOURCE s, Json j, String key, boolean isWeak) {
					RAmount d = new RAmount(s, new CInt(s.key));
					d.read(j.json(key), Integer.MIN_VALUE);
					datas.add(d);
				}
			};
			
			
			data.checkUnused();
		}

		@Override
		public void setContext(Event event, EContext data) {
			for (RAmount d : datas) {
				d.set(event, data, SETT.ROOMS().STOCKPILE.tally().amountReservable.get(d.t));
			}
		}
		

		@Override
		public void exe(Event event, EContext data) {

			for (RAmount d : datas) {
				int am = d.amount.get(event, data);
				if (am < 0) {
					d.t.remove(-am, RTYPE.DIPLOMACY);
				}else if (am > 0) {
					SETT.ROOMS().IMPORT.tally.deliver(d.t, am, ITYPE.diplomacy);
				}
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
						b.title(d.t.names);
						int ava = SETT.ROOMS().STOCKPILE.tally().amountReservable.get(d.t);
						if (d.amount.get(event, data) < 0) {
							b.textLL(Dic.¤¤Needed);
							b.tab(6);
							GText t = b.text();
							b.add(GFORMAT.i(t, d.amount.get(event, data)));
							if (ava < -d.amount.get(event, data))
								t.errorify();
							b.NL();
							
						}
						b.textLL(Dic.¤¤Available);
						b.tab(6);
						
						b.add(GFORMAT.i(b.text(), ava));
						b.NL();
						
					};
					
				}.hh(d.t.icon()).hoverInfoSet(d.t.names));
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
				b.add(d.t.icon());
				int ava = SETT.ROOMS().STOCKPILE.tally().amountReservable.get(d.t);
				{
					GText te = b.text();
					GFORMAT.i(te, d.amount.get(event, context));
					if (d.amount.get(event, context) < 0 && -d.amount.get(event, context) > ava)
						te.errorify();
					else
						te.normalify2();
					b.add(te);
					
					te = b.text();
					te.add('(');
					GFORMAT.i(te, ava);
					te.add(')');
					te.normalify();
					b.add(te);
					
				}
			}
		}
		
		@Override
		public CharSequence problem(Event event, EContext context) {
			for (RAmount d : datas) {
				
				
				int ava = SETT.ROOMS().STOCKPILE.tally().amountReservable.get(d.t);
				if (d.amount.get(event, context) < 0 && -d.amount.get(event, context) > ava)
					return ¤¤noEnough;
			}
			return null;
		}
		
	}
	
	
	private static class RAmount extends Amount{

		public final RESOURCE t;
		
		RAmount(RESOURCE res, CInt amount) {
			super(amount);
			this.t = res;
		}
		
		
	}
	
}
