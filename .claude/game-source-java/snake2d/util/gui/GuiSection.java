package snake2d.util.gui;

import java.util.Iterator;

import snake2d.SPRITE_RENDERER;
import snake2d.SoundEffect;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.datatypes.Rec;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.ArrayListResize;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.SPRITE;

public class GuiSection implements CLICKABLE{
	
	protected final ArrayListResize<RENDEROBJ> renderables = new ArrayListResize<RENDEROBJ>(2,512*4);
	
	private RECTANGLE previous;
	private HOVERABLE hovered;
	protected CLICKABLE clicked;
	
	private ACTION clickAction;
	
	private boolean visable = true;
	protected boolean active = true;
	
	private Bounds bounds = new Bounds();
	
	protected boolean hoveredIs = false;
	
	private CharSequence hoverInfo = null;
	private CharSequence hoverTitle = null;
	
	public GuiSection(){
		previous = bounds;
	}
	
	public GuiSection(float x, float y){
		bounds.moveX1Y1(x, y);
		previous = bounds;
	}
	
	public GuiSection(HOVERABLE r){
		bounds.set(r.body());
		add(r);
	}

	public GuiSection(CLICKABLE g){
		bounds.set(g.body());
		renderables.add(g);
		previous = g.body();
		hovered = g;
	}
	
	public GuiSection(SPRITE s, int x, int y){
		bounds.set(x,x,y,y);
		add(s, x, y);
	}
	
	public void clear(){
		renderables.clearSoft();
		hovered = null;
		clicked = null;
		hoveredIs = false;
		clickAction = null;
		bounds.setWidth(0).setHeight(0);
		bounds.moveX1Y1(0, 0);
		previous = bounds;
	}
	
	public LIST<RENDEROBJ> elements(){
		return renderables;
	}
	
	public void pad(int margin) {
		body().incrW(margin*2);
		body().incrH(margin*2);
		for (RENDEROBJ r : renderables)
			r.body().incrX(margin).incrY(margin);
	}
	
	public void pad(int mx, int my) {
		body().incrW(mx*2);
		body().incrH(my*2);
		for (RENDEROBJ r : renderables)
			r.body().incrX(mx).incrY(my);
	}
	
	public void padX(int left, int right) {
		
		body().incrX(left);
		
		body().incrW(right);
		for (RENDEROBJ r : renderables)
			r.body().incrX(right);
	}
	
	@Override
	public boolean hover(COORDINATE mCoo){
		
		if (!visable)
			return false;
		
		hoveredIs = mCoo.isWithinRec(bounds);
		
//		if (hovered != null && hovered.hover(mCoo)){
//			return true;
//		}
//		
		hovered = null;
		
		Iterator<RENDEROBJ> itr = renderables.iteratorReverse();
		
		while(itr.hasNext()){
			RENDEROBJ c = itr.next();
			if (c instanceof HOVERABLE && ((HOVERABLE)c).hover(mCoo)){
				hovered = (HOVERABLE)c;
				return true;
			}
			
		}

		return hoveredIs;
	}
	
	public boolean isHoveringAHoverElement() {
		return hovered != null && hovered instanceof HOVERABLE;
	}
	
	public RENDEROBJ getHovered() {
		return hovered;
	}
	
	
	
	@Override
	public boolean click(){
		
		if (!active || !visable)
			return false;
		
		if (hovered!= null && visable){
			if (hovered instanceof CLICKABLE) {
				((CLICKABLE) hovered).click();
				return true;
			}
		}
		if (hoveredIs) {
			
			clickA();
			if (clickAction != null) {
				clickAction.exe();
			}
			
		}
		
		return false;
	}
	
	protected void clickA() {
		
	}
	
	@Override
	public void render(SPRITE_RENDERER r, float ds){
		
		
		
		if (!visable) {
			hoveredIs = false;
			return;
		}
		
		for (RENDEROBJ ren : renderables)
			ren.render(r, ds);
		hoveredIs = false;
	}
	
	public GuiSection add(RENDEROBJ r){

		if (bounds.width() == 0 && renderables.isEmpty()){
			bounds.set(r.body());
		}else{
			bounds.unify(r.body());
		}
		if (r instanceof CLICKABLE){
			renderables.add((CLICKABLE) r);
			//hovered = (CLICKABLE) r;
		}else{
			renderables.add(r);
		}
		previous = r.body();
		return this;
	}
	
	public GuiSection moveLastToBack() {
		renderables.shiftRight();
		return this;
	}
	
	public GuiSection add(RENDEROBJ r, int x, int y){
		r.body().moveX1Y1(x, y);
		return add(r);
	}
	
