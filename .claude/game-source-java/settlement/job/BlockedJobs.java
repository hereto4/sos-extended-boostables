package settlement.job;

import java.io.IOException;

import game.GAME;
import game.audio.SoundRace;
import game.time.TIME;
import init.resources.RBIT;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import settlement.entity.humanoid.Humanoid;
import settlement.job.StateManager.State;
import settlement.main.SETT;
import settlement.misc.job.SETT_JOB;
import settlement.room.main.throne.THRONE;
import snake2d.LOG;
import snake2d.PathTile;
import snake2d.PathUtilOnline.Flooder;
import snake2d.Renderer;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Coo;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.ArrayList;
import util.GUTIL;
import util.rendering.RenderData;
import util.rendering.ShadowBatch;
import util.text.Dic;
import util.updating.IUpdater;
import view.sett.IDebugPanelSett;

public class BlockedJobs implements SAVABLE{

	private final BlockedJob[] all = new BlockedJob[16];
	private final ArrayList<BlockedJob> active = new ArrayList<BlockedJob>(all.length);
	private final ArrayList<BlockedJob> free = new ArrayList<BlockedJob>(all.length);
	private int upI = -1;
	private final IUpdater uper = new IUpdater(SETT.TAREA, TIME.secondsPerDay()) {
		
		@Override
		protected void update(int i, double timeSinceLast) {
			if (free.size() == 0)
				return;
			
			Job job = SETT.JOBS().getter.get(i);
			if (job == null)
				return;
			
			
			
			int tx = i%SETT.TWIDTH;
			int ty = i/SETT.THEIGHT;
			if (SETT.PATH().reachability.is(tx, ty))
				return;
			
			if (SETT.JOBS().state.get(tx, ty) == State.DORMANT)
				return;
			
			if (job.jobResourceBitToFetch() != null && !job.jobResourceBitToFetch().isClear() && !SETT.PATH().finders.resource.has(THRONE.coo().x(), THRONE.coo().y(), job.jobResourceBitToFetch()))
				return;
			
			for (BlockedJob j : active) {
				if (j.blocked.isSameAs(tx, ty))
					return;
			}
			
			Flooder f = GUTIL.flooder();
			
			f.init(this);
			f.pushSloppy(tx, ty, 0);
			while(f.hasMore()) {
				PathTile t = GUTIL.flooder().pollSmallest();
				
				if (t.getValue() > 16)
					break;
				
				if (SETT.PATH().reachability.is(t)) {
					BlockedJob j = free.removeLast();
					j.blocked.set(tx, ty);
					j.coo.set(t);
					active.add(j);
					f.done();
					return;
				}
				
				for (DIR d : DIR.ALL) {
					if (SETT.IN_BOUNDS(t, d)) {
						f.pushSmaller(t, d, t.getValue()+d.tileDistance());
					}
				}
				
				
			}
				
			f.done();
			
		}
	};
	
	void render(Renderer r, ShadowBatch shadowBatch, RenderData data) {
		
		
	}
	
	
	public BlockedJobs() {
		for (int i = 0; i < all.length; i++) {
			all[i] = new BlockedJob(i);
			free.add(all[i]);
		}
		while(free.hasRoom())
			free.add(new BlockedJob(free.size()));
		
		IDebugPanelSett.add("BLOCKED_JOB PERFORM", new ACTION() {
			
			@Override
			public void exe() {
				BlockedJob j = next();
				LOG.ln(j);
				if (j != null) {
					LOG.ln(j.coo + "  " + j.blocked);
					
					RBIT bb = j.jobResourceBitToFetch();
					RESOURCE res = null;
					if (bb != null) {
						for (RESOURCE r : RESOURCES.ALL())
							if (bb.has(r)) {
								res = r;
								break;
							}
					}
					
					if (j.jobReserveCanBe()) {
						j.jobReserve(res);
					}
					j.jobPerform(null, res, 1);
				}
				
			}
		});
	}
	
	public BlockedJob next() {
		if (upI != GAME.updateI()) {
			for (BlockedJob j : active) {
				if (!isActive(j, true)) {
					Job job = SETT.JOBS().getter.get(j.blocked);
					if (job != null)
						job.jobReserveCancel(null);
					
					active.remove(j);
					free.add(j);
					return null;
				}else if (j.jobReserveCanBe())
					return j;
			}
			upI = GAME.updateI();
		}
		return null;
	}
	
	public BlockedJob getByRef(int id) {
		BlockedJob j = all[id];
		if (isActive(j, false))
			return j;
		return null;
	}
	
