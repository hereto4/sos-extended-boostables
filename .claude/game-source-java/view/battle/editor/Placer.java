package view.battle.editor;

import static world.WORLD.MINIMAP;

import game.battle.state.BattleState;
import game.battle.state.BattleStateExiter;
import game.battle.state.BattleStateResult;
import game.battle.state.BattleStateSpec;
import game.battle.state.BattleStateSpec.SpecSide;
import game.battle.util.DIV_SPEC;
import game.battle.util.DivGeneration;
import game.faction.FACTIONS;
import game.time.TIME;
import init.constant.C;
import init.sprite.UI.UI;
import menu.Menu;
import snake2d.CORE;
import snake2d.CORE_STATE;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LIST;
import util.colors.GCOLOR;
import util.data.INT.INTE;
import util.gui.misc.GButt;
import util.gui.slider.GSliderInt;
import util.text.D;
import util.text.Dic;
import view.main.VIEW;
import view.tool.PlacableSimpleTile;
import view.world.generator.WorldViewGenerator;
import view.world.generator.tools.UIWorldGenerateTerrain;
import world.WORLD;

class Placer extends PlacableSimpleTile {


	private static CharSequence ¤¤select = "Select Location";
	private static CharSequence ¤¤time = "time of day";
	static {
		D.ts(Placer.class);
	}
	
	private ArrayListGrower<CLICKABLE> butts = new ArrayListGrower<CLICKABLE>();
	private final UIWorldGenerateTerrain terrain = new UIWorldGenerateTerrain(WORLD.GEN());
	
	
	
	public final ACTION generate = new ACTION() {
		
		@Override
		public void exe() {
			int time = RND.rInt(100);
			TIME.set(TIME.secondsPerDay()*time/100);
			WORLD.TERRAIN().saver().generate(WorldViewGenerator.loadPrint);
			WORLD.LANDMARKS().saver().generate(WorldViewGenerator.loadPrint);
			WorldViewGenerator.loadPrint.exe();
			MINIMAP().repaint();
			WorldViewGenerator.loadPrint.exe();
			WORLD.GEN().hasGeneratedTerrain = true;
			FACTIONS.otherFaction().bonus.clear();
			
			
		}
	};
	
	private final ArmySide player;
	private final ArmySide enemy;
	
	public Placer(ArmySide player, ArmySide enemy) {
		super(¤¤select);
		
		this.player = player;
		this.enemy = enemy;
		
		terrain.addRelBody(2, DIR.S, new GButt.ButtPanel(Dic.¤¤Generate) {
			@Override
			protected void clickA() {
				generate.exe();
			}
			
		});
		
		butts.add(new GButt.ButtPanel(UI.icons().m.arrow_left) {
			
			@Override
			protected void clickA() {
				VIEW.b().editor.tools.placer.deactivate();
			}
			
			
			
		}.hoverInfoSet(Dic.¤¤Back));
		
		butts.add(new GButt.ButtPanel(UI.icons().m.terrain) {
			
			@Override
			protected void clickA() {
				VIEW.inters().popup.show(terrain, this);
			}
			
			
			
		}.hoverInfoSet(Dic.¤¤Generate));
		
		{
			INTE ii = new INTE() {
				
				@Override
				public int min() {
					return 0;
				}
				
				@Override
				public int max() {
					return 100;
				}
				
				@Override
				public int get() {
					return CLAMP.i((int) (100*TIME.currentSecond()/TIME.secondsPerDay()), 0 , 100);
				}
				
				@Override
				public void set(int t) {
					TIME.set(TIME.secondsPerDay()*t/100.0);
					
				}
			};
			
			
			
			GSliderInt sl = new GSliderInt(ii, 100, false) {
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					text.text(¤¤time);
				}
			};
			sl.addRelBody(4, DIR.W, UI.icons().s.clock);
			
			sl.pad(8, 2);
			
			butts.add(sl);
		}
		
	}

	
	
	@Override
	public CharSequence isPlacable(int tx, int ty) {
		for (int i = 0; i < DIR.ORTHO.size(); i++) {
			if (BattleState.okWorldTile(tx, ty, DIR.ORTHO.get(i)))
				return null;
		}
		return Dic.empty;
	}

	@Override
	public void place(int tx, int ty) {
		BattleStateSpec spec = new BattleStateSpec();

		DIR d = DIR.ORTHO.rnd();
		
		for (int i = 0; i < DIR.ORTHO.size(); i++) {
			if (BattleState.okWorldTile(tx, ty, d))
				break;
			d = d.next(2);
		}
		
		
		set(player, spec.player, tx, ty);
		set(enemy, spec.enemy, tx+d.x(), ty+d.y());
		
		BattleStateExiter res = new BattleStateExiter() {
			
			@Override
			public void afterExit(BattleStateResult res) {
				CORE.setCurrentState(new CORE_STATE.Constructor() {
					@Override
					public CORE_STATE getState() {
						return Menu.make();
					}
				});
			}
		};
		
		BattleState.setGenerate(res, spec);
		
	}
	
	private void set(ArmySide s, SpecSide ss, int tx, int ty) {
		for (int i = 0; i < ss.artillery.length; i++) {
			ss.artillery[i] = s.artillery[i];
		}
		ss.wCoo.set(tx, ty);
		ss.moraleBase = 1.0;
		for (DIV_SPEC d : s.divs) {
			ss.divs.add(new DivGeneration(d, d));
		}
	}
	
	@Override
	public void renderPlaceHolder(SPRITE_RENDERER r, int tx, int ty, int cx, int cy, boolean isPlacable) {
		super.renderPlaceHolder(r, tx, ty, cx, cy, isPlacable);
		if (!isPlacable)
			GCOLOR.MAP().OK.bind();
		else
			GCOLOR.MAP().BAD.bind();
		
		int ri = (int) (C.TILE_SIZE*VIEW.renderSecond()*0.5);
		ri %= C.TILE_SIZE;
		ri = C.TILE_SIZE-ri;
		for (DIR d : DIR.ORTHO) {
			
			UI.icons().s.chevron(d.perpendicular()).renderCScaled(r, cx+d.x()*C.TILE_SIZE + d.x()*ri,  cy+d.y()*C.TILE_SIZE + d.y()*ri, C.SCALE);
			
		}
		
		COLOR.unbind();
		
	}
	
	@Override
	public LIST<CLICKABLE> getAdditionalButt() {
		return butts;
	}

}
