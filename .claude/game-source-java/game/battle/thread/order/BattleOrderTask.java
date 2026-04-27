package game.battle.thread.order;

import java.io.IOException;

import game.GAME;
import game.battle.div.Div;
import game.battle.util.Copyable;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;

public class BattleOrderTask implements Copyable<BattleOrderTask>{

	private DIVTASK task = DIVTASK.MOVE;
	private int target;
	private boolean orderedWhenFighting;
	
	public static enum DIVTASK {
		
		STOP(false, false),
		MOVE(true, true),
		ATTACK_BUILDING(false, true),
		ATTACK_MELEE(false, true),
		ATTACK_RANGED(false, true),
		CHARGE(false, false),
		;
		
		public final boolean showDest;
		public final boolean showPath;
		
		private DIVTASK(boolean dest, boolean path) {
			showDest = dest;
			showPath = path;
		}
		
		public static final LIST<DIVTASK> all = new ArrayList<DIVTASK>(values());
	}
	
	public DIVTASK task() {
		return task;
	}
	
	private void set(DIVTASK t, Div div) {
		orderedWhenFighting = div.status().isFighting();
		this.task = t;
	}
	
	public void move(Div div) {
		set(DIVTASK.MOVE, div);
	}
	
	public BattleOrderTask stop(Div div) {
		set(DIVTASK.STOP, div);
		return this;
	}
	
	public void attack(int tx, int ty, Div div) {
		target = tx | ty <<16;
		set(DIVTASK.ATTACK_BUILDING, div);
	}
	
	public void attackMelee(Div other, Div div) {
		target = other.index();
		set(DIVTASK.ATTACK_MELEE, div);
	}
	
	public void attackRanged(Div other, Div div) {
		target = other.index();
		set(DIVTASK.ATTACK_RANGED, div);
	}
	
	public void charge(Div div) {
		set(DIVTASK.CHARGE, div);
	}

	@Override
	public void save(FilePutter file) {
		file.i(task.ordinal());
		file.i(target);
		file.bool(orderedWhenFighting);
	}

	@Override
	public void load(FileGetter file) throws IOException {
		if (false) {
			//need an attack ranged building (catapults)
		}
		task = DIVTASK.all.get(file.i());
		target = file.i();
		orderedWhenFighting = file.bool();
	}

	@Override
	public void clear() {
		task = DIVTASK.STOP;
		target = 0;
		orderedWhenFighting = false;
	}

	@Override
	public void copy(BattleOrderTask toBeCopied) {
		task = toBeCopied.task;
		target = toBeCopied.target;
		orderedWhenFighting = toBeCopied.orderedWhenFighting;
	}
	
	public Div targetDiv() {
		if (task != DIVTASK.ATTACK_MELEE && task != DIVTASK.ATTACK_RANGED)
			return null;
		return GAME.ARMIES().division((short) target);
	}
	
	public int targetTileX() {
		if (task != DIVTASK.ATTACK_BUILDING)
			return -1;
		return target &0x0FFFF;
	}
	
	public int targetTileY() {
		if (task != DIVTASK.ATTACK_BUILDING)
			return -1;
		return (target >> 16)&0x0FFFF;
	}

	public boolean orderedWhenFighting() {
		return orderedWhenFighting;
	}
	
	
}
