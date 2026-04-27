package util.data;

import java.io.IOException;

import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import util.info.INFO;

public interface BOOLEAN {

	public boolean is();
	
	public interface BOOLEAN_MUTABLE extends BOOLEAN{
		
		public BOOLEAN_MUTABLE set(boolean b);
		
		public default BOOLEAN_MUTABLE toggle() {
			return set(!is());
		}
		
		public default BOOLEAN_MUTABLE setOn() {
			return set(true);
		}
		
		public default BOOLEAN_MUTABLE setOff() {
			return set(false);
		}
	}
	
	public static class BOOLEANImp implements BOOLEAN_MUTABLE, SAVABLE {

		public boolean b;
		public INFO info;
		
		public BOOLEANImp(){
	
		}
		
		public BOOLEANImp(CharSequence name, CharSequence desc){
			info = new INFO(name, desc);
		}
		
		public BOOLEANImp(boolean b){
			this.b = b;
		}
		
		@Override
		public boolean is() {
			return b;
		}

		@Override
		public BOOLEAN_MUTABLE set(boolean b) {
			this.b = b;
			return this;
		}

		@Override
		public void save(FilePutter file) {
			file.bool(b);
		}

		@Override
		public void load(FileGetter file) throws IOException {
			b = file.bool();
		}

		@Override
		public void clear() {
			b = false;
		}
		
	}
	
}
