package view.sett.ui.minimap;

import java.io.IOException;

import game.GAME;
import game.save.Savable;
import init.race.RACES;
import init.race.Race;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import init.type.HTYPE;
import init.type.HTYPES;
import settlement.entity.ENTITY;
import settlement.entity.animal.Animal;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.room.main.RoomBlueprint;
import settlement.room.main.RoomBlueprintIns;
import settlement.room.main.RoomInstance;
import settlement.stats.STATS;
import settlement.stats.stat.STAT;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.color.OPACITY;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.Bitmap1D;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LinkedList;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.text.Str;
import snake2d.util.sprite.text.StringInputSprite;
import util.data.BOOLEAN.BOOLEANImp;
import util.data.BOOLEANO;
import util.gui.misc.GButt;
import util.gui.misc.GInput;
import util.gui.table.GScrollRows;
import util.text.Dic;
import view.interrupter.IPopCurrent;
import view.subview.GameWindow;

public final class UIMinimapSettConfigExt extends UIMinimapSettConfig {
	
	private final static COLOR good = new ColorImp(0, 0, 127);
	private final static COLOR bad = new ColorImp(127, 0, 0);
	private final static COLOR soso = new ColorImp(127, 127, 0);

	private final BOOLEANImp showAnimals = new BOOLEANImp(true);
	private final BOOLEANImp showGrowable = new BOOLEANImp(true);
	private final BOOLEANImp showMinerals = new BOOLEANImp(true);
	private final BOOLEANImp showDiv = new BOOLEANImp(true);
	
	private final Bitmap1D bitsHType = new Bitmap1D(HTYPES.ALL().size(), false);
	private final Bitmap1D bRaces = new Bitmap1D(RACES.all().size(), false);
	private final Bitmap1D bitsRooms = new Bitmap1D(SETT.ROOMS().AMOUNT_OF_BLUEPRINTS, false);
	private final Bitmap1D bitsEmployed = new Bitmap1D(SETT.ROOMS().AMOUNT_OF_BLUEPRINTS, false);
	{
		bitsHType.setAll(true);
		bitsRooms.setAll(true);
		bitsEmployed.setAll(true);
		bRaces.setAll(true);
	}
	private boolean bitUnemployed = true;
	private STAT statC = null;
	
	private final BOOLEANO<Humanoid> showHuman = new BOOLEANO<Humanoid>() {

		@Override
		public boolean is(Humanoid t) {
			if (bitsHType.get(t.indu().hType().index()) && bRaces.get(t.race().index())) {
				RoomInstance ins = STATS.WORK().EMPLOYED.get(t);
				if (ins == null)
					return bitUnemployed;
				return bitsEmployed.get(ins.blueprint().index());
			}
			return false;
		}
	
	};
	
	private final BOOLEANO<RoomBlueprintIns<?>> showRoom = new BOOLEANO<RoomBlueprintIns<?>>() {

		@Override
		public boolean is(RoomBlueprintIns<?> b) {
			return bitsRooms.get(b.index());
		}
	
	};

	public UIMinimapSettConfigExt(String key){

		if (key != null) {
			GAME.saver().add(new Savable("VIEW_MINI_SETT" + key) {
				
				@Override
				protected void save(FilePutter file) {
					showAnimals.save(file);
					showGrowable.save(file);
					showMinerals.save(file);
					showDiv.save(file);
					bitsHType.save(file);
					bitsRooms.save(file);
					bitsEmployed.save(file);
					bRaces.save(file);
				}
				
				@Override
				protected void load(FileGetter file) throws IOException {
					showAnimals.load(file);
					showGrowable.load(file);
					showMinerals.load(file);
					showDiv.load(file);
					bitsHType.load(file);
					bitsRooms.load(file);
					bitsEmployed.load(file);
					bRaces.load(file);
				}
			});
			
		}

		
	
		
	}
	
	@Override
	public COLOR col(ENTITY e) {
		boolean ani = showAnimals.is();
		if (e instanceof Animal) {
			if (!ani)
				return null;
			return colAnimal;
		}else if (e instanceof Humanoid) {
			Humanoid h = (Humanoid) e;
			if (!showHuman.is(h))
				return null;
			
			Humanoid a = (Humanoid) e;
			if (a.indu().hostile()) {
				if (STATS.BATTLE().ROUTING.indu().get(a.indu()) == 0)
					return colHostile;
				return colHostileRout;
			}
			
			if (statC != null) {
				double d = statC.indu().getD(h.indu());
				if (d == 0)
					return bad;
				if (d == 1)
					return good;
				return soso;
			}else {
				return colNormal;
			}
			
		}else {
			return null;
		}
		
		
	}

	@Override
	public boolean halfEnts() {
		return true;
	}

	@Override
	public boolean room(RoomBlueprintIns<?> b) {
		return showRoom.is(b);
	}

	@Override
	public boolean renderGrowable() {
		return showGrowable.is();
	}

	@Override
	public boolean renderMinable() {
		return showMinerals.is();
	}

