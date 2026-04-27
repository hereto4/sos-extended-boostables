package view.ui.div;

import java.util.Arrays;

import game.battle.util.DIV_SPEC.DIV_SPECE;
import game.boosting.BOOSTABLES;
import game.faction.FACTIONS;
import game.faction.Faction;
import init.constant.Config;
import init.race.RACES;
import init.race.Race;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import settlement.room.military.training.ROOM_M_TRAINER;
import settlement.stats.STATS;
import settlement.stats.colls.StatsBattle.StatTraining;
import settlement.stats.equip.EquipBattle;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.text.Str;
import snake2d.util.sprite.text.StringInputSprite;
import util.colors.GCOLOR;
import util.data.INT;
import util.data.INT.INTE;
import util.data.INT.IntImp;
import util.gui.common.UIPickerRace;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GInput;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.slider.GAllocator;
import util.gui.table.GRows;
import util.gui.table.GScrollRows;
import util.info.GFORMAT;
import util.text.Dic;
import view.main.VIEW;

public final class UIDivEditor extends GuiSection{

	private final static int width = 150;
	private final static int height = 42;
	
	private final Stats div;

	private final ArrayListGrower<Changer> changes = new ArrayListGrower<Changer>();
	private final boolean changeOverlay;
	
	private final UIPickerRace race;	
	
	@Override
	public void render(SPRITE_RENDERER r, float ds) {
		if (race.race() != div.race())
			race.set(div.race());
			
		super.render(r, ds);
	}
	
	
	private final UIDivBannerEditor ee = new UIDivBannerEditor();

	public DIV_SPECE div() {
		return div;
	}
	
	public void clearChanges() {
		for (Changer c : changes)
			c.init();
	}
	
	public void copyChanges(DIV_SPECE to) {
		for (Changer c : changes)
			if (c.isChanged()) {
				c.copyTo(to);
			}
	}
	
	public boolean hasChanges() {
		for (Changer c : changes)
			if (c.isChanged())
				return true;
		return false;
	}
	
	public UIDivEditor(double maxTraining, boolean name, boolean experience, boolean changeOverlay, LIST<Race> races) {
		this.changeOverlay = changeOverlay;
		int xs = 3;
		int ys = 6;
		race = new UIPickerRace(races) {
			
			@Override
			public void hover(GBox b, Race race) {
				b.title(race.info.names);
				b.text(race.info.desc);
				b.sep();
				
				race.boosts.hover(b, 1.0, null, BOOSTABLES.BATTLE().filter, -1);
			};
			
			@Override
			public void set(int ri) {
				super.set(ri);
				Race r = race();
				div.raceSet(r);
				
				for (int ei = 0; ei < STATS.EQUIP().BATTLE_ALL().size(); ei++) {
					EquipBattle e = STATS.EQUIP().BATTLE_ALL().get(ei);
					if (!e.allowed(r)) {
						div.equipSet(e, 0);
					}
				}
				
			};
			
		};	
		
		div = new Stats(maxTraining);
		div.raceSet(race.race());
		div.name.clear().add(div.race().info.armyNames.rnd());
		if (name){
			
			CLICKABLE title = new GInput(new  StringInputSprite(20, UI.FONT().H2) {
				@Override
				public void renAction() {
					text().clear().add(div.name);
				}
				
				@Override
				protected void change() {
					div.name.clear().add(text());
				};
			});

			Changer ch = new Changer(null) {
				
				final Str str = new Str(24);
				
				@Override
				void copyTo(DIV_SPECE to) {
					to.nameE().clear().add(div.name());
				}
				
				@Override
				void init() {
					str.clear().add(div.name());
				}
				
				@Override
				boolean isChanged() {
					return !Str.isSame(str, div.name());
				}
			};
			add(change(ch, title));
			
		}
		
		{
			GuiSection s = new GuiSection();
			Changer ch = new Changer(null) {
				
				int i = -1;
				
				@Override
				void init() {
					i = ee.bannerI();
				}
				
				@Override
				boolean isChanged() {
					return i != ee.bannerI();
				}
				
				@Override
				void copyTo(DIV_SPECE to) {
					to.bannerISet(div.bannerI());
				}
			};
			s.add(change(ch, ee.butt()));
			
			ch = new Changer(div.race) {
				
				@Override
				void copyTo(DIV_SPECE to) {
					to.raceSet(div.race());
				}
			};
			s.addRightC(8, change(ch, race.section));
			
			
			GStat st = new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.i(text, div.men());
				}
				
				@Override
				public void hoverInfoGet(GBox b) {

					b.title(Dic.¤¤SoldiersTarget);
					b.add(GFORMAT.i(b.text(), div.men()));
				};
				
			};

