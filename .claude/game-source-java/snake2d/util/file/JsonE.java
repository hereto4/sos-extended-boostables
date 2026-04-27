package snake2d.util.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.HashSet;

import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LinkedList;
import snake2d.util.sets.Tuple.TupleImp;

public class JsonE {

	private final LinkedList<TupleImp<String, String>> list = new LinkedList<>();
	
	private final HashSet<String> map = new HashSet<>();
	
	public JsonE(){
		
	}
	
	public void add(String key, String value) {
		padd(key, value);
	}
	
	
	private void padd(String key, String value) {
		if (map.contains(key))
			throw new RuntimeException(" " + key);
		map.add(key);
		list.add(new TupleImp<String, String>(key,  value));
		
	}
	
	public void add(String key, String[] values) {
		add(key, new ArrayList<String>(values));
	}
	
	public void add(String key, LIST<String> values) {
		StringBuilder b = new StringBuilder();
		b.append('[');
		b.append(System.lineSeparator());
		for (String v : values) {
			b.append(v);
			b.append(',');
			b.append(System.lineSeparator());
		}
		b.append(']');
		add(key, b.toString());
	}
	
	public void addString(String key, String value) {
		add(key, "\""+ value + "\"");
	}
	
	public void addStrings(String key, String[] values) {
		String[] vvs = new String[values.length];
		for (int i = 0; i < values.length; i++) {
			vvs[i] = "\""+ values[i] + "\"";
		}
		add(key, vvs);
	}
	
	public boolean has(String key) {
		return map.contains(key);
	}
	
	public void add(String key, boolean b) {
		padd(key, b ? "true" : "false");
	}
	
	public void add(String key, int i) {
		padd(key, ""+i);
	}
	
	public void add(String key, int[] is) {
		String[] values = new String[is.length];
		for (int i = 0; i < is.length; i++) {
			values[i] = ""+is[i];
		}
		add(key, values);
	}
	
	public void add(String key, double d) {
		padd(key, ""+d);
	}
	
	public void add(String key, double[] is) {
		String[] values = new String[is.length];
		for (int i = 0; i < is.length; i++) {
			values[i] = ""+is[i];
		}
		add(key, values);
	}
	
	public void add(String key, JsonE json) {
		StringBuilder b = new StringBuilder();
		b.append('{');
		b.append(System.lineSeparator());
		b.append(json.toString());
		b.append('}');
		add(key, b.toString());
	}
	
	public void add(String key, JsonE[] jsons) {
		String[] strings = new String[jsons.length];
		for (int i = 0; i < jsons.length; i++) {
			strings[i] = "{" + System.lineSeparator() + jsons[i].toString() + System.lineSeparator() + "}";
		}
		add(key, strings);
	}
	
	public void addJ(String key, LIST<JsonE> jsons) {
		String[] strings = new String[jsons.size()];
		for (int i = 0; i < jsons.size(); i++) {
			strings[i] = '\t' + "{" + System.lineSeparator() + jsons.get(i).toString(2) + System.lineSeparator() + '\t' + "}";
		}
		add(key, strings);
	}
	
	@Override
	public String toString() {
		return toString(0);
	}
	
	public String toString(int tabs) {
		StringBuilder b = new StringBuilder();
		for(TupleImp<String, String> e : list) {
			for (int t = 0; t < tabs; t++)
				b.append('\t');
			b.append(e.a);
			b.append(':');
			b.append(' ');
			String[] ss = e.b.split(System.lineSeparator());
			b.append(ss[0]);
			for (int i = 1; i < ss.length; i++) {
				b.append(System.lineSeparator());
				for (int t = 0; t < tabs; t++)
					b.append('\t');
				if (i < ss.length-1)
					b.append('\t');
				b.append(ss[i]);
			}
			b.append(',');
			b.append(System.lineSeparator());
		}
		return b.toString();
	}
	
	public boolean save(String path) {
		try {
			if (new File(path).exists())
				new File(path).delete();
			PrintWriter out = new PrintWriter(path);
		    out.println(toString());
		    out.flush();
		    out.close();
		    return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean save(java.nio.file.Path path) {
		try {
			Files.deleteIfExists(path);
			PrintWriter out = new PrintWriter(Files.newOutputStream(path));
		    out.println(toString());
		    out.flush();
		    out.close();
		    return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
}
