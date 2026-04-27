package view.ui.tourism;

import game.faction.FACTIONS;
import game.faction.FCredits.CTYPE;
import game.tourism.Review;
import game.tourism.TOURISM;
import init.race.Race;
import init.sprite.SPRITES;
import init.type.HTYPES;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.room.main.RoomBlueprintImp;
import settlement.stats.STATS;
import snake2d.SPRITE_RENDERER;
import snake2d.util.MATH;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.CLAMP;
import snake2d.util.sprite.SPRITE;
import util.data.INT.INTE;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GHeader;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.slider.GTarget;
import util.gui.table.GStaples;
import util.info.GFORMAT;
import util.statistics.HISTORY_INT;
import util.statistics.HistoryInt;
import util.text.D;
import util.text.Dic;
import util.text.DicTime;
import view.main.VIEW;
import view.ui.wiki.WIKI;

final class Tourism extends GuiSection{

	int hovered = -1;
	private static CharSequence ¤¤goTo = "¤Go to next tourist";
	private static CharSequence ¤¤Permit = "¤Click to toggle permission for race to visit and sightsee in your city.";
	private static CharSequence ¤¤Generosity = "¤Generosity";
	private static CharSequence ¤¤Attractions = "¤Attracted by:";
	
	private static CharSequence ¤¤bad = "¤Poor";
	private static CharSequence ¤¤ok = "¤Mixed";
	private static CharSequence ¤¤good = "¤Overwhelmingly Positive";
	
	private static CharSequence ¤¤attracted = "¤Attracted (year)";
	
	static {
		D.ts(Tourism.class);
	}
	
