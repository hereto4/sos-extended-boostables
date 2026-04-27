package util.spritecomposer;

import init.constant.C;
import snake2d.util.datatypes.BODY_HOLDER;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.datatypes.Rec;
import util.spritecomposer.ComposerDests.DestChunk;
import util.spritecomposer.ComposerSources.Source;

public final class ComposerTexturer implements BODY_HOLDER{
	
	private final S source = new S();
	private final ComposerUtil c;
	private int destX,destY;
	public Rec body = new Rec();
	
	ComposerTexturer(ComposerUtil c){
		this.destX= 0;
		this.destY = 0;
		this.c = c;
	}
	
	public SpriteData paste(int sourceX1, int sourceY1, int tilesX, int tilesY) {
		
		body.setWidth(tilesX*C.T_PIXELS + 2*6).setHeight(tilesY*C.T_PIXELS+2*6);
		body.moveX1Y1(sourceX1, sourceY1);
		
		int sx = sourceX1+6;
		int sy = sourceY1+6;
		
		//C
		copy(sx, sy, 0, tilesX, 0, tilesY);
		past(1, tilesX+1, 1, tilesY+1);
		
		//up
		copy(sx, sy, 0, tilesX, tilesY-1, tilesY);
		past(1, tilesX+1, 0, 1);
		
//		//down
		copy(sx, sy, 0, tilesX, 0, 1);
		past(1, tilesX+1, tilesY+1, tilesY+2);
//		
		//left
		copy(sx, sy, tilesX-1, tilesX, 0, tilesY);
		past(0, 1, 1, tilesY+1);
		
		//left upper
		copy(sx, sy, tilesX-1, tilesX, tilesY-1, tilesY);
		past(0, 1, 0, 1);
		
//		
//		//right
		copy(sx, sy, 0, 1, 0, tilesY);
		past(tilesX+1, tilesX+2, 1,tilesY+1);
		
		//up Right
		copy(sx, sy, 0, 1, tilesY-1, tilesY);
		past(tilesX+1, tilesX+2, 0, 1);
		
		//down Right
		copy(sx, sy, 0, 1, 0, 1);
		past(tilesX+1, tilesX+2, tilesY+1,tilesY+2);
		
		
		//down Left
		copy(sx, sy, tilesX-1, tilesX, 0, 1);
		past(0, 1, tilesY+1,tilesY+2);
		
		SpriteData s =  SpriteData.save(destX, destY, destX+C.T_PIXELS*(tilesX+2), destY+C.T_PIXELS*(tilesY+2), 24);
		destX += (tilesX+2)*C.T_PIXELS;
		if (destX + (tilesX+2)*C.T_PIXELS >= Resources.dests.chunk.destWidth()) {
			destX = 0;
			destY += 160;
		}
		return s;
	}
	
	private void copy(int sx, int sy, int tx1, int tx2, int ty1, int ty2) {
		source.x1 = sx + tx1*C.T_PIXELS;
		source.y1 = sy + ty1*C.T_PIXELS;
		source.width = (tx2-tx1)*C.T_PIXELS;
		source.height = (ty2-ty1)*C.T_PIXELS;
		c.copy(source);
	}
	
	private void past(int tx1, int tx2, int ty1, int ty2) {
		DestChunk d = Resources.dests.chunk;
		d.rec.moveX1Y1(destX + tx1*C.T_PIXELS, destY + ty1*C.T_PIXELS);
		d.rec.setDim((tx2-tx1)*C.T_PIXELS, (ty2-ty1)*C.T_PIXELS);
		c.paste(d);
	}
	
	private final class S extends Source {
		
		private int x1,y1,width,height;
		
		@Override
		public RECTANGLE body() {
			return null;
		}
		
		@Override
		int y1() {
			return y1;
		}
		
		@Override
		int x1() {
			return x1;
		}
		
		@Override
		int width() {
			return width;
		}
		
		@Override
		int height() {
			return height;
		}
	};

	@Override
	public RECTANGLE body() {
		return body;
	};

	
}
