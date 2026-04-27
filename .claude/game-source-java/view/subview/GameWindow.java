package view.subview;

import static init.constant.C.TILE_SIZE;

import java.io.IOException;

import init.constant.C;
import init.settings.S;
import snake2d.MButt;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Coo;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.datatypes.Rec;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import snake2d.util.map.MAP_SETTER;
import view.keyboard.KEYS;
import view.main.VIEW;

public class GameWindow{

	private final RECTANGLE gameMax;
	private final Rec max = new Rec();
	private final Rec viewWindowDefault;
	private final Rec viewWindow;
	private final Pixels pixels;
	private final Pixels2 pixels2 = new Pixels2();
	private int maxZoomOut = 2;
	private boolean dragging = false;
	private int dragX = -1;
	private int dragY = -1;
	private int dragCX = -1;
	private int dragCY = -1;
	private boolean hovered;
	
	private final SubMouse pixel = new SubMouse() {
		
		private static final long serialVersionUID = 1L;
		private boolean hasMoved = true;
		private Coo relative = new Coo();
		
		@Override
		protected void update() {
			relative.set(VIEW.mouse().x() << zoomout, VIEW.mouse().y() << zoomout);
			int x = VIEW.mouse().x()- viewWindow.x1();
			int y = VIEW.mouse().y()- viewWindow.y1();
			
			x = pixels.x1() + (x << zoomout); 
			y = pixels.y1() + (y << zoomout);
			
			if (y >= max.height()) {
				y = max.height()-1;
			}
			if (x >= max.width())
				x = max.width()-1;
			if (x < 0)
				x = 0;
			if (y < 0)
				y = 0;
			hasMoved = set(x, y);
			
		}

		@Override
		public boolean hasMoved() {
			return hasMoved;
		}

		@Override
		public COORDINATE rel() {
			return relative;
		}
	};
	private final SubMouse tile = new SubMouse() {
		
		private static final long serialVersionUID = 1L;
		private boolean hasMoved = true;
		private Coo relative = new Coo();
		
		
		
		@Override
		protected void update() {
			
			int x = pixel.x() >> C.T_SCROLL; 
			int y = pixel.y() >> C.T_SCROLL;
			hasMoved = set(x, y);
			
			int relX = (pixel.x() & ~C.T_MASK) - pixels.x1() + (viewWindow.x1()<<zoomout);
			int relY = (pixel.y() & ~C.T_MASK) - pixels.y1() + (viewWindow.y1()<<zoomout);
			
			relative.set(relX,relY);

			
		}

		@Override
		public boolean hasMoved() {
			return hasMoved;
		}

		@Override
		public COORDINATE rel() {
			return relative;
		}
	};
	
	private final Rec tiles;
	private final static double maxSpeed = 32*TILE_SIZE;
	private final static double accD = maxSpeed*1.5; 
	private Coo speed = new Coo();
	private Coo acc = new Coo();
	private int zoomout;
	private boolean hasZoomedOutMore;

	/**
	 * 
	 * @param zoomout
	 * @param view
	 * @param gameMax
	 * @param maxbase
	 */
	public GameWindow(int zoomout, RECTANGLE view, RECTANGLE gameMax, int outMargin) {
		
		this.zoomout = zoomout;
		viewWindow = new Rec(view);
		viewWindowDefault = new Rec(view);
		pixels = new Pixels(gameMax);
		Rec m = new Rec(gameMax);
		m.incrW(outMargin*2);
		m.incrH(outMargin*2);
		m.incrX(-outMargin);
		m.incrY(-outMargin);
		this.gameMax = new Rec(m);
		this.max.set(m);
		
		tiles = new Rec();
		update(0);
	}
	
	public Rec viewWindow() {
		return viewWindow;
	}
	
	public GameWindow(RECTANGLE abs, RECTANGLE gameMax, int outMargin) {
		this(0, abs, gameMax, outMargin);
	}
	
	public GameWindow setzoomoutMax(int max) {
		this.maxZoomOut = max;
		setZoomout(zoomout);
		return this;
	}
	
	public int zoomoutmax() {
		return this.maxZoomOut;
	}
	
