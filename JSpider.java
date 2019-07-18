import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * JSpider (version: 1.01)
 * By Jimmy Y. (June 25, 2019 to July 3, 2019)
 * 
 * Problems:
 * - After multiple resizings of the component, it freezes to fixed size?
 * 
 * TODO:
 * - Play game 10 times in a row to catch any potential bugs (make sure you say yes to the dialog for new game and try undo, restart, and newGame buttons)
 * - Animations?
 * 
 * Changes:
 * - 7/17/2019: reset current difficulty only
 * - 7/17/2019: fixed deal, after deal, check for cards to remove
 * - 7/13/2019: added new rule, cannot undo after cards removed
 * - 7/6/2019: fixed a score bug
 */
@SuppressWarnings("serial")
public class JSpider extends JFrame implements ActionListener, ComponentListener, WindowListener {
	private int width = 1100;
	private int height = 700;
	
	private JMenuBar menuBar;
	
	private JMenu menu;
	
	private JMenuItem newGame;
	private JMenuItem restartGame;
	private JMenuItem undo;
	private JMenuItem deal;
	private JMenuItem changeDifficulty;
	private JMenuItem showStats;
	private JMenuItem toggleDebugMode;
	private JMenuItem howto;
	private JMenuItem about;
	private JMenuItem exit;
	
	private GameBoard board = null;
	
	private DataTracker tracker;
	
	private ImageIcon icon;
	
	private boolean debug;
	
