package game.events.slave;

import java.io.IOException;
import java.util.Arrays;

import game.GAME;
import game.battle.div.Div;
import init.constant.Config;
import init.race.RACES;
import init.race.Race;
import init.type.HTYPES;
import settlement.entity.humanoid.Humanoid;
import settlement.room.main.throne.THRONE;
import settlement.stats.STATS;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Coo;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayList;

public final class UprisingSpots{

	private ArrayList<UprisingSpot> spots = new ArrayList<>(Config.battle().DIVISIONS_PER_ARMY);
	private int divI;
	private int inposition;
	private final Coo position = new Coo();
	
	private int[] amountsTotal = new int[RACES.all().size()];
	private int[] signedUps = new int[RACES.all().size()];
	
	UprisingSpots(){
		
	}
	
	public int riot(double amountD) { 
		
		
		
		//amount = STATS.POP().pop(HTYPE.SLAVE);
		clear();
		
		if (!UprisingSpot.setStart(position, THRONE.coo().x(), THRONE.coo().y(), 128))
			return 0;
		
		int amountTotal = 0; 
		GAME.BATTLE_THREADS().pause();
		for (int ri = 0; ri < RACES.all().size(); ri++) {
			
			Race r = RACES.all().get(ri);
			int am = (int) (STATS.POP().pop(r, HTYPES.SLAVE())*amountD);
			
			
			if (am > 0) {
				int sp = (int) Math.ceil((double)am/(Config.battle().MEN_PER_DIVISION));
				sp = CLAMP.i(sp, 0, spots.max());
				
				int menPerSpot = (int) Math.ceil((double)am/sp);
				for (int i = 0; i < sp && spots.size() < Config.battle().DIVISIONS_PER_ARMY; i++) {
					
					int a = CLAMP.i(menPerSpot, 0, am);
					am -= a;
					
					UprisingSpot s = UprisingSpot.make(position.x(), position.y(), a, r);
					amountsTotal[s.race] += s.amountTotal;
					if (s != null) {
						spots.add(s);
						amountTotal += s.amountTotal;
					}
					
				}
			}
		}

		GAME.BATTLE_THREADS().unpause(true);
		
		
		return amountTotal;
		
	}
	
	boolean hasMore() {
		return spots.size() > 0;
	}
	
	void save(FilePutter file) {
		file.i(divI);
		file.i(0);
		file.i(0);
		file.i(inposition);
		position.save(file);
		file.i(spots.size());
		for (UprisingSpot s : spots)
			s.save(file);
	}
	
	protected void load(FileGetter file) throws IOException {
		divI = file.i();
		file.i();
		file.i();
		inposition = file.i();
		position.load(file);
		int am = file.i();
		spots.clear();
		for (int i = 0; i < am; i++) {
			spots.add(UprisingSpot.make(file));
		}
		Arrays.fill(amountsTotal, 0);
		Arrays.fill(signedUps, 0);
		for (UprisingSpot s : spots) {
			amountsTotal[s.race] += s.amountTotal;
			signedUps[s.race] += s.signedUp;
		}
		
		
	}
	
	
	protected void clear() {
		spots.clear();
		divI = 0;
		Arrays.fill(amountsTotal, 0);
		Arrays.fill(signedUps, 0);
		inposition = 0;
	}
	
	public boolean shouldSignUpUpriser(Humanoid h) {
		return signedUps[h.race().index()] < amountsTotal[h.race().index()];
	}
	
	public int signUpUpriserPositionByte(Humanoid h) {
		for (int i = 0; i < spots.size(); i++) {
			if (spots.get(i).race == h.race().index() && spots.get(i).signedUp < spots.get(i).amountTotal) {
				spots.get(i).signedUp++;
				signedUps[spots.get(i).race] ++;
				return i;
			}
		}
		return -1;
	}
	
	public boolean confirmUpriser(int positionByte) {
		if (positionByte < 0 || positionByte >= spots.size())
			return false;
		return spots.get(positionByte).signedUp <= spots.get(positionByte).amountTotal;
	}
	
	public void reportUpriserInPosition(int positionByte) {
		if (positionByte < 0 || positionByte >= spots.size())
			return;
		inposition ++;
	}
	
	public void cancelUpriser(Humanoid h, int positionByte, boolean inPosition) {
		if (positionByte < 0 || positionByte >= spots.size())
			return;
		spots.get(positionByte).signedUp --;
		signedUps[h.race().index] --;
		if (inPosition) {
			inposition --;
		}
	}
	
	public COORDINATE getUpriserTile(int positionByte) {
		return spots.get(positionByte);
	}
	
	private double dri = 0;
	
	boolean update(double ds) {

		dri += ds;
		
		if (dri < 1) {
			return false;
		}
		
		for(int i = 0; i < spots.size(); i++) {
			spots.get(i).validate();
		}
		
		dri -= 1;
		
		for (Race r : RACES.all()) {
			if (amountsTotal[r.index] > STATS.POP().pop(r, HTYPES.SLAVE())){
				for(int i = 0; i < spots.size(); i++) {
					if (spots.get(i).race == r.index() && spots.get(i).amountTotal > 0) {
						spots.get(i).amountTotal--;
						amountsTotal[r.index] --;
					}
				}
			}
		}
		
		int amTot = 0;
		for(int i = 0; i < spots.size(); i++) {
			amTot += spots.get(i).amountTotal;
		}
		if (inposition >= amTot) {
			GAME.ARMIES().factors.init(GAME.ARMIES().enemy(), 1.0);
			while(nextDivision() && spots.size() > 0) {
				Div d = GAME.ARMIES().enemy().divisions().get(divI);
				spots.removeLast().makeDiv(d, spots.size());
			}
			clear();
			GAME.ARMIES().factors.init(GAME.ARMIES().enemy(), 1.0);
			return true;
		}
		
		return false;
		
	}
	
	private boolean nextDivision() {
		
		if (divI >= Config.battle().DIVISIONS_PER_ARMY) {
			return false;
		}
		while(GAME.ARMIES().enemy().divisions().get(divI).menNrOf() > 0) {
			divI ++;
			
			if (divI >= Config.battle().DIVISIONS_PER_ARMY) {
				return false;
			}
		}
		return true;
	}
	



	


	
}
