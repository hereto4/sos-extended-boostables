package game.event.actions;

import game.GAME;
import game.battle.div.Div;
import game.boosting.BOOSTABLE_O;
import game.boosting.BOOSTING;
import game.boosting.BSourceInfo;
import game.boosting.BValue;
import game.boosting.BoostSpec;
import game.boosting.BoostSpecs;
import game.boosting.Boostable;
import game.boosting.Booster;
import game.event.engine.EChoice;
import game.event.engine.EContext;
import game.event.engine.Event;
import game.event.engine.EventCollection;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.npc.FactionNPC;
import game.faction.player.Player;
import init.sprite.UI.UI;
import init.type.POP_CL;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.file.Json;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.KeyMap;
import snake2d.util.sets.LISTE;
import util.gui.misc.GBox;
import util.gui.misc.GHeader;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.table.GRows;
import util.info.GFORMAT;
import util.text.D;
import util.text.Dic;
import world.map.regions.Region;
import world.region.RD;

final class _BOOST extends EventActionConstructor{

	public final ArrayListGrower<_BOOST.Imp> boosts = new ArrayListGrower<>();
	private static CharSequence ¤¤sTitle = "Boosted Subjects";
	private static CharSequence ¤¤sTitleR = "Boosted Regions";
	
	static {
		D.ts(_BOOST.class);
	}
	
	_BOOST() {
		super("BOOST");
	}
	
	@Override
	public EventAction action(Data data) {
		return new Imp(key, data.parent, data.choice, data.json, data.all);
	}
	
	public final class Imp extends EventAction  {
		
		public final BoostSpecs player = new BoostSpecs("", UI.icons().s.time, false);
		public final BoostSpecs subjects = new BoostSpecs("", UI.icons().s.time, false);
		public final BoostSpecs regions = new BoostSpecs("", UI.icons().s.time, false);
		public final Event parent;
		public final EChoice choice;
		
		Imp(String key, Event parent, EChoice choice, Json data, LISTE<EventAction> all) {
			super(key, all);
			this.parent = parent;
			player.read("PLAYER", data, BValue.VALUE1);
			subjects.read("SUBJECTS", data, BValue.VALUE1);
			regions.read("REGIONS", data, BValue.VALUE1);
			this.choice = choice;
			boosts.add(this);
			data.checkUnused();
		}

		
		@Override
		public void hover(GBox b, Event event, EContext context) {
			
			if (player.all().size()>0) {
				player.hover(b, 1, Dic.¤¤Boosts, -1);
			}
			if (subjects.all().size() > 0) {
				b.textLL(¤¤sTitle);
				b.add(GFORMAT.i(b.text(), STATS.EVENT().stat().data().get(null)));
				subjects.hover(b, 1, null, -1);
			}
			if (regions.all().size() > 0) {
				b.textLL(¤¤sTitleR);
				b.add(GFORMAT.i(b.text(), STATS.EVENT().stat().data().get(null)));
				regions.hover(b, 1, null, -1);
			}
		}
		
		@Override
		public void addToMessageBody(LISTE<RENDEROBJ> rows, Event event, EContext context, RECTANGLE messBody) {
			GRows rr = new GRows(6).setMin(100);
			if (player.all().size() > 0) {
				rows.add(new GHeader(Dic.¤¤Boosts, UI.FONT().S));
				for (BoostSpec s : player.all()) {
					rr.add(new GStat() {
						@Override
						public void update(GText text) {
							s.booster.format(text, s.booster.getValue(1));
						}
						
						@Override
						public void hoverInfoGet(GBox b) {
							s.boostable.hover(b);
						};
						
					}.hh(s.boostable.icon));
				}
				rows.add(rr.rows());
				rr = new GRows(6).setMin(100);
			}
			
			if (subjects.all().size() > 0 && context.indu.am > 0) {
				rows.add(new GStat() {
					
					@Override
					public void update(GText text) {
						GFORMAT.i(text, context.indu.am );
					}
				}.hh(¤¤sTitle));
				
				for (BoostSpec s : subjects.all()) {
					rr.add(new GStat() {
						@Override
						public void update(GText text) {
							s.booster.format(text, s.booster.getValue(1));
						}
						
						@Override
						public void hoverInfoGet(GBox b) {
							s.boostable.hover(b);
						};
						
					}.hh(s.boostable.icon));
				}
				rows.add(rr.rows());
				rr = new GRows(6).setMin(100);
			}
			
			if (regions.all().size() > 0 && context.regs.am > 0) {
				rows.add(new GStat() {
					
					@Override
					public void update(GText text) {
						GFORMAT.i(text, context.regs.am );
					}
				}.hh(¤¤sTitleR));
				
				for (BoostSpec s : regions.all()) {
					rr.add(new GStat() {
						@Override
						public void update(GText text) {
							s.booster.format(text, s.booster.getValue(1));
						}
						
						@Override
						public void hoverInfoGet(GBox b) {
							s.boostable.hover(b);
						};
						
					}.hh(s.boostable.icon));
				}
				rows.add(rr.rows());
				rr = new GRows(6).setMin(100);
			}
		}
	}

