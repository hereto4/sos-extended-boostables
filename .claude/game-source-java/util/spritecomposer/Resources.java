package util.spritecomposer;

import java.io.IOException;

import init.paths.PATH;
import init.paths.PATHS;
import snake2d.CORE;
import snake2d.Errors;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SnakeImage;

final class Resources {

	static FileGetter g;
	static FilePutter p;
	static ComposerDests dests;
	static ComposerUtil c;
	static ComposerSources sources;
	static ComposerTexturer immi;
	static ComposerFonter fonter;
	static boolean needsBigger = false;
	
	private static final int CHECK = 669966991;
	
	static Result read(String prefix, int width) throws IOException {
		
		dispose();
		
		PATH d = PATHS.CACHE_DATA();
		PATH t = PATHS.CACHE_TEXTURE();
		if (d.exists(prefix + "TextureData") && t.exists(prefix + "Diffuse") && t.exists(prefix + "Normal")) {
			FileGetter gg = new FileGetter(d.get(prefix + "TextureData"));
			int check = gg.lastInt();
			
			if (check != CHECK) {
				d.delete(prefix + "TextureData");
				return null;
			}else {
				SnakeImage diffuse;
				SnakeImage normal;
				try {
					diffuse = new SnakeImage(t.get(prefix + "Diffuse"));
					normal =  new SnakeImage(t.get(prefix + "Normal"));
				}catch(Errors.DataError e) {
					e.printStackTrace();
					t.delete(prefix + "Diffuse");
					t.delete(prefix + "Normal");
					return null;
				}
				
				g = gg;
				c = new ComposerUtil();
				
				if (diffuse.width != normal.width || diffuse.height != normal.height || diffuse.width != width) {
					d.delete(prefix + "TextureData");
					diffuse.dispose();
					normal.dispose();
					return null;
				}
				new Optimizer(g, diffuse);
				Result res = new Result();
				res.diffuse = diffuse;
				res.normal = normal;
				return res;
			}
		}
		
		return null;
		
	}
	
	public static void delete(String prefix) {
		if (PATHS.CACHE_DATA().exists(prefix + "TextureData")) {
			PATHS.CACHE_DATA().delete(prefix + "TextureData");
		}
	}
	
	static boolean init(String prefix, int size) throws IOException {
		
		dispose();
		
		PATH d = PATHS.CACHE_DATA();
		p = new FilePutter(d.create(prefix + "TextureData"), (1<<18));
		c = new ComposerUtil();
		dests = new ComposerDests(size);
		
		sources = new ComposerSources();
		immi = new ComposerTexturer(c);
		fonter = new ComposerFonter(c);
		
		return false;
		
	}
	
	
	static void save(String prefix, int extraHeight) {
		CORE.checkIn();
		PATH t = PATHS.CACHE_TEXTURE();
		dests.save(t.create(prefix + "Diffuse"), t.create(prefix + "Normal"), p, extraHeight);
		p.i(669966991);
		p.save();
		dispose();
	}

	public static void dispose() {
		g = null;
		if (p != null) {
			p = null;
		}
		if (dests != null) {
			dests.dispose();
			dests = null;
		}
		sources = null;
		immi = null;
		fonter = null;
		if (c != null) {
			c.dispose();
		}
		c = null;
	}
	
	
}
