package view.sett.ui.army;

import game.GAME;
import game.battle.div.Div;
import init.sprite.SPRITES;
import settlement.main.SETT;
import settlement.room.military.training.ROOM_M_TRAINER;
import settlement.stats.STATS;
import settlement.stats.equip.EquipBattle;
import settlement.stats.equip.EquipRange;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.CLAMP;
import util.gui.misc.GBox;
import util.gui.misc.GMeter;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.panel.GFrame;
import util.gui.slider.GGauge;
import util.info.GFORMAT;
import util.text.D;
import util.text.Dic;
import world.army.AD;

class Info extends GuiSection{

	private static CharSequence ¤¤RecruitD = "¤The amount of men currently training to be able to join a division.";
	private static CharSequence ¤¤SoldierD = "The amount of soldiers that are ready to be deployed in your city.";
	
	static {
		D.ts(Info.class);
	}
	
	public Info(){
		
		
		int gi = 0;
		int wi = 100;
		int hi = 58;
		int cols = 6;
		DIR al = DIR.N;
		
		addGridD(new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.iBig(text, (int)AD.cityDivs().total());
			}
			
			@Override
			public void hoverInfoGet(GBox b) {
				b.title(Dic.¤¤Campaigning);
				b.text(Dic.¤¤CampaigningD);
				b.NL();
				b.add(GFORMAT.iBig(b.text(), (int)AD.cityDivs().total()));
			};
		}.hv(SPRITES.icons().m.arrow_left), gi++, cols, wi, hi, al);
		
		addGridD(new GStat() {
		
			@Override
			public void update(GText text) {
				GFORMAT.iofk(text, (int)STATS.BATTLE().DIV.stat().data().get(null, 0), SETT.BATTLE().info.targetMen());
			}
			
			@Override
			public void hoverInfoGet(GBox b) {
				b.title(Dic.¤¤Soldiers);
				b.text(¤¤SoldierD);
				b.NL();
				b.add(GFORMAT.iBig(b.text(), (int)STATS.BATTLE().DIV.stat().data().get(null, 0)));
				b.NL(4);
				
				b.textLL(Dic.¤¤Recruits);
				b.add(GFORMAT.iBig(b.text(), (int)STATS.BATTLE().RECRUIT.stat().data().get(null, 0)));
				b.NL();
				b.text(¤¤RecruitD);
			
			};
		}.hv(SPRITES.icons().m.sword), gi++, cols, wi, hi, al);
		
		for (ROOM_M_TRAINER<?> ro : ROOM_M_TRAINER.ALL()) {
			addGridD(new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.iofk(text, ro.employment().employed(), ro.employment().neededWorkers());
				}
				
				@Override
				public void hoverInfoGet(GBox b) {
					b.title(ro.tInfo.name);
					b.text(ro.tInfo.desc);
					b.NL(4);
					b.textLL(ro.info.names);
					b.add(GFORMAT.iofk(b.text(), ro.employment().employed(), ro.employment().neededWorkers()));
					
					b.NL(8);
					b.NL();
					
					ro.boosters.hover(b, 1.0, -1);

				};
			}.hv(ro.icon), gi++, cols, wi, hi, al);
		}

		
		
		for (EquipBattle e : STATS.EQUIP().BATTLE_MELEE()){
			
			GGauge g = new GGauge(40, 16, GMeter.C_REDGREEN) {
				
				@Override
				public double getD() {
					int needs = 0;
					double has = SETT.ROOMS().STOCKPILE.tally().amountTotal(e.resource());
					for (Div d : GAME.ARMIES().player().divisions()) {
						has += e.stat().div().get(d);
						needs += d.info.men()*e.target(d);
					}
					return CLAMP.d(has/needs, 0, 1);
				}
			};
			addGridD(new CLICKABLE.Pair(new RENDEROBJ.Sprite(e.resource.icon()), new RENDEROBJ.Sprite(g), DIR.S, 4) {
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					GBox b = (GBox) text;
					b.title(e.stat().info().name);
					b.text(e.stat().info().desc);
					b.NL(4);
					int needs = 0;
					int has = SETT.ROOMS().STOCKPILE.tally().amountTotal(e.resource());
					for (Div d : GAME.ARMIES().player().divisions()) {
						
						has += e.stat().div().get(d);
						needs += d.info.men()*e.target(d);
					}
					b.add(GFORMAT.iofkInv(b.text(), has, needs));
					b.NL(4);
					b.textLL(Dic.¤¤Boosts);
					b.NL(4);
					e.stat().boosters.hover(text, 1.0, -1);
					
				}
			}, gi++, cols, wi, hi, al);
			
			
		}
		
		for (EquipRange e : STATS.EQUIP().RANGED()){
			GGauge g = new GGauge(48, 16, GMeter.C_REDGREEN) {
				
				@Override
				public double getD() {
					int needs = 0;
					double has = SETT.ROOMS().STOCKPILE.tally().amountTotal(e.resource());
					for (Div d : GAME.ARMIES().player().divisions()) {
						has += e.stat().div().get(d);
						needs += d.info.men()*e.target(d);
					}
					return CLAMP.d(has/needs, 0, 1);
				}
			};
			
			addGridD(new CLICKABLE.Pair(new RENDEROBJ.Sprite(e.resource.icon()), new RENDEROBJ.Sprite(g), DIR.S, 4) {
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					GBox b = (GBox) text;
					b.title(e.stat().info().name);
					b.text(e.stat().info().desc);
					b.NL(4);
					int needs = 0;
					int has = SETT.ROOMS().STOCKPILE.tally().amountTotal(e.resource());
					for (Div d : GAME.ARMIES().player().divisions()) {
						has += e.stat().div().get(d);
						needs += d.info.men()*e.target(d);
					}
					b.add(GFORMAT.iofkInv(b.text(), has, needs));
					b.NL(4);
					e.hover(text);
				}
			}, gi++, cols, wi, hi, al);
			
		}

		addRelBody(6, DIR.S, GFrame.separator(body().width()));
	}
	
}
