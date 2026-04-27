package snake2d.util.file;

import java.util.HashMap;

import snake2d.Errors;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LinkedList;

class JsonParser {
	
	private final String content;
	private final HashMap<String, Value> map = new HashMap<>();
	private final LinkedList<String> keys = new LinkedList<>();
	private int i = 0;
	final String path;
	private int line;
	final int linestart;
	private final int end;
	
	JsonParser(String content, String errorPath){
		this(content, errorPath, 0, content.length(),1);
	}
	
	private JsonParser(String content, String errorPath, int index, int end, int linestart){
		this.content = content;
		this.path = errorPath;
		line = linestart;
		this.linestart = linestart;
		
		this.i = index;
		this.end = end;
		
		try {
			while(nextChar()) {
				String key = getKey();
				Value v = getValue();
				if (map.containsKey(key))
					throwError("Duplicate entry: " + key);
				map.put(key, v);
				keys.add(key);
			
			}
		}catch(StringIndexOutOfBoundsException e) {
			e.printStackTrace();
			throwError("unexpected end of file after line: " +line + ", char: " + index);
		}

	}

	private boolean isNewline() {
		char c = content.charAt(i);

			
		if (c == '\r' && i < end-2 && content.charAt(i+1) == '\n') {
			line++;
			i+= 2;
			return true;
		}else if(c == '\r') {
			line++;
			i+= 1;
			return true;
		}else if(c == '\n') {
			line++;
			i+= 1;
			return true;
		}
		return false;
	}
	
	private boolean nextChar() {
		boolean comment = false;
		while(true) {
			if (i >= end-1)
				return false;

			if (content.charAt(i) == '*' && i < end-1 && content.charAt(i+1) == '*') {
				comment = true;
				i++;
				continue;
			}
			
			if (isNewline()) {
				comment = false;
				continue;
			}
			
			char c = content.charAt(i);
			
			if (comment) {
				i++;
			}else if(c == ' ' || c == '\t') {
				i++;
			}else {
				return true;
			}

		}
	}
	
	private boolean startsWithChar(int start, int end, char open) {
		int oi = i;
		int ol = line;
		
		i = start;
		if (nextChar()) {
			if (content.charAt(i) == open) {
				i = oi;
				line = ol;
				return true;
			}
		}
		i = oi;
		line = ol;
		return false;
	}
	
	private String getKey() {
		int i2 = i;
		int l = line;
		while(content.charAt(i2) != ':') {
			if (i2 >= end-1)
				throwError("Expecting a keyword followed by a ':' after line: " + l);
			i2++;
		}
		String key = content.substring(i, i2).trim();
		i = i2+1;
		nextChar();
		return key;
	}

	
	private Value getValue() {
		char c = content.charAt(i);
		int l = line;
		if (c == '\"') {
			return findValue('\"', '\"');
		}else if(c == '{') {
			return findValue('{', '}');
		}else if(c == '[') {
			return findValue('[', ']');
		}else {
			int start = i;
			while(++i < end) {
				if (isNewline())
					throwError("Expecting: ','");
				if (content.charAt(i) == ',') {
					i++;
					return new Value(start, i-1, l);
				}
			}
			int i2 = i - 10 >= 0 ? i-10 : 0;
			String after = i2 < i-1 ? content.substring(i2, i-1) : " ";
			throwError("Expecting: ',' after: '" + after + "'.");
			return null;
		}
	}
	
	private Value findValue(char open, char close) {
		
		int nesting = 0;
		int start = i;
		int l = line;
		i++;
		
		while(true) {
			if (i >= end) {
				
				throwError("Expecting a close : " + close + " followed by a ',' after line " + l);
			}
			if (isNewline()) {
				continue;
			}
			char c = content.charAt(i);
			
			if (c == close) {
				if((i == end-1 || content.charAt(i+1) == ',') && nesting == 0) {
					i+=2;
					return new Value(start, i, l);
				}
				nesting--;
			}
			if (c == open) {
				nesting++;
			}
			
			i++;
		}
	}
	

	
	private void throwError(String error){
		
		String m = "Error parsing line: " + line + ". " + error;
		throw new Errors.DataError(m, path);
	}
	
	public void throwError(String error, CharSequence key){
		Value v = map.get(key);
		if (v != null) {
			String m = "Error parsing line: " + v.line + ", key: " + key + ". " + error;
			throw new Errors.DataError(m, path);
		}else {
			throw new Errors.DataError(error + ". Error parsing key " + key, path);
		}
	}
	
	public String getError(String error, CharSequence key){
		Value v = map.get(key);
		if (v != null) {
			return error + " Key: " + key + " line: " + v.line + " " + path;
		}else {
			return (error + ". Error parsing key " + key + " " + path);
		}
	}
	
	public int line(String key) {
		Value v = testKey(key);
		return v.line;
	}
	
	private Value testKey(String key) {
		if (!map.containsKey(key)) {
			throw new Errors.DataError("Missing property: " + key + ", in object starting at line: " + linestart, path);
		}
		return map.get(key);
	}
	
	private Value testKey(CharSequence key, char start, char end, String type) {

		if (!map.containsKey(key)) {
			throw new Errors.DataError("Missing property: " + key + ", in object starting at line: " + linestart, path);
		}
		Value v = map.get(key);
		if (content.charAt(v.start) != start) {
			throw new Errors.DataError("Expecting a " + type + " (" + start + end +") at line: " + v.line, path);
		}
		return v;
	}
	
