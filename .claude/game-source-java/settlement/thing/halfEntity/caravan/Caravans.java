package settlement.thing.halfEntity.caravan;

import java.io.IOException;
import java.util.Arrays;

import init.resources.RESOURCE;
import init.resources.RESOURCES;
import settlement.main.SETT;
import settlement.thing.halfEntity.Factory;
import settlement.thing.halfEntity.HalfEntity;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sets.LISTE;

public class Caravans extends Factory<Caravan>{

	private final Type export = new TypeExport();
	private final Type delivery = new TypeDelivier();
	private final Type delivery_throne = new TypeDelivierStorage();
	private int[] tmpSold = new int[RESOURCES.ALL().size()];
	
	public Caravans(LISTE<Factory<?>> all) {
		super(all);
	}

	@Override
	protected void save(FilePutter file) {
		RESOURCES.map().saver().save(tmpSold, file);
	}

	@Override
	protected void load(FileGetter file) throws IOException {
		RESOURCES.map().loader().load(tmpSold, file, 0);
	}

	
	@Override
	protected void clear() {
		Arrays.fill(tmpSold, 0);
	}

	@Override
	protected Caravan make() {
		return new Caravan();
	}
	
	public boolean createFetcher(RESOURCE res, int amount) {
		return create(res, amount, export, true);
	}

	public boolean createDelivery(RESOURCE res, int amount, boolean dump) {
		if (create(res, amount, delivery, false))
			return true;
		if (dump)
			return create(res, amount, delivery_throne, true);
		return false;
	}
	
	private boolean create(RESOURCE res, int amount, Type type, boolean dump) {
		COORDINATE coo = SETT.ENTRY().points.randomReachable();
		if (coo == null)
			return false;
		
		Caravan c = create();
		if (c.init(coo.x(), coo.y(), type, res, amount)) {
			return true;
		}else {
			type.cancel(c, false);
		}
		return false;
	}

	public int getExport(RESOURCE res) {
		int am = 0;
		for (HalfEntity e : SETT.HALFENTS().all()) {
			if (e instanceof Caravan) {
				Caravan c = (Caravan) e;
				if (c.type() == export) {
					am += c.reservedGlobally;
				}
			}
			
		}
		return am;
	}
	
	
	

}
