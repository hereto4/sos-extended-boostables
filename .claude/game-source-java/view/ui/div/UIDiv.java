package view.ui.div;

import java.util.Comparator;

import game.GAME;
import game.battle.util.DIV_SPEC;
import init.constant.Config;
import init.sprite.UI.UI;
import settlement.stats.STATS;
import settlement.stats.colls.StatsBattle.StatTraining;
import settlement.stats.equip.EquipBattle;
import settlement.stats.equip.EquipBattle.DivSprite;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.Rec;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayList;
import util.colors.GCOLOR;

public final class UIDiv {

	
	final int WIDTH = 58;
	final int HEIGHT = 64+14;
	
	public final UIDivCardSett settCivic = new UIDivCardSett(this);
	public final UIDivCardBasic normal = new UIDivCardBasic(this);
	public final UIDivCardWorld world = new UIDivCardWorld(this);
	public final UIDivCardBattle battle = new UIDivCardBattle(this);

	private final ArrayList<EquipBattle> equips = new ArrayList<EquipBattle>(STATS.EQUIP().BATTLE_ALL().size());
	private final Rec body = new Rec();
	
	public UIDiv() {
		// TODO Auto-generated constructor stub
	}
	
	private static class Comp implements Comparator<EquipBattle>{

		DIV_SPEC d;
		
		@Override
		public int compare(EquipBattle o1, EquipBattle o2) {
			return o1.sprites[d.race().index].z - o2.sprites[d.race().index].z;
		}
		
		
	}
	
	private final Comp comp = new Comp();
	
	public void renderBasics(SPRITE_RENDERER r, int x1, int y1, int scale, DIV_SPEC d) {
		
		if (d == null)
			return;
		
		body.set(x1,x1+WIDTH*scale, y1, y1+HEIGHT*scale);
		
		int cx = body.cX();
		
		GAME.ARMIES().banners.get(d.bannerI()).renderCX(r, cx-12*scale, y1+4*scale, scale);
		d.race().appearance().icon.renderCX(r, cx, body.y1()+18*scale, scale);
		
		equips.clearSloppy();
		for (EquipBattle e : STATS.EQUIP().BATTLE_ALL()) {
			if (d.equip(e) > 0) {
				equips.add(e);
				
			}
		}
		comp.d = d;
		equips.sort(comp);
		
		for (EquipBattle e : equips) {
			DivSprite s = e.sprites[d.race().index()];
			
			OPACITY.O50.bind();
			COLOR.BLACK.bind();
			s.icon.renderCX(r, cx+s.ox*scale+2*scale, y1+54*scale+s.oy*scale, scale);
			OPACITY.unbind();
			
			ColorImp.TMP.interpolate(s.cols, d.equip(e)).bind();
			s.icon.renderCX(r, cx+s.ox*scale, y1+52*scale+s.oy*scale, scale);
		}
		
		
		
		{
			int k = 0;
			for (StatTraining t : STATS.BATTLE().TRAINING_ALL) {
				double ds = d.training(t);
				if (ds > 0) {
					int am = (int)(ds*3);
					ColorImp.TMP.interpolate(t.room.divCols, ds).bind();
					for (int i = 0; i <= am; i++) {
						t.room.divIcon.renderScaled(r, cx-2*scale+k*12*scale, y1+2*scale+scale*i*5, scale);
					}
					k++;
					if (k >= 2)
						break;
				}
				
			}
		}
		
		{
			double ds = d.experience();
			int am = (int) (ds*5);
			GCOLOR.T().H1.bind();
			for (int i = 0; i < am; i++) {
				UI.icons().s.smallSkull.renderCScaled(r, cx-20*scale, y1+30*scale+scale*6*i, scale);
			}
		}
		
		
			
		COLOR.unbind();		
		
	}
	
	private final COLOR[] cPower = new COLOR[] {
			new ColorImp(114, 84, 33).shade(0.7),
			new ColorImp(114, 114, 114).shade(0.7),
			new ColorImp(114, 114, 33).shade(0.7),
		};
	
	public void renderPower(int x1, int y1, SPRITE_RENDERER r, double l) {
		l = l/(5.0*Config.battle().MEN_PER_DIVISION);
		
		int ci = CLAMP.i((int) (l*3), 0, 2);
		l -= ci/3.0;
		int am = (int) (1 + l*5);
		am = CLAMP.i(am, 1, 6);
		
		
		
		OPACITY.O50.bind();
		COLOR.BLACK.render(r, x1, x1+10, y1, y1+am*8+4);
		OPACITY.unbind();
		y1 += 2;
		x1 += 1;
		cPower[ci].bind();
		for (int i = 0; i < am; i++) {
			UI.icons().s.chevron(DIR.N).render(r, x1, y1+i*8);
		}
		COLOR.unbind();
	};
	
}
