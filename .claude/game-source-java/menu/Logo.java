package menu;

import init.constant.C;
import init.sprite.UI.UI;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.Pendulum;
import snake2d.util.light.AmbientLight;
import snake2d.util.light.PointLight;
import snake2d.util.rnd.RND;
import util.gui.misc.GText;

final class Logo{

	private double d = 0.25;
	private final int startY;
	private final int startX;
	private int letterI = 1;
	private double letterTimer = 0;
	private final int presX1;
	private final int presY1;
	private boolean presents = false;
	private double presentsTimer = 0;
	private final int flashY1;
	private boolean flashRetreat = false;
	private final static double letterMax = 0.06;
	private double wait = 0.0;
	private final AmbientLight light = new AmbientLight(1.2,1.2,1.2,45,45);
	private final Pendulum lightD = new Pendulum().setZero(0.3).setFactor(1 + RND.rFloat1(0.3));
	private final PointLight finish = new PointLight();
	private final GText pres = new GText(UI.FONT().H1, "presents").color(COLOR.WHITE65);
	
	private final Menu menu;
	
	Logo(Menu menu){
		
		this.menu = menu;
		int w = 0;
		for (int i = 0; i < menu.res.s().logoGlyps.length; i++) {
			w+= menu.res.s().logoGlyps[i].width();
		}
		
		int m = 10;
		
		startX = (C.WIDTH() - w)/2;		
		startY = (C.HEIGHT() - (menu.res.s().logoGlyps[0].height() + m + menu.res.s().logoPresents.height()))/2;
		
		flashY1 = startY - (menu.res.s().logoFlash.height() - menu.res.s().logoGlyps[0].height())/2;
		
		presX1 = (C.WIDTH()/2 - pres.width()/2);
		presY1 = startY + m + menu.res.s().logoGlyps[0].height();
		finish.setRadius(menu.res.s().logoGlyps[0].height());
		finish.setZ(40);
		finish.setRed(10);
		
	}
	
	private double finTimer = 0;
	
	boolean update(float ds) {
		
		
		if (d > 0) {
			d-= ds;
			if (d == 0)
				d = -1;
			return true;
		}
			
		
		if (d != 0) {
			menu.res.sound().logo.setGain(0.5);
			menu.res.sound().logo.play();
		}
		d= 0;
		
		if (letterI < menu.res.s().logoGlyps.length) {
			letterTimer += ds;
			if (letterTimer >= letterMax) {
				letterTimer = 0;
				if (flashRetreat) {
					letterI++;
				}
				flashRetreat = !flashRetreat;
			}
			
		}else if (wait > 0) {
			wait -= ds;
		}else if (finTimer < 0.5){
			finTimer += ds;
			
		}else {
			presents = true;
			presentsTimer += ds;
			finTimer += ds;
			if (presentsTimer > 3) {
				return false;
			}
		}
		

		
		return true;
	
	}

	private final ColorImp co = new ColorImp();
	

	
	protected void render(Renderer r, float ds) {
	
		if (d > 0)
			return;
		
		
		int x1 = startX;
		for (int i = 0; i < letterI; i++) {
			x1 += menu.res.s().logoGlyps[i].width();
		}
		
		light.r(1.2*RND.rFloat1(0.05));
		light.g(1.2*RND.rFloat1(0.05));
		light.b(1.2*RND.rFloat1(0.05));
		light.setDir(45 + RND.rFloat0(15));
		
		
		if (flashRetreat || letterI < menu.res.s().logoGlyps.length) {
			double op;
			if (flashRetreat)
				op =  (255-255.0*letterTimer/letterMax);
			else
				op = (255*letterTimer/letterMax);

			int q = (int) op/2;
			co.setRed(q).setGreen(q).setBlue(q);
			co.bind();
			
			lightD.update(ds);
			light.r(lightD.get() + 0.9);
			light.g(lightD.get() + 0.9);
			light.b(lightD.get() + 0.9);
			
			x1 = x1 - (menu.res.s().logoFlash.width()-menu.res.s().logoGlyps[letterI].width())/2;
			menu.res.s().logoFlash.render(r, x1, flashY1);
			COLOR.unbind();
		}
		light.register(C.DIM());
		x1 = startX;
		int dx = RND.rInt0(64)/64;
		int dy = RND.rInt0(64)/64;
		for (int i = 0; i < letterI; i++) {
			menu.res.s().logoColors[i].bind();
			menu.res.s().logoGlyps[i].render(r, x1+dx, startY+dy);
			x1 += menu.res.s().logoGlyps[i].width();
		}
		COLOR.unbind();
		
		
		
		if (presents) {
			pres.render(r, presX1+dx, presY1+dy);
			
			//RESOURCES.s().logoPresents.render(r, presX1, presY1);
		}
		
		if (finTimer > 0) {
			
			int xl = (int) (C.MIN_WIDTH*finTimer + (C.WIDTH()-C.MIN_WIDTH)/2);
			finish.set(xl, C.HEIGHT()/2);
			finish.register();
		}
		
		
	}

}
