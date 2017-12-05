import javax.swing.*;

import java.applet.AudioClip;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.net.URL;

public class ThomasShootEmUpController extends JComponent implements ActionListener, Runnable
{
	private ImageIcon[] images = new ImageIcon[8];
	private Image gun;
	private int gunDirection;
	private int widthOfScreen = java.awt.Toolkit.getDefaultToolkit().getScreenSize().width;
	private int heightOfScreen = java.awt.Toolkit.getDefaultToolkit().getScreenSize().height;
	private JFrame mainGameWindow = new JFrame("NewGame");// Makes window with title "NewGame"
	private AffineTransform identityTx = new AffineTransform();
	private Timer paintTicker = new Timer(20, this);
	private Timer animationTicker = new Timer(45, this);
	private ImageIcon thomasImageIcon = new ImageIcon();
	private Image thomasImage;
	private int pictureCounter;
	private int thomasXPos = widthOfScreen / 2;
	private int thomasYPos = (int) (heightOfScreen * 0.69);
	private Image roadImage = Toolkit.getDefaultToolkit().createImage(getClass().getResource("ground.png"));
	private Image tracksImage = Toolkit.getDefaultToolkit().createImage(getClass().getResource("Standard Gauge Train Track Sprite.png"));
	private int roadXPos = 0;
	private int groundLevelTrackYPos = (int) (heightOfScreen * 0.449);
	private int level2TrackYPos = (int) (heightOfScreen * 0.2);
	private boolean isGoingRight;
	private boolean isGoingLeft;
	private boolean isNotMoving;
	private boolean isJumping;
	private boolean isFalling;
	private int position;
	private int initialJumpingVelocity = -37;
	public int jumpingVelocity = initialJumpingVelocity;
	private int movingVelocity;
	public ThomasUtilites util = new ThomasUtilites(jumpingVelocity);
	private int gravityAcceleration = 1;
	AffineTransform tx;
	Rectangle2D.Double thomasRectLeft;
	Rectangle2D.Double thomasRectRight;
	Rectangle2D.Double upperTracksRect;
	URL thomasThemeAddress = getClass().getResource("Thomas The Tank Engine Theme Song.wav");
	AudioClip thomasThemeSong = JApplet.newAudioClip(thomasThemeAddress);

	public static void main(String[] args)
	{
		SwingUtilities.invokeLater(new ThomasShootEmUpController());
	}
	public void run()
	{
		paintTicker.start();
		animationTicker.start();
		images[0] = new ImageIcon("src/Thomas1.png");
		images[1] = new ImageIcon("src/Thomas2.png");
		images[2] = new ImageIcon("src/Thomas3.png");
		images[3] = new ImageIcon("src/Thomas4.png");
		images[4] = new ImageIcon("src/Thomas5.png");
		images[5] = new ImageIcon("src/Thomas6.png");
		images[6] = new ImageIcon("src/Thomas7.png");
		images[7] = new ImageIcon("src/Thomas8.png");
		gun = Toolkit.getDefaultToolkit().createImage(getClass().getResource("Minigun_SU.png"));
		mainGameWindow.setTitle("Thomas the tank");
		mainGameWindow.setSize(widthOfScreen, heightOfScreen);
		mainGameWindow.add(this);// Adds the paint method to the JFrame
		mainGameWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainGameWindow.getContentPane().setBackground(new Color(200, 235, 255));
		mainGameWindow.setVisible(true);
		mainGameWindow.addKeyListener(util);
		thomasThemeSong.play();
		isGoingLeft = true;
		isGoingRight = false;
	}

