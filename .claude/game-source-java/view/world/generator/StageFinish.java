package view.world.generator;

import game.GAME;
import game.faction.FACTIONS;
import init.constant.C;
import init.sprite.SPRITES;
import snake2d.CORE;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.Coo;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LISTE;
import snake2d.util.sets.LinkedList;
import snake2d.util.sprite.SPRITE;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.text.D;
import util.text.Dic;
import view.main.VIEW;
import view.tool.PlacableSimple;
import view.tool.ToolConfig;
import world.WORLD;
import world.map.landmark.WorldLandmark;
import world.map.pathing.WRegFinder.Treaty;
import world.map.regions.Region;

class StageFinish{

	private static CharSequence ¤¤inspectRegions = "Inspect World";
	static {
		D.ts(StageFinish.class);
	}
	
	
	public StageFinish(WorldViewGenerator stages) {
		
		stages.minimap.show();
		LinkedList<CLICKABLE> butts = new LinkedList<>();
		
		final Coo start = new Coo(-1,-1);
		
		final PlacableSimple simp = new PlacableSimple(¤¤inspectRegions) {
			

			@Override
			public void place(int x, int y) {

			}
			
			@Override
			public CharSequence isPlacable(int x, int y) {
				
				
				return null;
			}
			
			@Override
			public LIST<CLICKABLE> getAdditionalButt() {
				return butts;
			}
			
			@Override
			public void renderPlaceHolder(SPRITE_RENDERER r, int cx, int cy, boolean isPlacable) {
				
			}
			
			@Override
			public void renderAction(int cx, int cy) {
				if (start.x() >= 0) {
					int tx = cx/C.TILE_SIZE;
					int ty = cy/C.TILE_SIZE;
					WORLD.OVERLAY().path.add(start.x(), start.y(), tx, ty, Treaty.DUMMY);
					
				}
				
				//World.OVERLAY().landmarks().add();
				//World.OVERLAY().regions().add();
				super.renderAction(cx, cy);
			}
			
			@Override
			public void placeInfo(GBox b, int cx, int cy) {
				int tx = cx/C.TILE_SIZE;
				int ty = cy/C.TILE_SIZE;
				Region reg = WORLD.REGIONS().map.get(tx, ty);
				
				WORLD.OVERLAY().landmarks.add();
				
				if (reg != null) {
					WORLD.OVERLAY().hoverBox(reg);
					
					WORLD.OVERLAY().regionOutline.add(reg);
					if (WORLD.REGIONS().isCentre.is(tx, ty)) {
						VIEW.world().UI.regions.hover(reg, b);
					}
					
					
				}else if (WORLD.LANDMARKS().setter.get(tx, ty) != null) {
					WorldLandmark m = WORLD.LANDMARKS().setter.get(tx, ty);
					b.title(m.name);
					b.text(m.description);
					WORLD.OVERLAY().landmarks.hover(WORLD.LANDMARKS().setter.get(tx, ty));
				}
				
				b.NL(8);
				b.add(b.text().add(tx).add(':').add(ty));
				b.NL();
				
				
			}
			
			
		
			
		};
		
		
		
		butts.add(new GButt.ButtPanel(SPRITES.icons().m.arrow_left) {
			
			@Override
			protected void clickA() {
				new StageCapitol(stages, true);
			}
			
			@Override
			protected void renAction() {
				
			}
			
		}.hoverInfoSet(Dic.¤¤Back));
		butts.add(new GButt.ButtPanel(new SPRITE.Twin(SPRITES.icons().m.terrain, SPRITES.icons().m.rotate)){
			@Override
			protected void clickA() {
				StageCapitol.regenerate();
			};
		}.hoverInfoSet(WorldViewGenerator.¤¤regenerate));

		butts.add(new GButt.ButtPanel(SPRITES.icons().m.crossair){
			@Override
			protected void clickA() {
				stages.window.centerAtTile(WORLD.GEN().playerX, WORLD.GEN().playerY);
			};
		}.hoverInfoSet(WorldViewGenerator.¤¤home));
		
		butts.add(new GButt.ButtPanel(SPRITES.icons().m.arrow_right){
			@Override
			protected void clickA() {
				SPRITES.loader().minify(false, Dic.¤¤Generating);
				GAME.factions().prime();
				WORLD.ARMIES().saver().generate(WorldViewGenerator.loadPrint);
				WORLD.GEN().isDone = true;
				WORLD.FOW().toggled.set(true);
				VIEW.world().activate();
				VIEW.world().window.centererTile.set(FACTIONS.player().capitolRegion().cx(), FACTIONS.player().capitolRegion().cy());
				GAME.s().CreateFromWorldMap(WORLD.GEN().playerX-1, WORLD.GEN().playerY-1, false);
				GAME.saver().saveNew();
				CORE.getInput().clearAllInput();
			};
		}.hoverInfoSet(Dic.¤¤OK));

		ToolConfig fixed = new ToolConfig() {
			
			@Override
			public boolean back() {
				
				return false;
			};
			
			@Override
			public void addUI(LISTE<RENDEROBJ> uis) {
				stages.tools.placer.addStandardButtons(uis, false);
			}
			
		};
		stages.tools.place(simp,fixed);
		
	}
	
}
