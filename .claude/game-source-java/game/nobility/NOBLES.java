package game.nobility;

import java.io.IOException;
import java.util.Arrays;

import game.GAME;
import game.GAME.GameResource;
import game.boosting.BOOSTABLES;
import game.boosting.BOOSTING;
import game.boosting.BSourceInfo;
import game.boosting.BValue;
import game.boosting.BoostSpecs;
import game.boosting.Boostable;
import game.boosting.BoosterValue;
import game.debug.Profiler;
import game.faction.FACTIONS;
import game.faction.npc.FactionNPC;
import game.faction.player.BoostCompound;
import init.paths.PATHS;
import init.race.appearence.RPortrait;
import init.sprite.UI.UI;
import init.type.HCLASSES;
import init.type.POP_CL;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.Json;
import snake2d.util.gui.GuiSection;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.text.Str;
import util.gui.misc.GButt;
import util.text.D;
import util.updating.IUpdater;
import view.interrupter.IDebugPanel;
import view.main.VIEW;
import view.ui.message.MessageSection;

public final class NOBLES extends GameResource{

	public static int MAX_MAX = 256;
	
	public final CharSequence[] nameRanks;
	public static final int RANK_INCREASE = 2;

	private final ArrayList<Noble> active = new ArrayList<Noble>(256);
	private final ArrayList<Noble> all = new ArrayList<Noble>(256);
	private final IUpdater upper;

	public final BoostSpecs boosters;
	public final Boostable MAX;
	public final Boostable MAX_RANKS;
	final BoostCompound<NobleOffice> bos;
	
	public final LIST<NobleOffice> OFFICES = NobleOfficeUtil.make();
	
	

	private int ranksAllocated = 0;
	private int[] allocations = new int[OFFICES.size()];
	private int ri = -1;
	
	public NOBLES() {
		super("NOBILITIES", false);
		D.t(this);
		MAX = BOOSTING.push("NOBLES_MAX", 0, HCLASSES.NOBLE().names, D.g("desc", "The amount of nobles you may appoint."), UI.icons().s.noble, BOOSTABLES.CIVICS());
		MAX_RANKS = BOOSTING.push("NOBLES_RANKS_MAX", 0, D.g("rname", "Noble Promotions"), D.g("rdesc", "The amount of promotions you can offer your nobles."), UI.icons().s.noble.twin(UI.icons().s.chevron(DIR.N).createColored(COLOR.ORANGE100), DIR.C, 0), BOOSTABLES.CIVICS());
		boosters = new BoostSpecs(HCLASSES.NOBLE().names, UI.icons().s.noble, true);
		nameRanks = new Json(PATHS.PLAYER().folder("noble").text.get("_RANKS")).texts("RANKS");
		while(all.hasRoom())
			new Noble(all);
		

		upper = new IUpdater(all.size(), 10) {
			
			@Override
			protected void update(int i, double timeSinceLast) {
				all.get(i).update(timeSinceLast);
			}
		};

		bos = new BoostCompound<NobleOffice>(boosters, OFFICES) {

			//double npc = CLAMP.d(all.size()/20.0, 0, 1);
			
			@Override
			protected BoostSpecs bos(NobleOffice t) {
				return t.boosts;
			}

			@Override
			protected double get(Boostable bo, FactionNPC f, boolean isMul) {
				return 0;
			}
			
			@Override
			protected double getValue(NobleOffice t) {
				return t.value(allocations(t));
			}
			
			
			
			
		};
		
		IDebugPanel.add("noble galore", new ACTION() {
			
			@Override
			public void exe() {
				new BoosterValue(BValue.VALUE1, new BSourceInfo("cheat", UI.icons().s.cancel), 10, false).add(MAX);
				new BoosterValue(BValue.VALUE1, new BSourceInfo("cheat", UI.icons().s.cancel), 10, false).add(MAX_RANKS);
			}
		});
		
		
	}
	
	@Override
	protected void save(FilePutter file) {
		for (Noble n : all)
			n.saver.save(file);
		upper.save(file);
	}

	@Override
	protected void load(FileGetter file) throws IOException {
		for (Noble n : all)
			n.saver.load(file);
		upper.load(file);
		bos.clearChache();
		ri = -1;
		active.clear();
		for (Noble n : all) {
			if (n.subject() != null)
				active.add(n);
		}
	}

	@Override
	protected void update(double ds, Profiler prof) {
		prof.logStart(NOBLES.class);
		upper.update(ds);
		prof.logEnd(NOBLES.class);
	}

	public LIST<Noble> ALL(){
		return all;
	}
	
	public int maxRanks() {
		return nameRanks.length;
	}
	
	private void cache() {
		if (ri != active.size()) {
			ri = active.size();
			ranksAllocated = 0;
			Arrays.fill(allocations, 0);
			for (int ni = 0; ni < active.size(); ni++) {
				ranksAllocated += active.get(ni).rank();
				NobleOffice n = active.get(ni).office();
				if (n != null)
					allocations[n.index] += 1 + RANK_INCREASE*active.get(ni).rank();
			}
		}
	}
	
