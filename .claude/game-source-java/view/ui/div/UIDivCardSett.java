package view.ui.div;

import java.util.Arrays;

import game.GAME;
import game.battle.div.Div;
import init.constant.Config;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import init.type.HTYPES;
import settlement.entity.EntityIterator;
import settlement.entity.humanoid.Humanoid;
import settlement.room.military.training.ROOM_M_TRAINER;
import settlement.stats.STATS;
import settlement.stats.colls.StatsBattle.StatTraining;
import settlement.stats.equip.EquipBattle;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.DIMENSION;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.Rec;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.text.Str;
import util.colors.GCOLOR;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GMeter;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.info.GFORMAT;
import util.text.D;
import util.text.Dic;
import world.army.AD;

public final class UIDivCardSett implements DIMENSION{

	private static CharSequence ¤¤needs = "¤Needs to Train";
	private static CharSequence ¤¤fully = "¤Fully Trained";
	private static CharSequence ¤¤currently = "¤Currently Training";
	
	private static CharSequence ¤¤army = "¤Division is currently attached to the world army '{0}'. It must be recalled in order to be edited;";
	private static CharSequence ¤¤armyTime = "¤Division is returning home to our capital. The soldiers will arrive in {0} days.";
	static {
		D.ts(UIDivCardSett.class);
	}
	
	private final Rec body = new Rec();
	private final int WIDTH;
	private final int HEIGHT;
	private final UIDiv m;
	private final TrainingSpec spec = new TrainingSpec();
	
	private GuiSection sec = new GuiSection();
	private final UIDivStats stat = new UIDivStats();
	private Div current;
	
