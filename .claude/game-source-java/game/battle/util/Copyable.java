package game.battle.util;

import snake2d.util.file.SAVABLE;

public interface Copyable<T> extends SAVABLE {

	void copy(T toBeCopied);
	
}
