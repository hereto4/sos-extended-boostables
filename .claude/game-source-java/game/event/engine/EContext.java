package game.event.engine;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;

import game.event.actions.EventActionContext;
import game.event.engine.ESelection.ESelectionType;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.npc.FactionNPC;
import game.faction.royalty.NPCCourt;
import game.faction.royalty.Royalty;
import init.race.appearence.RPortrait;
import init.type.HTYPES;
import init.value.Lockable;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.Coo;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.text.Str;
import util.data.GETTER_TRANS;
import util.text.INSERT;
import util.text.Inserter;
import world.WORLD;
import world.map.regions.Region;
import world.region.RD;

public final class EContext implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public double random;
	public int[] actionContext = new int[16];

	public final Coo coo = new Coo(-1, -1);
	public double actionAmount;
	
	public final SelContext<Induvidual> indu = new SelIndu(); 
	public final SelContext<Region> regs = new SelReg(); 
	public final SelContext<Faction> faction = new SelFac(); 
	public final SelContext<Royalty> royalty = new SelRoy(); 
	
	
	private final LIST<SelContext<?>> sels = new ArrayList<>(indu, regs, faction, royalty);
	
	public ColorImp colorinduAll = null;
	public ColorImp colorIndu = null;

	
	public boolean init(Event abs) {
		coo.set(-1,-1);
		actionAmount = 0;
		colorinduAll = null;
		colorIndu = null;
		random = RND.rFloat();
		actionContext = EventActionContext.makeData(abs, actionContext);
		EventActionContext.setData(abs, this);
		
		for (SelContext<?> c : sels){
			if (!c.init(abs))
				return false;
		}
		
		return true;
	}
	
	public void initLight(Event abs) {
		actionContext = EventActionContext.makeData(abs, actionContext);
		EventActionContext.setData(abs, this);
	}

	public static EContext read(FileGetter file) throws IOException {
		EContext c = (EContext) file.object(true);
		return c;
	}	
	public void write(FilePutter file) {
		file.object(this);
	}

	public EContext() {

	}
	
	public EContext(EContext data) {
		this.coo.set(data.coo);
		this.actionAmount = data.actionAmount;
		this.random = data.random;
		this.colorinduAll = data.colorinduAll;
		this.colorIndu = data.colorIndu;
		if (data.actionContext == null)
			actionContext = new int[16];
		else {
			this.actionContext = Arrays.copyOf(data.actionContext, data.actionContext.length);
		}
		indu.copy(data.indu);
		regs.copy(data.regs);
		faction.copy(data.faction);
		royalty.copy(data.royalty);
		
	}
	
	
	final static Inserter<EContext> insert = new Inserter<>();

	public String insert(CharSequence mess) {
		Str srt = new Str(mess);
		insert.set(srt, this);
		return ""+srt;
	}
	
	static {
		
		insert.join(INSERT.player, new GETTER_TRANS<EContext, Integer>(){

			@Override
			public Integer get(EContext f) {
				return (int) (f.random*Integer.MAX_VALUE);
			}
			
		});
		insert.join(new Inserter<Faction>(INSERT.faction, "PLAYER_"), new GETTER_TRANS<EContext, Faction>(){

			@Override
			public Faction get(EContext f) {
				return FACTIONS.player();
			}
			
		});
		
		insert.new II("SELECTED_AMOUNT") {

			@Override
			public void set(EContext t, Str str) {
				int am = 0;
				for (SelContext<?> s : t.sels)
					am += s.am;
				str.add(am);
			}
			
		};
		
		insert.new II("ACTION_AMOUNT_I") {

			@Override
			public void set(EContext t, Str str) {
				str.add((int)t.actionAmount);
			}
			
		};
		
		insert.new II("ACTION_AMOUNT_F") {

			@Override
			public void set(EContext t, Str str) {
				str.add(t.actionAmount);
			}
			
		};
		
		insert.join(new Inserter<Induvidual>(INSERT.indu, "SUBJECT1_"), new GETTER_TRANS<EContext, Induvidual>(){

			@Override
			public Induvidual get(EContext f) {
				return f.indu.first();
			}
			
		});
		
		insert.join(new Inserter<Induvidual>(INSERT.indu, "SUBJECT2_"), new GETTER_TRANS<EContext, Induvidual>(){

			@Override
			public Induvidual get(EContext f) {
				return f.indu.second();
			}
			
		});
		
		insert.join(new Inserter<Region>(INSERT.reg, "REGION1_"), new GETTER_TRANS<EContext, Region>(){

			@Override
			public Region get(EContext f) {
				return f.regs.first();
			}
			
		});
		
		insert.join(new Inserter<Region>(INSERT.reg, "REGION2_"), new GETTER_TRANS<EContext, Region>(){

			@Override
			public Region get(EContext f) {
				return f.regs.second();
			}
			
		});

		insert.join(new Inserter<Faction>(INSERT.faction, "FACTION1_"), new GETTER_TRANS<EContext, Faction>(){

			@Override
			public Faction get(EContext f) {
				return f.faction.first() == null ? FACTIONS.player() : f.faction.first();
			}
			
		});
		
		insert.join(new Inserter<Faction>(INSERT.faction, "FACTION2_"), new GETTER_TRANS<EContext, Faction>(){

			@Override
			public Faction get(EContext f) {
				return f.faction.second();
			}
			
		});
		
		insert.join(new Inserter<Royalty>(INSERT.royalty, "ROYALTY1_"), new GETTER_TRANS<EContext, Royalty>(){

			@Override
			public Royalty get(EContext f) {
				return f.royalty.first();
			}
			
		});
		
		insert.join(new Inserter<Royalty>(INSERT.royalty, "ROYALTY2_"), new GETTER_TRANS<EContext, Royalty>(){

			@Override
			public Royalty get(EContext f) {
				return f.royalty.second();
			}
			
		});
		
	}
	
	public static abstract class SelContext<T> implements Serializable{
		

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		public int am = 0;
		public int max;
		public int min;
		
		private static int eMark = 0;
		private static int eClear = 0; 
		private static int eFilter = 0;
		
		private SelContext(String name) {

		}
		
		
		public abstract T first();
		
		public abstract T second();
		
		boolean init(Event abs) {
			am = 0;
			ESelectionType<T> ss = sel(abs);
			eMark = ss.mark.mark ? abs.allIndex + 1 : 0;
			eClear = 0; 
			if (ss.mark.clear != null) {
				for (Event e : Event.all) {
					if (e.key.equals(ss.mark.clear)) {
						eClear = e.allIndex+1;
						break;
					}
				}
			}
			eFilter = 0;
			if (ss.mark.filter != null) {
				for (Event e : Event.all) {
					if (e.key.equals(ss.mark.filter)) {
						eFilter = e.allIndex+1;
						break;
					}
				}
			}
			
			int maxAm = max();
			
			if (maxAm <= 0)
				return true;

			
			for (int ei = 0; ei < maxAm; ei++) {
				T e = get(ei);
				if (e != null) {
					eventSet(e, 0);
					if (eClear != 0 && eClear == markGet(e)) {
						markSet(e, 0);
					}
					
					for (Lockable<T> l : ss.filters) {
						if (l.passes(e)) {
							
							am ++;
							break;
						}
					}
				}
			}
			max = ss.max.am(tot(), am);
			min = ss.min.am(tot(), am);
			
			if (am < min)
				return false;
			
			am = 0;
			
			T first = null;
			T second = null;
			
			int ri = RND.rInt(maxAm);
			
			
			
			for (int ii = 0; ii < maxAm; ii++) {
				int i = ii+ri;
				i %= maxAm;
				
				T e = get(i);
				
				if (e == null)
					continue;
				
				if (eFilter != 0) {
					if (markGet(e) != eFilter)
						continue;
				}
				
				for (Lockable<T> l : ss.filters) {
					if (l.passes(e)) {
						if (am == 0) {
							first = e;
						}else if (am == 1) {
							second = e;
						}
						am ++;
						eventSet(e, 1);
						
						if (eMark != 0)
							markSet(e, eMark);
						
						if (am >= max) {
							set(first, second);
							return true;
						}
						break;
					}
				}

				
			}
			set(first, second);
			return true;
		}
		
		abstract void set(T first, T second);
		
		abstract int max();
		int tot() {
			return max();
		}
		abstract T get(int index);
		
		abstract void markSet(T t, int id);
		abstract int markGet(T t);
		
		abstract void eventSet(T t, int b);
		abstract boolean eventGet(T t);
		
		void copy(SelContext<T> o) {
			am = o.am;
			max = o.max;
			min = o.min;
			set(o.first(), o.second());
		}
		
		abstract ESelectionType<T> sel(Event abs);
		
		abstract SPRITE sprite();
		
		abstract Class<?> cl();
		
	}
	
	public static class SelIndu extends SelContext<Induvidual> {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private Induvidual i1;
		private Induvidual i2;
		
		private SelIndu() {
			super("indu");
		}

		@Override
		boolean init(Event abs) {
			
			fix();
			return super.init(abs);
		}

		private void fix() {
			if (i1 == null)
				i1 = new Induvidual(HTYPES.SOLDIER(), FACTIONS.player().race());
			if (i2 == null)
				i2 = new Induvidual(HTYPES.SOLDIER(), FACTIONS.player().race());
		}
		
		@Override
		public Induvidual first() {
			return i1;
		}

		@Override
		public Induvidual second() {
			return i2;
		}
		
		@Override
		void copy(SelContext<Induvidual> o) {
			fix();
			super.copy(o);
		}

		@Override
		SPRITE sprite() {
			if (i1 != null) {
				 return new SPRITE.Imp(RPortrait.P_WIDTH*3, RPortrait.P_HEIGHT*3) {
						@Override
						public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
							STATS.APPEARANCE().portraitRender(r, i1, X1, Y1, 3);
							
						}
				};
			}
			return null;
		}

		@Override
		ESelectionType<Induvidual> sel(Event abs) {
			return abs.selection.indu;
		}

		@Override
		void set(Induvidual first, Induvidual second) {
			if (first != null)
				i1.copyFromHard(first);
			if (second != null)
				i2.copyFromHard(second);
		}

		@Override
		int max() {
			return SETT.ENTITIES().Imax();
		}

		@Override
		Induvidual get(int index) {
			ENTITY e = SETT.ENTITIES().getAllEnts()[index];
			if (e != null && e instanceof Humanoid) {
				return ((Humanoid)e).indu();
			}
			return null;
		}

		@Override
		void markSet(Induvidual t, int id) {
			STATS.EVENT().mark.set(t, id);
		}

		@Override
		int markGet(Induvidual t) {
			return STATS.EVENT().mark.get(t);
		}

		@Override
		void eventSet(Induvidual t, int b) {
			STATS.EVENT().set(t, b == 1);
		}
		
		@Override
		boolean eventGet(Induvidual t) {
			return STATS.EVENT().has(t);
		}

		@Override
		int tot() {
			return STATS.POP().POP.data().get(null);
		}

		@Override
		Class<?> cl() {
			return Induvidual.class;
		}
		
	}
	
	public static class SelReg extends SelContext<Region> {

		private SelReg() {
			super("regs");
		}
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private int i1 = -1;
		private int i2 = -1;
		
		@Override
		public Region first() {
			if (i1 < 0)
				return null;
			return WORLD.REGIONS().getByIndex(i1);
		}

		@Override
		public Region second() {
			if (i2 < 0)
				return null;
			return WORLD.REGIONS().getByIndex(i2);
		}

		@Override
		SPRITE sprite() {
			return null;
		}

		@Override
		void set(Region first, Region second) {
			i1 = first == null ? -1 : first.index();
			i2 = second == null ? -1 : second.index();
		}

		@Override
		int max() {
			return WORLD.REGIONS().active().size();
		}

		@Override
		Region get(int index) {
			return WORLD.REGIONS().active().get(index);
		}

		@Override
		void markSet(Region t, int id) {
			RD.event().mark.set(t, id);
		}

		@Override
		int markGet(Region t) {
			return RD.event().mark.get(t);
		}
		
		@Override
		boolean eventGet(Region t) {
			return RD.event().ii.get(t) == 1;
		}

		@Override
		void eventSet(Region t, int b) {
			RD.event().ii.set(t, b);
		}

		@Override
		ESelectionType<Region> sel(Event abs) {
			return abs.selection.reg;
		}

		@Override
		Class<?> cl() {
			return Region.class;
		}
		
	}
	
	public static class SelFac extends SelContext<Faction> {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private int i1 = -1;
		private int i2 = -1;
		
		private SelFac() {
			super("fac");
		}
		
		@Override
		public Faction first() {
			if (i1 < 0)
				return null;
			return FACTIONS.all().get(i1);
		}

		@Override
		public Faction second() {
			if (i2 < 0)
				return null;
			return FACTIONS.all().get(i2);
		}

		@Override
		SPRITE sprite() {
			if (first() != null)
				return first().banner().HUGE;
			return null;
		}

		@Override
		void set(Faction first, Faction second) {
			i1 = first == null ? -1 : first.index();
			i2 = second == null ? -1 : second.index();
		}

		@Override
		int max() {
			return FACTIONS.active().size();
		}

		@Override
		Faction get(int index) {
			return FACTIONS.active().get(index);
		}

		@Override
		void markSet(Faction t, int id) {
			t.eventMark = (short) id;
		}

		@Override
		int markGet(Faction t) {
			return t.eventMark;
		}

		@Override
		void eventSet(Faction t, int b) {
			t.eventSet(b == 1);
			
		}
		
		@Override
		boolean eventGet(Faction t) {
			return t.event();
		}

		@Override
		ESelectionType<Faction> sel(Event abs) {
			return abs.selection.faction;
		}

		@Override
		Class<?> cl() {
			return Faction.class;
		}
		
	}
	
	public static class SelRoy extends SelContext<Royalty> {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private int i1 = -1;
		private int i2 = -1;
		private int ii1 = -1;
		private int ii2 = -1;
		private Induvidual roy;
		private boolean isKing;
		
		private SelRoy() {
			super("roy");
		}
		
		@Override
		public Royalty first() {
			if (i1 < 0)
				return null;
			FactionNPC f = (FactionNPC) FACTIONS.all().get(i1);
			if (ii1 < f.court().all().size())
				return f.court().all().get(ii1);
			return null;
		}

		@Override
		public Royalty second() {
			if (i2 < 0)
				return null;
			FactionNPC f = (FactionNPC) FACTIONS.all().get(i2);
			if (ii2 < f.court().all().size())
				return f.court().all().get(ii2);
			return null;
		}
		
		@Override
		void copy(SelContext<Royalty> o) {
			super.copy(o);
			i1 = -1;
			i2 = -1;
			if (o.first() != null) {
				i1 = o.first().court.faction.index();
				ii1 = o.first().successionI();
			}
			if (o.second() != null) {
				i2 = o.second().court.faction.index();
				ii2 = o.second().successionI();
			}
		}
		
		@Override
		SPRITE sprite() {
			if (roy != null) {
				 return new SPRITE.Imp(RPortrait.P_WIDTH*3, RPortrait.P_HEIGHT*3) {
						@Override
						public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
							STATS.APPEARANCE().portraitRender(r, roy, X1, Y1, 3);
							if (isKing)
								roy.race().appearance().crown.crowns().get(0).renderScaled(r, X1, Y1 + 8 * 3, 3);
						}
				};
			}
			return null;
		}

		@Override
		void set(Royalty first, Royalty second) {
			i1 = -1;
			i2 = -1;
			if (first != null) {
				i1 = first.court.faction.index();
				ii1 = first.successionI();
				if (roy == null)
					roy = new Induvidual(HTYPES.NOBILITY(), FACTIONS.player().race());
				roy.copyFromHard(first.induvidual);
			}
			if (second != null) {
				i2 = second.court.faction.index();
				ii2 = second.successionI();
			}
			
		}

		@Override
		int max() {
			return FACTIONS.NPCs().size()*NPCCourt.MAX;
		}

		@Override
		Royalty get(int index) {
			int fi = index/NPCCourt.MAX;
			if (fi < 0)
				return null;
			Faction ff = FACTIONS.all().get(fi);
			if (ff == null || !(ff instanceof FactionNPC))
				return null;
			FactionNPC f = (FactionNPC) ff;
			if (!f.isActive())
				return null;
			int ri = index%NPCCourt.MAX;
			if (ri < f.court().all().size())
				return f.court().all().get(ri);
			return null;
		}

		@Override
		void markSet(Royalty t, int id) {
			t.eventMark = (short) id;
			
		}

		@Override
		int markGet(Royalty t) {
			return t.eventMark;
		}

		@Override
		void eventSet(Royalty t, int b) {
			t.eventSet(b==1);
			
		}
		
		@Override
		boolean eventGet(Royalty t) {
			return t.event();
		}

		@Override
		ESelectionType<Royalty> sel(Event abs) {
			return abs.selection.royalty;
		}

		@Override
		Class<?> cl() {
			return Royalty.class;
		}
		
	}
	
}
