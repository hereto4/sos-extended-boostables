package game.battle.thread.general;

import java.io.IOException;

import game.battle.thread.BattleThread;
import game.battle.thread.general.offence.Strategos2000UpdaterOffense;
import game.time.TIME;
import settlement.main.ON_TOP_RENDERABLE;
import snake2d.Renderer;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.ACTION;
import util.rendering.RenderData;
import util.rendering.RenderData.RenderIterator;
import util.rendering.ShadowBatch;
import view.interrupter.IDebugPanel;

public class Strategos2000 extends BattleThread {

	private final StrategosUtil context;
	private Strategos2000Updater current;
	private final Strategos2000Updater offense;
	private int oldDivs;
	private boolean debug = false;
	private double prevUpdate = 0;

	public Strategos2000() {
		super(1.0/120.0);
		this.context = new StrategosUtil();

		offense = new Strategos2000UpdaterOffense(context);
		current = offense;
		
		IDebugPanel.add("Battle General Debug", new ACTION() {
			ON_TOP_RENDERABLE ren = new ON_TOP_RENDERABLE() {

				@Override
				public void render(Renderer r, ShadowBatch shadowBatch, RenderData data, double ds) {
					RenderIterator it = data.onScreenTiles();

					if (current == null)
						return;
					
					while (it.has()) {
						current.render(r, it);
						it.next();
					}
					
					current.render(r, shadowBatch, data);
					
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
		
		IDebugPanel.add("battle general pause", new ACTION() {
			
			@Override
			public void exe() {
				stop();
			}
		});
		IDebugPanel.add("battle general unpause", new ACTION() {
			
			@Override
			public void exe() {
				stop();
				start();
			}
		});
	}

	@Override
	public void save(FilePutter file) {
		file.mark(offense);
		offense.save(file);
		file.mark(offense);
	}

	@Override
	public void load(FileGetter file) throws IOException {
		file.check(offense);
		offense.load(file);
		file.check(offense);
	}


	public enum State {

		ATTACK

	}

	@Override
	protected void init() {
		offense.clear();
	}

	@Override
	protected void doThreadJob() {
		int newDivs = 0;
		for (int di = 0; di < context.getArmy().divisions().size(); di++) {
			if (context.getArmy().divisions().get(di).active())
				newDivs++;
		}
		if (newDivs == 0) {
			if (oldDivs > 0) {
				offense.clear();
			}
		}
		oldDivs = newDivs;
		
		
		if (prevUpdate - TIME.currentSecond() > -2)
			return;
		
		prevUpdate = TIME.currentSecond() + 2;
		
		while(thread.working()) {
			if (!offense.update())
				break;
		}
		
	}

}
