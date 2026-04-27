package init.type;

import init.sprite.UI.UI;
import snake2d.util.color.ColorImp;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.KeyMap;
import snake2d.util.sets.LIST;
import util.keymap.RMAPS;
import util.text.D;
import util.text.Dic;

public final class HTYPES {

	private final ArrayListGrower<HTYPE> all = new ArrayListGrower<HTYPE>();
	
	{
		D.gInit(this);
	}
	
	private final HTYPE SUBJECT = new HTYPE(all, "CITIZEN",
			HCLASSES.CITIZEN(),
			D.g("Citizen"), D.g("Citizens"), 
			D.g("CitizenD", "Citizens are the bulk of your population and will carry out your wishes."),
			true, true, false, new ColorImp(3,1,19),
			UI.icons().s.typeCitizen.createColored(new ColorImp(50, 255, 255)));
	private final HTYPE RETIREE = new HTYPE(all, "RETIREE",
			HCLASSES.CITIZEN(),
			D.g("Retiree"), D.g("Retirees"), 
			D.g("RetireeD", "Retired people are citizens that have served you for many years and are now entitled to some relaxation their final years. They do not work."),
			true, false, false, new ColorImp(new ColorImp(8, 20, 20)),
			UI.icons().s.typeRetire.createColored(new ColorImp(255, 128, 0)));
	private final HTYPE RECRUIT = new HTYPE(all, "RECRUIT",
			HCLASSES.CITIZEN(),
			D.g("Recruit"), D.g("Recruits"), 
			D.g("RecruitD", "Recruits are citizens either training their combat skills for a place in a division, or honing these skills towards the limit you've set for said division."),
			true, false, false, new ColorImp(20,8,16),
			UI.icons().s.typeRecruit.createColored(new ColorImp(50, 255, 128)));
	private final HTYPE STUDENT = new HTYPE(all, "STUDENT",
			HCLASSES.CITIZEN(),
			D.g("Student"), D.g("Students"), 
			D.g("StudentD", "Students are citizens currently attending university. They do not count towards your workforce."),
			true, false, false, new ColorImp(20,8,16),
			UI.icons().s.typeStudent.createColored(new ColorImp(50, 128, 255)));
	private final HTYPE PRISONER = new HTYPE(all, "PRISONER",
			HCLASSES.OTHER(),
			D.g("Prisoner"), D.g("Prisoners"), 
			D.g("PrisonerD", "Prisoners are caught criminals, or POWs. Prisoners will spend their time in your dungeons. They can be used as sacrifices in temples, or gladiators. They can also be enslaved, or executed."),
			false, false, false, new ColorImp(20,20,8),
			UI.icons().s.typePrison.createColored(new ColorImp(200, 200, 200)));
	private final HTYPE TOURIST = new HTYPE(all, "TOURIST",
			HCLASSES.OTHER(),
			Dic.¤¤Tourist, Dic.¤¤Tourists, 
			D.g("TouristD", "Tourists are foreigners visiting your city in search of a spectacle. Treat them well, and they will show their appreciation by tossing you some coins."),
			false, false, false, new ColorImp(20,20,8),
			UI.icons().s.typeTourist.createColored(new ColorImp(128, 128, 255)));
	
