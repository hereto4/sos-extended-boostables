package settlement.path.finders;

import static settlement.main.SETT.PATH;

import settlement.path.components.SComponent;
import settlement.path.path.SPath;
import snake2d.LOG;
import view.sett.IDebugPanelSett;
import view.tool.PlacableSingle;

class TestPath extends PlacableSingle{

	private final SFINDER finder;
	static final SPath tester = new SPath();
	
	TestPath(CharSequence name, SFINDER finder){
		super("path test: " + name);
		this.finder = finder;
		IDebugPanelSett.add(this);
	}
	

	@Override
	public CharSequence isPlacable(int tx, int ty) {
		SComponent c =  PATH().comps.zero.get(tx, ty);
		if(c == null) {
			return E;
		}
		return null;
	}

	@Override
	public void placeFirst(int tx, int ty) {
		place(tx, ty, tester);
		if (!tester.isSuccessful())
			LOG.ln("nay!");
		else
			LOG.ln("yay " + tester.destX() + " " + tester.destY());
	}
	
	protected void place(int sx, int sy, SPath p) {
		tester.request(sx, sy, finder, Integer.MAX_VALUE);
	}

	
	
}
