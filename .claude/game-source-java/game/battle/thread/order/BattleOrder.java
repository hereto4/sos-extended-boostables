package game.battle.thread.order;

import java.io.IOException;

import game.battle.formation.DivFormationImp;
import game.battle.util.Copyable;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;

public class BattleOrder {

	public final Locked<BattleOrderPath> path = new Locked<BattleOrderPath>(new BattleOrderPath());
	public final Locked<DivFormationImp> dest = new Locked<DivFormationImp>(new DivFormationImp());
	public final Locked<BattleOrderTask> task = new Locked<BattleOrderTask>(new BattleOrderTask());
	
	BattleOrder() {
		
	}
	
	void save(FilePutter file) {
		path.save(file);
		dest.save(file);
		task.save(file);
	}

	void load(FileGetter file) throws IOException {
		path.load(file);
		dest.load(file);
		task.load(file);
		
	}
	
	void clear() {
		path.clear();
		dest.clear();
		task.clear();
		
	}
	
	public static class Locked<T extends Copyable<T>> implements SAVABLE{

		private volatile boolean hasNew;
		private volatile boolean lock;
		private volatile int setI = 0;
		private final T t;
		
		Locked(T t){
			this.t = t;
		}
		
		private synchronized void lock() {
			while(lock)
				;
			lock = true;
		}
		
		public void get(T to) {
			lock();
			to.copy(t);
			lock = false;
		}
		
		public void set(T from) {
			lock();
			t.copy(from);
			setI++;
			lock = false;
			hasNew = true;
		}
		
		public boolean consumeNew(T copyTo) {
			if (hasNew) {
				get(copyTo);
				hasNew = false;
				return true;
			}
			return false;
		}
		
		public int setI() {
			return setI;
		}
		
		public boolean isNew(short i) {
			return (short)(setI & 0x0FFFF) != i;
		}
		
		@Override
		public void save(FilePutter file) {
			t.save(file);
			file.i(setI);
		}
		
		@Override
		public void load(FileGetter file) throws IOException {
			t.load(file);
			setI = file.i();
			hasNew = false;
		}
		
		@Override
		public void clear() {
			t.clear();
			setI = 0;
			hasNew = false;
		}
		
		
	}



	
}