	static {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
			ex.printStackTrace();
		}
	}
	
	private JSpider() {
		super("JSpider");
		
		setMinimumSize(new Dimension(width, height));
		setLocationRelativeTo(null);
		
		menuBar = new JMenuBar();
		
		menu = new JMenu("Menu");
		
		newGame = new JMenuItem("New game");
		restartGame = new JMenuItem("Restart current game");
		undo = new JMenuItem("Undo");
		deal = new JMenuItem("Deal!");
		changeDifficulty = new JMenuItem("Change difficulty");
		showStats = new JMenuItem("Show statistics");
		toggleDebugMode = new JMenuItem("Enter debug mode");
		howto = new JMenuItem("How to play?!?");
		about = new JMenuItem("About JSpider");
		exit = new JMenuItem("Exit window");
		
		newGame.addActionListener(this);
		restartGame.addActionListener(this);
		undo.addActionListener(this);
		deal.addActionListener(this);
		changeDifficulty.addActionListener(this);
		showStats.addActionListener(this);
		toggleDebugMode.addActionListener(this);
		howto.addActionListener(this);
		about.addActionListener(this);
		exit.addActionListener(this);
		
		toggleDebugMode.setEnabled(false);
		
		menu.add(newGame);
		menu.add(restartGame);
		menu.add(undo);
		menu.add(deal);
		menu.add(changeDifficulty);
		menu.add(showStats);
		menu.add(toggleDebugMode);
		menu.add(howto);
		menu.add(about);
		menu.add(exit);
		
		menuBar.add(menu);
		
		setJMenuBar(menuBar);
		
		selectDifficulty();
		setContentPane(board);
		board.setInsets(getInsets());
		
		tracker = new DataTracker("stats.db");
		
		icon = new ImageIcon(readImage("images\\icon.png"));
		setIconImage(icon.getImage());
		
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setExtendedState(MAXIMIZED_BOTH);
		addComponentListener(this);
		addWindowListener(this);
		
		debug = false;
	}
	
	public static void main(String[] args) {
		EventQueue.invokeLater(() -> {
			JSpider main = new JSpider();
			main.setVisible(true);
		});
	}
	
	private Image readImage(String fileName) {
		Image img = null;
		
		try {
			img = ImageIO.read(new File(fileName));
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		
		return img;
	}
	
	private void selectDifficulty() {
		String[] difficulties = {"Easy", "Medium", "Hard"};
		String choice = (String) JOptionPane.showInputDialog(this, "Select difficulty:", "New game", JOptionPane.QUESTION_MESSAGE, null, difficulties, difficulties[0]);
		
		if (board == null && choice == null) {
			board = new GameBoard("Easy"); // if user does not select any (ie. he cancelled), then easy is defaulted at startup
		} else if (board == null && choice != null) {
			board = new GameBoard(choice);
		} else if (choice != null) {
			board.clearCards();
			board.loadImages(choice);
			board.newGame();
		}
		
		// else if choice is null then do nothing
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		
		if (source == newGame) {
			board.newGame();
			int[] a = tracker.getData(board.getDifficulty());
			a[3]++;
		} else if (source == restartGame) {
			board.resetGame();
		} else if (source == undo) {
			board.undo();
		} else if (source == deal) {
			board.deal();
		} else if (source == changeDifficulty) {
			selectDifficulty();
		} else if (source == showStats) {
			JDialog dialog = new JDialog(this, true);
			
			dialog.setTitle("Statistics");
			dialog.setSize(190, 170);
			dialog.setLocationRelativeTo(null);
			dialog.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			
			JTabbedPane tabbedPane = new JTabbedPane();
			
			for (String difficulty : new String[]{"Easy", "Medium", "Hard"}) {
				JPanel panel = new JPanel();
				
				panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
				
				int[] a = tracker.getData(difficulty);
				
				JLabel label1 = new JLabel("Best Score: " + a[0]);
				JLabel label2 = new JLabel("Best moves: " + a[1]);
				JLabel label3 = new JLabel("Wins: " + a[2]);
				JLabel label4 = new JLabel("Quits: " + a[3]);
				
				label1.setAlignmentX(JComponent.CENTER_ALIGNMENT);
				label2.setAlignmentX(JComponent.CENTER_ALIGNMENT);
				label3.setAlignmentX(JComponent.CENTER_ALIGNMENT);
				label4.setAlignmentX(JComponent.CENTER_ALIGNMENT);
				
				JButton button = new JButton("Reset");
				button.addActionListener(evt -> {
					tracker.reset(difficulty);
					dialog.dispose();
				});
				button.setAlignmentX(JComponent.CENTER_ALIGNMENT);
				
				panel.add(label1);
				panel.add(label2);
				panel.add(label3);
				panel.add(label4);
				panel.add(button);
				
				tabbedPane.addTab(difficulty, panel);
			}
			
			dialog.add(tabbedPane);
			dialog.setVisible(true);
		} else if (source == toggleDebugMode) {
			debug = !debug;
			toggleDebugMode.setText(debug ? "Exit debug mode" : "Enter debug mode");
		} else if (source == howto) {
			JDialog dialog = new JDialog(this, true);
			
			dialog.setTitle("Spider solitaire gameplay (shamelessly copied and pasted from Wikipedia)");
			dialog.setSize(500, 400);
			dialog.setLocationRelativeTo(null);
			dialog.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			
			JEditorPane textRegion = new JEditorPane("text/html", "");
			
			textRegion.setEditable(false);
			
			String text = "The game is played with two decks of cards for a total of 104 " +
						  "cards. Fifty-four of the cards are laid out horizontally in ten " +
						  "columns with only the top card showing. The remaining fifty cards " +
						  "are laid out in the lower right hand corner in five piles of ten with " +
						  "no cards showing." +
						  "<p>In the horizontal columns a card may be moved to any other card " +
						  "in the column as long as it is in descending numerical sequence. " +
						  "For example, a six of hearts may be moved to a seven of any suit. " +
						  "However, a sequence of cards can only be moved if they are all of " +
						  "the same suit in numerical descending order. For example, a six " +
						  "and seven of hearts may be moved to an eight of any suit, but a six " +
						  "of hearts and seven of clubs cannot be moved together. Moving the top card in a " +
						  "column allows the topmost hidden card to be turned over. This card then enters " +
						  "into the play. Other cards can be placed on it, and it can be moved to other cards " +
						  "in a sequence or to an empty column.</p>" +
						  "<p>The object of the game is to uncover all the hidden cards and by moving cards " +
						  "from one column to another to place cards in sequential order from King to Ace " +
						  "using the fewest moves. Each final sequence must be all of the same suit. Once a " +
						  "complete sequence is achieved the cards are removed from the table and 100 " +
						  "points are added to the score. Once a player has made all the moves possible with the current card layout, the player draws a new row of cards from one of the piles of ten in the right lower hand corner by " +
						  "clicking on the cards. Each of the ten cards in this draw lands face up on each of the ten horizontal columns and the player then " +
						  "proceeds to place these in such a way to create a sequence of cards all in one suit.</p>";
			
			textRegion.setText(text);
			
			dialog.add(new JScrollPane(textRegion));
			dialog.setVisible(true);
		} else if (source == about) {
			JOptionPane.showMessageDialog(this, "JSpider (version: 1.0)\nThis program is built by Jimmy Y. (codingexpert123@gmail.com)", "About JSpider", JOptionPane.INFORMATION_MESSAGE);
		} else {
			int[] a = tracker.getData(board.getDifficulty());
			a[3]++;
			tracker.writeToFile();
			System.exit(0);
		}
	}
	
	@Override
	public void componentResized(ComponentEvent e) {
		width = e.getComponent().getWidth();
		height = e.getComponent().getHeight();
		
		if (debug) {
			System.err.println("width:" + width + ", height:" + height);
		}
		
		board.calcYCutoff();
		board.fixPiles();
		board.fixDeck();
		board.fixJunk();
		board.repaint();
	}
	
	@Override
	public void windowClosing(WindowEvent e) {
		int[] a = tracker.getData(board.getDifficulty());
		a[3]++;
		tracker.writeToFile();
	}
	
	@Override public void componentMoved(ComponentEvent e) {}
	@Override public void componentShown(ComponentEvent e) {}
	@Override public void componentHidden(ComponentEvent e) {}
	@Override public void windowOpened(WindowEvent e) {}
	@Override public void windowClosed(WindowEvent e) {}
	@Override public void windowIconified(WindowEvent e) {}
	@Override public void windowDeiconified(WindowEvent e) {}
	@Override public void windowActivated(WindowEvent e) {}
	@Override public void windowDeactivated(WindowEvent e) {}
	
	private class GameBoard extends JComponent implements MouseListener, MouseMotionListener {
		private final int piles = 10;
		private final int slots = 6; // deck slots, each slot have 10 (piles) cards
		
		private final int margin = 10;
		
		private final int cardWidth = 71;
		private final int cardHeight = 96;
		
		private final Color bgColor = new Color(0, 120, 0);
		
		private List<Card> allCards; // also it's a junk pile
		
		private Image cardBack;
		
		private List<Card>[] pile;
		private List<Card>[] deck;
		
		private int top; // pointer to top
		private int ptr;
		
		private int yCutoff;
		
		private int score;
		private int moves;
		
		private List<Card> movingPile;
		
		private int index;
		
		private int prevMX;
		private int prevMY;
		
		private String difficulty;
		
		private Stack<GameState> undoStack;
		
		private Insets insets;
		
		public GameBoard(String difficulty) {
			cardBack = readImage("images\\back.png");
			undoStack = new Stack<>();
			insets = new Insets(0, 0, 0, 0);
			
			loadImages(difficulty);
			calcYCutoff();
			newGame();
			
			addMouseListener(this);
			addMouseMotionListener(this);
		}
		
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			
			g.setColor(bgColor);
			g.fillRect(0, 0, width, height);
			
			int xGap = (width - piles * cardWidth) / (piles + 1);
			int y = margin;
			
			for (int i = 0; i < piles; i++) {
				int x = xGap + i * cardWidth + i * xGap - insets.left;
				
				g.setColor(Color.WHITE);
				g.fillRect(x, y, cardWidth, cardHeight); // a white rectangle border indicates that there's no cards at that pile
				g.setColor(bgColor);
				g.fillRect(x + 1, y + 1, cardWidth - 2, cardHeight - 2);
				
				pile[i].stream().forEach(card -> {
					g.drawImage(card.isFaceDown() ? cardBack : card.getImage(), card.x, card.y, this);
				});
			}
			
			for (int i = 0; i <= top; i++) {
				Card card = deck[i].get(0);
				g.drawImage(cardBack, card.x, card.y, this);
			}
			
			for (int i = 12; i <= ptr; i += 13) {
				Card card = allCards.get(i);
				g.drawImage(card.getImage(), card.x, card.y, this);
			}
			
			g.setColor(bgColor.darker());
			g.fillRect(width / 2 - 100 - insets.left, height - cardHeight - margin - insets.bottom - 50, 200, cardHeight);
			
			StringBuilder sb = new StringBuilder();
			sb.append("Score: ").append(score);
			sb.append("\nMoves: ").append(moves);
			
			g.setColor(Color.WHITE);
			g.setFont(new Font("consolas", Font.BOLD, 14));
			drawString(g, sb.toString(), width / 2 - 50, height - cardHeight - margin - insets.bottom - 50 + 25);
			
			if (movingPile != null) {
				movingPile.stream().forEach(card -> {
					g.drawImage(card.getImage(), card.x, card.y, this);
				});
			}
		}
		
		public void setInsets(Insets insets) {
			this.insets = insets;
			repaint();
		}
		
		public String getDifficulty() {
			return difficulty;
		}
		
		public void undo() {
			GameState state = undoStack.pop();
			
			allCards = state.getAllCards();
			pile = state.getPile();
			top = state.getTop();
			ptr = state.getPtr();
			
			repaint();
			
			score--;
			moves++;
			
			if (!canUndo()) {
				undo.setEnabled(false);
			}
		}
		
		public boolean canUndo() {
			return !undoStack.empty() && !undoStack.peek().isFlagged();
		}
		
		public void resetGame() {
			if (undoStack.empty()) {
				return;
			}
			
			while (undoStack.size() > 1) {
				undoStack.pop();
			}
			
			GameState state = undoStack.pop();
			
			allCards = state.getAllCards();
			pile = state.getPile();
			deck = state.getDeck();
			top = state.getTop();
			ptr = state.getPtr();
			
			repaint();
			
			score = 500;
			moves = 0;
			
			undo.setEnabled(false);
		}
		
		public void clearCards() {
			collectAllCards();
			allCards = null;
		}
		
		/*
		 * This method is an extension of Graphics::drawString, it handles strings with newlines, thanks to SO!
		 */
		private void drawString(Graphics g, String str, int x, int y) {
			for (String line : str.split("\n")) {
				g.drawString(line, x, y += g.getFontMetrics().getHeight());
			}
		}
		
		private void deal() {
			for (int i = 0; i < piles; i++) {
				Card card = deck[top].get(i);
				pile[i].add(card);
				card.flip();
				fixPile(i);
			}
			
			deck[top--] = null;
			undoStack.push(new GameState());
			undo.setEnabled(false);
			
			for (int i = 0; i < piles; i++) {
				if (checkForCardsToRemove(i)) {
					score += 100;
				}
			}
			
			repaint();
		}
		
		public void calcYCutoff() {
			yCutoff = height * 3 / 5;
		}
		
		public void fixJunk() {
			int y = height - cardHeight - margin - 40 - insets.bottom - 16;
			
			for (int i = 0; i <= ptr; i++) {
				Card card = allCards.get(i);
				
				card.y = y;
			}
		}
		
		public void fixDeck() {
			int y = height - cardHeight - margin - 40 - insets.bottom - 16;
			
			for (int i = 0; i <= top; i++) {
				int x = width - cardWidth - margin - insets.left - 10 - (margin + 2) * i;
				
				deck[i].stream().forEach(card -> {
					card.x = x;
					card.y = y;
				});
			}
		}
		
		public void fixPiles() {
			for (int i = 0; i < piles; i++) {
				fixPile(i);
			}
		}
		
		private void fixPile(int index) {
			if (pile[index].size() == 0) {
				return;
			}
			
			int xGap = (width - piles * cardWidth) / (piles + 1);
			int topX = xGap + index * cardWidth + index * xGap - insets.left;
			int yGap = 35;
			
			for (int i = 0; i < 6; i++) {
				int cards = pile[index].size();
				
				Card prevCard = null;
				
				yGap -= (i < 4) ? 4 : 1;
				
				for (int j = 0; j < cards; j++) {
					Card card = pile[index].get(j);
					
					int topY = (prevCard == null) ? margin : prevCard.y + (prevCard.isFaceDown() ? margin : yGap);
					
					card.x = topX;
					card.y = topY;
					
					prevCard = card;
				}
				
				int lastY = pile[index].get(pile[index].size() - 1).y;
				
				if (lastY < yCutoff) {
					break;
				}
			}
		}
		
		private void collectAllCards() {
			if (allCards.size() < 104) {
				for (int i = 0; i < piles; i++) {
					int cards = pile[i].size();
					
					for (int j = 0; j < cards; j++) {
						Card card = pile[i].get(j);
						
						if (!card.isFaceDown()) {
							card.flip();
						}
						
						allCards.add(card);
					}
				}
				
				for (int i = 0; i <= top; i++) {
					List<Card> slot = deck[i];
					
					int size = slot.size();
					
					for (int j = 0; j < size; j++) {
						allCards.add(slot.get(j));
					}
				}
			}
		}
		
		/*
		 * preconditions:
		 * - undoStack is !null
		 * - loadImages() is called
		 * - calcYCutoff() is called, otherwise fixPile() will be slow
		 */
		public void newGame() {
			collectAllCards();
			Collections.shuffle(allCards);
			initDeck();
			initPiles();
			deal();
			undoStack.clear();
			undo.setEnabled(false);
			
			ptr = -1;
			score = 500;
			moves = 0;
			movingPile = null;
		}
		
		@SuppressWarnings("unchecked")
		private void initDeck() {
			deck = new List[slots];
			
			top = slots - 1;
			
			int y = height - cardHeight - margin - 40 - insets.bottom - 16;
			
			for (int i = 0; i < slots; i++) {
				deck[i] = new ArrayList<>();
				
				int x = width - cardWidth - margin - insets.left - 10 - (margin + 2) * i;
				
				for (int j = 0; j < piles; j++) {
					Card card = draw();
					
					card.x = x;
					card.y = y;
					card.width = cardWidth;
					card.height = cardHeight;
					
					deck[i].add(card);
				}
			}
		}
		
		@SuppressWarnings("unchecked")
		private void initPiles() {
			pile = new List[piles];
			
			for (int i = 0; i < piles; i++) {
				pile[i] = new ArrayList<>();
				
				int cards = (i < 4) ? 5 : 4;
				
				for (int j = 0; j < cards; j++) {
					Card card = draw();
					
					card.width = cardWidth;
					card.height = cardHeight;
					
					pile[i].add(card);
				}
				
				fixPile(i);
			}
		}
		
		private Card draw() {
			Card card = allCards.get(0);
			allCards.remove(card);
			return card;
		}
		
		public void loadImages(String difficulty) {
			this.difficulty = difficulty;
			
			int value = -1;
			
			if (difficulty.equals("Easy")) {
				value = 4;
			} else if (difficulty.equals("Medium")) {
				value = 3;
			} else {
				value = 1;
			}
			
			allCards = new ArrayList<>();
			
			int counter = 0;
			
			while (counter < 8) {
				for (int suit = value; suit <= 4; suit++) {
					for (int rank = 1; rank <= 13; rank++) {
						allCards.add(new Card(readImage("images\\" + rank + "" + suit + ".png"), suit, rank, true));
					}
					
					counter++;
				}
			}
		}
		
		private boolean showPlayAgainDialog() {
			int[] a = tracker.getData(difficulty);
			
			a[0] = Math.max(a[0], score);
			a[1] = a[1] == 0 ? moves : Math.min(a[1], moves);
			a[2]++;
			
			int resp = JOptionPane.showConfirmDialog(this, "Do you want to play again?", "Game over! You won!", JOptionPane.YES_NO_OPTION);
			
			if (resp == JOptionPane.YES_OPTION) {
				newGame();
				return true;
			}
			
			tracker.writeToFile();
			System.exit(0);
			
			return false; // it will never be reached
		}
		
		private boolean checkForCardsToRemove(int index) {
			int suit = -1;
			int rank = 1;
			
			for (int i = pile[index].size() - 1; i >= 0 && rank <= 13; i--, rank++) {
				Card card = pile[index].get(i);
				
				if (suit == -1) { suit = card.getSuit(); }
				if (suit != card.getSuit()) { return false; }
				if (card.isFaceDown()) { return false; }
				if (card.getRank() != rank) { return false; }
			}
			
			if (rank == 14) {
				int y = height - cardHeight - margin - 40 - insets.bottom - 16;
				
				Card prevCard = (ptr == -1) ? null : allCards.get(ptr);
				
				for (int i = pile[index].size() - 1; i >= 0 && --rank >= 1; i--) {
					Card card = pile[index].get(i);
					
					card.x = (prevCard == null) ? margin : prevCard.x + margin + 2;
					card.y = y;
					card.flip(); // the card must be flipped face down for new game
					
					pile[index].remove(card);
					allCards.add(card);
					ptr++;
				}
				
				Card last = (pile[index].size() > 0) ? pile[index].get(pile[index].size() - 1) : null;
				
				if (last != null && last.isFaceDown()) {
					last.flip();
				}
				
				return true;
			}
			
			return false;
		}
		
		@Override
		public void mousePressed(MouseEvent e) {
			int mouseX = e.getX();
			int mouseY = e.getY();
			
			prevMX = mouseX;
			prevMY = mouseY;
			
			int index = -1;
			int startFrom = -1;
			
			outer:
			for (int i = 0; i < piles; i++) {
				int cards = pile[i].size();
				
				for (int j = cards - 1; j >= 0; j--) {
					Card card = pile[i].get(j);
					
					if (card.contains(mouseX, mouseY)) {
						index = i;
						startFrom = j;
						break outer;
					}
				}
			}
			
			if (index != -1) {
				if (pile[index].get(startFrom).isFaceDown()) {
					return;
				}
				
				List<Card> touched = pile[index].subList(startFrom, pile[index].size()).stream().collect(Collectors.toCollection(ArrayList::new)); // I know it's ugly but it resolves ConcurrentModificationException
				
				if (!debug) {
					int suit = -1; // ensures that the selected cards have same suite
					int rank = -1; // ensures that the selected cards follow decreasing numerical order
					
					int size = pile[index].size() - startFrom;
					
					for (int i = 0; i < size; i++, rank--) {
						if (suit == -1) { suit = touched.get(i).getSuit(); }
						if (rank == -1) { rank = touched.get(i).getRank(); }
						if (suit != touched.get(i).getSuit()) { return; }
						if (rank != touched.get(i).getRank()) { return; }
					}
				}
				
				undoStack.push(new GameState(allCards, pile, deck, top, ptr));
				pile[index].removeAll(touched);
				movingPile = touched;
				this.index = index;
				
				repaint();
			} else if (top >= 0) {
				Card topCard = deck[top].get(0);
				Card botCard = deck[0].get(0);
				
				Rectangle rect = new Rectangle(topCard.x, topCard.y, botCard.x + botCard.width - topCard.x, topCard.height);
				
				if (rect.contains(mouseX, mouseY)) {
					// make sure that there's no empty piles
					for (int i = 0; i < piles; i++) {
						if (pile[i].size() == 0) {
							return;
						}
					}
					
					deal();
				}
			}
		}
		
		@Override
		public void mouseReleased(MouseEvent e) {
			if (movingPile != null) {
				boolean success = false;
				boolean b = false;
				
				Card firstCard = movingPile.get(0);
				Card lastCard = movingPile.get(movingPile.size() - 1);
				
				Rectangle dragRegion = new Rectangle(firstCard.x, firstCard.y, firstCard.width, lastCard.y + lastCard.height - firstCard.y);
				
				for (int i = 0; i < piles; i++) {
					if (i == index) {
						continue;
					}
					
					Card card = (pile[i].size() > 0) ? pile[i].get(pile[i].size() - 1) : null; // last card
					
					if (card != null && card.intersects(dragRegion) && (!debug ? (card.getRank() == firstCard.getRank() + 1) : true)) {
						pile[i].addAll(movingPile);
						
						if (checkForCardsToRemove(i)) {
							score += 100;
							undoStack.push(new GameState());
							undo.setEnabled(false);
							b = true;
							
							if (allCards.size() == 104) {
								movingPile = null;
								repaint();
								
								if (showPlayAgainDialog()) {
									return;
								}
							}
						}
						
						fixPile(i);
						success = true;
						break;
					} else if (card == null && pile[i].size() == 0) {
						int xGap = (width - piles * cardWidth) / (piles + 1);
						int topX = xGap + i * cardWidth + i * xGap - insets.left;
						
						Rectangle rect = new Rectangle(topX, margin, cardWidth, cardHeight); // white rectangle border
						
						if (rect.intersects(dragRegion)) {
							pile[i].addAll(movingPile);
							fixPile(i);
							success = true;
							break;
						}
					}
				}
				
				if (!success) {
					pile[index].addAll(movingPile);
					undoStack.pop();
				} else {
					Card last = (pile[index].size() > 0) ? pile[index].get(pile[index].size() - 1) : null;
					
					if (last != null && last.isFaceDown()) {
						last.flip();
					}
					
					score--;
					moves++;
					
					if (!b) {
						undo.setEnabled(true);
					}
				}
				
				fixPile(index);
				movingPile = null;
				
				repaint();
				
				if (allCards.size() == 104) {
					showPlayAgainDialog();
				}
			}
		}
		
		@Override
		public void mouseDragged(MouseEvent e) {
			if (movingPile != null) {
				int mouseX = e.getX();
				int mouseY = e.getY();
				
				int dx = mouseX - prevMX;
				int dy = mouseY - prevMY;
				
				movingPile.stream().forEach(c -> c.translate(dx, dy));
				
				prevMX = mouseX;
				prevMY = mouseY;
				
				repaint();
			}
		}
		
		@Override public void mouseClicked(MouseEvent e) {}
		@Override public void mouseEntered(MouseEvent e) {}
		@Override public void mouseExited(MouseEvent e) {}
		@Override public void mouseMoved(MouseEvent e) {}
		
		private class GameState {
			private List<Card> allCards;
			
			private List<Card>[] pile;
			private List<Card>[] deck;
			
			private int top;
			private int ptr;
			
			private boolean flag;
			
			public GameState() {
				flag = true;
			}
			
			@SuppressWarnings("unchecked")
			public GameState(List<Card> allCards, List<Card>[] pile, List<Card>[] deck, int top, int ptr) {
				this.allCards = new ArrayList<>();
				
				for (Card card : allCards) {
					this.allCards.add((Card) card.clone());
				}
				
				this.pile = new List[piles];
				
				for (int i = 0; i < piles; i++) {
					this.pile[i] = new ArrayList<>();
					
					for (Card card : pile[i]) {
						this.pile[i].add((Card) card.clone());
					}
				}
				
				this.deck = new List[slots];
				
				for (int i = 0; i < slots; i++) {
					if (deck[i] == null) {
						this.deck[i] = null;
					} else {
						this.deck[i] = new ArrayList<>();
						
						for (Card card : deck[i]) {
							this.deck[i].add((Card) card.clone());
						}
					}
				}
				
				this.top = top;
				this.ptr = ptr;
				
				flag = false;
			}
			
			public List<Card> getAllCards() {
				return allCards;
			}
			
			public List<Card>[] getPile() {
				return pile;
			}
			
			public List<Card>[] getDeck() {
				return deck;
			}
			
			public int getTop() {
				return top;
			}
			
			public int getPtr() {
				return ptr;
			}
			
			public boolean isFlagged() {
				return flag;
			}
		}
	}
	
	private class DataTracker {
		private String fileName;
		
		private Map<String, int[]> map;
		
		public DataTracker(String fileName) {
			this.fileName = fileName;
			map = new TreeMap<>();
			readData();
		}
		
		public void reset(String difficulty) {
			int[] a = getData(difficulty);
			Arrays.fill(a, 0);
		}
		
		public void writeToFile() {
			try {
				DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(fileName)));
				
				for (String difficulty : new String[]{"Easy", "Medium", "Hard"}) {
					int[] a = getData(difficulty);
					
					for (int i = 0; i < 4; i++) {
						dos.writeInt(a[i]);
					}
				}
				
				dos.close();
			} catch (IOException ex) {
				ex.printStackTrace();
				System.exit(1);
			}
		}
		
		public int[] getData(String difficulty) {
			return map.get(difficulty);
		}
		
		private void readData() {
			try {
				DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(fileName)));
				
				for (String difficulty : new String[]{"Easy", "Medium", "Hard"}) {
					map.put(difficulty, new int[]{dis.readInt(), dis.readInt(), dis.readInt(), dis.readInt()});
				}
				
				dis.close();
			} catch (IOException ex) {
				ex.printStackTrace();
				System.exit(1);
			}
		}
	}
	
	private class Card extends Rectangle {
		private Image cardImage;
		
		private int suit;
		private int rank;
		
		private boolean isFaceDown;
		
		public Card(Image cardImage, int suit, int rank, boolean isFaceDown) {
			super();
			this.cardImage = cardImage;
			this.suit = suit;
			this.rank = rank;
			this.isFaceDown = isFaceDown;
		}
		
		public void flip() {
			isFaceDown = !isFaceDown;
		}
		
		public Image getImage() {
			return cardImage;
		}
		
		public int getSuit() {
			return suit;
		}
		
		public int getRank() {
			return rank;
		}
		
		public boolean isFaceDown() {
			return isFaceDown;
		}
		
		@Override
		public Object clone() {
			Card copy = new Card(cardImage, suit, rank, isFaceDown);
			
			copy.x = x;
			copy.y = y;
			copy.width = width;
			copy.height = height;
			
			return copy;
		}
	}
}