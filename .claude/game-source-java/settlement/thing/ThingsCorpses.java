package settlement.thing;

import java.io.IOException;
import java.util.Arrays;

import game.GAME;
import game.audio.AUDIO;
import game.audio.SoundRace;
import init.constant.C;
import init.race.Race;
import init.race.appearence.RPortrait;
import init.settings.S;
import init.type.CAUSE_LEAVE;
import init.type.CAUSE_LEAVES;
import init.type.HCLASS;
import init.type.HCLASSES;
import init.type.HTYPES;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.spirte.HCorpseRenderer;
import settlement.main.SETT;
import settlement.path.components.SComponent;
import settlement.path.finders.SFinderFindable;
import settlement.room.main.RoomInstance;
import settlement.room.spirit.grave.GraveData;
import settlement.room.spirit.grave.GraveData.GRAVE_DATA_HOLDER;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import settlement.stats.colls.StatsBurial.StatGrave;
import settlement.thing.DRAGGABLE.DRAGGABLE_HOLDER;
import settlement.thing.THINGS.ThingFactory;
import settlement.thing.ThingsCorpses.Corpse;
import snake2d.CORE;
import snake2d.Renderer;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.datatypes.Rec;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.gui.GuiSection;
import snake2d.util.rnd.RND;
import snake2d.util.sets.LISTE;
import snake2d.util.sprite.SPRITE;
import util.gui.misc.GBox;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.rendering.ShadowBatch;
import util.text.D;
import util.updating.IUpdater;
import view.sett.SETT_HOVERABLE;

public class ThingsCorpses extends ThingFactory<Corpse>{
	
	public final static int MAX = 2048*4;
	private final Corpse[] corpses = new Corpse[MAX];
	private final SpecialHolder holder = new SpecialHolder();
	private static String ¤¤burrial = "¤Burial Site";
	private static String ¤¤onlyMassGrave = "¤This corpse can only be dumped in a mass grave.";
	private static String ¤¤inDanger = "¤This corpse is in a dangerous zone, and will not be fetched.";
	private static String ¤¤noClaim = "¤No one has claimed these remains. Build more burial sites.";
	private final Hov hov = new Hov();
	private static CharSequence ¤¤Cause = "¤Death Cause:";
	private static CharSequence ¤¤Corpse = "¤Remains";
	
	public final SoundRace soundDecay = AUDIO.race("ROTTING_CORPSE");
	
	static {
		D.ts(ThingsCorpses.class);
	}
	private final IUpdater updater = new IUpdater(corpses.length, 100) {

		@Override
		protected void update(int i, double timeSinceLast) {
			if (!corpses[i].isRemoved()) {
				corpses[i].update();
			}
		}
	};
	
	public final DRAGGABLE_HOLDER draggable = new DRAGGABLE_HOLDER() {
		
		@Override
		public DRAGGABLE draggable(short index) {
			return corpses[index];
		}
	};

	
	public ThingsCorpses(LISTE<ThingFactory<?>> all) {
		super(all, MAX);
		for (int i = 0; i < corpses.length; i++) {
			corpses[i] = new Corpse(i);
			
		}
		Arrays.fill(holder.amounts, 0);
		Arrays.fill(holder.firsts, -1);
		Arrays.fill(holder.lasts, -1);
	}
	
	@Override
	protected Corpse[] all() {
		return corpses;
	}
	
	public Corpse create(Humanoid h, boolean intact, CAUSE_LEAVE cause) {
		
		return create(h.indu(), h.body().cX(), h.body().cY(), h.speed.dir(), intact, cause);
	
	}
	
	public Corpse create(Induvidual a, int cx, int cy, DIR d, boolean intact, CAUSE_LEAVE cause) {
		
		if (!cause.leavesCorpse)
			return null;
		
		if (remainingToAdd() == 0)
			return null;
		
		if (SETT.TERRAIN().WATER.DEEP.is(cx>>C.T_SCROLL, cy>>C.T_SCROLL))
			return null;
		
		Corpse c = nextInLine();
		
		if (!c.isRemoved()) {
			throw new RuntimeException();
		}
		
		c.init(a, cx, cy, d, intact, cause);
		return c;
		
	}
	
	public Corpse getByIndex(short index) {
		if (index < 0 || index >= corpses.length)
			return null;
		if (corpses[index].isRemoved())
			return null;
		return corpses[index];
	}
	
	
	public String debug(int index) {
		return corpses[index].isRemoved() + " " + corpses[index].ctx() + " " + corpses[index].cty(); 
	}
	
	
	@Override
	void update(double ds){
		updater.update(ds);
		

	}
	
