package view.ui.div;

import game.GAME;
import game.battle.div.Div;
import game.battle.factors.DivFactor;
import game.battle.formation.DivFormation;
import game.battle.formation.DivFormationImp;
import game.battle.thread.order.BattleOrderTask;
import game.battle.thread.trajectory.BattleTrajectories;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import settlement.stats.STATS;
import settlement.stats.colls.StatsBattle.StatTraining;
import settlement.stats.equip.EquipBattle;
import settlement.stats.equip.EquipRange;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.color.OPACITY;
import snake2d.util.color.OpacityImp;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIMENSION;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.datatypes.Rec;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.TextureCoords;
import snake2d.util.sprite.text.Str;
import util.colors.GCOLOR;
import util.data.GETTER;
import util.data.GETTER.GETTER_IMP;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GMeter;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.misc.GTextR;
import util.info.GFORMAT;
import util.text.D;
import util.text.Dic;
import view.main.VIEW;

public final class UIDivCardBattle implements DIMENSION{

	public final int width;
	public final int height;
	private final UIDiv m;
	private final Rec body = new Rec();
	private final DivFormationImp forDest;
	
	private static CharSequence ¤¤Standing = "¤Standing";
	private static CharSequence ¤¤Moving = "¤Moving";
	private static CharSequence ¤¤Attacking = "¤Attacking";
	
	private static CharSequence ¤¤Building = "¤Attacking Structure";
	private static CharSequence ¤¤Charging = "¤Charging";
	private static CharSequence ¤¤NotMuster = "¤Not Mustered";
	private static CharSequence ¤¤NoPosition = "¤Unit has no valid position";
	
	private static CharSequence ¤¤AReloading = "¤Reloading";
	private static CharSequence ¤¤AAiming = "¤Aiming";
	private static CharSequence ¤¤AFiring = "¤Firing";
	
	static {
		D.ts(UIDivCardBattle.class);
	}
	
	private final GETTER_IMP<Div> h1 = new GETTER_IMP<Div>();
	private final GETTER_IMP<Div> h2 = new GETTER_IMP<Div>();
	private final GuiSection hov1;
	private final GuiSection hov2;
	
	
	UIDivCardBattle(UIDiv m) {
		this.m = m;
		width = m.WIDTH;
		height =m.HEIGHT + 8;
		forDest = new DivFormationImp();
		hov1 = hoveSection(h1);
		hov2 = hoveSection(h2);
	}
	
	@Override
	public int width() {
		return width;
	}

	@Override
	public int height() {
		return height;
	}
	
	private static COLOR cMoraleBad = new ColorImp(40, 5, 5);
	private static COLOR cMoraleWorse = new ColorImp(60, 5, 5);
	private static COLOR cMoraleGood = new ColorImp(5, 15, 40);
	