	public void crop(RECTANGLE rec) {
		int moveX = rec.x1() - this.viewWindow.x1();
		int moveY = rec.y1() - this.viewWindow.y1();
		pixels.incr(moveX<<zoomout, moveY<<zoomout);
		this.viewWindow.setWidth(rec.width());
		this.viewWindow.setHeight(rec.height());
		this.viewWindow.moveX1Y1(rec.x1(), rec.y1());
		update(0);
	}
	
	public void uncrop() {
		crop(viewWindowDefault);
	}
	
	public boolean hasZoomedOutMoreandConsumeThatMotherFZoom() {
		boolean ret = hasZoomedOutMore;
		hasZoomedOutMore = false;
		return ret;
	}
	
	public GameWindow setZoomout(int pow2) {
		int old = this.zoomout;
		this.zoomout = pow2;
		if (zoomout > maxZoomOut) {
			hasZoomedOutMore = true;
			zoomout = maxZoomOut;
		}
		if (zoomout < 0)
			zoomout = 0;
		
		int dx = (viewWindow.width()-2)<< (zoomout);
		int dy = (viewWindow.height()-2)<< (zoomout);
		
		max.set(-dx, gameMax.width()+dx, -dy, gameMax.height()+dy);
		
		
		
		int cx = pixels.cX();
		int cy = pixels.cY();
		pixels.update();
		pixels.moveC(cx, cy);
		if (old != this.zoomout) {
			double s = ((double)(this.zoomout+1)/(old+1));
			speed.scale(s, s);
		}
		
		update(0);
		return this;
	}
	
	public GameWindow zoomInc() {
		return setZoomout(zoomout+1);
	}
	
	public GameWindow zoomInc(int delta) {
		return setZoomout(zoomout+delta);
	}
	
	public int zoomout() {
		return zoomout;
	}
	
	public void setFromOther(GameWindow c) {
		int px = c.pixel().x();
		int py = c.pixel().y();
		pixel.update();
		
		
		int dx = (px-pixel.x());
		int dy = (py-pixel.y());
		
		pixels.incr(dx, dy);
		update(0);
	}

	public void hover(){
		hovered = true;
		if (!KEYS.MAIN().MOD.isPressed()) {
			double s = MButt.peekWheel();
			if (s != 0) {
				int z = zoomout();
				int d = s < 0 ? 1 : -1;
				zoomByMouse(zoomout+d);
				
				if (z != zoomout())
					MButt.clearWheelSpin();
			}
		}
		if (!dragging && MButt.WHEEL.consumeClick()) {
			dragX = VIEW.mouse().x();
			dragY = VIEW.mouse().y();
			dragCX = pixels.cX();
			dragCY = pixels.cY();
			dragging = true;
		}
	}
	
	public void zoomByMouse(int z) {
		int zold = zoomout();
		int px = pixel().x();
		int py = pixel().y();
		int dx = (px-pixels.x1())>>zoomout();
		int dy = (py-pixels.y1())>>zoomout();
	
		setZoomout(z);
		
		if (zold != zoomout()) {
			pixels.moveX1Y1(px-(dx<<zoomout()), py-(dy<<zoomout()));
			update(0);
		}

	}
	
	public boolean consumeHover() {
		boolean h = hovered;
		hovered = false;
		return h;
	}
	
