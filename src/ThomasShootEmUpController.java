import javax.swing.*;
import java.applet.AudioClip;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.SynchronousQueue;

import static javax.imageio.ImageIO.read;

//TODO: MAKE THE TRACKS APPEAR IN THEIR CORRECT PLACES
/***********************************************************************************************
 * David Frieder's Thomas Game Copyright 2018 David Frieder 3/4/2018 rev 1.6
 * Thomas can turn left and right with some bugginess
 ***********************************************************************************************/
public class ThomasShootEmUpController extends JComponent implements ActionListener, Runnable, KeyListener {
	public boolean isGoingRight = false;
	private Rectangle2D.Double upperTrackDetectionZone = new Rectangle2D.Double(0, 0, 200, 49);
	private URL thomasThemeAddress = getClass().getResource("ThomasThemeSong.wav");
	private AudioClip thomasThemeSong = JApplet.newAudioClip(thomasThemeAddress);
	private Image[] thomasSpriteImageArray = new Image[8];
	private Image[] reverseThomasImageArray = new Image[8];
	private Image gun = Toolkit.getDefaultToolkit().createImage(getClass().getResource("Minigun_SU.png"));
	private int widthOfScreen = java.awt.Toolkit.getDefaultToolkit().getScreenSize().width;
	private int heightOfScreen = java.awt.Toolkit.getDefaultToolkit().getScreenSize().height;
	private JFrame mainGameWindow = new JFrame("NewGame");// Makes window with
	// title "NewGame"
	private AffineTransform identityTx = new AffineTransform();
	private AffineTransform thomasTransform = new AffineTransform();// Set
																	// Thomas to
																	// 0,
	// 0
	private AffineTransform backgroundTx = new AffineTransform();
	private Timer animationTicker = new Timer(40, this);
	private Timer jumpingTicker = new Timer(1000 / 60, this);
	private Image thomasSpriteImage;
	private Image reverseThomasImage;
	private int thomasSpriteImageCounter;
	private Image roadImage;
	private Image trackImage;
	private int groundLevelTrackYPos = (int) (heightOfScreen * 0.809);
	private int level2TrackYPos = (int) (heightOfScreen * 0.2);
	private boolean isGoingLeft;
	private boolean isNotMoving;
	private boolean isJumping;
	private int thomasMaxSpeed = 13;
	private int initialJumpingVelocity = -31;
	public int jumpingVelocity = initialJumpingVelocity;
	private int movingVelocity;
	private int gravityAcceleration = 1;
	private Graphics2D g2;
	private int roadWidth;
	private int trackWidth;
	private int thomasYPos = 0;
	private boolean lastWayFacing = true;

