package settlement.room.knowledge.university;

import java.io.IOException;
import java.util.Arrays;

import game.boosting.BOOSTABLE_O;
import game.time.TIME;
import init.race.RACES;
import init.race.Race;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.path.finders.SFinderRoomService;
import settlement.room.main.RoomBlueprintIns;
import settlement.room.main.RoomInstance;
import settlement.room.main.category.RoomCategorySub;
import settlement.room.main.employment.RoomEmploymentSimple.EmployerSimple;
import settlement.room.main.furnisher.Furnisher;
import settlement.room.main.util.RoomInitData;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.LISTE;
import util.data.BOOLEAN;
import util.data.DOUBLE_O.DOUBLE_OE;
import util.info.INFO;
import util.text.D;
import view.sett.ui.room.UIRoomModule;

public final class ROOM_UNIVERSITY extends RoomBlueprintIns<UniversityInstance>{

	
	
	final UniversityConstructor constructor;
	public final double learningSpeed;
	final Job job = new Job(this);
	private double[] limits = new double[RACES.all().size()];
	private static CharSequence ¤¤iName = "Limit";
	private static CharSequence ¤¤iDesc = "The maximum education to teach. Subjects that are above this knowledge limit will not attend this facility.";
	
	private static CharSequence ¤¤bonus = "Learning speed of: ";
	
	static {
		D.ts(ROOM_UNIVERSITY.class);
	}
	
	public final EmployerSimple emp = new EmployerSimple(employment());
	
	public ROOM_UNIVERSITY(String key, int index, RoomInitData init, RoomCategorySub block) throws IOException {
		super(index, init, key, block);
	

		constructor = new UniversityConstructor(this, init);
		learningSpeed = init.data().d("LEARNING_SPEED", 0, 1);
		clearP();
		pushBo(init.data(), info.name, ¤¤bonus + ": " + info.name, "UNIVERSITY", true);
	}
	
	@Override
	protected void update(double ds) {

	}

	@Override
	public SFinderRoomService service(int tx, int ty) {
		return null;
	}

	@Override
	protected void saveP(FilePutter f){
		f.ds(limits);
	}
	
	@Override
	protected void loadP(FileGetter f) throws IOException{
		f.ds(limits);
		
	}
	
	@Override
	protected void clearP() {
		Arrays.fill(limits, 0.75);
	}
	
	@Override
	public Furnisher constructor() {
		return constructor;
	}
	
	@Override
	public void appendView(LISTE<UIRoomModule> mm) {
		mm.add(new Gui(this).make());
	}
	
	public BOOLEAN isTime = new BOOLEAN() {

		@Override
		public boolean is() {
			return TIME.days().bitPartOf() > employment().getShiftStart() && TIME.days().bitPartOf() < employment().getShiftStart()+Humanoid.WORK_PER_DAY;
		}
		
	};
	
	public double learningSpeed(RoomInstance i, BOOSTABLE_O h) {
		UniversityInstance ins = (UniversityInstance)i;
		return learningSpeed*(1.0-ins.getDegrade())*constructor.quality.get(ins)*bonus().get(h);
	}
	
	public final DOUBLE_OE<Race> limit = new DOUBLE_OE<Race>() {

		private final INFO info = new INFO(¤¤iName, ¤¤iDesc);
		
		@Override
		public double getD(Race t) {
			if (t == null) {
				double lim = 0;
				for (Race r : RACES.all()) {
					lim = Math.max(limits[r.index()], lim);
				}
				return lim;
			}
			return limits[t.index()];
		}

		@Override
		public DOUBLE_OE<Race> setD(Race t, double d) {
			if (t == null) {
				for (Race r : RACES.all())
					setD(r, d);
				return this;
			}
			limits[t.index] = CLAMP.d(d, 0, 1);
			return this;
		}
		
		@Override
		public INFO info() {
			return info;
		};
	
	};

	
	public boolean isLecturer(COORDINATE c) {
		return SETT.ROOMS().fData.tileData.get(c) == UniversityConstructor.IWORKE;
	}
	
	public DIR spotDir(COORDINATE c) {
		return DIR.ORTHO.get(SETT.ROOMS().fData.spriteData.get(c) & 0b011);
	}
	
}
