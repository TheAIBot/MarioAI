package ch.idsia.ai.tasks;

import ch.idsia.ai.agents.Agent;
import ch.idsia.mario.engine.MarioComponent;
import ch.idsia.mario.engine.level.Level;
import ch.idsia.tools.EvaluationOptions;

/**
 * Created by IntelliJ IDEA.
 * User: Sergey Karakovskiy
 * Date: Apr 8, 2009
 * Time: 11:20:41 AM
 * Package: ch.idsia.ai.tasks
 */
public interface Task {
	
	public MarioComponent loadLevel(Level level, Agent controller);
	
	public MarioComponent setRandomLevel(Agent controller);
	
    public double[] evaluate(Agent controller);

    public void setOptions(EvaluationOptions options);

    public EvaluationOptions getOptions();

}