	private boolean isKey(CharSequence key, char start, char end) {

		if (!map.containsKey(key)) {
			return false;
		}
		Value v = map.get(key);
		if (content.charAt(v.start) == start) {
			return true;
		}
		return false;
	}
	
	private Value[] array(int line, int start, int end, String kay) {
		
		
		if (content.charAt(start) != '[') {
			throwError("Expecting an array ([]) Key: " + kay);
		}
		
		this.line = line;
		this.i = start+1;
		
		int size = 0;
		while(true) {
			if (i >= end || !nextChar()) {
				throwError("Expecting ']'");
			}
			if (content.charAt(i) == ']')
				break;
			getValue();
			size++;
		}
		Value[] res = new Value[size];
		this.line = line;
		this.i = start+1;
		size = 0;
		
		while(true) {
			if (i >= end || !nextChar()) {
				throwError("Expecting ']'");
			}
			if (content.charAt(i) == ']')
				break;
			res[size++] = getValue();
		}
		return res;
	}
	
	private String string(Value v, CharSequence key) {
		String s = content.substring(v.start+1, v.end-2);
		s = s.replaceAll("[\\n\\r]+","" + (char) 10);
		s = s.replaceAll("[\\t]+","");
		s = s.replaceAll("%r%", "" + (char) 10);
		return s;
	}
	
	public String string(CharSequence key) {
		Value v = testKey(key, '"', '"', "String");
		return string(v, key);
	}
	

	
	public String[] strings(String key) {
		Value v = testKey(key);
		Value[] vv = array(v.line, v.start, v.end, key);
		String[] res = new String[vv.length];
		for (int i = 0; i < vv.length; i++)
			res[i] = string(vv[i], key); 
		return res;
	}
	
	public JsonParser json(String key) {
		Value v = testKey(key, '{', '}', "Object");
		return new JsonParser(content, path, v.start+1, v.end-1, v.line);
	}
	
	public boolean jsonIs(String key) {
		return isKey(key, '{', '}');
	}
	
	
	public boolean textIs(String key) {
		return isKey(key, '"', '"');
	}
	
	public boolean jsonsIs(String key) {
		if (arrayIs(key)) {
			Value v = map.get(key);
			return startsWithChar(v.start+1, v.end, '{');
		}
		return false;
	}
	
	public boolean arrayIs(String key) {
		return isKey(key, '[', ']');
	}
	
	public boolean arrayArrayIs(String key) {
		if (arrayIs(key)) {
			Value v = map.get(key);
			return startsWithChar(v.start+1, v.end, '[');
		}
		return false;
	}
	
	public JsonParser[] jsons(String key) {
		Value v = testKey(key);
		Value[] vv = array(v.line, v.start, v.end, key);
		JsonParser[] res = new JsonParser[vv.length];
		for (int i = 0; i < vv.length; i++)
			res[i] = new JsonParser(content, path, vv[i].start+1, vv[i].end-1, vv[i].line);
		return res;
	}
	

	
	public String value(String key) {
		Value v = testKey(key);
		String s = content.substring(v.start, v.end);
		return s;
	}
	
	public String[] values(String key) {
		Value v = testKey(key);
		Value[] vv = array(v.line, v.start, v.end, key);
		String[] res = new String[vv.length];
		for (int i = 0; i < vv.length; i++)
			res[i] = content.substring(vv[i].start, vv[i].end); 
		return res;
	}
	
	public String[][] values2(String key) {
		Value v = testKey(key);
		Value[] vv = array(v.line, v.start, v.end, key);
		String[][] res = new String[vv.length][];
		for (int i = 0; i < vv.length; i++) {
			
			Value[] vvv = array(v.line, vv[i].start, vv[i].end, key); 
			content.substring(vvv[i].start, vvv[i].end); 
			res[i] = new String[vvv.length];
			for (int i2 = 0; i2 < vvv.length; i2++) {
				res[i][i2] = content.substring(vvv[i2].start, vvv[i2].end); 
			}
		}
		return res;
	}
	
	public String content() {
		return content;
	}
	
	public String content(String key) {
		Value v = testKey(key);
		int rem = 1;
		int ti = v.start-1;
		while(ti >= 0 && content.charAt(ti) != '\r' && content.charAt(ti) != '\n') {
			if (content.charAt(ti) == '\t')
				rem ++;
			ti--;
		}

		String[] ss = content.substring(v.start, v.end).split("\\r?\\n");
		
		if (ss[ss.length-1].indexOf(',') >= 0) {
			ss[ss.length-1] = ss[ss.length-1].substring(0, ss[ss.length-1].lastIndexOf(','));
		}
		
		
		String res = "";
		for (int si = 0; si < ss.length; si++) {
			String s = ss[si];
			int start = 0;
			while(start < rem && start < s.length() && s.charAt(start) == '\t') {
				
				start++;
			}
			res += s.substring(start, s.length());
			if (si < ss.length -1)
				res += System.lineSeparator();
		}
		
		return res;
	}
	
	private static class Value {
		
		private final int start;
		private final int end;
		private final int line;
		
		Value(int start, int end, int line){
			this.start = start;
			this.end = end;
			this.line = line;
		}
		
	}

	public boolean test(String key) {
		return map.containsKey(key);
	}

	public int count() {
		return map.size();
	}
	
	public LIST<String> keys() {
		return keys;
	}
	
}