	public void render(Div div, int x1, int y1, int scale, SPRITE_RENDERER r, boolean isActive, boolean isSelected,
			boolean isHovered) {
		
		body.set(x1,x1+width()*scale, y1, y1+height()*scale);
		GButt.ButtPanel.renderBG(r, isActive, isSelected, isHovered, body);
		
		moralebg(r, scale, div, body, 4);
		
	
		
		
		m.renderBasics(r, x1, y1, scale, div.info);
		body.set(x1,x1+width()*scale, y1, y1+height()*scale);
		int cx = body.cX();
		
		double before = div.info.men()+GAME.ARMIES().factors.casulties(div);
		double men = div.info.men();
		double n = div.menNrOf();
		
		if (men == 0)
			GMeter.renderDelta(r, 0, 0, body.x1()+7*scale, body.x2()-7*scale, body.y2()-22*scale, body.y2()-12*scale);
		else
			GMeter.renderDelta(r, before/men, n/men, body.x1()+7*scale, body.x2()-7*scale, body.y2()-22*scale, body.y2()-12*scale);
		
		if (hasAmmo(div)){
			EquipRange ra = div.settings().ammo();
			double m = 0;
			if (ra != null)
				m = ra.ammoD(div);
			
			GMeter.render(r, GMeter.C_ORANGE, m, body.x1()+7*scale, body.x2()-7*scale, body.y2()-12*scale, body.y2()-4*scale);
		}
		
		if (div.menPrevious() > div.men()) {
			
			double d = div.menPrevious()-div.men();
			d /= div.menPrevious();
			double speed = d > 0.05 ? 4 : 2;
			
			double op = (speed*VIEW.renderSecond())%1.0;
			OpacityImp.TMP.set(op);
			OpacityImp.TMP.bind();

			
		}
		
		UI.FONT().S.renderC(r, cx, body.y2()-13*scale, Str.TMP.clear().add((int)n), scale);
		OPACITY.unbind();
		
		GCOLOR.UI().border().renderFrame(r, body, 0, 1);
		
		if (!div.settings().mustering() || div.position().deployed() == 0 || !isActive || div.menNrOf() <= 0) {
			OPACITY.O50.bind();
			COLOR.BLACK.render(r, x1, y1, width, height, -1);
			OPACITY.unbind();
			if (!div.settings().mustering() || div.position().deployed() == 0) {
				if (!div.settings().mustering()) {
					GCOLOR.UI().BAD.hovered.bind();
				}else {
					GCOLOR.UI().SOSO.hovered.bind();
				}
				UI.icons().s.muster.renderCScaled(r, cx, body.y1()+10, scale);
				COLOR.unbind();
			}
				
			
		}else {
			
			div.order().dest.get(forDest);
			DivFormation forCurrent = div.position();
			
			COORDINATE dd = forDest.centrePixel();
			COORDINATE cc = forCurrent.centrePixel();
			
			if (dd == null || cc == null)
				return;
			
			if (forCurrent.deployed() > 0 && forDest.deployed() > 0 && !dd.isSameAs(cc)) {
				
				GCOLOR.T().H1.bind();
				OPACITY.O25.bind();
				COLOR.WHITE100.render(r,  x1+4*scale, y1+4*scale, 16*scale, 16*scale, 0);
				OPACITY.unbind();
				if (div.settings().running)
					UI.icons().s.divRun.renderScaled(r, x1+4*scale, y1+4*scale, scale);
				else
					UI.icons().s.divWalk.renderScaled(r, x1+4*scale, y1+4*scale, scale);			
				
				if (div.status().isFighting()) {
					OPACITY.O25.bind();
					COLOR.WHITE100.render(r,  x1+18*scale, y1+4*scale, 16*scale, 16*scale, 0);
					OPACITY.unbind();
					GCOLOR.UI().BAD.hovered.bind();
					UI.icons().s.sword.renderScaled(r, x1+18*scale, y1+4*scale, scale);
				}
			}else if (div.status().isFighting()) {
				OPACITY.O25.bind();
				COLOR.WHITE100.render(r,  x1+18*scale, y1+4*scale, 16*scale, 16*scale, 0);
				OPACITY.unbind();
				GCOLOR.UI().BAD.hovered.bind();
				UI.icons().s.sword.renderScaled(r, x1+18*scale, y1+4*scale, scale);
			}else if (div.settings().shouldFire()) {
				OPACITY.O25.bind();
				COLOR.WHITE100.render(r,  x1+18*scale, y1+4*scale, 16*scale, 16*scale, 0);
				OPACITY.unbind();
				GCOLOR.T().H1.bind();
				UI.icons().s.crossheir.renderScaled(r, x1+18*scale, y1+4*scale, scale);
			}
			COLOR.unbind();
			
		}
	}
	
	private GuiSection hoveSection(GETTER<Div> g) {
		
		final UIDivStats stat = new UIDivStats();
		GuiSection s = new GuiSection() {
			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				stat.get(g.get());
				super.render(r, ds);
			}
		};
		
		
		