	public int ranksAllocated() {
		cache();
		return ranksAllocated;
	}
	
	public int allocations(NobleOffice o) {
		cache();
		return allocations[o.index];
	}
	
	public void ranksAllocate(Noble n) {
		if (ranksAllocated() < (int)MAX_RANKS.get(POP_CL.clP())) {
			n.rankInc();
			ri = -1;
		}
	}
	
	public short assignOnlyCallFromHumanoid(Humanoid h) {
		if (!active.hasRoom())
			return -1;
		for (Noble n : all) {
			if (n.subject() == null) {
				n.assign(h);
				bos.clearChache();
				ri = -1;
				if (active.contains(n))
					throw new RuntimeException();
				active.add(n);
				return (short) n.index;
			}
		}
		throw new RuntimeException();
	}
	
	public void vacateOnlyCallFromHumanoid(Humanoid h, short pos) {
		
		
		Noble e = all.get(pos);
		if (e.subject() != h)
			throw new RuntimeException();
		DeathMess m = new DeathMess(h, e);
		active.remove(e);

		e.saver.clear();
		ri = -1;
		bos.clearChache();
		m.send();
	}
	
	public void setOffice(Noble n, NobleOffice office) {
		n.setOffice(office);
		ri = -1;
	}
	
	public Noble get(short index) {
		return all.get(index);
	}
	
	public LIST<Noble> active() {
		return active;
	}
	
	private static CharSequence ¤¤title = "Nobility passed!";
	private static CharSequence ¤¤mess = "It is a sad day. {0} {1}, {2}, passed today. We can now assign a new nobleman to this cause.";
	private static CharSequence ¤¤messNo = "It is a sad day. {0} {1} passed today.";
	private static CharSequence ¤¤replace = "Replace";
	private static CharSequence ¤¤replaceD = "Automatically replace this noble, including rank and office, with a willing citizen.";
	
	static {
		D.ts(NOBLES.class);
	}
	
	private static class DeathMess extends MessageSection {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private final Induvidual indu;
		private final int no;
		private final int ranks;
		private String text;
		private boolean replaced = false;
		
		public DeathMess(Humanoid h, Noble n) {
			super(¤¤title);
			indu = new Induvidual(h.indu().hType(), h.race()); 
			indu.copyFromHard(h.indu());
			STATS.APPEARANCE().dead.indu().set(indu, 1);
			no = n.office() == null ? -1 : n.office().index;
			ranks = n.rank();
			if (no >= 0) {
				Str.TMP.clear().add(¤¤mess);
				Str.TMP.insert(0, n.rankName());
				Str.TMP.insert(1, STATS.APPEARANCE().name(h.indu()));
				Str.TMP.insert(2, n.title());
			}else {
				Str.TMP.clear().add(¤¤messNo);
				Str.TMP.insert(0, n.rankName());
				Str.TMP.insert(1, STATS.APPEARANCE().name(h.indu()));
			}
				text = "" + Str.TMP;
		}

		@Override
		protected void make(GuiSection section) {


			
			paragraph(text);
			
			section.addRelBody(8, DIR.N, new SPRITE.Imp(RPortrait.P_WIDTH*4, RPortrait.P_HEIGHT*4) {
				
				@Override
				public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
					STATS.APPEARANCE().portraitRender(r, indu, X1, Y1, 4);
				}
			});
			
			if (no >= 0) {
				GButt.ButtPanel b = new GButt.ButtPanel(¤¤replace) {
					
					
					@Override
					protected void renAction() {
						
						
						activeSet(!replaced && GAME.NOBLE().active.size() < GAME.NOBLE().MAX.get(POP_CL.clP()));
					}
					
					@Override
					protected void clickA() {
						if (replaced || GAME.NOBLE().active.size() >= GAME.NOBLE().MAX.get(POP_CL.clP()))
							return;
						replaced = true;
						
						for (ENTITY e : SETT.ENTITIES().getAllEnts()) {
							if (e instanceof Humanoid) {
								Humanoid a = (Humanoid) e;
								if (a.race() == indu.race() && a.indu().clas() == HCLASSES.CITIZEN()) {
									NobleOffice o = GAME.NOBLE().OFFICES.get(no);
									a.nobleSet();
									GAME.NOBLE().setOffice(a.noble(), o);
									for (int i = 0; i < ranks; i++) {
										if (GAME.NOBLE().ranksAllocated() < GAME.NOBLE().MAX_RANKS.get(POP_CL.clP()))
										GAME.NOBLE().ranksAllocate(a.noble());
									}
									VIEW.messages().hide();
									return;
								}
							}
							
							
						}
					}
				
					
				};
				b.hoverInfoSet(¤¤replaceD);
				
				section.addRelBody(8, DIR.S, b);
			}
			
					
		}
		
		
	}
	
}
