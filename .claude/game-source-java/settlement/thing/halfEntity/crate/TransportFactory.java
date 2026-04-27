package settlement.thing.halfEntity.crate;

import java.io.IOException;

import init.resources.RESOURCE;
import settlement.entity.humanoid.Humanoid;
import settlement.thing.halfEntity.Factory;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sets.LISTE;

public class TransportFactory extends Factory<TransportEntity>{

	public final Sprite sprite;
	
	
	public TransportFactory(LISTE<Factory<?>> all) throws IOException {
		super(all);
		sprite = new Sprite();
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
	protected TransportEntity make() {
		return new TransportEntity();
	}

	public void make(Humanoid h, int tx, int ty, RESOURCE res, byte ran, boolean mil) {
		TransportEntity e = create();
		e.init(h, tx, ty, res, ran, mil);
	}
	
}
