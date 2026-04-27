package game.faction.player.emmi;

import java.util.Arrays;

import game.faction.FACTIONS;
import game.faction.npc.FactionNPC;
import game.faction.royalty.NPCCourt;
import game.faction.royalty.Royalty;
import snake2d.util.sprite.SPRITE;

public abstract class EmiTypeRoy extends EmiType<Royalty> {

	private final int[] ftot = new int[FACTIONS.MAX()];

	EmiTypeRoy(SPRITE icon, CharSequence name, CharSequence desc) {
		super(icon, name, desc, FACTIONS.MAX()*NPCCourt.MAX, 300);
	}
	
	public int total(FactionNPC f) {
		return ftot[f.index()];
	}

	@Override
	void count(int index, int am) {
		ftot[index/NPCCourt.MAX] += am;
		super.count(index, am);
	}

	@Override
	void clear() {
		Arrays.fill(ftot, 0);
		super.clear();
	}

	@Override
	int index(Royalty t) {
		return t.court.faction.index()*NPCCourt.MAX + t.successionI();
	}
	
	void clear(FactionNPC f){
		int k = f.index()*NPCCourt.MAX;
		for (int i = 0; i < NPCCourt.MAX; i++) {
			set(k+i, 0);
		}
	}

}