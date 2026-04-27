package view.sett.ui.army;

import static settlement.main.SETT.ENTITIES;
import static settlement.main.SETT.IN_BOUNDS;
import static settlement.main.SETT.PATH;

import game.GAME;
import game.battle.Army;
import game.battle.div.Div;
import init.constant.C;
import init.constant.Config;
import init.race.RACES;
import init.sprite.SPRITES;
import init.type.HTYPE;
import init.type.HTYPES;
import settlement.entity.humanoid.Humanoid;
import settlement.stats.STATS;
import settlement.stats.colls.StatsBattle.StatTraining;
import settlement.stats.equip.EquipBattle;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.SPRITE;
import view.tool.PlacableFixedImp;
import view.ui.div.UIDivEditor;

final class FormationDebugPlacer extends PlacableFixedImp{

	private static final int SIZES = 5;
	private final Army team;
	private final int[] widths = new int[SIZES];
	private final int[] height = new int[SIZES];
	private Div div;
	private final ArrayList<Div> divs = new ArrayList<>(1);
	private final LIST<CLICKABLE> butts;
	private final UIDivEditor editor = new UIDivEditor(STATS.BATTLE().TRAINING_ALL.size(), true, true, false, RACES.all());
	
	public FormationDebugPlacer(Army team) {
		super(team == GAME.ARMIES().player() ? "Place Division" : "Place Division Enemy", 1, SIZES);
		this.team = team;
		
		for (int i = 0; i < SIZES; i++) {
			
			double s = (i+1)*Config.battle().MEN_PER_DIVISION/SIZES;
			int w = (int) Math.ceil(Math.sqrt(s));
			int h = (int) Math.ceil(s/w);
			widths[i] = w;
			height[i] = h;
			
		}


		butts = new ArrayList<CLICKABLE>(editor);
		
		
		
	}
	
	
	@Override
	public SPRITE getIcon() {
		return SPRITES.icons().m.for_loose;
	}

	
	@Override
	public void place(int tx, int ty, int rx, int ry) {
		place(tx, ty);
	}
	
	@Override
	public void afterPlaced(int tx1, int ty1) {
		
		int x1 = tx1;
		int y1 = ty1;
		int x2 = x1 + width();
		x1 = x1*C.TILE_SIZE + C.TILE_SIZEH;
		y1 = y1*C.TILE_SIZE + C.TILE_SIZEH;
		x2 = x2*C.TILE_SIZE + C.TILE_SIZEH;
		GAME.ARMIES().placer.deploy(divs, x1, x2, y1, y1);
		
		GAME.ARMIES().initAndTeleport(divs);
		
	}
	
	private void place(int tx, int ty) {
		
		if (div.menNrOf() == 0) {
			div.info.copyFrom(editor.div());
			div.settings().musteringSet(true);
			div.info.menSet(width()*height());
			divs.clear();
			divs.add(div);
		}
		
		if (div.menNrOf() < Config.battle().MEN_PER_DIVISION) {
			HTYPE t = team != GAME.ARMIES().player() ? HTYPES.ENEMY() : HTYPES.SUBJECT();
			Humanoid h = new Humanoid(tx*C.TILE_SIZE+C.TILE_SIZEH, ty*C.TILE_SIZE+C.TILE_SIZEH, editor.div().race(), t, null);
			for (StatTraining tr : STATS.BATTLE().TRAINING_ALL) {
				tr.stat.indu().setD(h.indu(), editor.div().training(tr));
			}
			
			for (EquipBattle b : STATS.EQUIP().BATTLE_ALL()) {
				b.stat().indu().setD(h.indu(), editor.div().equip(b));
			}
			
			STATS.BATTLE().COMBAT_EXPERIENCE.indu().setD(h.indu(), editor.div().experience());
			
			STATS.BATTLE().basicTraining.setD(h.indu(), 1.0);
			div.info.menSet(Config.battle().MEN_PER_DIVISION);
			h.setDivision(div);
		}
		
	}
		

	private boolean setDiv() {
		div = team.getNextEmptyOrdered();
		if (div != null) {
			return true;
		}
		return false;
	}
	
	@Override
	public CharSequence placableWhole(int tx1, int ty1) {
		if (!setDiv())
			return E;
		return null;
	}

	@Override
	public CharSequence placable(int tx, int ty, int rx, int ry) {
		if (!IN_BOUNDS(tx, ty))
			return E;
		
		if (PATH().solidity.is(tx, ty)) {
			return E;
		}
		if (ENTITIES().hasAtTile(tx, ty))
			return E;
		
		return null;
	}


	@Override
	public int width() {
		return widths[size()];
	}

	@Override
	public int height() {
		return height[size()];
	}

	@Override
	public LIST<CLICKABLE> getAdditionalButt() {
		return butts;
	}
	
}
