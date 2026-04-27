package view.sett.ui.bottom;

import game.boosting.BOOSTABLES;
import game.faction.FACTIONS;
import init.race.RACES;
import init.race.Race;
import init.religion.Religion;
import init.sprite.SPRITES;
import init.type.HCLASSES;
import init.type.POP_CL;
import settlement.environment.SettEnvMap.SettEnv;
import settlement.main.SETT;
import settlement.room.industry.module.INDUSTRY_HASER;
import settlement.room.industry.module.Industry;
import settlement.room.industry.module.IndustryResource;
import settlement.room.infra.monument.ROOM_MONUMENT;
import settlement.room.main.RoomBlueprintImp;
import settlement.room.main.RoomBlueprintIns;
import settlement.room.main.furnisher.Furnisher;
import settlement.room.main.furnisher.FurnisherItemGroup;
import settlement.room.main.furnisher.FurnisherStat;
import settlement.room.service.module.RoomServiceAccess.ROOM_SERVICE_ACCESS_HASER;
import settlement.room.spirit.shrine.ROOM_SHRINE;
import settlement.room.spirit.temple.ROOM_TEMPLE;
import settlement.room.water.pool.ROOM_POOL;
import settlement.stats.STATS;
import settlement.stats.standing.STANDINGS;
import settlement.stats.standing.StatStanding;
import settlement.stats.stat.STAT;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.misc.CLAMP;
import snake2d.util.sprite.SPRITE;
import util.colors.GCOLOR;
import util.gui.misc.GBox;
import util.gui.slider.GGaugeMutable;
import util.info.GFORMAT;
import util.text.D;
import util.text.Dic;

public final class UIRoomBuild{
	
	
	private static CharSequence ¤¤cost = "¤Costs";
	private static CharSequence ¤¤production = "¤Production";
	private static CharSequence ¤¤optional = "¤(Optional)";
	private static CharSequence ¤¤Emits = "¤Emits";
	private static CharSequence ¤¤CurrentRooms = "¤Current Rooms";
	private static CharSequence ¤¤CurrentBelievers = "¤Current Worshippers";
	
	static {
		D.ts(UIRoomBuild.class);
	}
	
	private UIRoomBuild() {
		
	}
	