	@Override
	public boolean renderPack() {
		return showAnimals.is();
	}
	
	@Override
	public OPACITY shade() {
		return OPACITY.O0;
	}

	@Override
	public boolean renderDivs() {
		return showDiv.is();
	}

	@Override
	public void addButtons(GuiSection sec, GameWindow w, UIMinimapSett s) {
		super.addButtons(sec, w, s);
		new Butts(sec);
	}
	
	private class Butts {

		public Butts(GuiSection s){
			
			{
				CLICKABLE c;
	
				c = new GButt.ButtPanel(SPRITES.icons().m.wildlife) {
					@Override
					protected void clickA() {
						showAnimals.toggle();
					}
					
					@Override
					protected void renAction() {
						selectedSet(showAnimals.is());
					}
					
				}.hoverInfoSet(Dic.¤¤Animals);
				s.addRightC(0, c);
				
				
				
				c = new GButt.ButtPanel(SPRITES.icons().m.clear_food) {
					@Override
					protected void clickA() {
						showGrowable.toggle();
					}
					
					@Override
					protected void renAction() {
						selectedSet(showGrowable.is());
					}
					
				}.hoverInfoSet(Dic.¤¤Growth);
				s.addRightC(0, c);
				
				c = new GButt.ButtPanel(SPRITES.icons().m.pickaxe) {
					@Override
					protected void clickA() {
						showMinerals.toggle();
					}
					
					@Override
					protected void renAction() {
						selectedSet(showMinerals.is());
					}
					
				}.hoverInfoSet(Dic.¤¤Minerals);
				s.addRightC(0, c);
				
				c = new GButt.ButtPanel(SPRITES.icons().m.sword) {
					@Override
					protected void clickA() {
						showDiv.toggle();
					}
					
					@Override
					protected void renAction() {
						selectedSet(showDiv.is());
					}
					
				}.hoverInfoSet(Dic.¤¤Armies);
				s.addRightC(0, c);
			}
			
			{
				
				LinkedList<RENDEROBJ> bbs = new LinkedList<>();
				
				bbs.add(new BSearchable(UI.icons().m.ok, Dic.¤¤All) {
					
					@Override
					protected void clickA() {
						bitsRooms.setAll(true);
					}
					
				});
				
				bbs.add(new BSearchable(UI.icons().m.cancel, Dic.¤¤None) {
					
					@Override
					protected void clickA() {
						bitsRooms.setAll(false);
					}
					
				});
				
				LinkedList<BSearchable> all = new LinkedList<>();
				
				for (RoomBlueprint b : SETT.ROOMS().all()) {
					if (b instanceof RoomBlueprintIns<?>) {
						RoomBlueprintIns<?> bb = (RoomBlueprintIns<?>) b;
						all.add(new BSearchable(bb.icon.big, bb.info.names) {
							
							@Override
							protected void clickA() {
								bitsRooms.toggle(bb.index());
							}
							
							@Override
							protected void renAction() {
								selectedSet(bitsRooms.get(bb.index()));
							}
							
						});
						
					}
				}
				
				IPopCurrent ii = bSearchList(bbs, all);
				
				CLICKABLE c = new GButt.ButtPanel(UI.icons().m.building) {
					@Override
					protected void clickA() {
						ii.show(this);
					}
				}.hoverTitleSet(Dic.¤¤Buildings);
				s.addRightC(0, c);
				
			}
			
			{
				
				LinkedList<RENDEROBJ> bbs = new LinkedList<>();
				
				bbs.add(new BSearchable(UI.icons().m.ok, Dic.¤¤All) {
					
					@Override
					protected void clickA() {
						bitsHType.setAll(true);
					}
					
				});
				
				bbs.add(new BSearchable(UI.icons().m.cancel, Dic.¤¤None) {
					
					@Override
					protected void clickA() {
						bitsHType.setAll(false);
					}
					
				});
				
				LinkedList<BSearchable> all = new LinkedList<>();
				
				for (HTYPE t : HTYPES.ALL()) {
					all.add(new BSearchable(UI.icons().s.human, t.names) {
						
						@Override
						protected void clickA() {
							bitsHType.toggle(t.index());
						}
						
						@Override
						protected void renAction() {
							selectedSet(bitsHType.get(t.index()));
						}
						
					});
				}
				
				IPopCurrent ii = bSearchList(bbs, all);
				
				CLICKABLE c = new GButt.ButtPanel(UI.icons().m.citizen) {
					@Override
					protected void clickA() {
						ii.show(this);
					}
				}.hoverTitleSet(Dic.¤¤Population);
				s.addRightC(0, c);
				
			}
			
			{
				
				LinkedList<RENDEROBJ> bbs = new LinkedList<>();
				
				bbs.add(new BSearchable(UI.icons().m.ok, Dic.¤¤All) {
					
					@Override
					protected void clickA() {
						bitsEmployed.setAll(true);
					}
					
				});
				
				bbs.add(new BSearchable(UI.icons().m.cancel, Dic.¤¤None) {
					
					@Override
					protected void clickA() {
						bitsEmployed.setAll(false);
					}
					
				});
				
				LinkedList<BSearchable> all = new LinkedList<>();
				
				for (RoomBlueprint b : SETT.ROOMS().all()) {
					if (b.employment() != null && b instanceof RoomBlueprintIns<?>) {
						RoomBlueprintIns<?> bb = (RoomBlueprintIns<?>) b;
						all.add(new BSearchable(bb.icon.big, bb.employment().title) {
							
							@Override
							protected void clickA() {
								bitsEmployed.toggle(bb.index());
							}
							
							@Override
							protected void renAction() {
								selectedSet(bitsEmployed.get(bb.index()));
							}
							
						});
						
					}
				}
				
				IPopCurrent ii = bSearchList(bbs, all);
				
				CLICKABLE c = new GButt.ButtPanel(UI.icons().m.workshop) {
					@Override
					protected void clickA() {
						ii.show(this);
					}
				}.hoverTitleSet(Dic.¤¤Employment);
				s.addRightC(0, c);
				
			}
			
			{
				LinkedList<RENDEROBJ> bbs = new LinkedList<>();
				
				

				bbs.add(new BSearchable(UI.icons().m.ok, Dic.¤¤All) {
					
					@Override
					protected void clickA() {
						bRaces.setAll(true);
					}
					
				});
				
				bbs.add(new BSearchable(UI.icons().m.cancel, Dic.¤¤None) {
					
					@Override
					protected void clickA() {
						bRaces.setAll(false);
					}
					
				});
				
				LinkedList<BSearchable> all = new LinkedList<>();
				
				for (Race  p : RACES.all()) {
					SPRITE ico = p.appearance().icon;
					all.add(new BSearchable(ico, p.info.names) {
						
						@Override
						protected void clickA() {
							bRaces.toggle(p.index);
						}
						
						@Override
						protected void renAction() {
							selectedSet(bRaces.get(p.index()));
						}
						
						
					});
				}
				
				IPopCurrent ii = bSearchList(bbs, all);
				
				CLICKABLE c = new GButt.ButtPanel(UI.icons().m.descrimination) {
					@Override
					protected void clickA() {
						ii.show(this);
					}
				}.hoverTitleSet(RACES.name());

				s.addRightC(0, c);
			}
			
			{
				LinkedList<RENDEROBJ> bbs = new LinkedList<>();
				
				
				bbs.add(new BSearchable(UI.icons().m.cancel, Dic.¤¤Clear) {
					
					@Override
					protected void clickA() {
						statC = null;
					}
					
				});
				
				LinkedList<BSearchable> all = new LinkedList<>();
				
				for (STAT p : STATS.createMatterList(true, false, null)) {
					SPRITE ico = p.info().icon;
					if (ico == null)
						ico = UI.icons().m.heart;
					all.add(new BSearchable(ico, p.info().name) {
						
						@Override
						protected void clickA() {
							if (statC == p)
								statC = null;
							else
								statC = p;
						}
						
						@Override
						protected void renAction() {
							selectedSet(statC == p);
						}
						
						@Override
						public void hoverInfoGet(GUI_BOX text) {
							text.text(p.info().desc);
						}
						
					});
				}
				
				IPopCurrent ii = bSearchList(bbs, all);
				
				CLICKABLE c = new GButt.ButtPanel(UI.icons().m.heart) {
					@Override
					protected void clickA() {
						ii.show(this);
					}
				}.hoverTitleSet(Dic.¤¤Happiness);

				s.addRightC(0, c);
			}
			
		}
		
