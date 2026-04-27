package view.main;


import java.io.IOException;

import game.GAME;
import game.save.GameLoader;
import game.save.Savable;
import game.time.TIME;
import init.constant.C;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import snake2d.CORE;
import snake2d.CORE_STATE;
import snake2d.KeyBoard.KeyEvent;
import snake2d.MButt;
import snake2d.Renderer;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import util.GUTIL;
import util.gui.misc.GBox;
import util.rendering.RenderData;
import util.rendering.ShadowBatch;
import util.text.Dic;
import view.battle.BattleView;
import view.interrupter.InterManager;
import view.keyboard.KEYS;
import view.keyboard.KeyPoller;
import view.sett.SettView;
import view.ui.UIView;
import view.ui.message.Messages;
import view.world.WorldView;
import view.world.generator.WorldViewGenerator;
import world.WORLD;

public class VIEW extends CORE_STATE{
	
	private static VIEW i;

	{
		KEYS.init();
	}
	private KeyPoller keyPoller = KEYS.get();
	private final UIView ui;
	private final WorldView world;
	private final SettView sett;
	private final BattleView battle;
	private ViewSubSimple current;
	private ViewSub previous;
	
	private final Mouse mouse;
	
	private final Interrupters inters;
	private boolean hideUI = false;
	private static double renderSecond;
	public static int renI;
	
	private final GAME game;
	
	public VIEW(GAME game){
		
		i = this;
		this.game = game;
		ViewSub.all.clear();
		mouse = new Mouse();
		inters = new Interrupters();
		ui = new UIView();
		world = new WorldView();
		sett = new SettView();
		battle = new BattleView();
		world.activate();
		KEYS.get().readSettings();
		setFirstView(world);
		
		GAME.saver().add(new Savable("VIEW") {
			
			@Override
			protected void save(FilePutter file) {
				if (current instanceof ViewSub)
					file.i(((ViewSub)current).index);
				else
					file.i(-1);
				
			}
			
			@Override
			protected void load(FileGetter file) throws IOException {
				int si = file.i();
				ViewSub v = null;
				if (si >= 0)
					v = ViewSub.all.get(si);

				KEYS.get().readSettings();
				setFirstView(v);
				current.activate();
				
			}
		});
		
		
	}
	


	private void setFirstView(ViewSubSimple prefered) {
		if (WORLD.GEN().isEditing)
			prefered = world.editor;
		else if (prefered == null || !WORLD.GEN().isDone) {
			prefered = new WorldViewGenerator();
		}
		prefered.activate();
		previous = null;
		
	}
	

	@Override
	protected void keyPush(LIST<KeyEvent> keys, boolean hasCleared) {
		keyPoller.poll(keys);
		keyPoller = KEYS.get();
	}
	

	@Override
	protected void mouseClick(MButt button) {
		if (!inters.manager.click(button))
			return;
		
		if (inters.mouseMessage.close())
			return;
		GAME.script().callback.mouseClick(button);
//		hoverTimer = 0;
		if (current.uiManager.click(button))
			current.mouseClick(button);
//		hoverClick = true;
		
	}

	
	private double hoverTimer = 0;

	private void hover() {
		
		COORDINATE mCoo = CORE.getInput().getMouse().getCoo();
		
		int dx = mCoo.x()-mouse.x();
		int dy = mCoo.y()-mouse.y();
		int d = dx*dx+dy*dy;
		boolean mouseHasMoved = d > 5;
		mouse.getCoo().set(mCoo);
		
		GAME.script().callback.hover(mCoo, mouseHasMoved);
		
		
		
		if (mouseHasMoved){
			hoverTimer = 0;
//			hoverClick = false;
		}
		
		
		
		if (inters.manager.hover(mCoo, mouseHasMoved))
			if (current.uiManager.hover(mCoo, mouseHasMoved))
				current.hover(mCoo, mouseHasMoved);
		
		GAME.script().callback.hoverTimer(hoverTimer, inters.mouseMessage.get());
		if (!inters.mouseMessage.get().emptyIs())
			return;
		
		if (inters.manager.hoverTimer(hoverTimer, inters.mouseMessage.get())) 
			if (current.uiManager.hoverTimer(hoverTimer, inters.mouseMessage.get()))
				current.hoverTimer(hoverTimer, inters.mouseMessage.get());
		
		if (!inters.mouseMessage.get().emptyIs()) {
			mouse.setReplacement(UI.icons().m.questionmark);
		}
		
		if (hoverTimer < 0.4) {
			inters.mouseMessage.get().clear();
		}
		
	}
	