		s.add(new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.iofk(text, g.get().menNrOf(), g.get().info.men());
				
			}
		}.hh(UI.icons().s.sword, Dic.¤¤Deployable, 200), 0, s.body().y2()+2);
		
		for (StatTraining tt : STATS.BATTLE().TRAINING_ALL) {
			s.add(new GStat() {
				
				@Override
				public void update(GText text) {
					
					int target = ((int)(100*g.get().info.training(tt)));
					int cu = (int) Math.round(100*tt.stat.div().getD(g.get()));
					
					text.add(cu).add('/').add(target).add('%');
					if (target > 0)
						text.color(ColorImp.TMP.interpolate(GCOLOR.T().IBAD, GCOLOR.T().IGOOD, (double)cu/target));
					else
						text.color(GCOLOR.T().INACTIVE);
				}
				
				@Override
				public void hoverInfoGet(GBox b) {
					b.add(tt.stat.info());
					b.NL();
					tt.stat.boosters.hover(b, g.get());
					
				};
			}.hh(tt.room.icon.small, tt.stat.info().name, 200), 0, s.body().y2()+2);
			
		}
		
		s.add(new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.percGood(text, ((int)100*(g.get().info.experience()))/100.0);
			}
		}.hh(UI.icons().s.death, Dic.¤¤Experience, 200), 0, s.body().y2()+8);
		
		
		
		
		s.add(new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.i(text, GAME.ARMIES().factors.kills(g.get()));
				
			}
			
			@Override
			public void hoverInfoGet(GBox b) {
				b.textLL(Dic.¤¤Battle);
				b.tab(6);
				b.add(GFORMAT.i(b.text(), GAME.ARMIES().factors.kills(g.get())));
				b.NL();
				b.textLL(Dic.¤¤Soldiers);
				b.tab(6);
				b.add(GFORMAT.i(b.text(), STATS.BATTLE().ENEMY_KILLS.div().get(g.get())));
			};
			
		}.hh(STATS.BATTLE().ENEMY_KILLS.info().icon, STATS.BATTLE().ENEMY_KILLS.info().name, 200), 0, s.body().y2()+2);

		
		s.add(new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.perc(text, STATS.NEEDS().INJURIES.COUNT.div().getD(g.get()));
				
			}
		}.hh(STATS.NEEDS().INJURIES.COUNT.info().icon, STATS.NEEDS().INJURIES.COUNT.info().name, 200), 0, s.body().y2()+2);
		
		
		
		
		s.add(new GStat() {
			final BattleOrderTask task = new BattleOrderTask();
			@Override
			public void update(GText text) {
				DivFormation forCurrent = g.get().position();
				if (!g.get().settings().mustering()) {
					text.errorify().add(¤¤NotMuster);
				}else if (forCurrent.deployed() == 0){
					text.warnify().add(¤¤NoPosition);
				}else {
					g.get().order().task.get(task);
					switch (task.task()) {
					case ATTACK_MELEE:
						text.add(¤¤Attacking);
						break;
					case MOVE:
						text.add(¤¤Moving);
						break;
					case STOP:
						if (g.get().settings().ammo() != null && g.get().settings().fireAtWill() && BattleTrajectories.trajectories(g.get()) > 0)
							range(g.get(), text);
						else
							text.add(¤¤Standing);
						break;
					case ATTACK_RANGED:
						range(g.get(), text);
						
						break;
					case ATTACK_BUILDING:
						text.add(¤¤Building);
						break;
					case CHARGE:
						text.add(¤¤Charging);
						break;
					}
				}
				
				

			}
			
			private void range(Div div, GText text) {
				
				EquipRange rr = g.get().settings().ammo();
				if (rr != null && BattleTrajectories.trajectories(g.get()) > 0) {
					double di = rr.drawInter(g.get());
					if (di > 0.75) {
						text.add(¤¤AFiring);
					}else if (di > 0.5) {
						text.add(¤¤AAiming);
					}else {
						double tt = rr.projectile.reloadSeconds(rr.ref(g.get()))/2.0;
						di*=2;
						di = 1.0-di;
						text.add(¤¤AReloading);
						text.s().add(tt*di, 1).add('s');
					}
					
				}else {
					text.add(¤¤AFiring);
				}
				
				
			}
			
		}, 0, s.body().y2()+8);
		
		
		
		
		s.addRelBody(8, DIR.W, new RENDEROBJ.RenderImp(width*2, height*2) {
			
			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				UIDivCardBattle.this.render(g.get(), body.x1(), body.y1(), 2, r, true, false, false);
			}
		});
		
		
		GuiSection EE = new GuiSection();
		for (EquipBattle e : STATS.EQUIP().BATTLE_ALL()) {
			SPRITE hh = new SPRITE.Imp(Icon.M) {
				
				@Override
				public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
					if (g.get().info.equipI(e) == 0) {
						OPACITY.O50.bind();
					}
					e.resource.icon().render(r, X1, X2, Y1, Y2);
					OPACITY.unbind();
					
				}
			};
			
			RENDEROBJ o = new GStat() {
				
				@Override
				public void update(GText text) {
					if (g.get().info.equipI(e) == 0) {
						text.color(COLOR.WHITE50).add('-');
					} else {
						GFORMAT.f(text, ((int)10*(e.stat().div().getD(g.get())*e.max())/10.0), 1);
						text.add('/').add(g.get().info.equipI(e));
					}
				}
				
				@Override
				public void hoverInfoGet(GBox b) {
					e.hover(b, g.get());
				};
				
			}.hh(hh);
			
			if (e instanceof EquipRange) {
				EquipRange er = (EquipRange) e;
				GuiSection ss = new GuiSection();
				ss.add(o);
				ss.add(new SPRITE.Imp(64, 6) {
					
					
					@Override
					public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
						if (er.stat().div().get(g.get()) > 0)
							GMeter.render(r, GMeter.C_ORANGE, er.ammoD(g.get()), X1, X2, Y1, Y2);
					}
				}, 0, 22);
				ss.body().set(o.body());
				EE.addGrid(ss, e.indexMilitary(), 5, 48, 0);
			}else
				EE.addGrid(o, e.indexMilitary(), 5, 48, 0);
			
			
			
		}
		
		s.addRelBody(4, DIR.N, EE);
		
		
