package settlement.thing.projectiles;

import java.io.IOException;

import game.GAME;
import game.GameDisposable;
import game.audio.AUDIO;
import game.audio.SoundRace;
import game.boosting.BOOSTABLES;
import game.boosting.BOOSTABLES.BDamage;
import init.constant.C;
import init.sprite.SPRITES;
import settlement.entity.ENTITY;
import settlement.main.SETT;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.Rec;
import snake2d.util.datatypes.VectorImp;
import snake2d.util.file.Json;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayListResize;
import snake2d.util.sets.INDEXED;
import util.gui.misc.GBox;
import util.gui.misc.GText;
import util.info.GFORMAT;
import util.rendering.ShadowBatch;
import util.text.D;
import util.text.Dic;

public abstract class Projectile implements INDEXED{

	static final ArrayListResize<Projectile> ALL = new ArrayListResize<>(8, Short.MAX_VALUE);
	static {
		new GameDisposable() {
			
			@Override
			protected void dispose() {
				ALL.clear();
			}
		};
	}
	
	private static CharSequence ¤¤name = "¤Projectile";
	private static CharSequence ¤¤splashDamage = "¤Splash Damage (tiles)";
	private static CharSequence ¤¤Range = "¤Range (tiles)";
	private static CharSequence ¤¤Accuracy = "¤Accuracy";
	private static CharSequence ¤¤Reload = "¤Reload (seconds)";
	private static CharSequence ¤¤Arch = "¤Max angle (degrees)";
	
	static {
		D.ts(Projectile.class);
	}
	
	
	public final short index;
	

	public Projectile() {
		
		if (!ALL.hasRoom())
			throw new RuntimeException("Too many projectiles declared! max is " + Short.MAX_VALUE);
		index = (byte) ALL.add(this);
	}
	
	@Override
	public int index() {
		return index;
	}
	
	
	public final double range(int height, double ref) {
		return Trajectory.range(height, maxAngle(ref), velocity(ref));
	}
	
	public abstract double velocity(double ref);
	
	public abstract double maxAngle(double ref);
	
	public final double bluntDamage(double ref) {
		return mass(ref)*velocity(ref)*C.ITILE_SIZE;
	}
	
	public abstract double reloadSeconds(double ref);
	
	public abstract double accuracy(double ref);
	
	public abstract double skill(double ref);
	
	public abstract double mass(double ref);

	public abstract double damage(int battleI, double ref);

	public abstract double areaAttack(double ref);
	
	public abstract SoundRace soundRelease();
	
	public abstract SoundRace soundHit();
	
	public abstract ProjectileSprite sprite();
	
	
	
	public static void renderArrow(Renderer r, ShadowBatch s, double x, double y, int h, int ran, double dx, double dy, double dz, int zoomout) {
		if (zoomout < 2) {
			double l = Math.sqrt(dx*dx+dy*dy+dz*dz*4);
			dx /= l;
			dy /= l;
			dx*= C.SCALE;
			dy*= C.SCALE;
			for (int k = 0; k < 8; k++) {
				r.renderParticle((int)x, (int)(y));
				x += dx;
				y += dy;
			}
		}
		s.setHeight(0);
		s.setDistance2Ground(h/4);
		SPRITES.icons().s.dot.renderC(s, (int)x, (int)y);
	}
	
	private static final Rec pixels = new Rec();
	private static final VectorImp tVec = new VectorImp();
	private static final VectorImp sVec = new VectorImp();
	
