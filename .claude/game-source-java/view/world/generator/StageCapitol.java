package view.world.generator;

import static world.WORLD.MINIMAP;

import game.faction.FACTIONS;
import init.constant.C;
import init.sprite.SPRITES;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.rnd.RND;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LISTE;
import snake2d.util.sprite.SPRITE;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.panel.GPanel;
import util.text.D;
import util.text.Dic;
import view.tool.PlacableFixedImp;
import view.tool.ToolConfig;
import view.world.generator.tools.UIWorldToolCapitolPlaceInfo;
import world.WORLD;
import world.map.regions.centre.WCentre;
import world.map.regions.centre.WorldCentrePlacablity;

class StageCapitol{

	private static CharSequence ¤¤name = "Place Capital";
	static CharSequence ¤¤prompt = "¤Generating new terrain will reset faction and regions. Continue?";
	static CharSequence ¤¤none = "¤Settling in this location is not possible.";
	static {
		D.ts(StageCapitol.class);
	}

	public StageCapitol(WorldViewGenerator stages, boolean clear) {
		if (clear) {
			WorldViewGenerator.loadPrint.exe();
			clear();
			MINIMAP().repaint();
		}
		stages.minimap.show();
//		LinkedList<RENDEROBJ> butts = new LinkedList<>();

		GuiSection butts = new GuiSection();
		
		butts.add(new GButt.ButtPanel(new SPRITE.Twin(SPRITES.icons().m.terrain, SPRITES.icons().m.rotate)){
			@Override
			protected void clickA() {
				WORLD.GEN().seed = RND.rInt(Integer.MAX_VALUE);
				WORLD.TERRAIN().saver().generate(WorldViewGenerator.loadPrint);
				WORLD.LANDMARKS().saver().generate(WorldViewGenerator.loadPrint);
				WorldViewGenerator.loadPrint.exe();
				MINIMAP().repaint();
				WorldViewGenerator.loadPrint.exe();
			};
			
			@Override
			protected void renAction() {
				WORLD.OVERLAY().landmarks.add();
			}
			
		}.hoverInfoSet(WorldViewGenerator.¤¤regenerate));
		butts.addRightC(0, new GButt.ButtPanel(SPRITES.icons().m.admin){
			@Override
			protected void clickA() {
				new StageEdit(stages);
			};
			
			@Override
			public void hoverInfoGet(snake2d.util.gui.GUI_BOX text) {
				text.title(StageEdit.¤¤name);
			};
			
		}.hoverInfoSet(Dic.¤¤Terrain));
		if (WORLD.GEN().playerX > -1) {
			butts.addRightC(0, new GButt.ButtPanel(SPRITES.icons().m.arrow_right){
				@Override
				protected void clickA() {
					stages.set();
				};
			}.hoverInfoSet(Dic.¤¤Next));
		}
		

		
		PlacableFixedImp t = new PlacableFixedImp(¤¤name, 1, 1) {
			
			final UIWorldToolCapitolPlaceInfo info = new UIWorldToolCapitolPlaceInfo();
			
			@Override
			public int width() {
				return WCentre.TILE_DIM;
			}
			
			@Override
			public void place(int tx, int ty, int rx, int ry) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterPlaced(int tx1, int ty1) {
				int cx = tx1+WCentre.TILE_DIM/2;
				int cy = ty1+WCentre.TILE_DIM/2;
				stages.reset();
				generate(cx, cy);
				stages.set();
			}
			
			@Override
			public CharSequence placableWhole(int tx1, int ty1) {
				CharSequence p = WorldCentrePlacablity.terrain(tx1, ty1);
				if (p != null)
					return p;
				return null;
//				
//				RES.flooder().init(this);
//				int cx = tx1+WCentre.TILE_DIM/2;
//				int cy = ty1+WCentre.TILE_DIM/2;
//				bb.moveC(cx, cy);
//				RES.flooder().pushSloppy(cx, cy, 0);
//				while(RES.flooder().hasMore()) {
//					PathTile t = RES.flooder().pollSmallest();
//					if (t.tileDistanceTo(cx, cy) > WCentre.TILE_DIM/2+2) {
//						if (WorldCentrePlacablity.terrain(t.x(), t.y()) == null) {
//							while(t != null) {
//								t = t.getParent();
//								
//							}
//								
//							RES.flooder().done();
//							return null;
//						}
//							
//					}
//					if (bb.holdsPoint(t)) {
//						for (DIR d : DIR.ORTHO) {
//							if (WTRAV.canLand(t.x(), t.y(), d, false)) {
//								RES.flooder().pushSmaller(t, d, t.getValue()+d.tileDistance(), t);
//							}
//						}
//					}else {
//						for (DIR d : DIR.ALL) {
//							if (WTRAV.can(t.x(), t.y(), d, false)) {
//								RES.flooder().pushSmaller(t, d, t.getValue()+d.tileDistance(), t);
//							}
//						}
//					}
//				}
//				
//				RES.flooder().done();
//				return ¤¤none;
				
			}

			
			@Override
			public int height() {
				return WCentre.TILE_DIM;
			}
			
			@Override
			public LIST<CLICKABLE> getAdditionalButt() {
				return null;
			}

			@Override
			public CharSequence placable(int tx, int ty, int rx, int ry) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public void placeInfo(GBox b, int x1, int y1) {
				

				
				
				info.placeInfo(b, x1, y1, FACTIONS.player().race());
			}
		};

		GPanel p = new GPanel(260, butts.body().height()).setButt();
		p.setTitle(¤¤name);
		p.inner().set(butts);
		butts.add(p);
		butts.moveLastToBack();
		butts.body().moveY1(64).centerX(C.DIM());
		
		ToolConfig fixed = new ToolConfig() {
			
			@Override
			public boolean back() {
				return false;
			};
			
			@Override
			public void addUI(LISTE<RENDEROBJ> uis) {
				uis.add(butts);
				//stages.tools.placer.addStandardButtons(uis, false);
			}
			
		};
		
		stages.tools.place(t,fixed);
		
	}
	
