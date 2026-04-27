package game.event.actions;

import game.GAME;
import game.boosting.BValue;
import game.boosting.BoostSpec;
import game.boosting.tmp.TmpBoostSpec;
import game.event.engine.EChoice;
import game.event.engine.EContext;
import game.event.engine.Event;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.npc.FactionNPC;
import game.faction.royalty.Royalty;
import init.sprite.UI.UI;
import init.type.POP_CL;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.file.Json;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LISTE;
import util.gui.misc.GBox;
import util.gui.misc.GHeader;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.table.GRows;
import util.info.GFORMAT;
import util.text.D;
import world.WORLD;
import world.map.regions.Region;
import world.region.RD;

final class _BOOST_PERM extends EventActionConstructor{

	private static CharSequence ¤¤sTitle = "Boosted Subjects";
	private static CharSequence ¤¤sTitleR = "Boosted Regions";
	private static CharSequence ¤¤sTitleF = "Boosted Factions";
	
	static {
		D.ts(_BOOST_PERM.class);
	}
	
	_BOOST_PERM() {
		super("BOOST_PERM");
	}
	
	@Override
	public EventAction action(Data data) {
		return new Imp(key, data.parent, data.choice, data.json, data.all);
	}
	
	public final class Imp extends EventAction  {
		
		private final TmpBoostSpec spec;
		private LIST<POP_CL> pops;
		private final boolean player;
		private final boolean regions;
		private final boolean factions;
		private final boolean royalties;
		
		
		Imp(String key, Event parent, EChoice choice, Json data, LISTE<EventAction> all) {
			super(key, all);
			
			String k = "EVENT_" + parent.key;
			if (choice != null)
				k += choice.index;
			spec = new TmpBoostSpec(k, parent.info.name, parent.info.desc, parent.info.icon);
			spec.spec.read(data, BValue.VALUE1);
			pops = POP_CL.MAP().readManyWarn(data);
			player = data.bool("BOOST_ONLY_PLAYER", false);
			regions = data.bool("USE_SELECTION_REGIONS", true);
			factions = data.bool("USE_SELECTION_FACTIONS", true);
			royalties = data.bool("USE_SELECTION_ROYALTIES", true);
			data.checkUnused();
		}

		@Override
		public void exe(Event e, EContext data) {
			
			if (player) {
				GAME.BOOST().factions.set(FACTIONS.player(), spec, true);
				return;
			}
			
			if (regions && data.regs.am > 0) {
				for (Region reg : WORLD.REGIONS().active()) {
					if (RD.event().ii.get(reg) == 1) {
						GAME.BOOST().regions.set(reg, spec, true);
					}
				}
			}
			
			if (factions && data.faction.am > 0) {
				for (Faction f : FACTIONS.active()) {
					if (f.event()) {
						GAME.BOOST().factions.set(f, spec, true);
					}
				}
			}
			
			if (royalties && data.royalty.am > 0) {
				for (FactionNPC f : FACTIONS.NPCs()) {
					for (Royalty roy : f.court().all()) {
						if (roy.event()) {
							GAME.BOOST().factions.set(f, spec, true);
							break;
						}
					}
				}
			}
			
			
			
			for (POP_CL p : pops) {
				GAME.BOOST().popcl.set(p, spec, true);
			}
			
			
			super.exe(e, data);
		}
		
		
		@Override
		public void hover(GBox b, Event event, EContext context) {
			
		}
		
		@Override
		public void addToMessageBody(LISTE<RENDEROBJ> rows, Event event, EContext context, RECTANGLE messBody) {
			
			if (!player && pops.size() > 0) {
				rows.add(new GHeader(¤¤sTitle, UI.FONT().S));
				for (POP_CL p : pops) {
					rows.add(new GStat() {
						@Override
						public void update(GText text) {
							text.add(p.race.info.names);
							text.s();
							text.add('(');
							text.add(p.cl.names);
							text.add(')');
						}
						
					}.hh(p.race.appearance().icon));
				}
			}
			
			if (!player && context.regs.am > 0) {
				rows.add(new GStat() {
					
					@Override
					public void update(GText text) {
						GFORMAT.i(text, context.regs.am );
					}
				}.hh(¤¤sTitleR));
				
			}
			
			if (!player && context.faction.am > 0) {
				rows.add(new GStat() {
					
					@Override
					public void update(GText text) {
						GFORMAT.i(text, context.faction.am);
					}
				}.hh(¤¤sTitleF));
				
			}
			
			GRows rr = new GRows(6).setMin(100);
			
			for (BoostSpec s : spec.spec.all()) {
				rr.add(new GStat() {
					
					@Override
					public void update(GText text) {
						if (s.booster.isMul) {
							text.add('*').s();
							GFORMAT.f1(text, s.booster.to());
						}else {
							GFORMAT.f0(text, s.booster.to());
						}
						
						
					}
				}.hh(s.boostable.icon));
			}
			
			rows.add(rr.rows());
			
		}
	}




	
}