	UIDivCardSett(UIDiv m) {
		this.m = m;
		WIDTH = m.WIDTH;
		HEIGHT =m.HEIGHT + 20;
		
		{
			GuiSection s = new GuiSection();
			
			for (EquipBattle e : STATS.EQUIP().BATTLE_ALL()) {
				SPRITE hh = new SPRITE.Imp(Icon.M) {
					
					@Override
					public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
						if (current.info.equipI(e) == 0) {
							OPACITY.O50.bind();
						}
						e.resource.icon().render(r, X1, X2, Y1, Y2);
						OPACITY.unbind();
						
					}
				};
				
				RENDEROBJ o = new GStat() {
					
					@Override
					public void update(GText text) {
						if (current.info.equipI(e) == 0) {
							text.color(COLOR.WHITE50).add('-');
						} else {
							GFORMAT.f(text, ((int)10*(current.info.equipI(e)))/10.0);
						}
					}
				}.hh(hh);
				s.addGrid(o, e.indexMilitary(), 4, 48, 0);
				
			}
			
			GCOLOR.T().H1.bind();
			s.add(UI.icons().s.death, 0, s.body().y2()+2);
			
			s.addRightC(4, new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.percGood(text, ((int)100*(current.info.experience()))/100.0);
				}
			}.hh(Dic.¤¤Experience, 220));
			
			for (StatTraining tt : STATS.BATTLE().TRAINING_ALL) {
				s.add(tt.room.icon.small, 0, s.body().y2()+2);
				s.addRightC(4, new GStat() {
					
					@Override
					public void update(GText text) {
						
						int target = ((int)(100*current.info.training(tt)));
						int cu = (int) Math.round(100*tt.stat.div().getD(current));
						
						text.add(cu).add('/').add(target).add('%');
						if (target > 0)
							text.color(ColorImp.TMP.interpolate(GCOLOR.T().IBAD, GCOLOR.T().IGOOD, (double)cu/target));
						else
							text.color(GCOLOR.T().INACTIVE);
					}
				}.hh(tt.stat.info().name, 200));
				
			}
			
			s.add(UI.icons().s.sword, 0, s.body().y2()+8);
			s.addRightC(4, new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.iofk(text, current.menNrOf(), current.info.men());
					
				}
			}.hh(Dic.¤¤Deployable, 200));
			
			s.add(UI.icons().s.fist, 0, s.body().y2()+2);
			s.addRightC(4, new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.iofk(text, STATS.BATTLE().RECRUIT.inDiv(current), current.info.men()-current.menNrOf());
					
				}
			}.hh(Dic.¤¤Recruits, 200));
			
			
			sec.add(s);
			
			sec.addRelBody(8, DIR.W, new RENDEROBJ.RenderImp(WIDTH*2, HEIGHT*2) {
				
				@Override
				public void render(SPRITE_RENDERER r, float ds) {
					UIDivCardSett.this.render(r, body.x1(), body.y1(), 2, current, true, false, false);
				}
			});
		}
		
	}
	
	@Override
	public int width() {
		return WIDTH;
	}

	@Override
	public int height() {
		return HEIGHT;
	}
	
	public void render(SPRITE_RENDERER r, int x1, int y1, int scale, Div d, boolean isActive, boolean isSelected, boolean isHovered) {
		
		
		body.set(x1,x1+width()*scale, y1, y1+height()*scale);
		GButt.ButtPanel.renderBG(r, isActive, isSelected, isHovered, body);
		
		m.renderBasics(r, x1, y1, scale, d.info);
		body.set(x1,x1+width()*scale, y1, y1+height()*scale);
		int cx = body.cX();
		
		double men = d.info.men();
		double n = d.menNrOf();
		
		if (men == 0)
			GMeter.renderDelta(r, 0, 0, body);
		else
			GMeter.renderDelta(r, n/men, (n+STATS.BATTLE().RECRUIT.inDiv(d))/men, body.x1()+4*scale, body.x2()-4*scale, body.y2()-26*scale, body.y2()-12*scale);
		
		double trTarget = 0;
		double tr = 0;
		
		for (StatTraining tt : STATS.BATTLE().TRAINING_ALL) {
			trTarget += d.info.training(tt);
			tr += tt.stat.div().getD(d);
		}
		GMeter.render(r, GMeter.C_GRAY, tr/trTarget, body.x1()+4*scale, body.x2()-4*scale, body.y2()-12*scale, body.y2()-4*scale);
		
		
		UI.FONT().S.renderC(r, cx, body.y2()-18*scale, Str.TMP.clear().add(d.info.men()), scale);
		
		if (AD.cityDivs().attachedArmy(d) != null){
			OPACITY.O50.bind();
			COLOR.BLACK.render(r, body, -4);
			OPACITY.unbind();
			UI.icons().m.arrow_left.renderCScaled(r, body.cX(), body.cY(), scale);
		}else if (AD.cityDivs().daysToReturn(d) > 0) {
			OPACITY.O50.bind();
			COLOR.BLACK.render(r, body, -4);
			OPACITY.O75TO100.bind();
			UI.icons().m.time.renderCScaled(r, body.cX(), body.cY(), scale);
			OPACITY.unbind();
		}
		
		GCOLOR.UI().border().renderFrame(r, body, 0, 1);
		
		
		
	}
	

	
	public void hover(GUI_BOX box, Div div) {
		GBox b = (GBox) box;
		
		b.title(div.info.name());
		
		current = div;
		b.add(sec);

		b.sep();

		b.add(stat.get(div.info));
		b.sep();
		
		if (AD.cityDivs().attachedArmy(div) != null){
			GText t = b.text().warnify();
			t.add(¤¤army);
			t.insert(0, AD.cityDivs().attachedArmy(div).name);
			b.add(t);
			b.NL(8);
		}else if (AD.cityDivs().daysToReturn(div) >= 0){
			GText t = b.text().warnify();
			t.add(¤¤armyTime);
			t.insert(0, (int)Math.ceil(AD.cityDivs().daysToReturn(div)));
			b.add(t);
			b.NL(8);
			
			
		}
		
		b.sep();

		
		b.textLL(¤¤needs);
		b.tab(6);
		b.add(GFORMAT.i(b.text(), needsTraining(div)));
		b.NL();
		
		b.textLL(¤¤currently);
		b.tab(6);
		b.add(GFORMAT.i(b.text(), training(div)));
		b.NL();
		
		b.textLL(¤¤fully);
		b.tab(6);
		b.add(GFORMAT.i(b.text(), div.info.men() -  needsTraining(div)));
		b.NL();
		
		
		
		//div.hoverInfo((GBox)box);
	}
	
	final class TrainingSpec {

		private  int upI = -1;
		
		private final int[] needsTraining = new int[Config.battle().DIVISIONS_PER_ARMY];
		private final int[] training = new int[Config.battle().DIVISIONS_PER_ARMY];
		
		private final EntityIterator.Humans iter = new EntityIterator.Humans() {
			
			@Override
			protected boolean processAndShouldBreakH(Humanoid h, int ie) {
				if (h.indu().clas().player)
					count(h);
				return false;
				
			}
			
			private void count(Humanoid h) {
				Div div = STATS.BATTLE().DIV.get(h);
				if (div != null) {
					if (h.indu().hType() == HTYPES.RECRUIT()) {
						training[div.indexArmy()] ++;
						needsTraining[div.indexArmy()] ++;
					}else {
						for (ROOM_M_TRAINER<?> tra : ROOM_M_TRAINER.ALL()) {
							if (tra.training().shouldTrain(h.indu(), div.info.training(tra.training()), false)) {
								needsTraining[div.indexArmy()] ++;
								return;
							}
							
						}
					}
				}else {
					div = STATS.BATTLE().RECRUIT.get(h);
					if (div != null) {
						if (h.indu().hType() == HTYPES.RECRUIT()) {
							training[div.indexArmy()] ++;
							needsTraining[div.indexArmy()] ++;
						}
					}
				}
				
			}
			
		};
		
		private void init() {
//			if (GAME.updateI() == upI)
//				return;
//			Arrays.fill(needsTraining, 0);
//			Arrays.fill(training, 0);
//			iter.iterate();
//			upI = GAME.updateI();
//			
			if (GAME.updateI() == upI)
				return;
			//int mul = ROOM_M_TRAINER.ALL().size();
			for (int di = 0; di < GAME.ARMIES().player().divisions().size(); di++) {
				Div d = GAME.ARMIES().player().divisions().get(di);
				
				needsTraining[di] = d.info.men()-(STATS.BATTLE().DIV.stat().div().get(d)+STATS.BATTLE().RECRUIT.inDiv(d));
				if (AD.cityDivs().attachedArmy(d) != null) {
					needsTraining[di] -= AD.cityDivs().get(d).men();
				}
			}
			
			Arrays.fill(training, 0);
			iter.iterate();
			upI = GAME.updateI();
		}
		

		
	}
	
	public int training(Div div) {
		spec.init();
		return spec.training[div.indexArmy()];
	}
	
	public int needsTraining(Div div) {
		spec.init();
		return spec.needsTraining[div.indexArmy()];
	}
	
}