	private final HTYPE SOLDIER = new HTYPE(all, "SOLDIER",
			HCLASSES.CITIZEN(),
			D.g("Soldier"), D.g("Soldiers"), 
			D.g("SoldierD", "Soldiers are men on the battlefield."),
			true, false, false, false, new ColorImp(3,1,19),
			UI.icons().s.typeSoldier.createColored(new ColorImp(50, 128, 255)));
	private final HTYPE ENEMY = new HTYPE(all, "ENEMY",
			HCLASSES.OTHER(),
			D.g("Enemy"), D.g("Enemies"),
			D.g("EnemyD", "Enemies are hostile peoples, bent on destroying your rule"),
			false, false, true, new ColorImp(30,1,1),
			UI.icons().s.typeSoldier.createColored(new ColorImp(255, 50, 50)));
	private final HTYPE RIOTER = new HTYPE(all, "RIOTER",
			HCLASSES.OTHER(),
			D.g("Rioter"), D.g("Rioters"),
			D.g("RioterD", "Rioters are former citizens, who have had enough of your rule and express their disappointment by burning your city to ashes."),
			false, false, true, new ColorImp(30,1,1),
			UI.icons().s.typeRioter.createColored(new ColorImp(255, 50, 50)));
	private final HTYPE DERANGED = new HTYPE(all, "DERANGED",
			HCLASSES.OTHER(),
			D.g("Deranged"), D.g("Derangeds", "Deranged"),
			D.g("DerangedD", "Deranged are people who have gone insane. They will do no work, and wander around your city doing erratic things. Can be cured in an asylum."),
			false, false, false, new ColorImp(30,30,1),
			UI.icons().s.typeCrazy.createColored(new ColorImp(255, 255, 50)));
	private final HTYPE NOBILITY = new HTYPE(all, "NOBILITY",
			HCLASSES.NOBLE(),
			D.g("Nobility"), D.g("Nobles"),
			D.g("NobilityD", "The nobility are above the common plebs. Do not work in a traditional sense and require the best of services."),
			true, false, false, new ColorImp(20, 8, 20),
			UI.icons().s.noble.createColored(new ColorImp(255, 50, 255)));
	private final HTYPE SLAVE = new HTYPE(all, "SLAVE",
			HCLASSES.SLAVE(),
			D.g("Slave"), D.g("Slaves"), 
			D.g("SlaveD", "Slaves do mundane and hard work, but need little in return. They can not be trained into soldiers, or be educated, or replicated, but are gained through processing your captives. If you mistreat slaves they may revolt."),
			true, true, false, new ColorImp(20, 20, 20),
			UI.icons().s.slave.createColored(new ColorImp(200, 200, 200)));
	
	private final HTYPE CHILD = new HTYPE(all, "CHILD",
			HCLASSES.CHILD(),
			D.g("Child"), D.g("Children"), 
			D.g("ChildD", "Children require staffed nurseries to grow up to become subjects. Children can be educated by schools."),
			true, false, false, new ColorImp(20, 20, 20),
			UI.icons().s.typeChild.createColored(new ColorImp(50, 255, 50)));
	
	private final RMAPS<HTYPE> map;
	
	private static HTYPES self;
	
	HTYPES(HCLASSES cls) {
		self = this;
		
		KeyMap<HTYPE> mm = new KeyMap<HTYPE>();
		for (HTYPE h : all)
			mm.put(h.key, h);
		
		map = new RMAPS<HTYPE>("HTYPE", all);
	}
	
	public static RMAPS<HTYPE> MAP(){
		return self.map;
	}
	
	public static LIST<HTYPE> ALL(){
		return self.all;
	}

	public static HTYPE SUBJECT() {
		return self.SUBJECT;
	}

	public static HTYPE RETIREE() {
		return self.RETIREE;
	}

	public static HTYPE RECRUIT() {
		return self.RECRUIT;
	}

	public static HTYPE STUDENT() {
		return self.STUDENT;
	}

	public static HTYPE PRISONER() {
		return self.PRISONER;
	}

	public static HTYPE TOURIST() {
		return self.TOURIST;
	}

	public static HTYPE SOLDIER() {
		return self.SOLDIER;
	}

	public static HTYPE ENEMY() {
		return self.ENEMY;
	}

	public static HTYPE RIOTER() {
		return self.RIOTER;
	}

	public static HTYPE DERANGED() {
		return self.DERANGED;
	}

	public static HTYPE NOBILITY() {
		return self.NOBILITY;
	}

	public static HTYPE SLAVE() {
		return self.SLAVE;
	}

	public static HTYPE CHILD() {
		return self.CHILD;
	}

}
