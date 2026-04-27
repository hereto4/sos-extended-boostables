package view.sett.ui.home;

import java.util.Arrays;

import game.GAME;
import init.race.RACES;
import init.race.Race;
import init.sprite.SPRITES;
import init.type.HCLASS;
import init.type.HCLASSES;
import init.type.HGROUP;
import init.type.HGROUP.HTypeBits;
import init.type.HTYPE;
import init.type.HTYPES;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.room.home.house.HomeInstance;
import settlement.stats.STATS;
import snake2d.util.datatypes.AREA;
import snake2d.util.sprite.SPRITE;
import util.gui.misc.GBox;
import util.text.D;
import view.tool.PLACER_TYPE;
import view.tool.PlacableMulti;

final class UIHomeOdd extends PlacableMulti{

	private static CharSequence ¤¤name = "O-Mover";
	private static CharSequence ¤¤desc = "Oddjobbers will automatically move out if an employed subject needs their home. This tool manually moves random oddjobbers to desired housing.";
	private static CharSequence ¤¤prob = "Must be placed on a house with vacancies.";
	private static CharSequence ¤¤odd = "No oddjobbers to move!";
	private static CharSequence ¤¤oddNo = "There are no oddjobbers that can be moved into the specific house. They are either full, or their settings doesn't match the oddjobbers species and class.";
	
	static {
		D.ts(UIHomeOdd.class);
	}
	
	private int updateTick;
	private int[][] oddjobbers = new int[HCLASSES.ALL().size()][RACES.all().size()]; 
	private int total;

	
	public UIHomeOdd() {
		super(¤¤name, ¤¤desc, new SPRITE.Twin(SPRITES.icons().m.workshop, SPRITES.icons().s.arrow_right));
		
		
	}

	@Override
	public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
		
		if (updateTick != GAME.updateI()) {
			updateTick = GAME.updateI();
			total = 0;
			for (int[] i : oddjobbers) {
				Arrays.fill(i, 0);
			}
			for (HTYPE c : HTYPES.ALL()) {
				if (!c.works || !c.player)
					continue;
				for (Race r : RACES.all()) {
					int am = STATS.POP().pop(r, c);
					
					total += am;
					oddjobbers[c.CLASS.index()][r.index()] += am;
				}
			}
			
			for (HCLASS c : HCLASSES.ALL()) {
				for (Race r : RACES.all()) {
					int am = STATS.WORK().EMPLOYED.stat().data(c).get(r);
					
					total -= am;
					oddjobbers[c.index()][r.index()] -= am;
				}
				
			}
		}
		
		if (total <= 0)
			return ¤¤odd;
		
		HomeInstance h = SETT.ROOMS().HOME.getter.get(tx, ty);
		if (h == null)
			return ¤¤prob;
		
		if (h.occupants() >= h.occupantsMax()) {
			return ¤¤prob;
		}
		
		HTypeBits a = h.availability();
		
		if (a == null)
			return ¤¤prob;
		
		for (int ti = 0; ti < HGROUP.all().size(); ti++) {
			HGROUP t = HGROUP.all().get(ti);
			if (t.race == null) {
				continue;
			}
			if (oddjobbers[t.type.index()][t.race.index()] > 0)
				return null;
		}

		return ¤¤oddNo;
	}
	
	@Override
	public void placeInfo(GBox b, int oktiles, AREA a) {
		
		super.placeInfo(b, oktiles, a);
	}
	

	int ie = 0;
	
	
	@Override
	public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
		
		HomeInstance h = SETT.ROOMS().HOME.getter.get(tx, ty);
		
		if (h == null)
			return;
		
		if (tx == h.serviceX() && ty == h.serviceY()) {
			HTypeBits t = h.availability();
			if (t != null && h.occupants() < h.occupantsMax()) {
				ENTITY[] ee = SETT.ENTITIES().getAllEnts();
				for (int i = 0; i < ee.length; i++) {
					if (ie >= ee.length)
						ie = 0;
					ENTITY e = ee[ie];
					if (e instanceof Humanoid) {
						Humanoid a = (Humanoid) e;
						if (STATS.WORK().EMPLOYED.get(a) == null && t.is(a)) {
							STATS.HOME().GETTER.set(a, h);
							t = h.availability();
							if (t == null || h.occupants() >= h.occupantsMax()) {
								break;
							}
						}
					}
					ie ++;
					
				}
			}
		}
	}
	
	@Override
	public boolean expandsTo(int fromX, int fromY, int toX, int toY) {
		return SETT.ROOMS().HOME.is(fromX, fromY) && SETT.ROOMS().map.get(fromX, fromY).isSame(fromX, fromY, toX, toY);
	}
	
}
