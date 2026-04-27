package util.gui.misc;

import init.constant.C;
import init.sprite.SPRITES;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.datatypes.Rec;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.text.Font;
import snake2d.util.sprite.text.Text;
import util.colors.GCOLOR;
import util.info.INFO;

/**
 * A clickable button
 * @author mail__000
 *
 */
public abstract class GButt extends CLICKABLE.ClickableAbs{
	
	protected SPRITE label;
	
	protected GButt(SPRITE r){
		this.label = r;
	}

	public GButt replaceLabel(SPRITE label, DIR d){
		//d.reposition(body, label.getWidth(), label.getHeight());
		this.label = label;
		return this;
	}
	
	public GButt hoverSet(INFO info) {
		hoverTitleSet(info.name);
		hoverInfoSet(info.desc);
		return this;
	}
	
	public static class Base extends GButt{
		
		protected final LIST<SPRITE> sprite;
		protected int labelXOff = 0;
		protected int labelYOff = 0;
		
		public Base(LIST<SPRITE> sprite, SPRITE label){
			super(label);
			
			int w = sprite.get(0).width();
			int h = sprite.get(0).height();
			
			body.setWidth(w > label.width() ? w : label.width());
			body.setHeight(h > label.height() ? h : label.height());
			labelXOff = (w - label.width())/2;
			labelYOff = (h - label.height())/2;
			this.sprite = sprite;
		}
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive,
				boolean isSelected, boolean isHovered) {
			renAction();
			int x = body.x1();
			int y = body.y1();
			
			if (isSelected && isHovered){
				sprite.get(3).render(r, x, y);
				COLOR.WHITE200.bind();
			}
			else if (isSelected){
				sprite.get(2).render(r, x, y);
				COLOR.WHITE150.bind();
			}else if (isHovered){
				sprite.get(1).render(r, x, y);
				COLOR.WHITE150.bind();
			}else if (isActive){
				sprite.get(0).render(r, x, y);
				COLOR.WHITE100.bind();
			}else{
				sprite.get(0).render(r, x, y);
				GCOLOR.T().INACTIVE.bind();
			}
			label.render(r, x + labelXOff, y + labelYOff);
			COLOR.unbind();
			
		}
	}
	

	
	public static class Panel extends GButt{

		
		public Panel(CharSequence label){
			this(UI.FONT().M.getText(label));
		}
		
		public Panel(SPRITE label){
			super(label);
			body.setDim(label.width()+6, label.height()+6);
				
			
		}
		
		public Panel(SPRITE label, CharSequence hovInfo){
			this(label);
			
			hoverInfoSet(hovInfo);
		}


		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
			renAction();
			
			if (isSelected ||isHovered) {
				
				if (isSelected) {
					OPACITY.O25To50.bind();
					COLOR.WHITE100.render(r, body);
				}else if (isHovered){
					OPACITY.O25.bind();
					COLOR.WHITE100.render(r, body);
				}
				OPACITY.unbind();
			}
			
			if (!isActive)
				GCOLOR.T().INACTIVE.bind();
			else if (isSelected && isHovered)
				GCOLOR.T().HOVER_SELECTED.bind();
			else if (isSelected)
				GCOLOR.T().SELECTED.bind();
			else if (isHovered)
				GCOLOR.T().HOVERED.bind();
			else
				COLOR.WHITE100.bind();
			label.renderC(r, body());
			COLOR.unbind();
			

			
			
		}
	}
	

	
	public static class Glow extends GButt{
		
		protected final SPRITE bg;
		private COLOR normal = COLOR.WHITE100;
		
		public Glow(SPRITE label){
			this(label, null);
			body.setHeight(body.height()+C.SG*6);
			body.incrW(4);
		}
		
		public Glow(CharSequence text){
			this((SPRITE)new Text(UI.FONT().S, text));
			body.setHeight(body.height()+C.SG*6);
		}
		
		public Glow(SPRITE label, SPRITE bg){
			super(label);
			body.setWidth(label.width());
			body.setHeight(label.height());
			if (bg != null) {
				if (bg.width() > body.width())
					body.setWidth(bg.width());
				if (bg.height() > body.height())
					body.setHeight(bg.height());
			}
			this.bg = bg;
		}
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive,
				boolean isSelected, boolean isHovered) {
			renAction();
			int x = body.x1();
			int y = body.y1();
			if (bg != null) {
				bg.render(r, x, y);
				x += (body.width()-label.width())/2;
			}
			
			if (!isActive)
				GCOLOR.T().INACTIVE.bind();
			else if (isSelected && isHovered)
				GCOLOR.T().HOVER_SELECTED.bind();
			else if (isSelected)
				GCOLOR.T().SELECTED.bind();
			else if (isHovered)
				GCOLOR.T().HOVERED.bind();
			else
				normal.bind();
			y += (body.height()-label.height())/2;
			label.render(r, x, y);
			COLOR.unbind();
			
		}
		
		public void color(COLOR color) {
			this.normal = color;
		}
	}
	
	public static abstract class BText extends Glow{
		
		private final Text text;
		private DIR d;
		
		public BText(Font f, int max, DIR d){
			super((SPRITE)new Text(f, max), null);
			this.text = (Text) label;
			this.d = d;
		}
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive,
				boolean isSelected, boolean isHovered) {
			renAction();
			
			update(text);
			text.adjustWidth();
			d.reposition(body, text.width(), text.height());
			
			super.render(r, ds, isActive, isSelected, isHovered);
			
		}
		
		protected abstract void update(Text text);
	}
	
	
	public static class Checkbox extends GButt.ButtPanel{
		
		private final static String on = "turn off";
		private final static String off = "turn on";
		
		public Checkbox(){
			this(16);
			icon(iconn());
		}
		
		public Checkbox(int dim){
			super((SPRITE)null);
			icon(iconn());
		}
		
		public Checkbox(CharSequence label){
			super(UI.FONT().H2.getText(label));
			icon(iconn());
		}
		
		public Checkbox(SPRITE label){
			super(label);
			icon(iconn());
		}
		
		private SPRITE iconn() {
			return new SPRITE.Imp(Icon.S) {
				
				@Override
				public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
					if (selectedIs()) {
						GCOLOR.UI().GOOD.hovered.bind();
						UI.icons().s.allRight.render(r, X1, X2, Y1, Y2);
					}else {
						GCOLOR.UI().BAD.hovered.bind();
						UI.icons().s.cancel.render(r, X1, X2, Y1, Y2);
					}
					
				}
			};
		}
		
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			if (hoverTitle != null)
				text.title(hoverTitle);
			if (hoverInfo != null)
				text.text(hoverInfo);
			else
				text.text(selectedIs() ? on : off);
		}
		
	}

	
	
	public static class BStat2 extends GButt {
		
		private final GStat stat;
		private COLOR color = COLOR.WHITE25;
		
		public BStat2(SPRITE icon, GStat stat) {
			super(icon);
			this.stat = stat;
			body.setWidth(icon.width() + stat.height()*4);
			body.setHeight(icon.height()+8);
		}
		
		public BStat2(CharSequence title, GStat stat) {
			super(new GText(UI.FONT().S, title).lablify());
			this.stat = stat;
			body.setWidth(label.width()+8 + stat.height()*4);
			body.setHeight(label.height()+stat.height()+8);
		}

		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
			
			renAction();
			GCOLOR.UI().border().render(r, body());
			GCOLOR.UI().bg().render(r, body(), -3);
			
			if (isSelected)
				COLOR.WHITE85.render(r, body, -2);
			else if (isHovered)
				COLOR.WHITE50.render(r, body, -2);
			
			if (isHovered) {
				OPACITY.O100.bind();
			}else if(isActive) {
				OPACITY.O50.bind();
			}else {
				OPACITY.O012.bind();
			}
			
			if (isHovered || isSelected) {
				OPACITY.O100.bind();
			}else if(isActive) {
				OPACITY.O50.bind();
			}else {
				OPACITY.O012.bind();
			}
			color.render(r, body(), -4);
			OPACITY.unbind();
			stat.adjust();
			label.renderCY(r, body().x1()+4, body.cY());
			if (label instanceof GText) {
				
				stat.renderCY(r, body().x1()+4+label.width()+2, body().cY());
			}else {
				stat.renderCY(r, body().x1()+4+label.width()+2, body().cY());
			}
			
			
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			stat.hoverInfoGet((GBox) text);
			super.hoverInfoGet(text);
		}
		
		public BStat2 bg(COLOR c) {
			this.color = c;
			return this;
		}
		
		public void bgClear() {
			color = COLOR.WHITE25;
		}
		
		public BStat2 setWidth(int width) {
			this.body.setWidth(width);
			return this;
		}
	}
	
	public static class ButtPanel extends GButt{
		
		private final int M = 4;
		private COLOR color = COLOR.WHITE35;
		private SPRITE icon;
		private DIR align = DIR.C;
		
		public ButtPanel(CharSequence label){
			this(UI.FONT().H2.getText(label));
			
		}
		
		public ButtPanel(CharSequence label, int width){
			this(UI.FONT().H2.getText(label));
			body.setWidth(width);
		}
		
		
		public ButtPanel(SPRITE label){
			super(label);
			if (label == null)
				body.setDim(16);
			else
				body.setDim(this.label.width() + M*4, this.label.height() + M*2);
		}
		
		public ButtPanel setDim(int width, int height) {
			this.body.setDim(width, height);
			return this;
		}
		
		public ButtPanel setDim(int width) {
			this.body.setWidth(width);
			return this;
		}
		
		public ButtPanel pad(int x, int y) {
			this.body.incrW(x*2);
			this.body.incrH(y*2);
			return this;
		}


		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
			renAction();
			COLOR border = GCOLOR.UI().border();
			border.render(r, body());
			GCOLOR.UI().bg().render(r, body(), -1);
			
			if (isSelected)
				COLOR.WHITE85.render(r, body, -2);
			else if (isHovered)
				COLOR.WHITE50.render(r, body, -2);
			
			GCOLOR.UI().bg().render(r, body(), -4);
			
			if (!isActive) {
				OPACITY.O0.bind();
			}else if (isHovered || isSelected) {
				OPACITY.O100.bind();
			}else {
				OPACITY.O35.bind();
				
			}
			color.render(r, body(), -4);
			OPACITY.unbind();
			

			
			if (icon != null) {
				if (label == null)
					icon.renderC(r, body);
				else {
					icon.renderC(r, body().x1()+icon.width()/2+M, body().cY());
				}
			}
			
			if (label instanceof Text) {
				GCOLOR.T().H1.bind();
				
				int ww = body.width();
				if (icon != null)
					ww -= 24 + icon.width();
				((Text)label).setMultipleLines(false);
				((Text)label).setMaxWidth(ww);
				
				
			}else {
				COLOR.WHITE100.bind();
			}
			
			if (label != null) {
				if (icon != null) {
					label.renderC(r, body().x1()+icon.width()+12+label.width()/2, body().cY());
				}else {
					int dx = align.x()*(body().width()-label.width()-8)/2;
					int dy = align.y()*(body().height()-label.height()-8)/2;
					label.renderC(r, body.cX()+dx, body().cY()+dy);
				}
				
			}
			
			COLOR.unbind();
			
			if (!isActive) {
				OPACITY.O50.bind();
				COLOR.BLACK.render(r, body,-2);
				OPACITY.unbind();
			}
			
			

			
			
		}
		
		public static void renderBG(SPRITE_RENDERER r, boolean isActive, boolean isSelected, boolean isHovered, RECTANGLE body) {
			GCOLOR.UI().bg().render(r, body, -1);
			
			if (isSelected)
				COLOR.WHITE85.render(r, body, -2);
			else if (isHovered)
				COLOR.WHITE50.render(r, body, -2);
			else
				COLOR.BLACK.render(r, body, -2);
			
			GCOLOR.UI().bg().render(r, body, -4);
			
			if (!isActive) {
				OPACITY.O0.bind();
			}else if (isHovered) {
				OPACITY.O100.bind();
			}else {
				OPACITY.O35.bind();
				
			}
			COLOR.WHITE35.render(r, body, -5);
			OPACITY.unbind();
		}
		
		public static void renderBGMini(SPRITE_RENDERER r, boolean isActive, boolean isSelected, boolean isHovered, RECTANGLE body) {

			if (isSelected)
				COLOR.WHITE85.render(r, body, 0);
			else if (isHovered)
				COLOR.WHITE50.render(r, body, 0);
			else
				COLOR.BLACK.render(r, body, 0);
			
			GCOLOR.UI().bg().render(r, body, -1);
			
			if (!isActive) {
				OPACITY.O0.bind();
			}else if (isHovered) {
				OPACITY.O100.bind();
			}else {
				OPACITY.O35.bind();
				
			}
			COLOR.WHITE35.render(r, body, -2);
			OPACITY.unbind();
		}
		
		public static void renderFrame(SPRITE_RENDERER r, boolean isActive, boolean isSelected, boolean isHovered, RECTANGLE body) {
			COLOR border = GCOLOR.UI().border();
			COLOR.BLACK.renderFrame(r, body.x1()+1, body.x2(), body.y1()+1, body.y2(), 0, 1);
			border.renderFrame(r, body.x1(), body.x2()-1, body.y1(), body.y2()-1, 0, 1);
		}
		
		public static void renderFrame(SPRITE_RENDERER r, RECTANGLE body) {
			COLOR border = GCOLOR.UI().border();
			//COLOR.BLACK.renderFrame(r, body.x1()+1, body.x2(), body.y1()+1, body.y2(), 0, 1);
			border.renderFrame(r, body.x1(), body.x2(), body.y1(), body.y2(), 0, 1);
		}
		
		public ButtPanel align(DIR d) {
			this.align = d;
			return this;
		}
		
		public ButtPanel bg(COLOR c) {
			this.color = c;
			return this;
		}
		
		public ButtPanel bgClear() {
			this.color = COLOR.WHITE35;
			return this;
		}
		
		public ButtPanel icon(SPRITE icon) {
			this.icon = icon;
			
			if (label == null) {
				if (body().width() < icon.width()+ M*2)
					body().setWidth(icon.width()+ M*2);
				if (body().height() < icon.height()+ M*2)
					body.setHeight(icon.height()+ M*2);
			}else {
				int w = icon.width()+16+label.width();
				if (body().width() < w)
					body().setWidth(w);
				if (body().height() < icon.height()+ M*2)
					body.setHeight(icon.height()+ M*2);
			}
			
			
			
			return this;
		}
		
		@Override
		public Rec body() {
			return body;
		}
	}
	
	public static class BSection extends GuiSection {
		
		private boolean selectedIs;
		private boolean link = false;
		public static final int M = 2;
		private CharSequence hov = null;
		
		
		public BSection(int width, int height) {
			this.body().setWidth(width).setHeight(height);
		}
		
		public BSection() {

		}
		
		public BSection setAsLink() {
			link = true;
			return this;
		}
		
		public void pad() {
			super.pad(4);
		}
		
		
		@Override
		public void render(SPRITE_RENDERER r, float ds) {
			renAction();
			if (visableIs()) {
				
				renderBG(r, body(), activeIs(), hoveredIs(), selectedIs());
				
				boolean hov = hoveredIs();
				super.render(r, ds);
				
				if (!activeIs()) {
					OPACITY.O25.bind();
					COLOR.BLACK.render(r, body(), -1);
					OPACITY.unbind();
				}else if (hov && link) {
					SPRITES.icons().m.arrow_right.render(r, body().x2()-init.sprite.UI.Icon.M-6, body().y1());
				}
				
				
			}
			
			
		}
		
		public static void renderBG(SPRITE_RENDERER r, RECTANGLE body, boolean isActive, boolean isHovered, boolean isSelected) {
			
			ButtPanel.renderFrame(r, body);
			ButtPanel.renderBG(r, isActive, isSelected, isHovered, body);
			
			
		}
		
		protected void renAction() {
			
		}
		
		@Override
		public GuiSection selectedSet(boolean yes) {
			selectedIs = yes;
			return super.selectedSet(yes);
		}
		
		public void selectOnlythis(boolean yes) {
			selectedIs = yes;
		}
		
		@Override
		public boolean selectedIs() {
			return selectedIs;
		}
		
		@Override
		public GuiSection hoverInfoSet(CharSequence s) {
			hov = s;
			return this;
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			if (hov != null)
				text.text(hov);
			super.hoverInfoGet(text);
		}
		
	}
	
	

	
}