	private void generate(int cx, int cy) {
		WORLD.GEN().playerX = cx;
		WORLD.GEN().playerY = cy;
		generate();
	}
	
	static void regenerate() {
		WorldViewGenerator.loadPrint.exe();
		int px = WORLD.GEN().playerX;
		int py = WORLD.GEN().playerY;
		clear();
		WORLD.GEN().playerX = px;
		WORLD.GEN().playerY = py;
		generate();
	}
	
	
	public static void generate() {

		WORLD.OVERLAY().regNames.active.set(false);
		WorldViewGenerator.loadPrint.exe();
		WORLD.BUILDINGS().saver().generate(WorldViewGenerator.loadPrint);
		WorldViewGenerator.loadPrint.exe();
		
		
		WORLD.REGIONS().saver().generate(WorldViewGenerator.loadPrint);
		WORLD.ROADS().saver().generate(WorldViewGenerator.loadPrint);
		WORLD.PATH().saver().generate(WorldViewGenerator.loadPrint);
		WORLD.ENTITIES().saver().generate(WorldViewGenerator.loadPrint);
		WORLD.RD().saver().generate(WorldViewGenerator.loadPrint);
		

		WorldViewGenerator.loadPrint.exe();
		MINIMAP().repaint();
		WORLD.initBeforePlay();
		WORLD.OVERLAY().regNames.active.set(true);
	}

	
	public static void clear() {
		
		WorldViewGenerator.loadPrint.exe();
		WORLD.GEN().playerX = -1;
		WORLD.GEN().playerY = -1;
		WORLD.BUILDINGS().saver().clear();
		WORLD.ROADS().saver().clear();
		WORLD.PATH().saver().clear();
		WORLD.ENTITIES().saver().clear();
		WORLD.REGIONS().saver().clear();
		WORLD.RD().saver().clear();
	}
	
}
