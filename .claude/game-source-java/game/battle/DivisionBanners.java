package game.battle;

import java.io.IOException;

import init.paths.PATHS;
import init.sprite.BitmapSprite;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import snake2d.util.sets.Bitmap2D;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.TextureCoords;

public class DivisionBanners implements SAVABLE{

	private final DivisionBanner[] all;
	
	DivisionBanners(){
		
		Bitmap2D[] data = BitmapSprite.read(PATHS.SPRITE_UI().get("DivisionSymbols"));
		all = new DivisionBanner[data.length];
		COLOR[] cols = COLOR.generateUnique(40, data.length, true);
		for (int i = 0; i < data.length; i++) {
			DivisionBanner d = new DivisionBanner(new BitmapSprite());
			d.sprite.paint(data[i]);
			d.col.set(cols[i]);
			all[i] = d;
		}
		
	}
	
	public DivisionBanner get(int index) {
		index = index % all.length;
		if (index < 0)
			index += all.length;
		return all[index];
	}
	


	@Override
	public void save(FilePutter file) {
		file.i(all.length);
		for (DivisionBanner d : all) {
			d.sprite.save(file);
			d.col.save(file);
			d.bg.save(file);
		}
	}

	@Override
	public void load(FileGetter file) throws IOException {
		int am = file.i();
		for(int i = 0; i < am; i++) {
			get(i).sprite.load(file);
			get(i).col.load(file);
			get(i).bg.load(file);
		}
		
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}
	
	public class DivisionBanner implements SPRITE{
		
		public final BitmapSprite sprite;
		public final ColorImp col = new ColorImp();
		public final ColorImp bg = new ColorImp(20, 20, 20);
		private final int m = 2;

		public DivisionBanner(BitmapSprite sprite) {
			this.sprite = sprite;
		}

		@Override
		public int width() {
			return BitmapSprite.WIDTH*2+m*2;
		}
		
		@Override
		public int height() {
			return BitmapSprite.HEIGHT*2+m*2;
		}
		
		@Override
		public void renderTextured(TextureCoords texture, int X1, int X2, int Y1, int Y2) {
			
		}
		
		@Override
		public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
			
//			bg.render(r, X1, X2, Y1, Y2);
			
			int s = (X2-X1)/width();
			if (s < 1)
				s = 1;
			
//			ColorImp.TMP.set(bg).shadeSelf(0.75);
//			ColorImp.TMP.renderFrame(r, X1, X1+width()*s, Y1, Y1+height()*s, s, s);
//			ColorImp.TMP.shadeSelf(0.75);
//			ColorImp.TMP.renderFrame(r, X1, X1+width()*s, Y1, Y1+height()*s, 0, s);
//			
			
			renderSymbol(r, X1+m, Y1+m, s);
			
			
		}
		
		public void renderSymbol(SPRITE_RENDERER r, int X1, int Y1, int scale) {
			
			
			
			for (int y = -1; y <= BitmapSprite.WIDTH*2+2; y++) {
				for (int x = -1; x <= BitmapSprite.HEIGHT*2+2; x++) {
					int dx = (x-1)/2;
					int dy = (y-1)/2;
					
					if (sprite.is(dx, dy)) {
						COLOR c = col;
						for (DIR d: DIR.ALL) {
							int ddx = (x-1+d.x())/2;
							int ddy = (y-1+d.y())/2;
							if (!sprite.is(ddx, ddy)) {
								c = ColorImp.TMP.set(c).shadeSelf(0.6);
								break;
							}
						}
						c.render(r, X1+x*scale, X1+x*scale + scale, Y1+y*scale, Y1+y*scale+scale);
					}else {
						for (DIR d: DIR.ALL) {
							int ddx = (x-1+d.x())/2;
							int ddy = (y-1+d.y())/2;
							if (sprite.is(ddx, ddy)) {
								bg.render(r, X1+x*scale, X1+x*scale + scale, Y1+y*scale, Y1+y*scale+scale);
								break;
							}
						}
					}

				}
			}

			
			
		}
		

	}

	public int size() {
		return all.length;
	}
	



	
}
