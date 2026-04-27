package view.world.ui.battle;

import game.faction.FACTIONS;
import game.faction.royalty.opinion.ROPINIONS;
import init.constant.Config;
import init.race.RACES;
import init.race.Race;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.sprite.SPRITES;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import init.type.HCLASSES;
import init.type.HTYPES;
import settlement.stats.STATS;
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
import util.data.DOUBLE;
import util.data.INT.INTE;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GHeader;
import util.gui.misc.GMeter;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.slider.GSliderInt;
import util.gui.table.GRows;
import util.gui.table.GScrollRows;
import util.info.GFORMAT;
import util.text.D;
import util.text.Dic;
import world.army.AD;
import world.battle.spec.BATTLE_RESULT;
import world.battle.spec.WBattleResult;
import world.battle.spec.WBattleSide;
import world.battle.spec.WBattleUnit;

class Res extends GuiSection{

	private static CharSequence ¤¤Victory = "Victory";
	private static CharSequence ¤¤victoryD = "¤The gods have smiled upon your name. Victory is ours and our foe has been beaten.";
	private static CharSequence ¤¤Retreat = "Retreat";
	private static CharSequence ¤¤RetreatD = "¤Our army has retreated to fight another day.";
	private static CharSequence ¤¤Defeat = "¤Defeat";
	private static CharSequence ¤¤DefeatD = "¤A dark day in the annals. The enemy has snatched victory from us.";
	private static CharSequence ¤¤RetreatDefeat = "¤Our army attempted to retreat, but was destroyed in the process.";
	private static CharSequence ¤¤Capture = "Capture";
	private static CharSequence ¤¤CaptureD = "Ship the selected captives to your capital.";
	private static CharSequence ¤¤Execute = "Execute";
	private static CharSequence ¤¤ExecuteD = "Execute the selected captives.";
	private static CharSequence ¤¤Release = "Release";
	private static CharSequence ¤¤ReleaseD = "Have mercy and release all captives. Surely they will never bear arms against you again?";
	private static CharSequence ¤¤eret = "¤Enemy Retreats";
	private static CharSequence ¤¤eretD = "¤Enemy forces trembled before our might and ran before any engagement. We managed to hunt some down and plunder their baggage train.";
	
	public static final int width = 600;
	
	static {
		D.ts(Res.class);
	}

	private Slaves slaves;
	private Spoils spoils;
	private final CharSequence name;
	
	Res(ACTION close, WBattleResult result, boolean enemyRetreats){
		
		CharSequence desc = null;
		if (result.result == BATTLE_RESULT.VICTORY) {
			if (enemyRetreats) {
				name = ¤¤eret;
				desc = ¤¤eretD;
			}else {
				name = ¤¤Victory;
				desc = ¤¤victoryD;
			}
		}else if (result.result == BATTLE_RESULT.RETREAT) {
			name = ¤¤Retreat;
			desc = result.player.losses() >= result.player.men() ? ¤¤RetreatDefeat : ¤¤RetreatD;
		}else{
			name = ¤¤Defeat;
			desc = ¤¤DefeatD;
		}
		
		CharSequence[] descs = UI.FONT().M.getRows(desc, width);
		
		for (CharSequence d : descs) {
			GText t = new GText(UI.FONT().M, d);
			t.warnify();
			addRelBody(4, DIR.S, t);
		}
		
		addRelBody(16, DIR.S, result(result.player, result.enemy));
		
		if (result.result == BATTLE_RESULT.VICTORY) {
			
			spoils = new Spoils(result.lostResources, new DOUBLE.DoubleImp().setD(1.0));
			addRelBody(16, DIR.S, spoils);
			slaves = new Slaves(result.capturedRaces, new DOUBLE.DoubleImp().setD(1.0));
			addRelBody(16, DIR.S, slaves);
			
		}
		
		addRelBody(16, DIR.S, new Battle.Butt(SPRITES.icons().m.ok, Dic.¤¤Accept) {
			
			@Override
			protected void clickA() {
				close.exe();
				if (false)
					;//we need a cooldown for chivalry here since it can be cheesed by releasing the captives over and over again. No, it's actually in the next stage... Actually its in both. Catch a city and see.
				if (result.result == BATTLE_RESULT.VICTORY) {
					AD.stats().mercy().incD(FACTIONS.player(), slaves.mercy());
					result.accept(slaves.accepted(), spoils.accepted());
				}else {
					result.accept(null, null);
				}
				
			}
			
		});

	}
	
