package view.battle;

import static settlement.main.SETT.ENTITIES;

import game.GAME;
import game.battle.div.Div;
import game.battle.thread.order.BattleOrderTask;
import init.constant.C;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.room.main.Room;
import settlement.room.main.furnisher.FurnisherItem;
import settlement.room.military.artillery.ArtilleryInstance;
import settlement.thing.projectiles.SProjectiles;
import settlement.thing.projectiles.Trajectory;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Coo;
import snake2d.util.sprite.SPRITE;
import util.gui.misc.GBox;
import util.gui.misc.GText;
import util.rendering.RenderData;
import util.rendering.ShadowBatch;
import util.text.D;
import util.text.Dic;
import view.battle.BattlePlacer.Action;
import view.battle.BattlePlacer.Mode;
import view.keyboard.KEYS;
import view.main.VIEW;
import view.subview.GameWindow;

final class BattlePlacerAttack extends Mode{

	private static CharSequence ¤¤AttackMelee = "¤Attack unit";
	private static CharSequence ¤¤AttackRanged = "¤Fire at unit";
	private static CharSequence ¤¤AttackMix = "¤Attack/fire at unit";
	
	private static CharSequence ¤¤AttackBuilding = "¤Attack Building";
	private static CharSequence ¤¤BombardBuilding = "¤Bombard Building";
	private static CharSequence ¤¤BombardArea = "¤Bombard Area";

	private boolean melees;
	private boolean archers;
	private boolean artillery;
	private final Trajectory traj = new Trajectory();
	
	static {
		D.ts(BattlePlacerAttack.class);
	}
	
	private final GameWindow w;
	private final DivSelection s;
	private final Action a;
	private final BattleOrderTask task = new BattleOrderTask();
	private Target tar;
	
	public BattlePlacerAttack(GameWindow w, DivSelection s, Action a) {
		this.w = w;
		this.s = s;
		this.a = a;
	}
	
	private Target get() {
		if (div.set())
			return div;
		if (room.set())
			return room;
		if (building.set())
			return building;
		if (ground.set())
			return ground;
		return null;
	}
	
	public boolean init() {
		
		melees = false;
		archers = false;
		artillery = false;
		for (Div dd : s.selection()) {
			
			
			if (dd.menNrOf() > 0) {
				if (dd.settings().ammo() != null && !KEYS.MAIN().UNDO.isPressed())
					archers |= true;
				else
					melees |= true;
			}
		}
		artillery = !KEYS.MAIN().UNDO.isPressed() && !s.artillery.isClear();
		
		if ((melees || archers || artillery) == false)
			return false;
		
		tar = get();
		
		return tar != null;

		
	}
	
	@Override
	void update(boolean hovered) {
		
		if (!hovered)
			return;
	
		if (!a.clickReleased)
			return;
		
		get().click();
		
	}

	@Override
	void render(Renderer r, ShadowBatch shadowBatch, RenderData data, double ds) {

		tar.render(r, shadowBatch, data);
		
	}

	@Override
	void hoverTimer(GBox text) {
		
		text.text(tar.name());
		text.NL();
		tar.problem(text);
		
		
	}
	

	private abstract class Target {
		
		Target(){
			
		}
		
		abstract boolean set();
		
		abstract CharSequence name();
		abstract void problem(GBox box);
		abstract void click();
		abstract void render(Renderer r, ShadowBatch shadowBatch, RenderData data);
	}
	
	private boolean testArtillery(ArtilleryInstance ins, int cx, int cy, Trajectory traj) {
		CharSequence s = ins.testTarget(cx, cy, traj, true);
		if (s == null || s == SProjectiles.¤¤FRIENDLIES)
			return true;
		return false;
	}
	
