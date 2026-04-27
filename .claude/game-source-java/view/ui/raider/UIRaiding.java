package view.ui.raider;

import game.GAME;
import game.raiding.Raider;
import init.sprite.UI.UI;
import snake2d.util.datatypes.DIR;
import util.text.D;
import util.text.Dic;
import view.ui.manage.IFullView;

public final class UIRaiding extends IFullView {

	static boolean debug;
	public static CharSequence ¤¤name = "Raiders";
	
	static {
		D.ts(UIRaiding.class);
	}

//	final Army army = new Army();
	
	public UIRaiding() {
		super(Dic.¤¤Raiding, UI.icons().l.rebel);
		
		section.body().setWidth(WIDTH);
		section.body().setHeight(1);
		
		section.addRelBody(2, DIR.S, new Info());
		
		Current c = new Current(HEIGHT- section.getLastY2()-8);
		List l = new List(c, HEIGHT- section.getLastY2()-8);
		l.addRelBody(8, DIR.E, c);
		
		section.addRelBody(16, DIR.S, l);
		
	}
	
	@Override
	public void activate() {

		super.activate();
	}
	


	private static int ci = -1;
	private static int vi = -1;
	
	static boolean statsVisible(Raider r) {
		if (r.defeated)
			return true;
		if (r.raids > 0)
			return true;
		if (UIRaiding.debug)
			return true;
		if (r.isScared())
			return true;
		if (r.hasInterrest())
			return true;
		if (ci != GAME.updateI()) {
			ci = GAME.updateI();
			for (Raider rr : GAME.raiders().ALL()) {
				if (!rr.defeated && !rr.isScared() && !rr.hasInterrest()) {
					vi = rr.bounty;
					break;
				}
			}
		}
		return r.bounty == vi;
	}
	
	static boolean portVisible(Raider r) {
		if (r.defeated)
			return true;
		if (r.raids > 0)
			return true;
		if (UIRaiding.debug)
			return true;
		return false;
	}


}
