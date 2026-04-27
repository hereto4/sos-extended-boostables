package init.sprite;

import java.io.IOException;

import game.GAME;
import init.sprite.UI.Icons;
import init.sprite.UI.UI;
import init.sprite.UI.UIConses;
import init.sprite.UI.UISpecials;
import init.sprite.game.GameSheets;
import snake2d.CORE;
import snake2d.util.color.ColorImp;
import snake2d.util.sprite.SPRITE;

public class SPRITES {

	private static SPRITES self;
	private final UIConses panelsOverlays;
	private final Textures textures;

	private final SPRITE loadScreen;
	private final UISpecials specials;
	private final GameSheets game;
	private final RLoadPrinter loader;
	
	public SPRITES(GAME gameg) throws IOException{
		

		
		self = this;
		CORE.checkIn();

		CORE.checkIn();
		
		panelsOverlays = new UIConses();
		loadScreen = UI.image().get("_LoadScreen", null, null);
		CORE.checkIn();
		specials = new UISpecials();
		CORE.checkIn();
		textures = new Textures();
		game = new GameSheets();
		loader = new RLoadPrinter();
		
	}
	
	public final static class COLOR_REMOVE{
		


		
		public static void bad2Good(ColorImp c, double d) {
			if (d < 0)
				d = 0;
			if (d > 1)
				d = 1;
			double r = (d > 0.5) ? (1.0-(d-0.5)*2) : 1;
			double g = (d < 0.5) ? d*2 : 1;
			c.set(30+(int)(70*r), 30+(int)(70*g), 30);
		}
	}
	
	
	public static Icons icons(){
		return UI.icons();
	}
	
	public static UIConses cons(){
		return self.panelsOverlays;
	}
	
	public static SPRITE loadScreen() {
		return self.loadScreen;
	}
	
	public static UISpecials specials() {
		return self.specials;
	}

	public static Textures textures() {
		return self.textures;
	}
	
	public static GameSheets GAME() {
		return self.game;
	}

	public static RLoadPrinter loader(){
		return self.loader;
	}
	

	
}
