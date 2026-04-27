package game.battle.thread.general.offence;

import java.io.IOException;

import game.battle.div.Div;
import game.battle.formation.DivFormation;
import game.battle.thread.general.StrategosUtil;
import init.constant.C;
import snake2d.util.datatypes.VectorImp;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;

class ContextLines implements SAVABLE{

	private int lineI;
	private Line[] all = new Line[0];
	private static final VectorImp vec = new VectorImp();
	
	ContextLines(){
		
		
		
	}
	

	
	
	public static class Line implements SAVABLE{
		
		int sx;
		int sy;
		int length;
		double dx;
		double dy;
		byte mark;
		int back;
		int blobID;
		
		private Line() {
			
		}
		
		public int cx() {
			return (int) (sx+dx*length/2);
		}
		public int cy() {
			return (int) (sy+dy*length/2);
		}

		public DivFormation deploy(StrategosUtil util, Div d) {
			
			int m = d.menNrOf();
			if (m == 0)
				return null;
			int w = (int) Math.sqrt(m);
			if (w == 0)
				return null;
			
			int x = sx;
			int y = sy;
			
			vec.set(dx, dy);
			vec.rotate90();
			x += back*vec.nX();
			y += back*vec.nY();
			
			DivFormation f = util.divDeployer.deploy(d, x, y, length, dx, dy);
			
			if (f != null) {
				back += d.settings().formation.size(d)*Math.ceil(m /= (length/d.settings().formation.size(d))) + C.TILE_SIZE;
				
			}
			return f;
			
			
		}

		@Override
		public void save(FilePutter file) {
			file.i(sx);
			file.i(sy);
			file.i(length);
			file.d(dx);
			file.d(dy);
			file.i(back);
			file.b(mark);
			file.i(blobID);
			
		}

		@Override
		public void load(FileGetter file) throws IOException {
			sx = file.i();
			sy = file.i();
			length = file.i();
			dx = file.d();
			dy = file.d();
			back = file.i();
			mark = file.b();
			blobID = file.i();
			
		}

		@Override
		public void clear() {
			sx = 0;
			sy = 0;
			length = 1;
			dx = 1;
			dy = 0;
			back = 0;
			mark = 0;
			blobID = 0;
			
		}

	}
	
	public Line get(int index) {
		if (index >= lineI)
			throw new RuntimeException();
		return all[index];
	}
	
	public void remove(int index) {
		Line l = all[index];
		all[index] = all[lineI-1];
		all[lineI-1] = l;
		lineI--;
	}
	
	public int lines() {
		return lineI;
	}
	
	public Line makeNew() {
		if (lineI >= all.length) {
			Line[] lines2 = new Line[all.length+256];
			for (int i = 0; i < all.length; i++) {
				lines2[i] = all[i];
			}
			for (int i = all.length; i < lines2.length; i++) {
				lines2[i] = new Line();
			}
			all = lines2;
		}
		Line l = all[lineI];
		l.clear();
		lineI ++;
		return l;
	}

	@Override
	public void save(FilePutter file) {
		file.i(all.length);
		file.i(lineI);
		
		for (int i = 0; i < lineI; i++) {
			Line l = all[i];
			l.save(file);
		}
		
	}

	@Override
	public void load(FileGetter file) throws IOException {
		all = new Line[file.i()];
		for (int i = 0; i < all.length; i++) {
			all[i] = new Line();
		}
		lineI = file.i();
		

		for (int i = 0; i < lineI; i++) {
			Line l = all[i];
			l.load(file);
		}
		
		
	}
	
	@Override
	public void clear() {
		lineI = 0;
	}
}
