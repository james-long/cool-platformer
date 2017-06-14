/******
* name: Patrick Au, James Long
* date: March 2017
* code: ICS4U1
* note: Main Game Screen
*******/
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

import javax.swing.*;

class GameScreen extends JPanel implements ActionListener, Runnable, ComponentListener
{
	public static final int FPS = 40; // 20;
	public static final int delay = 1000 / FPS;
	private static final int edW = 1; // Edge Width around Screen
	private static final int WIFW = JComponent.WHEN_IN_FOCUSED_WINDOW;

	private static final String JUMP = "p.jump";
	private static final String MOVE_LEFT = "p.m_left";
	private static final String MOVE_RIGHT = "p.m_right";
	private static final String DOWN = "p.m_down";
	private static final String JUMP_R = "r.jump";
	private static final String MOVE_LEFT_R = "r.m_left";
	private static final String MOVE_RIGHT_R = "r.m_right";
	private static final String DOWN_R = "r.m_down";
	private static final String P_KEY = "p.p";

	private static Block[][] blocks;
	private static Thread gameScreen;
	private static Timer timer;

	// Entity Objects
	private static Player mainChar;
	private static ArrayList<Enemy> enemies;
	private static Vector2 goalPos;

	GameScreen(Dimension dim)
	{
		addComponentListener(this);
		
		blocks = new Block[dim.height / Block.getLen() + edW*2][dim.width / Block.getLen() + edW*2];
		for(int i = 0; i < blocks.length; ++i)
			for(int j = 0; j < blocks[i].length; ++j) // Default Tiling
				blocks[i][j] = new Block((j-edW) * Block.getLen(), (i-edW) * Block.getLen(), Entity.DOWN, 0);

		mainChar = new Player();
		enemies = new ArrayList<Enemy>();

		// Key Bindings on release
		this.getInputMap(WIFW).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0, false), JUMP);
		this.getActionMap().put(JUMP, new SetKeyAction(Entity.UP, true));
		this.getInputMap(WIFW).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0, true), JUMP_R);
		this.getActionMap().put(JUMP_R, new SetKeyAction(Entity.UP, false));
		
		this.getInputMap(WIFW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0, false), DOWN);
		this.getActionMap().put(DOWN, new SetKeyAction(Entity.DOWN, true));
		this.getInputMap(WIFW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0, true), DOWN_R);
		this.getActionMap().put(DOWN_R, new SetKeyAction(Entity.DOWN, false));
		
		this.getInputMap(WIFW).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0, false), MOVE_LEFT);
		this.getActionMap().put(MOVE_LEFT, new SetKeyAction(Entity.LEFT, true));
		this.getInputMap(WIFW).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0, true), MOVE_LEFT_R);
		this.getActionMap().put(MOVE_LEFT_R, new SetKeyAction(Entity.LEFT, false));

		this.getInputMap(WIFW).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0, false), MOVE_RIGHT);
		this.getActionMap().put(MOVE_RIGHT, new SetKeyAction(Entity.RIGHT, true));
		this.getInputMap(WIFW).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0, true), MOVE_RIGHT_R);
		this.getActionMap().put(MOVE_RIGHT_R, new SetKeyAction(Entity.RIGHT, false));
		
		this.getInputMap(WIFW).put(KeyStroke.getKeyStroke(KeyEvent.VK_P, 0, false), P_KEY);
		this.getActionMap().put(P_KEY, new ChangeScreenAction());

		timer = new Timer(delay, this);
	}	// end constructor()

	public void freeze(boolean yesOrNo)
	{
		//Placeholder: all relevant game states should be frozen here
		mainChar.freeze(yesOrNo);
		for(Enemy ene: enemies)
			ene.freeze(yesOrNo);
	}	// end method freeze
	
	public void init(String fileName)
	{
		gameScreen = new Thread(this);

		enemies.clear();
		
		// Hardcoded Enemies
		enemies.add(new NormalEnemy(11 * Block.getLen(),  3 * Block.getLen())); // Left  Field
		enemies.add(new NormalEnemy(21 * Block.getLen(),  5 * Block.getLen())); // Right Field
		enemies.add(new NormalEnemy(16 * Block.getLen(), 16 * Block.getLen())); // Up    Field
		// enemies.add(new NormalEnemy( 5 * Block.getLen(), 11 * Block.getLen())); // Solo Pocket

		// Load level
		StageManager.loadMap(System.getProperty("user.dir") + "/include/levels", fileName, blocks);
		goalPos = StageManager.getGoalPos(System.getProperty("user.dir") + "/include/levels", fileName, blocks);
		goalPos.X *= Block.getLen();
		goalPos.Y *= Block.getLen();

		gameScreen.start();
	}	// end method init

	@Override // Interface: ActionListener
	public void actionPerformed(ActionEvent ae)
	{
		mainChar.advance();
		for(Enemy ene: enemies)
			ene.advance();
		this.repaint();
	}	// end method actionPerformed
	
	public static Block getBlocks(int y, int x)
	{
		return blocks[y+edW][x+edW];
	}	// end method getBlocks

	@Override // Superclass: JPanel
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);

		// Clears JPanel
		g.clearRect(0, 0, getWidth(), getHeight());

		// Draws Blocks
		for(int i = edW; i < blocks.length-edW; ++i)
			for(int j = edW; j < blocks[i].length-edW; ++j)
				blocks[i][j].draw(g, blocks[i-1][j].getBlock()==1);

		// Draws Enemies
		for(Enemy ene: enemies)
			ene.draw(g);
		// Draws Main Character
		mainChar.draw(g);
	}	// end method paintComponent

	public void componentShown(ComponentEvent e)
	{
		freeze(false);
	}
	public void componentHidden(ComponentEvent e)
	{
		freeze(true);
	}
	public void componentResized(ComponentEvent e){   }
	public void componentMoved(ComponentEvent e){   }
	
	@Override // Interface: Runnable
	public void run()
	{
		timer.start();
	}	// end method run
	
	class ChangeScreenAction extends AbstractAction
	{		
		@Override // Superclass: AbstractAction
		public void actionPerformed(ActionEvent ae)
		{
			CoolPlatformer.changeScreen("PauseScreen");
		}	// end method ActionPerformed
	}	// end class ChangeScreenAction
	
	class SetKeyAction extends AbstractAction
	{
		private int indexToSet;
		private boolean pressedDown;

		SetKeyAction(int indexToSet, boolean pressedDown)
		{
			this.indexToSet = indexToSet;
			this.pressedDown = pressedDown;
		}	// end constructor(int, double)
		
		@Override // Superclass: AbstractAction
		public void actionPerformed(ActionEvent ae)
		{
			mainChar.setKey(indexToSet, pressedDown);
		}	// end method ActionPerformed
	}	// end class SetKeyAction
}	// end class GameScreen