	@Override
	protected void save(FilePutter file) {
		super.save(file);
	}
	
	@Override
	protected void load(FileGetter file) throws IOException {
		clear();
		super.load(file);

		for (Corpse c : corpses) {
			if (!c.isRemoved()) {
				holder.add(c);
			}
		}
	}
	
	@Override
	protected void clear() {
		for (int i = 0; i < corpses.length; i++)
			corpses[i] = new Corpse(i);
		super.clear();
		Arrays.fill(holder.amounts, 0);
		Arrays.fill(holder.amountsPlayer, 0);
		Arrays.fill(holder.firsts, -1);
		Arrays.fill(holder.lasts, -1);
		
	}
	
	public int nrOfCorpses() {
		return added();
	}
	
	public int amount(CAUSE_LEAVE l) {
		return holder.amount(l);
	}
	
	public Corpse getFirst(CAUSE_LEAVE l) {
		return holder.getFirst(l);
	}
	
	public Corpse getNext(Corpse corpse) {
		return holder.getNext(corpse);
	}
	
	static final Flies flies = new Flies();
	
	final class SpecialHolder {
		
		private final int[] amounts = new int[CAUSE_LEAVES.DEATHS().size()];
		private final int[] amountsPlayer = new int[CAUSE_LEAVES.DEATHS().size()];
		private final int[] firsts = new int[CAUSE_LEAVES.DEATHS().size()];
		private final int[] lasts = new int[CAUSE_LEAVES.DEATHS().size()];
		
		
		void add(Corpse corpse) {
			int i = corpse.cause.indexDeath;
			corpse.sParent = -1;
			corpse.sNext = -1;
			if (firsts[i] == -1) {
				firsts[i] = corpse.index();
				lasts[i] = corpse.index();
			}else {
				corpses[lasts[i]].sNext = corpse.index();
				corpse.sParent = corpses[lasts[i]].index();
				lasts[i] = corpse.index();
			}
			amounts[i] ++;
			if (corpse.indu.player())
				amountsPlayer[i] ++;
		}
		
		void remove(Corpse corpse) {
			
			int i = corpse.cause.indexDeath;
			if (corpse.sParent != -1) {
				corpses[corpse.sParent].sNext = corpse.sNext;
			}
			if (corpse.sNext != -1) {
				corpses[corpse.sNext].sParent = corpse.sParent;
			}
			if (firsts[i] == corpse.index())
				firsts[i] = corpse.sNext;
			if (lasts[i] == corpse.index())
				lasts[i] = corpse.sParent;
			
			corpse.sNext = -1;
			corpse.sParent = -1;
			amounts[i] --;
			if (corpse.indu.player())
				amountsPlayer[i] --;
		}
		
		int amount(CAUSE_LEAVE l) {
			if (!l.death)
				return 0;
			return amountsPlayer[l.indexDeath];
		}
		
		public Corpse getFirst(CAUSE_LEAVE l) {
			if (firsts[l.indexDeath] != -1)
				return corpses[firsts[l.indexDeath]];
			return null;
		}
		
		public Corpse getNext(Corpse corpse) {
			if (corpse.sNext == -1)
				return null;
			return corpses[corpse.sNext];
		}
		
	}
	
	
	static class Flies {
		
		private final int frames = 64;
		private final int flies = 64;
		private final int dim = 32*C.SCALE;
		private final byte[] positions = new byte[frames*flies*2];
		
		Flies(){
			
			for (int f = 0; f < flies; f++) {
				int x = (int) (RND.rFloat0(1)*RND.rFloat()*dim);
				int y = (int) (RND.rFloat0(1)*RND.rFloat()*dim);
				double dx = RND.rFloat()*1 * (RND.rBoolean() ? 1: -1);
				double dy = RND.rFloat()*1 * (RND.rBoolean() ? 1: -1);
				for (int k = 0; k <= frames/2; k++) {
					int i = f*frames*2 + k*2;
					positions[i] = (byte) x;
					positions[i+1] = (byte) y;
					x += RND.rInt0(3) + dx;
					y += RND.rInt0(3) + dy;
					dx += RND.rFloat0(1);
					dy +=  RND.rFloat0(1);
					if (x > dim)
						x -= 4;
					if (y > dim)
						y-= 4;
				}
				for (int k = 1; k < frames/2; k++) {
					int o = f*frames*2 + (frames/2-k)*2;
					int n = f*frames*2 + (frames/2+k)*2;
					positions[n] = positions[o];
					positions[n+1] =  positions[o+1];
				}
			}
			
		}
		
