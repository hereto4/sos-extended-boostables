package game.battle.thread.trajectory;

import java.io.IOException;
import java.util.Arrays;

import init.constant.Config;
import settlement.entity.humanoid.Humanoid;
import settlement.thing.projectiles.Trajectory;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;

final class DivTrajectory {

	int targets = 0;
	boolean potential;
	private final float[] data = new float[Config.battle().MEN_PER_DIVISION*3];
	private static Trajectory tra = new Trajectory();
	
	
	public DivTrajectory() {

	}
	
	void set(int pos, Trajectory t) {
		int i = pos*3;
		targets++;
		data[i] = (float) t.vx();
		data[i+1] = (float) t.vy();
		data[i+2] = (float) t.vz();
	}

	public boolean has(int pos) {
		return !Float.isNaN(data[pos*3]);
	}
	
	
	public Trajectory get(int pos, Humanoid a) {
		int i = pos*3;
		if (Float.isNaN(data[i]))
			return null;
		
		tra.set(data[i], data[i+1], data[i+2]);
		return tra;
	}
	
	public void save(FilePutter file) {
		file.fs(data);
		file.i(targets);
		file.bool(potential);
	}

	public void load(FileGetter file) throws IOException {
		file.fs(data);
		targets = file.i();
		potential = file.bool();
	}
	
	public void clear() {
		Arrays.fill(data, Float.NaN);
		targets = 0;
		potential = false;
	}
	
	
	
}