	public void impact(double ref, double cx, double cy, double dx, double dy, double dz) {
		double areaAttack = areaAttack(ref);
		if (areaAttack <= 0)
			return;
		
		if (dx == 0 && dy == 0 && dx == 0)
			return;
		
		double mass = mass(ref);
		double speed = Math.sqrt(dx*dx+dy*dy+dz*dz);
		double mom = mass*speed;
		
		SETT.THINGS().gore.debris((int)cx, (int)cy, dx*0.5, dy*0.5);
		SETT.GRASS().current.increment((int)cx, (int)cy, -0.5);
		
		sVec.set(dx, dy);
		dx = sVec.nX();
		dy = sVec.nY();
		
		pixels.setDim(areaAttack*2);
		pixels.moveC(cx, cy);
		
		for (ENTITY e : SETT.ENTITIES().fill(pixels)) {
			double l = tVec.set(cx, cy, e.body().cX(), e.body().cY());
			if (l > areaAttack)
				continue;
			l = 1.0 - (l / areaAttack);
			if (l < 0)
				continue;
			
			tVec.set(tVec.nX() + sVec.nX()*(0.1+RND.rFloat()) + RND.rFloat0(0.1), tVec.nY() + sVec.nY()*(0.1+RND.rFloat()) + RND.rFloat0(0.1));
			
			GAME.battle().fight.projectileAttack(e, tVec.nX(), tVec.nY(), speed, this, ref);
			
		}
		
		for (int tdy = (int) -areaAttack; tdy <= areaAttack; tdy+=C.TILE_SIZE) {
			for (int tdx = (int) -areaAttack; tdx <= areaAttack; tdx+=C.TILE_SIZE) {
				int x = (int) (cx + tdx);
				int y = (int) (cy + tdy);
				double l = tVec.set(cx, cy, x, y);
				if (l > areaAttack)
					continue;
				l = 1.0 - (l / areaAttack);
				l*= mom*l;
				double str = GAME.ARMIES().map.strength.get(x>>C.T_SCROLL, y>>C.T_SCROLL);
				if (l*RND.rFloat() > str) {
					GAME.ARMIES().map.breakIt(x>>C.T_SCROLL, y>>C.T_SCROLL);
				}
			}
		}
		
		
	}
	
	public void hover(GUI_BOX box, CharSequence name, double ref, int height) { 
		GBox b = (GBox) box;
		
		if (name != null) {
			b.add(b.text().lablify().add(¤¤name).add(':').s().add(name));
			b.NL();
		}
		
		b.add(b.text().add(ref));
		b.NL();
		
		hov(b, ¤¤Range, range(height, ref)/C.TILE_SIZE);
		hov(b, ¤¤Accuracy, GFORMAT.perc(b.text(), accuracy(ref)));
		hov(b, BOOSTABLES.BATTLE().DEXTERITY.name, skill(ref));
		hov(b, ¤¤Reload, reloadSeconds(ref));
		hov(b, ¤¤Arch, maxAngle(ref));
		hov(b, ¤¤splashDamage, areaAttack(ref));
		hov(b, BOOSTABLES.BATTLE().BLUNT_ATTACK.name, bluntDamage(ref));
		
		for (BDamage pp : BOOSTABLES.BATTLE().DAMAGES) {
			hov(b, pp.attack.name, damage(pp.index(), ref));
		}
	}
	
	public void hover(GUI_BOX box, CharSequence name) { 
		GBox b = (GBox) box;
		
		if (name != null) {
			b.add(b.text().lablify().add(¤¤name).add(':').s().add(name));
			b.tab(6);
			b.textL(Dic.¤¤From);
			b.tab(8);
			b.textL(Dic.¤¤To);
			
			b.NL();
		}
		
		hov(b, ¤¤Range, range(0, 0)/C.TILE_SIZE, range(0, 1)/C.TILE_SIZE);
		hov(b, BOOSTABLES.BATTLE().DEXTERITY.name, skill(0), skill(1));
		hov(b, ¤¤Accuracy, accuracy(0), 1);
		hov(b, ¤¤Reload, reloadSeconds(0), reloadSeconds(1));
		hov(b, ¤¤Arch, maxAngle(0), maxAngle(1));
		hov(b, ¤¤splashDamage, areaAttack(0), areaAttack(1));
		hov(b, BOOSTABLES.BATTLE().BLUNT_ATTACK.name, bluntDamage(0), bluntDamage(1));
		
		for (BDamage pp : BOOSTABLES.BATTLE().DAMAGES) {
			hov(b, pp.attack.name, damage(pp.index(), 0), damage(pp.index(), 1));
		}
	}
	
	private static void hov(GBox b, CharSequence name, double v) {
		GText t = b.text();
		GFORMAT.f(t, v);
		if (v == 0)
			t.color(COLOR.WHITE50);
		hov(b, name, t);
	}
	
	private static void hov(GBox b, CharSequence name, GText vv) {
		b.textL(name);
		b.tab(6);
		b.add(vv);
		b.NL();
	}
	