		void render(int ran, int amount, int x, int y) {
			if (amount <= 0)
				return;
			COLOR.WHITE20.bind();
			ran &= flies-1;
			for (int i = 0; i < amount; i++) {
				
				int k = ran*frames*2;
				k += ((GAME.intervals().get20()+i*20) % frames)*2;
				CORE.renderer().renderParticle(x + positions[k], y + positions[k+1]);
				
				ran++;
				ran &= flies-1;
				
			}
			COLOR.unbind();
			
			
		}
		
	}
	
	public static final class Corpse extends ThingFindable implements SETT_HOVERABLE, DRAGGABLE{

		private Induvidual indu;
		private byte direction;
		private float decay;
		private float flyTimer = 0;
		private boolean intact;
		private int ran = RND.rInt();
		private Rec hitbox = new Rec();
		
		private CAUSE_LEAVE cause;
		private short sParent = -1;
		private short sNext = -1;
		private float res;
		
		private boolean claimed;
		private boolean inDangerZone = false;;
		private int burryServiceTile = -1;;
		
		
		Corpse(int index) {
			super(index);
		}
		
		@Override
		protected void save(FilePutter f) {
			indu.save(f);
			f.b(direction);
			f.f(decay);;
			f.f(flyTimer);
			f.bool(intact);
			f.i(ran);
			hitbox.save(f);
			f.bool(claimed);
			f.b((byte) cause.index());
			f.s(sParent);
			f.s(sNext);
			f.f(res);
			f.i(burryServiceTile);
			f.bool(inDangerZone);
		}
		
		@Override
		protected void load(FileGetter f) throws IOException {
			indu = new Induvidual(f);
			direction = f.b();
			decay = f.f();;
			flyTimer = f.f();
			intact = f.bool();
			ran = f.i();
			hitbox.load(f);
			claimed = f.bool();
			cause = CAUSE_LEAVES.ALL().get(f.b());
			sParent = f.s();
			sNext = f.s();
			res = f.f();
			burryServiceTile = f.i();
			inDangerZone = f.bool();
		}
		
		@Override
		public RECTANGLE body() {
			return hitbox;
		}

		@Override
		public void render(Renderer r, ShadowBatch shadows, float ds, int offsetX, int offsetY) {

			boolean inWater = SETT.ENTITIES().submerged.is(ctx(), cty());
			int x = hitbox.x1() + offsetX - indu.race().appearance().off;
			int y = hitbox.y1() + offsetY - indu.race().appearance().off;
			
//			COLOR.GREEN100.render(r, hitbox.x1() + offsetX, hitbox.x1() + offsetX+hitbox.width(), hitbox.y1() + offsetY, hitbox.y1() + offsetY+hitbox.height());
//			
			if (decay > 1.2) {
				HCorpseRenderer.renderSkelleton(indu.race(), indu.hType() != HTYPES.CHILD(), direction, inWater, r, shadows, ran, x, y);
			}else {
				float decay = this.decay > 1 ? 1 : this.decay;
				
				if (!indu.race().physics.decays) {
					decay = 0;
				}
				STATS.NEEDS().DIRTINESS.setD(indu, decay);

				if (intact)
					HCorpseRenderer.renderCorpse(indu, direction, inWater, decay, r, shadows, x, y, 0);
				else
					HCorpseRenderer.renderGore(indu, direction, inWater, decay, r, shadows, x, y);
				
				x = hitbox.cX() + offsetX;
				y = hitbox.cY() + offsetY;
				
				int d = (int)(5*decay);
				if (d > 0) {
					flyTimer += ds*decay;
					if (flyTimer >= 0) {
						this.flyTimer = -RND.rFloat(15f);
						SETT.THINGS().corpses.soundDecay.rnd(indu.race(), hitbox);
					}
					ThingsCorpses.flies.render(ran, d, x, y);
				}
			}
			
		}
		
