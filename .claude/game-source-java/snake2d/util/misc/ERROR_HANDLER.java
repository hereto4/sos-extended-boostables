package snake2d.util.misc;

import snake2d.Errors.DataError;
import snake2d.Errors.GameError;

public interface ERROR_HANDLER {

	void handle(String output, String dump);
	void handle(Throwable e, String dump);
	void handle(DataError e, String dump);
	void handle(GameError e, String dump);
	
}