	/**
	 * true if the window has moved
	 * @param ds
	 * @return
	 */
	public void update(float ds){
		
		if (VIEW.UI().manager.open())
			return;
		
		if (KEYS.MAIN().ZOOM_IN.consumeClick()) {
			setZoomout(zoomout-1);
		}
		if (KEYS.MAIN().ZOOM_OUT.consumeClick()) {
			setZoomout(zoomout+1);
		}
		
		double maxy = 1.0;
		
		double accD = GameWindow.accD*(1<<zoomout);
		double maxSpeed = GameWindow.maxSpeed*(1<<zoomout);

		
		
		acc.ySet(0);
		if (KEYS.MAIN().SCROLL_UP.isPressed()){
			if (speed.y() > 0)
				speed.ySet(0);
			acc.ySet(-accD);
		}else if (KEYS.MAIN().SCROLL_DOWN.isPressed()){
			if (speed.y() < 0)
				speed.ySet(0);
			acc.ySet(accD);
		}
		acc.xSet(0);
		if (KEYS.MAIN().SCROLL_LEFT.isPressed()){
			acc.xSet(-accD);
			if (speed.x() > 0)
				speed.xSet(0);
		}else if (KEYS.MAIN().SCROLL_RIGHT.isPressed()){
			acc.xSet(accD);
			if (speed.x() < 0)
				speed.xSet(0);
		}
		
		else if(S.get().scroll.get() == 1) {
			if (VIEW.mouse().x() < 4) {
				acc.xSet(-accD);
			}else if (VIEW.mouse().x() > C.DIM().x2()-5)
				acc.xSet(accD);
			
			if (VIEW.mouse().y() < 4) {
				acc.ySet(-accD);
			}else if (VIEW.mouse().y() > C.DIM().y2()-5)
				acc.ySet(accD);
		}
		
		double maxx = 1.0;
		
		dragging &= MButt.WHEEL.isDown();
		if(dragging){
			
			int dx = VIEW.mouse().x()-dragX;
			int dy = VIEW.mouse().y()-dragY;
			
			
			
			pinc((dragCX-(dx<<zoomout))-pixels.cX(), ( dragCY-(dy<<zoomout))-pixels.cY());
			//pixels.moveC(dragCX-(dx<<zoomout), dragCY-(dy<<zoomout));
			bondify();
			
			pixels.update();
			pixels2.update();
			pixel.update();
			tile.update();
			
			if (pixels.hasMoved()){
				int tX1 = (int) (pixels.x1()/TILE_SIZE);
				int tX2 = (int) (pixels.x2()/TILE_SIZE);
				int tY1 = (int) (pixels.y1()/TILE_SIZE);
				int tY2 = (int) (pixels.y2()/TILE_SIZE);

				tiles.set(tX1, tX2, tY1, tY2);
				
			}
			return;
		}
		
		if (acc.x() == 0)
			speed.decrease(ds*accD*4.0, 0);
		if (acc.y() == 0)
			speed.decrease(0, ds*accD*4.0);
		
		speed.increment(acc, ds);
		if (speed.x() > maxSpeed*maxx) {
			speed.decrease(Math.max(accD*2*ds, Math.abs(speed.x())*8*ds), 0);
			if (speed.x() < maxSpeed*maxx)
				speed.xSet(maxSpeed*maxx);
		}else if(speed.x() < -maxSpeed*maxx) {
			speed.decrease(Math.max(accD*2*ds, Math.abs(speed.x())*8*ds), 0);
			if (speed.x() > -maxSpeed*maxx)
				speed.xSet(-maxSpeed*maxx);
		}
		if (speed.y() > maxSpeed*maxy) {
			speed.decrease(0, Math.max(accD*2*ds, Math.abs(speed.y())*8*ds));
			if (speed.y() < maxSpeed*maxy)
				speed.ySet(maxSpeed*maxy);
		}
		else if(speed.y() < -maxSpeed*maxy) {
			speed.decrease(0, Math.max(accD*2*ds, Math.abs(speed.y())*8*ds));
			if (speed.y() > -maxSpeed*maxy)
				speed.ySet(-maxSpeed*maxy);
		}
		
		pinc(speed.x()*ds, speed.y()*ds);
		
		
		double d = 1+zoomout;
		
		if (KEYS.MAIN().MUP.isPressed()){
			pinc(0, -d);
		}else if (KEYS.MAIN().MDOWN.isPressed()){
			pinc(0, d);
		}
		if (KEYS.MAIN().MLEFT.isPressed()){
			pinc(-d, 0);
		}else if (KEYS.MAIN().MRIGHT.isPressed()){
			pinc(d, 0);
		}
		
		
		bondify();

		pixels.update();
		pixels2.update();
		pixel.update();
		tile.update();
		
		if (pixels.hasMoved()){
			int tX1 = (int) (pixels.x1()/TILE_SIZE);
			int tX2 = (int) (pixels.x2()/TILE_SIZE);
			int tY1 = (int) (pixels.y1()/TILE_SIZE);
			int tY2 = (int) (pixels.y2()/TILE_SIZE);

			tiles.set(tX1, tX2, tY1, tY2);
			
		}

		
	}
	