		void init(Induvidual a, int cx, int cy, DIR d, boolean intact, CAUSE_LEAVE cause) {
			this.indu = a;
			this.intact = intact;
			this.decay = 0;
			this.hitbox.setDim(a.race().physics.hitBoxsize(), a.race().physics.hitBoxsize());
			this.hitbox.moveC(cx, cy);
			this.direction = (byte) d.id();
			this.flyTimer = -RND.rFloat(15f);
			this.claimed = false;
			this.cause = cause;
			this.res = (float) ((intact ? 1.0 : 0.5)*(a.hType() == HTYPES.CHILD() ? 0.25 : 1));
			this.burryServiceTile = -1;
			STATS.APPEARANCE().dead.indu().set(a, 1);
			
			if (!SETT.PATH().reachability.is(ctx(), cty())) {
				for (int di = 0; di < DIR.ALL.size(); di++) {
					if (SETT.PATH().reachability.is(ctx(), cty(), DIR.ALL.get(di))) {
						this.hitbox.incrX(DIR.ALL.get(di).x()*C.TILE_SIZE);
						this.hitbox.incrY(DIR.ALL.get(di).y()*C.TILE_SIZE);
						break;
					}
					
				}
			}
			
//			for (StatGrave g : indu.race().service().GRAVES.get(indu.clas().index())) {
//				g.grave().get(indu.clas()).fail(this, 1);
//			}
			
			add();
			updateDanger();
			reserve();
		}
		
		
		private void reserve() {
			if (findableReservedCanBe() && !isRemoved()) {
				
				HCLASS c = indu.clas() == HCLASSES.CHILD() ? HCLASSES.CITIZEN() : indu.clas();
				for (StatGrave g : indu.race().service().GRAVES.get(c.index())) {
					
					if (g.grave().permission().get(c, indu.race())) {
						burryServiceTile = g.grave().requestAccessCorpse(this);
						if (burryServiceTile != -1) {
							findableReserve();
							break;
						}
					}
				}
			}
		}
		
		@Override
		protected void addAction() {
			super.addAction();
			SETT.THINGS().corpses.holder.add(this);
//			for (StatGrave g : indu.race().service().GRAVES.get(indu.clas().index())) {
//				g.grave().get(indu.clas()).fail(this, -1);
//			}
		}
		
		@Override
		protected void removeAction() {
//			for (StatGrave g : indu.race().service().GRAVES.get(indu.clas().index())) {
//				g.grave().get(indu.clas()).fail(this, 1);
//			}
			SETT.THINGS().corpses.holder.remove(this);
			super.removeAction();
		}


		
		
		@Override
		public void hover(GBox box) {
			SETT.THINGS().corpses.hov.cause = cause;
			SETT.THINGS().corpses.hov.indu = indu;
			box.add(SETT.THINGS().corpses.hov);
			box.NL();
			
			
			box.NL(C.SG*2);
			RoomInstance r = getSpot();
			if (r != null) {
				box.textL(¤¤burrial);
				box.text(r.name());
			}else if(inDangerZone) {
				box.NL(8);
				box.text(¤¤inDanger);
			}else if(indu().clas() != HCLASSES.CITIZEN() && indu().clas() != HCLASSES.CHILD()) {
				box.NL(8);
				box.text(¤¤onlyMassGrave);
			}
			
			if (findableReservedCanBe()) {
				box.NL(8);
				box.NL(8);
				box.text(¤¤noClaim);
			}
			
			if (S.get().developer) {
				box.add(box.text().add(findableReservedIs()));
			}
		}
		@Override
		public boolean canBeClicked() {
			return false;
		}
		
		@Override
		public void click() {
			
		}

		@Override
		protected int z() {
			return 99;
		}
		
		private RoomInstance getSpot() {
			if (claimed && burryServiceTile != -1) {
				RoomInstance r = SETT.ROOMS().map.instance.get(burryServiceTile);
				if (r != null && r.blueprint() instanceof GraveData.GRAVE_DATA_HOLDER) {
					GraveData.GRAVE_DATA_HOLDER h = (GRAVE_DATA_HOLDER) r.blueprint();
					if (h.graveData().hasAccessCorpse(burryServiceTile, this)) {
						return r;
					}
				}
			}
			return null;
		}
		
		void update() {
			
			if (decay == 0 && STATS.NEEDS().INJURIES.COUNT.indu().getD(indu) > RND.rFloat()) {
				SETT.THINGS().gore.drops.make(body().cX(), body().cY(), 0, 0, ColorImp.TMP.set(indu.race().appearance().colors.blood).shadeSelf(0.5 + 0.5*(0.4-decay)));
			}
			
			float d = 0.05f;
			
			decay += d*RND.rFloat();
			if (decay > 2.5f && !findableReservedIs() || decay > 20f) {
				HCLASS c = indu.clas() == HCLASSES.CHILD() ? HCLASSES.CITIZEN() : indu.clas();
				for (StatGrave g : indu.race().service().GRAVES.get(c.index())) {
					g.grave().get(c).fail(this, 1);
				}
				remove();
			}
			if (inDangerZone)
				updateDanger();
			reserve();
		}
		
