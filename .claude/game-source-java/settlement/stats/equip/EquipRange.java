package settlement.stats.equip;

import java.io.IOException;
import java.util.Arrays;

import game.GAME;
import game.battle.div.Div;
import game.boosting.BOOSTABLES;
import game.boosting.BOOSTING;
import game.boosting.Boostable;
import game.time.TIME;
import init.constant.C;
import init.constant.Config;
import init.paths.PATH;
import init.sprite.UI.UI;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import settlement.stats.StatsInit;
import settlement.stats.StatsInit.StatUpdatable;
import settlement.thing.projectiles.Projectile;
import settlement.thing.projectiles.SProjectiles;
import settlement.thing.projectiles.Trajectory;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.Json;
import snake2d.util.file.SAVABLE;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.rnd.RND;
import snake2d.util.sets.KeyMap;
import snake2d.util.sets.LISTE;
import snake2d.util.sprite.TILE_SHEET;
import util.gui.misc.GBox;
import util.info.GFORMAT;
import util.text.D;
import util.text.Dic;
import util.text.DicTime;
import util.updating.IUpdater;

public class EquipRange extends EquipBattle {

	public final Projectile projectile;
	public final int ammoMax;
	public final double ammoReplenishHours;
	public final short tIndex;

	private final double[] drawInters = new double[Config.battle().DIVISIONS_PER_BATTLE];
	private final int[] drawIntersI = new int[Config.battle().DIVISIONS_PER_BATTLE];
	private final double[] ammoWasted = new double[Config.battle().DIVISIONS_PER_BATTLE];
	public final Boostable boostable;
	
	private static CharSequence ¤¤ammoC = "Ammunition (Current)";
	private static CharSequence ¤¤ammoR = "Am. Replenish Time (hours)";
	private static CharSequence ¤¤ammoU = "Unlimited ammunition!";
	
	static {
		D.ts(EquipRange.class);
	}

	EquipRange(String key, PATH path, LISTE<Equip> all, LISTE<EquipRange> type, LISTE<EquipBattle> mil, StatsInit init, KeyMap<TILE_SHEET> spriteMap)
			throws IOException {
		super("RANGED", key, path, all, mil, init, spriteMap);
		tIndex = (short) type.add(this);
		Json data = new Json(path.get(key));

		ammoMax = data.i("AMMUNITION_AMOUNT", 1, 255);
		ammoReplenishHours = data.d("AMMUNITION_REPLENISH_TIME_HOURS");
		
		
		boostable = BOOSTING.push("RANGED_" + key, 0.1, Dic.¤¤Skill + ": " + resource.name,
				Dic.¤¤Skill + ": " + resource.name, resource.icon(), BOOSTABLES.BATTLE());
		projectile = new Projectile.ProjectileImp(data, "EQUIP_" + key);
		
		IUpdater up = new IUpdater(Config.battle().DIVISIONS_PER_BATTLE, 10.0) {
			
			@Override
			protected void update(int i, double timeSinceLast) {
				Div d = GAME.ARMIES().division((short) i);
				if (d.menNrOf() == 0) {
					ammoWasted[i] = 0;
					return;
				}
				
				int men = d.men();
				
				int m = men*ammoMax;
				if (ammoWasted[i] > m)
					ammoWasted[i] = m;
				
				
				if (d.player() && GAME.ARMIES().enemy().men() == 0) {
					ammoWasted[i] -= men*timeSinceLast*TIME.secondsPerDayI()*TIME.hoursPerDay()/ammoReplenishHours;
					if (ammoWasted[i] < 0)
						ammoWasted[i] = 0;
				}
				
			}
		};
		
		init.upers.add(new StatUpdatable() {
			
			@Override
			public void update(double ds) {
				up.update(ds);
			}
		});
		
		init.savers.put("EQUIP_AMMO_" + key, new SAVABLE() {
			
			@Override
			public void save(FilePutter file) {
				up.save(file);
				file.ds(ammoWasted);
			}
			
			@Override
			public void load(FileGetter file) throws IOException {
				up.load(file);
				file.ds(ammoWasted);
				
			}
			
			@Override
			public void clear() {
				up.clear();
				Arrays.fill(ammoWasted, 0.0);
				
			}
		});
		
		

	}
	