	@Override
	protected void update(float ds, double slowDown) {
		

		
		game.afterTick();

		inters.manager.afterTick();
		current.uiManager.afterTick();
		current.afterTick();

		
		
		hover();
		
		//inters.mouseMessage.close();
		hoverTimer += ds;

		if (KEYS.MAIN().DEBUGGER.consumeClick())
			GUTIL.debugger().toggle();
		if (hideUI) {
			if (KEYS.MAIN().ESCAPE.consumeClick() | MButt.RIGHT.consumeAllClick()) {
				hideUI = false;
				inters.mouseMessage.get().clear();
			}else {
				if (VIEW.s().isActive() && VIEW.s().ui.subjects.current() != null) {
					VIEW.s().getWindow().centerer.set( VIEW.s().ui.subjects.current().body().cX(),  VIEW.s().ui.subjects.current().body().cY());
				}
			}
		}
			

		inters.mouseMessage.update(mouse);
		if (inters.manager.update(ds) & current.uiManager.update(ds)) {
			current.update(ds, true);
		}else {
			current.update(ds, false);
			ds = 0;
			slowDown = 1.0;
		}

		if (KEYS.MAIN().SWAP.consumeClick()) {
			if (VIEW.UI().manager.open()) {
				VIEW.UI().manager.close();
				VIEW.world().activate();
			}
			else if (VIEW.world().isActive()){
				VIEW.s().activate();
			}
			else if (VIEW.s().isActive()) {
				VIEW.UI().manager.show();
			}
		}
		
		if (KEYS.MAIN().ESCAPE.consumeClick()) {
			inters.menu.show(); 
		}
		
		if (KEYS.MAIN().QUICKSAVE.consumeClick() && canSave()) {
			SPRITES.loader().minify(true, Dic.¤¤SAVING);
			GAME.saver().quicksave();
			SPRITES.loader().minify(false, Dic.¤¤SAVING);
		}
		
		if (KEYS.MAIN().QUICKLOAD.consumeClick() && GameLoader.quickload()) {
			SPRITES.loader().minify(true, Dic.¤¤load);
			return;
		}
		
		game.update(ds, slowDown);

	}
	
	@Override
	protected void render(Renderer r, float ds) {
		
		renI++;
		renderSecond += ds;
		if (renderSecond > 10000)
			renderSecond -= 10000;
		
		GUTIL.debugger().flush();
		
		if (hideUI) {
			current.render(r, ds, true);
			
			return;
		}
		

		
		
		
		TIME.light().applyGuiLight(ds, C.DIM());
		
		mouse.render(r, ds);
		
		inters.mouseMessage.render(r, ds);
		r.newLayer(true, 0);
		GAME.script().callback.render(r, ds);
		
		
		if (!inters.manager.render(r, ds)) {
			return;
		}
		
		if (!current.uiManager.render(r, ds))
			return;
		
		current.render(r, ds, false);

	}
	
	public static void render() {
		i.render(CORE.renderer(), 0);
		i.inters.manager.afterTick();
		i.current.uiManager.afterTick();
		i.current.afterTick();
	}
	
	public static Mouse mouse(){
		return i.mouse;
	}
	
	public static WorldView world(){
		return i.world;
	}
	
	public static SettView s(){
		return i.sett;
	}
	
	public static BattleView b(){
		return i.battle;
	}
	
	public static ViewSubSimple current() {
		return i.current;
	}

	public static void setPrev() {
		if (i.previous == null)
			i.world.activate();
		else
			i.previous.activate();
	}
	
	public static Interrupters inters(){
		return i.inters;
	}
	
	public static Messages messages() {
		return i.inters.messages;
	}
	
	public static GBox hoverBox() {
		return i.inters.mouseMessage.get();
	}
	
	public static GBox timeBox() {
		return i.inters.mouseMessage.init(i.mouse, true);
	}

	public static void hoverBoxDistance(int max) {
		
		i.inters.mouseMessage.setDistance(max);
		
	}
	

	
	public static boolean hideUI() {
		return i.hideUI;
	}
	
	public static void hide() {
		i.hideUI = true;
	}
	
	public static double renderSecond() {
		return renderSecond;
	}
	
	public static boolean existTemp() {
		return i != null;
	}
	
	public static UIView UI() {
		return i.ui;
	}
	
	
	public static int RI() {
		return renI;
	}
	
	public static void setKeyPoller(KeyPoller poller) {
		i.keyPoller = poller;
	}
	
	public static boolean canSave() {
		return i.inters.manager.canSave() && VIEW.current().uiManager.canSave() && VIEW.current().canSave();
	}
	
	@Override
	protected void exit() {
		GAME.count().flush();
	}
	
	public static abstract class ViewSubSimple{
		
		protected abstract void hoverTimer(double mouseTimer, GBox text);
		
		protected boolean canSave() {
			return true;
		}

		protected abstract boolean update(float ds, boolean shouldUpdate);
		protected abstract void render(Renderer r, float ds, boolean hide);
		
		public void renderBelowTerrain(Renderer r, ShadowBatch s, RenderData data) {
			
		}
		
		protected abstract void mouseClick(MButt button);
		protected abstract void hover(COORDINATE mCoo, boolean mouseHasMoved);
		public final InterManager uiManager = new InterManager();
		
		public void activate(){
			
			if (i.current == this)
				return;
			if (i.current != null)
				i.current.deactivate();
			i.inters.mouseMessage.close();
			if (i.current instanceof ViewSub)
				i.previous = (ViewSub) i.current;
			i.current = this;
			hover(CORE.getInput().getMouse().getCoo(), true);
		}
		
		public void deactivate() {
			
		}
		
		public final boolean isActive(){
			return this == VIEW.i.current;
		}
		
		protected void afterTick() {
			
		}
		
	}
	
	public static abstract class ViewSub extends ViewSubSimple{

		private static final ArrayList<ViewSub> all = new ArrayList<>(20);
		private final int index = all.add(this);
		
//		public final ArrayListGrower<SAVABLE> savers = new ArrayListGrower<>();
		
		public int index() {
			return index;
		}
		
//		protected void save(FilePutter file) {
//			for (SAVABLE s : savers)
//				s.save(file);
//		}
//		protected void load(FileGetter file) throws IOException {
//			for (SAVABLE s : savers)
//				s.load(file);
//		}
		
		
		
	}

//	private static final ArrayListGrower<Tuple<String, SAVABLE>> savers = new ArrayListGrower<>();
//	
//	public static void addSpecialSaver(String key, SAVABLE s) {
//		savers.add(new Tuple.TupleImp<String, SAVABLE>(key, s));
//	}
	
}
