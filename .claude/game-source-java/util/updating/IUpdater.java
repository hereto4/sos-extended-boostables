package util.updating;

import java.io.IOException;
import java.io.Serializable;

import snake2d.LOG;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;

public abstract class IUpdater implements SAVABLE{

	private final int amount;
	private int i;
	private final double secondsBetween;
	private final double tilesPerSecond;
	private double acc = 0;
	
	
	public IUpdater(int amount, double secondsBetween) {
		this.amount = amount;
		this.secondsBetween = secondsBetween;
		tilesPerSecond = (amount/secondsBetween);
	}
	
	public void update(double ds) {
		
		acc += ds*tilesPerSecond;
		
		int a = (int) acc;
		acc -= a;
		while(a > 0) {
			a--;
			update(i, secondsBetween);
			i++;
			if (i >= amount)
				i = 0;
		}
	}
	
	protected abstract void update(int i, double timeSinceLast);
	
	@Override
	public void save(FilePutter file) {
		file.i(i);
		file.d(acc);
	}
	
	@Override
	public void load(FileGetter file) throws IOException {
		i = file.i();
		i = i % amount;
		acc = file.d();
	}
	
	@Override
	public void clear() {
		i = 0;
		acc = 0;
	}
	
	public void debug() {
		LOG.ln();
		LOG.ln(this.amount);
		LOG.ln(this.secondsBetween);
		LOG.ln(this.tilesPerSecond);
		LOG.ln(acc);
		LOG.ln(i);
		
	}
	
	public static abstract class IUpdaterSer implements Serializable{

		private final int amount;
		private int i;
		private final double secondsBetween;
		private final double tilesPerSecond;
		private double acc = 0;
		
		
		public IUpdaterSer(int amount, double secondsBetween) {
			this.amount = amount;
			this.secondsBetween = secondsBetween;
			tilesPerSecond = (amount/secondsBetween);
		}
		
		public void update(double ds) {
			
			acc += ds*tilesPerSecond;
			
			int a = (int) acc;
			acc -= a;
			while(a > 0) {
				a--;
				update(i, secondsBetween);
				i++;
				if (i >= amount)
					i = 0;
			}
		}
		
		protected abstract void update(int i, double timeSinceLast);
		
		public void debug() {
			LOG.ln();
			LOG.ln(this.amount);
			LOG.ln(this.secondsBetween);
			LOG.ln(this.tilesPerSecond);
			LOG.ln(acc);
			LOG.ln(i);
			
		}
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		
		
	}
	
}
