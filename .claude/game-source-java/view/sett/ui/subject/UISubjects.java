package view.sett.ui.subject;

import init.type.HTYPE;
import init.type.HTYPES;
import settlement.entity.humanoid.Humanoid;
import settlement.room.main.RoomInstance;
import util.gui.misc.GBox;
import view.interrupter.ISidePanel;
import view.main.VIEW;

public class UISubjects {
	
	public final UIList list = new UIList();
	private final Panel[] subjects = new UISubject[HTYPES.ALL().size()];
	private Panel subject = null;
	final UIHoverer hoverer = new UIHoverer();
	
	public UISubjects() {
		for (int ti = 0; ti < HTYPES.ALL().size(); ti++) {
			HTYPE t = HTYPES.ALL().get(ti);
			subjects[t.index()] = new UISubject(t);
		}
	}
	
	public Humanoid current() {
		if (subject == null)
			return null;
		Humanoid a = subject.showing();
		if (a != null)
			return a;
		return null;
	}
	
	public void hoverInfo(Humanoid h, GBox text) {
		hoverer.hover(h, text);
	}
	
	public void show() {
		list.show();
	}
	
	public boolean listActive() {
		return VIEW.s().panels.added(list);
	}
	
	public boolean shows(Humanoid h) {
		return current() == h;
	}
	
	public boolean shows(HTYPE t) {
		return current() != null && current().indu().hType() == t;
	}
	
	public void show(Humanoid h) {
		list.show(h);
		if (get(h) != null) {
			get(h).activate(h, list);
			subject = get(h);
		}
	}
	
	public void showSingle(Humanoid h) {
		
		if (get(h) != null) {
			get(h).activate(h, list);
			subject = get(h);
		}
	}
	
	public void showProfession(RoomInstance work) {
		list.showProfession(work);
	}
	
	public boolean canShow(Humanoid a) {
		return get(a) != null;
	}
	
	public Panel get(Humanoid a) {
		return subjects[a.indu().hType().index()];
	}
	
	interface Panel {
		
		public void activate(Humanoid a, ISidePanel list);
		public Humanoid showing();
		
	}

}
