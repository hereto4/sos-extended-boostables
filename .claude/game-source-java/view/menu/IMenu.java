package view.menu;

import game.GAME;
import init.constant.C;
import init.sprite.UI.UI;
import menu.Menu;
import snake2d.CORE;
import snake2d.CORE_STATE;
import snake2d.MButt;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.ACTION;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.text.Font;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.text.D;
import util.text.Dic;
import view.interrupter.InterManager;
import view.interrupter.Interrupter;
import view.keyboard.KEYS;
import view.main.VIEW;

public class IMenu extends Interrupter{

	private final GuiSection main;
	private final GuiSection options;
	private final ScKeys keys;
	private GuiSection current;
	private final Font big;
	private final Font small;
	private final InterManager manager;
	
	private static CharSequence ¤¤resume = "resume";
	private static CharSequence ¤¤quicksave = "quick-save";
	private static CharSequence ¤¤saveFirst = "save first?";
	private static CharSequence ¤¤options = "options";
	private static CharSequence ¤¤quitMenu = "quit to menu";
	private static CharSequence ¤¤exit = "exit";
	private static CharSequence ¤¤THEMENU = "THE MENU";
	
	static {
		D.ts(IMenu.class);
	}
	
	public IMenu(InterManager manager){
		super();
		this.manager = manager;
		pin().desturberSet();
		big = UI.FONT().H1;
		small = UI.FONT().H2;
		keys = new ScKeys(this, big, small);
		main = new GuiSection() {
			
			@Override
			public boolean click() {	
				return super.click();
			}
		};
		
		

		
		GButt tx = new GButt.Glow((SPRITE)big.getText(¤¤resume));
		tx.clickActionSet(new ACTION() {
			@Override
			public void exe() {
				hide();
			}
		});
		main.addDownC(10, tx);
		
		tx = new GButt.Glow(big.getText(¤¤quicksave)) {
			
			@Override
			protected void renAction() {
				activeSet(VIEW.canSave());
			}
			
			@Override
			protected void clickA() {
				GAME.saver().quicksave();
				hide();
			}
			
		};
		main.addDownC(6, tx);
		
		tx = new GButt.Glow(big.getText(Dic.¤¤save)) {
			@Override
			protected void renAction() {
				activeSet(VIEW.canSave());
			}
			
			@Override
			protected void clickA() {
				current = new IMenuSave(IMenu.this, big, small, null);
			}
		};
		main.addDownC(6, tx);
		
		tx = new GButt.Glow(big.getText(Dic.¤¤load));
		tx.clickActionSet(new ACTION() {
			IMenuLoad i = new IMenuLoad(IMenu.this);
			@Override
			public void exe() {
				i.init();
				current = i;
			}
		});
		main.addDownC(6, tx);
		
		tx = new GButt.Glow(big.getText(¤¤options));
		tx.clickActionSet(new ACTION() {
			@Override
			public void exe() {
				current = options;
			}
		});
		main.addDownC(6, tx);
		
		tx = new GButt.Glow(big.getText(keys.¤¤nameBig));
		tx.clickActionSet(new ACTION() {
			@Override
			public void exe() {
				current = keys.activate();
			}
		});
		main.addDownC(6, tx);
		
		ACTION exit2Menu = new ACTION() {
			@Override
			public void exe() {
				exit2Menu();
			}
		};
		ACTION exit = new ACTION() {
			@Override
			public void exe() {
				CORE.annihilate();
			}
		};
		
		tx = new GButt.Glow(big.getText(¤¤quitMenu));
		tx.clickActionSet(new ACTION() {
			@Override
			public void exe() {
				
				if (GAME.saver().getTimeSinceLastSave() < 5 || !VIEW.canSave()){
					exit2Menu();
				}
				
				GButt yes = new GButt.Glow(big.getText(Dic.¤¤Yes));
				yes.clickActionSet(new ACTION() {
					@Override
					public void exe() {
						current = new IMenuSave(IMenu.this, big, small, exit2Menu);
					}
				});
				GButt no = new GButt.Glow(big.getText(Dic.¤¤No));
				no.clickActionSet(exit2Menu);
				
				VIEW.inters().fullScreen.activate(¤¤saveFirst, COLOR.WHITE100, null, yes, no);				
			}
		});
		main.addDownC(6, tx);
		
		tx = new GButt.Glow(big.getText(¤¤exit));
		tx.clickActionSet(new ACTION() {
			@Override
			public void exe() {
				
				if (!VIEW.canSave()) {
					exit.exe();
					return;
				}
				
				if (GAME.saver().getTimeSinceLastSave() < 5){
					CORE.annihilate();
				}
				
				GButt yes = new GButt.Glow(big.getText(Dic.¤¤Yes));
				yes.clickActionSet(new ACTION() {
					@Override
					public void exe() {
						current = new IMenuSave(IMenu.this, big, small, exit);
					}
				});
				GButt no = new GButt.Glow(big.getText(Dic.¤¤No));
				no.clickActionSet(exit);
				
				VIEW.inters().fullScreen.activate(¤¤saveFirst, COLOR.WHITE100, null, yes, no);
			}
		});
		main.addDownC(6, tx);
		
		main.body().centerIn(C.DIM());
		
		main.add(UI.decor().frame(main.body()));
		main.moveLastToBack();
		
		{
			RENDEROBJ o = UI.decor().getDecored(¤¤THEMENU);
			o.body().centerX(main);
			o.body().moveY2(main.body().y1());
			main.add(o);
		}
//		
		current = main;
		
		options = new IMenuOptions(this, big, small);
		
	}
	

	
	void exit2Menu(){
		CORE.setCurrentState(new CORE_STATE.Constructor() {
			@Override
			public CORE_STATE getState() {
				return Menu.make();
			}
		});
	}
	
	public void show(){
		super.show(manager);
		setMain();
	}
	
	void setMain(){
		current = main;
	}
	
	@Override
	protected boolean hover(COORDINATE mCoo, boolean mouseHasMoved) {
		current.hover(mCoo);
		return true;
		
	}

	@Override
	protected void hoverTimer(GBox text) {
		
	}

	@Override
	protected boolean render(Renderer r, float ds) {
		current.render(r, ds);
		return false;
	}

	@Override
	protected void mouseClick(MButt button) {
		if (button == MButt.LEFT)
			current.click();
		else if (button == MButt.RIGHT) {
			if (current != main)
				current = main;
			else
				hide();
		}
	}

	@Override
	protected boolean update(float ds) {
		if (KEYS.MAIN().ESCAPE.consumeClick())
			hide();
		return false;
	}
	
}
