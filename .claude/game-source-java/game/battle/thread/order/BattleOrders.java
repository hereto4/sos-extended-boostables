package game.battle.thread.order;

import java.io.IOException;

import game.GAME;
import game.battle.div.Div;
import game.battle.formation.DivFormationImp;
import game.battle.thread.BattleThread;
import init.constant.Config;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sprite.text.Str;

public final class BattleOrders extends BattleThread{

	private final BattleOrder[] orders = new BattleOrder[Config.battle().DIVISIONS_PER_ARMY*2];
	private final DivFormationImp[] nexts = new DivFormationImp[Config.battle().DIVISIONS_PER_ARMY*2];
	private final BattleOrderUpdater plans = new BattleOrderUpdater();
	private DivFormationImp tmp = new DivFormationImp();
	
	
	public BattleOrders() {
		super(1.0/60);
		for (int i = 0; i < orders.length; i++) {
			orders[i] = new BattleOrder();
			nexts[i] = new DivFormationImp();
		}
	}

	@Override
	protected void save(FilePutter file) {
		for (BattleOrder o : orders)
			o.save(file);
		for (DivFormationImp f : nexts)
			f.save(file);
		plans.save(file);
	}
	
	@Override
	protected void load(FileGetter file) throws IOException {
		
		for (BattleOrder o : orders)
			o.load(file);
		for (DivFormationImp f : nexts)
			f.load(file);
		plans.load(file);
		
	}
	
	@Override
	protected void init() {
		plans.clear();
		for (short di = 0; di < orders.length; di++) {
			orders[di].clear();
		}
		for (short di = 0; di < orders.length; di++) {
			update(GAME.ARMIES().division(di));
		}
	}

	@Override
	protected void doThreadJob() {
		for (short di = 0; di < orders.length; di++) {
			if (!thread.working())
				break;
			update(GAME.ARMIES().division(di));
		}
		
	}
	
	private void update(Div div) {
		DivFormationImp f = plans.update(div, orders[div.index()], nexts[div.index()]);
		if (f != null) {
			tmp.copy(f);
			DivFormationImp oo = nexts[div.index()];
			nexts[div.index()] = tmp;
			tmp = oo;
		}
	}
	
	public static BattleOrder get(Div div) {
		return GAME.BATTLE_THREADS().orders.orders[div.index()];
	}

	public static DivFormationImp next(Div div) {
		return GAME.BATTLE_THREADS().orders.nexts[div.index()];
	}
	
	public void init(Div div) {
		boolean started = thread.working();
		
		stop();
		orders[div.index()].dest.get(nexts[div.index()]);
		
		orders[div.index()].task.set(new BattleOrderTask().stop(div));
		if (started)
			start();
	}
	
	public static void debug(Div div, Str text) {
		BattleOrders s = GAME.BATTLE_THREADS().orders;
		s.plans.debug(div, text);
		
	}
	
}
