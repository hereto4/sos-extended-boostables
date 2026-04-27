package snake2d.util.sprite;

public class TextureCoords {

	
	public short x1;
//	public short x1();
//	public short x2();
//	public short y1();
//	public short y2();

	public short x2;

	public short y1;

	public short y2;
	
	public static final TextureCoords Texture = new TextureCoords();
	public static final TextureCoords Normal = new TextureCoords();
	
	public TextureCoords get(int x1, int y1, int width, int height) {
		this.x1 = (short) x1; this.x2 = (short) (x1+width);  this.y1 = (short) y1;  this.y2 = (short) (y1+height);
		return this;
	}
	public TextureCoords get(TextureCoords other) {
		this.x1 = other.x1; this.x2 = other.x2;  this.y1 = other.y1;  this.y2 = other.y2;
		return this;
	}
	
	public int width() {
		return x2-x1;
	}

	public int height() {
		return y2-y1;
	}
	
	public TextureCoords(int x1, int x2, int y1, int y2) {
		this.x1 = (short) x1;
		this.y1 = (short) y1;
		this.x2 = (short) x2;
		this.y2 = (short) y2;
	}
	
	public TextureCoords() {
		
	}
	
//	public class Imp implements TextureCoords, DIMENSION{
//
//		
//		
//		public TextureCoords get(int x1, int y1, int width, int height) {
//			this.x1 = (short) x1; this.x2 = (short) (x1+width);  this.y1 = (short) y1;  this.y2 = (short) (y1+height);
//			return this;
//		}
//		public TextureCoords get(TextureCoords other) {
//			this.x1 = other.x1(); this.x2 = other.x2();  this.y1 = other.y1();  this.y2 = other.y2();
//			return this;
//		}
//		
//		@Override
//		public short y2() {
//			return y2;
//		}
//		
//		@Override
//		public short y1() {
//			return y1;
//		}
//		
//		@Override
//		public short x2() {
//			return x2;
//		}
//		
//		@Override
//		public short x1() {
//			return x1;
//		}
//
//		@Override
//		public int width() {
//			return x2-x1;
//		}
//
//		@Override
//		public int height() {
//			return y2-y1;
//		}
//	}
	
}

