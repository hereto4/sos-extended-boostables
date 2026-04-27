package snake2d.util.misc;
import snake2d.CORE;
import snake2d.CoreStats;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.light.AmbientLight;
import snake2d.util.sets.ArrayList;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.text.Font;
import snake2d.util.sprite.text.Text;

public class Debugger {

	public static abstract class Formatter {
		
		private Formatter() {
			
		}
		
		abstract char[] getFormat(double v);
		
		public final static Formatter PERCENTAGE = new Formatter() {

			private final char[] chars = new char[7];
			{chars[3] = ',';}
			{chars[6] = '%';}
			@Override
			char[] getFormat(double v) {
				
				int d = (int) (v*100);
				
				chars[5] = (char) ('0' + d%10);
				d /= 10;
				chars[4] = (char) ('0' + d%10);
				d /= 10;
				chars[2] = (char) ('0' + d%10);
				d /= 10;
				chars[1] = (char) ('0' + d%10);
				d /= 10;
				chars[0] = (char) ('0' + d%10);
				
				return chars;
			}
			
		};
		
		public static final Formatter Amount = new Formatter() {

			private final char[] chars = new char[11];
			@Override
			char[] getFormat(double v) {
				
				int d = (int) (v);
				
				chars[10] = (char) ('0' + d%10); d /= 10;
				chars[9] = (char) ('0' + d%10); d /= 10;
				chars[8] = (char) ('0' + d%10); d /= 10;
				chars[7] = '.';
				chars[6] = (char) ('0' + d%10); d /= 10;
				chars[5] = (char) ('0' + d%10); d /= 10;
				chars[4] = (char) ('0' + d%10); d /= 10;
				chars[3] = '.';
				chars[2] = (char) ('0' + d%10); d /= 10;
				chars[1] = (char) ('0' + d%10); d /= 10;
				chars[0] = (char) ('0' + d%10); d /= 10;
				
				return chars;
			}
			
		};
		
	}
	
	private final Font font;
	private final int size;
	private boolean show = false;
	private final ArrayList<Value> values = new ArrayList<Value>(150);
	
	public Debugger(Font font){
		this.font = font;
		size = font.height();
		
		values.add(getCoreValue(CoreStats.FPS, 0, Formatter.Amount));
		values.add(getCoreValue(CoreStats.coreTotal, 0, Formatter.PERCENTAGE));
		values.add(getCoreValue(CoreStats.coreFlush, 25, Formatter.PERCENTAGE));
		values.add(getCoreValue(CoreStats.corePoll, 25, Formatter.PERCENTAGE));
		values.add(getCoreValue(CoreStats.coreSound, 25, Formatter.PERCENTAGE));
		values.add(getCoreValue(CoreStats.coreSleep, 25, Formatter.PERCENTAGE));
		values.add(getCoreValue(CoreStats.coreFinish, 25, Formatter.PERCENTAGE));
		values.add(getCoreValue(CoreStats.swapPercentage, 0, Formatter.PERCENTAGE));
		values.add(getCoreValue(CoreStats.totalPercentage, 0, Formatter.PERCENTAGE));
		values.add(getCoreValue(CoreStats.renderPercentage, 25, Formatter.PERCENTAGE));
		values.add(getCoreValue(CoreStats.updatePercentage, 25, Formatter.PERCENTAGE));

		values.add(getCoreValue(CoreStats.smallUpdates, 0, Formatter.Amount));
		values.add(getCoreValue(CoreStats.droppedTicks, 0, Formatter.Amount));
		values.add(getCoreValue(CoreStats.heap, 0, Formatter.Amount));
		values.add(getCoreValue(CoreStats.usedHeap, 0, Formatter.Amount));
		values.add(getCoreValue(CoreStats.heapGrowth, 0, Formatter.Amount));
		values.add(new Value("sprites", 0, Formatter.Amount) {
			@Override
			protected double getValue() {
				return CORE.renderer().getSpritesSprocessed();
			}
		});
		values.add(new Value("shadows", 0, Formatter.Amount) {
			@Override
			protected double getValue() {
				return CORE.renderer().getShadowsRendered();
			}
		});
		values.add(new Value("lights", 0, Formatter.Amount) {
			@Override
			protected double getValue() {
				return CORE.renderer().getLightsProcessed();
			}
		});
		values.add(new Value("particles", 0, Formatter.Amount) {
			@Override
			protected double getValue() {
				return CORE.renderer().getParticlesProcessed();
			}
		});
		
	}
	
	public void add(Value value){
		values.add(value);
	}

	public void flush(){
		
		if (!show)
			return;
		
		AmbientLight.full.register(0, CORE.getGraphics().nativeWidth, 0, CORE.getGraphics().nativeHeight);
		
		int y1 = size;
		int x1 = size;
		
		for (Value v : values){
			v.render(CORE.renderer(), x1, y1);
			y1+= size;
			if (y1 + size >= CORE.getGraphics().nativeHeight){
				y1 = size;
				x1 += 300;
			}
		}
		
		CORE.renderer().newLayer(false, 0);
	}
	
	public void toggle(){
		show ^= true;
	}
	
	public boolean isToggled() {
		return show;
	}
	
	public void show(){
		show = true;
	}
	
	public void hide(){
		show = false;
	}
	
	private Value getCoreValue(CoreStats.Value v, int off, Formatter f){
		
		return new Value(v.getLabel(), off, f) {
			@Override
			protected
			double getValue() {
				return v.ave;
			}
		};
		
	}
	
	public abstract class Value{
		
		private double last = -1;
		private final SPRITE label;
		private final Text value = new Text(font, 16);
		private final int off;
		private final Formatter format;
		
		public Value(String label, int off, Formatter f){
			this.label = font.getText(label);
			this.off = off;
			format = f;
		}
		
		Value(CoreStats.Value v, Formatter f){
			this(v.getLabel(), 0, f);
			setValue(v.ave);
		}
		
		void render(SPRITE_RENDERER r, int x, int y){
			double val = getValue();
			if (val != last){
				setValue(val);
			}
			COLOR.WHITE65.bind();
			label.render(r, x+off, y);
			COLOR.unbind();
			value.render(r, x+off+150, y);
		}
		
		private void setValue(double val){
			last = val;
			
			char[] d = format.getFormat(val);
			value.clear();
			for (int i = 0; i < d.length; i++) {
				value.add(d[i]);
			}
			value.adjustWidth();
		}
		
		protected abstract double getValue();
		
	}
	
}
