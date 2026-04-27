package settlement.entity.humanoid.ai.main;

import settlement.entity.humanoid.HEvent.HEventData;
import settlement.entity.humanoid.HPoll.HPollData;
import settlement.entity.humanoid.Humanoid;

public interface HEventListener {
	
	public boolean event(Humanoid a, AIManager d, HEventData e);
	public double poll(Humanoid a, AIManager d, HPollData e);

}