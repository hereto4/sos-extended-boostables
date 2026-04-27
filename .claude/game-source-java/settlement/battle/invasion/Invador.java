package settlement.battle.invasion;

import java.io.IOException;

import game.GAME;
import game.battle.util.DIV_SETTING.DIV_SETTINGImp;
import game.battle.util.DIV_SPEC;
import game.battle.util.DivGeneration;
import game.debug.Profiler;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.diplomacy.DIP;
import game.faction.diplomacy.DipStance;
import game.faction.npc.FactionNPC;
import game.time.TIME;
import init.constant.Config;
import init.paths.PATHS;
import init.race.Race;
import init.type.HCLASSES;
import init.type.HTYPES;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.main.SETT.SettResource;
import settlement.room.military.artillery.ArtilleryInstance;
import settlement.room.military.artillery.ROOM_ARTILLERY;
import settlement.stats.colls.StatsBattle.StatTraining;
import settlement.stats.equip.EquipBattle;
import settlement.stats.standing.STANDINGS;
import settlement.thing.projectiles.Projectile;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.Json;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.misc.ACTION;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import util.gui.misc.GBox;
import util.info.GFORMAT;
import util.text.D;
import util.text.Dic;
import view.sett.IDebugPanelSett;
import world.entity.army.WArmy;

public final class Invador extends SettResource{

	private ArrayList<Invasion> active = new ArrayList<>(16);
	final Projectile proj;
	
	private static CharSequence ¤¤inv = "¤Pending Invasion";
	static {
		D.ts(Invador.class);
	}
	public Invador() throws IOException {
		super("INVADOR", false);
		IDebugPanelSett.add("Invade Small", new ACTION() {
			@Override
			public void exe() {
				invade(20, 0.2);
			}
		});
		
		IDebugPanelSett.add("Invade Medium", new ACTION() {
			@Override
			public void exe() {
				invade(200, 0.2);
			}
		});
		
		IDebugPanelSett.add("Invade Huge", new ACTION() {
			@Override
			public void exe() {
				invade(2000, 0.2);
			}
		});
		proj = new Projectile.ProjectileImp(new Json(PATHS.CONFIG().get("DefaultProjectile")), "_DefaultProjectile");
		
		IDebugPanelSett.add("Invasion Finish", new ACTION() {
			@Override
			public void exe() {
				if (active.size() > 0) {
					active.get(0).fastForward();
				}
			}
		});
		
		new DIP.DipActivityListener() {
			
			@Override
			public void change(Faction faction, Faction other, DipStance old, DipStance nn) {
				if (old == DIP.WAR() && nn != DIP.WAR()) {
					FactionNPC o = null;
					if (faction == FACTIONS.player()) {
						o = (FactionNPC) other;
					}else {
						o = (FactionNPC) faction;
					}
					for (int i = 0; i < active.size(); i++) {
						if (active.get(i).spec.fi == o.index()) {
							cancel(active.get(i).spec.ref);
						}
					}
					
				}
			}
		};
	}
	
	private void invade(int amount, double quality) {
		
		if (active.size() == 0) {
			GAME.ARMIES().factors.init(GAME.ARMIES().enemy(), 1.0);
		}
		
		if (!active.hasRoom())
			return;
		
		
		int menPerDivision = amount/Config.battle().DIVISIONS_PER_ARMY;
		if (menPerDivision < 50)
			menPerDivision = 50;
		if (amount < menPerDivision)
			menPerDivision = amount;
		
		int divisions = amount/menPerDivision;
		
		InvasionSpec sp = new InvasionSpec();
		sp.wx =  RND.rInt(1000);
		sp.wy =  RND.rInt(1000);
	
		
		Race race = FACTIONS.player().race();
		DIV_SETTINGImp spec = new DIV_SETTINGImp();
		for (int i = 0; i < divisions; i++) {
			spec.copySettings(GAME.battle().types.rnd(race, FACTIONS.player(), RND.rFloat()), menPerDivision, Math.pow(RND.rFloat(), 1.5), Math.pow(RND.rFloat(), 1.5));
			double experience = quality/2;
			sp.divs.add(make(menPerDivision, race, spec, experience));
		}
		
		active.add(new Invasion(sp));
		
	}
	
	
	private DivGeneration make(int men, Race race, DIV_SETTINGImp spec, double ex) {

		final CharSequence name = race.info.armyNames.rnd();
		final int bannerI = RND.rInt(GAME.ARMIES().banners.size());
		DIV_SPEC dd = new DIV_SPEC() {
			
			@Override
			public double training(StatTraining tr) {
				return spec.training(tr);
			}
			
			@Override
			public double equip(EquipBattle e) {
				return spec.equip(e);
			}
			
			@Override
			public Race race() {
				return race;
			}
			
			@Override
			public int men() {
				return men;
			}
			
			@Override
			public Faction faction() {
				return null;
			}
			
			@Override
			public double experience() {
				return ex;
			}
			
			@Override
			public CharSequence name() {
				return name;
			}

			@Override
			public int bannerI() {
				return bannerI;
			};
		};
		return new DivGeneration(dd, dd);
	}
	