	@Override
	public void render(SPRITE_RENDERER r, float ds) {
		super.render(r, ds);
		int w = UI.FONT().H2.width(name);
		UI.PANEL().titleBoxes[1].renderCY(r, body().cX()-w/2, body().y1()-16, w);
		GCOLOR.T().H1.bind();
		UI.FONT().H2.renderC(r, body().cX(), body().y1()-16, name);
		COLOR.unbind();
	}
	
	private static RENDEROBJ result(WBattleSide player, WBattleSide enemy) {

		GuiSection s = new GuiSection();
		s.add(rSide(player));
		s.add(rSide(enemy), width / 2, 0);
		return s;
	}

	private static RENDEROBJ rSide(WBattleSide player) {

		LinkedList<RENDEROBJ> rows = new LinkedList<>();
		for (int i = 0; i < player.units().size(); i++) {
			rows.add(new UnitLoss(player.units().get(i)));
		}

		return new GScrollRows(rows, rows.get(0).body().height() * 5).view();
	}
	

	
	private static class UnitLoss extends GuiSection {

		private static int width = 300 - 50;
		private final WBattleUnit u;

		public UnitLoss(WBattleUnit u) {
			this.u = u;

			addRightC(8, new SPRITE.Imp(Icon.M) {

				@Override
				public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
					u.icon().render(r, X1, X2, Y1, Y2);
				}
			});

			addRightC(8, new SPRITE.Imp(200, 16) {

				@Override
				public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
					double dmen = Math.sqrt((double) u.men() / Config.battle().MEN_PER_ARMY);
					X2 = (int) (X1 + (X2 - X1) * dmen);

					double d = (double) (u.men() - u.losses()) / u.men();
					GMeter.render(r, GMeter.C_ORANGE, d, X1, X2, Y1, Y2);
				}
			});

