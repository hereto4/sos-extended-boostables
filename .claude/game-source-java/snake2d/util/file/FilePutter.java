package snake2d.util.file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.zip.DeflaterOutputStream;

import snake2d.util.misc.ACTION;

public final class FilePutter {

	private final OutputStream out;
	private final ObjectOutputStream object;
	private final ByteBuffer buffer;
	public final Path path;

	public FilePutter(Path path, int size) {
		buffer = ByteBuffer.allocate(size);
		this.path = path;
		out = new ByteBufferBackedOutputStream(buffer);
		try {
			object = new ObjectOutputStream(out);
			object.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void save() {

		try {

			object.flush();
			File f = new File(""+path);
			f.createNewFile();
			FileOutputStream outfile = new FileOutputStream(f);
			
			buffer.flip();
			
			outfile.getChannel().write(buffer);
			outfile.flush();
			outfile.close();
			
		} catch (IOException e1) {
			
			throw new RuntimeException(e1);
		}
		
	}
	
	private volatile Exception ex;
	private volatile boolean working = false;
	
	public boolean zip(ACTION checkin) {
		
		Thread current = Thread.currentThread();
		
		ex = null;
		working = true;
		
		Thread t = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					object.flush();
					File f = new File(""+path);
					f.createNewFile();
					f.setWritable(true);
					
					
					FileOutputStream out = new FileOutputStream(f);
					DeflaterOutputStream defl = new DeflaterOutputStream(out, true);
					buffer.flip();
					
					ByteBuffer b = ByteBuffer.allocate(4);
					b.putInt(buffer.limit());
					b.flip();
					defl.write(b.array());
					defl.flush();
					
					defl.write(buffer.array(), 0, buffer.limit());
					defl.flush();
					defl.close();
					
					out.close();
					working = false;
				} catch (IOException e1) {
					ex = e1;
					working = false;
					current.interrupt();
					e1.printStackTrace();
					throw new RuntimeException(e1);
				}
				
			}
		});
		t.setName("zipper");
		t.start();
		

		long m = System.currentTimeMillis();
		long check = 0;
		
		while (System.currentTimeMillis()-m < 10000 && working) {
			Thread.yield();
			if (check != (System.currentTimeMillis()-m)/1000) {
				check = (System.currentTimeMillis()-m)/100;
				checkin.exe();
			}
		}
		
		if (working) {
			System.err.println("saver thread took too long to save...");
		}
		
		if (ex != null) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
		
		return true;
	}

	public FilePutter bool(boolean bool) {
		b((byte) (bool ? 1 :0));
		return this;
	}
	
	public void mark(String s) {
		i(s.hashCode());
	}

	public void mark(Class<?> c) {
		mark(c.getSimpleName());
	}

	public void mark(Object o) {
		mark(o.getClass().getSimpleName());
	}

	public void object(Object o) {
		try {
			int pos = getPosition();
			i(0);
			object.writeObject(o);
			object.flush();
			int npos = getPosition();
			int l = getPosition()-pos;
			buffer.position(pos);
			i(l);
			buffer.position(npos);
		} catch (IOException e) {
			System.err.println(o);
			System.err.println(o.getClass());
			System.err.println(o.getClass().getName());
			throw new RuntimeException(o + " " + e);
		}
	}

	public FilePutter i(int i) {
		writeInt(i);
		return this;
	}
	
	public FilePutter b(byte b) {
		buffer.put(b);
		return this;
	}
	
	public FilePutter l(long l) {
		buffer.putLong(l);
		return this;
	}
	
	public FilePutter ls(long[] ls) {
		for (long l : ls)
			buffer.putLong(l);
		return this;
	}
	
	public void lsE(long[] tiles) {
		i(tiles.length);
		ls(tiles);
	}
	
	public FilePutter ls(long[][] ls) {
		for (long[] l : ls)
			ls(l);
		return this;
	}

	public void writeArray(short[][] tiles) {
		for (short[] sa : tiles) {
			ss(sa);
		}
	}

	public FilePutter s(short s) {
		buffer.putShort(s);
		return this;
	}
	
	public void ss(short[] tiles) {
		for (short s : tiles)
			buffer.putShort(s);
	}
	
	public void ssE(short[] tiles) {
		i(tiles.length);
		for (short s : tiles)
			buffer.putShort(s);
	}

	public void is(int[] tiles) {
		for (int i : tiles)
			buffer.putInt(i);
	}
	
	public void isE(int[] tiles) {
		i(tiles.length);
		for (int i : tiles)
			buffer.putInt(i);
	}
	
	public void ds(double[] tiles) {
		for (double i : tiles)
			buffer.putDouble(i);
	}
	
	public void dsE(double[] tiles) {
		i(tiles.length);
		for (double i : tiles)
			buffer.putDouble(i);
	}
	
	public void save(SAVABLE ss) {
		int pos = getPosition();
		i(0);
		ss.save(this);
		int le = getPosition()-pos-4;
		setAtPosition(pos, le);
	}
	

	public void fs(float[] data) {
		for (float i : data)
			buffer.putFloat(i);
	}
	
	public void fsE(float[] data) {
		i(data.length);
		for (float i : data)
			buffer.putFloat(i);
	}
	
	public void f(float f) {
		buffer.putFloat(f);
	}

	public void is(int[][] tiles) {
		
		for (int[] sa : tiles) {
			is(sa);
		}
	}
	
	public void isE(int[][] tiles) {
		i(tiles.length);
		for (int[] i : tiles)
			isE(i);
	}

	public void bs(byte[][] bytes) {
		for (byte[] sa : bytes) {
			bs(sa);
		}
	}

	public void bs(byte[] sa) {
		buffer.put(sa);
	}
	
	public void bsE(byte[] sa) {
		i(sa.length);
		buffer.put(sa);
	}

	public void writeInt(int i){
		buffer.putInt(i);
	}
	
	public FilePutter d(double d){
		buffer.putDouble(d);
		return this;
	}
	
	public int writtenInts(){
		return buffer.position()/4;
	}
	
	public int getPosition() {
		return buffer.position();
	}
	
	public void setAtPosition(int pos, int value) {
		int p = buffer.position();
		buffer.position(pos);
		i(value);
		buffer.position(p);
	}
	
	public void chars(CharSequence c) {
		i(0);
		i(c.length());
		for (int i = 0; i < c.length(); i++) {
			buffer.putShort((short) c.charAt(i));
		}
	}
	
	public void charss(CharSequence[] cc) {
		i(0);
		i(cc.length);
		for (int i = 0; i < cc.length; i++) {
			chars(cc[i]);
		}
	}

	private static class ByteBufferBackedOutputStream extends OutputStream {
		ByteBuffer buf;

		ByteBufferBackedOutputStream(ByteBuffer buf) {
			this.buf = buf;
		}

		@Override
		public void write(int b) throws IOException {
			buf.put((byte) b);
		}

		@Override
		public void write(byte[] bytes, int off, int len) throws IOException {
			buf.put(bytes, off, len);
		}

	}


}
