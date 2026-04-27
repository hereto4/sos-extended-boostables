package menu;

import init.constant.C;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.light.AmbientLight;
import snake2d.util.sprite.SPRITE;
import util.text.D;

final class Intro{

	private double timer = 0;
	private int stage = 0;
	private AmbientLight moon = new AmbientLight();
	{
		D.gInit(this);
	}
	private HOVERABLE head = GUI.getBigText(D.g("greeting", "HAIL MIGHTY DESPOT!"));
	
	private SPRITE[] greeting = new SPRITE[] {
			GUI.getSmallText(D.g("0", "You are about to enter the world of Syx.")),
			GUI.getSmallText(D.g("1", "")),
			GUI.getSmallText(D.g("2", "This game is still in active development and some features might be changed, removed or added.")),
			GUI.getSmallText(D.g("3", "If you're a pirate, no one will come after you. But when times are good, consider a purchase!")),
			GUI.getSmallText(D.g("4", "Suggestions and feedback are welcome.")),
			GUI.getSmallText(D.g("5", "Beware, if you are using mods, you play at your PC's own risk.")),
			GUI.getSmallText(D.g("6", "")),
			GUI.getSmallText(D.g("7", "May the Astari guide your hand to swift victory...")),
	};
	private final ScMain main;
	private final Background bg;
	
	Intro(ScMain main, Background bg){
		moon.Set(AmbientLight.Strongmoonlight, 0);
		this.main = main;
		this.bg = bg;
	}
	
	boolean update(float ds) {
		
		timer += ds;
		
		switch(stage) {
		case 0 :
			moon.Set(AmbientLight.Strongmoonlight, timer/1.5);
			if (timer > 1.5) {
				moon.Set(AmbientLight.Strongmoonlight, 1.0);
				timer = 0;
				stage++;
			}
			break;
		case 1 :
			if (timer > 11) {
				timer = 0;
				stage++;
			}
			break;
		case 2 :
			moon.Set(AmbientLight.Strongmoonlight, 1.0-timer*2.0);
			if (timer > 0.5) {
				moon.Set(AmbientLight.Strongmoonlight, 0);
				timer = 0;
				stage++;
			}
			break;
		case 3 :
			if (timer > 0.5) {
				timer = 0;
				stage++;
			}
			break;
		case 4 :
			moon.Set(AmbientLight.Strongmoonlight, timer/2.0);
			if (timer > 1.0) {
				mask.setRed((int) ((timer-1)*128));
				mask.setGreen((int) ((timer-1)*128));
				mask.setBlue((int) ((timer-1)*128));
			}
			
			if (timer > 2.0) {
				moon.Set(AmbientLight.Strongmoonlight, 1.0);
				timer = 0;
				stage++;
			}
			break;
		case 5 :
			return false;
			
		}
		
		
		return true;
		
	}

	private final ColorImp mask = new ColorImp(COLOR.BLACK);
	
	protected void render(Renderer r, float ds) {
		
		if (stage >= 0 && stage < 3) {
			int y = GUI.inner.cY()-100;
			moon.register(C.DIM());
			head.body().moveY1(y);
			head.body().centerX(C.DIM());
			head.render(r, ds);
			
			y+= head.body().height()*2;
			for (SPRITE s : greeting) {
				int x1 = (C.WIDTH() - s.width())/2;
				s.render(r, x1, y);
				y+= s.height();
			}
			
		}
		
		
		
		if(stage > 3) {
			moon.register(C.DIM());
			main.render(r, ds);
		}
		
		if (stage >= 4) {
			r.newLayer(false, 0);
			mask.bind();
			bg.render(r, ds);
		}
		
		
	}

}
