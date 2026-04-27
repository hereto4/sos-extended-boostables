package util.data;

import java.io.IOException;

import init.sprite.UI.UI;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import snake2d.util.sprite.SPRITE;
import util.info.INFO;

public interface DOUBLE {

	public double getD();

	public interface DOUBLE_MUTABLE extends DOUBLE{
		
		public default DOUBLE_MUTABLE incD(double d) {
			setD(getD()+d);
			return this;
		}
		public DOUBLE_MUTABLE setD(double d);
	}
	
	public default INFO info() {
		return null;
	}
	
	public static abstract class DOUBLEI implements DOUBLE {
		
		private final INFO info;
		public final SPRITE icon;
		
		public DOUBLEI(CharSequence name, CharSequence desc){
			info = new INFO(name, desc);
			icon = UI.icons().s.cancel;
		}
		
		public DOUBLEI(CharSequence name, CharSequence desc, SPRITE icon){
			info = new INFO(name, desc);
			this.icon = icon;
		}
		
		@Override
		public INFO info() {
			return info;
		}
	}
	
	public static class DoubleImp implements DOUBLE_MUTABLE, SAVABLE {
		
		private double d;
		
		@Override
		public double getD() {
			return d;
		}

		@Override
		public void save(FilePutter file) {
			file.d(d);
		}

		@Override
		public void load(FileGetter file) throws IOException {
			d = file.d();
		}

		@Override
		public void clear() {
			d = 0;
		}

		@Override
		public DOUBLE_MUTABLE setD(double d) {
			this.d = d;
			return this;
		}
		
		
	}
	
	
	
}
