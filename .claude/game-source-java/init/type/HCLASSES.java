package init.type;

import init.sprite.SPRITES;
import init.sprite.UI.Icon;
import snake2d.util.color.ColorImp;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.KeyMap;
import snake2d.util.sets.LIST;
import util.keymap.RMAPS;
import util.text.D;

public class HCLASSES {
	
	{
		D.gInit(this);
	}
	
	private final ArrayListGrower<HCLASS> all = new ArrayListGrower<>();
	private final ArrayListGrower<HCLASS> allP = new ArrayListGrower<>();
	
	private final KeyMap<HCLASS> map = new KeyMap<>();
	private final HCLASS NOBLE = new HCLASS(all, allP,
			"NOBLE",
			D.g("Noble"), D.g("Nobilities"), 
			D.g("NobilityD", "The Nobility are the top social layer of your kingdom. They do not work traditionally and demand a salary amongst high tier services. The rewards for having nobles around can be great however."),
			true, new ColorImp(3,1,19)) {
		
		@Override
		public Icon icon() {
			return SPRITES.icons().m.noble;
		}

		@Override
		public Icon iconSmall() {
			return SPRITES.icons().s.noble;
		}
		
	};
	private final HCLASS CITIZEN = new HCLASS(all, allP,
			"CITIZEN",
			D.g("Plebeian"), D.g("Plebeians"), 
			D.g("PlebeianD", "Plebeians are the bulk of your population and will carry out your wishes."),
			true, new ColorImp(3,1,19)) {
		
		@Override
		public Icon icon() {
			return SPRITES.icons().m.citizen;
		}

		@Override
		public Icon iconSmall() {
			return SPRITES.icons().s.citizen;
		}
		
	};
	private final HCLASS SLAVE = new HCLASS(all, allP,
			"SLAVE",
			D.g("Slave"), D.g("Slaves"), 
			D.g("SlaveD", "Slaves do mundane and hard work, but need little in return. They can not be trained into soldiers, or be educated, or replicated, but are gained through processing your captives. If you mistreat slaves they may revolt."),
			true, new ColorImp(20,20,8)) {
		
		@Override
		public Icon icon() {
			return SPRITES.icons().m.slave;
		}

		@Override
		public Icon iconSmall() {
			return SPRITES.icons().s.slave;
		}
		
	};
	private final HCLASS CHILD = new HCLASS(all, allP,
			"CHILD",
			D.g("Child"), D.g("Children"), 
			D.g("ChildD", "Children are to-be citizens. They require food and protection from a manned nursery and can be educated in a school. They will sometimes run around causing mischief."),
			true, new ColorImp(3,1,19)) {
		
		@Override
		public Icon icon() {
			return SPRITES.icons().m.citizen;
		}

		@Override
		public Icon iconSmall() {
			return SPRITES.icons().s.citizen;
		}
		
	};
	
	{
		for (HCLASS cl : all)
			map.put(cl.key, cl);
	}
	
	private final HCLASS OTHER = new HCLASS(all, allP,
			"OTHER",
			"Other", "Others", 
			"",
			false, new ColorImp(20,20,8)) {
		
		@Override
		public Icon icon() {
			return SPRITES.icons().m.citizen;
		}

		@Override
		public Icon iconSmall() {
			// TODO Auto-generated method stub
			return null;
		}
		
	};
	private final RMAPS<HCLASS> MAP = new RMAPS<HCLASS>("CLASS", all);
	
	HCLASSES() {
		self = this;
	}
	
	private static HCLASSES self;
	
	public static HCLASS NOBLE() {
		return self.NOBLE;
	}
	public static HCLASS CITIZEN() {
		return self.CITIZEN;
	}
	public static HCLASS SLAVE() {
		return self.SLAVE;
	}
	public static HCLASS CHILD() {
		return self.CHILD;
	}
	public static HCLASS OTHER() {
		return self.OTHER;
	}
	public static RMAPS<HCLASS> MAP() {
		return self.MAP;
	}

	public static LIST<HCLASS> ALL(){
		return self.all;
	}
	
	public static LIST<HCLASS> ALLP(){
		return self.allP;
	}
	
}
