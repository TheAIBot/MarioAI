package ch.idsia.ai.agents.human;

import ch.idsia.ai.agents.Agent;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Sergey Karakovskiy
 * Date: Mar 29, 2009
 * Time: 12:19:49 AM
 * Package: ch.idsia.ai.agents.ai;
 */
public class HumanKeyboardAgent extends KeyAdapter implements Agent
{
    List<boolean[]> history = new ArrayList<boolean[]>();
    private boolean[] Action = null;
    private String Name = "HumanKeyboardAgent";

    public HumanKeyboardAgent()
    {
        this.reset ();
//        RegisterableAgent.registerAgent(this);
    }

    public void reset()
    {
        // Just check you keyboard. Especially arrow buttons and 'A' and 'S'!
        Action = new boolean[Environment.numberOfButtons];
    }

    public boolean[] getAction(Environment observation)
    {
        float[] enemiesPos = observation.getEnemiesFloatPos();
        System.out.println(observation.getMarioFloatPos()[0] / 16 + " " + observation.getMarioFloatPos()[1] / 16);
        //System.out.println(observation.getCompleteObservation().length + " " + observation.getCompleteObservation()[0].length);
        /*byte[][] level = observation.getCompleteObservation();
        for (int x = 0; x < level.length; x++) {
			for (int y = 0; y < level[0].length; y++) {
				System.out.print((char)(level[x][y] + 50));
			}
			System.out.println("");
		}
        System.out.println();
        */
        return Action;
    }

    public AGENT_TYPE getType() {        return AGENT_TYPE.HUMAN;    }

    public String getName() {   return Name; }

    public void setName(String name) {        Name = name;    }


    public void keyPressed (KeyEvent e)
    {
        toggleKey(e.getKeyCode(), true);
    }

    public void keyReleased (KeyEvent e)
    {
        toggleKey(e.getKeyCode(), false);
    }


    private void toggleKey(int keyCode, boolean isPressed)
    {
        switch (keyCode) {
            case KeyEvent.VK_LEFT:
                Action[Mario.KEY_LEFT] = isPressed;
                break;
            case KeyEvent.VK_RIGHT:
                Action[Mario.KEY_RIGHT] = isPressed;
                break;
            case KeyEvent.VK_DOWN:
                Action[Mario.KEY_DOWN] = isPressed;
                break;

            case KeyEvent.VK_S:
                Action[Mario.KEY_JUMP] = isPressed;
                break;
            case KeyEvent.VK_A:
                Action[Mario.KEY_SPEED] = isPressed;
                break;
        }
    }

   public List<boolean[]> getHistory () {
       return history;
   }
}
