package snake2d.util.file;

import java.io.IOException;

public interface SAVABLE {

	public void save(FilePutter file);
	public void load(FileGetter file) throws IOException;
	public void clear();
	
	public abstract static class SuperSavable implements SAVABLE{
		
		private final String key;
		
		SuperSavable(String key){
			this.key = key;
		}

		@Override
		public void save(FilePutter f) {
			f.chars(key);
			int pos = f.getPosition();
			f.i(0);
			psave(f);
			int le = f.getPosition()-pos-4;
			f.setAtPosition(pos, le);
			
		}

		@Override
		public void load(FileGetter file) throws IOException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void clear() {
			// TODO Auto-generated method stub
			
		}
		
		protected abstract void psave(FilePutter f);
		
	}
	
}
