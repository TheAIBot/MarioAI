package tickbased.game.world;

import java.awt.*;
import java.util.Random;
import ch.idsia.mario.engine.level.*;


public class LevelRenderer
{
    private int xCam;
    private int yCam;
    private Image image;
    private Graphics2D g;
    private static final Color transparent = new Color(0, 0, 0, 0);
    private Level level;

    private Random random = new Random();
    public boolean renderBehaviors = false;

    public int width;
    public int height;

    public LevelRenderer(Level level, GraphicsConfiguration graphicsConfiguration, int width, int height)
    {
        this.width = width;
        this.height = height;

        this.level = level;
        image = graphicsConfiguration.createCompatibleImage(width, height, Transparency.BITMASK);
        g = (Graphics2D) image.getGraphics();
        g.setComposite(AlphaComposite.Src);

    }
}