	private void pinc(double dx, double dy ) {
		if (dx < 0) {
			if (pixels.x1() > gameMax.x1()) {
				pixels.incrX(dx);
				if (pixels.x1() < gameMax.x1()) {
					pixels.moveX1(gameMax.x1());
					speed.xSet(0);
				}
			}else if (pixels.x2() > gameMax.x2()) {
				pixels.incrX(dx);
				if (pixels.x2() < gameMax.x2()) {
					pixels.moveX2(gameMax.x2());
					speed.xSet(0);
				}
			}
		}else {
			if (pixels.x2() < gameMax.x2()) {
				pixels.incrX(dx);
				if (pixels.x2() > gameMax.x2()) {
					pixels.moveX2(gameMax.x2());
					speed.xSet(0);
				}
			}else if (pixels.x1() < 0) {
				pixels.incrX(dx);
				if (pixels.x1() > 0) {
					pixels.moveX1(0);
					speed.xSet(0);
				}
			}
		}
		if (dy < 0) {
			if (pixels.y1() > gameMax.y1()) {
				pixels.incrY(dy);
				if (pixels.y1() < gameMax.y1()) {
					pixels.moveY1(gameMax.y1());
					speed.ySet(0);
				}
			}else if (pixels.y2() > gameMax.y2()) {
				pixels.incrY(dy);
				if (pixels.y2() < gameMax.y2()) {
					pixels.moveY2(gameMax.y2());
					speed.ySet(0);
				}
			}
		}else {
			if (pixels.y2() < gameMax.y2()) {
				pixels.incrY(dy);
				if (pixels.y2() > gameMax.y2()) {
					pixels.moveY2(gameMax.y2());
					speed.ySet(0);
				}
			}else if (pixels.y1() < 0) {
				pixels.incrY(dy);
				if (pixels.y1() > 0) {
					pixels.moveY1(0);
					speed.ySet(0);
				}
			}
		}
	}
	
	private void bondify() {
		if (pixels.x1() < max.x1() || (pixels.width()) > max.width()){
			speed.xSet(0);
			pixels.moveX1(max.x1());
		}else if (pixels.x2() > max.x2()){
			pixels.moveX2(max.x2());
			speed.xSet(0);
		}
		if (pixels.y1() < max.y1() || (pixels.height()) > max.height()){
			speed.ySet(0);
			pixels.moveY1(max.y1());
		}else if (pixels.y2() > max.y2()){
			
			pixels.moveY2(max.y2());
			speed.ySet(0);
		}
	}
	
	public void stop(){
		speed.set(0, 0);
		acc.set(0,0);
	}
	
	public void centerAt(int x1, int y1){
		stop();
		pixels.moveC(x1, y1);
		update(0);
	}
	
	public void inc(int x1, int y1){
		stop();
		pixels.incr(x1, y1);
		update(0);
	}
	
	public void centerAt(COORDINATE coo) {
		centerAt(coo.x(), coo.y());
	}
	
	public void centerAtTile(int tileX, int tileY){
		centerAt(tileX*C.TILE_SIZE + C.TILE_SIZE/2, 
				tileY*C.TILE_SIZE + C.TILE_SIZE/2);
	}
	
	public final MAP_SETTER centerer = new MAP_SETTER() {
		
		@Override
		public MAP_SETTER set(int tx, int ty) {
			stop();
			pixels.moveC(tx, ty);
			update(0);
			return this;
		}
		
		@Override
		public MAP_SETTER set(int tile) {
			throw new RuntimeException();
		}
	};
	
	public final MAP_SETTER centererTile = new MAP_SETTER() {
		
		@Override
		public MAP_SETTER set(int tx, int ty) {
			stop();
			pixels.moveC(tx*C.TILE_SIZE + C.TILE_SIZE/2, ty*C.TILE_SIZE + C.TILE_SIZE/2);
			update(0);
			return this;
		}
		
		@Override
		public MAP_SETTER set(int tile) {
			throw new RuntimeException();
		}
	};
	
	/**
	 * the mouse's position in game coordinates
	 * @return
	 */
	public SUB_MOUSE pixel(){
		return pixel;
	}
	
	public SUB_MOUSE tile(){
		return tile;
	}
	
	public RECTANGLE view(){
		return viewWindow;
	}
	
