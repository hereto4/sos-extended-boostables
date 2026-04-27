package view.sett.ui.subject;

import game.GAME;
import game.faction.FACTIONS;
import init.resources.RES_AMOUNT;
import init.settings.S;
import init.sprite.SPRITES;
import init.type.HTYPE;
import init.type.HTYPES;
import init.value.GVALUES;
import settlement.main.SETT;
import settlement.room.home.HOME;
import settlement.room.main.RoomInstance;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import settlement.stats.muls.StatsMultipliers.StatMultiplier;
import settlement.stats.muls.StatsMultipliers.StatMultiplierAction;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.STRING_RECIEVER;
import util.data.GETTER;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.info.GFORMAT;
import util.text.D;
import util.text.Dic;
import view.main.VIEW;
import view.ui.util.UIValues;

final class SInfoActions extends GuiSection{
	
	final GuiSection section = new GuiSection();
	
	private static CharSequence ¤¤rename = "¤Rename subject.";

	private static CharSequence ¤¤Elevate = "¤Elevate.";
	private static CharSequence ¤¤NobleOk = "Click to elevate this subject into a position of power.";
	private static CharSequence ¤¤NobleSure = "Are you sure you wish to elevate this subject to the rank of noble?";
	private static CharSequence ¤¤NobleNo = "Your status determines how many nobles you can elevate. To elevate more, you must increase your status level.";
	private static CharSequence ¤¤NobleAlready = "This subject is already a noble one.";
	private static CharSequence ¤¤workplace = "¤Go to workplace";

	
	private static CharSequence ¤¤ActionNotFor = "¤Action not available for:";
	private static CharSequence ¤¤ActionMarked = "¤The subject is marked for this action, but the action has not yet been consummated. Make sure the requirements of the action are fulfilled, and give it some time.";
	private static CharSequence ¤¤ActionConsumed = "¤This action has been consumed. Its effect will last for a few days while tampering off.";
	private static CharSequence ¤¤ActionCantBe = "¤Action can currently not be performed.";
	static {
		D.ts(SInfoActions.class);
	}

	SInfoActions(AInfo a, HTYPE t) {
		int i = 0;
		final int rrr = 4;
		
		if (t.player && t != HTYPES.CHILD()) {
			addGrid(new GButt.ButtPanel(SPRITES.icons().m.admin) {
				
				STRING_RECIEVER r = new STRING_RECIEVER() {
					
					@Override
					public void acceptString(CharSequence string) {
						if (string != null && string.length() > 0)
							STATS.APPEARANCE().customName(a.a.indu()).clear().add(string);
					}
				};
				
				@Override
				protected void clickA() {
					VIEW.inters().input.requestInput(r, ¤¤rename, STATS.APPEARANCE().name(a.a.indu()));

				}
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					text.text(¤¤rename);
					super.hoverInfoGet(text);
				}
				
			}.setDim(40, 40), i++, rrr, 2, 2);
			
			addGrid(new GButt.ButtPanel(SPRITES.icons().m.noble) {
				
				ACTION yes = new ACTION() {
					
					@Override
					public void exe() {
						a.a.nobleSet();
					}
				};
				@Override
				protected void renAction() {				
					activeSet(a.a.race().playable && a.a.noble() == null);
				}
				
				@Override
				protected void clickA() {
					if (a.a.noble() == null && a.a.race().playable)
						if (S.get().developer || GAME.NOBLE().active().size() < GAME.NOBLE().MAX.get(FACTIONS.player()))
							VIEW.inters().yesNo.activate(¤¤NobleSure, yes, null, true);
				}
				
				
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					text.title(¤¤Elevate);
					if (a.a.noble() != null)
						text.text(¤¤NobleAlready);
					else if (GAME.NOBLE().active().size() < GAME.NOBLE().MAX.get(FACTIONS.player()))
						text.text(¤¤NobleOk);
					else
						((GBox)text).error(¤¤NobleNo);
				}
				
				
			}.setDim(40, 40), i++, rrr, 2, 2);
			
			if (t.works) {
				addGrid(new GButt.ButtPanel(SPRITES.icons().m.workshop) {
					@Override
					protected void clickA() {
						RoomInstance i = STATS.WORK().EMPLOYED.get(a.a.indu());
						a.follow = 0;
						if (i != null)
							VIEW.s().getWindow().centererTile.set(i.body().cX(), i.body().cY());
					};
					
					@Override
					protected void renAction() {
						activeSet(STATS.WORK().EMPLOYED.get(a.a.indu()) != null);
					};
					
					@Override
					public void hoverInfoGet(GUI_BOX text) {
						text.text(¤¤workplace);
						text.NL(4);
						if (STATS.WORK().EMPLOYED.get(a.a.indu()) != null)
							text.text(STATS.WORK().EMPLOYED.get(a.a.indu()).name());
						super.hoverInfoGet(text);
					}
					
				}.setDim(40, 40), i++, rrr, 2, 2);
			}
			
