package view.ui.top;

import game.GAME;
import game.GameDisposable;
import game.faction.FACTIONS;
import game.faction.diplomacy.DIP;
import game.time.TIME;
import init.constant.C;
import init.race.RACES;
import init.race.Race;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import init.type.CAUSE_LEAVE;
import init.type.CAUSE_LEAVES;
import init.type.HCLASSES;
import init.type.HTYPES;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.stats.STATS;
import settlement.stats.stat.STAT;
import settlement.thing.ThingsCorpses;
import settlement.thing.ThingsCorpses.Corpse;
import snake2d.MButt;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.color.ColorShifting;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.Bitmap1D;
import snake2d.util.sets.LinkedList;
import snake2d.util.sprite.SPRITE;
import util.colors.GCOLOR;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GText;
import util.info.GFORMAT;
import util.info.INFO;
import util.text.D;
import util.text.Dic;
import util.text.DicTime;
import view.main.VIEW;
import world.WORLD;
import world.army.AD;
import world.army.ADSupply;
import world.entity.army.WArmy;
import world.map.regions.Region;
import world.map.regions.WREGIONS;
import world.region.RD;
import world.region.building.RDBuilding;

public final class UINotifications extends GuiSection {
	
	
	
	private static final LinkedList<UINotification> butts = new LinkedList<>();
	private static boolean created = false;
	
	private HOVERABLE invasion;
	private Siege siege;
	private final static int BW = 46;
	private static final int max = 6 + (C.WIDTH()-C.MIN_WIDTH)/(2*BW);
	
	private static CharSequence ¤¤constructed = "Regions that can be upgraded";
	private static CharSequence ¤¤regProblem = "Regions in trouble";
	private static CharSequence ¤¤siege = "Sieges";
	private static CharSequence ¤¤siegeD = "Amount of regions currently under siege";
	private static CharSequence ¤¤wrong = "Wrongful deaths";
	private static CharSequence ¤¤wrongD = "Some subjects have died in ways that could have been prevented.";
	private static CharSequence ¤¤room = "New Rooms";
	private static CharSequence ¤¤roomD = "New rooms that have been constructed.";
	private static CharSequence ¤¤roomB = "Broken Rooms";
	private static CharSequence ¤¤roomBD = "Rooms that have been broken and needs repair.";
	
	private static CharSequence ¤¤supply = "Supply Issues";
	private static CharSequence ¤¤supplyD = "Some of our armies are low on crucial supplies. This can be due to supplies being cut off from your capitol, poor production, or supply depot problems. If not supplied armies will desert.";
	
	static {
		new GameDisposable() {
			
			@Override
			protected void dispose() {
				butts.clear();
				created = false;
			}
		};
		
		D.ts(UINotifications.class);
	}
	
	UINotifications(){

		if (false) {
			//Add one for a closed city with no immigrants possible
		}
		
		if (!created) {
			new UINotification(SPRITES.icons().s.plate, new ColorImp(127, 100, 0), STATS.FOOD().STARVATION.info()) {
				
				int k = 0;
				@Override
				public int get() {
					return STATS.FOOD().STARVATION.data(null).get(null);
				}
				
				@Override
				protected void clickA() {
					k = showNextH(k, STATS.FOOD().STARVATION);
				}
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					super.hoverInfoGet(text);
					UINotifications.this.hover(text, STATS.FOOD().STARVATION);
				}
				
			};
			
			new UINotification(SPRITES.icons().s.alert, new ColorImp(127, 100, 0), STATS.POP().TRAPPED.info()) {
				int k = 0;
				
				@Override
				public int get() {
					return STATS.POP().TRAPPED.data(null).get(null);
				}
				
				@Override
				protected void clickA() {
					k = showNextH(k, STATS.POP().TRAPPED);
					super.clickA();
				}
				
			};
			
			new UINotification(SPRITES.icons().s.drop, new ColorImp(127, 20, 0), STATS.NEEDS().INJURIES.DANGER.info()) {
				int k = 0;
				
				@Override
				public int get() {
					return STATS.NEEDS().INJURIES.DANGER.data(null).get(null);
				}
				
				@Override
				protected void clickA() {
					k = showNextH(k, STATS.NEEDS().INJURIES.DANGER);
					super.clickA();
				}
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					super.hoverInfoGet(text);
					UINotifications.this.hover(text, STATS.NEEDS().INJURIES.DANGER);
				}
				
			};
			