//		{
//			for (DivMoraleFactor f : MORALE.DIV().fact)
//		}
		
		
		
		s.body().incrW(48);
		
		
		s.addRelBody(8, DIR.S, stat.get(GAME.ARMIES().division((short)0)));
		
		
		{
			int w = 450;
			ArrayListGrower<HOVERABLE> all = new ArrayListGrower<HOVERABLE>();
			for (DivFactor f : GAME.ARMIES().factors.all()) {
				
				GTextR r = new GTextR(new GText(UI.FONT().S, f.message)) {
					
					@Override
					protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
						double m = f.midValue;
						double d = CLAMP.d(f.getD(g.get()), 0, 1);
						if (d < m) {
							text().color(ColorImp.TMP.interpolate(GCOLOR.UI().BAD.hovered, GCOLOR.T().NORMAL, d/m));
						}else if (d > m){
							d -= m;
							d /= 1.0-m;
							text().color(ColorImp.TMP.interpolate(GCOLOR.T().NORMAL, GCOLOR.UI().GOOD.hovered, d));
						}else {
							text().color(GCOLOR.T().INACTIVE);
						}
						super.render(r, ds, isHovered);
					}
					
					@Override
					public void hoverInfoGet(GUI_BOX text) {
						f.hover(g.get(), text);
					}
				};
				all.add(r);
				
			}
			
			GuiSection stats = new GuiSection();
			
			for (HOVERABLE h: all) {
				
				if (stats.getLastX2() + 16 + h.body().width() > w) {
					stats.add(h, 0, stats.body().y2()+2);
				}else {
					stats.addRightC(16, h);
				}
				
			}
			s.addRelBody(8, DIR.S, stats);
			
		}
		
		return s;
	}
	
	private void moralebg(SPRITE_RENDERER r, int scale, Div div, RECTANGLE body, int mm) {
		double morale = GAME.ARMIES().factors.valueCurrent(div);

		COLOR.WHITE10.render(r, body, -mm*scale);
//		
		mm++;
		if (morale < 0.5) {
			COLOR c = ColorImp.TMP.interpolate(cMoraleBad, COLOR.WHITE10, morale*2);
			c.render(r, body, -mm*scale);
			double m = div.morale();
			if (m < 1) {
				
				double speed = m < 0 ? 8 : 3;
				
				double op = 1-(speed*VIEW.renderSecond())%1.0;
				OpacityImp.TMP.set(op);
				OpacityImp.TMP.bind();
				cMoraleWorse.render(r, body, -mm*scale);
				OPACITY.unbind();
			}
			
		}else {
			morale = 2.0*(morale-0.5);
			COLOR c = ColorImp.TMP.interpolate(COLOR.WHITE10, cMoraleGood, morale);
			c.render(r, body, -mm*scale);
		}
	}
	
	public void hover(GUI_BOX box, Div div) {
		GBox b = (GBox) box;
		
		b.title(div.info.name());
		h1.set(div);
		b.add(hov1);
	}
	
	public GuiSection hovBox(Div div) {
		h2.set(div);
		return hov2;
	}
	
	public boolean hasAmmo(Div div) {
		for (int k = 0; k < STATS.EQUIP().RANGED().size(); k++) {
			EquipRange a = STATS.EQUIP().RANGED().get(k);
			if (a.stat().div().get(div) > 0) {
				return true;
			}
		}
		return false;
	}
	

	
	private final MiniMap mini = new MiniMap();
	
	public SPRITE miniDiv(Div div, boolean hovered, boolean selected) {
		mini.hovered = hovered;
		mini.selected = selected;
		mini.miniDiv = div;
		return mini;
	}
	
	private final COLOR[] cPower = new COLOR[] {
			new ColorImp(114, 84, 33).shade(0.7),
			new ColorImp(114, 114, 114).shade(0.7),
			new ColorImp(114, 114, 33).shade(0.7),
		};
	
	private class MiniMap implements SPRITE{
		
		private final DivFormationImp forDest  = new DivFormationImp();;
		private final Rec body = new Rec();
		private Div miniDiv;
		private boolean hovered;
		private boolean selected;


		@Override
		public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
			int scale = (X2-X1)/width();

			body.setDim(width()*scale, height()*scale);
			body.moveX1Y1(X1, Y1);
			
			GButt.ButtPanel.renderBGMini(r, true, selected, hovered, body);
			moralebg(r, scale, miniDiv, body, 2);
			
			GAME.ARMIES().banners.get(miniDiv.info.bannerI()).renderScaled(r, body.x1()+scale, body.y1()+scale, scale);;
			
			
			double men = miniDiv.info.men();
			double n = miniDiv.menNrOf();
			
			if (men == 0)
				GMeter.renderDelta(r, 0, 0, body.x1()+2*scale, body.x2()-2*scale, body.y2()-10*scale, body.y2()-2*scale);
			else
				GMeter.renderDelta(r, n/men, n/men, body.x1()+2*scale, body.x2()-2*scale, body.y2()-10*scale, body.y2()-2*scale);
			
			miniDiv.race().appearance().icon.small.render(r, body.x1()+2, body.y2()-18);
			
			double pow = miniDiv.settings().getPower();
			pow /= miniDiv.men();
			cPower[CLAMP.i((int) (pow/9.0), 0, cPower.length-1)].bind();
			int am = (int) (pow/3.0);
			am %= 3;
			am += 1;
			
			for (int i = 0; i < am; i++) {
				UI.icons().s.chevron(DIR.N).render(r, body.x2()-16, body.y1()+i*8);
			}
			
			int sx = body.x2()-16*scale;
			int sy = body.y1();
			if (!miniDiv.settings().mustering()) {
				GCOLOR.UI().BAD.hovered.bind();
				UI.icons().s.muster.renderScaled(r, sx, sy, scale);
			}else if (miniDiv.position().deployed() == 0){
				GCOLOR.UI().SOSO.hovered.bind();
				UI.icons().s.muster.renderScaled(r, sx, sy, scale);
			}else if (miniDiv.status().isFighting()) {
				GCOLOR.UI().BAD.hovered.bind();
				UI.icons().s.sword.renderScaled(r, sx, sy, scale);
			}else if (miniDiv.settings().shouldFire()) {
				GCOLOR.T().H1.bind();
				UI.icons().s.crossheir.renderScaled(r, sx, sy, scale);
			}else {
				DivFormation forCurrent = miniDiv.position();
				miniDiv.order().dest.get(forDest);
				
				COORDINATE dd = forDest.centrePixel();
				COORDINATE cc = forCurrent.centrePixel();
				
				if (dd != null && cc != null) {
					if (forCurrent.deployed() > 0 && forDest.deployed() > 0 && !cc.isSameAs(dd)) {
						
						GCOLOR.T().H1.bind();
						if (miniDiv.settings().running)
							UI.icons().s.divRun.renderScaled(r, sx, sy, scale);
						else
							UI.icons().s.divWalk.renderScaled(r, sx, sy, scale);
					}
				}
					
				
				
				
				
				
			}
			
			
			
			COLOR.unbind();

			
			
			//GCOLOR.UI().border().renderFrame(r, body, 0, scale);
			
		}

		@Override
		public int width() {
			return 40;
		}

		@Override
		public int height() {
			return 40;
		}

		@Override
		public void renderTextured(TextureCoords texture, int X1, int X2, int Y1, int Y2) {
			// TODO Auto-generated method stub
			
		}
	}
	

	
}
