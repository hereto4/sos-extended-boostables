package snake2d.util.sprite.text;

import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.TextureCoords;

public class Text extends Str implements SPRITE {

	protected int maxWidth = 2000;
	private int width;
	private int height;
	private Font font;
	private double scale = 1;
	private boolean multipleLines = true;
		
	public Text(Font font, CharSequence text) {
		super(text.length());
		this.font = font;
		set(text);
	}

	public Text(Font font, int size) {
		super(size);
		this.font = font;
		adjustWidth();
	}
	
	public Text(Font font, int size, int width) {
		super(size);
		this.font = font;
		adjustWidth();
	}

	@Override
	public Text clear() {
		super.clear();
		adjustWidth();
		return this;
	}

	@Override
	public Text add(CharSequence string) {
		super.add(string);
		return this;
	}

	public Text adjustWidth() {
		COORDINATE c = font.getDim(this, Integer.MAX_VALUE, scale);
		width = c.x();
		height = c.y();
		if (height == 0)
			height = font.height(scale);
		
		if (!multipleLines) {
			height = font.height(scale);
			if (width > maxWidth) {
				width = maxWidth;
			}
		}else if (width > maxWidth) {
			width = maxWidth;
			height = font.getHeight(this, width);
			width = font.getDim(this, width+2, scale).x();
		}
		
		return this;
	}

	public int maxWidth() {
		return maxWidth;
	}
	
	@Override
	public Text addBinary(int i) {
		super.addBinary(i);
		return this;
	}
	
	@Override
	public Text addBinary(long i) {
		super.addBinary(i);
		return this;
	}
	
	@Override
	public Text add(long i, boolean format) {
		super.add(i, format);
		return this;
	}
	
	@Override
	public Text add(long i) {
		super.add(i);
		return this;
	}
	
	@Override
	public Text add(double d) {
		super.add(d);
		return this;
	}
	
	@Override
	public Text add(char chars) {
		super.add(chars);
		return this;
	}
	
	public Text para(CharSequence str) {
		if (length() > 0)
			s();
		add('(');
		add(str);
		add(')');
		return this;
	}
	
	
	@Override
	public Text s() {
		super.s();
		return this;
	}
	
	@Override
	public Text s(int i) {
		super.s(i);
		return this;
	}
	
	@Override
	public Text add(boolean b) {
		super.add(b);
		return this;
	}

	public Text setFont(Font font) {
		this.font = font;
		this.height = font.height();
		adjustWidth();
		return this;
	}
	
	public Font getFont() {
		return font;
	}

//	public Text set(Object o) {
//		return set(o.toString());
//	}
	
	public Text set(CharSequence s) {
		clear();
		add(s);
		return adjustWidth();
	}
//	
//	public Text setText(Object o) {
//		return set(o.toString());
//	}

	@Override
	public int width() {
		return width;
	}

	@Override
	public int height() {
		return height;
	}

	public int getHeight(int width) {
		return font.getHeight(this, width);
	}

	public Text setScale(double scale) {
		this.scale = scale;
		adjustWidth();
		return this;
	}
	
	public Text setMaxWidth(int max) {
		this.maxWidth = max;
		if (width > maxWidth)
			adjustWidth();
		return this;
	}

	@Override
	public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
		int width = X2 - X1;
		if (width <= 0)
			width = maxWidth;
		
		if (multipleLines)
			font.render(r, this, X1, Y1, width, scale);
		else {
			font.renderCropped(r, this, X1, Y1, width, scale);
		}
	}

	public Text setMultipleLines(boolean m) {
		this.multipleLines = m;
		adjustWidth();
		return this;
	}
	
	@Override
	public void renderTextured(TextureCoords texture, int X1, int X2, int Y1,
			int Y2) {
		int width = X2 - X1;
		if (width <= 0)
			width = maxWidth;
		//font.renderTextured(this, X1, Y1, width, scale, texture);
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof CharSequence) {
			CharSequence o = (CharSequence) other;
			return o.toString().equalsIgnoreCase(this.toString());
		}
		return false;
	}
	
	@Override
	public Text toCamel() {
		super.toCamel();
		adjustWidth();
		return this;
	}

	@Override
	public Text toLower() {
		super.toLower();
		adjustWidth();
		return this;
	}

	@Override
	public Text toUpper() {
		super.toUpper();
		adjustWidth();
		return this;
	}

}
