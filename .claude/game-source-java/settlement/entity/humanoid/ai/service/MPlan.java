package settlement.entity.humanoid.ai.service;

import init.settings.S;
import init.type.NEED;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AIPLAN;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.misc.util.FSERVICE;
import settlement.room.service.module.RoomServiceAccess.ROOM_SERVICE_ACCESS_HASER;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.text.Str;

public abstract class MPlan<T extends ROOM_SERVICE_ACCESS_HASER> extends AIPLAN.PLANRES{

	static int dist;
	public final LIST<T> services;
	private final boolean include;
	public MPlan(String key, LIST<T> services, boolean include) {
		super("SPlan_" + key);
		this.services = services;
		this.include = include;
	}
	
	@Override
	protected final AISubActivation init(Humanoid a, AIManager d) {
		return walk.set(a, d);
	}
	
	private final Resumer walk = new Resumer("Walk") {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			AISubActivation s = null;
			
			if (include)
				s = AI.SUBS().walkTo.serviceInclude(a, d, blue(d).service(), dist);
			else
				s = AI.SUBS().walkTo.service(a, d, blue(d).service().finder, dist);
			if (s == null)
				return null;
			d.planTile.set(d.path.destX(), d.path.destY());
			blue(d).service().reportAccess(a, d.planTile);
			blue(d).service().reportDistance(a);
			blue(d).service().reportAccess(a, d.planTile);
			return s;
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			return arrive(a, d);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return true;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			
		}
	};
	
	protected abstract AISubActivation arrive(Humanoid a, AIManager d);
	
	protected T blue(AIManager d) {
		return services.get(d.planByte3);
	}
	
	@Override
	protected void name(Humanoid a, AIManager d, Str string) {
		
		string.add(blue(d).service().verb);
		if (S.get().debug) {
			string.s().add('(');
			super.name(a, d, string);
			string.add(')');
		}
	}

	@Override
	protected void cancel(Humanoid a, AIManager d) {
		//blue(d).service().clearAccess(a);
		super.cancel(a, d);
	}

	protected FSERVICE get(Humanoid a, AIManager d) {
		T blue = blue(d);
		if (blue != null)
			return blue.service().service(d.planTile.x(), d.planTile.y());
		return null;
	}

	public NEED need(AIManager d) {
		return blue(d).service().need;
	}
	
}