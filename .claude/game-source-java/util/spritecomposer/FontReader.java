package util.spritecomposer;

import java.nio.file.Path;

import snake2d.Errors;
import snake2d.util.file.SnakeImage;
import snake2d.util.sprite.text.Font;
import snake2d.util.sprite.text.Font.FontGlyph;

public class FontReader {
	
	private final int GREEN = 0x00FF00FF;
	private final int glyphs;
	
	
	
	public FontReader(String charset){
		glyphs = charset.length();
		Font.setCharset(charset);
	}
	
	public Font get(final int x1, final int y1, SnakeImage source, Path path, int trail) {

		
		int x = x1 + 1;
		int y = y1;
		
		if (source.rgb.get(x, y) != GREEN)
			throw new Errors.DataError("error with font. Expecting full green at pixel: " + x + "," + y, path);

		int height = 0;
		
		while (true) {
			if (y >= source.height-1) {
				throw new Errors.DataError("unable to find height of font. Make sure surrounding edges are full green!", path);
			}
			y++;
			int col = source.rgb.get(x, y);
			if (col == GREEN) {
				height = y-y1-1;
				break;
			}
		}
		

		
		FontGlyph[] ggs = new FontGlyph[glyphs];
		for (int i = 0; i < ggs.length; i++)
			ggs[i] = new FontGlyph();
		
		for (int i = 0; i < glyphs; i++) {
			ggs[i].ty1 = (short) (y1+1);
			ggs[i].tx1 = (short) (x);
			ggs[i].width = getWidth(x, y1+1, source, ""+path, i);
			x += ggs[i].width +1;
		}
		
		return new Font(ggs, height, 1.0, trail);
		

		
	}
	
	private byte getWidth(int x1, int y1, SnakeImage source, String path, int glyph) {
		
		int w = 0;
		
		while(source.rgb.get(x1, y1) != GREEN) {
			if (w > 100)
				throw new Errors.DataError("unable to find width of gyph " + glyph + ". At pixel: " + x1 + "," + y1, path);
			if (x1 >= source.width)
				throw new Errors.DataError("unable2 to find width of gyph " + glyph + ". At pixel: " + x1 + "," + y1, path);
			w++;
			x1++;
		}
		
		return (byte) w;
		
	}
	

	
}
