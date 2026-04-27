package game.nobility;

import java.io.IOException;

import game.GAME;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.INDEXED;
import util.gui.misc.GBox;

public final class Noble implements INDEXED{

	public final short index;
	private int subjectID = -1;
	private int office = -1;
	private int rank = 0;
	
	Noble(ArrayList<Noble> ii){
		
		index = (short) ii.add(this);
		
	}
	
	public Humanoid subject() {
		if (subjectID == -1)
			return null;
		ENTITY e = SETT.ENTITIES().getByID(subjectID);
		if (e != null && e instanceof Humanoid) {
			return (Humanoid) e;
		}else {
			subjectID = -1;
			return null;
		}
	}
	
	void rankInc() {
		if (rank < GAME.NOBLE().maxRanks()-1) {
			rank ++;
		}
	}
	
	void assign(Humanoid h) {
		saver.clear();
		subjectID = h.id();
		update(0);
	}
	
	void setOffice(NobleOffice office) {
		this.office = office == null ? -1 : office.index;
	}
	
	void update(double ds) {
		if (subject() != null) {
			
		}
	}
	
	final SAVABLE saver = new SAVABLE() {
		
		@Override
		public void save(FilePutter file) {
			file.i(subjectID);
			file.i(rank);
			file.i(office);
		}
		
		@Override
		public void load(FileGetter file) throws IOException {
			subjectID = file.i();
			rank = file.i();
			office = file.i();
		}
		
		@Override
		public void clear() {
			subjectID = -1;
			rank = 0;
			office = -1;
		}
	};
	
	public NobleOffice office(){
		if (office < 0 || office >= GAME.NOBLE().OFFICES.size())
			return null;
		return GAME.NOBLE().OFFICES.get(office);
	}
	
	public CharSequence title() {
		NobleOffice n = office();
		if (n == null)
			return rankName();
		return n.name;
	}
	
	public int rank() {
		return rank;
	}
	
	public void hoverOffice(GUI_BOX box) {
		
		NobleOffice o = office();
		if (o == null)
			return;
		GBox b = (GBox) box;
		
		b.title(o.name);
		b.text(o.desc);
		b.NL();
		
		o.hoverValue(b, 1 + rank()*NOBLES.RANK_INCREASE);
		
	}
	
	public CharSequence rankName() {
		return GAME.NOBLE().nameRanks[rank];
	}

	@Override
	public int index() {
		return index;
	}
	
}
