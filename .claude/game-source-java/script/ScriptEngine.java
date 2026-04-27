package script;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import game.GAME;
import game.GAME.GameResource;
import game.debug.Profiler;
import script.SCRIPT.SCRIPT_INSTANCE;
import snake2d.Errors;
import snake2d.LOG;
import snake2d.MButt;
import snake2d.Renderer;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.KeyMap;
import snake2d.util.sets.LIST;
import util.gui.misc.GBox;

public class ScriptEngine extends GameResource {

	private ArrayListGrower<Script> loads = new ArrayListGrower<Script>();
	private static LIST<ScriptLoad> all;

	public ScriptEngine(String[] scripts) {
		super("SCRIPTS", true);
		LOG.ln("adding scripts " + scripts.length);
		
		
		KeyMap<Boolean> map = new KeyMap<Boolean>();
		for (String s : scripts) {
			if (!map.containsKey(s))
				map.put(s, false);
		}
		LIST<ScriptLoad> loads = ScriptLoad.getAll();
		for (ScriptLoad l : loads) {
			if (map.containsKey(l.key) || l.script.forceInit()) {
				map.putReplace(l.key, true);
				LOG.ln("adding script: " + l.file + " " + l.className + " " + l.script.forceInit());
				Script sc = new Script(l);
				this.loads.add(sc);
			}
		}

		for (String s : scripts) {
			if (!map.get(s)) {
				GAME.Warn("Could not find script: " + s);
			}
		}
		
		init.initBeforeGameCreated();

	}

	public void init(GAME game) {
		for (Script s : loads) {
			try {
				s.ins = s.load.script.createInstance();
			} catch (Exception e) {
				error(s.load, e);
			}
		}
	}

	public String[] currentScripts() {
		String[] scripts = new String[loads.size()];
		int i = 0;
		for (Script l : loads)
			scripts[i++] = l.load.key;
		return scripts;
	}

	private void error(ScriptLoad l, Exception e) {

		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);
		out.append("error in script " + l.className);
		out.append(System.lineSeparator());
		e.printStackTrace(out);

		throw new Errors.DataError(writer.toString(), l.file);

	}

	public static LIST<ScriptLoad> getAll() {
		if (all == null)
			all = ScriptLoad.getAll();

		return all;
	}

	public static LIST<ScriptLoad> getInJar(String jarFile) {
		ArrayListGrower<ScriptLoad> res = new ArrayListGrower<>();
		for (ScriptLoad l : getAll()) {
			if (l.file.equals(jarFile)) {
				res.add(l);
			}
		}
		return res;

	}

	@Override
	protected void save(FilePutter file) {
		file.mark(this);
		file.i(loads.size());
		for (Script s : loads) {
			file.chars(s.load.key);
			int pos = file.getPosition();
			file.i(0);
			s.ins.save(file);
			int size = (file.getPosition() - pos) - 4;
			file.setAtPosition(pos, size);
		}
	}

	@Override
	public void load(FileGetter file) throws IOException {
		file.check(this);
		int am = file.i();

		KeyMap<Script> map = new KeyMap<>();
		for (Script s : loads)
			map.put(s.load.key, s);
		
		while (am-- > 0){
			String key = file.chars();
			int size = file.i();
			int position = file.getPosition();
			if (map.containsKey(key)) {
				Script l = map.get(key);
				try {
					l.ins.load(file);
				} catch (Exception e) {
					error(l.load, e);
				}
				if (size != (file.getPosition() - position)) {			
					LOG.ln("Unable to load script. Was saved with + " + size + " bytes, but read "
							+ (file.getPosition() - position) + " " + l.ins.hashCode());
					file.setPosition(position + size);
					if (l.ins.handleBrokenSavedState()) {
						LOG.ln("Script wants to carry on anyway, so be it.");
					} else
						continue;
				}
				
			}else {
				LOG.ln("Script does not exist. Skipping. " + size + " " + key);
				for (int i = 0; i < size; i++)
					file.b();
			}
			
		}

	}

	@Override
	protected void update(double ds, Profiler prof) {
		prof.logStart(ScriptEngine.class);
		for (Script s : loads)
			try {
				s.ins.update(ds);
			} catch (Exception e) {
				error(s.load, e);
			}
		prof.logEnd(ScriptEngine.class);
	}
//
//	public void hoverTimer(double mouseTimer, GBox text) {
//		for (Script s : loads)
//			try {
//				s.ins.hoverTimer(mouseTimer, text);
//			} catch (Exception e) {
//				error(s.load, e);
//			}
//	}
//
//	public void render(Renderer r, float ds) {
//		for (Script s : loads)
//			try {
//				s.ins.render(r, ds);
//			} catch (Exception e) {
//				error(s.load, e);
//			}
//	}
//
//	public void mouseClick(MButt button) {
//		for (Script s : loads)
//			try {
//				s.ins.mouseClick(button);
//			} catch (Exception e) {
//				error(s.load, e);
//			}
//	}
//
//	public void hover(COORDINATE mCoo, boolean mouseHasMoved) {
//		for (Script s : loads)
//			try {
//				s.ins.hover(mCoo, mouseHasMoved);
//			} catch (Exception e) {
//				error(s.load, e);
//			}
//	}

	
	
	private static class Script {

		private final ScriptLoad load;
		private SCRIPT_INSTANCE ins;

		Script(ScriptLoad load) {
			this.load = load;
		}

	}
	
	public final SCRIPT init = new SCRIPT() {
		
		@Override
		public CharSequence name() {
			throw new RuntimeException();
		}
		
		@Override
		public boolean isSelectable() {
			throw new RuntimeException();
		}
		
		@Override
		public void initBeforeGameCreated() {
			for (Script s : loads)
				try {
					s.load.script.initBeforeGameCreated();
				} catch (Exception e) {
					error(s.load, e);
				}
		}
		
		@Override
		public CharSequence desc() {
			throw new RuntimeException();
		}
		
		@Override
		public SCRIPT_INSTANCE createInstance() {
			throw new RuntimeException();
		}
		
		@Override
		public void initBeforeGameInited() {
			for (Script s : loads)
				try {
					s.load.script.initBeforeGameInited();
				} catch (Exception e) {
					error(s.load, e);
				}
		}


	};
	
	public final SCRIPT_INSTANCE callback = new SCRIPT_INSTANCE() {
		
		@Override
		public void update(double ds) {
			throw new RuntimeException();
		}
		
		@Override
		public void hoverTimer(double mouseTimer, GBox text) {
			for (Script s : loads)
				try {
					s.ins.hoverTimer(mouseTimer, text);
				} catch (Exception e) {
					error(s.load, e);
				}
		}
		
		@Override
		public void render(Renderer r, float ds) {
			for (Script s : loads)
				try {
					s.ins.render(r, ds);
				} catch (Exception e) {
					error(s.load, e);
				}
		}
		
		@Override
		public void mouseClick(MButt button) {
			for (Script s : loads)
				try {
					s.ins.mouseClick(button);
				} catch (Exception e) {
					error(s.load, e);
				}
		}
		
		@Override
		public void hover(COORDINATE mCoo, boolean mouseHasMoved) {
			for (Script s : loads)
				try {
					s.ins.hover(mCoo, mouseHasMoved);
				} catch (Exception e) {
					error(s.load, e);
				}
		}
		
		@Override
		public void save(FilePutter file) {
			throw new RuntimeException();
		}
		
		@Override
		public void load(FileGetter file) throws IOException {
			throw new RuntimeException();
		}
	};
	

}
