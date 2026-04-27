package game.battle.thread.trajectory;

import java.io.IOException;

import game.GAME;
import game.battle.div.Div;
import game.battle.thread.BattleThread;
import game.time.TIME;
import init.constant.Config;
import settlement.entity.humanoid.Humanoid;
import settlement.thing.projectiles.Trajectory;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;

public final class BattleTrajectories extends BattleThread{

	
	
	private int divI = 0;
	private double currentSecond = TIME.currentSecond();
	private final UpdaterTraj up = new UpdaterTraj();
	private final UpdaterArtillery art = new UpdaterArtillery();
	
	private final DivTrajectory[] all = new DivTrajectory[Config.battle().DIVISIONS_PER_BATTLE];
	private final Request[] request = new Request[Config.battle().DIVISIONS_PER_BATTLE];
	
	public BattleTrajectories() {
		super(1.0/60.0);
		for (int i = 0; i < all.length; i++) {
			all[i] = new DivTrajectory();
			request[i] = new Request();
		}
	}

	@Override
	public void save(FilePutter file) {
		file.i(divI);
		for (DivTrajectory t : all)
			t.save(file);
		for (Request r : request)
			r.save(file);
	}

	@Override
	public void load(FileGetter file) throws IOException {
		divI = file.i();
		for (DivTrajectory t : all)
			t.load(file);
		for (Request r : request)
			r.load(file);
	}

	@Override
	protected void init() {
		for (DivTrajectory t : all)
			t.clear();
		for (Request r : request)
			r.clear();
		currentSecond = TIME.currentSecond();
	}
	
	public void clear() {
		divI = 0;
	}



	@Override
	protected void doThreadJob() {
		double curr = TIME.currentSecond();
		
		double ds = (curr-currentSecond)*Config.battle().DIVISIONS_PER_BATTLE;
		if (ds > 0) {
			int old = divI;
			while(ds > 0) {
				
				
				if (divI == Config.battle().DIVISIONS_PER_BATTLE)
					art.update();
				else {
					DivTrajectory n = up.update(request[divI], GAME.ARMIES().division((short) divI), all[divI]);
					all[divI] = n;
				}
				divI++;
				
				divI %= Config.battle().DIVISIONS_PER_BATTLE+1;
				if (divI == old)
					break;
				ds -= 1;
			}
			currentSecond = TIME.currentSecond();
		}
	}
	
	public static Trajectory request(Humanoid h, Div div) {
		int pos = div.reporter.positionSpot(h);
		
		if (GAME.BATTLE_THREADS().trajs.request[div.index()].request(pos, h, div)) {
			return GAME.BATTLE_THREADS().trajs.all[div.index()].get(pos, h);
		}
		return null;
	}
	
	public static void register(Humanoid h, Div div) {
		GAME.BATTLE_THREADS().trajs.request[div.index()].request(div.reporter.positionSpot(h), h, div);
	}
	
	public static int trajectories(Div div) {
		return GAME.BATTLE_THREADS().trajs.all[div.index()].targets;
	}
	
	public static boolean hasPotential(Div div) {
		return GAME.BATTLE_THREADS().trajs.all[div.index()].potential;
	}
	
}