	private final Target div = new Target() {
		
		private Div target;

		@Override
		boolean set() {
			
			target = null;
			ENTITY e = ENTITIES().getArroundPoint(w.pixel().x(), w.pixel().y());
			if (e instanceof Humanoid) {
				Div d = ((Humanoid) e).division();
				if (d != null && d.army() == GAME.ARMIES().enemy()) {
					target = d;
					s.hover(target);
				}
			}
			
			if (target == null)
				return false;
			return true;
		}

		@Override
		CharSequence name() {
			if (melees && (archers || artillery))
				return ¤¤AttackMix;
			else if (melees)
				return ¤¤AttackMelee;
			return ¤¤AttackRanged;
		}

		@Override
		void problem(GBox b) {
			artilleryProb(target.reporter.body().cX(), target.reporter.body().cY(), true, b);
		}

		@Override
		void click() {
			
		
			for (Div dd : s.selection()) {
				if (archers && dd.settings().ammo() != null) {
					task.attackRanged(target, dd);
				}else {
					task.attackMelee(target, dd);
				}
				dd.order().task.set(task);
			}
			if (artillery) {
				for (ArtilleryInstance ins : s.artillery.selection()) {
					if (testArtillery(ins, target.reporter.body().cX(), target.reporter.body().cY(), traj))
						ins.targetDivSet(target, true);
				}
			}
			
		}

		@Override
		void render(Renderer r, ShadowBatch shadowBatch, RenderData data) {
			s.hover(target);
			BattleRenderer.colAttack.bind();
			
			SPRITE sp = UI.icons().l.swords;
			
			if (melees && (archers || artillery)) {
				SPRITE s = ((((int)(VIEW.renderSecond()*2)) % 2) == 1) ? SPRITES.icons().m.bow : SPRITES.icons().m.sword;
				sp = ((((int)(VIEW.renderSecond()*2)) % 2) == 1) ? UI.icons().l.crossheir : UI.icons().l.swords;
				VIEW.mouse().setReplacement(s);
			}else if (melees)
				VIEW.mouse().setReplacement(SPRITES.icons().m.sword);
			else {
				VIEW.mouse().setReplacement(SPRITES.icons().m.bow);
				sp = UI.icons().l.crossheir;
			}
			
			
			
			if  (archers || artillery) {
				boolean someInRange = false;
				boolean allInRange = true;
				for (Div dd : s.selection()) {
					if (dd.menNrOf() > 0 && dd.settings().ammo() != null) {
						if (SProjectiles.problem(dd, target) == null) {
							someInRange |= true;
						}else {
							allInRange = false;
						}
					}
				}
				for (ArtilleryInstance ins : s.artillery.selection()) {
					if (testArtillery(ins, target.reporter.body().cX(), target.reporter.body().cY(), traj)){
						someInRange |= true;
					}else
						allInRange = false;
				}
				
				if (!someInRange &&(!archers && artillery)) {
					VIEW.mouse().setReplacement(SPRITES.icons().m.cancel);
					return;
				}
				if (!allInRange) {
					COLOR.ORANGE100.bind();
				}
			}
			

			int cx = target.centre().cX()-data.offX1();
			int cy = target.centre().cY()-data.offY1();
			
			sp.renderCScaled(r, cx, cy, 8);
//			
//			
//			SPRITES.cons().ICO.crosshair.renderScaled(r, x1-C.TILE_SIZE-C.TILE_SIZEH, y1-C.TILE_SIZE-C.TILE_SIZEH, 4);
			COLOR.unbind();
			
		}
	};
	
	private final Target room = new Target() {
		
		private Room r;
		private int cx,cy;
		
		@Override
		boolean set() {
			
			if (melees || archers || artillery) {
				r = SETT.ROOMS().map.get(w.tile());
				if (r != null) {
					FurnisherItem it = SETT.ROOMS().fData.item.get(w.tile());
					if (it != null) {
						COORDINATE c = SETT.ROOMS().fData.itemX1Y1(w.tile(), Coo.TMP);
						cx = c.x()*C.TILE_SIZE + it.width()*C.TILE_SIZE/2;
						cy = c.y()*C.TILE_SIZE + it.height()*C.TILE_SIZE/2;
						return true;
					}
				}
			}
			
			
			return false;
		}

		@Override
		CharSequence name() {
			if (melees || archers)
				return ¤¤AttackBuilding;
			else
				return ¤¤BombardBuilding;
		}
		
		@Override
		void problem(GBox b) {
			artilleryProb(cx, cy, false, b);
		}

		@Override
		void click() {
			
			
			for (Div dd : s.selection()) {
				task.attack(cx/C.TILE_SIZE, cy/C.TILE_SIZE, dd);
				dd.order().task.set(task);
			}
			if (artillery) {
				for (ArtilleryInstance ins : s.artillery.selection()) {
					if (ins.testTarget(cx, cy, traj, false) == null)
						ins.targetCooSet(cx, cy, false, true);
				}
			}
			
		}

		@Override
		void render(Renderer r, ShadowBatch shadowBatch, RenderData data) {

			COLOR.RED100.bind();
			
			if ((melees || archers) && artillery)
				VIEW.mouse().setReplacement((((int)VIEW.renderSecond()*2) % 2 == 1) ? SPRITES.icons().m.bow : SPRITES.icons().m.sword);
			else if ((melees || archers))
				VIEW.mouse().setReplacement(SPRITES.icons().m.sword);
			else
				VIEW.mouse().setReplacement(SPRITES.icons().m.bow);
			
			
			
			int p = artilleryProblem(cx, cy, false);
			if (p == 0) {
				VIEW.mouse().setReplacement(SPRITES.icons().m.cancel);
				return;
			}
			if (p == 1)
				COLOR.ORANGE100.bind();
			
			FurnisherItem it = SETT.ROOMS().fData.item.get(w.tile());
			if (it != null) {
				COORDINATE c = SETT.ROOMS().fData.itemX1Y1(w.tile(), Coo.TMP);
				int x1 = c.x()*C.TILE_SIZE-data.offX1();
				int y1 = c.y()*C.TILE_SIZE-data.offY1();
				SPRITES.cons().BIG.outline.renderBox(r, x1, y1, it.width()*C.TILE_SIZE, it.height()*C.TILE_SIZE);
				COLOR.unbind();
				return;
			}
		}
	};
	
