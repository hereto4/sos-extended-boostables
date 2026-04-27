package snake2d.util.datatypes;

public interface DIMENSION {

	public int width();
	public int height();
	
	
	public class Imp implements DIMENSION{

		private double w, h;
		
		
		@Override
		public int width() {
			return (int) w;
		}

		@Override
		public int height() {
			return (int) h;
		}
		
		public Imp widthSet(double w) {
			this.w = w;
			return this;
		}
		
		public Imp heightSet(double h) {
			this.h= h;
			return this;
		}
		
		
	}
	
}
