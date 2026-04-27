package view.sett.ui.law;

import game.faction.FACTIONS;
import init.race.RACES;
import init.race.Race;
import init.sprite.SPRITES;
import init.sprite.UI.Icon;
import init.type.HCLASSES;
import init.type.HTYPES;
import settlement.stats.STATS;
import settlement.stats.law.LAW;
import settlement.stats.law.PRISONER_TYPE;
import settlement.stats.law.Processing.Extra;
import settlement.stats.law.Processing.Punishment;
import settlement.stats.law.Processing.PunishmentDec;
import settlement.stats.law.Processing.PunishmentImp;
import settlement.stats.law.StatsLaw.StatLaw;
import settlement.stats.standing.STANDINGS;
import settlement.stats.standing.StatStanding.StandingDef.StandingData;
import settlement.stats.stat.STAT;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.LinkedList;
import snake2d.util.sprite.SPRITE;
import util.colors.GCOLOR;
import util.data.INT.INTE;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GHeader;
import util.gui.misc.GMeter;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.slider.GSliderInt;
import util.gui.table.GScrollRows;
import util.info.GFORMAT;
import util.text.D;
import util.text.Dic;
import view.interrupter.ISidePanel;
import view.main.VIEW;

final class Settings extends GuiSection{

	private static CharSequence ¤¤everyone = "Everyone (Overwrites all specific races)";
	private static CharSequence ¤¤rate = "Set target rate for:";
	private static CharSequence ¤¤toggle = "Toggle for:";
	private static CharSequence ¤¤Prosecute = "Are you sure you want to persecute your entire population of this race?";
	private static CharSequence ¤¤TargetRate = "Target Rate";
	private static CharSequence ¤¤CurrentRate = "Current Rate";
	private static CharSequence ¤¤PunishementSettings = "Punishment Settings";
	private static CharSequence ¤¤PunishementSettD = "Once you have caught the criminals, all you have to do is decide what to do with them. Different punishments have different effects. If no punishment is selected, or the facilities are inadequate, subjects will go into exile, which is bad for crime determent. Changing the settings will act on already imprisoned criminals, unless they have been specifically judged by your grace.";
	
	
	static CharSequence ¤¤Deter = "Determent";
	
	
	static {
		D.ts(Settings.class);
	}
	
	private final double minStanding;
	private final double maxStanding;
	private final ISidePanel momy;
	
	Settings(int height, ISidePanel momy){
		
		this.momy = momy;
		{
			double min = 0;
			double max = 0;
			for (StatLaw stat : STATS.LAW().punishments) {
				for (Race r : RACES.all()) {
					StandingData def = stat.standing().definition(r).get(HCLASSES.CITIZEN());
					if (def.to >= def.from)
						max = Math.max(max, def.max);
					else
						min = Math.min(min, -def.max);
				}
			}
			minStanding = -min;
			maxStanding = max;
		}
		
		
		addRelBody(4, DIR.N, new GHeader(¤¤PunishementSettings).hoverInfoSet(¤¤PunishementSettD));
		
		
		
		{
			GuiSection hh = new GuiSection();
			int hi = 42;
			
			hh.addRight(0, new RENDEROBJ.RenderDummy(100, hi));
			
			hh.addRightC(0, header(LAW.process().none, 80, hi));
			
			for (PunishmentImp p : LAW.process().punishmentsdec) {
				hh.addRightC(12, header(p, 100, hi));
			}
			
			for (PunishmentImp p : LAW.process().extras) {
				hh.addRightC(12, header(p, 60, hi));
			}
			
			for (Extra p : LAW.process().other) {
				hh.addRightC(8, header(p, 60, hi));
			}
			
			addRelBody(4, DIR.S, hh);
			
			
			
		}
		
		add(row(null, 38), body().x1(), body().y2()+4);
		
		
		LinkedList<RENDEROBJ> rows = new LinkedList<>();

		for (int i = 0; i < RACES.all().size(); i++) {
			final Race race = FACTIONS.player().races.get(i);
			rows.add(row(race, 28));
		}
		
		
		
		add(new GScrollRows(rows, height-body().height()-8).view(), body().x1(), body().y2()+8);
		
		pad(8, 0);
		
	}
	