	public double ammoD(Div div) {
		if (div.men() == 0)
			return 0;
		double m = div.men()*ammoMax;
		double a = ammoWasted[div.index()];
		if (a > m)
			a = m;
		return (m-a)/m;
	}
	
	public double ammoPerMan(Div div) {
		return ammoD(div)*ammoMax;
	}
	
	public void ammoClear(Div div) {
		ammoWasted[div.index()] = 0;
	}
	

	public double ref(Induvidual a) {
		 return ref((double)get(a)/equipMax, boostable.get(a));
	}

	public double ref(Div div) {
		return ref((double)stat.div().getD(div), boostable.get(div));
	}

	public double ref(double equip, double skill) {
		return equip*(0.2 + equip*0.8)*skill;
	}

	public void launch(Humanoid a, Trajectory j) {
		double ref = ref(a.indu());
		double ran = 1.0 - projectile.accuracy(ref);
		int x = a.body().cX() + a.speed.dir().x() * C.TILE_SIZEH;
		int y = a.body().cY() + a.speed.dir().y() * C.TILE_SIZEH;
		int h = SProjectiles.releaseHeight(a.tc().x(), a.tc().y());
		SETT.PROJS().launch(x, y, h, j, a.division().settings().ammo().projectile, ran, ref, a);
		
		if (ammoReplenishHours > 0)
			ammoWasted[a.division().index()]++;
		
		double dex = (double)2.0*STATS.NEEDS().EXHASTION.indu().max(a.indu())/ammoMax;
		int ex = (int) dex;
		if (dex-ex > RND.rFloat())
			ex++;
		STATS.NEEDS().EXHASTION.indu().inc(a.indu(), ex);
//		ex += STATS.NEEDS().EXHASTION.indu().get(a.indu());
//		ex = CLAMP.i(ex, 0, STATS.NEEDS().EXHASTION.indu().max(a.indu())-1);
//		STATS.NEEDS().EXHASTION.indu().set(a.indu(), ex);
		
	}

	public double drawInter(Div div) {
		if ((GAME.updateI() & ~0b011) != drawIntersI[div.index()]) {
			double reloadSeconds = projectile.reloadSeconds(ref(div));
			double t = TIME.currentSecond();
			double inter = reloadSeconds;
			;
			double tt = t / inter;
			drawInters[div.index()] = tt - (int) tt;
			;
			drawIntersI[div.index()] = GAME.updateI() & ~0b011;
		}

		return drawInters[div.index()];
	}

	@Override
	public void hover(GUI_BOX box) {

		super.hover(box);
		GBox b = (GBox) box;
		b.sep();
		projectile.hover(box, resource.name);
		b.NL(8);
		if (ammoReplenishHours > 0) {
			b.textL(Dic.¤¤Ammunition);
			b.tab(6);
			b.add(GFORMAT.i(b.text(), ammoMax));
			b.tab(8);
			b.add(UI.icons().s.clock);
			b.add(GFORMAT.f(b.text(), ammoReplenishHours));
			b.text(DicTime.¤¤Hours);
		}
		b.NL(8);
	}

	private void hover(GUI_BOX box, double ref, double ammo) {

		
		GBox b = (GBox) box;
		b.sep();
		projectile.hover(box, resource.name, ref, 0);
		b.NL(8);
		
		
		
		b.sep();
		
		if (ammoReplenishHours > 0) {
			
			b.textLL(¤¤ammoC);
			b.tab(7);
			b.add(GFORMAT.fofkInv(b.text(), ammo, ammoMax));
			b.NL();
			
			b.textL(¤¤ammoR);
			b.tab(7);
			b.add(GFORMAT.f(b.text(), ammoReplenishHours));

		}else {
			b.textLL(¤¤ammoU);
		}
	}

	@Override
	public void hover(GUI_BOX box, Div div) {
		super.hover(box, div);
		hover(box, ref(div), ammoPerMan(div));
		box.NL();
		boostable.hoverDetailed(box, div, "", true);
	}

	@Override
	public void hover(GUI_BOX box, Induvidual i) {
		super.hover(box, i);
		hover(box, ref(i), ammoMax);

	}

}