		private class BSearchable extends GButt.ButtPanel {

			public final String search;
			
			public BSearchable(SPRITE icon, CharSequence label) {
				super(label);
				search = ""+label;
				icon(icon);
				body.setWidth(300);
			}
			
			
		}
		
		private IPopCurrent bSearchList(LIST<RENDEROBJ> pre, LIST<BSearchable> sss) {
			

			StringInputSprite input = new StringInputSprite(10, UI.FONT().M);
			input.placeHolder(Dic.¤¤Search);
			
			GInput in = new GInput(input);
			
			IPopCurrent pop = new IPopCurrent() {
				@Override
				public void show(CLICKABLE trigger) {
					super.show(trigger);
					in.focus();
				}
			};
			
			GuiSection s = pop.expansion;
			
			for (RENDEROBJ r : pre)
				s.addDown(0, r);
			
			s.addDown(8, in);;
			
			GScrollRows rr = new GScrollRows(sss, sss.get(0).body.height()*10) {
				@Override
				protected boolean passesFilter(int i, RENDEROBJ o) {
					if (input.text().length() == 0)
						return true;
					BSearchable s = (BSearchable) o;
					return Str.containsText(s.search, input.text());
				}
			};
			
			s.addDown(4, rr.view());
			
			return pop;
		}

	}
	

}