	/***********************************************************************************************
	 * Main
	 ***********************************************************************************************/
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new ThomasShootEmUpController());
	}

	/***********************************************************************************************
	 * Run
	 ***********************************************************************************************/
	@Override
	public void run() {
		loadImages();
		setUpMainGameWindow();
		thomasThemeSong.loop();
		animationTicker.start();
		jumpingTicker.start();
	}

	/***********************************************************************************************
	 * Paint
	 ***********************************************************************************************/
	public void paint(Graphics g) {
		g2 = (Graphics2D) g;
		drawThomas();
		drawRoad();// ........................ Draw Road
		drawUpperTracks();// ................. Draw Upper Tracks
		drawLowerTracks();// ................. Draw Lower Tracks
	}

	/***********************************************************************************************
	 * Draw road
	 ***********************************************************************************************/
	private void drawRoad() {
		g2.setTransform(backgroundTx);
		g2.translate(-widthOfScreen, heightOfScreen - 200);
		g2.scale(1.5, 1.5);
		for (int i = 0; i < (2 * (widthOfScreen / roadImage.getWidth(null))) + 2; i++) // fits
																						// road
																						// images
																						// to
																						// screen
																						// width
		{
			g2.drawImage(roadImage, 0, 0, null);
			g2.translate(roadImage.getWidth(null), 0);
		}
	}

	/***********************************************************************************************
	 * Draw upper tracks
	 ***********************************************************************************************/
	private void drawUpperTracks() {
		g2.setTransform(backgroundTx);
		g2.translate(0, heightOfScreen / 2); // center in screen
		g2.scale(1.5, 1.5);
		for (int i = 0; i < 2; i++) // fits track images to screen width
		{
			g2.drawImage(trackImage, 0, 0, null);
			g2.translate(trackImage.getWidth(null), 0);
		}
	}

	/***********************************************************************************************
	 * Draw lower tracks
	 ***********************************************************************************************/
	private void drawLowerTracks() {
		g2.setTransform(backgroundTx);
		g2.translate(-widthOfScreen, heightOfScreen - 200);
		g2.scale(1.5, 1.5);
		for (int i = 0; i < (2 * (widthOfScreen / trackImage.getWidth(null))) + 2; i++) // fits
																						// track
																						// images
																						// to
																						// screen
																						// width
		{
			g2.drawImage(trackImage, 0, 0, null);
			g2.translate(trackImage.getWidth(null), 0);
		}
		repaint();
	}

	/***********************************************************************************************
	 * Draw Thomas with sprite files
	 ***********************************************************************************************/
	public void drawThomas() {
		g2.setTransform(backgroundTx);
		thomasTransform.setToTranslation(widthOfScreen / 3, heightOfScreen - 420);
		
		g2.setTransform(thomasTransform);
		try {
			thomasSpriteImageCounter = thomasSpriteImageCounter % 8;
			thomasSpriteImage = thomasSpriteImageArray[thomasSpriteImageCounter];
			reverseThomasImage = reverseThomasImageArray[thomasSpriteImageCounter];
			thomasTransform.setToTranslation(widthOfScreen / 3, heightOfScreen - 420 + thomasYPos);
			g2.setTransform(thomasTransform);
			
			if(isGoingLeft || lastWayFacing == true) {
			g2.drawImage(thomasSpriteImage, 0, 0, null);
			lastWayFacing = true;
			}
			if(isGoingRight || lastWayFacing == false)
			{
				g2.drawImage(reverseThomasImage, 0, 0, null);
				lastWayFacing = false;
			}
		} catch (Exception ex) {
			System.out.println("error reading thomas thomasSpriteImage from thomas sprite thomasSpriteImage array");
		}
	}

	/***********************************************************************************************
	 * Action Performed.....Respond to animation ticker and paint ticker
	 ***********************************************************************************************/
	@Override
	public void actionPerformed(ActionEvent e) {
		thomasTransform.setToTranslation(0, thomasYPos);
		if (isGoingLeft == true) {
			if (e.getSource() == animationTicker) {
				if (g2 != null) {
					thomasSpriteImageCounter++;
					backgroundTx.setToTranslation(backgroundTx.getTranslateX() + 20, 0);
					if (backgroundTx.getTranslateX() > widthOfScreen) {
						backgroundTx.setToTranslation(-widthOfScreen, 0);
					}
					repaint();
				}
			}
		}
		if (isGoingRight == true) {
			if (e.getSource() == animationTicker) {
				if (g2 != null) {
					thomasSpriteImageCounter++;
					if (thomasSpriteImageCounter < 0) {
						thomasSpriteImageCounter = 7;
					}
					backgroundTx.setToTranslation(backgroundTx.getTranslateX() - 20, 0);
					if (backgroundTx.getTranslateX() < -widthOfScreen) {
						backgroundTx.setToTranslation(widthOfScreen, 0);
					}
					repaint();
				}
			}
		}
		if (isJumping == true) {
			if (e.getSource() == jumpingTicker) {
				if (g2 != null) {
					thomasYPos += jumpingVelocity;
					jumpingVelocity += gravityAcceleration;
				}
				if (thomasYPos > 0)
				{
					jumpingVelocity = initialJumpingVelocity;
					thomasYPos = 0;
					isJumping = false;
				}
				repaint();
			}
		}
	}

	/***********************************************************************************************
	 * Respond to key typed
	 ***********************************************************************************************/
	@Override
	public void keyTyped(KeyEvent e) {
	}

	/***********************************************************************************************
	 * Respond to key pressed
	 ***********************************************************************************************/
	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_RIGHT) // going right
		{
			isGoingRight = true;
			isGoingLeft = false;
		}
		if (e.getKeyCode() == KeyEvent.VK_LEFT) // going left
		{
			isGoingLeft = true;
			isGoingRight = false;
			animationTicker.start();
		}
		if (e.getKeyCode() == KeyEvent.VK_LEFT && e.getKeyCode() == KeyEvent.VK_RIGHT)
		{
			isGoingLeft = false;
			isGoingRight = false;
			System.out.println("yes");
		}
		if (e.getKeyCode() == KeyEvent.VK_UP) {
			isJumping = true;
		}
	}

	/***********************************************************************************************
	 * Respond to key released
	 ***********************************************************************************************/
	@Override
	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_RIGHT) // going right
		{
			isGoingRight = false;
		}
		if (e.getKeyCode() == KeyEvent.VK_LEFT) // going left
		{
			isGoingLeft = false;
		}
		if (e.getKeyCode() == KeyEvent.VK_UP) {
		}
	}

	/***********************************************************************************************
	 * Get .png files, convert to Image and load sprite array
	 ***********************************************************************************************/
	private void loadImages() {
		try {
			thomasSpriteImageArray[0] = read(getClass().getResource("Thomas1.png"));
			thomasSpriteImageArray[1] = read(getClass().getResource("Thomas2.png"));
			thomasSpriteImageArray[2] = read(getClass().getResource("Thomas3.png"));
			thomasSpriteImageArray[3] = read(getClass().getResource("Thomas4.png"));
			thomasSpriteImageArray[4] = read(getClass().getResource("Thomas5.png"));
			thomasSpriteImageArray[5] = read(getClass().getResource("Thomas6.png"));
			thomasSpriteImageArray[6] = read(getClass().getResource("Thomas7.png"));
			thomasSpriteImageArray[7] = read(getClass().getResource("Thomas8.png"));
			reverseThomasImageArray[0] = read(getClass().getResource("Reversed Thomas1.png"));
			reverseThomasImageArray[1] = read(getClass().getResource("Reversed Thomas2.png"));
			reverseThomasImageArray[2] = read(getClass().getResource("Reversed Thomas3.png"));
			reverseThomasImageArray[3] = read(getClass().getResource("Reversed Thomas4.png"));
			reverseThomasImageArray[4] = read(getClass().getResource("Reversed Thomas5.png"));
			reverseThomasImageArray[5] = read(getClass().getResource("Reversed Thomas6.png"));
			reverseThomasImageArray[6] = read(getClass().getResource("Reversed Thomas7.png"));
			reverseThomasImageArray[7] = read(getClass().getResource("Reversed Thomas8.png"));
		} catch (IOException e) {
			System.out.println("error reading from thomas sprite array");
		}
		roadImage = Toolkit.getDefaultToolkit().createImage(getClass().getResource("ground.png"));
		trackImage = Toolkit.getDefaultToolkit().createImage(getClass().getResource("Tracks.png"));
		roadWidth = roadImage.getWidth(null);
		trackWidth = roadImage.getWidth(null);
	}

	/***********************************************************************************************
	 * Set up main JFrame
	 ***********************************************************************************************/
	private void setUpMainGameWindow() {
		mainGameWindow.setTitle("Thomas the tank");
		mainGameWindow.setSize(widthOfScreen, heightOfScreen);
		mainGameWindow.add(this);// Adds the paint method to the JFrame
		mainGameWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainGameWindow.getContentPane().setBackground(new Color(200, 235, 255));
		mainGameWindow.setVisible(true);
		mainGameWindow.addKeyListener(this);
	}
}