			addGrid(new GButt.ButtPanel(SPRITES.icons().m.sword) {

				@Override
				protected void renAction() {
					activeSet(a.a.division() != null || STATS.BATTLE().RECRUIT.get(a.a) != null);
				}
				
				@Override
				protected void clickA() {
					VIEW.s().battle.activate();
				}
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					text.title(Dic.¤¤Division);
					
					if (a.a.division() != null)
						text.text(a.a.division().info.name());
					if (STATS.BATTLE().RECRUIT.get(a.a) != null)
						text.text(STATS.BATTLE().RECRUIT.get(a.a).info.name());
					else
						text.text(Dic.¤¤None);
				};
				
				
			}.setDim(40, 40), i++, rrr, 2, 2);
			
			addGrid(new GButt.ButtPanel(SPRITES.icons().m.building) {
				
				@Override
				protected void renAction() {
					HOME h = STATS.HOME().GETTER.get(a.a, this);
					activeSet(h != null);
				};
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					GBox b = (GBox) text;
					b.title(SETT.ROOMS().HOME.info.name);
					int ri = 0;
					HOME h = STATS.HOME().GETTER.get(a.a, this);
					if (h != null) {
						for (RES_AMOUNT ra : a.a.race().home().clas(a.a.indu().clas()).resources()) {
							b.add(ra.resource().icon());
							b.add(GFORMAT.iofkInv(b.text(), STATS.HOME().current(a.a, ri++), ra.amount()));
							b.NL();
						}
					}
					
					text.NL();
					
//					CharSequence s = a.a.race().bio().houseProblem(a.a);
//					if (s != null)
//						b.add(b.text().warnify().add(s));
				}
				
				@Override
				protected void clickA() {
					HOME h = STATS.HOME().GETTER.get(a.a, this);
					if (h != null) {
						VIEW.s().getWindow().centerAtTile(h.serviceX(), h.serviceY());
						a.follow = 0;
					}else {
						VIEW.s().panels.add(VIEW.s().ui.home, true);
					}
				}
				
			}.setDim(40, 40), i++, rrr, 2, 2);
			
			for (StatMultiplier m : STATS.MULTIPLIERS().all()) {
				if (m instanceof StatMultiplierAction) {
					addGrid(mulAction(a, (StatMultiplierAction) m), i++, rrr, 2, 2);
				}
			}
		}
		
		addGrid(new GButt.ButtPanel(SPRITES.icons().m.crossair) {
			
			@Override
			protected void clickA() {
				COORDINATE c = a.a.ai().getDestination();
				if (c != null) {
					VIEW.s().getWindow().centerAtTile(c.x(), c.y());
					a.follow = 0;
					VIEW.s().clearAllInterrupters();
				}
			}
			
			@Override
			protected void renAction() {
				COORDINATE c = a.a.ai().getDestination();
				activeSet(c != null);
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				GBox box = (GBox) text;
				box.text(Dic.¤¤Destination);
				box.NL();
				COORDINATE c = a.a.ai().getDestination();
				if (c != null) {
					box.add(box.text().add('(').add(c.x()).add(':').add(c.y()).add(')'));
				}
			}
		}.setDim(40, 40), i++, rrr, 2, 2);
		

		

		
		
		
		if (S.get().developer) {
			addGrid(new GButt.ButtPanel(SPRITES.icons().m.exit) {

				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					
				}
				
				@Override
				protected void clickA() {
					a.a.interrupt();
				}
				
			}.setDim(40, 40), i++, rrr, 2, 2);
			
			addRelBody(8, DIR.S, UIValues.butt(GVALUES.INDU, new GETTER<Induvidual>() {

				@Override
				public Induvidual get() {
					return a.a.indu();
				}
				
			}));

			
			
		}
		

		
	}
	
	private static RENDEROBJ mulAction(AInfo a, StatMultiplierAction m) {
		return new GButt.ButtPanel(m.icon) {
			
			@Override
			protected void renAction() {
				activeSet(m.available(a.a.indu()) && !m.consumeIs(a.a) && (m.markIs(a.a) || m.canBeMarked(a.a.indu())));
				selectedSet(m.markIs(a.a) || m.consumeIs(a.a));
			};
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				GBox b = (GBox) text;
				b.title(m.verb);
				b.text(m.desc);
				b.NL(8);
				
				if (!m.available(a.a.indu())){
					b.add(b.text().errorify().add(¤¤ActionNotFor).s().add(a.a.indu().clas().names));
				}else if (m.consumeIs(a.a)){
					b.textL(¤¤ActionConsumed);
					m.boosters.hover(text, 1.0, -1);

				}else if(m.markIs(a.a)) {
					b.textL(¤¤ActionMarked);
				}else if(!m.canBeMarked(a.a.indu())) {
					b.error(¤¤ActionCantBe);
					b.NL(4);
					m.info(b, 1);
				}else {
					m.boosters.hover(text, 1.0, -1);
				}
				
				
				
			}
			
			@Override
			protected void clickA() {
				if (activeIs())
					m.mark(a.a, !m.markIs(a.a));
			}
			
		}.setDim(40, 40);
	}

	
}