	boolean isActive(BlockedJob j, boolean res) {
		
		Job job = SETT.JOBS().getter.get(j.blocked);
		if (job == null)
			return false;
		if (!job.becomesSolid())
			return false;
		
		if (SETT.PATH().reachability.is(j.blocked))
			return false;
		if (SETT.JOBS().state.get(j.blocked) == State.DORMANT)
			return false;
		if (!SETT.PATH().reachability.is(j.coo))
			return false;
		if (res) {
			if (job.jobResourceBitToFetch() != null && !job.jobResourceBitToFetch().isClear() && !SETT.PATH().finders.resource.has(THRONE.coo().x(), THRONE.coo().y(), job.jobResourceBitToFetch()))
				return false;
		}
		
		return true;
		
	}
	
	void update(double ds) {
		if (free.size() == 0)
			return;
		uper.update(ds);
	}
	
	public final class BlockedJob implements SETT_JOB{

		private final Coo coo = new Coo();
		private final Coo blocked = new Coo();
		public final int ID;
		
		private BlockedJob(int id) {
			this.ID = id;
		}
		
		@Override
		public void jobReserve(RESOURCE r) {
			Job j = SETT.JOBS().getter.get(blocked);
			if (j != null)
				j.jobReserve(r);
		}

		@Override
		public boolean jobReservedIs(RESOURCE r) {
			Job j = SETT.JOBS().getter.get(blocked);
			if (j != null)
				return j.jobReservedIs(r);
			return false;
		}

		@Override
		public void jobReserveCancel(RESOURCE r) {
			Job j = SETT.JOBS().getter.get(blocked);
			if (j != null)
				j.jobReserveCancel(r);
		}

		@Override
		public boolean jobReserveCanBe() {
			Job j = SETT.JOBS().getter.get(blocked);
			if (j != null)
				return j.jobReserveCanBe();
			return false;
		}

		@Override
		public RBIT jobResourceBitToFetch() {
			Job j = SETT.JOBS().getter.get(blocked);
			if (j != null) {
				return j.jobResourceBitToFetch();
			}
			return null;
		}

		@Override
		public int jobResourcesNeeded(Humanoid skill) {
			Job j = SETT.JOBS().getter.get(blocked);
			if (j != null) {
				return j.jobResourcesNeeded(skill);
			}
			return 1;
		}
		
		@Override
		public double jobPerformTime(Humanoid a) {
			Job j = SETT.JOBS().getter.get(blocked);
			if (j != null) {
				return j.jobPerformTime(a);
			}
			return 1;
		}

		@Override
		public void jobStartPerforming() {
			
		}

		@Override
		public RESOURCE jobPerform(Humanoid skill, RESOURCE r, int rAm) {
			Job j = SETT.JOBS().getter.get(blocked);
			if (j != null) {
				return j.jobPerform(skill, r, rAm);
			}
			return null;
		}

		@Override
		public COORDINATE jobCoo() {
			return coo;
		}

		@Override
		public CharSequence jobName() {
			Job j = SETT.JOBS().getter.get(blocked);
			if (j != null)
				return j.jobName();
			return Dic.empty;
		}

		@Override
		public boolean jobUseTool() {
			Job j = SETT.JOBS().getter.get(blocked);
			if (j != null)
				return j.jobUseTool();
			return false;
		}

		@Override
		public SoundRace jobSound() {
			Job j = SETT.JOBS().getter.get(blocked);
			if (j != null)
				return j.jobSound();
			return null;
		}
		
	}

	@Override
	public void save(FilePutter file) {
		uper.save(file);
		for (BlockedJob j : active) {
			file.i(j.ID);
			j.coo.save(file);
			j.blocked.save(file);
			file.bool(true);
		}
		for (BlockedJob j : free) {
			file.i(j.ID);
			j.coo.save(file);
			j.blocked.save(file);
			file.bool(false);
		}
	}


	@Override
	public void load(FileGetter file) throws IOException {
		uper.load(file);
		active.clearSloppy();
		free.clearSloppy();
		for (int i = 0; i < all.length; i++) {
			BlockedJob j = all[file.i()];
			j.coo.load(file);
			j.blocked.load(file);
			if (file.bool())
				active.add(j);
			else
				free.add(j);
		}
		
	}


	@Override
	public void clear() {
		active.clearSloppy();
		free.clearSloppy();
		for (BlockedJob j : all)
			free.add(j);
	}
}
