package snake2d;

import java.nio.IntBuffer;
import java.util.Arrays;

final class VboSorter {

	private final Chunk[] chunks = new Chunk[256];
	private Chunk[] firstChunk = new Chunk[128];
	private Chunk[] currentChunk = new Chunk[128];
	private final Counts counts = new Counts();
	
	private final int[] data;
	private int chunkI = 0;

	VboSorter(int size){
		
		int MAX = size/chunks.length;
		
		for (int i = 0; i < chunks.length; i++) {
			chunks[i] = new Chunk(i*MAX, i*MAX+MAX);
		}
		data = new int[size];
		
	}
	
	public final void add(int layer, int v) {
		
		if (chunkI >= chunks.length) {
			return;
		}
		
		Chunk c = currentChunk[layer];
		if (c == null) {
			c = chunks[chunkI];
			c.nextChunk = null;
			c.count = c.start;
			chunkI ++;
			firstChunk[layer] = c;
			currentChunk[layer] = c;
			
		}else if (c.count >= c.max) {
			Chunk prev = c;
			c = chunks[chunkI];
			c.nextChunk = null;
			c.count = c.start;
			chunkI ++;
			prev.nextChunk = c;
			currentChunk[layer] = c;
		}

		data[c.count] = v;
		c.count++;

	}
	
	public void clear() {
		Arrays.fill(currentChunk, null);
		Arrays.fill(firstChunk, null);
		chunkI = 0;
	}
	
	public Counts fill(IntBuffer buff) {
		
		buff.position(0);
		
		for (int i = 0; i < firstChunk.length; i++) {
			Chunk c = firstChunk[i];
			if (c != null) {
				counts.from[i] = buff.position();
				while(c != null) {
					buff.put(data, c.start, c.count-c.start);
					c = c.nextChunk;
				}
				counts.to[i] = buff.position();
				
			}else {
				counts.from[i] = 0;
				counts.to[i] = 0;
			}
		}
		
		
		clear();
		return counts;
	}
	
	private static final class Chunk {
		
		final int start;
		final int max;
		int count;
		Chunk nextChunk;
		
		Chunk(int start, int max){
			this.start = start;
			this.max = max;
		}
		
	}
	
	public static class Counts {
		
		public final int from[] = new int[128];
		public final int to[] = new int[128];
		
	}

}
