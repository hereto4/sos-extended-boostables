package settlement.overlay;

import settlement.environment.SettEnvMap.SettEnv;
import snake2d.Renderer;
import util.rendering.RenderData.RenderIterator;

class Env extends Addable{

	final SettEnv envThing;
	
	Env(SettEnv env, boolean above) {
		super(env.icon, env.key, env.info.name, env.info.desc, true, above);
		this.envThing = env;
	}
	
	@Override
	public void renderBelow(Renderer r, RenderIterator it) {
		renderUnder(envThing.getView(it.tx(), it.ty()), r, it);
	}


	
}
