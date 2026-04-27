package view.battle;

import static settlement.main.SETT.ENTITIES;

import game.GAME;
import game.battle.div.Div;
import init.constant.C;
import init.settings.S;
import init.sprite.SPRITES;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.room.main.Room;
import settlement.room.military.artillery.ArtilleryInstance;
import snake2d.Renderer;
import snake2d.util.datatypes.Rec;
import util.colors.GCOLOR;
import util.gui.misc.GBox;
import util.rendering.RenderData;
import util.rendering.ShadowBatch;
import view.battle.BattlePlacer.Action;
import view.battle.BattlePlacer.Mode;
import view.main.VIEW;
import view.subview.GameWindow;

final class BattlePlacerSelect extends Mode{

	private final GameWindow w;
	private final DivSelection s;
	private final boolean debug;
	private final Action a;
	
	public BattlePlacerSelect(GameWindow w, DivSelection s, Action action) {
		this.w = w;
		this.s = s;
		this.a = action;
		debug = S.get().developer;
	}
	private final Rec fill = new Rec();

	@Override
	void update(boolean hovered) {

		if (!hovered) {
			return;
		}
		
		if (a.clicked || a.clickReleased) {
			int x1 = (Math.min(a.start.x(), w.pixel().x()))-C.TILE_SIZEH;
			int y1 = (Math.min(a.start.y(), w.pixel().y()))-C.TILE_SIZEH;
			int x2 = (Math.max(a.start.x(), w.pixel().x()))+C.TILE_SIZEH;
			int y2 = (Math.max(a.start.y(), w.pixel().y()))+C.TILE_SIZEH;
			fill.set(x1, x2, y1, y2);
			x1 = x1 >> C.T_SCROLL;
			y1 = y1 >> C.T_SCROLL;
			x2 = x2 >> C.T_SCROLL;
			y2 = y2 >> C.T_SCROLL;
			boolean include = false;
			for (ENTITY e : ENTITIES().fill(fill)) {
				if (e instanceof Humanoid) {
					Div d = ((Humanoid) e).division();

					if (d != null)
						if (debug || d.army() == GAME.ARMIES().player()) {
							s.hover(d);
							include |= !s.selected(d);
						}
				}
			}
			for (int y = y1; y <= y2; y++) {
				for (int x = x1; x <= x2; x++) {
					
					Room r = SETT.ROOMS().map.get(x, y);
					if (r != null && r instanceof ArtilleryInstance) {
						ArtilleryInstance ca = (ArtilleryInstance) r;
						
						if (ca.army() == GAME.ARMIES().player()) {
							ca.hovered = true;
							include |= !ca.selected;
						}
					}
				}
				
			}
			
			if (a.clickReleased) {
				for (ENTITY e : ENTITIES().fill(fill)) {
					if (e instanceof Humanoid) {
						Div d = ((Humanoid) e).division();

						if (d != null)
							if (debug || d.army() == GAME.ARMIES().player()) {
								if (include)
									s.select(d);
								else
									s.deSelect(d);
									
							}
					}
				}
				
				for (int y = y1; y <= y2; y++) {
					for (int x = x1; x <= x2; x++) {
						for (ENTITY e : ENTITIES().getAtTile(x, y)) {
							if (e instanceof Humanoid) {
								Div d = ((Humanoid) e).division();

								if (d != null)
									if (debug || d.army() == GAME.ARMIES().player()) {
										if (include)
											s.select(d);
										else
											s.deSelect(d);
											
									}
							}
							
						}
						Room r = SETT.ROOMS().map.get(x, y);
						if (r != null && r instanceof ArtilleryInstance) {
							ArtilleryInstance ca = (ArtilleryInstance) r;
							if (ca.army() == GAME.ARMIES().player()) {
								if (include)
									s.artillery.select(ca);
								else
									s.artillery.deSelect(ca);
							}
						}
					}
				}
			}
		}else {
			ENTITY e = ENTITIES().getArroundPoint(w.pixel().x(), w.pixel().y());
			if (e instanceof Humanoid) {
				Div d = ((Humanoid) e).division();
				if (d != null) {
					s.hover(d);
					return;
				}
			}
			Room r = SETT.ROOMS().map.get(w.tile());
			if (r != null && r instanceof ArtilleryInstance) {
				ArtilleryInstance ca = (ArtilleryInstance) r;
				ca.hovered = true;
					
			}
			
			return;
		}
		

		
		
		
	}

	@Override
	void render(Renderer r, ShadowBatch shadowBatch, RenderData data, double ds) {
		VIEW.mouse().setReplacement(SPRITES.icons().m.expand);
		if (!a.clicked) {
			return;
		}
		int x1 = Math.min(a.start.x(), w.pixel().x());
		int y1 = Math.min(a.start.y(), w.pixel().y());
		int x2 = Math.max(a.start.x(), w.pixel().x());
		int y2 = Math.max(a.start.y(), w.pixel().y());
		
		final int dim = (2)<<w.zoomout();
		final int mX = C.WIDTH()<<w.zoomout();
		final int mY = C.HEIGHT()<<w.zoomout();
		
		if (x2-x1 < dim)
			return;
		if (y2-y1 < dim)
			return;
		
		//select
		
		//render
		
		x1 -= data.offX1();
		x2 -= data.offX1();
		y1 -= data.offY1();
		y2 -= data.offY1();
		
		//left
		if (x1 + dim > 0) {
			int ry1 = Math.max(y1, 0);
			int ry2 = Math.min(y2, mY);
			GCOLOR.MAP().BATTLE_OK.render(r, x1, x1+dim, ry1, ry2);
		}
		//right
		if (x2 < mX) {
			int ry1 = Math.max(y1, 0);
			int ry2 = Math.min(y2, mY);
			GCOLOR.MAP().BATTLE_OK.render(r, x2, x2+dim, ry1, ry2);
		}
		
		//up
		if (y1 + dim > 0) {
			int rx1 = Math.max(x1, 0);
			int rx2 = Math.min(x2, mX);
			GCOLOR.MAP().BATTLE_OK.render(r, rx1, rx2, y1, y1+dim);
		}
		//down
		if (y2 < mY) {
			int rx1 = Math.max(x1, 0);
			int rx2 = Math.min(x2, mX);
			GCOLOR.MAP().BATTLE_OK.render(r, rx1, rx2, y2, y2+dim);
		}
		
		
		
	
	}
	
	void renderCurrent(Renderer r, ShadowBatch shadowBatch, RenderData data, double ds) {
		
	}
	
	private final CharSequence sSelect = "Click to select. Click and hold to select area";
	
	double hovDelay = 0;
	
	@Override
	void hoverTimer(GBox text) {
		
		if (!a.clicked) {
			ENTITY e = ENTITIES().getArroundPoint(w.pixel().x(), w.pixel().y());
			if (e instanceof Humanoid) {
				Div d = ((Humanoid) e).division();
				if (d != null) {
					s.hover(d);
					if (GAME.SPEED.isPaused() || VIEW.renderSecond() - hovDelay > 1.5) {
						d.hoverInfo(text);
						if (d.army() == GAME.ARMIES().player()) {
							text.NL(5);
							text.text(sSelect);
							
						}
						
					}
					return;
				}
			}
			hovDelay = VIEW.renderSecond();
			
			Room r = SETT.ROOMS().map.get(w.tile());
			if (r != null && r instanceof ArtilleryInstance) {
				((ArtilleryInstance) r).hover(text);
			}
			
			
		}else {
			hovDelay = VIEW.renderSecond();
		}
		
	}




	
}