			new UINotification(SPRITES.icons().s.human, new ColorImp(0, 110, 127), STATS.NEEDS().EXPOSURE.DANGER.info()) {
				int k = 0;
				
				@Override
				public int get() {
					return STATS.NEEDS().EXPOSURE.DANGER.data(null).get(null);
				}
				
				@Override
				protected void clickA() {
					k = showNextH(k, STATS.NEEDS().EXPOSURE.DANGER);
					super.clickA();
				}
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					super.hoverInfoGet(text);
					UINotifications.this.hover(text, STATS.NEEDS().EXPOSURE.DANGER);
				}
				
			};
			
			new UINotification(SPRITES.icons().s.arrow_left, new ColorImp(127, 20, 0), STATS.POP().EMMIGRATING.info()) {
				int k = 0;
				
				@Override
				public int get() {
					return STATS.POP().EMMIGRATING.data(null).get(null);
				}
				
				@Override
				protected void clickA() {
					k = showNextH(k, STATS.POP().EMMIGRATING);
					super.clickA();
				}
				
			};
			
			
			
			new UINotification(SPRITES.icons().s.death, new ColorImp(127, 20, 0), true) {
				
				LinkedList<CAUSE_LEAVE> wrongful = new LinkedList<>();
				
				{
					for (CAUSE_LEAVE l : CAUSE_LEAVES.ALL()) {
						if (l.defaultStanding() > 0) {
							wrongful.add(l);
						}
					}
				}

				private int ci = 0;
				private Corpse corpse;
				
				@Override
				public int get() {
					int i = 0;
					for (CAUSE_LEAVE c : wrongful) {
						i+= SETT.THINGS().corpses.amount(c);
					}
					return i;
				}
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					GBox b = (GBox) text;
					b.title(¤¤wrong);
					b.text(¤¤wrongD).NL();
					for (CAUSE_LEAVE c : wrongful) {
						b.textL(c.name);
						b.tab(4);
						b.add(GFORMAT.i(b.text(), SETT.THINGS().corpses.amount(c)));
						b.NL();
					}
					super.hoverInfoGet(text);
				}
				
				@Override
				protected void clickA() {
					if (corpse == null || corpse.isRemoved() || corpse.cause() != wrongful.get(ci)) {
						corpse = getCorpse();
					}
					if (corpse != null) {
						VIEW.s().activate();
						VIEW.s().getWindow().centererTile.set(corpse.ctx(), corpse.cty());
						corpse = SETT.THINGS().corpses.getNext(corpse);
					}
				}
				
				private Corpse getCorpse() {
					for (int i = 0; i < wrongful.size(); i++) {
						ci ++;
						ci %= wrongful.size();
						Corpse c = SETT.THINGS().corpses.getFirst(wrongful.get(ci));
						if (c != null) {
							return c;
						}
					}
					return null;
				}
				
			};
			
			new UINotification(SPRITES.icons().s.shrine, new ColorImp(110, 110, 0), true) {
				
				
				private short ci = 0;
				private Corpse corpse;
				
				@Override
				public int get() {
					return SETT.THINGS().corpses.nrOfCorpses();
				}
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					GBox b = (GBox) text;
					b.title(STATS.ENV().UNBURRIED.info().name);
					for (CAUSE_LEAVE c : CAUSE_LEAVES.ALL()) {
						int am = SETT.THINGS().corpses.amount(c);
						if (am != 0) {
							b.textL(c.name);
							b.tab(4);
							b.add(GFORMAT.i(b.text(), SETT.THINGS().corpses.amount(c)));
							b.NL();
						}
						
					}
					super.hoverInfoGet(text);
				}
				
				@Override
				protected void clickA() {
					if (corpse == null || corpse.isRemoved()) {
						corpse = getCorpse();
					}
					if (corpse != null) {
						VIEW.s().activate();
						VIEW.s().getWindow().centererTile.set(corpse.ctx(), corpse.cty());
						corpse = SETT.THINGS().corpses.getNext(corpse);
					}
				}
				
				private Corpse getCorpse() {
					for (int i = 0; i < ThingsCorpses.MAX; i++) {
						
						Corpse c = SETT.THINGS().corpses.getByIndex(ci);
						if (c != null) {
							return c;
						}
						ci ++;
						ci %= ThingsCorpses.MAX;
					}
					return null;
				}
				
			};
			
			new UINotification(SPRITES.icons().s.hammer, new ColorImp(20, 127, 20), ¤¤room, ¤¤roomD) {
				@Override
				public int get() {
					return SETT.ROOMS().stats.finished().amount();
				}
				
				@Override
				protected void clickA() {
					COORDINATE r = SETT.ROOMS().stats.finished().poll();
					if (r != null) {
						VIEW.s().activate();
						VIEW.s().getWindow().centererTile.set(r);
						if (SETT.ROOMS().map.instance.get(r) != null)
							VIEW.s().ui.rooms.open(SETT.ROOMS().map.instance.get(r));
					}
					super.clickA();
				}
				
				@Override
				protected void supress() {
					SETT.ROOMS().stats.finished().clear();
				}
				
			};
			
			new UINotification(SPRITES.icons().s.hammer, new ColorImp(127, 20, 20), ¤¤roomB, ¤¤roomBD) {
				@Override
				public int get() {
					return SETT.ROOMS().stats.broken().amount();
				}
				
				@Override
				protected void clickA() {
					COORDINATE r = SETT.ROOMS().stats.broken().poll();
					if (r != null) {
						VIEW.s().activate();
						VIEW.s().getWindow().centererTile.set(r);
					}
					super.clickA();
				}
				
				@Override
				protected void supress() {
					SETT.ROOMS().stats.broken().clear();
				}
				
			};
			
			new UINotification(SPRITES.icons().s.crazy, new ColorImp(127, 100, 20), HTYPES.DERANGED().names, HTYPES.DERANGED().desc) {
				int k = 0;
				@Override
				public int get() {
					int am = STATS.POP().pop(HTYPES.DERANGED());
					am -= SETT.ROOMS().ASYLUM.prisoners();
					am = CLAMP.i(am, 0, Integer.MAX_VALUE);
					return am;
				}
				
				@Override
				protected void clickA() {
					ENTITY[] es = SETT.ENTITIES().getAllEnts();
					for (int q = 0; q < es.length; q++) {
						if (k >= es.length)
							k = 0;
						ENTITY e = es[k];
						k++;
						if (e instanceof Humanoid && (((Humanoid) e).indu().hType()) == HTYPES.DERANGED()){
							VIEW.s().activate();
							VIEW.s().getWindow().centererTile.set(e.tc());
							VIEW.s().ui.subjects.show(((Humanoid) e));
							return;
						}
						
					}
				}
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					super.hoverInfoGet(text);
					GBox b = (GBox) text;
					b.NL();
					for (int ri = 0; ri < RACES.all().size(); ri++) {
						Race r = FACTIONS.player().races.get(ri);
						b.add(r.appearance().icon);
						b.textL(r.info.names);
						b.tab(6);
						b.add(GFORMAT.i(b.text(), STATS.POP().pop(r, HTYPES.DERANGED())));
						b.NL();
					}
				}
				
			};
			
			new UINotification(SPRITES.icons().s.sword, new ColorImp(127, 20, 20), Dic.¤¤Trespassing, Dic.¤¤TrespassingD) {
				int am = 0;
				private final GAME.Cache cache = new GAME.Cache(60);
				@Override
				public int get() {
					if (cache.shouldAndReset()) {
						am = 0;
						for (int ri = 0; ri < FACTIONS.player().realm().regions(); ri++) {
							Region reg = FACTIONS.player().realm().region(ri);
							for (WArmy a : WORLD.ENTITIES().armies.fill(reg))
								if (DIP.WAR().is(FACTIONS.player(), a.faction()))
									am++;
						}
					}
					return am;
				}
				
				@Override
				protected void clickA() {
					
					cache.shouldAndReset();
					int k = get();
					if (k == 0)
						return;
					
					for (int ri = 0; ri < FACTIONS.player().realm().regions(); ri++) {
						Region reg = FACTIONS.player().realm().region(ri);
						for (WArmy a : WORLD.ENTITIES().armies.fill(reg))
							if (DIP.WAR().is(FACTIONS.player(), a.faction())) {
								k--;
								if (k == 0) {
									VIEW.world().activate();
									VIEW.world().window.centererTile.set(a.ctx(), a.cty());
									return;
								}
							}
					}
				}
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					super.hoverInfoGet(text);
				}
				
			};
			
			new UINotification(SPRITES.icons().s.world, new ColorImp(20, 127, 20), null, ¤¤constructed) {
				
				ArrayListGrower<RDBuilding> notis = new ArrayListGrower<>();
				final Bitmap1D has = new Bitmap1D(WREGIONS.MAX, false);
				
				{
					for (RDBuilding b : RD.BUILDINGS().all) {
						if (b.notify)
							notis.add(b);
					}
				}
				
				
				int am = 0;
				int ri = 0;
				int k = 0;
				private final GAME.Cache cache = new GAME.Cache(4);
				@Override
				public int get() {
					
					if (!cache.shouldAndReset())
						return am;
					Region reg = WORLD.REGIONS().getByIndex(ri);
					if (has.get(ri))
						am --;
					if (reg != null && reg.active() && !reg.capitol() && reg.faction() == FACTIONS.player()) {
						
						for (RDBuilding bu: notis) {
							
							if (!bu.level.isMax(reg) && bu.canAfford(reg,  bu.level.get(reg), bu.level.get(reg)+1) == null) {
								am++;
								has.set(ri, true);
							}
								
						}
					}
					ri++;
					ri %= WREGIONS.MAX;
					return am;
				}
				
				@Override
				protected void clickA() {
					
					for (int ri = 0; ri < FACTIONS.player().realm().regions(); ri++) {
						k %= FACTIONS.player().realm().regions();
						Region reg = FACTIONS.player().realm().region(k);
						k++;
						if (reg.capitol())
							continue;
						for (RDBuilding bu: notis) {
							if (!bu.level.isMax(reg) && bu.canAfford(reg,  bu.level.get(reg), bu.level.get(reg)+1) == null) {
								VIEW.world().activate();
								VIEW.world().UI.regions.open(reg);
								VIEW.world().window.centererTile.set(reg.cx(), reg.cy());
								return;
							}
								
						}
					}
					
					am = 0;
					has.setAll(false);

				}
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					super.hoverInfoGet(text);
				}
				
			};
			
			
			
			new UINotification(SPRITES.icons().s.world, new ColorImp(127, 20, 20), null, ¤¤regProblem) {
				
				ArrayListGrower<RDBuilding> notis = new ArrayListGrower<>();
				final Bitmap1D has = new Bitmap1D(WREGIONS.MAX, false);
				
				{
					for (RDBuilding b : RD.BUILDINGS().all) {
						if (b.notify)
							notis.add(b);
					}
				}
				
				
				int am = 0;
				int ri = 0;
				int k = 0;
				private final GAME.Cache cache = new GAME.Cache(4);
				@Override
				public int get() {
					
					if (!cache.shouldAndReset())
						return am;
					Region reg = WORLD.REGIONS().getByIndex(ri);
					if (has.get(ri))
						am --;
					if (reg != null && reg.active() && !reg.capitol() && reg.faction() == FACTIONS.player()) {
						
						if (problem(reg)) {
							am++;
							has.set(ri, true);
						}
						
					}
					ri++;
					ri %= WREGIONS.MAX;
					return am;
				}
				
				private boolean problem(Region reg) {
					if (false)
						;//have a method like this with texts in RD. Also mage emmigration negative growth slower than health or riot negative growth to prevent whack a mole mechanics when capturing a big region.
					
					for (RDBuilding bu: notis) {
						
						if (bu.efficiency.get(reg) < 1) {
							return true;
						}
							
					}
					
					if (RD.RACES().popTarget.getD(reg) < RD.RACES().population.get(reg))
						return true;
					
					if (RD.RACES().loyaltyAll.getD(reg) < 0) {
						return true;
					}
					
					if (RD.HEALTH().boostablee.get(reg) < 0.5) {
						return true;
					}
					return false;
				}
				
				@Override
				protected void clickA() {
					
					for (int ri = 0; ri < FACTIONS.player().realm().regions(); ri++) {
						k %= FACTIONS.player().realm().regions();
						Region reg = FACTIONS.player().realm().region(k);
						k++;
						if (reg.capitol())
							continue;
						if (problem(reg)) {
							VIEW.world().activate();
							VIEW.world().UI.regions.open(reg);
							VIEW.world().window.centererTile.set(reg.cx(), reg.cy());
							return;
						}
					}
					
					am = 0;
					has.setAll(false);

				}
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					super.hoverInfoGet(text);
				}
				
			};
			
			new UINotification(SPRITES.icons().s.degrade, new ColorImp(127, 127, 20), ¤¤siege, ¤¤siegeD) {
				int am = 0;
				private final GAME.Cache cache = new GAME.Cache(60);
				@Override
				public int get() {
					if (cache.shouldAndReset()) {
						am = 0;
						for (int ri = 0; ri < FACTIONS.player().realm().regions(); ri++) {
							Region reg = FACTIONS.player().realm().region(ri);
							if (reg.besieged())
								am++;
						}
					}
					return am;
				}
				
				@Override
				protected void clickA() {
					
					cache.shouldAndReset();
					int k = get();
					if (k == 0)
						return;
					
					for (int ri = 0; ri < FACTIONS.player().realm().regions(); ri++) {
						Region reg = FACTIONS.player().realm().region(ri);
						if (reg.besieged()) {
							VIEW.world().activate();
							VIEW.world().window.centererTile.set(reg.cx(), reg.cy());
						}
					}
				}
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					super.hoverInfoGet(text);
					GBox b = (GBox) text;
					b.sep();
					for (int ri = 0; ri < FACTIONS.player().realm().regions(); ri++) {
						Region reg = FACTIONS.player().realm().region(ri);
						
						if (reg.besieged()) {
							b.text(reg.info.name());
							b.NL();
						}
					}
				}
				
			};
			
			new UINotifications.UINotification(UI.icons().s.storage, GCOLOR.UI().BAD.hovered, ¤¤supply, ¤¤supplyD) {
				
				int armyI = 0;
				int current = 0;
				int next = 1;
				
				@Override
				public int get() {
					
					if (armyI >= FACTIONS.player().armies().all().size()) {
						armyI = 0;
						current = next;
						next = 0;
					}else {
						
						WArmy a = FACTIONS.player().armies().all().get(armyI);
						for (ADSupply s : AD.supplies().healths) {
							if (s.daysStored(a) < ADSupply.STOCKPILE_DAYS*0.5) {
								next++;
								break;
							}
						}
						
						armyI ++;
					}
					return current;
				}
			};
			