	static void init(EventCollection handler) {
		BOOSTING.connecter(new ACTION() {
			
			@Override
			public void exe() {
				KeyMap<Cluster> map = new KeyMap<Cluster>();
				for (Event e : handler.all) {
					for (EventAction a : e.actions()) {
						if (a instanceof Imp) {
							Imp b = (Imp) a;
							for (BoostSpec s : new ArrayList<>(b.player.all())) {
								String k = s.boostable.key() + s.booster.isMul;
								if (!map.containsKey(k))
									map.put(k, new Cluster(s.boostable, s.booster.isMul));
								map.get(k).addA(b);
							}
							for (BoostSpec s : new ArrayList<>(b.subjects.all())) {
								String k = s.boostable.key() + s.booster.isMul;
								if (!map.containsKey(k))
									map.put(k, new Cluster(s.boostable, s.booster.isMul));
								map.get(k).addB(b);
							}
							for (BoostSpec s : new ArrayList<>(b.regions.all())) {
								String k = s.boostable.key() + s.booster.isMul;
								if (!map.containsKey(k))
									map.put(k, new Cluster(s.boostable, s.booster.isMul));
								map.get(k).addC(b);
							}
							
							
						}
					}
					
				}
				
				for (Cluster c : map.all()) {
					
					c.add(c.target);
					
				}
			}
		});
		
	}

	
	private static class Cluster extends Booster implements BValue{
		


		private int upI = -1;
		private final ArrayListGrower<ClusterEntry> bplayer = new ArrayListGrower<>();
		private final ArrayListGrower<ClusterEntry> bregion = new ArrayListGrower<>();
		private final ArrayListGrower<ClusterEntry> bindu = new ArrayListGrower<>();
		
		private double player;
		private double reg;
		private double indu;
		
		private double from = 0;
		private double to = 0;
		private final double def;
		private final Boostable target;
		
		public Cluster(Boostable target, boolean isMul) {
			super(new BSourceInfo(Dic.¤¤Event, UI.icons().s.time), isMul);
			if (isMul) {
				from = 1;
				to = 1;
				def = 1;
			}else {
				def = 0;
			}
			this.target = target;
		}

		void addA(_BOOST.Imp b) {
			for (BoostSpec s : b.player.all()) {
				if (s.boostable == target && s.booster.isMul == isMul) {
					add(new ClusterEntry(b, s.booster.to()), bplayer);
				}
			}
		}
		
		void addB(_BOOST.Imp b) {
			for (BoostSpec s : b.subjects.all()) {
				if (s.boostable == target && s.booster.isMul == isMul) {
					add(new ClusterEntry(b, s.booster.to()), bindu);
				}
			}
		}
		
		void addC(_BOOST.Imp b) {
			for (BoostSpec s : b.regions.all()) {
				if (s.boostable == target && s.booster.isMul == isMul) {
					add(new ClusterEntry(b, s.booster.to()), bregion);
				}
			}
		}
		
		private void add(ClusterEntry b, ArrayListGrower<ClusterEntry> li) {
			
			li.add(b);
			
			if ((isMul && b.value < 1) || b.value < 0) {
				from = Math.min(from, b.value);
			}else {
				to = Math.max(to, b.value);
			}
			
		}
		
		private void cache() {
			if (upI == GAME.updateI())
				return;
			upI = GAME.updateI();
			player = def;
			indu = def;
			reg = def;
			
			if (isMul) {
				for (ClusterEntry e : bplayer) {
					if (isActive(e)) {
						player *= e.value;
					}
				}
				
				for (ClusterEntry e : bindu) {
					if (isActive(e))
						indu *= e.value;
				}
				
				
				for (ClusterEntry e : bregion) {
					if (isActive(e)) {
						reg *= e.value;
					}
				}
				
				indu = indu*player - player;
				reg = reg*player - player;
				
			}else {
				for (ClusterEntry e : bplayer) {
					if (isActive(e))
						player += e.value;
				}
				
				for (ClusterEntry e : bregion) {
					if (isActive(e))
						reg += e.value;
				}
				
				for (ClusterEntry e : bindu) {
					if (isActive(e))
						indu += e.value;
				}
			}
		}
		
		private boolean isActive(ClusterEntry e) {
			if (e.imp.choice == null)
				return GAME.EVENT().current() == e.imp.parent;
			return false;
		}
		
		@Override
		public double getValue(double input) {
			return input;
		}
		
		@Override
		protected double pget(BOOSTABLE_O o) {
			return o.boostableValue(this);
		}
		
		@Override
		public double vGet(Faction f) {
			if (f == FACTIONS.player())
				return vGet(POP_CL.clP());
			return def;
		}

		@Override
		public double vGet(Region reg) {
			cache();
			return player + (RD.event().ii.get(reg) == 1 ? this.reg : 0);
		}

		@Override
		public double vGet(Induvidual indu) {
			cache();
			return player + (STATS.EVENT().has(indu) ? this.indu : 0);
		}

		@Override
		public double vGet(Div div) {
			if (div.army().faction() == FACTIONS.player()) {
				cache();
				return player + STATS.EVENT().stat().div().getD(div)*this.indu;
			}
			return def;
		}
		
		@Override
		public double vGet(PopTime popTime) {
			cache();
			return player + STATS.EVENT().stat().data(popTime.pop.cl).getD(popTime.pop.race)*this.indu;
		}

		@Override
		public double vGet(Player f) {
			cache();
			return player;
		}

		@Override
		public double vGet(FactionNPC f) {
			return def;
		}

		@Override
		public double from() {
			return from;
		}

		@Override
		public double to() {
			return to;
		}


		
	}
	
	private static class ClusterEntry {
		
		private final _BOOST.Imp imp;
		private final double value;
		
		ClusterEntry(_BOOST.Imp imp, double value){
			this.imp = imp;
			this.value = value;
		}
		
	}


	
}
