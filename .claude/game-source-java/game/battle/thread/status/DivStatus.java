package game.battle.thread.status;

import java.io.IOException;
import java.util.Arrays;

import game.GAME;
import game.battle.div.Div;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LISTE;

public final class DivStatus implements SAVABLE{

	byte enemyDirMask;
	
	double flanks = 0;
	double encirclement = 0;
	double enemyThreats = 0;
	double friends = 0;
	short engagements = 0;
	
	static final int iSize = 8;
	private static int iFriendlyColl = 0;
	private static int iEnemyColl = iSize;
	private static int iEnemyInRange = 2*iSize;
	private static int iEnemyDist = 3*iSize;
	private static int iFriendlyInRange = 4*iSize;
	private static int iEnemyCharging = 5*iSize;
	
	final short[] lists = new short[iEnemyCharging +iSize];
	
	//dir faceEnemyDirection (the best way to face the enemy
	// faceEnemyWidth
	
	DivStatus() {
		
	}
	
	public boolean threatAt(DIR d, Div div) {
		return GAME.ARMIES().factors.projectiles(div) > 0 || (enemyDirMask != 0 && (threat(d) || threat(d.next(-1)) || threat(d.next(1))));
	}
	
	public boolean threat(DIR d) {
		if (d.isOrtho()) {
			return (enemyDirMask & d.mask()) != 0;
		}else {
			return ((enemyDirMask)>>4 & d.mask()) != 0;
		}
	}
	
	@Override
	public void save(FilePutter file) {
		file.ss(lists);
		file.b(enemyDirMask);
		file.d(flanks);
		file.d(enemyThreats);
		file.d(friends);
		file.s(engagements);
	}

	@Override
	public void load(FileGetter file) throws IOException {
		file.ss(lists);
		enemyDirMask = file.b();
		flanks = file.d();
		enemyThreats = file.d();
		friends = file.d();
		engagements = file.s();
	}

	@Override
	public void clear() {
		Arrays.fill(lists, (short)-1);
		enemyDirMask = 0;
		flanks = 0;
		enemyThreats = 0;
		friends = 0;
		engagements = 0;
	}

	public LIST<Div> friendlyCollisions(LISTE<Div> res){
		return fill(iFriendlyColl, res);
	}
	
	public int friendlyCollisions(){
		return count(iFriendlyColl);
	}
	
	void friendlyCollisionSet(short di) {
		set(iFriendlyColl, di);
	}
	
	public LIST<Div> enemyCollisions(LISTE<Div> res){
		return fill(iEnemyColl, res);
	}
	
	public int enemyCollisions(){
		return count(iEnemyColl);
	}
	
	void enemyCollisionSet(short di) {
		set(iEnemyColl, di);
	}
	
	public LIST<Div> enemiesClosest(LISTE<Div> res){
		return fill(iEnemyInRange, res);
	}
	
	public int enemiesClosest(){
		return count(iEnemyInRange);
	}
	
	public Div enemyClosest(){
		return getFirst(iEnemyInRange);
	}
	
	public int enemyClosestDist(){
		return lists[iEnemyDist];
	}
	
	public int enemyClosestDist(int i){
		return lists[iEnemyDist + i];
	}
	
	void enemiesClosestSet(short di, int tiles) {
		set(iEnemyInRange, di);
		set(iEnemyDist, (short) (tiles&0x0FFFF));
	}
	
	public LIST<Div> friendlyClosest(LISTE<Div> res){
		return fill(iFriendlyInRange, res);
	}
	
	public int friendlyClosest(){
		return count(iFriendlyInRange);
	}
	
	void friendlyClosestSet(short di) {
		set(iFriendlyInRange, di);
	}
	
	
	private void set(int start, short di) {
		if (lists[start+iSize-1] != -1)
			return;
		for (int i = 0; i < iSize; i++) {
			int k = i+start;
			if (lists[k] == -1) {
				lists[k] = di;
				return;
			}
		}
	}
	
	private int count(int start) {
		for (int i = 0; i < iSize; i++) {
			int k = i+start;
			if (lists[k] == -1)
				return i;
		}
		return iSize;
	}
	
	private LIST<Div> fill(int start, LISTE<Div> res) {
		for (int i = 0; i < iSize; i++) {
			int k = i+start;
			if (lists[k] == -1)
				return res;
			res.add(GAME.ARMIES().division(lists[k]));
			if (!res.hasRoom())
				return res;
		}
		return res;
	}
	
	private Div getFirst(int start) {
		if (lists[start] == -1)
			return null;
		return GAME.ARMIES().division(lists[start]);
		
	}
	
	public boolean isFighting() {
		return engagements > 0;
	}
	
	public double ajacentFriendsPower() {
		return friends;
	}
	
	public double ajacentEnemiesPower() {
		return enemyThreats;
	}
	
	public double encirclementPower() {
		return encirclement;
	}
	
	public int engagements() {
		return engagements;
	}
	
	public double flanks() {
		return flanks;
	}

	
}