	private RENDEROBJ row(Race race, int hi) {
		
		GuiSection row = new GuiSection();
		
		
		GStat s = new GStat() {
			@Override
			public void update(GText text) {
				GFORMAT.i(text, LAW.prisoners().amount(race));
			}
			@Override
			public void hoverInfoGet(GBox b) {
				if (race != null)
					b.textLL(race.info.names);
				b.NL(8);
				
				b.textLL(HTYPES.PRISONER().names);
				b.NL();
				b.textL(Dic.¤¤Total);
				b.tab(5);
				b.add(GFORMAT.i(b.text(), LAW.prisoners().amount(race)));
				b.NL(4);
				
				for (PRISONER_TYPE t : PRISONER_TYPE.ALL) {
					
					b.textL(t.titles);
					b.tab(5);
					b.add(GFORMAT.i(b.text(), LAW.prisoners().amount(race, t)));
					b.NL(4);
				}
				
			}
		}.increase();
		row.add(s.hh(race == null ? SPRITES.icons().m.citizen : race.appearance().icon));
		
		HOVERABLE h = new HOVERABLE.HoverableAbs(80-4, hi) {
			
			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
				double d = 1.0;
				for (PunishmentDec p : LAW.process().punishmentsdec)
					d -= p.limit(race);
				d = CLAMP.d(d, 0, 1);
				GSliderInt.renderMid(r, body.x1(), body.x2(), body.y1()+2, body.y2()-2,d, true, false, isHovered);
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				Punishment p = LAW.process().none;
				GBox b = (GBox) text;
				b.title(p.action);
				text.text(p.desc);
				b.NL(8);
				
				if (race != null)
					b.textLL(race.info.names);
				b.NL();
				b.textL(¤¤TargetRate);
				b.tab(6);
				double d = 1.0;
				for (PunishmentDec pp : LAW.process().punishmentsdec)
					d -= pp.limit(race);
				b.add(GFORMAT.perc(b.text(), d));
				b.NL(2);
				
				b.textL(¤¤CurrentRate);
				b.tab(6);
				b.add(GFORMAT.perc(b.text(), p.rate(race).getD()));
				b.NL(2);
			}
		};
		
		row.addRightCAbs(102, h);
		
		for (PunishmentDec p : LAW.process().punishmentsdec) {
			
			row.addRightC(12, slider(race, p, 100, hi));
			
		}
		
		for (Extra e : LAW.process().extras) {
			row.addRightC(12, check(race, e, 60, hi));
		}
		
		for (Extra e : LAW.process().other) {
			row.addRightC(12, check(race, e, 60, hi));
		}
		
