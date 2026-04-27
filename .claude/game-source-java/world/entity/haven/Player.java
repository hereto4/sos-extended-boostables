package world.entity.haven;

import java.io.IOException;
import java.util.Arrays;

import game.faction.FACTIONS;
import game.faction.Faction;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import settlement.main.SETT;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import snake2d.util.gui.GuiSection;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.text.Str;
import util.gui.misc.GButt;
import util.text.D;
import view.main.VIEW;
import view.ui.message.MessageSection;
import view.ui.message.MessageText;
import world.WORLD;
import world.entity.WEntity;

class Player implements SAVABLE{

	private static CharSequence ¤¤titleNew = "Haven controlled";
	private static CharSequence ¤¤bodyNew = "We now have under our control a haven. Havens are bastions containing powerful races that you can sway to your cause if you fulfill their requirements.";
	
	private static CharSequence ¤¤titleMore = "More {0} join you";
	private static CharSequence ¤¤bodyMore = "Since your worth has increased in the eyes of your {0}. More are willing to join your cause. Make sure you accept them as immigrants.";
	
	private static CharSequence ¤¤titleLess = "{0} are leaving!";
	private static CharSequence ¤¤bodyLess = "Since you've failed to uphold the standards of your {0} havens, many have now stopped supporting you, and all its members will start to return home.";
	
	static {
		D.ts(Player.class);
	}
	
	private double[] oldValue;
	private boolean hasAny;
	private double upT;

	Player(WHavens havens){
		oldValue = new double[havens.types.size()];
	}
	
	@Override
	public void save(FilePutter file) {
		file.dsE(oldValue);
		file.d(upT);
		file.bool(hasAny);
	}
	@Override
	public void load(FileGetter file) throws IOException {
		file.dsE(oldValue);
		upT = file.d();
		hasAny = file.bool();
	}
	@Override
	public void clear() {
		Arrays.fill(oldValue, 0);
		upT = 0;
		hasAny = false;
	}
	
	void update(double ds) {
		
		upT += ds*0.1;
		if (upT < 10)
			return;
		upT -= 10;
		
		for (WHavenType t : WORLD.ENTITIES().havens.types) {
			if (update(t))
				return;
		}
		
	}
	
	private boolean update(WHavenType t) {

		if (WORLD.ENTITIES().havens.camps(FACTIONS.player(), t) == 0)
			return false;
		
		if (!hasAny && WORLD.ENTITIES().havens.camps(FACTIONS.player(), t) > 0) {
			hasAny = true;
			new MessOwn().send();
			return true;
		}
		
		double old = oldValue[t.index()];
		
		double nn = t.amount();

		if (nn <= 0 && old > 0) {
			if (t.reqsFrom.progress(FACTIONS.player()) < 0.75) {
				oldValue[t.index()] = 0;
				new MessageText(new Str(¤¤titleLess).insert(0, t.race.info.names), new Str(¤¤bodyLess).insert(0, t.race.info.names)).send();
				return true;
			}
			return false;
		}
		
		int iold = (int) Math.round(old*5);
		int inew = (int) Math.round(nn*5);
		
		if (inew > iold) {
			SETT.ENTRY().immi().setHigher(t.race, (int)(WORLD.camps().max(FACTIONS.player(), t)*nn));
			if (iold == 0) {
				new MessChange(t, new Str(¤¤titleMore).insert(0, t.race.info.names), t.sJoin).send();
			}else
				new MessChange(t, new Str(¤¤titleMore).insert(0, t.race.info.names), new Str(¤¤bodyMore).insert(0, t.race.info.names)).send();
			oldValue[t.index()] = nn;
			return true;
		}else if (inew < iold-1) {
			new MessChange(t, new Str(¤¤titleLess).insert(0, t.race.info.names), t.sLeave).send();
			oldValue[t.index()] = nn;
			return true;
		}
		
		return false;
		
	}
	
	double get(Faction f, WHavenType t) {
		if (f == FACTIONS.player())
			return oldValue[t.index()];
		return 1.0;
	}
	
	private static class MessOwn extends MessageSection{

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		final int tx;
		final int ty;
		
		public MessOwn() {
			super(¤¤titleNew);
			WHaven f = first();
			this.tx = f.ctx();
			this.ty = f.cty();
		}

		private WHaven first() {
			for (WEntity e : WORLD.ENTITIES().allSlow()) {
				if (e.faction() == FACTIONS.player() && e instanceof WHaven) {
					return ((WHaven) e);
				}
			}
			return null;
		}
		
		@Override
		protected void make(GuiSection section) {
			paragraph(¤¤bodyNew);
			
			section.addRelBody(16, DIR.S, new GButt.ButtPanel(UI.icons().m.crossair) {
				
				@Override
				protected void clickA() {
					VIEW.world().activate();
					VIEW.world().window.setZoomout(0);
					VIEW.world().window.centererTile.set(tx, ty);
				}
				
			});
			
		}
		
		
		
	}
	
	private static class MessChange extends MessageSection{

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private final int ti;
		private final String desc;
		public MessChange(WHavenType t, CharSequence title, CharSequence desc) {
			super(title);
			ti = t.index();
			this.desc = ""+desc;
		}
		
		@Override
		protected void make(GuiSection section) {
			
			paragraph(desc);
			section.addRelBody(16, DIR.N, new SPRITE.Imp(Icon.HUGE) {
				
				@Override
				public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
					WORLD.camps().types.getC(ti).race.appearance().iconBig.render(r, X1, X2, Y1, Y2);
				}
			});
			
			
			
		}
		
		
		
	}
	
}