	private static void hov(GBox b, CharSequence name, double from, double to) {
		b.textL(name);
		b.tab(6);
		GText t = b.text();
		GFORMAT.f(t, from);
		if (from == 0)
			t.color(COLOR.WHITE50);
		b.add(t);
		
		b.tab(8);
		t = b.text();
		GFORMAT.f(t, to);
		if (to == 0)
			t.color(COLOR.WHITE50);
		b.add(t);
		b.NL();
	}
	
	public static final class ProjectileSpec {
		
		public double maxAngle;
		public double velocity;
		public double accuracy;
		public double dexterity;
		public double reloadSpeed;
		
		public double mass,areaAttack;
		public double[] damage = new double[BOOSTABLES.BATTLE().DAMAGES.size()];
		
		public ProjectileSpec(Json json) {
			if (json.has("PROJECTILE"))
				json = json.json("PROJECTILE");
			mass = json.d("MASS", 0.01, 100000);
			velocity = json.d("TILE_SPEED", 0.5, 250)*C.TILE_SIZE;
			
			reloadSpeed = json.d("RELOAD_SECONDS", 0.01, 10000);
			
			accuracy = json.d("ACCURACY", 0.01, 1);
			dexterity = json.d("DEXTERITY", 0, 10000000);
			BOOSTABLES.BATTLE().DAMAGE_COLL.readFill(damage, json, 0, 100000);
			areaAttack = json.dTry("TILE_RADIUS_DAMAGE", 0, 10000, 0)*C.TILE_SIZE;
			maxAngle = json.d("MAX_ARCH_ANGLE_DEGREES", 0, 75);
		}
		
	}
	
	public static final class ProjectileImp extends Projectile {
		
		private final ProjectileSpec from;
		private final ProjectileSpec delta;
		private final ProjectileSprite sprite;
		public final SoundRace soundRelease;
		public final SoundRace soundHit;
		
		public ProjectileImp(Json data, String key) throws IOException {
			if (data.has("PROJECTILE"))
				data = data.json("PROJECTILE");
			
			sprite = ProjectileSprite.get(data);
			soundRelease = AUDIO.race("PROJECTILE_RELEASE_" + key);
			soundHit = AUDIO.race("PROJECTILE_HIT_"+ key);
			from = new ProjectileSpec(data.json("FROM"));
			delta = new ProjectileSpec(data.json("TO"));

			delta.accuracy-= from.accuracy;
			delta.dexterity -= from.dexterity;
			delta.areaAttack -= from.areaAttack;
			delta.mass -= from.mass;
			delta.maxAngle -= from.maxAngle;
			delta.reloadSpeed -= from.reloadSpeed;
			delta.velocity -= from.velocity;
			
			for (BDamage d : BOOSTABLES.BATTLE().DAMAGES)
				delta.damage[d.index()] -= from.damage[d.index()];

		}
		
		@Override
		public double mass(double ref) {
			return from.mass + delta.mass*ref;
		}
		
		@Override
		public double damage(int battleI, double ref) {
			return from.damage[battleI]+delta.damage[battleI]*ref;			
		}
		
		@Override
		public double areaAttack(double ref) {
			return from.areaAttack + delta.areaAttack*ref;
		}

		@Override
		public double velocity(double ref) {
			return from.velocity + delta.velocity*ref;
		}

		@Override
		public double maxAngle(double ref) {
			ref = CLAMP.d(ref, 0, 1);
			return from.maxAngle + delta.maxAngle*ref;
		}

		@Override
		public double reloadSeconds(double ref) {
			ref = CLAMP.d(ref, 0, 1);
			return from.reloadSpeed + delta.reloadSpeed*ref;
		}

		@Override
		public double accuracy(double ref) {
			return CLAMP.d(from.accuracy + delta.accuracy*ref, 0, 1);
		}

		@Override
		public double skill(double ref) {
			return from.dexterity + delta.dexterity*ref;
		}
		
		@Override
		public SoundRace soundRelease() {
			return soundRelease;
		}

		@Override
		public SoundRace soundHit() {
			return soundHit;
		}

		@Override
		public ProjectileSprite sprite() {
			return sprite;
		}



	}
	


	
}
