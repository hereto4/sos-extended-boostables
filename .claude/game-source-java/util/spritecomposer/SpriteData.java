package util.spritecomposer;

import java.io.IOException;

import snake2d.util.file.FileGetter;

public class SpriteData {

	public final int x1, y1, width, height;
	
	private SpriteData(int x1, int y1, int width, int height) {
		this.x1 = x1; this.y1 = y1; this.width = width; this.height = height;
	}
	
	static SpriteData save(int x1, int y1, int width, int height, int ts) {
		Resources.p.mark("sprite");
		Resources.p.i(x1);
		Resources.p.i(y1);
		Resources.p.i(width);
		Resources.p.i(height);
		Resources.p.i(ts);
		return new SpriteData(x1,y1,width,height);
	}
	
	static SpriteData read(FileGetter g) throws IOException {
		g.check("sprite");
		int x1 = g.i();
		int y1 = g.i();
		int width = g.i();
		int height = g.i();
		Optimizer.Tile t = Optimizer.get(g.i());
		return new SpriteData(x1, y1+t.startY, width, height);
	}
}
