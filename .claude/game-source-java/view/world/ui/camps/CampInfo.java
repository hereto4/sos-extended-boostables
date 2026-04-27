package view.world.ui.camps;

import game.faction.FACTIONS;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.sprite.SPRITE;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GHeader;
import util.gui.misc.GMeter;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.info.GFORMAT;
import util.text.D;
import util.text.Dic;
import world.WORLD;
import world.entity.haven.WHaven;
import world.entity.haven.WHavenType;

final class CampInfo extends GuiSection{

	private final WHavenType type;
	
	private static CharSequence ¤¤notFull = "¤Fulfill this species requirements to unlock the help of the havens that are within your realm.";
	private static CharSequence ¤¤full = "¤The requirements have been met and the havens on your lands are at your service.";
	private static CharSequence ¤¤Replenish = "Replenish";
	
	private static CharSequence ¤¤unlocked = "This haven is on your lands and at your service.";
	private static CharSequence ¤¤onLands = "This haven is on your lands, but the requirements are not met for them to join your cause.";
	private static CharSequence ¤¤distant = "This haven is not on your lands and can not serve you.";
	
	static {
		D.ts(CampInfo.class);
	}
	
	public CampInfo(WHavenType type) {
		this.type = type;
		add(type.race.appearance().iconBig, 0, 0);
		add(new GHeader(type.race.info.names, UI.FONT().S), getLastX2()+8, 0);
		
		add(new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.iofkInv(text, WORLD.camps().current(FACTIONS.player(), type), WORLD.camps().max(FACTIONS.player(), type));
			}
		}.hh(SPRITES.icons().s.human), getLastX1(), getLastY2());
		
		addRightC(64, new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.f0(text, WORLD.camps().replenishPerDay(FACTIONS.player(), type));
			}
		}.hh(SPRITES.icons().s.clock));
		
		addRightC(64, new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.i(text, WORLD.camps().camps(FACTIONS.player(), type));
			}
		}.hh(SPRITES.icons().s.house));
		
		body().incrW(48);
		
		addRelBody(4, DIR.S, new SPRITE.Imp(body().width(), 12) {
			
			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				double d = type.reqsFrom.progress(null);
				if (d >= 1) {
					GMeter.render(r, GMeter.C_BLUE, type.amount(), X1, X2, Y1, Y2);
				}else  {
					GMeter.render(r, GMeter.C_ORANGE, d, X1, X2, Y1, Y2);
				}
			}
		});
		
		pad(16, 8);
		
	}
	
	@Override
	public void hoverInfoGet(GUI_BOX text) {
		GBox b = (GBox) text;
		b.title(type.race.info.names);
		b.text(type.race.info.desc);
		
		b.NL(4);
		if (type.reqsFrom.passes(null))
			b.add(b.text().normalify2().add(¤¤full));
		else
			b.add(b.text().warnify().add(¤¤notFull));
		
		b.NL(8);
		b.textLL(Dic.¤¤havens);
		b.tab(6);
		b.add(GFORMAT.i(b.text(), WORLD.camps().camps(FACTIONS.player(), type)));
		
		b.NL();
		b.textLL(Dic.¤¤Population);
		b.tab(6);
		b.add(GFORMAT.iofkInv(b.text(), WORLD.camps().current(FACTIONS.player(), type), WORLD.camps().max(FACTIONS.player(), type)));
		
		b.NL();
		b.textLL(¤¤Replenish);
		b.tab(6);
		b.add(GFORMAT.f0(b.text(), WORLD.camps().replenishPerDay(FACTIONS.player(), type)));
		
		

		
		
		b.NL(8);
		type.reqsFrom.hover(text, null);
		b.NL(8);
		
		
		b.sep();
		b.textLL(Dic.¤¤Progress);
		b.NL();
		type.hoverProgress(b);
		
		super.hoverInfoGet(text);
	}
	
	@Override
	public void render(SPRITE_RENDERER r, float ds) {
		GButt.ButtPanel.renderBG(r, true, false, hoveredIs(), body());
		super.render(r, ds);
		GButt.ButtPanel.renderFrame(r, body());
	}
	
	static void hover(GUI_BOX box, WHaven ins) {
		GBox b = (GBox) box;
		b.title(ins.name);
		b.NL();
		b.textL(ins.type().race.info.names);
		b.tab(5);
		b.add(GFORMAT.i(b.text(), ins.pop()));
		b.NL(8);
		
		if (ins.faction() != FACTIONS.player())
			b.add(b.text().warnify().add(¤¤distant));
		else if (ins.type().reqsFrom.passes(null))
			b.add(b.text().normalify2().add(¤¤unlocked));
		else
			b.add(b.text().warnify().add(¤¤onLands));
	}
	
}
