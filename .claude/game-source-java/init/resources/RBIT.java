package init.resources;

import java.io.IOException;
import java.io.Serializable;

import snake2d.LOG;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;

public class RBIT implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final RBIT NONE = new RBIT();
	public static final RBIT ALL = new RBIT(-1l, -1l);
	
	protected long l1;
	protected long l2;
	
	public RBIT() {
		
	}
	
	RBIT(long l1, long l2) {
		this.l1 = l1;
		this.l2 = l2;
	}
	
	public boolean has(RESOURCE res) {
		return (l1 & res.bitL1) != 0 || (l2 & res.bitL2) != 0;
		
	}

	public boolean has(RBIT other) {
		if (other == null)
			return false;
		return (l1 & other.l1) != 0 || (l2 & other.l2) != 0;
	}
	
	public boolean hasAll(RBIT other) {
		if (other == null)
			return false;
		return (l1 & other.l1) == other.l1 && (l2 & other.l2) == other.l2;
	}
	
	@Override
	public String toString() {
		return Long.toBinaryString(l2) + " " + Long.toBinaryString(l1);
	}
	
	public boolean isClear() {
		return l1 == 0 && l2 == 0;
	}
	
	public void debug() {
		LOG.ln("rbits");
		for (RESOURCE res : RESOURCES.ALL()) {
			if (has(res))
				LOG.ln(res);
		}
	}
	
	public static class RBITImp extends RBIT implements SAVABLE{
		
		public static RBITImp tmp = new RBITImp();
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public RBITImp(){
			
		}
		
		public RBITImp and(RESOURCE res) {
			l1 &= res.bitL1;
			l2 &= res.bitL2;
			return this;
		}
		
		public RBITImp and(RBIT other) {
			l1 &= other.l1;
			l2 &= other.l2;
			return this;
		}
		
		public RBITImp or(RESOURCE res) {
			l1 |= res.bitL1;
			l2 |= res.bitL2;
			return this;
		}
		
		public RBITImp or(RBIT other) {
			l1 |= other.l1;
			l2 |= other.l2;
			return this;
		}
		
		public RBITImp clear(RESOURCE res) {
			l1 &= ~res.bitL1;
			l2 &= ~res.bitL2;
			return this;
		}
		
		public RBITImp clear(RBIT other) {
			l1 &= ~other.l1;
			l2 &= ~other.l2;
			return this;
		}
		
		public RBITImp setAll() {
			l1 = -1;
			l2 = -1;
			return this;
		}
		
		@Override
		public void clear() {
			l1 = 0;
			l2 = 0;
		}
		
		public RBITImp clearSet(RBIT other) {
			l1 = other.l1;
			l2 = other.l2;
			return this;
		}

		@Override
		public void save(FilePutter file) {
			file.l(l1);
			file.l(l2);
		}

		@Override
		public void load(FileGetter file) throws IOException {
			l1 = file.l();
			l2 = file.l();
		}

		public void toggle(RESOURCE resource) {
			l1 ^= resource.bitL1;
			l2 ^= resource.bitL2;
		}

		public void xor(RBIT bits) {
			l1 &= ~bits.l1;
			l2 &= ~bits.l2;
			
		}

		public void flip() {
			l1 = ~l1;
			l2 = ~l2;
			
		}

		public void set(RESOURCE res, boolean yes) {
			if (yes)
				or(res);
			else
				clear(res);
		}


	}
	

	
}
