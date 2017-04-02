package MarioAI.astar;

import java.util.List;

public interface State {
	
	int id = 0;
	
	//List<Action> actions;
	List<Action> getPossibleActions();

}