	public static void hoverRoomBuild(RoomBlueprintImp b, GUI_BOX text) {
		
		
		GBox box = (GBox) text;
		box.title(b.info.name);
		box.text(b.info.desc);
		box.NL();
		
		if (b instanceof ROOM_TEMPLE || b instanceof ROOM_SHRINE) {
			Religion rel = null;
			if (b instanceof ROOM_TEMPLE) {
				rel = ((ROOM_TEMPLE) b).religion;
			}else {
				rel = ((ROOM_SHRINE) b).religion;
			}	
			
			box.textLL(¤¤CurrentBelievers);
			box.add(GFORMAT.perc(box.text(), STATS.RELIGION().ALL.get(rel.index()).followers.data().getD(null)));
			box.NL();
			
		}
		
		b.reqs.hover(text, FACTIONS.player());
		
		box.sep();
		
		if (b instanceof RoomBlueprintIns<?>) {
			RoomBlueprintIns<?> ins = (RoomBlueprintIns<?>) b;
			box.NL(2);
			box.textLL(¤¤CurrentRooms);
			box.add(GFORMAT.i(box.text(), ins.instancesSize()));
		}
		
		box.NL(8);
		
		boolean e = false;
		for (SettEnv en : SETT.ENV().map.all()) {
			if (b.constructor().envValue(en)) {
				if (!e) {
					box.textLL(¤¤Emits);
					e = true;
				}
				box.text(en.info.name);

			}
		}
		box.NL();
		
		if (b.employment() != null) {
			box.textLL(Dic.¤¤AccidentRate);
			box.add(GFORMAT.perc(box.text(), b.employment().accidentsPerYear / (1+BOOSTABLES.CIVICS().ACCIDENT.get(POP_CL.clP(null, null))), 4));
			box.NL();
		}
		
		if (b.constructor().resources() > 0) {
			box.NL(8);
			box.textLL(¤¤cost);
			int o = 0;
			for (int ri = 0; ri < b.constructor().resources(); ri++) {
				if (b.upgrades().resMask(0,ri) == 0)
					continue;
				if (optional(b.constructor(), ri)) {
					o++;
					continue;
				}
				box.add(b.constructor().resource(ri).icon());
			}
			
			if (o > 0) {
				box.space().space();
				box.add(box.text().lablifySub().add(¤¤optional));
				for (int ri = 0; ri < b.constructor().resources(); ri++) {
					if (b.upgrades().resMask(0, ri) == 0)
						continue;
					if (optional(b.constructor(), ri)) {
						box.add(b.constructor().resource(ri).icon());
					}
					
				}
			}
			box.NL();
		}
		
		if (b instanceof INDUSTRY_HASER) {
			box.NL(8);
			box.textLL(¤¤production);
			box.NL();
			
			for (Industry i : ((INDUSTRY_HASER)b).industries()) {
				if (i.outs().size() == 0) {
					for (IndustryResource r : i.ins()) {
						box.add(r.resource.icon()).add(GFORMAT.f0(box.text(),  -r.rate*(i.bonus() == null ? 1 : i.bonus().get(FACTIONS.player()))));
						box.space();
					}
				}else {
					
					for (int ri = 0; ri < i.ins().size(); ri++) {
						IndustryResource r = i.ins().get(ri);
						box.add(r.resource.icon()).add(GFORMAT.f0(box.text(), -r.rate*i.bonus().get(FACTIONS.player())));
						if (ri < i.ins().size()-1)
							box.add(box.text().add('&'));
					}
					
					box.add(SPRITES.icons().s.arrow_right);
					
					for (int ri = 0; ri < i.outs().size(); ri++) {
						IndustryResource r = i.outs().get(ri);
						box.add(r.resource.icon()).add(GFORMAT.fRel(box.text(), (r.rate*i.bonus().get(FACTIONS.player())), r.rate));
						if (ri < i.outs().size()-1)
							box.add(SPRITES.icons().s.plus);
					}
					
					
				}
				
				if (!i.lockable().passes(FACTIONS.player())) {
					box.add(SPRITES.icons().m.lock);
					box.NL();
					i.lockable().hover(text, FACTIONS.player());
				}
				
				box.sep();
			}
			
			if (b.bonus() != null) {
				int tab = 0;
				for (Race r : RACES.all()) {
					box.tab(tab*2);
					box.add(r.appearance().icon);
					double d = RACES.boosts().getNorSkill(r, b.employment());
					GGaugeMutable.bad2Good(ColorImp.TMP, d);
					int am = (int) Math.ceil(0.1 + d*3);
					am = CLAMP.i(am, 0, 3);
					box.rewind(4);
					for (int i = 0; i < am; i++) {
						
						box.add(SPRITES.icons().s.hammer, ColorImp.TMP);
						box.rewind(8);
					}
					
					tab++;
					if (tab > 6) {
						box.NL();
						tab = 0;
					}
				}
				box.NL();
				
				
			}
		}
		
		STAT stat = null;
		if (b instanceof ROOM_SERVICE_ACCESS_HASER)
			stat = ((ROOM_SERVICE_ACCESS_HASER) b).service().stats().total();
		else if (b instanceof ROOM_MONUMENT) {
			ROOM_MONUMENT m = (ROOM_MONUMENT) b;
			stat = STATS.ACCESS().MONUMENTS.ALL().get(m.monumentIndex);
		}
		if (stat != null) {
			box.NL(4);
			box.textLL(STANDINGS.CITIZEN().fullfillment.info().name);
			box.NL();
			int tab = 0;
			double min = 0;
			double max = 0;
			for (Race r : RACES.all()) {
				StatStanding d =  stat.standing();
				if (d.definition(r).inverted)
					min = Math.max(min, d.definition(r).get(HCLASSES.CITIZEN()).max);
				else
					max = Math.max(max, d.definition(r).get(HCLASSES.CITIZEN()).max);
			}

			for (Race r : RACES.all()) {
				
				
				StatStanding d =  stat.standing();
				COLOR c = GCOLOR.UI().GOOD.hovered;
				SPRITE sp = SPRITES.icons().s.arrowUp;
				int am = (int) (3*Math.ceil(CLAMP.d(d.definition(r).get(HCLASSES.CITIZEN()).max/max, 0, max)));
				if (d.definition(r).inverted) {
					sp = SPRITES.icons().s.arrowDown;
					c = GCOLOR.UI().BAD.hovered;
					am = (int) (4*Math.round(CLAMP.d(d.definition(r).get(HCLASSES.CITIZEN()).max/min, 0, min)));
				}
				box.tab(tab*2);
				box.add(r.appearance().icon);
				box.rewind(4);
				for (int i = 0; i < am; i++) {
					
					box.add(sp, c);
					box.rewind(8);
				}
				
				tab++;
				if (tab > 8) {
					box.NL();
					tab = 0;
				}
			}
			
			
		}
		
		
		if (b instanceof ROOM_POOL) {
			ROOM_POOL pool = (ROOM_POOL) b;
			for (Race r : RACES.all()) {
				double d = r.pref().pool(pool);
				int k = 1 + (int) (5*d);
				if ((r.index & 0b0011) == 0)
					box.NL();
				box.tab((r.index&0b011)*3);
				box.add(r.appearance().icon);
				ColorImp.TMP.interpolate(GCOLOR.UI().BAD.hovered, GCOLOR.UI().GOOD.hovered, d);
				for (int i = 0; i < k; i++) {
					box.add(SPRITES.icons().s.heart, ColorImp.TMP);
					box.rewind(8);
				}
				box.space();
				
			}
		}

		
	}
	
	private static boolean optional(Furnisher f, int ri) {
		if (f.areaCost(ri, 0) > 0)
			return false;
		if (!f.usesArea())
			return false;
		for (FurnisherItemGroup g : f.groups()) {
			if (needed(f, g) && g.cost(ri, 0) > 0) {
				return false;
			}
		}
		return true;
	}
	
	private static boolean needed(Furnisher f, FurnisherItemGroup g) {
		if (g.min > 0)
			return true;
		
		for (FurnisherStat s : f.stats()) {
			if (g.stat(s.index()) > 0 && s.min > 0)
				return true;
		}
		return false;
	}
	
}