	public GuiSection addGrid(RENDEROBJ r, int i, int rows, int mx, int my){
		
		int x = body().x1() + (i%rows)*(r.body().width()+mx);
		int y = body().y1() + (i/rows)*(r.body().height()+my);
		
		return add(r, x, y);
	}
	
	public GuiSection addGridD(RENDEROBJ r, int i, int cols, int width, int height, DIR align){
		
		int x = body().x1() + (i%cols)*(width);
		int y = body().y1() + (i/cols)*(height);
		
		if (x + width > body().width())
			body().setWidth(x+width);
		
		r.body().moveC(x + width/2, y + height/2);
		int dx = (width-r.body().width())/2;
		int dy = (height-r.body().height())/2;
		r.body().incr(dx*align.x(), dy*align.y());
		return add(r);
	}
	
	public GuiSection addC(RENDEROBJ r, int cx, int cy){
		r.body().moveC(cx, cy);
		return add(r);
	}
	
	
	public RENDEROBJ.Sprite addC(SPRITE r, int cx, int cy){
		return add(r, cx-r.width()/2, cy-r.height()/2);
	}
	
	public RENDEROBJ.Sprite add(SPRITE s, int x, int y){
		
		RENDEROBJ.Sprite r = new RENDEROBJ.Sprite(s);
		r.body().moveX1Y1(x, y);
		add(r);
		return r;
	}
	
	public void addRight(int margin, RENDEROBJ s){
		s.body().moveX1(previous.x2() + margin);
		s.body().moveY1(previous.y1());
		add(s);
	}
	
	public void addRight(int margin, SPRITE s){
		add(s, previous.x2() + margin, previous.y1());
	}
	
	public GuiSection addRightC(int margin, RENDEROBJ s){
		s.body().moveX1(previous.x2() + margin);
		s.body().centerY(previous);
		add(s);
		return this;
	}
	
	public void addRightC(int margin, SPRITE s){
		int dy = previous.height() - s.height();
		add(s, previous.x2() + margin, previous.y1() + dy/2);
	}
	
	public void addRightCAbs(int x, RENDEROBJ s){
		s.body().moveX1(previous.x1() + x);
		s.body().centerY(previous);
		add(s);
	}
	
	public void addCentredY(RENDEROBJ s, int x1){
		s.body().moveX1(x1);
		s.body().centerY(previous);
		add(s);
	}
	
	public void addCentredY(SPRITE s, int x1){
		add(s, x1, previous.cY()-s.height()/2);
	}
	
	public void addCentredX(RENDEROBJ s, int cx){
		s.body().moveCX(cx);
		s.body().centerY(previous);
		add(s);
	}
	
	public void addCentredX(SPRITE s, int cx){
		add(s, cx-s.width()/2, previous.cY()-s.height()/2);
	}
	
	public void addRightCAbs(int x, SPRITE s){
		int dy = previous.height() - s.height();
		add(s, previous.x1() +x, previous.y1() + dy/2);
	}
	
	public void addDown(int margin, RENDEROBJ s){
		s.body().moveY1(previous.y2() + margin);
		s.body().moveX1(previous.x1());
		add(s);
	}
	
	public void addDown(int margin, SPRITE s){
		add(s, previous.x1(), previous.y2() + margin);
	}
	
	public void addDownC(int margin, RENDEROBJ s){
		s.body().centerIn(previous);
		s.body().moveY1(previous.y2() + margin);
		add(s);
	}
	
	public void addDownC(int margin, SPRITE s){
		add(s, previous.x1() + (previous.width()-s.width())/2, previous.y2() + margin);
	}
	
	public void addOnTop(RENDEROBJ s){
		s.body().moveX1(previous.x1());
		s.body().moveY1(previous.y1());
		add(s);
	}
	
	public void addOnTop(SPRITE s){
		add(s, previous.x1(), previous.y1());
	}
	
	public void addOnTopC(RENDEROBJ s){
		s.body().centerIn(previous);
		add(s);
	}
	
	public void addOnTopC(SPRITE s){
		int dx = (previous.width() - s.width())/2;
		int dy = (previous.height() - s.height())/2;
		add(s, previous.x1() + dx, previous.y1() + dy);
	}
	
	public RECTANGLE getLast(){
		return previous;
	}
	
	public int getLastX1(){
		return previous.x1();
	}
	
	public int getLastX2(){
		return previous.x2();
	}
	
	public int getLastY1(){
		return previous.y1();
	}
	
	public int getLastY2(){
		return previous.y2();
	}
	
