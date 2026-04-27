package game.boosting.superb;

import java.io.IOException;
import java.util.Arrays;

import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;

public class SuperData implements SAVABLE{

	private final SuperBoostable<?> daddy;
	private double values[];
	private double times[];
	private double state[];
	
	SuperData(SuperBoostable<?> daddy){
		this.daddy = daddy;
	}

	private void init() {
		if (values == null || values.length != daddy.ups.size()) {
			values = new double[daddy.ups.size()];
			times = new double[daddy.ups.size()];
			state = new double[daddy.ups.size()];
		}
	}
	
	double[] values() {
		init();
		return values;
	}
	
	double[] times() {
		init();
		return times;
	}
	
	double[] states() {
		init();
		return state;
	}
	
	@Override
	public void save(FilePutter file) {
		init();
		for (int i = 0; i < values.length; i++) {
			file.d(values[i]);
			file.d(times[i]);
			file.d(state[i]);
		}
	}

	@Override
	public void load(FileGetter file) throws IOException {
		init();
		clear();
		
		int[] so = daddy.saveOrder();
		for (int i = 0; i < so.length; i++) {
			double v = file.d();
			double t = file.d();
			double s = file.d();
			if (so[i] >= 0 && so[i] < values.length) {
				values[so[i]] = v;
				times[so[i]] = t;
				state[so[i]] = s;
			}
		}
	}

	@Override
	public void clear() {
		init();
		Arrays.fill(values, 0);
		Arrays.fill(times, 0);
		Arrays.fill(state, 0);
	}
	
}