		row.pad(0, 4);
		row.add(new SPRITE.Imp(row.body().width(), 1) {
			
			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				GCOLOR.UI().border().render(r, X1, X2, Y1, Y2);
			}
		}, 0, row.body().y2());
		
		return row;
	}
	
	private RENDEROBJ slider(Race race, PunishmentDec p, int width, int hi) {
		INTE in = new INTE() {
			
			@Override
			public int min() {
				return 0;
			}
			
			@Override
			public int max() {
				return 20;
			}
			
			@Override
			public int get() {
				return (int) Math.round(p.limit(race)*20);
			}
			
			@Override
			public void set(int t) {
				p.limitSet(race, t/20.0);
			}
		};
		
		final StatLaw stat = stat(p);
		
		
		return new GSliderInt(in, width, hi, false){
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				
				GBox b = (GBox) text;
				b.title(p.action);
				b.text(p.desc);
				if (p.multiplier != 0) {
					b.NL();
					b.textL(¤¤Deter);
					b.tab(6);
					b.add(GFORMAT.percInc(b.text(), p.multiplier));
				}
				
				b.NL(8);
			
				
				hoverStanding(b, stat, race);
				
				
				
				b.textL(¤¤rate);
				if (race != null) {
					b.textLL(race.info.names);
				}else {
					b.textLL(¤¤everyone);
				}
				b.NL(8);
				
				b.textL(¤¤TargetRate);
				b.tab(6);
				b.add(GFORMAT.perc(b.text(), in.getD()));
				b.NL(2);
				
				b.textL(¤¤CurrentRate);
				b.tab(6);
				b.add(GFORMAT.perc(b.text(), p.rate(race).getD()));
			}
			
			@Override
			protected void renderMidColor(SPRITE_RENDERER r, int x1, int width, int widthFull, int y1, int y2) {
				GCOLOR.UI().NEUTRAL.hovered.render(r, x1, x1+width, y1, y2);
			}
			
			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				activeSet(LAW.process().arrests.allowed.is(race));
				super.render(r, ds);
			}
			
			
		};
	}
	
	private StatLaw stat(PunishmentImp p) {
		for (StatLaw s : STATS.LAW().punishments) {
			if (s.p == p)
				return s;
		}
		return null;
	}
	
	private RENDEROBJ header(PunishmentImp p, int width, int height) {
		
		
		
		HOVERABLE h = new ClickableAbs(width, height) {

			
			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected,
					boolean isHovered) {
				
				if (isHovered && p.room != null) {
					COLOR.WHITE50.render(r, body, 2);
				}
				
				GMeter.render(r, GMeter.C_ORANGE, p.rate(null).getD(), body);
				
				p.icon.renderC(r, body);
				
				if (p.ser != null && p.ser.punishUsed() >= p.ser.punishTotal()*0.8) {
					GCOLOR.UI().BAD.normal.bind();
					SPRITES.icons().s.alert.render(r, body().x2()-Icon.S, body().y1());
					COLOR.unbind();
				}
				
				
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				GBox b = (GBox) text;
				b.title(p.action);
				text.text(p.desc);
				
				if (p.multiplier != 0) {
					b.NL();
					b.textL(¤¤Deter);
					b.tab(6);
					b.add(GFORMAT.percInc(b.text(), p.multiplier));
				}
				
				b.NL(8);
			
				b.textLL(¤¤CurrentRate);
				b.tab(6);
				b.add(GFORMAT.perc(b.text(), p.rate(null).getD()));
				b.NL(16);
				
				if (p.room != null) {
				
				b.textL(Dic.¤¤Available);
				b.textL(p.room.info.names);
				b.tab(6);
				b.add(GFORMAT.iofkInv(b.text(), p.ser.punishTotal()-p.ser.punishUsed(), p.ser.punishTotal()));
				}
			}

			
			@Override
			protected void clickA() {
				if (p.room != null && VIEW.s().ui.rooms.open(p.room) != null) {
					VIEW.s().panels.addDontRemove(momy, VIEW.s().ui.rooms.open(p.room));
				}
			}

			
		};
		
		return h;
		
		
	}
	
	private RENDEROBJ check(Race race, Extra p, int width, int height) {
		
		if (race == null && p == LAW.process().prosecute)
			return new RENDEROBJ.RenderDummy(width, height);
		
		final StatLaw stat = stat(p);
		
		ACTION a = new ACTION() {
			
			@Override
			public void exe() {
				p.allowed.toggle(race);
			}
		};
		
		HOVERABLE h = new GButt.Checkbox() {
			
			@Override
			protected void renAction() {
				activeSet(p == LAW.process().arrests || LAW.process().arrests.allowed.is(race));
				selectedSet(p.allowed.is(race));
			}
			
			@Override
			protected void clickA() {
				if (p == LAW.process().prosecute && !p.allowed.is(race)) {
					VIEW.inters().yesNo.activate(¤¤Prosecute, a, ACTION.NOP, true);
				}else {
					a.exe();
				}
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {

				GBox b = (GBox) text;
				b.title(p.action);
				b.text(p.desc);
				b.NL(8);
				
				hoverStanding(b, stat, race);
				
				b.textL(¤¤toggle);
				if (race != null) {
					b.textLL(race.info.names);
				}else {
					b.textLL(¤¤everyone);
				}
				b.NL(8);
				
				b.textL(¤¤CurrentRate);
				b.tab(6);
				b.add(GFORMAT.perc(b.text(), p.rate(race).getD()));
			}
			
		}.setDim(width, height);
		
		return h;
		
		
	}
	
	private void hoverStanding(GBox b, STAT stat, Race race) {
		if (stat != null && race != null) {
			b.textLL(STANDINGS.CITIZEN().fullfillment.info().name);
			b.NL();
			StandingData def = stat.standing().definition(race).get(HCLASSES.CITIZEN());
			
			int am = 0;
			COLOR c = GCOLOR.UI().GOOD.hovered;
			SPRITE sp = SPRITES.icons().s.arrowUp;
			
			if (def.to > def.from) {
				am = (int) Math.ceil(6*def.max/maxStanding);
			}else if(def.to < def.from) {
				am = (int) Math.ceil(6*def.max/minStanding);
				c = GCOLOR.UI().BAD.hovered;
				sp = SPRITES.icons().s.arrowDown;
			}
			for (int i = 0; i < am; i++) {
				
				b.add(sp, c);
				b.rewind(8);
			}
			
			b.NL(8);
		}
	}
	
	
	
}