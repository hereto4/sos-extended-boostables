package init.sprite;

import java.nio.file.Path;

import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.SnakeImage;
import snake2d.util.sets.Bitmap2D;

public class BitmapSprite extends Bitmap2D {

	public static final int WIDTH = 12;
	public static final int HEIGHT = 12;
	public static final int AREA = WIDTH * HEIGHT;

	public BitmapSprite() {
		super(12, 12, false);
	}

	public void paint(Bitmap2D data) {
		for (int y = 0; y < HEIGHT; y++) {
			for (int x = 0; x < WIDTH; x++) {
				set(x, y, data.is(x, y));
			}
		}
	}

	public void scaled(SPRITE_RENDERER r, int sx, int sy, int scale, COLOR foreground, COLOR borderN, COLOR borderS) {

		OPACITY.O99.bind();
		int hs = scale / 2;

		for (int y = -1; y <= HEIGHT; y++) {
			for (int x = -1; x <= WIDTH; x++) {
				if (is(x, y)) {
					foreground.render(r, sx + x * scale, sx + x * scale + scale, sy + y * scale,
							sy + y * scale + scale);
				} else {
					for (DIR d : DIR.ORTHO) {
						if (is(x, y, d)) {
							int dx = hs * ((1 + d.x()) / 2);
							int dy = hs * ((1 + d.y()) / 2);
							COLOR c = d.x() < 0 || d.y() < 0 ? borderN : borderS;
							c.render(r, sx + x * scale + dx, sx + x * scale + dx + hs * (1 + Math.abs(d.y())),
									sy + y * scale + dy, sy + y * scale + dy + hs * (1 + Math.abs(d.x())));
						}
					}

					for (DIR d : DIR.NORTHO) {
						if (is(x, y, d)) {
							int dx = hs * ((1 + d.x()) / 2);
							int dy = hs * ((1 + d.y()) / 2);
							COLOR c = d.x() < 0 || d.y() < 0 ? borderN : borderS;
							c.render(r, sx + x * scale + dx, sx + x * scale + dx + hs, sy + y * scale + dy,
									sy + y * scale + dy + hs);
						}
					}
				}

			}
		}
		OPACITY.unbind();
	}
	
	public static Bitmap2D[] read(Path path) {
		SnakeImage im = new SnakeImage(path);
		int w = (im.width-2)/(WIDTH+2);
		int h = (im.height-2)/(HEIGHT+2);
		
		Bitmap2D[] datas = new Bitmap2D[w*h];
		for (int i = 0; i < datas.length; i++)
			datas[i] = new Bitmap2D(WIDTH, HEIGHT, false);
		
		int di = 0;
		for (int fy = 0; fy < h; fy ++) {
			for (int fx = 0; fx < w; fx ++) {
				int sx = 2 +fx*(WIDTH+2);
				int sy = 2 +fy*(HEIGHT+2);
				for (int y = 0; y < HEIGHT; y++) {
					for (int x = 0; x < WIDTH; x++) {
						int px = sx+x;
						int py = sy+y;
						if (((im.rgb.get(px, py) >> 8)&0x00FFFFFF) == 0) {
							datas[di].set(x,  y, true); 
						}
					
					}
				}
				di++;
			}
		}
		im.dispose();
		
		return datas;
	}

}
