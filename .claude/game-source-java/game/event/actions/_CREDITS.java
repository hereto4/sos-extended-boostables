package game.event.actions;

import game.event.engine.EContext;
import game.event.engine.Event;
import game.faction.FACTIONS;
import game.faction.FCredits.CTYPE;
import init.sprite.UI.UI;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.file.Json;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.LISTE;
import util.gui.misc.GBox;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.info.GFORMAT;
import util.text.D;
import util.text.Dic;

final class _CREDITS extends EventActionConstructor{
	
	
	private static CharSequence ¤¤tooPoor = "You are too poor to pay this sum.";
	static {
		D.ts(_CREDITS.class);
	}
	
	_CREDITS() {
		super("CREDITS");
	}
	
	@Override
	public EventAction action(Data data) {
		return new Imp(key, data.json, data.all);
	}
	
	public final class Imp extends EventAction  {

		private final Amount amount;
		private final boolean negativeAllowed;
		
		Imp(String key, Json data, LISTE<EventAction> all) {
			super(key, all);
			amount = new Amount(new CInt("AMOUNT"));
			amount.read(data, -Integer.MIN_VALUE);
			negativeAllowed = data.bool("NEGATIVE_ALLOWED", true);
			data.checkUnused();
		}

		@Override
		public void setContext(Event event, EContext data) {
			amount.set(event, data, (int) FACTIONS.player().credits().credits());
		}
		

		@Override
		public void exe(Event event, EContext data) {

			FACTIONS.player().credits().inc(amount.amount.get(event, data), CTYPE.MISC);
			
		}
		
		@Override
		public void addToMessageBody(LISTE<RENDEROBJ> rows, Event e, EContext data, RECTANGLE messBody) {
			
			rows.add(new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.i(text, amount.amount.get(e, data));
				}
			}.hh(UI.icons().m.coins));
		}
		
		@Override
		public void hover(GBox b, Event event, EContext context) {
			
			b.add(UI.icons().s.money);
			b.textLL(Dic.¤¤Currs);
			b.tab(6);
			int ava = (int) FACTIONS.player().credits().credits();
			{
				GText te = b.text();
				GFORMAT.i(te, amount.amount.get(event, context));
				b.add(te);
				
				te = b.text();
				te.add('(');
				GFORMAT.i(te, ava);
				te.add(')');
				te.normalify();
				b.add(te);
				
			}
		}
		
		@Override
		public CharSequence problem(Event event, EContext context) {
			if (!negativeAllowed && -amount.amount.get(event, context) > FACTIONS.player().credits().getD())
				return ¤¤tooPoor;
			return null;
		}
		
	}
	
}