	private int ref = 1;
	
	public int invade(InvasionSpec spec, WArmy a) {
		if (spec.divs.size() <= 0) {
			GAME.Notify("nope");
			return -1;
		}
		
		spec.ref = ref;
		ref ++;
		
		active.add(new Invasion(spec));
		
		for (InvasionListener l : InvasionListener.all) {
			l.register(a, ref);
		}
		
		return spec.ref;
	}
	
	@Override
	protected void update(double ds, Profiler profiler) {
		

		if (active.size() == 0)
			return;
		
		Invasion in = active.get(0);
		
		STANDINGS.CITIZEN().buff.execute(HCLASSES.CITIZEN(), TIME.secondsPerDay()*6);
		
		if (in.update(ds))
			return;
		
		active.removeOrdered(0);
		
		for (ENTITY e : SETT.ENTITIES().getAllEnts()) {
			if (e instanceof Humanoid) {
				Humanoid h = (Humanoid) e;
				if (h.indu().hType() == HTYPES.ENEMY()) {
					h.helloMyNameIsInigoMontoyaYouKilledMyFatherPrepareToDie();
				}
			}
		}
		
		for (ROOM_ARTILLERY b : SETT.ROOMS().ARTILLERY) {
			for (int i = 0; i < b.instancesSize(); i++) {
				ArtilleryInstance ins = b.getInstance(i);
				if (ins.army() == GAME.ARMIES().enemy()) {
					ins.destroyTile(ins.mX(), ins.mY());
					i--;
				}
			}
		}
		
	}
	
	
	
	public boolean invading(FactionNPC f) {
		for (Invasion i : active) {
			if (i.invador() == f)
				return true;
		}
		return false;
	}
	
	public boolean invading() {
		return active.size() > 0;
	}
	
	public boolean invadingPending() {
		if (active.size() > 0) {
			for (int i = 0; i < active.size(); i++)
				if (active.get(i).spec.canBeAttacked)
					return true;
		}
		return false;
	}
	
	@Override
	protected void save(FilePutter file) {
		file.i(active.size());
		for (Invasion i : active) {
			i.save(file);
		}
		file.i(ref);
	}
	
	@Override
	protected void load(FileGetter file) throws IOException {
		active.clear();
		int am = file.i();
		for (int i = 0; i < am; i++)
			active.add(new Invasion(file));
		ref = file.i();
	}
	
	@Override
	protected void clear() {
		active.clear();
	}

	public void hover(GUI_BOX text) {
		GBox b = (GBox) text;
		if (active.size() > 0) {
			b.title(¤¤inv);
			for (Invasion i : active) {
				int men = 0;
				for (DivGeneration s : i.spec.divs)
					men += s.indus.length;
				b.textLL(Dic.¤¤Soldiers);
				b.tab(6);
				b.add(GFORMAT.i(b.text(), men));
				b.NL();
				b.textL(i.spot.dir.perpendicular().getName());
				b.NL(8);
			}	
		}
	}
	
	public InvasionSpec spec(int ref) {
		
		for (Invasion i : active) {
			if (i.spec.ref == ref)
				return i.spec;
		}
		
		return null;
	}

	public void cancel(int ref) {
		for (Invasion i : active) {
			if (i.spec.ref == ref) {
				active.removeOrdered(i);
				return;
			}
		}
	}
	
}
