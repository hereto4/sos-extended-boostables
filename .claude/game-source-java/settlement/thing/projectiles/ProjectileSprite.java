package settlement.thing.projectiles;

import java.io.IOException;

import game.GameDisposable;
import init.constant.C;
import init.paths.PATHS;
import init.sprite.SPRITES;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.Json;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.KeyMap;
import snake2d.util.sprite.TILE_SHEET;
import util.rendering.ShadowBatch;
import util.spritecomposer.ComposerDests;
import util.spritecomposer.ComposerSources;
import util.spritecomposer.ComposerThings.ITileSheet;
import util.spritecomposer.ComposerUtil;

public abstract class ProjectileSprite {

	private static final KeyMap<TILE_SHEET> map = new KeyMap<TILE_SHEET>();
	
	static {
		new GameDisposable() {
			
			@Override
			protected void dispose() {
				map.clear();
			}
		};
	}
	
	public static final ProjectileSprite DUMMY = new ProjectileSprite() {
		
		@Override
		public void renderProj(Projectile p, double ref, Renderer r, ShadowBatch s, double x, double y, int h, int ran,
				double dx, double dy, double dz, float ds, int zoomout) {
			if (zoomout < 2) {
				double l = Math.sqrt(dx*dx+dy*dy+dz*dz*4);
				dx /= l;
				dy /= l;
				dx*= C.SCALE;
				dy*= C.SCALE;
				for (int k = 0; k < 8; k++) {
					r.renderParticle((int)x, (int)(y));
					x += dx;
					y += dy;
				}
			}
			
			s.setDistance2Ground(h/4);
			SPRITES.icons().s.dot.renderC(s, (int)x, (int)y);
		}
	};

	public static ProjectileSprite get(Json json) throws IOException {
		COLOR col = new ColorImp(json, "COLOR");
		if (json.has("SPRITE_FILE"))
			return get(col, json.value("SPRITE_FILE"));
		else {
			COLOR[] cols = cols(col);
			return new ProjectileSprite() {
				
				@Override
				public void renderProj(Projectile p, double ref, Renderer r, ShadowBatch s, double x, double y, int h, int ran,
						double dx, double dy, double dz, float ds, int zoomout) {
					if (zoomout < 2) {
						cols[ran&0b0111111].bind();
						double l = Math.sqrt(dx*dx+dy*dy+dz*dz*4);
						dx /= l;
						dy /= l;
						dx*= C.SCALE;
						dy*= C.SCALE;
						for (int k = 0; k < 8; k++) {
							r.renderParticle((int)x, (int)(y));
							x += dx;
							y += dy;
						}
						COLOR.unbind();
					}
					
					s.setHeight(0).setDistance2Ground(h/4);
					SPRITES.icons().s.dot.renderC(s, (int)x, (int)y);
				}
			};
		}
			
	}
	
	public static ProjectileSprite get(COLOR col, String key) throws IOException {
		if (!map.containsKey(key)) {
			TILE_SHEET sheet = new ITileSheet(PATHS.SPRITE_SETTLEMENT().getFolder("projectile").get(key), 112,28) {
				
				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d)  {
					s.singles.init(0, 0, 2, 1, 1, 1, d.s16);
					for (int i = 0; i < 4; i++) {
						s.singles.setVar(0).pasteRotated(i, true);
						s.singles.setVar(1).pasteRotated(i, true);
					}
					
					return d.s16.saveGame();
				}
			}.get();
			map.put(key, sheet);
			
			
		}
		COLOR[] cols = cols(col);
		TILE_SHEET sheet = map.get(key);
		return new ProjectileSprite() {

				@Override
				public void renderProj(Projectile p, double ref, Renderer r, ShadowBatch s, double x, double y, int h,
						int ran, double dx, double dy, double dz, float ds, int zoomout) {
					int i = DIR.get(dx, dy).id();
					double sc = 1 + h*C.ITILE_SIZE*0.125;
					
					int w = (int) (sheet.size()*sc);
					int x1 = (int) (x-w/2);
					int y1 = (int) (y-w/2);
					int x2 = x1 + w;
					int y2 = y1 + w;
					
					
					cols[ran&0b0111111].bind();
					sheet.render(r, i, x1, x2, y1, y2);
					s.setHeight(0).setDistance2Ground(h/4);
					sheet.renderC(s, i, (int)x, (int)y);
					COLOR.unbind();
				}
				
				
			
			};
	}
	
	public abstract void renderProj(Projectile p, double ref, Renderer r, ShadowBatch s, double x, double y, int h, int ran, double dx, double dy, double dz,
			float ds, int zoomout);

	
	private ProjectileSprite() {

	}
	
	private static COLOR[] cols(COLOR col) {
		COLOR[] cols = new COLOR[64];
		for (int i = 0; i < cols.length; i++) {
			cols[i] = new ColorImp(
					CLAMP.i(col.red()+RND.rInt(5), 0, 128), 
					CLAMP.i(col.green()+RND.rInt(5), 0, 127), 
					CLAMP.i(col.blue()+RND.rInt(5), 0, 127))
					.shadeSelf(RND.rFloat1(0.5));
		}
		return cols;
	}
}
