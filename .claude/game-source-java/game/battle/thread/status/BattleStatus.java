package game.battle.thread.status;

import java.io.IOException;

import game.GAME;
import game.battle.div.Div;
import game.battle.thread.BattleThread;
import init.sprite.UI.UI;
import settlement.main.ON_TOP_RENDERABLE;
import snake2d.Renderer;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.ACTION;
import snake2d.util.sprite.text.Str;
import util.rendering.RenderData;
import util.rendering.RenderData.RenderIterator;
import util.rendering.ShadowBatch;
import view.interrupter.IDebugPanel;

public final class BattleStatus extends BattleThread {

	private BattleContext current = new BattleContext();
	private volatile BattleContext nextnext = new BattleContext();
	private volatile BattleContext next = nextnext;
	private final Updater updater = new Updater();
	
	public BattleStatus() {
		super(0.5);
		IDebugPanel.add("Battle Status Debug", new ACTION() {
			boolean debug = false;
			ON_TOP_RENDERABLE ren = new ON_TOP_RENDERABLE() {

				@Override
				public void render(Renderer r, ShadowBatch shadowBatch, RenderData data, double ds) {
					RenderIterator it = data.onScreenTiles();
					if (current == null)
						return;
					
					while (it.has()) {
						int p = current.map.soldiers(GAME.ARMIES().player()).get(it.tile());
						int e = current.map.soldiers(GAME.ARMIES().enemy()).get(it.tile());
						if (p != 0 || e != 0) {
							
							Str.TMP.clear().add(p).add('/').add(e);
							UI.FONT().S.render(r, Str.TMP, it.x(), it.y());
							
						}
						it.next();
					}
					if (!debug)
						remove();
				}
			};
			@Override
			public void exe() {
				debug = !debug;
				ren.add();
			}
		});
		
		
		
		
		new Tests(this);
		
	}

	@Override
	protected void stop() {
		updater.stop = true;
		super.stop();
		updater.stop = false;
	}
	
	@Override
	protected void save(FilePutter file) {
		
	}
	
	@Override
	protected void load(FileGetter file) throws IOException {
		
	}
	
	@Override
	protected void init() {
		updater.init(current);
	}

	@Override
	protected void doThreadJob() {
		
		if (next == null) {
			next = nextnext;
			return;
		}
		
		updater.init(next);
		BattleContext c = current;
		current = next;
		nextnext = c;
		next = null;
	}
	
	public static DivStatus status(Div d) {
		return GAME.BATTLE_THREADS().status.current.statuses[d.index()];
	}
	
	public static DivsTileMap map() {
		return GAME.BATTLE_THREADS().status.current.map;
	}
	
	public static DivsQuadMap quads() {
		return GAME.BATTLE_THREADS().status.current.quads;
	}
	
	public static DivsSpaceMap space() {
		return GAME.BATTLE_THREADS().status.current.space;
	}

	public static DivArmyMap army() {
		return GAME.BATTLE_THREADS().status.current.army;
	}
	
	
}