	Tourism(int height){
		
		
		stats();
		perm();
		addRelBody(16, DIR.N, new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.i(text, TOURISM.perYear());
			}
		}.hv(¤¤attracted));
		rev(height);

		
		
		pad(6, 0);
		
	}
	
	@Override
	public void render(SPRITE_RENDERER r, float ds) {
		if (htourist() != null)
			SETT.OVERLAY().add(htourist());
		super.render(r, ds);
	}
	
	private Humanoid htourist() {
		if (hovered == -1)
			return null;
		ENTITY e = SETT.ENTITIES().getAllEnts()[MATH.mod(hovered, SETT.ENTITIES().getAllEnts().length)];
		if (e != null && e instanceof Humanoid && !e.isRemoved()) {
			Humanoid a = (Humanoid) e;
			if (a.indu().hType() == HTYPES.TOURIST())
				return a;
		}
		hovered = -1;
		return null;
	}
	
	private void stats() {
		
		int x1 = body().x1();
		
		add(new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.i(text, STATS.POP().pop(HTYPES.TOURIST()));
			}
		}.increase().hh(HTYPES.TOURIST().names));
		
		addRightC(100, new GButt.ButtPanel(SPRITES.icons().m.crossair) {
			
			@Override
			protected void clickA() {
				
				ENTITY[] all = SETT.ENTITIES().getAllEnts();
				int mm = hovered + 1;
				for (int i = 1; i <= all.length; i++) {
					int ei = MATH.mod(i+mm, all.length);
					ENTITY e = all[ei];
					if (e instanceof Humanoid) {
						Humanoid a = (Humanoid) e;
						if (a.indu().hType() == HTYPES.TOURIST()) {
							hovered = ei;
							VIEW.UI().manager.close();
							VIEW.s().getWindow().centerer.set(a.body().cX(), a.body().cY());
							return;
						}	
					}
				}
				hovered = -1;
			}
			
			@Override
			protected void renAction() {
				activeSet(STATS.POP().pop(HTYPES.TOURIST()) > 0);
			}
			
		}.hoverInfoSet(¤¤goTo));
		
		addRightC(8, new GButt.ButtPanel(SPRITES.icons().m.questionmark) {
			
			@Override
			protected void clickA() {
				TOURISM.wiki().exe();
			}
		}.hoverInfoSet(WIKI.¤¤name));
		
		{
			HISTORY_INT c = TOURISM.history();
			GStaples s = new GStaples(c.historyRecords()) {
				
				@Override
				protected void hover(GBox box, int stapleI) {
					int i = c.historyRecords()-1-stapleI;
					box.textLL(DicTime.setSpanDays(box.text(), (i)*c.time().bitSeconds(), (i+1)*c.time().bitSeconds()));
					box.NL();
					box.add(GFORMAT.iIncr(box.text(), c.get(i)));
				}
				
				@Override
				protected double getValue(int stapleI) {
					int i = c.historyRecords()-1-stapleI;
					return c.get(i);
				}
			};
			s.body().setWidth(400).setHeight(64);
			add(s, x1, body().y2()+2);
		}
		
		HistoryInt c = FACTIONS.player().credits().get(CTYPE.TOURISM).IN;
		add(new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.i(text, c.get(0));
			}
		}.increase().hh(Dic.¤¤Earnings), x1, body().y2()+12);
		
		GStaples s = new GStaples(c.historyRecords()) {
			
			@Override
			protected void hover(GBox box, int stapleI) {
				int i = c.historyRecords()-1-stapleI;
				box.textLL(DicTime.setSpanDays(box.text(), (i)*c.time().bitSeconds(), (i+1)*c.time().bitSeconds()));
				box.NL();
				box.add(GFORMAT.iIncr(box.text(), c.get(i)));
			}
			
			@Override
			protected double getValue(int stapleI) {
				int i = c.historyRecords()-1-stapleI;
				return c.get(i);
			}
		};
		s.body().setWidth(400).setHeight(64);
		addRelBody(2, DIR.S, s);
	}
	
	private void perm() {
		
		int i = 0;
		GuiSection s = new GuiSection();
		for (Race r : TOURISM.races()) {
			
			s.addGrid(new GButt.ButtPanel(r.appearance().iconBig) {
				
				@Override
				protected void renAction() {
					selectedSet(TOURISM.permit(r));
				};
				
				@Override
				protected void clickA() {
					TOURISM.permit(r, !TOURISM.permit(r));
				}
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					GBox b = (GBox) text;
					b.title(r.info.names);
					
					b.textL(Dic.¤¤Occurrence);
					b.tab(5);
					b.add(GFORMAT.perc(b.text(), r.tourism().occurence));
					b.NL(2);
					
					b.textL(¤¤Generosity);
					b.tab(5);
					b.add(GFORMAT.perc(b.text(), r.tourism().credits));
					b.NL(2);
					
					
					b.textLL(¤¤Attractions);
					b.NL();
					boolean line = false;
					for (RoomBlueprintImp p : r.tourism().attractions) {
						b.add(p.iconBig());
						b.text(p.info.names);
						
						if (line)
							b.NL();
						else {
							b.tab(7);
						}
						line = !line;
					}
					
					b.NL(8);
					
					b.textL(¤¤Permit);
				}
				
				
			}.pad(8, 2),
					i++, 8, 2, 2);
			
		}
		
		addRelBody(12, DIR.S, s);
		
		
	}
	
	private void rev(int height) {
		{
			
			INTE in = new INTE() {
				int i = 0;
				@Override
				public int min() {
					return 0;
				}
				
				@Override
				public int max() {
					return CLAMP.i(TOURISM.reviews().size()-1, 0, 100);
				}
				
				@Override
				public int get() {
					return i;
				}
				
				@Override
				public void set(int t) {
					i = t;
				}
			};
			
			addRelBody(4, DIR.S, new GHeader(Dic.¤¤Reviews));
			
			int x1 = getLastX2();
			int cy = getLast().cY();
			
			GTarget t = new GTarget(100, (SPRITE)null, false, true, new GStat() {
				
				@Override
				public void update(GText text) {
					if (in.max() == 0)
						GFORMAT.iofk(text, in.get(), in.max());
					else
						GFORMAT.iofk(text, in.get()+1, in.max()+1);
					text.normalify();
				}
			}, in);
			addRelBody(4, DIR.S, t);
			
			
			RENDEROBJ o = new RENDEROBJ.RenderImp(900, height-Tourism.this.body().height()-16) {
				
				@Override
				public void render(SPRITE_RENDERER r, float ds) {
					Review rev = TOURISM.reviews().get(in.get());
					if (rev != null) {
						rev.render(r,  body().x1(), body().y1(), body().width());
					}
				}
			};
			
			addRelBody(16, DIR.S, o);
			
			addC(new GStat() {
				
				@Override
				public void update(GText text) {
					text.add('(');
					double d = TOURISM.score();
					if (d < 0.3)
						text.add(¤¤bad);
					else if (d < 0.8)
						text.add(¤¤ok);
					else
						text.add(¤¤good);
					text.add(')');
					text.lablifySub();
				}
				
				@Override
				public void hoverInfoGet(GBox b) {
					b.add(GFORMAT.perc(b.text(), TOURISM.score()));
				};
			}.r(), x1 + 120, cy);
			
			
		}
	}
	
}
