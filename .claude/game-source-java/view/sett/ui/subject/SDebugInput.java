package view.sett.ui.subject;

import init.settings.S;
import settlement.entity.humanoid.Humanoid;
import settlement.stats.Induvidual;
import snake2d.util.misc.STRING_RECIEVER;
import util.data.DOUBLE_O.DOUBLE_OE;
import view.main.VIEW;

final class SDebugInput {

	private static DOUBLE_OE<Induvidual> data;
	private static String name = "Set value";
	private static Humanoid h;
	private static final STRING_RECIEVER rec = new STRING_RECIEVER() {
		
		@Override
		public void acceptString(CharSequence string) {
			try {
				double v = Double.parseDouble(""+string);
				
				data.setD(h.indu(), v);
			}catch(Exception e) {
				
			}
		}
	};
	
	static void activate(DOUBLE_OE<Induvidual> data, Humanoid h) {
		if (!S.get().developer)
			return;
		SDebugInput.data = data;
		SDebugInput.h = h;
		VIEW.inters().input.requestInput(rec, name);
	}
	
}