	@Override
	public GuiSection activeSet(boolean activate) {
		active = activate;
		for (RENDEROBJ c: renderables){
			if (c instanceof CLICKABLE)
				((CLICKABLE) c).activeSet(activate);
		}
		return this;
	}

	@Override
	public GuiSection selectedSet(boolean yes) {
		for (RENDEROBJ c: renderables){
			if (c instanceof CLICKABLE)
				((CLICKABLE) c).selectedSet(yes);
		}
		return this;
	}
	
	@Override
	public CLICKABLE selectTmp() {
		for (RENDEROBJ c: renderables){
			if (c instanceof CLICKABLE)
				((CLICKABLE) c).selectTmp();
		}
		return this;
	}

	@Override
	public boolean activeIs() {
		return active;
	}

	@Override
	public GuiSection visableSet(boolean yes) {
		visable = yes;
		return this;
	}

	@Override
	public boolean visableIs() {
		return visable;
	}
	
	@Override
	public GuiSection hoverSoundSet(SoundEffect sound) {
		for (RENDEROBJ c: renderables){
			if (c instanceof CLICKABLE)
				((CLICKABLE) c).hoverSoundSet(sound);;
		}
		return this;
	}

	@Override
	public GuiSection clickSoundSet(SoundEffect sound) {
		for (RENDEROBJ c: renderables){
			if (c instanceof CLICKABLE)
				((CLICKABLE) c).clickSoundSet(sound);;
		}
		return this;
	}

	@Override
	public boolean selectedIs() {
		for (RENDEROBJ c : renderables){
			if (c instanceof CLICKABLE)
				if (((CLICKABLE) c).selectedIs())
				return true;
		}
		
		return false;
	}
	

	@Override
	public GuiSection selectedToggle() {
		for (RENDEROBJ c : renderables){
			if (c instanceof CLICKABLE)
				((CLICKABLE) c).selectedToggle();
		}
		return this;
		
	}

	@Override
	public boolean hoveredIs() {
		return hoveredIs; //(hovered != null && hovered.hoveredIs());
	}

	@Override
	public void hoverInfoGet(GUI_BOX text) {
		if (hovered != null && hovered.hoveredIs()){
			hovered.hoverInfoGet(text);
		}
		
		if (hoveredIs() && text.emptyIs()) {
			hoverInfoSelf(text);
		}
	}
	
	protected void hoverInfoSelf(GUI_BOX box) {
		if (hoverInfo != null) {
			box.text(hoverInfo);
		}
		if (hoverTitle != null)
			box.title(hoverTitle);
	}
	
	private class Bounds extends Rec{
		
		private static final long serialVersionUID = 1L;

		@Override
		public Rec moveX1(double X1) {
			int dx = (int) (X1 - x);
			
			for (RENDEROBJ ren : renderables) {
				if (ren.body() == this)
					throw new RuntimeException();
				ren.body().incrX(dx);
			}
			x = X1;
			moveCallback();
			return this;
		}
		
		@Override
		public Rec moveY1(double Y1) {
			int dy = (int) (Y1 - y);
			
			for (RENDEROBJ ren : renderables)
				ren.body().incrY(dy);
			y = Y1;
			moveCallback();
			return this;
		}
		
	}
	
	protected void moveCallback() {
		
	}

	public void merge(GuiSection section){
		for (RENDEROBJ r : section.renderables){
			renderables.add(r);
		}
		if (body().width() == 0 && body().height() == 0)
			body().set(section);
		else
			body().unify(section.body());
		previous = section.body();
	}
	
	public void absorb(GuiSection section){
		for (RENDEROBJ r : section.renderables){
			add(r);
		}
		body().unify(section.body());
	}

	@Override
	public Rec body() {
		return bounds;
	}

	@Override
	public GuiSection hoverInfoSet(CharSequence s) {
		this.hoverInfo = s;
		return this;
	}

	@Override
	public CLICKABLE hoverTitleSet(CharSequence s) {
		this.hoverTitle = s;
		return this;
	}
	
	@Override
	public CLICKABLE clickActionSet(ACTION f) {
		this.clickAction = f;
		return this;
	}

	protected HOVERABLE hovered() {
		return hovered;
	}

	public GuiSection addRelBody(int m, DIR e, RENDEROBJ ren) {
		
		int cx = body().cX() + e.x()*((ren.body().width() +body().width())/2 + m);
		int cy = body().cY() + e.y()*((ren.body().height() +body().height())/2 + m);
		ren.body().moveC(cx, cy);
		add(ren);
		return this;
	}
	
	public GuiSection addRelBody(int m, DIR e, SPRITE ren) {
		return addRelBody(m, e, new RENDEROBJ.Sprite(ren));
	}




}
