package game.events.advice;

import java.io.IOException;

import game.events.EVENTS.EventResource;
import game.events.citizen.EventCitizen;
import init.paths.PATHS;
import settlement.main.SETT;
import settlement.stats.STATS;
import settlement.stats.law.LAW;
import settlement.stats.standing.STANDINGS;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.Json;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.LinkedList;
import util.info.INFO;
import util.text.D;
import view.main.VIEW;
import view.sett.UISettMap;
import view.ui.message.MessageSection;

public final class EventAdvisor extends EventResource{

	private boolean toggled = true;
	private double timer = 5;
	private LinkedList<Advice> all = new LinkedList<>();
	private LinkedList<Advice> active = new LinkedList<>();
	
	private static CharSequence ¤¤Advice = "Advisor: ";
	
	static {
		D.ts(EventAdvisor.class);
	}
	
	public EventAdvisor() {
		super("ADVICE");
		Json json = new Json(PATHS.TEXT_MISC().get("Advice"));
		
		new AdviceHighlight("ROOMS", "WORKFORCE", json) {
			
			@Override
			public boolean shouldsend() {
				
				int t = STATS.WORK().workforce();
				int e = SETT.ROOMS().employment.NEEDED.get();
				if (t-e < -5) {
					return true;
				}
				return false;
			}
		};
		
		new AdviceHighlight("HEALTH", "SICKNESS", json) {
			
			@Override
			public boolean shouldsend() {
				
				if (STATS.DISEASE().sick().data().get(null) > 0)
					return true;
				return false;
			}
		};
		
		new AdviceHighlight("LAW", "CRIME", json) {
			
			@Override
			public boolean shouldsend() {
				
				if (LAW.crimes().crimes(null).get() > 0)
					return true;
				return false;
			}
		};
		
		new AdviceHighlight("CITIZENS", "LOYALTY", json) {
			
			@Override
			public boolean shouldsend() {
				if (STANDINGS.CITIZEN().loyalty.getD(null) < EventCitizen.breakPoint && STATS.POP().POP.data().get(null) > 15)
					return true;
				return false;
			}
		};
		
		for (Advice a : all)
			active.add(a);
		
	}
	
	@Override
	protected void update(double ds) {
		if (!toggled)
			return;
		if (!VIEW.s().isActive()) {
			timer = 3;
			return;
		}
		timer -= ds;
		if (timer < 0) {
			timer += 5;
			for (Advice a : active) {
				if (a.send()) {
					active.remove(a);
					return;
				}
			}
		}
	}

	@Override
	protected void save(FilePutter file) {
		file.i(active.size());
		for (Advice a : active)
			file.i(a.index);
	}

	@Override
	protected void load(FileGetter file) throws IOException {
		active.clear();
		int k = file.i();
		for (int i = 0; i < k; i++) {
			int q = file.i();
			if (q >= 0 && q < all.size())
				active.add(all.get(q));
		}

	}

	@Override
	protected void clear() {
		active.clear();
		for (Advice a : all)
			active.add(a);
		
	}
	
	public abstract class Advice {
		
		private final int index;
		
		Advice(){
			this.index = all.add(this);
		}
		
		public abstract boolean send();
		
	}
	
	public abstract class AdviceHighlight extends Advice {
		
		private final String keyButt;
		private final INFO info;
		AdviceHighlight(String keyButt, String keyj, Json json){
			
			this.keyButt = keyButt;
			UISettMap.getByKey(keyButt);
			info = new INFO(json.json(keyj));
			
		}
		
		public abstract boolean shouldsend();

		@Override
		public boolean send() {
			if (shouldsend()) {
				new MessageHighlight(info.name, info.desc, keyButt).send();
				return true;
			}
			return false;
		}
		
	}
	
	private static class MessageHighlight extends MessageSection{

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private final String UIKey;
		private final String body;
		
		public MessageHighlight(CharSequence title, CharSequence body, String UIKey) {
			super("" + ¤¤Advice + title);
			this.UIKey = UIKey;
			this.body = "" + body;
		}

		@Override
		protected void make(GuiSection section) {
			paragraph(body);
			
			section.addDown(0, new RENDEROBJ.RenderImp(0) {
				final RENDEROBJ o = UISettMap.getByKey(UIKey);
				@Override
				public void render(SPRITE_RENDERER r, float ds) {
					highlight(section, r, o);
					if (!VIEW.s().isActive())
						VIEW.s().activate();
				}
			});
		}
		
		private static void highlight(GuiSection s, SPRITE_RENDERER r, RENDEROBJ o) {
			
			COLOR c = COLOR.RED2RED;

			c.render(r, o.body().x1()-8, o.body().x2() + 8, o.body().y1()-8, o.body().y1()-4);
			c.render(r, o.body().x1()-8, o.body().x2() + 8, o.body().y2()+8, o.body().y2()+4);
			c.render(r, o.body().x1()-8, o.body().x1() - 4, o.body().y1()-8, o.body().y2()+8);
			c.render(r, o.body().x2()+4, o.body().x2() + 8, o.body().y1()-8, o.body().y2()+8);
			
			
			
			if (o.body().cX() < s.body().cX()) {
				c.render(r, o.body().x2()+4, s.body().cX() + 4, o.body().cY()-4, o.body().cY()+4);
			}else {
				c.render(r, o.body().x1()-4, s.body().cX() + 4, o.body().cY()-4, o.body().cY()+4);
			}
			
			int y1 = s.body().y1()-80;
			int y2 = s.body().y2();
			
			if (o.body().y2() < y1) {
				c.render(r, s.body().cX()-4, s.body().cX()+4, o.body().cY(), y1);
			}else {
				c.render(r, s.body().cX()-4, s.body().cX()+4, o.body().cY(), y2);
			}
			
			
			OPACITY.unbind();
		}
		
	}

}
