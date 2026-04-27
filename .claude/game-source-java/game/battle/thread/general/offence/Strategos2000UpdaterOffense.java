package game.battle.thread.general.offence;

import java.io.IOException;

import game.battle.div.Div;
import game.battle.formation.DIV_FORMATION;
import game.battle.thread.general.Strategos2000Updater;
import game.battle.thread.general.StrategosUtil;
import game.battle.thread.general.offence.ContextLines.Line;
import game.battle.thread.order.BattleOrderTask;
import game.battle.thread.trajectory.BattleTrajectories;
import init.constant.C;
import init.constant.Config;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LIST;
import util.rendering.RenderData;
import util.rendering.RenderData.RenderIterator;
import util.rendering.ShadowBatch;

public class Strategos2000UpdaterOffense extends Strategos2000Updater {

	private final LIST<State> states;
	private final StrategosUtil u;
	private int state;
	private final Context c;
	
	public Strategos2000UpdaterOffense(StrategosUtil u){
		this.u = u;
		this.c = new Context();
		states = attack();
	}
	
	@Override
	public void clear() {
		state = 0;
		c.clear();
	}
	
	@Override
	public void save(FilePutter file) {
		file.i(state);
		c.save(file);
	}

	@Override
	public void load(FileGetter file) throws IOException {
		state = file.i();
		c.load(file);
	}
	
	@Override
	public boolean update() {

		if (state >= states.size()) {
			state = 0;
			return false;
		}
		
		@SuppressWarnings("unused")
		int s = state;
		long millis = System.currentTimeMillis();
		
		if (!states.get(state).is()) {
			state++;
		}
		millis = System.currentTimeMillis()-millis;
		//LOG.ln(states.get(s).name + " " + millis);
		return true;
		
	}
	

	
	@Override
	public void render(Renderer r, RenderIterator it) {
		
		
		

		
		if (c.blob.is(it.tile())) {
			COLOR.ORANGE100.bind();
			SPRITES.cons().BIG.outline.render(r, 0, it.x(), it.y());
		}
		if (c.block.is(it.tile())) {
			COLOR.WHITE50.bind();
			UI.icons().s.cancel.renderScaled(r, it.x(), it.y(), C.SCALE);
		}
		
		COLOR.unbind();
		//context.preLines.render(r, it);
	}
	
	@Override
	public void render(Renderer r, ShadowBatch shadowBatch, RenderData data) {
		for (int li = 0; li < c.lines.lines(); li++) {
			Line l = c.lines.get(li);
			int ox = data.offX1();
			int oy = data.offY1();
			
			for (int s = 0; s <= l.length; s+=C.TILE_SIZEH) {
				int cx = (int) (l.sx+l.dx*s-ox);
				int cy = (int) (l.sy+l.dy*s-oy);
				
				UI.icons().s.dot.renderC(r, cx, cy);
			}
			
			
		}
		
	}
	
	private LIST<State> attack(){
		ArrayListGrower<State> states = new ArrayListGrower<>();
		
		states.add(new State("clear") {
			
			private final BattleOrderTask task = new BattleOrderTask();
			
			@Override
			public boolean is() {
				c.deployedToLine.clear();
				
				for (int di = 0; di < Config.battle().DIVISIONS_PER_ARMY; di++) {
					Div d = u.getArmy().divisions().get(di);
					if (d.active() && d.settings().ammo() != null && BattleTrajectories.trajectories(d) > d.men()/2) {
						d.settings().fireAtWill = true;
						d.settings().formation = DIV_FORMATION.LOOSE;
						task.stop(d);
						d.order().task.set(task);
						c.deployedToLine.set(di, true);
					}
				}
				
				return false;
			}
			
		});	
		states.add(new State("bombard") {
			
			StepArtilleryBombard b = new StepArtilleryBombard(u);
			
			@Override
			public boolean is() {
				b.bombard();
				return false;
			}
		});
		states.add(new State("blob") {
			
			StepBlob s = new StepBlob(u);
			
			@Override
			public boolean is() {
				s.update(c.blob, 24);
				return false;
			}
		});
		states.add(new State("throne") {
			//StepEarlyThrone t = new StepEarlyThrone(u, c);
			@Override
			public boolean is() {
				//t.setToThrone();
				return false;
			}
			
		});	
		
		
		states.add(new State("make lines") {

			StepLinesMaker s = new StepLinesMaker(u, c);
			
			@Override
			public boolean is() {
				s.make();
				return false;
			}
		});	
		
		StepLinesChecker check = new StepLinesChecker(u, c);
		states.add(new State("checkLines init") {
			@Override
			public boolean is() {
				
				check.init();
				return false;
			}
		});	
		states.add(new State("checkLines") {

			@Override
			public boolean is() {
				return check.check();
			}
			
		});	
		
		states.add(new State("blockLines") {

			StepLinesBlocker s = new StepLinesBlocker(u, c);
			
			@Override
			public boolean is() {
				s.make();
				return false;
			}
		});	
		
		StepLinesBacker back = new StepLinesBacker(u, c);
		states.add(new State("back lines") {

			@Override
			public boolean is() {
				back.init();
				return false;
			}
			
		});	
		states.add(new State("back lines 2") {

			@Override
			public boolean is() {
				return back.retreatThroneLine();
			}
			
		});	
		
		
		StepLinesMoveTo line = new StepLinesMoveTo(u, c);
		states.add(new State("line move init") {

			@Override
			public boolean is() {
				line.init();
				return false;
			}
			
		});	
		states.add(new State("line move 1") {

			@Override
			public boolean is() {
				return line.deployDivsToLine();
			}
			
		});	
		states.add(new State("line move 2") {

			@Override
			public boolean is() {
				return line.deployDivsToLineRanged();
			}
			
		});	
		states.add(new State("line move3") {

			@Override
			public boolean is() {
				line.setSpeedAndFormation();
				
				return false;
			}
			
		});	
		
		StepLineCharge charge = new StepLineCharge(u, c);
		states.add(new State("charge 1") {
			
			@Override
			public boolean is() {
				charge.init();
				return false;
			}
			
		});	
		states.add(new State("charge 2") {
			
			@Override
			public boolean is() {
				return charge.charge();
			}
			
		});	
		
		
		StepMoveToThrone throne = new StepMoveToThrone(u, c);
		states.add(new State("throne init") {
			
			@Override
			public boolean is() {
				throne.init();
				return false;
			}
			
		});	
		states.add(new State("throne") {
			
			@Override
			public boolean is() {
				return throne.setToThrone();
			}
			
		});	
		
		states.add(new State("attack") {
			StepAttackEnemyNear s = new StepAttackEnemyNear(u, c);
			@Override
			public boolean is() {
				return s.attackEnemies();
			}
			
		});	
		
		
		

		
		StepAttackOthers kite = new StepAttackOthers(u, c);
		states.add(new State("kite init") {
			
			@Override
			public boolean is() {
				kite.init();
				return false;
			}
			
		});	
		states.add(new State("kite") {
			
			@Override
			public boolean is() {
				return kite.attack();
			}
			
		});	
		
		
//		states.add(new BOOLEAN() {
//
//			@Override
//			public boolean is() {
//				debugWait = 120;
//				return false;
//			}
//			
//		});	
		
		

		

		
		return states;
	}
	
	
	private abstract static class State {
		
		@SuppressWarnings("unused")
		private final String name;
		
		State(String name){
			this.name = name;
		}
		
		public abstract boolean is();
		
	}



}