	public void paint(Graphics g)
	{
		Graphics2D g2 = (Graphics2D) g;
		// TODO:WORK ON MAKING THOMAS ACCELERATE BEFORE HE REACHES FULL SPEED
		g2.scale(-1, 1);
		g2.scale((double) (1 / g2.getTransform().getScaleX()) * 0.9, 1 * 0.9);
//		g2.drawImage(gun, thomasXPos + 150, thomasYPos + (int)(thomasYPos * 0.01),
//		 gunDirection, gun.getHeight(this) / 2, null);
		Image im = thomasImageIcon.getImage();
		if (isGoingRight)
		{
			g2.setTransform(identityTx); //resets the scale to 1-1 TODO: try using this to reset all the transforms before everything is drawn
			thomasImageIcon = images[pictureCounter];
			tx = AffineTransform.getScaleInstance(-1, 1);
			tx.translate(-thomasXPos - (thomasXPos / 3), thomasYPos);
			g2.drawImage(im, tx, null);
			g2.setColor(Color.green);
			thomasRectRight = new Rectangle2D.Double(thomasXPos - (thomasXPos / 6), thomasYPos, thomasImageIcon.getIconWidth(), thomasImageIcon.getIconHeight());
			gunDirection = gun.getWidth(this) / 2;
		}
		if (isGoingLeft)
		{
			thomasImageIcon = images[pictureCounter];
			tx = AffineTransform.getScaleInstance(1, 1);
			tx.translate(thomasXPos - (thomasXPos / 6), thomasYPos);
			g2.drawImage(im, tx, null);
			g2.setColor(Color.green);
			thomasRectLeft = new Rectangle2D.Double(thomasXPos - (thomasXPos / 6), thomasYPos, thomasImageIcon.getIconWidth(), thomasImageIcon.getIconHeight());
			g2.draw(thomasRectLeft);
			gunDirection = -gun.getWidth(this) / 2;
		}
		if(!util.isJumping && jumpingVelocity < 0){
			gravityAcceleration = 3;
		}
		if (util.isJumping || thomasYPos < (int) (heightOfScreen * 0.69))
		{
			thomasImageIcon = images[pictureCounter];
			thomasYPos += jumpingVelocity;
			jumpingVelocity += gravityAcceleration;
			gravityAcceleration = 1;
		}
		int trackWidth = tracksImage.getWidth(mainGameWindow);
		int roadWidth =  roadImage.getWidth(mainGameWindow);
		g2.scale(2, 2);
		for (int i = -5; i < 5; i++) //for loop that condenses the drawing of the roads
		{
			g2.drawImage(roadImage, roadXPos + roadWidth * i, groundLevelTrackYPos, this); 
		}
		for (int i = -4; i < 4; i++) //for loop that condenses the drawing of the tracks
		{
			g2.drawImage(tracksImage, roadXPos + trackWidth * i, groundLevelTrackYPos, this);
		}
		g2.setColor(Color.green);
		upperTracksRect = new Rectangle2D.Double(roadXPos, level2TrackYPos, tracksImage.getWidth(mainGameWindow), tracksImage.getHeight(mainGameWindow));
		g2.draw(upperTracksRect);
		g2.drawImage(tracksImage, roadXPos, level2TrackYPos, this); //draws an elevated set of tracks
		if (upperTracksRect.intersects(thomasRectLeft))//NEED TO FIX: intersection area is shifted to the left of the upper tracks
		{
			System.out.println("crash");
		}
		if (roadXPos > mainGameWindow.getWidth())
		{
			roadXPos = -mainGameWindow.getWidth() / 4;
		}
		if (roadXPos < -mainGameWindow.getWidth() / 4)
		{
			roadXPos = mainGameWindow.getWidth();
		}
		if (thomasYPos > (int) (heightOfScreen * 0.69)){
			thomasYPos = (int) (heightOfScreen * 0.69); //Added this to compensate for Thomas falling through the tracks
		}
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == paintTicker)
		{
			repaint();
			if (thomasYPos >= (int) (heightOfScreen * 0.69))
			{
				if (util.isJumping)
				{
					isJumping = true;
				} 
			}
		}
		if (e.getSource() == animationTicker)
		{
			roadXPos = roadXPos + movingVelocity;
			if (movingVelocity > 12 || (movingVelocity > 0 && !(util.moveLeft || util.moveRight))  || movingVelocity > 0 && isNotMoving){ //allows Thomas to decelerate going right
				movingVelocity -= 1;
			}
			if (movingVelocity < -12 || (movingVelocity < 0 && !(util.moveLeft || util.moveRight))  || movingVelocity < 0 && isNotMoving){ //allows Thomas to decelerate going left
				movingVelocity ++;
			}
			if (util.moveLeft)
			{
				isGoingLeft = true;
				isGoingRight = false;
				isNotMoving = false;
				thomasImageIcon = images[pictureCounter];
				movingVelocity = movingVelocity + 1;
				repaint();
			}
			if (!isNotMoving && (util.moveLeft || util.moveRight))
			{
				pictureCounter = (pictureCounter + 1) % 8;
			}
			if (util.moveRight)
			{
				isGoingRight = true;
				isGoingLeft = false;
				isNotMoving = false;
				thomasImageIcon = images[pictureCounter];
				AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
				movingVelocity = movingVelocity - 1;
				repaint();
			}
			if (util.moveLeft && util.moveRight)
			{
				isNotMoving = true;
				//Figure out a way to make thomas' wheels stay still when moveLeft & moveRight are not activated
			}

			if (thomasYPos >= (int) (heightOfScreen * 0.69))
			{
				jumpingVelocity = initialJumpingVelocity;
			}
		}
	}
}