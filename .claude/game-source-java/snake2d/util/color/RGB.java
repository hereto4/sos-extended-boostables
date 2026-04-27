package snake2d.util.color;

public interface RGB {


	public double r();
	
	public double g();
	
	public double b();
	
	public final static RGB WHITE = new RGBImp().set(1, 1, 1);
	
	public class RGBImp implements RGB{

		private double red;
		private double green;
		private double blue;
		
		@Override
		public double r() {
			return red;
		}
		
		@Override
		public double g() {
			return green;
		}
		
		@Override
		public double b() {
			return blue;
		}

		public RGBImp r(double r) {
			red = r;
			return this;
		}
		

		public RGBImp g(double g) {
			green = g;
			return this;
		}
		
		public RGBImp b(double b) {
			blue = b;
			return this;
		}
		
		public RGBImp set(double r, double g, double b) {
			return r(r).g(g).b(b);
		}
		
		public RGBImp shade(double shade) {
			red *= shade;
			green*=shade;
			blue*= shade;
			return this;
		}
		
		public RGBImp copy(RGB other) {
			this.red = other.r();
			this.green = other.g();
			this.blue = other.b();
			return this;
		}
		
		public RGBImp interpolate(RGB a, RGB b, double part) {
			r(a.r() + (b.r()-a.r())*part);
			g(a.g() + (b.g()-a.g())*part);
			b(a.b() + (b.b()-a.b())*part);
			return this;
		}

		public RGBImp multiply(RGB currentDay) {
			r(r()*currentDay.r());
			g(g()*currentDay.g());
			b(b()*currentDay.b());
			return this;
		}
		
		public RGBImp multiply(RGB currentDay, double part) {
			r(r()*currentDay.r());
			g(g()*currentDay.g());
			b(b()*currentDay.b());
			return this;
		}
		
	}
	
}
