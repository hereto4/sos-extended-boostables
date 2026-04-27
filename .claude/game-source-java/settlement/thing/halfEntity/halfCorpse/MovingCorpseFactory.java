package settlement.thing.halfEntity.halfCorpse;

import java.io.IOException;

import init.type.CAUSE_LEAVE;
import settlement.entity.humanoid.Humanoid;
import settlement.thing.halfEntity.Factory;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sets.LISTE;

public class MovingCorpseFactory extends Factory<MovingCorpse>{

	public MovingCorpseFactory(LISTE<Factory<?>> all) {
		super(all);
		
	}

	@Override
	protected void save(FilePutter file) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void load(FileGetter file) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void clear() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected MovingCorpse make() {
		return new MovingCorpse();
	}

	public void make(Humanoid h, boolean gore, CAUSE_LEAVE l) {
		MovingCorpse e = create();
		e.init(h, gore, l);
	}
	
}