//			new UINotifications.UINotification(UI.icons().s.cog, GCOLOR.UI().SOSO.hovered, dic.wo¤¤supply, ¤¤supplyD) {
//				
//				int armyI = 0;
//				int current = 0;
//				int next = 1;
//				
//				@Override
//				public int get() {
//					
//					if (armyI >= FACTIONS.player().armies().all().size()) {
//						armyI = 0;
//						current = next;
//						next = 0;
//					}else {
//						
//						WArmy a = FACTIONS.player().armies().all().get(armyI);
//						for (ADSupply s : AD.supplies().healths) {
//							if (s.daysStored(a) < ADSupply.STOCKPILE_DAYS*0.5) {
//								next++;
//								break;
//							}
//						}
//						
//						armyI ++;
//					}
//					return current;
//				}
//			};
			
			created = true;
		}
		
		
		
		if (false) {
			//alert for low workload
			//when hapiness drop, alert of what it's about in particular.
		}
		
		siege = new Siege();
		
		invasion = new HOVERABLE.HoverableAbs(32, 22) {
			
			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
				GButt.ButtPanel.renderBG(r, true, false, isHovered, body);
				
				GCOLOR.T().IBAD.bind();
				UI.icons().s.degrade.renderC(r, body);
				COLOR.unbind();
				GButt.ButtPanel.renderFrame(r, body);
				
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				SETT.INVADOR().hover(text);
			}
			
			
		};
		
		body().setDim(max*butts.get(0).body.width(), butts.get(0).body().height());
		
		D.spop();
		
		
	}
	
	private static class Siege extends ClickableAbs {
 
		int aii = 0;
		int am = 0;
		int amLast = 0;
		
		public Siege() {
			super(32, 22);
		}
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
			GButt.ButtPanel.renderBG(r, true, false, isHovered, body);
			
			for (WArmy a : FACTIONS.player().armies().all()) {
				if (a.besieging() != null) {
					double p = RD.MILITARY().defensePower(a.besieging())*(1-RD.MILITARY().besigeMul(a.besieging()));
					p /= AD.power().get(a);
					
					if (p > 1)
						GCOLOR.T().IBAD.bind();
					else if (p < 0.5)
						GCOLOR.T().IGREAT.bind();
					else
						GCOLOR.T().IGOOD.bind();
					UI.icons().s.shield.renderC(r, body);
				}
			}
			COLOR.unbind();
			GButt.ButtPanel.renderFrame(r, body);
			
		}
		
		@Override
		protected void clickA() {
			
			for (WArmy a : FACTIONS.player().armies().all()) {
				if (a.besieging() != null) {
					VIEW.world().activate();
					VIEW.world().window.centererTile.set(a.ctx(), a.cty());
					return;
				}
			}
		}
		
		public int am() {
			if (aii >= FACTIONS.player().armies().all().size()) {
				amLast = am;
				am = 0;
				aii = 0;
				return amLast;
			}
			WArmy a = FACTIONS.player().armies().all().get(aii);
			if (a != null && a.besieging() != null) {
				am++;
			}
			aii++;
			
			return amLast;
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			GBox b = (GBox) text;
			b.title(Dic.¤¤Besiege);
			
			for (WArmy a : FACTIONS.player().armies().all()) {
				if (a.besieging() != null) {
					b.textLL(Dic.¤¤Army);
					b.tab(6);
					b.text(a.name);
					b.NL();
					b.textLL(Dic.¤¤Region);
					b.tab(6);
					b.text(a.besieging().info.name());
					b.NL();
					b.textLL(DicTime.¤¤Days);
					b.tab(6);
					b.text(GFORMAT.f(b.text(), WORLD.BATTLES().besigedTime(a.besieging())/TIME.secondsPerDay()));
					b.NL();
					b.textLL(Dic.¤¤Defences);
					b.tab(6);
					
					double p = RD.MILITARY().defensePower(a.besieging())*(1-RD.MILITARY().besigeMul(a.besieging()));
					p /= AD.power().get(a);
					p = 1-p;
					b.text(GFORMAT.percInv(b.text(), p));
					b.NL();
					
				}
			}
		}
		
	}
	
	private int showNextH(int k, STAT s) {
		ENTITY[] es = SETT.ENTITIES().getAllEnts();
		for (int q = 0; q < es.length; q++) {
			if (k >= es.length)
				k = 0;
			ENTITY e = es[k];
			k++;
			if (e instanceof Humanoid && s.indu().isMax(((Humanoid) e).indu())){
				Humanoid h = (Humanoid) e;
				if (h.indu().clas().player && s.indu().isMax(h.indu())) {
					VIEW.s().activate();
					VIEW.s().getWindow().centererTile.set(e.tc());
					VIEW.s().ui.subjects.show(((Humanoid) e));
				}
				return k;
			}
			
		}
		return k;
	}

	void add(RENDEROBJ b, int i) {
		b.body().moveX1Y1(b.body().width()*(i/2), b.body().height()*(i%2));
		addRightC(0, b);
	}
	
	@Override
	public void render(SPRITE_RENDERER r, float ds) {
		int x1 = body().x1();
		int y1 = body().y1();
		clear();
		int am = 0;
		if (SETT.INVADOR().invadingPending())
			addRight(0, invasion);
		if (siege.am() > 0)
			addRight(0, siege);
		for (UINotification b : butts) {
			if (b.get() > 0) {
				addRight(0, b);
				am++;
				if (am > max)
					return;
			}
		}
		body().moveX1Y1(x1, y1);
		super.render(r, ds);
	}
	
	
	public static abstract class UINotification extends CLICKABLE.ClickableAbs {

		private final GText text = new GText(UI.FONT().S, 10);
		private final static COLOR flashBg = new ColorShifting(GCOLOR.UI().bg(), GCOLOR.UI().bg().shade(4.0)).setSpeed(1.5);
		private final SPRITE icon;
		private final COLOR color;
		private boolean showNumber;
		
		int lastValue = -1;
		double flashFor = 0;
		
		public UINotification(SPRITE icon, COLOR color, boolean showNumber){
			this.icon = icon;
			this.color = color;
			body.setDim(showNumber ? BW : 22, 22);
			this.showNumber = showNumber;
			butts.add(this);
		}
		
		public UINotification(SPRITE icon, COLOR color, CharSequence hover){
			this(icon, color, true);
			hoverInfoSet(hover);
			body.setDim(BW, 22);
		}
		
		public UINotification(SPRITE icon, COLOR color, CharSequence name, CharSequence desc){
			this(icon, color, true);
			hoverTitleSet(name);
			hoverInfoSet(desc);
			body.setDim(BW, 22);
		}
		
		public UINotification(SPRITE icon, COLOR color, INFO info){
			this(icon, color, true);
			hoverTitleSet(info.name);
			hoverInfoSet(info.desc);
			body.setDim(BW, 22);
		}
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
			
			GCOLOR.UI().border().render(r, body);
			int am = get();
			if (lastValue == -1 || lastValue != am) {
				lastValue = am;
				flashFor = 20;
			}else {
				flashFor -= ds;
			}
			
			if (flashFor > 0) {
				flashBg.render(r, body, -1);
			}else {
				GCOLOR.UI().bg(true, isHovered, false).render(r, body, -1);
			}
			
			ColorImp.TMP.set(color);
			
			ColorImp.TMP.set(color).bind();
			icon.renderCY(r, body().x1()+2, body().cY());
			COLOR.unbind();
			
			text.clear();
			if (showNumber) {
				GFORMAT.i(text, CLAMP.i(am, 0, 99));
				text.adjustWidth();
				text.renderCY(r, body().x1()+20, body().cY()+2);
				text.color(COLOR.WHITE100);
			}
			

			OPACITY.unbind();
		}
		
		@Override
		public boolean hover(COORDINATE mCoo) {
			if (super.hover(mCoo)) {
				flashFor = 0;
				if (MButt.RIGHT.consumeClick()) {
					supress();
				}
				return true;
			}
			return false;
		}
		
		protected void supress() {
			
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			super.hoverInfoGet(text);
		}
		
		public abstract int get();
		
	}
	
	private void hover(GUI_BOX text, STAT s) {
		
		GBox b = (GBox) text;
		b.NL();
		b.tab(6);
		b.textLL(HCLASSES.CITIZEN().names);
		b.tab(9);
		b.textLL(HCLASSES.SLAVE().names);
		b.tab(12);
		b.textLL(HCLASSES.NOBLE().names);
		b.NL();
		for (int ri = 0; ri < RACES.all().size(); ri++) {
			Race r = FACTIONS.player().races.get(ri);
			b.add(r.appearance().icon);
			b.textL(r.info.names);
			b.tab(6);
			b.add(GFORMAT.i(b.text(), s.data(HCLASSES.CITIZEN()).get(r)));
			b.tab(9);
			b.add(GFORMAT.i(b.text(), s.data(HCLASSES.SLAVE()).get(r)));
			b.tab(12);
			b.add(GFORMAT.i(b.text(), s.data(HCLASSES.NOBLE()).get(r)));
			b.NL();
		}
		
		b.NL(4);
		
		b.textL(HCLASSES.CHILD().names);
		b.tab(6);
		b.add(GFORMAT.i(b.text(), s.data(HCLASSES.CHILD()).get(null)));
		
	}
	

}