			ch = new Changer(div.men) {
				
				@Override
				void copyTo(DIV_SPECE to) {
					to.menSet(div.men());
				}
			};
			s.addRightC(8, change(ch, new Spec(SPRITES.icons().m.citizen, COLOR.GREEN100.makeSaturated(0.7), div.men, st)));
			
			
			s.addRelBody(2, DIR.E, new GButt.ButtPanel(UI.icons().m.cancel) {
				
				@Override
				protected void clickA() {
					div.clear();
				}
				
			}.setDim(32, s.getLast().height()).hoverInfoSet(Dic.¤¤Clear));
			
			
			addRelBody(8, DIR.S, s);
		}
		
		
		
		
		GRows rows = new GRows(xs);
		
		if (experience) {
			GStat s = new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.perc(text, div.experience());
				}
				
				@Override
				public void hoverInfoGet(GBox b) {

					
					b.title(STATS.BATTLE().COMBAT_EXPERIENCE.info().name);
					b.add(GFORMAT.perc(b.text(), div.experience()));
					b.NL();
					b.text(STATS.BATTLE().COMBAT_EXPERIENCE.info().desc);
					b.sep();
					STATS.BATTLE().COMBAT_EXPERIENCE.boosters.hover(b, 1.0, -1);
					VIEW.inters().mouseMessage.setAbove();
					
				};
				
			};
			Changer ch = new Changer(div.experience) {
				
				@Override
				void copyTo(DIV_SPECE to) {
					to.experienceSet(div.experience());
				}
			};
			rows.add(change(ch, new Spec(STATS.BATTLE().COMBAT_EXPERIENCE.info().icon, COLOR.RED100.makeSaturated(0.7), div.experience, s)));
		}
		

		for (ROOM_M_TRAINER<?> t : ROOM_M_TRAINER.ALL()) {
			
			GStat s = new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.perc(text, div.training(t.training()));
				}
				
				@Override
				public void hoverInfoGet(GBox b) {
					
					b.title(t.tInfo.name);
					b.add(GFORMAT.perc(b.text(), div.training(t.training())));
					b.NL();
					b.text(t.tInfo.desc);
					b.sep();
					t.boosters.hover(b, 1.0, -1);
					VIEW.inters().mouseMessage.setAbove();
					
				};
				
			};
			Changer ch = new Changer(div.traini(t.training())) {
				
				@Override
				void copyTo(DIV_SPECE to) {
					to.trainingSet(t.training(), div.training(t.training()));
				}
			};
			rows.add(change(ch, new Spec(t.icon, COLOR.RED100.makeSaturated(0.7), div.traini(t.training()), s)));
		
			
		}
		
		
	
		
		for (EquipBattle m : STATS.EQUIP().BATTLE_ALL()) {
			GStat s = new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.i(text, div.men()*div.equipi(m).get());
				}
				
				@Override
				public void hoverInfoGet(GBox b) {
					m.hover(b);
				};
				
			};
			
			Spec sp = new Spec(m.resource().icon(), COLOR.ORANGE100.makeSaturated(0.7), div.equipi(m), s) {
				
				@Override
				public void render(SPRITE_RENDERER r, float ds) {

					
					super.render(r, ds);
					if (disabled()) {
						OPACITY.O50.bind();
						COLOR.BLACK.render(r, body(), -1);
						OPACITY.unbind();
					}
				}
				
				private boolean disabled() {
					
					if (div.equipi(m).get() == 0) {
						if (!div.canSet(m))
							return true;
					}
					return false;
				}
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					GBox b = (GBox) text;
					if (!m.allowed(div.race()))
						b.error(EquipBattle.¤¤raceProblem);
					else if (disabled()) {
						b.error(EquipBattle.¤¤combineProblem);
						
					}else {
						super.hoverInfoGet(text);
					}
					b.NL(8);
					VIEW.inters().mouseMessage.setAbove();
				}
				
				@Override
				public boolean click() {
					if (!m.allowed(div.race()))
						return false;
					return super.click();
				}
				
			};
			Changer ch = new Changer(div.equipi(m)) {
				
				@Override
				void copyTo(DIV_SPECE to) {
					to.equipSet(m, div.equip(m));;
				}
			};
			
			rows.add(change(ch, sp));
			
			
		
		}
		
		if (rows.rows().size() > ys) {
			addRelBody(8, DIR.S, new GScrollRows(rows.rows(), height*ys).view());
		}else {
			boolean first = true;
			for (RENDEROBJ o : rows.rows()) {
				addRelBody(first ? 8 : 0, DIR.S, o);
				first = false;
			}
		}
		
		addRelBody(8, DIR.S, new UIDivStats().get(div));
	
		
		
		
	}
	
	
	private static class Spec extends GuiSection{
		

		private final GStat stat;
		
		public Spec(SPRITE icon, COLOR col, INTE ii, GStat stat){
			GAllocator g = new GAllocator(col, ii, 6, 12);
			
			body().setDim(width, height);
			
			addC(icon, 20, body().cY());
			
			add(stat, 48, body().cY()-stat.height());
			add(g, 40, body().cY() + 2);
			if (body().width() > width)
				body().incrW(4);
			
			this.stat = stat;
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			super.hoverInfoGet(text);
			if (text.emptyIs())
				stat.hoverInfoGet((GBox) text);
		}
		
		@Override
		public void render(SPRITE_RENDERER r, float ds) {
			GCOLOR.UI().bg().render(r, body());
			GButt.ButtPanel.renderFrame(r, body());
			super.render(r, ds);
		}
		
		
	}
	
	private abstract class Changer{
		
		int old = -1;
		private final IntImp ii;
		
		Changer(IntImp ii){
			this.ii = ii;
		}
		
		void init() {
			old = ii.get();
		}
		
		boolean isChanged() {
			return old != ii.get();
		}
		
		abstract void copyTo(DIV_SPECE to);
		
	}
	
	private RENDEROBJ change(Changer ch, RENDEROBJ ob) {
		GuiSection s = new GuiSection();
		s.add(ob);
		if (changeOverlay) {
			RENDEROBJ.RenderImp r = new RenderImp(ob.body().width(), ob.body().height()) {
				
				@Override
				public void render(SPRITE_RENDERER r, float ds) {
					if (ch.isChanged())
						UI.icons().s.alert.renderC(r, body().x2()-10, body().y1()+10);
				}
			};
			s.add(r, 0, 0);
		}
		
		changes.add(ch);
		return s;
		
	}
	
	private class Stats implements DIV_SPECE {

		public final IntImp[] training = new IntImp[STATS.BATTLE().TRAINING_ALL.size()];
		public final Gear[] gear = new Gear[STATS.EQUIP().BATTLE_ALL().size()];
		public final INT.IntImp men = new INT.IntImp(1, (int) Math.ceil(Config.battle().MEN_PER_DIVISION/10));
		public final INT.IntImp experience = new INT.IntImp(0, 4);
		public IntImp race = new IntImp();
		public Str name = new Str(24).add(Dic.¤¤rename);
		public IntImp faction = new IntImp(); 
		
	
		
		public Stats(double maxTraining) {
			men.setD(0.75);
			for (int i = 0; i < gear.length; i++) {
				gear[i] = new Gear(i, gear);
			}
			
			for (int i = 0; i < training.length; i++) {
				training[i] = new IntImp(0, 10) {
					@Override
					public int max() {
						int am = (int) (maxTraining*10);
						for(IntImp ii : training)
							if (ii != this)
								am -= ii.get();
						return CLAMP.i(am, 0, 10);
					};
				};
			}		
			
		}
		
		private final double[] slots = new double[EquipBattle.SLOTS];
		
		private void setSlots() {
			Arrays.fill(slots, 0);
			for (Gear g : gear) {
				if (g.get() > 0) {
					for (int si = 0; si < slots.length; si++) {
						slots[si] += STATS.EQUIP().BATTLE_ALL().get(g.gi).slotUse(si);
					}
				}
			}
		}
		
		public boolean canSet(EquipBattle m) {
			if (!m.allowed(RACES.all().get(race.get())))
				return false;
			setSlots();
			for (int si = 0; si < slots.length; si++) {
				if (slots[si] + m.slotUse(si) > 1) {
					return false;
				}
			}
			return true;
		}
		
		private class Gear extends IntImp {
			
			private final int gi;
			private final Gear[] other;
			
			Gear(int gi, Gear[] other){
				super(0, STATS.EQUIP().BATTLE_ALL().get(gi).max());
				this.gi = gi;
				this.other = other;
			}
			
			@Override
			public void set(int t) {
				EquipBattle s = STATS.EQUIP().BATTLE_ALL().get(gi);
				if (t > 0) {
					Arrays.fill(slots, 0);
					for (Gear g : gear) {
						if (g.get() > 0) {
							for (int si = 0; si < slots.length; si++) {
								if (g != this)
									slots[si] += STATS.EQUIP().BATTLE_ALL().get(g.gi).slotUse(si);
							}
						}
					}
					for (int oi = 0; oi < STATS.EQUIP().BATTLE_ALL().size(); oi++) {
						EquipBattle o = STATS.EQUIP().BATTLE_ALL().get(oi);
						if (s != o) {
							for (int si = 0; si < slots.length; si++) {
								if (slots[si] + s.slotUse(si) > 1 && o.slotUse(si) > 0 && other[oi].get() > 0) {
									other[o.indexMilitary()].set(0);
									slots[si] -= o.slotUse(si);
								}
							}
							
							
						}
					}
					
					
					
					
				}
				super.set(t);
			}
		}
		

		@Override
		public double training(StatTraining tr) {
			return (double)training[tr.tIndex].get()/10.0;
		}

//		public void trainingSet(StatTraining tr, double training) {
//			training[tr.tIndex] = training;
//		}
		
		public IntImp equipi(EquipBattle e) {
			return gear[e.indexMilitary()];
		}
		
		public IntImp traini(StatTraining tr) {
			return training[tr.tIndex];
		}
		
		@Override
		public double equip(EquipBattle e) {
			return gear[e.indexMilitary()].getD();
		}

		@Override
		public int men() {
			return men.get()*10;
		}

		@Override
		public Race race() {
			return RACES.all().get(race.get());
		}
		
		@Override
		public void raceSet(Race race) {
			this.race.set(race.index);
			UIDivEditor.this.race.set(race);
		}

		@Override
		public double experience() {
			return experience.getD();
		}

		@Override
		public Faction faction() {
			return FACTIONS.getByIndex(faction.get());
		}

		@Override
		public CharSequence name() {
			return name;
		}
		
		@Override
		public int bannerI() {
			return ee.bannerI();
		}

		public void clear() {
			for (IntImp ii : gear)
				ii.set(0);
			for (IntImp ii : training)
				ii.set(0);
			experience.set(0);
		}

		@Override
		public void menSet(int men) {
			this.men.setD((double)men/Config.battle().MEN_PER_DIVISION);
		}

		@Override
		public void experienceSet(double experience) {
			this.experience.setD(experience);
		}

		@Override
		public Str nameE() {
			return name;
		}

		@Override
		public void bannerISet(int bannerI) {
			ee.bannerISet(bannerI);
			
		}

		@Override
		public void trainingSet(StatTraining tr, double d) {
			
			training[tr.tIndex].set((int) Math.round(d*10));
			
		}

		@Override
		public void equipSet(EquipBattle e, double d) {
			gear[e.indexMilitary()].setD(d);
			
		}

		@Override
		public void factionSet(Faction faction) {
			this.faction.set(faction == null ? 0 : faction.index());
		}
		
	}
	
	
}
