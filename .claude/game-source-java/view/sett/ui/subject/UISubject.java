package view.sett.ui.subject;

import init.sprite.SPRITES;
import init.type.HTYPE;
import init.type.HTYPES;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.stats.STATS;
import settlement.stats.standing.STANDINGS;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.sprite.text.Str;
import util.gui.misc.GButt;
import util.text.D;
import util.text.Dic;
import view.interrupter.ISidePanel;
import view.keyboard.KEYS;
import view.main.VIEW;
import view.sett.ui.subject.UISubjects.Panel;

final class UISubject extends ISidePanel implements Panel{
	
	private AInfo a = new AInfo();
	private final Str title = new Str(24);
	private final GuiSection s = new GuiSection() {
		@Override
		public void render(SPRITE_RENDERER r, float ds) {
			if (a == null || a.a.isRemoved()) {
				VIEW.s().panels.remove(UISubject.this);
				return;
			}
			super.render(r, ds);
			
			if (a != null) {
				SETT.OVERLAY().add(a.a);
				
				
				if (KEYS.anyDown())
					a.follow--;
				if (a.follow > 0)
					VIEW.s().getWindow().centerer.set(a.a.body().cX(), a.a.body().cY());
			}
			
		};
	};

	private final SInfo info;
	private final SProperties prop = new SProperties(a, ISidePanel.HEIGHT-40);
	private final SStats stats = new SStats(a, ISidePanel.HEIGHT-40);
	private Object current;
	private static CharSequence ¤¤race = "¤Read up about current race in the tome of knowledge.";
	private static CharSequence ¤¤favourite = "¤Mark as favourite";
	private static CharSequence ¤¤follow = "¤Center screen at subject.";
	
	static {
		D.ts(UISubject.class);
	}
	
	UISubject(HTYPE type) {
		
		
		
		s.addRightC(0, new GButt.ButtPanel(SPRITES.icons().m.arrow_right) {
			
			@Override
			protected void clickA() {
				a.follow = 20;
			}
			
			@Override
			protected void renAction() {
				selectedSet(a.follow > 0);
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				text.text(¤¤follow);
			}
		}.pad(4, 1));
		
		if (type.player)
			s.addRightC(0, new GButt.ButtPanel(SPRITES.icons().m.heart) {
				
				@Override
				protected void clickA() {
					STATS.APPEARANCE().favo.set(a.a.indu(), (STATS.APPEARANCE().favo.get(a.a.indu()) + 1)&1);
				}
				
				@Override
				protected void renAction() {
					selectedSet(STATS.APPEARANCE().favo.get(a.a.indu()) == 1);
				}
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					text.text(¤¤favourite);
				}
			}.pad(4, 1));
		
		info = new SInfo(a, ISidePanel.HEIGHT-40, type);
		s.addRightC(0, new GButt.ButtPanel(Dic.¤¤Info) {
			@Override
			protected void clickA() {
				set(info, info.activate());
				super.clickA();
			}
			
			@Override
			protected void renAction() {
				selectedSet(current == info);
			}
		}.pad(4, 1));
		
		s.addRightC(0, new GButt.ButtPanel(Dic.¤¤Properites) {
			@Override
			protected void clickA() {
				set(prop, prop.activate());
				super.clickA();
			}
			
			@Override
			protected void renAction() {
				selectedSet(current == prop);
			}
		}.pad(4, 1));
		
		if (type.player && type != HTYPES.CHILD())
			s.addRightC(0, new GButt.ButtPanel(STANDINGS.CITIZEN().fullfillment.info().name) {
				@Override
				protected void clickA() {
					set(stats, stats.activate());
					super.clickA();
				}
				
				@Override
				protected void renAction() {
					selectedSet(current == stats);
				}
			}.pad(4, 1));
		
		s.addRightC(0, new GButt.ButtPanel(SPRITES.icons().m.questionmark) {
			@Override
			protected void clickA() {
				VIEW.UI().wiki.showRace(a.a.race());

			}
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				text.text(¤¤race);
				super.hoverInfoGet(text);
			}
		}.pad(4, 1));
		
		int w = 0;
		w = Math.max(w, info.activate().body().width());
		w = Math.max(w, prop.activate().body().width());
		w = Math.max(w, stats.activate().body().width());
		
//		s.addDownC(C.SG*4, makeServiceAccess(s.body().width()));
//		int width = s.body().width()/2;
//		int height = ISidePanel.HEIGHT-s.body().height()-16;
//		s.add(makeProperties(height, width-24), s.body().x1(), s.body().y2()+16);
//		s.add(makeStats(height, width+24), s.getLastX2()+8, s.getLastY1());
		
		section().body().setDim(w, 1);
		
		set(info, info.activate());
		
		
		new SPortraitsDebug();

		
	}
	
	void set(Object o, GuiSection s) {
		current = o;
		int w = section().body().width();
		int x1 = section().body().x1();
		int y1 = section().body().y1();
		section().clear();
		section().body().setDim(w, 1);
		
		section().addRelBody(2, DIR.S, this.s);
		section().addDownC(8, s);
		section().body().moveX1Y1(x1, y1);
	}

	@Override
	public void activate(Humanoid a, ISidePanel p) {
		this.a.a = a;
		title.clear();
		title.add(a.race().info.namePosessive).add(' ').add(a.indu().hType().name);
		titleSet(title);
		//sname.text().clear().add(STATS.APPEARANCE().name(a.indu()));
		this.a.follow = 20;
		VIEW.s().panels.addDontRemove(p, this);
		VIEW.s().getWindow().centerer.set(a.body().cX(), a.body().cY());
	}

	@Override
	public Humanoid showing() {
		if (VIEW.s().panels.added(this))
			return a.a;
		return null;
	}
}

