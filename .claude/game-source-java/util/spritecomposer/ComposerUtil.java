package util.spritecomposer;

import static util.spritecomposer.Resources.g;
import static util.spritecomposer.Resources.p;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import init.paths.PATHS;
import snake2d.CORE;
import snake2d.Errors;
import snake2d.util.file.SnakeImage;
import util.spritecomposer.ComposerSources.Source;

public final class ComposerUtil {


	private Path sourcePath;
	private SnakeImage TexSource;
	private int sourceHalf;
	private final int[][] buffer = new int[1024][1024];
	private final int[][] buffern = new int[1024][1024];

	ComposerUtil() {
	
	}
	
	void drawNormal(ComposerDests.Dest dest, int x, int y, int width, int height) {
		for (int y1 = 0; y1 < height; y1++) {
			for (int x1 = 0; x1 < width; x1++) {
				dest.normalSet(x+x1, y+y1, 0x80, 0x80, 0xFF, 0xFF);
			}
		}
	}

	void setSource(Path path, int width, int minHeight) throws IOException{
		setSource(path);
		if (p != null) {
			
			if (TexSource.width != width) {
				throw new Errors.DataError("Image has the wrong width of " + TexSource.width + "\n"
						+ "resize the image's width to " + width, ""+path);
			}if (TexSource.height < minHeight) {
				throw new Errors.DataError("Image has the wrong height of " + TexSource.height + "\n"
						+ "resize the image's hight to at least " + minHeight, ""+path);
			}
		}
	}
	