		private boolean inDangerZone() {
			int dist = SETT.PATH().comps.zero.size();
			for (DIR d : DIR.ALLC) {
				SComponent c = SETT.PATH().comps.zero.get(ctx()+d.x()*dist, cty()+d.y()*dist);
				if (c != null) {
					if (SETT.PATH().comps.data.people(false).get(c) > 0)
						return true;
					if (SETT.PATH().comps.data.reservableAnimals.get(c) > 0)
						return true;
				}
			}
			return false;
		}
		

		@Override
		public boolean findableReservedCanBe() {
			return !claimed && !inDangerZone;
		}

		@Override
		public boolean findableReservedIs() {
			return claimed;
		}

		@Override
		public void findableReserveCancel() {
			super.findableReserveCancel();
			updateDanger();
		}
		
		@Override
		public int x() {
			return ctx();
		}

		@Override
		public int y() {
			return cty();
		}

		private void updateDanger() {
			boolean dan = inDangerZone();
			if (this.inDangerZone == dan)
				return;
			if (findableReservedCanBe())
				finder().report(this, -1);
			this.inDangerZone = dan;
			if (findableReservedCanBe())
				finder().report(this, 1);
			
		}
		
		@Override
		protected void reserve(int d) {
			if (d == -1 && claimed) {
				claimed = false;
				burryServiceTile = -1;
			}
			else if (d == 1 && !claimed)
				claimed = true;
			else
				throw new RuntimeException(d + " " + claimed);
		}

		@Override
		public SFinderFindable finder() {
			return SETT.PATH().finders.corpses;
		}
		
		@Override
		public void drag(DIR d, int cx, int cy, int fromDist) {
			if (!claimed)
				throw new RuntimeException();
			direction = (byte) d.perpendicular().id();
			hitbox.moveC(cx-fromDist*d.xN(), cy-fromDist*d.yN());
			if (hitbox.cX() < 0)
				hitbox.moveCX(0);
			if (hitbox.cX() >= SETT.PIXEL_BOUNDS.x2())
				hitbox.moveCX( SETT.PIXEL_BOUNDS.x2()-1);
			if (hitbox.cY() < 0)
				hitbox.moveCY(0);
			if (hitbox.cY() >= SETT.PIXEL_BOUNDS.y2())
				hitbox.moveCY( SETT.PIXEL_BOUNDS.y2()-1);
			super.move();
		}
		
		@Override
		public void drag(DIR d, int cx, int cy) {
			drag(d, cx, cy, body().width());
		}
		
		public boolean hasMeat() {
			return decay < 2;
		}
		
		public void removeMeat() {
			if (decay < 2)
				decay = 2;
		}
		
		@Override
		public ThingFactory<?> factory() {
			return SETT.THINGS().corpses; 
		}
		
		public CAUSE_LEAVE cause() {
			return cause;
		}
		
		public Induvidual indu() {
			return indu;
		}

		public double resLeft() {
			return res * (1.0-decay);
		}
		
		public void resRemove() {
			this.res -= 0.25;
			//STATS.NEEDS().INJURIES.indu().incD(indu(), 0.25);
		}

		@Override
		public boolean canBeDragged() {
			return claimed && !isRemoved();
		}
		
		public Race race() {
			return indu.race();
		}
		
	}

	private final class Hov extends GuiSection {
		
		private Induvidual indu;
		private CAUSE_LEAVE cause;
		
		Hov(){
			addRightC(0, new GStat() {
				
				@Override
				public void update(GText text) {
					text.color(COLOR.WHITE85);
					text.add(indu.race().info.namePosessive).s().add(¤¤Corpse);
				}
			}.increase());
			
			add(new GStat() {
				
				@Override
				public void update(GText text) {
					text.color(COLOR.WHITE85);
					int age = (int) Math.ceil(STATS.POP().age.years.getD(indu));
					text.clear().add(STATS.APPEARANCE().name(indu)).add(',').s().add(age).add(',').s().add(indu.hType().name);
	
					
					
				}
			}, 0, body().y2()+2);
			
			addDown(2, new GStat() {
				
				@Override
				public void update(GText text) {
					text.color(COLOR.WHITE85);
					text.add(¤¤Cause).s().add(cause.name);
				}
			});
			
			addRelBody(8, DIR.W, new SPRITE.Imp(RPortrait.P_WIDTH*2, RPortrait.P_HEIGHT*2) {
				@Override
				public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
					STATS.APPEARANCE().portraitRender(r, indu, X1, Y1, 2);
					OPACITY.O25.bind();
					COLOR.BLACK.render(r, X1, X2, Y1, Y2);
					OPACITY.unbind();
				}
			});
			
			body().setWidth(500);
		}
		
		
	}
	


	
}
