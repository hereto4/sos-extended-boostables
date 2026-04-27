package settlement.thing;

import settlement.misc.util.FINDABLE;
import settlement.path.finders.SFinderFindable;
import settlement.thing.THINGS.Thing;

public abstract class ThingFindable extends Thing implements FINDABLE{

	public ThingFindable(int index) {
		super(index);
	}

	@Override
	public void findableReserve() {
		if (!findableReservedCanBe())
			throw new RuntimeException();
		reserve(1);
		if (!findableReservedCanBe())
			finder().report(this, -1);
	}
	
	protected abstract void reserve(int delta);

	@Override
	public void findableReserveCancel() {
		if (!findableReservedIs())
			return;
		if (findableReservedCanBe())
			return;
		reserve(-1);
		finder().report(this, 1);
	}
	
	public abstract SFinderFindable finder();

	@Override
	protected void addAction() {
		if (findableReservedCanBe())
			finder().report(this, 1);
	}
	
	@Override
	protected void removeAction() {
		if (findableReservedCanBe())
			finder().report(this, -1);
	}
	

}