	void setSource(Path path) throws IOException{
		if (p != null) {
			sourcePath = path;
			if (TexSource != null) {
				TexSource.dispose();
				TexSource = null;
			}
			CORE.checkIn();
			TexSource = new SnakeImage(path);
			if (TexSource.width % 2 != 0)
				throw new RuntimeException(path + "  has the wrong dimension. Width must be divisible by 2");
			sourceHalf = TexSource.width / 2;
			saveFile(path);
		}else {
			validateFile(path);
		}
	}
	
	
	void saveFile(Path path) {
		p.chars(PATHS.getSavePath(path));
		try {
			p.l(Files.getLastModifiedTime(path).toMillis());
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}
	
	void validateFile(Path path) throws IOException{
		
		String path2 = g.chars(); 
		if (!PATHS.getSavePath(path).equals(path2))
			throw new IOException(PATHS.getSavePath(path) + " " + path2);
		long l = Files.getLastModifiedTime(path).toMillis();
		long l2 = g.l();
		if (l != l2)
			throw new IOException(path + " " + l + " " + l2);
	}
	
	Path getSourcePath() {
		return sourcePath;
	}
	
	void copy(Source source) {
		
		int sx = source.x1();
		int sy = source.y1();
		
		for (int y = 0; y < source.height(); y++) {
			for (int x = 0; x < source.width(); x++) {
				buffer[y][x] = TexSource.rgb.get(sx + x, sy + y); 
				if (sx + x + sourceHalf >= TexSource.width) {
					System.err.println(sx + " " + x + " " + sourceHalf);
					System.err.println(source.width() + " " + source.height());
				}
				buffern[y][x] = TexSource.rgb.get(sx + x + sourceHalf, sy + y);
			}
		}
	}
	
	void blendWithBackground(ComposerDests.Dest background, Source stencil, Source foreground) {
		
		int sx = stencil.x1();
		int sy = stencil.y1();
		int bx = background.x1();
		int by = background.y1();
		int fx = foreground.x1();
		int fy = foreground.y1();
		
		for (int y = 0; y < stencil.height(); y++) {
			for (int x = 0; x < stencil.width(); x++) {

				int sc = TexSource.rgb.get(sx + x, sy + y);
				int bc = background.diffuseGet(bx + x, by + y);
				
				if ((sc & 0x0000FF00) == 0) {
					buffer[y][x] = bc; 
					buffern[y][x] = background.normalGet(bx + x, by + y);
					continue;
				}
				
				buffer[y][x] = merge(
						sc,
						TexSource.rgb.get(fx + x, fy + y),
						bc
						);
				
				buffern[y][x] = mergeNormal(sc, TexSource.rgb.get(fx + x + sourceHalf, fy + y), background.normalGet(bx + x, by + y));

			}
		}
	}
	
	private int merge(int m, int c1, int c2) {
		
		m = m & 0x0000FF00;
		m = m >> 8;
		double value = ((double) m)/255.0;
		
		int c1a = (c1 >> 8) & 0x000000FF;
		int c1b = (c1 >> 16) & 0x000000FF;
		int c1c = (c1 >> 24) & 0x000000FF;
		
		int c2a = (c2 >> 8) & 0x000000FF;
		int c2b = (c2 >> 16) & 0x000000FF;
		int c2c = (c2 >> 24) & 0x000000FF;
		
		int t;
		int res = 0x000000FF;
		
		t = (int) (c1c*value + c2c*(1.0-value));
		res |= t << 24;
		
		t = (int) (c1b*value + c2b*(1.0-value));
		res |= t << 16;
		
		t = (int) (c1a*value + c2a*(1.0-value));
		res |= t << 8;
		
		return res;
	}
	
	private int mergeV(double value, int c1, int c2) {
		
		int c1a = (c1 >> 8) & 0x000000FF;
		int c1b = (c1 >> 16) & 0x000000FF;
		int c1c = (c1 >> 24) & 0x000000FF;
		
		int c2a = (c2 >> 8) & 0x000000FF;
		int c2b = (c2 >> 16) & 0x000000FF;
		int c2c = (c2 >> 24) & 0x000000FF;
		
		int t;
		int res = 0x000000FF;
		
		t = (int) (c1c*value + c2c*(1.0-value));
		res |= t << 24;
		
		t = (int) (c1b*value + c2b*(1.0-value));
		res |= t << 16;
		
		t = (int) (c1a*value + c2a*(1.0-value));
		res |= t << 8;
		
		return res;
	}
	
	private int mergeNormal(int m, int c1, int c2) {
		
		m = m & 0x0000FF00;
		m = m >> 8;
		double value = ((double) m)/255.0;
		
		int c1a = (c1 >> 8) & 0x000000FF;
		int c1b = (c1 >> 16) & 0x000000FF;
		int c1c = (c1 >> 24) & 0x000000FF;
		
		int c2a = (c2 >> 8) & 0x000000FF;
		int c2b = (c2 >> 16) & 0x000000FF;
		int c2c = (c2 >> 24) & 0x000000FF;
		
		double r = c1c*value + c2c*(1.0-value);
		double g = c1b*value + c2b*(1.0-value);
		double b = c1a*value + c2a*(1.0-value);
		
		double l = 1.0 / Math.sqrt(r*r + g*g + b*b);
		r*= l;
		g*= l;
		b*= l;
		
		int t;
		int res = 0x000000FF;
		
		t = (int) (c1c*value + c2c*(1.0-value));
		res |= t << 24;
		
		t = (int) (c1b*value + c2b*(1.0-value));
		res |= t << 16;
		
		t = (int) (c1a*value + c2a*(1.0-value));
		res |= t << 8;
		
		return res;
	}
	
	void paste(ComposerDests.Dest dest) {

		for (int y = 0; y < dest.height(); y++) {
			for (int x = 0; x < dest.width(); x++) {
				int dx = x;
				int dy = y;

				int c = buffer[y][x];
				if ((c & 0x000000FF) == 0)
					continue;
				dest.diffuseSet(dest.x1() + dx, dest.y1() + dy, c);
				
				int nc = buffern[y][x];
				if ((nc & 0x000000FF) == 0)
					continue;
				dest.normalSet(dest.x1() + dx, dest.y1() + dy, nc);
			}

		}

	}
	
	void paste(ComposerDests.Dest dest, double bgBlend) {

		for (int y = 0; y < dest.height(); y++) {
			for (int x = 0; x < dest.width(); x++) {
				int dx = x;
				int dy = y;

				int c = buffer[y][x];
				if ((c & 0x000000FF) == 0)
					continue;
				
				c = mergeV(bgBlend, dest.diffuseGet(dest.x1() + dx, dest.y1() + dy), c);
				
				dest.diffuseSet(dest.x1() + dx, dest.y1() + dy, c);
				
				int nc = buffern[y][x];
				if ((nc & 0x000000FF) == 0)
					continue;
				dest.normalSet(dest.x1() + dx, dest.y1() + dy, nc);
			}

		}

	}
	
	void pasteNormalOnly(ComposerDests.Dest dest, int rotation) {

		for (int y = 0; y < dest.size(); y++) {
			for (int x = 0; x < dest.size(); x++) {
				int dx = x;
				int dy = y;
				int r = rotation;

				int nc = buffern[y][x];
				if ((nc & 0x000000FF) == 0)
					continue;

				while (r > 0) {
					int odx = dx;
					dx = dest.size() - dy - 1;
					dy = odx;
					r--;

					int re = 256 - (nc >> 16) & 0x00FF;
					int gr = (nc >> 24) & 0x00FF;

					nc &= 0x0000FFFF;
					nc |= gr << 16;
					nc |= re << 25;

				}
				dest.normalSet(dest.x1() + dx, dest.y1() + dy, nc);
			}

		}

	}
	
	void pasteRotated(ComposerDests.Dest dest, int rotation) {

		assert rotation >= 0 && rotation < 4;

		for (int y = 0; y < dest.size(); y++) {
			for (int x = 0; x < dest.size(); x++) {
				int dx = x;
				int dy = y;
				int r = rotation;

				int c = buffer[y][x];
				if ((c & 0x000000FF) == 0)
					continue;

				int nc = buffern[y][x];

				while (r > 0) {
					int odx = dx;
					dx = dest.size() - dy - 1;
					dy = odx;
					r--;

					int re = 256 - (nc >> 16) & 0x00FF;
					int gr = (nc >> 24) & 0x00FF;

					nc &= 0x0000FFFF;
					nc |= gr << 16;
					nc |= re << 24;

				}
				
				dest.diffuseSet(dest.x1() + dx, dest.y1() + dy, c);
				if ((nc & 0xFF000000) != 0)
					dest.normalSet(dest.x1() + dx, dest.y1() + dy, nc);
			}

		}

	}
	
	public SnakeImage getSource() {
		return TexSource;
	}

	int sampleSource(int x1, int y1) {
		return TexSource.rgb.get(x1, y1);
	}
	
	void dispose() {
		if (TexSource != null) {
			TexSource.dispose();
			TexSource = null;
		}
	}
	

}