	public PIXELWINDOW pixels(){
		return pixels2;
	}
	
	public RECTANGLE tiles(){
		return tiles;
	}



	
	public static interface PIXELWINDOW extends RECTANGLE {
		
		public abstract COORDINATE relative();
		public default int relX() {
			return relative().x();
		}
		public default int relY() {
			return relative().y();
		}
		public int screenX(int x);
		public int screenY(int y);
		public boolean hasMoved();
		
	}
	
	public static interface SUB_MOUSE extends COORDINATE{
		
		public abstract boolean hasMoved();
		public COORDINATE rel();

	}
	
	public class Pixels extends Rec implements PIXELWINDOW {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -7484519024737753592L;
		private final Coo move = new Coo();
		private final Rec old = new Rec();
		private final Coo relative = new Coo();
		private boolean hasMoved = true;
		private int w,h;
		
		private Pixels(RECTANGLE max) {

			//setDim(width, height);
		}

		@Override
		public Rec moveX1(double X1) {
			move.xSet(X1);
			super.moveX1(X1);
			relative.set(this.x1(), this.y1());
			relative.increment(viewWindow.x1(), viewWindow.y1());
			
			return this;
		}
		
		@Override
		public Rec moveY1(double Y1) {
			move.ySet(Y1);
			super.moveY1(Y1);
			relative.set(this.x1(), this.y1());
			relative.increment(viewWindow.x1(), viewWindow.y1());
			return this;
		}
		
		@Override
		public COORDINATE relative() {
			return relative;
		}
		
		@Override
		public boolean hasMoved() {
			return hasMoved;
		}
		
		void update() {
			w = viewWindow.width() << zoomout;
			h = viewWindow.height() << zoomout;
			//Weird bug that caused wobbling when zooming out
//			if (zoomout != 0) {
//				super.moveX1((move.x() >> zoomout)<<zoomout);
//				super.moveY1((move.y() >> zoomout)<<zoomout);
//			}
			
			setDim(w, h);
			hasMoved = !old.isSameAs(this);
			old.set(this);
		}

		@Override
		public int screenX(int x) {
			return (x) - relX();
		}
		@Override
		public int screenY(int y) {
			return (y) - relY();
		}

	}
	
	private class Pixels2 extends Rec implements PIXELWINDOW {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -6275702531051471901L;
		private final Coo relative = new Coo();
		
		private Pixels2() {

			//setDim(width, height);
		}

		
		@Override
		public COORDINATE relative() {
			return relative;
		}
		
		@Override
		public boolean hasMoved() {
			return pixels.hasMoved;
		}
		
		void update() {
			set(pixels);
			
			if (zoomout != 0) {
				super.moveX1((x1() >> zoomout)<<zoomout);
				super.moveY1((y1() >> zoomout)<<zoomout);
			}
			
			relative.set(this.x1(), this.y1());
			relative.increment(-(viewWindow.x1()<<zoomout), -(viewWindow.y1()<<zoomout));
		}

		@Override
		public int screenX(int x) {
			return (x) - relX();
		}
		@Override
		public int screenY(int y) {
			return (y) - relY();
		}

	}
	
	public SAVABLE saver = new SAVABLE() {
		
		@Override
		public void save(FilePutter file) {
			file.i(pixels().cX());
			file.i(pixels().cY());
			file.i(zoomout);
		}
		
		@Override
		public void load(FileGetter file) throws IOException {
			centerAt(file.i(), file.i());
			setZoomout(file.i());
		}
		
		@Override
		public void clear() {
			// TODO Auto-generated method stub
			
		}
	};
	
	private abstract class SubMouse extends Coo implements SUB_MOUSE {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 4936922844334905532L;

		protected abstract void update();
		
	}

	public void copy(GameWindow window) {
		setZoomout(window.zoomout());
		int x1 = window.pixels().x1()-(window.viewWindow.x1()<<window.zoomout);
		int y1 = window.pixels().y1()-(window.viewWindow.y1()<<window.zoomout);
		
		x1 += viewWindow().x1()<<zoomout();
		y1 += viewWindow().y1()<<zoomout();
		
		centerAt(x1+pixels.width()/2, y1+pixels.height()/2);
		
	}
	
}