			body().setWidth(width);

		}

		@Override
		public void hoverInfoGet(GUI_BOX text) {
			u.hover(text);
			GBox b = (GBox) text;
			b.NL(16);
			b.text(Dic.¤¤Losses);
			b.add(GFORMAT.iofk(b.text(), u.losses(), u.men()));
		}

	}
	
	private static class Spoils extends GuiSection {

		private int[] accepted = new int[RESOURCES.ALL().size()];
		private final int[] available;
		private final DOUBLE mul;
		
		
		Spoils(int[] resources, DOUBLE mul) {
			this.available = resources;
			this.mul = mul;
			int am = 4;
			GRows rows = new GRows(am);
			GText t = new GText(UI.FONT().S, 16);
			for (RESOURCE res : RESOURCES.ALL()) {
				if (resources[res.index()] != 0) {

					rows.add(new HOVERABLE.HoverableAbs(width / am - 12, 28) {

						@Override
						protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
							res.icon().medium.renderCY(r, body().x1() + 8, body().cY());
							t.clear();
							GFORMAT.iIncr(t, (long) (resources[res.index()] * mul.getD()));
							t.renderCY(r, body().x1() + 40, body().cY());
						}

						@Override
						public void hoverInfoGet(GUI_BOX text) {
							text.title(res.names);
						}
					});

				}
			}
			add(new GScrollRows(rows.rows(), 28 * 4).view());
		}

		public int[] accepted() {
			for (int i = 0; i < accepted.length; i++) {
				accepted[i] = CLAMP.i((int) (available[i] * mul.getD()), 0, available[i]);
			}
			return accepted;
		}
	}
	
	private static class Slaves extends GuiSection {

		private int[] accepted = new int[RACES.all().size()];
		private final DOUBLE mul;
		private final int[] available;
		
		private boolean enslave = true;
		private boolean release = false;

		public Slaves(int[] available, DOUBLE mul) {
			
			add(new GHeader(HTYPES.PRISONER().names));
			
			this.mul = mul;
			this.available = available;
			// add(new GHeader(DicMisc.¤¤Captives).hoverInfoSet(DicMisc.¤¤CaptivesD));
			//
			int am = 2;
			GRows rows = new GRows(am);

			for (Race race : RACES.all()) {

				if (available[race.index] == 0)
					continue;
				
				accepted[race.index] = available[race.index];

				INTE in = new INTE() {

					@Override
					public int min() {
						return 0;
					}

					@Override
					public int max() {
						
						return (int) (available[race.index] * mul.getD());
					}

					@Override
					public int get() {
						if (release)
							return 0;
						return CLAMP.i(accepted[race.index], 0, max());
					}

					@Override
					public void set(int t) {
						accepted[race.index] = t;

					}
				};

				GSliderInt t = new GSliderInt(in, 200 - 24, active) {
					@Override
					public void hoverInfoGet(GUI_BOX text) {
						text.title(race.info.names);
						super.hoverInfoGet(text);
						text.NL();
						text.text(Dic.¤¤CaptivesD);
					}
				};

				t.addRelBody(4, DIR.W, race.appearance().icon.medium);
				t.body().incrW(24);
				t.pad(0, 2);
				rows.add(t);
			}

			addRelBody(4, DIR.S, new GScrollRows(rows.rows(), 28 * 4).view());
			
			GuiSection s = new GuiSection();
			
			s.add(new GButt.ButtPanel(¤¤Capture, 180) {
				
				@Override
				protected void clickA() {
					enslave = true;
					release = false;
				}
				
				@Override
				protected void renAction() {
					selectedSet(enslave && !release);
				}
				
			}.hoverInfoSet(¤¤CaptureD));
			s.addRightC(0, new GButt.ButtPanel(¤¤Execute, 180) {
				
				@Override
				protected void clickA() {
					enslave = false;
					release = false;
				}
				
				@Override
				protected void renAction() {
					selectedSet(!enslave && !release);
				}
				
			}.hoverInfoSet(¤¤ExecuteD));
			
			s.addRightC(0, new GButt.ButtPanel(¤¤Release, 180) {
				
				@Override
				protected void clickA() {
					release = true;
					enslave = false;
				}
				
				@Override
				protected void renAction() {
					selectedSet(release);
				}
				
			}.hoverInfoSet(¤¤ReleaseD));
			
			addRelBody(4, DIR.S, s);
			
			addRelBody(4, DIR.S, new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.percInc(text, mercy());
				}
			}.hv(ROPINIONS.STANCE().chivalry.info.name));
			
			pad(8);
			
		}

		public int[] accepted() {
			if (!enslave || release) {
				for (int i = 0; i < accepted.length; i++) {
					accepted[i] = 0;
				}
			}else {
				for (int i = 0; i < accepted.length; i++) {
					accepted[i] = CLAMP.i(accepted[i], 0, (int) (available[i] * mul.getD()));
				}
			}
			
			return accepted;
		}
		
		public int mercyAmount() {
			int tot = 0;
			int sel = 0;
			for (int i = 0; i< accepted.length; i++) {
				int t = (int) (available[i]*mul.getD());
				tot +=t;
				sel += release ? t : CLAMP.i(accepted[i], 0, t);
			}
			if (tot == 0)
				return 0;
			
			
			if (release)
				sel = tot;
			else if(enslave)
				sel = -sel/2;
			else
				sel = -sel;
			
			return sel;
		}
		
		
		
		
		
		public double mercy() {

			double m = mercyAmount();
			m /= 1 + STATS.POP().POP.data(HCLASSES.CITIZEN()).get(null);
			return m;
		}
		
//		private int mercyValue() {
//			int tot = 0;
//			int sel = 0;
//			for (int i = 0; i< accepted.length; i++) {
//				tot += available[i];
//				sel += release ? 0 : accepted[i];
//			}
//			if (tot == 0)
//				return 0;
//			if (sel == 0)
//				return tot;
//			if (enslave)
//				return tot-sel*4;
//			return tot-sel*16;
//		}
//		
//		public int mercyExtra() {
//			return 0;
//		}
		
//		public void execute() {
//			AD.stats().mercy().incD(FACTIONS.player(), mercy());
//		}
		
		@Override
		public void render(SPRITE_RENDERER r, float ds) {
			GButt.ButtPanel.renderFrame(r, body());
			super.render(r, ds);
		}
		
	}
	
}
