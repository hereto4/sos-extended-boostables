package util.spritecomposer;

import java.io.IOException;

import game.GAME;
import snake2d.CORE;
import snake2d.TextureHolder;

public abstract class Initer {
	
	public Initer() {
		
	}
	
	public abstract void createAssets() throws IOException;
	
	public TextureHolder get(String prefix, int WIDTH, int extraHeight) {
		

		try {
			TextureHolder t = read(prefix, WIDTH);
			if (t != null)
				return t;
		} catch (IOException e) {

		}
		Resources.delete(prefix);
		
		try {
			tryCreate(prefix, WIDTH, extraHeight);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		try {
			TextureHolder t = read(prefix, WIDTH);
			if (t == null)
				throw new RuntimeException("saved texture cache could not be loaded. Ensure the local file folder has access");
			return t;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
	}
	
	private void tryCreate(String prefix, int WIDTH, int extraHeight) throws IOException {
		GAME.Notify("creating new texture atlases " + prefix);
		Resources.init(prefix, WIDTH);
		createAssets();
		Resources.save(prefix, extraHeight);
	}
	
	private TextureHolder read(String prefix, int WIDTH) throws IOException {
		Resources.dispose();
		Result res = Resources.read(prefix, WIDTH);
		if (res == null)
			return null;
		try {
			createAssets();
		}catch(IOException e) {
			CORE.disposeClient();
			res.diffuse.dispose();
			res.normal.dispose();
			throw e;
		}
		return new TextureHolder(res.diffuse, res.normal, 0, Optimizer.get(16).startY, 16, 16);
	}
	
	

	
}