	private final Target building = new Target() {
		
		private int cx,cy;
		
		@Override
		boolean set() {

			if (melees || archers || artillery) {
				if (GAME.ARMIES().map.attackable.is(w.tile(), GAME.ARMIES().player())) {
					cx = w.tile().x()*C.TILE_SIZE + C.TILE_SIZEH;
					cy = w.tile().y()*C.TILE_SIZE + C.TILE_SIZEH;
					return true;
				}
			}
			
			
			return false;
		}

		@Override
		CharSequence name() {
			if (melees || archers)
				return ¤¤AttackBuilding;
			else
				return ¤¤BombardBuilding;
		}

		@Override
		void problem(GBox b) {
			artilleryProb(cx, cy, false, b);
		}

		@Override
		void click() {

			for (Div dd : s.selection()) {
				task.attack(cx >> C.T_SCROLL, cy >> C.T_SCROLL, dd);
				dd.order().task.set(task);
			}
			if (artillery) {
				for (ArtilleryInstance ins : s.artillery.selection()) {
					if (ins.testTarget(cx, cy, traj, false) == null)
						ins.targetCooSet(cx, cy, false, true);
				}
			}
			
		}

		@Override
		void render(Renderer r, ShadowBatch shadowBatch, RenderData data) {

			COLOR.RED100.bind();
			
			if ((melees || archers) && artillery)
				VIEW.mouse().setReplacement((((int)VIEW.renderSecond()*2) % 2 == 1) ? SPRITES.icons().m.bow : SPRITES.icons().m.sword);
			else if ((melees || archers))
				VIEW.mouse().setReplacement(SPRITES.icons().m.sword);
			else
				VIEW.mouse().setReplacement(SPRITES.icons().m.bow);
			
			int p = artilleryProblem(cx, cy, false);
			if (p == 0) {
				VIEW.mouse().setReplacement(SPRITES.icons().m.cancel);
				return;
			}
			if (p == 1)
				COLOR.ORANGE100.bind();
			
			
			int x1 = cx-data.offX1();
			int y1 = cy-data.offY1();
			SPRITES.cons().BIG.dots.renderCentered(r, 0, x1, y1);
			COLOR.unbind();
			return;
		}
	};
	
	private int artilleryProblem(int cx, int cy, boolean ent) {
		
		if (artillery) {
			int ok = 0;
			for (ArtilleryInstance ins : s.artillery.selection()) {
				if (ins.testTarget(cx, cy, traj, ent) == null){
					ok++;
				}
			}
			if (ok < s.artillery.selection().size()) {
				if (ok == 0)
					return 0;
				return 1;
			}
			
		}
		return 2;
	}
	
	private void artilleryProb(int cx, int cy, boolean ent, GBox b) {
		
		int am = 0;
		
		if (archers) {
			for (Div dd : s.selection()) {
				if (dd.menNrOf() > 0) {
					if (dd.settings().ammo() != null) {
						CharSequence s = SProjectiles.problem(traj, dd, cx, cy);
						if (s != null) {
							am++;
							if (am > 10) {
								GText t = b.text();
								t.errorify().add(Dic.¤¤More).add('.').add('.').add('.');
								b.add(t);
								b.NL();
								break;
							}
							
							b.add(SPRITES.icons().s.bow);
							b.error(s);
							b.NL();
						}
					}
					
				}
			}
		}
		am = 0;
		if (artillery) {
			for (ArtilleryInstance ins : s.artillery.selection()) {
				CharSequence s = ins.testTarget(cx, cy, traj, ent);
				if (s != null) {
					am++;
					if (am > 10) {
						GText t = b.text();
						t.errorify().add(Dic.¤¤More).add('.').add('.').add('.');
						b.add(t);
						b.NL();
						break;
					}
					
					b.add(SPRITES.icons().s.circle);
					b.error(s);
					b.NL();
				}
			}
		}
		
	}
	
	private final Target ground = new Target() {
		
		@Override
		boolean set() {
			if (!artillery)
				return false;
			
			return true;
		}

		@Override
		CharSequence name() {
			return ¤¤BombardArea;
		}

		@Override
		void problem(GBox b) {
			
			artilleryProb(w.pixel().x(), w.pixel().y(), false, b);
		}

		
		@Override
		void click() {
			for (ArtilleryInstance ins : s.artillery.selection()) {
				if (ins.testTarget(w.pixel().x(), w.pixel().y(), traj, false) == null)
					ins.targetCooSet(w.pixel().x(), w.pixel().y(), true, true);
			}
		}

		@Override
		void render(Renderer r, ShadowBatch shadowBatch, RenderData data) {
			int p = artilleryProblem(w.pixel().x(), w.pixel().y(), false);
			if (p == 0) {
				VIEW.mouse().setReplacement(SPRITES.icons().m.cancel);
				return;
			}
			if (p == 1)
				COLOR.ORANGE100.bind();
			else
				COLOR.RED100.bind();
			VIEW.mouse().setReplacement(SPRITES.icons().m.bow);
			int d = C.TILE_SIZE*3;
			int x1 = w.pixel().x()-d/2-data.offX1();
			int y1 = w.pixel().y()-d/2-data.offY1();
			
			SPRITES.cons().BIG.dots.renderBox(r, x1, y1, d, d);

			
		}
	};

	
}
