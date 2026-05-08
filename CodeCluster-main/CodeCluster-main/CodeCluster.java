import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
import javax.swing.border.*;
import javax.sound.sampled.*;

public class CodeCluster extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private GameData gameData;
    private String currentPlayer;
    private int currentLevel = 1;
    private SoundManager soundManager;

    public CodeCluster() {
        soundManager = new SoundManager();
        gameData = new GameData();
        gameData.loadData();

        setTitle("Code Cluster");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        mainPanel.add(new MainMenuPanel(this, soundManager), "menu");
        mainPanel.add(new NameSelectionPanel(this, soundManager), "nameSelection");
        mainPanel.add(new SettingsPanel(this, soundManager), "settings");
        mainPanel.add(new LeaderboardPanel(this, soundManager), "leaderboard");

        add(mainPanel);
        showPanel("menu");
        setVisible(true);
    }

    public void showPanel(String panelName) {
        cardLayout.show(mainPanel, panelName);
    }

    public void startGame(String playerName) {
        this.currentPlayer = playerName;
        this.currentLevel = 1;
        GameBoardPanel gameBoard = new GameBoardPanel(this, playerName, 1);
        mainPanel.add(gameBoard, "game");
        showPanel("game");
    }

    public void nextLevel() {
        currentLevel++;
        if (currentLevel <= 5) {
            GameBoardPanel gameBoard = new GameBoardPanel(this, currentPlayer, currentLevel);
            mainPanel.add(gameBoard, "game" + currentLevel);
            showPanel("game" + currentLevel);
        } else {
            showPanel("menu");
        }
    }

    public SoundManager getSoundManager() {
        return soundManager;
    }

    public GameData getGameData() {
        return gameData;
    }

    public String getCurrentPlayer() {
        return currentPlayer;
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new CodeCluster();
        });
    }
}

class SoundManager {
    private boolean soundEnabled = true;
    private Thread bgmThread;
    private volatile boolean bgmPlaying = false;

    public void playClick() {
        if (!soundEnabled) return;
        Thread soundThread = new Thread(() -> playBeep(800, 100), "SoundClickThread");
        soundThread.setDaemon(true);
        soundThread.start();
    }

    public void playSuccess() {
        if (!soundEnabled) return;
        Thread soundThread = new Thread(() -> {
            playBeep(1000, 150);
            try { Thread.sleep(100); } catch (InterruptedException e) {}
            playBeep(1200, 150);
        }, "SoundSuccessThread");
        soundThread.setDaemon(true);
        soundThread.start();
    }

    public void playError() {
        if (!soundEnabled) return;
        playBeep(400, 150);
        try { Thread.sleep(50); } catch (InterruptedException e) {}
        playBeep(300, 150);
    }

    public void playGameOver() {
        if (!soundEnabled) return;
        playBeep(400, 200);
        try { Thread.sleep(100); } catch (InterruptedException e) {}
        playBeep(300, 200);
        try { Thread.sleep(100); } catch (InterruptedException e) {}
        playBeep(200, 300);
    }

    // Start background music - loops continuously
    public void playBackgroundMusic() {
        if (!soundEnabled) return;
        if (bgmPlaying) return;
        
        bgmPlaying = true;
        bgmThread = new Thread(() -> {
            while (bgmPlaying) {
                try {
                    // Play a simple melodic loop (8-bar progression)
                    playBGMLoop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, "BGMThread");
        bgmThread.setDaemon(true);
        bgmThread.start();
    }

    // Play a melodic background music loop
    private void playBGMLoop() {
        int[] melody = {262, 330, 392, 440, 392, 330, 262, 196}; // C, E, G, A, G, E, C, B (all notes in C major)
        int noteDuration = 400;
        
        for (int note : melody) {
            if (!bgmPlaying) break;
            playBeep(note, noteDuration);
            try { Thread.sleep(50); } catch (InterruptedException e) {}
        }
    }

    // Stop background music
    public void stopBackgroundMusic() {
        bgmPlaying = false;
        if (bgmThread != null) {
            try {
                bgmThread.join(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // Play win sound effect - ascending notes
    public void playWinSound() {
        if (!soundEnabled) return;
        Thread soundThread = new Thread(() -> {
            int[] winNotes = {523, 659, 784, 1047}; // C5, E5, G5, C6
            for (int note : winNotes) {
                playBeep(note, 150);
                try { Thread.sleep(50); } catch (InterruptedException e) {}
            }
        }, "WinSoundThread");
        soundThread.setDaemon(true);
        soundThread.start();
    }

    // Play game over sound effect - descending notes
    public void playGameOverSound() {
        if (!soundEnabled) return;
        Thread soundThread = new Thread(() -> {
            int[] gameOverNotes = {523, 392, 330, 262, 196}; // C5, G4, E4, C4, B3
            for (int note : gameOverNotes) {
                playBeep(note, 200);
                try { Thread.sleep(100); } catch (InterruptedException e) {}
            }
        }, "GameOverSoundThread");
        soundThread.setDaemon(true);
        soundThread.start();
    }

    private void playBeep(int frequency, int duration) {
        try {
            float sampleRate = 44100f;
            int samples = Math.round(sampleRate * duration / 1000);
            byte[] tone = new byte[samples];
            double toneHz = frequency;

            for (int i = 0; i < samples; i++) {
                double sample = Math.sin(2.0 * Math.PI * toneHz * i / sampleRate);
                tone[i] = (byte) (sample * 100);
            }

            AudioFormat format = new AudioFormat(sampleRate, 8, 1, true, false);
            SourceDataLine line = AudioSystem.getSourceDataLine(format);
            line.open();
            line.start();
            line.write(tone, 0, tone.length);
            line.drain();
            line.close();
        } catch (Exception e) {
            // Sound failed, continue silently
        }
    }

    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
        if (!enabled && bgmPlaying) {
            stopBackgroundMusic();
        }
    }

    public boolean isSoundEnabled() {
        return soundEnabled;
    }
}

class GameData {
    private LinkedList<String> playerNames;
    private TreeMap<String, ArrayList<Score>> scores;
    private TreeMap<Integer, ArrayList<ArrayList<Category>>> levelData;
    private boolean sfxEnabled;
    private Stack<GameState> gameHistory;

    public GameData() {
        playerNames = new LinkedList<>();
        scores = new TreeMap<>();
        levelData = new TreeMap<>();
        sfxEnabled = true;
        gameHistory = new Stack<>();
        initializeLevelData();
    }

    private void initializeLevelData() {
        // Level 1 - 2 alternative sets of 4 categories
        ArrayList<ArrayList<Category>> level1Sets = new ArrayList<>();
        ArrayList<Category> level1a = new ArrayList<>();
        level1a.add(new Category("Java Keywords", new String[]{"STATIC", "VOID", "CLASS", "PUBLIC"}, 1));
        level1a.add(new Category("Data Structures", new String[]{"ARRAY", "STACK", "QUEUE", "TREE"}, 2));
        level1a.add(new Category("Primitive Types", new String[]{"INT", "BOOLEAN", "CHAR", "DOUBLE"}, 1));
        level1a.add(new Category("Common Methods", new String[]{"GET", "SET", "PUSH", "POP"}, 1));
        level1Sets.add(level1a);

        ArrayList<Category> level1b = new ArrayList<>();
        level1b.add(new Category("Control Keywords", new String[]{"IF", "ELSE", "SWITCH", "CASE"}, 1));
        level1b.add(new Category("Collection Types", new String[]{"LIST", "SET", "MAP", "QUEUE"}, 2));
        level1b.add(new Category("Numeric Types", new String[]{"BYTE", "SHORT", "INT", "LONG"}, 1));
        level1b.add(new Category("Common Operators", new String[]{"PLUS", "MINUS", "MULTIPLY", "DIVIDE"}, 1));
        level1Sets.add(level1b);
        levelData.put(1, level1Sets);

        // Level 2 - 2 alternative sets of 4 categories
        ArrayList<ArrayList<Category>> level2Sets = new ArrayList<>();
        ArrayList<Category> level2a = new ArrayList<>();
        level2a.add(new Category("OOP Concepts", new String[]{"POLYMORPHISM", "INHERITANCE", "ENCAPSULATION", "ABSTRACTION"}, 3));
        level2a.add(new Category("Loop Keywords", new String[]{"FOR", "WHILE", "DO", "FOREACH"}, 2));
        level2a.add(new Category("String Methods", new String[]{"LENGTH", "SUBSTRING", "CONCAT", "TRIM"}, 2));
        level2a.add(new Category("Boolean Operators", new String[]{"AND", "OR", "NOT", "XOR"}, 2));
        level2Sets.add(level2a);

        ArrayList<Category> level2b = new ArrayList<>();
        level2b.add(new Category("Array Methods", new String[]{"SORT", "FILL", "COPY", "BINARYSEARCH"}, 3));
        level2b.add(new Category("Exception Types", new String[]{"IOEXCEPTION", "NULLPOINTER", "ARITHMETIC", "INDEXOUTOFBOUNDS"}, 3));
        level2b.add(new Category("Access Modifiers", new String[]{"PUBLIC", "PRIVATE", "PROTECTED", "DEFAULT"}, 2));
        level2b.add(new Category("Inheritance Keywords", new String[]{"EXTENDS", "IMPLEMENTS", "SUPER", "THIS"}, 3));
        level2Sets.add(level2b);
        levelData.put(2, level2Sets);

        // Level 3 - 2 alternative sets of 5 categories
        ArrayList<ArrayList<Category>> level3Sets = new ArrayList<>();
        ArrayList<Category> level3a = new ArrayList<>();
        level3a.add(new Category("Java Collections", new String[]{"LIST", "MAP", "SET", "HASHMAP"}, 3));
        level3a.add(new Category("Exception Handling", new String[]{"TRY", "CATCH", "THROW", "FINALLY"}, 3));
        level3a.add(new Category("Access Modifiers", new String[]{"PRIVATE", "PROTECTED", "PACKAGE", "DEFAULT"}, 2));
        level3a.add(new Category("Wrapper Classes", new String[]{"INTEGER", "LONG", "FLOAT", "CHARACTER"}, 3));
        level3a.add(new Category("Keywords", new String[]{"FINAL", "SUPER", "THIS", "EXTENDS"}, 3));
        level3Sets.add(level3a);

        ArrayList<Category> level3b = new ArrayList<>();
        level3b.add(new Category("Stream Methods", new String[]{"FILTER", "MAP", "COLLECT", "FOR_EACH"}, 4));
        level3b.add(new Category("Thread States", new String[]{"NEW", "RUNNABLE", "BLOCKED", "TERMINATED"}, 4));
        level3b.add(new Category("Numeric Wrappers", new String[]{"DOUBLE", "FLOAT", "INTEGER", "LONG"}, 2));
        level3b.add(new Category("String Builders", new String[]{"APPEND", "INSERT", "DELETE", "TOSTRING"}, 3));
        level3b.add(new Category("Concurrency", new String[]{"SYNCHRONIZED", "VOLATILE", "LOCK", "ATOMIC"}, 4));
        level3Sets.add(level3b);
        levelData.put(3, level3Sets);

        // Level 4 - 2 alternative sets of 5 categories
        ArrayList<ArrayList<Category>> level4Sets = new ArrayList<>();
        ArrayList<Category> level4a = new ArrayList<>();
        level4a.add(new Category("Design Patterns", new String[]{"SINGLETON", "FACTORY", "OBSERVER", "DECORATOR"}, 4));
        level4a.add(new Category("Testing Terms", new String[]{"JUNIT", "MOCK", "ASSERT", "TEST"}, 4));
        level4a.add(new Category("Thread States", new String[]{"NEW", "RUNNABLE", "BLOCKED", "WAITING"}, 4));
        level4a.add(new Category("Memory Areas", new String[]{"HEAP", "STACK", "METASPACE", "POOL"}, 4));
        level4a.add(new Category("Synchronization", new String[]{"LOCK", "SYNCHRONIZED", "VOLATILE", "ATOMIC"}, 4));
        level4Sets.add(level4a);

        ArrayList<Category> level4b = new ArrayList<>();
        level4b.add(new Category("Build Tools", new String[]{"MAVEN", "GRADLE", "ANT", "NPM"}, 4));
        level4b.add(new Category("Web Concepts", new String[]{"HTTP", "HTTPS", "REST", "SOAP"}, 4));
        level4b.add(new Category("Database Terms", new String[]{"SQL", "INDEX", "JOIN", "TRANSACTION"}, 4));
        level4b.add(new Category("Caching", new String[]{"MEMCACHED", "REDIS", "GUAVA", "CACHE"}, 4));
        level4b.add(new Category("Testing Frameworks", new String[]{"TESTNG", "SPOCK", "CUCUMBER", "MOCKITO"}, 4));
        level4Sets.add(level4b);
        levelData.put(4, level4Sets);

        // Level 5 - 2 alternative sets of 6 categories
        ArrayList<ArrayList<Category>> level5Sets = new ArrayList<>();
        ArrayList<Category> level5a = new ArrayList<>();
        level5a.add(new Category("Spring Framework", new String[]{"BEAN", "AUTOWIRED", "COMPONENT", "SERVICE"}, 4));
        level5a.add(new Category("SQL Keywords", new String[]{"SELECT", "INSERT", "UPDATE", "DELETE"}, 4));
        level5a.add(new Category("Git Commands", new String[]{"COMMIT", "PUSH", "PULL", "MERGE"}, 3));
        level5a.add(new Category("HTTP Methods", new String[]{"GET", "POST", "PUT", "PATCH"}, 3));
        level5a.add(new Category("JSON Operations", new String[]{"PARSE", "STRINGIFY", "SERIALIZE", "DESERIALIZE"}, 3));
        level5a.add(new Category("Build Tools", new String[]{"MAVEN", "GRADLE", "ANT", "NPM"}, 4));
        level5Sets.add(level5a);

        ArrayList<Category> level5b = new ArrayList<>();
        level5b.add(new Category("Cloud Platforms", new String[]{"AZURE", "AWS", "GCP", "HEROKU"}, 4));
        level5b.add(new Category("CI/CD", new String[]{"JENKINS", "GITHUB", "GITLAB", "AZUREDEVOPS"}, 4));
        level5b.add(new Category("Container Tools", new String[]{"DOCKER", "KUBERNETES", "PODMAN", "SWARM"}, 4));
        level5b.add(new Category("API Methods", new String[]{"GET", "POST", "DELETE", "PATCH"}, 3));
        level5b.add(new Category("Data Formats", new String[]{"JSON", "XML", "YAML", "CSV"}, 3));
        level5b.add(new Category("Security", new String[]{"OAUTH", "JWT", "SSL", "TLS"}, 4));
        level5Sets.add(level5b);
        levelData.put(5, level5Sets);
    }

    public ArrayList<Category> getLevelCategories(int level) {
        ArrayList<ArrayList<Category>> sets = levelData.get(level);
        if (sets == null || sets.isEmpty()) {
            return new ArrayList<>();
        }
        ArrayList<Category> selected = sets.get(new Random().nextInt(sets.size()));
        return new ArrayList<>(selected);
    }

    public void addPlayerName(String name) {
        if (!playerNames.contains(name)) {
            playerNames.add(name);
            saveData();
        }
    }

    public LinkedList<String> getPlayerNames() {
        return playerNames;
    }

    public void addScore(Score score) {
        String key = score.getPlayerName();
        if (!scores.containsKey(key)) {
            scores.put(key, new ArrayList<>());
        }
        scores.get(key).add(score);
        saveData();
    }

    public TreeMap<String, ArrayList<Score>> getAllScores() {
        return scores;
    }

    public ArrayList<Score> getFlatScores() {
        ArrayList<Score> allScores = new ArrayList<>();
        for (ArrayList<Score> scoreList : scores.values()) {
            allScores.addAll(scoreList);
        }
        return allScores;
    }

    public boolean isSfxEnabled() {
        return sfxEnabled;
    }

    public void setSfxEnabled(boolean enabled) {
        this.sfxEnabled = enabled;
        saveData();
    }

    public void saveData() {
        try {
            // Save player names
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("players.dat"));
            oos.writeObject(playerNames);
            oos.close();

            // Save scores
            oos = new ObjectOutputStream(new FileOutputStream("scores.dat"));
            oos.writeObject(scores);
            oos.close();

            // Save settings
            oos = new ObjectOutputStream(new FileOutputStream("settings.dat"));
            oos.writeBoolean(sfxEnabled);
            oos.close();
        } catch (IOException e) {
            System.err.println("Error saving data: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public void loadData() {
        try {
            // Load player names
            File playerFile = new File("players.dat");
            if (playerFile.exists()) {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(playerFile));
                playerNames = (LinkedList<String>) ois.readObject();
                ois.close();
            }

            // Load scores
            File scoresFile = new File("scores.dat");
            if (scoresFile.exists()) {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(scoresFile));
                scores = (TreeMap<String, ArrayList<Score>>) ois.readObject();
                ois.close();
            }

            // Load settings
            File settingsFile = new File("settings.dat");
            if (settingsFile.exists()) {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(settingsFile));
                sfxEnabled = ois.readBoolean();
                ois.close();
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading data: " + e.getMessage());
        }
    }

    public void pushGameState(GameState state) {
        gameHistory.push(state);
    }

    public GameState popGameState() {
        if (!gameHistory.isEmpty()) {
            return gameHistory.pop();
        }
        return null;
    }
}

class Category implements Serializable {
    private String name;
    private String[] words;
    private int difficulty; // 1=Yellow (easy), 2=Green, 3=Blue, 4=Purple (hard)

    public Category(String name, String[] words, int difficulty) {
        this.name = name;
        this.words = words;
        this.difficulty = difficulty;
    }

    public String getName() {
        return name;
    }

    public String[] getWords() {
        return words;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public boolean matches(List<String> selectedWords) {
        if (selectedWords.size() != 4) return false;
        for (String word : words) {
            if (!selectedWords.contains(word)) return false;
        }
        return true;
    }

    public int getMatchCount(List<String> selectedWords) {
        int count = 0;
        for (String word : words) {
            if (selectedWords.contains(word)) count++;
        }
        return count;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Category category = (Category) obj;
        return difficulty == category.difficulty && 
               name.equals(category.name) && 
               java.util.Arrays.equals(words, category.words);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(name, difficulty, java.util.Arrays.hashCode(words));
    }
}

class Score implements Serializable {
    private String playerName;
    private int score;
    private int level;
    private int attempt;
    private String date;
    private ArrayList<String> achievements;

    public Score(String playerName, int score, int level, int attempt, ArrayList<String> achievements) {
        this.playerName = playerName;
        this.score = score;
        this.level = level;
        this.attempt = attempt;
        this.achievements = achievements;
        this.date = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new Date());
    }

    public String getPlayerName() { return playerName; }
    public int getScore() { return score; }
    public int getLevel() { return level; }
    public int getAttempt() { return attempt; }
    public String getDate() { return date; }
    public ArrayList<String> getAchievements() { return achievements; }
}

class GameState implements Serializable {
    private int level;
    private int timeLeft;
    private int mistakes;

    public GameState(int level, int timeLeft, int mistakes) {
        this.level = level;
        this.timeLeft = timeLeft;
        this.mistakes = mistakes;
    }

    public int getLevel() { return level; }
    public int getTimeLeft() { return timeLeft; }
    public int getMistakes() { return mistakes; }
}

class MainMenuPanel extends JPanel {
    public MainMenuPanel(CodeCluster game, SoundManager soundManager) {
        setLayout(new BorderLayout());

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(60, 100, 40, 100));

        JLabel titleLabel;
        try {
            ImageIcon icon = new ImageIcon("title.png");
            Image img = icon.getImage();

            java.awt.image.ImageFilter filter = new java.awt.image.RGBImageFilter() {
                @Override
                public final int filterRGB(int x, int y, int rgb) {
                    int r = (rgb >> 16) & 0xFF;
                    int g = (rgb >> 8) & 0xFF;
                    int b = rgb & 0xFF;
                    
                    if (r >= 254 && g >= 254 && b >= 254) {
                        return 0x00FFFFFF & rgb; // Fully transparent
                    }
                    return rgb;
                }
            };

            java.awt.image.ImageProducer ip = new java.awt.image.FilteredImageSource(img.getSource(), filter);
            img = java.awt.Toolkit.getDefaultToolkit().createImage(ip);

            if (icon.getIconWidth() > 800) {
                img = img.getScaledInstance(800, -1, Image.SCALE_SMOOTH);
            }
            titleLabel = new JLabel(new ImageIcon(img));
        } catch (Exception e) {
            titleLabel = new JLabel("CODE CLUSTER");
            titleLabel.setFont(new Font("Arial", Font.BOLD, 85));
            titleLabel.setForeground(Color.WHITE);
        }
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Find 4 words that share a category!");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 26));
        subtitleLabel.setForeground(Color.WHITE);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        centerPanel.add(Box.createVerticalGlue());
        centerPanel.add(titleLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        centerPanel.add(subtitleLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 60)));

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
        buttonsPanel.setOpaque(false);
        buttonsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton startButton = new GlowingButton("Start Game", new Color(34, 197, 94)); // Green
        startButton.addActionListener(e -> {
            soundManager.playClick();
            game.showPanel("nameSelection");
        });
        buttonsPanel.add(startButton);
        buttonsPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        JButton settingsButton = new GlowingButton("Settings", new Color(14, 165, 233)); // Blue
        settingsButton.addActionListener(e -> {
            soundManager.playClick();
            game.showPanel("settings");
        });
        buttonsPanel.add(settingsButton);
        buttonsPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        JButton leaderboardButton = new GlowingButton("Leaderboard", new Color(249, 115, 22)); // Orange
        leaderboardButton.addActionListener(e -> {
            soundManager.playClick();
            game.showPanel("leaderboard");
        });
        buttonsPanel.add(leaderboardButton);
        buttonsPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        JButton exitButton = new GlowingButton("Exit", new Color(239, 68, 68)); // Red
        exitButton.addActionListener(e -> {
            soundManager.playClick();
            System.exit(0);
        });
        buttonsPanel.add(exitButton);

        centerPanel.add(buttonsPanel);
        centerPanel.add(Box.createVerticalGlue());

        add(centerPanel, BorderLayout.CENTER);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();

        Color centerColor = new Color(30, 58, 138); // lighter navy
        Color edgeColor = new Color(15, 23, 42); // very dark navy
        
        java.awt.geom.Point2D center = new java.awt.geom.Point2D.Float(w / 2f, h / 2f);
        float radius = (float) Math.max(w, h);
        float[] dist = {0.0f, 1.0f};
        Color[] colors = {centerColor, edgeColor};
        RadialGradientPaint p = new RadialGradientPaint(center, radius, dist, colors);
        
        g2.setPaint(p);
        g2.fillRect(0, 0, w, h);
        g2.dispose();
    }
}

class GlowingButton extends JButton {
    private Color glowColor;
    private boolean solidPill;
    private boolean hoverGreen = false;

    public void setHoverGreen(boolean hoverGreen) {
        this.hoverGreen = hoverGreen;
    }

    public void setGlowColor(Color glowColor) {
        this.glowColor = glowColor;
        repaint();
    }

    public GlowingButton(String text, Color glowColor) {
        this(text, glowColor, true);
    }

    public GlowingButton(String text, Color glowColor, boolean solidPill) {
        super(text);
        this.glowColor = glowColor;
        this.solidPill = solidPill;
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setForeground(new Color(15, 23, 42)); 
        setFont(new Font("Arial", Font.BOLD, 24));
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(400, 65));
        setMinimumSize(new Dimension(400, 65));
        setMaximumSize(new Dimension(400, 65));
        setAlignmentX(Component.CENTER_ALIGNMENT);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        Color baseGlow = getModel().isRollover() && isEnabled() ? glowColor.brighter() : glowColor;

        if (!solidPill) {
            java.awt.geom.Area outer = new java.awt.geom.Area(new Rectangle(0, 0, width, height));
            java.awt.geom.Area inner = new java.awt.geom.Area(new java.awt.geom.RoundRectangle2D.Float(8, 8, width - 16, height - 16, height - 16, height - 16));
            outer.subtract(inner);
            g2.setClip(outer);
        }

        // outer soft glow layers
        for (int i = 0; i < 4; i++) {
            g2.setColor(new Color(baseGlow.getRed(), baseGlow.getGreen(), baseGlow.getBlue(), 40 - i * 10));
            g2.fillRoundRect(i * 2, i * 2, width - i * 4, height - i * 4, height, height);
        }

        if (!solidPill) {
            g2.setClip(null); 
        }

        // inner pill & border
        GradientPaint gp;
        if (solidPill) {
            // solid filled border
            g2.setColor(baseGlow);
            g2.fillRoundRect(6, 6, width - 12, height - 12, height - 12, height - 12);

            if (getModel().isArmed()) {
                 gp = new GradientPaint(0, 9, new Color(220, 220, 225), 0, height - 9, new Color(190, 190, 200));
            } else if (getModel().isRollover() && hoverGreen) {
                 gp = new GradientPaint(0, 9, new Color(134, 239, 172), 0, height - 9, new Color(34, 197, 94));
            } else {
                 gp = new GradientPaint(0, 9, Color.WHITE, 0, height - 9, new Color(210, 215, 225));
            }
            g2.setPaint(gp);
            g2.fillRoundRect(9, 9, width - 18, height - 18, height - 18, height - 18);
        } else {
            // 1. fill the inner background slightly to give it substance
            g2.setColor(new Color(0, 0, 0, 40)); 
            g2.fillRoundRect(8, 8, width - 16, height - 16, height - 16, height - 16);

            // 2. main translucent glass gradient
            if (getModel().isArmed()) {
                gp = new GradientPaint(0, 8, new Color(255, 255, 255, 10), 0, height - 8, new Color(255, 255, 255, 80));
            } else {
                gp = new GradientPaint(0, 8, new Color(255, 255, 255, 5), 0, height - 8, new Color(255, 255, 255, 120));
            }
            g2.setPaint(gp);
            g2.fillRoundRect(8, 8, width - 16, height - 16, height - 16, height - 16);

            // 3. sharp inner bottom highlight to give 3D bevel effect
            GradientPaint highlightStroke = new GradientPaint(
                0, height / 2, new Color(255, 255, 255, 0),
                0, height - 8, new Color(255, 255, 255, 200)
            );
            g2.setPaint(highlightStroke);
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(9, 9, width - 18, height - 18, height - 18, height - 18);

            // 4. colored neon border
            g2.setColor(baseGlow);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawRoundRect(8, 8, width - 16, height - 16, height - 16, height - 16);
        }

        FontMetrics fm = g2.getFontMetrics();
        int textWidth = fm.stringWidth(getText());
        int textX = (width - textWidth) / 2;
        int textY = (height - fm.getHeight()) / 2 + fm.getAscent();
        
        if ("New Profile".equals(getText())) {
            int iconSize = 24;
            int iconX = 30; 
            int iconY = (height - iconSize) / 2;
            g2.setColor(new Color(120, 120, 120));
            g2.fillOval(iconX + 6, iconY + 2, 12, 12);
            g2.fillArc(iconX + 2, iconY + 14, 20, 20, 0, 180);
        }

        g2.setColor(getForeground());
        g2.drawString(getText(), textX, textY);

        g2.dispose();
    }
}



class GlowingTextField extends JTextField {
    private Color glowColor;

    public GlowingTextField(Color glowColor) {
        super();
        this.glowColor = glowColor;
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        setFont(new Font("Arial", Font.BOLD, 24));
        setForeground(Color.BLACK);
        setCaretColor(Color.BLACK);
        setHorizontalAlignment(JTextField.CENTER);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        for (int i = 0; i < 4; i++) {
            g2.setColor(new Color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(), 40 - i * 10));
            g2.fillRoundRect(i * 2, i * 2, width - i * 4, height - i * 4, height, height);
        }

        g2.setColor(glowColor);
        g2.fillRoundRect(6, 6, width - 12, height - 12, height - 12, height - 12);

        g2.setColor(Color.WHITE);
        g2.fillRoundRect(9, 9, width - 18, height - 18, height - 18, height - 18);

        g2.dispose();
        super.paintComponent(g);
    }
}

class DarkRoundedPanel extends JPanel {
    public DarkRoundedPanel() {
        setOpaque(false);
    }
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(0, 0, 0, 80)); 
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
        g2.dispose();
        super.paintComponent(g);
    }
}

class NameSelectionPanel extends JPanel {
    public NameSelectionPanel(CodeCluster game, SoundManager soundManager) {
        setLayout(new BorderLayout());

        JPanel mainContainer = new JPanel();
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));
        mainContainer.setOpaque(false);
        mainContainer.setBorder(BorderFactory.createEmptyBorder(30, 50, 20, 50));

        JLabel titleLabel;
        try {
            ImageIcon icon = new ImageIcon("welcome.png");
            Image img = icon.getImage();

            java.awt.image.ImageFilter filter = new java.awt.image.RGBImageFilter() {
                @Override
                public final int filterRGB(int x, int y, int rgb) {
                    int r = (rgb >> 16) & 0xFF;
                    int g = (rgb >> 8) & 0xFF;
                    int b = rgb & 0xFF;
                    
                    if (r >= 254 && g >= 254 && b >= 254) {
                        return 0x00FFFFFF & rgb;
                    }
                    return rgb;
                }
            };

            java.awt.image.ImageProducer ip = new java.awt.image.FilteredImageSource(img.getSource(), filter);
            img = java.awt.Toolkit.getDefaultToolkit().createImage(ip);

            if (icon.getIconWidth() > 800) {
                img = img.getScaledInstance(800, -1, Image.SCALE_SMOOTH);
            }
            titleLabel = new JLabel(new ImageIcon(img));
        } catch (Exception e) {
            titleLabel = new JLabel("WELCOME PLAYER!");
            titleLabel.setFont(new Font("Arial", Font.BOLD, 75));
            titleLabel.setForeground(Color.WHITE);
        }
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Choose your name to continue");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 24));
        subtitleLabel.setForeground(Color.WHITE);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        mainContainer.add(titleLabel);
        mainContainer.add(Box.createRigidArea(new Dimension(0, 10)));
        mainContainer.add(subtitleLabel);
        mainContainer.add(Box.createRigidArea(new Dimension(0, 30)));

        // input section
        JLabel enterLabel = new JLabel("Enter your name:");
        enterLabel.setFont(new Font("Arial", Font.BOLD, 22));
        enterLabel.setForeground(Color.WHITE);
        enterLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainContainer.add(enterLabel);
        mainContainer.add(Box.createRigidArea(new Dimension(0, 10)));

        GlowingTextField nameField = new GlowingTextField(new Color(168, 85, 247)); // Purple glow
        nameField.setMaximumSize(new Dimension(500, 60));
        nameField.setPreferredSize(new Dimension(500, 60));
        mainContainer.add(nameField);
        mainContainer.add(Box.createRigidArea(new Dimension(0, 20)));

        // show saved names
        LinkedList<String> savedNames = game.getGameData().getPlayerNames();
        if (!savedNames.isEmpty()) {
            JSeparator sep = new JSeparator(SwingConstants.HORIZONTAL);
            sep.setMaximumSize(new Dimension(600, 1));
            sep.setForeground(new Color(255, 255, 255, 100));
            sep.setBackground(new Color(255, 255, 255, 100));
            mainContainer.add(sep);
            mainContainer.add(Box.createRigidArea(new Dimension(0, 15)));

            JLabel savedLabel = new JLabel("Or select a saved name:");
            savedLabel.setFont(new Font("Arial", Font.BOLD, 22));
            savedLabel.setForeground(Color.WHITE);
            savedLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            mainContainer.add(savedLabel);
            mainContainer.add(Box.createRigidArea(new Dimension(0, 10)));

            DarkRoundedPanel darkPanel = new DarkRoundedPanel();
            darkPanel.setLayout(new BoxLayout(darkPanel, BoxLayout.Y_AXIS));
            darkPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            darkPanel.setMaximumSize(new Dimension(540, 250));

            JScrollPane scrollPane = new JScrollPane();
            scrollPane.setOpaque(false);
            scrollPane.getViewport().setOpaque(false);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

            JPanel namesPanel = new JPanel();
            namesPanel.setLayout(new BoxLayout(namesPanel, BoxLayout.Y_AXIS));
            namesPanel.setOpaque(false);
            namesPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

            for (String name : savedNames) {
                JButton nameButton = new GlowingButton(name, new Color(249, 115, 22)); // orange
                nameButton.setMaximumSize(new Dimension(460, 50));
                nameButton.setPreferredSize(new Dimension(460, 50));
                nameButton.setFont(new Font("Arial", Font.BOLD, 20));
                nameButton.setAlignmentX(Component.CENTER_ALIGNMENT);
                nameButton.addActionListener(e -> {
                    soundManager.playClick();
                    game.startGame(name);
                });
                namesPanel.add(nameButton);
                namesPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            }

            JButton newProfileBtn = new GlowingButton("New Profile", new Color(150, 150, 150)); // gray glow
            newProfileBtn.setMaximumSize(new Dimension(460, 50));
            newProfileBtn.setPreferredSize(new Dimension(460, 50));
            newProfileBtn.setFont(new Font("Arial", Font.BOLD, 20));
            newProfileBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            newProfileBtn.addActionListener(e -> {
                soundManager.playClick();
                nameField.requestFocusInWindow();
            });
            namesPanel.add(newProfileBtn);

            scrollPane.setViewportView(namesPanel);
            darkPanel.add(scrollPane);

            mainContainer.add(darkPanel);
            mainContainer.add(Box.createRigidArea(new Dimension(0, 20)));
        }

        JButton submitButton = new GlowingButton("Start Playing", new Color(168, 85, 247)); // purple
        submitButton.setMaximumSize(new Dimension(500, 60));
        submitButton.setPreferredSize(new Dimension(500, 60));
        submitButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        submitButton.addActionListener(e -> {
            soundManager.playClick();
            String name = nameField.getText().trim();
            if (!name.isEmpty()) {
                game.getGameData().addPlayerName(name);
                game.startGame(name);
            }
        });
        mainContainer.add(submitButton);
        mainContainer.add(Box.createVerticalGlue());

        // back button
        GlowingButton backButton = new GlowingButton("Back to Menu", new Color(150, 150, 150), false);
        backButton.setPreferredSize(new Dimension(200, 50));
        backButton.setMaximumSize(new Dimension(200, 50));
        backButton.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        backButton.setForeground(Color.WHITE);
        backButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        backButton.addActionListener(e -> {
            soundManager.playClick();
            game.showPanel("menu");
        });
        mainContainer.add(backButton);

        add(mainContainer, BorderLayout.CENTER);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();

        // dark blue radial gradient
        Color centerColor = new Color(30, 58, 138); 
        Color edgeColor = new Color(15, 23, 42); 
        
        java.awt.geom.Point2D center = new java.awt.geom.Point2D.Float(w / 2f, h / 2f);
        float radius = (float) Math.max(w, h);
        float[] dist = {0.0f, 1.0f};
        Color[] colors = {centerColor, edgeColor};
        RadialGradientPaint p = new RadialGradientPaint(center, radius, dist, colors);
        
        g2.setPaint(p);
        g2.fillRect(0, 0, w, h);
        g2.dispose();
    }
}

class SettingsPanel extends JPanel {
    public SettingsPanel(CodeCluster game, SoundManager soundManager) {
        setLayout(new BorderLayout());

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);

        DarkRoundedPanel darkPanel = new DarkRoundedPanel();
        darkPanel.setLayout(new BoxLayout(darkPanel, BoxLayout.Y_AXIS));
        darkPanel.setBorder(BorderFactory.createEmptyBorder(60, 80, 60, 80));

        JLabel titleLabel = new JLabel("Settings");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 42));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        boolean initialSfx = game.getGameData().isSfxEnabled();
        Color sfxColor = initialSfx ? new Color(34, 197, 94) : new Color(239, 68, 68);
        String sfxText = initialSfx ? "Sound: ON" : "Sound: OFF";
        
        GlowingButton sfxToggle = new GlowingButton(sfxText, sfxColor, true);
        sfxToggle.setForeground(new Color(15, 23, 42)); 
        sfxToggle.addActionListener(e -> {
            boolean currentSfx = game.getGameData().isSfxEnabled();
            boolean newSfx = !currentSfx;
            game.getGameData().setSfxEnabled(newSfx);
            soundManager.setSoundEnabled(newSfx);
            if (newSfx) soundManager.playClick();
            
            sfxToggle.setText(newSfx ? "Sound: ON" : "Sound: OFF");
            sfxToggle.setGlowColor(newSfx ? new Color(34, 197, 94) : new Color(239, 68, 68));
        });

        GlowingButton doneButton = new GlowingButton("Done", new Color(150, 150, 150), false);
        doneButton.setForeground(Color.WHITE);
        doneButton.addActionListener(e -> {
            soundManager.playClick();
            game.showPanel("menu");
        });

        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        sfxToggle.setAlignmentX(Component.CENTER_ALIGNMENT);
        doneButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        darkPanel.add(titleLabel);
        darkPanel.add(Box.createRigidArea(new Dimension(0, 50)));
        darkPanel.add(sfxToggle);
        darkPanel.add(Box.createRigidArea(new Dimension(0, 40)));
        darkPanel.add(doneButton);

        centerPanel.add(darkPanel);

        add(centerPanel, BorderLayout.CENTER);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();

        Color centerColor = new Color(30, 58, 138); 
        Color edgeColor = new Color(15, 23, 42); 
        
        java.awt.geom.Point2D center = new java.awt.geom.Point2D.Float(w / 2f, h / 2f);
        float radius = (float) Math.max(w, h);
        float[] dist = {0.0f, 1.0f};
        Color[] colors = {centerColor, edgeColor};
        java.awt.RadialGradientPaint p = new java.awt.RadialGradientPaint(center, radius, dist, colors);
        
        g2.setPaint(p);
        g2.fillRect(0, 0, w, h);
        g2.dispose();
    }
}

class LeaderboardPanel extends JPanel {
    private CodeCluster game;
    private SoundManager soundManager;
    private JTable table;
    private JTextField searchField;
    private JComboBox<String> levelFilter;
    private JComboBox<String> sortOrder;
    private DefaultTableModel tableModel;

    public LeaderboardPanel(CodeCluster game, SoundManager soundManager) {
        this.game = game;
        this.soundManager = soundManager;
        setLayout(new BorderLayout());

        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.setOpaque(false);
        wrapperPanel.setBorder(BorderFactory.createEmptyBorder(30, 80, 30, 80));

        // title
        JLabel titleLabel = new JLabel("Leaderboard", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 42));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        // control panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        controlPanel.setOpaque(false);

        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        searchLabel.setForeground(Color.WHITE);
        searchField = new JTextField(12);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        searchField.setBackground(new Color(15, 23, 42));
        searchField.setForeground(Color.WHITE);
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(56, 189, 248), 1, true),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent e) {
                updateTable();
            }
        });

        JLabel levelLabel = new JLabel("Level:");
        levelLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        levelLabel.setForeground(Color.WHITE);
        String[] levels = {"All", "Level 1", "Level 2", "Level 3", "Level 4", "Level 5"};
        
        javax.swing.ListCellRenderer<? super String> darkRenderer = new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                c.setBackground(isSelected ? new Color(56, 189, 248) : new Color(15, 23, 42));
                c.setForeground(isSelected ? Color.BLACK : Color.WHITE);
                return c;
            }
        };

        levelFilter = new JComboBox<>(levels);
        levelFilter.setUI(new javax.swing.plaf.basic.BasicComboBoxUI());
        levelFilter.setRenderer(darkRenderer);
        levelFilter.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        levelFilter.setBackground(new Color(15, 23, 42));
        levelFilter.setForeground(Color.WHITE);
        levelFilter.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(56, 189, 248), 1, true),
            BorderFactory.createEmptyBorder(2, 5, 2, 5)
        ));
        levelFilter.addActionListener(e -> updateTable());

        JLabel sortLabel = new JLabel("Sort:");
        sortLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        sortLabel.setForeground(Color.WHITE);
        String[] sortOptions = {"Highest First", "Lowest First"};
        sortOrder = new JComboBox<>(sortOptions);
        sortOrder.setUI(new javax.swing.plaf.basic.BasicComboBoxUI());
        sortOrder.setRenderer(darkRenderer);
        sortOrder.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        sortOrder.setBackground(new Color(15, 23, 42));
        sortOrder.setForeground(Color.WHITE);
        sortOrder.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(56, 189, 248), 1, true),
            BorderFactory.createEmptyBorder(2, 5, 2, 5)
        ));
        sortOrder.addActionListener(e -> updateTable());

        controlPanel.add(searchLabel);
        controlPanel.add(searchField);
        controlPanel.add(levelLabel);
        controlPanel.add(levelFilter);
        controlPanel.add(sortLabel);
        controlPanel.add(sortOrder);

        // table
        String[] columns = {"Rank", "Player", "Score", "Level", "Date"};
        tableModel = new DefaultTableModel(new Object[0][0], columns) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setOpaque(false);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setRowHeight(50);
        
        // Custom Header
        table.getTableHeader().setOpaque(false);
        table.getTableHeader().setBackground(new Color(15, 23, 42));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 18));
        
        table.getTableHeader().setDefaultRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setBackground(new Color(15, 23, 42));
                c.setForeground(Color.WHITE);
                c.setFont(new Font("Segoe UI", Font.BOLD, 18));
                
                if (column <= 3) {
                    setHorizontalAlignment(SwingConstants.CENTER);
                } else {
                    setHorizontalAlignment(SwingConstants.LEFT);
                }
                
                setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
                return c;
            }
        });

        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            Color[] rowColors = {
                new Color(247, 133, 60), 
                new Color(236, 156, 51), 
                new Color(95, 191, 80),  
                new Color(63, 166, 245),  
                new Color(217, 66, 84),  
                new Color(73, 153, 225),  
                new Color(254, 189, 39), 
                new Color(68, 162, 72),  
                new Color(55, 197, 241),  
                new Color(167, 85, 224), 
                new Color(185, 85, 237)  
            };

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                int colorIndex = row % rowColors.length;
                Color bg = rowColors[colorIndex];
                c.setBackground(bg);
                
                double luminance = (0.299 * bg.getRed() + 0.587 * bg.getGreen() + 0.114 * bg.getBlue()) / 255.0;
                if (luminance > 0.6) {
                    c.setForeground(Color.BLACK);
                } else {
                    c.setForeground(Color.WHITE);
                }
                
                if (column == 0 || column == 2) {
                    setFont(new Font("Consolas", Font.BOLD, 20)); 
                } else {
                    setFont(new Font("Segoe UI", Font.BOLD, 18)); 
                }
                
                if (column <= 3) { 
                    setHorizontalAlignment(SwingConstants.CENTER);
                } else {
                    setHorizontalAlignment(SwingConstants.LEFT);
                }
                
                setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // Back Button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        GlowingButton backButton = new GlowingButton("Back to Menu", new Color(150, 150, 150), false);
        backButton.setPreferredSize(new Dimension(200, 50));
        backButton.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        backButton.setForeground(Color.WHITE);
        backButton.addActionListener(e -> game.showPanel("menu"));
        bottomPanel.add(backButton);

        wrapperPanel.add(titleLabel, BorderLayout.NORTH);
        
        JPanel centerContainer = new JPanel(new BorderLayout(0, 20));
        centerContainer.setOpaque(false);
        centerContainer.add(controlPanel, BorderLayout.NORTH);
        centerContainer.add(scrollPane, BorderLayout.CENTER);
        
        wrapperPanel.add(centerContainer, BorderLayout.CENTER);
        wrapperPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(wrapperPanel, BorderLayout.CENTER);

        updateTable();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();

        Color centerColor = new Color(30, 58, 138); 
        Color edgeColor = new Color(15, 23, 42); 
        
        java.awt.geom.Point2D center = new java.awt.geom.Point2D.Float(w / 2f, h / 2f);
        float radius = (float) Math.max(w, h);
        float[] dist = {0.0f, 1.0f};
        Color[] colors = {centerColor, edgeColor};
        java.awt.RadialGradientPaint p = new java.awt.RadialGradientPaint(center, radius, dist, colors);
        
        g2.setPaint(p);
        g2.fillRect(0, 0, w, h);
        g2.dispose();
    }

    private void updateTable() {
        tableModel.setRowCount(0);

        ArrayList<Score> allScores = game.getGameData().getFlatScores();

        // Apply level filter
        int selectedLevel = levelFilter.getSelectedIndex();
        if (selectedLevel > 0) {
            allScores.removeIf(s -> s.getLevel() != selectedLevel);
        }

        // Apply search filter
        String searchText = searchField.getText().toLowerCase().trim();
        if (!searchText.isEmpty()) {
            allScores.removeIf(s -> !s.getPlayerName().toLowerCase().contains(searchText));
        }

        // Apply sort order
        if (sortOrder.getSelectedIndex() == 0) {
            allScores.sort((a, b) -> Integer.compare(b.getScore(), a.getScore()));
        } else {
            allScores.sort((a, b) -> Integer.compare(a.getScore(), b.getScore()));
        }

        // Populate table
        for (int i = 0; i < allScores.size(); i++) {
            Score score = allScores.get(i);
            tableModel.addRow(new Object[]{
                i + 1,
                score.getPlayerName(),
                score.getScore(),
                "Level " + score.getLevel(),
                score.getDate()
            });
        }
    }
}

class GameBoardPanel extends JPanel {
    private CodeCluster game;
    private SoundManager soundManager;
    private String playerName;
    private int level;
    private ArrayList<Category> categories;
    private ArrayList<String> allWords;
    private LinkedList<String> selectedWords;
    private HashSet<Category> solvedCategories;
    private int mistakes = 0;
    private int mistakesLimit = 4;
    private int timeLeft = 120;
    private javax.swing.Timer timer;
    private JPanel wordsPanel;
    private JPanel solvedPanel;
    private JLabel timerLabel;
    private JLabel mistakesLabel;
    private JLabel hintLabel;
    private Queue<String> hintQueue;
    private long startTime;
    private boolean gameOver = false;

    public GameBoardPanel(CodeCluster game, String playerName, int level) {
        this.game = game;
        this.soundManager = game.getSoundManager();
        this.playerName = playerName;
        this.level = level;
        this.categories = game.getGameData().getLevelCategories(level);
        this.selectedWords = new LinkedList<>();
        this.solvedCategories = new HashSet<>();
        this.hintQueue = new LinkedList<>();
        this.allWords = new ArrayList<>();
        this.startTime = System.currentTimeMillis();

        // Set time and mistakes limit based on level
        if (level >= 1 && level <= 2) {
            timeLeft = 120; // 2 minutes
            mistakesLimit = 4;
        } else if (level >= 3 && level <= 4) {
            timeLeft = 180; // 3 minutes
            mistakesLimit = 5;
        } else if (level == 5) {
            timeLeft = 240; // 4 minutes
            mistakesLimit = 6;
        }

        for (Category cat : categories) {
            for (String word : cat.getWords()) {
                allWords.add(word);
            }
        }
        Collections.shuffle(allWords);

        setLayout(new BorderLayout());
        setBackground(new Color(99, 102, 241));

        setupUI();
        startTimer();
        // Start background music
        soundManager.playBackgroundMusic();
    }

    private void setupUI() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        JLabel levelLabel = new JLabel("Level " + level + " - Player: " + playerName);
        levelLabel.setFont(new Font("Segoe UI", Font.PLAIN, 26));
        levelLabel.setForeground(new Color(56, 189, 248));

        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        statsPanel.setOpaque(false);

        // Display correct initial time based on level
        String initialTime;
        if (level >= 1 && level <= 2) {
            initialTime = "2:00";
        } else if (level >= 3 && level <= 4) {
            initialTime = "3:00";
        } else if (level == 5) {
            initialTime = "4:00";
        } else {
            initialTime = "2:00";
        }

        timerLabel = new JLabel(initialTime);
        timerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 26));
        timerLabel.setForeground(new Color(56, 189, 248));

        mistakesLabel = new JLabel("Mistakes: 0/" + mistakesLimit);
        mistakesLabel.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        mistakesLabel.setForeground(Color.WHITE);

        statsPanel.add(timerLabel);
        statsPanel.add(Box.createRigidArea(new Dimension(30, 0)));
        statsPanel.add(mistakesLabel);

        topPanel.add(levelLabel, BorderLayout.WEST);
        topPanel.add(statsPanel, BorderLayout.EAST);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 50, 10, 50));

        JLabel instructionLabel = new JLabel("Create four groups of four!", SwingConstants.CENTER);
        instructionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 26));
        instructionLabel.setForeground(new Color(56, 189, 248));
        instructionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        hintLabel = new JLabel(" ");
        hintLabel.setFont(new Font("Arial", Font.BOLD, 18));
        hintLabel.setForeground(Color.YELLOW);
        hintLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        solvedPanel = new JPanel();
        solvedPanel.setLayout(new BoxLayout(solvedPanel, BoxLayout.Y_AXIS));
        solvedPanel.setOpaque(false);

        wordsPanel = new JPanel(new GridLayout(0, 4, 15, 15));
        wordsPanel.setOpaque(false);
        updateWordsPanel();

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonsPanel.setOpaque(false);

        JButton shuffleButton = createBottomButton("Shuffle");
        shuffleButton.addActionListener(e -> shuffleWords());

        JButton deselectButton = createBottomButton("Deselect All");
        deselectButton.addActionListener(e -> deselectAll());

        JButton submitButton = createBottomButton("Submit");
        submitButton.addActionListener(e -> submitGuess());

        JButton backButton = createBottomButton("Back to Menu");
        backButton.addActionListener(e -> {
            soundManager.playClick();
            timer.stop();
            game.showPanel("menu");
        });

        buttonsPanel.add(shuffleButton);
        buttonsPanel.add(deselectButton);
        buttonsPanel.add(submitButton);
        buttonsPanel.add(backButton);

        centerPanel.add(instructionLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        centerPanel.add(hintLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        centerPanel.add(solvedPanel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        centerPanel.add(wordsPanel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        centerPanel.add(buttonsPanel);

        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
    }

    private JButton createBottomButton(String text) {
        GlowingButton btn = new GlowingButton(text, new Color(200, 200, 220), false);
        btn.setPreferredSize(new Dimension(160, 50));
        btn.setMaximumSize(new Dimension(160, 50));
        btn.setFont(new Font("Arial", Font.PLAIN, 18));
        btn.setForeground(Color.WHITE);
        return btn;
    }

    private void updateWordsPanel() {
        wordsPanel.removeAll();
        for (String word : allWords) {
            boolean isSolved = false;
            for (Category cat : solvedCategories) {
                for (String w : cat.getWords()) {
                    if (w.equals(word)) {
                        isSolved = true;
                        break;
                    }
                }
            }

            if (!isSolved) {
                Color glow = selectedWords.contains(word) ? new Color(34, 197, 94) : new Color(56, 189, 248);
                GlowingButton wordButton = new GlowingButton(word, glow, true);
                wordButton.setHoverGreen(true);
                wordButton.setPreferredSize(new Dimension(160, 60)); // for GridLayout
                wordButton.setFont(new Font("Arial", Font.BOLD, 18));
                wordButton.setForeground(new Color(15, 23, 42));
                
                wordButton.addActionListener(e -> {
                    soundManager.playClick();
                    toggleWord(word);
                });
                wordsPanel.add(wordButton);
            }
        }
        wordsPanel.revalidate();
        wordsPanel.repaint();
    }

    private void toggleWord(String word) {
        if (gameOver) return;
        if (selectedWords.contains(word)) {
            selectedWords.remove(word);
        } else if (selectedWords.size() < 4) {
            selectedWords.add(word);
        }
        updateWordsPanel();
    }

    private void shuffleWords() {
        if (gameOver) return;
        ArrayList<String> unsolved = new ArrayList<>();
        for (String word : allWords) {
            boolean isSolved = false;
            for (Category cat : solvedCategories) {
                for (String w : cat.getWords()) {
                    if (w.equals(word)) {
                        isSolved = true;
                        break;
                    }
                }
            }
            if (!isSolved) unsolved.add(word);
        }
        Collections.shuffle(unsolved);

        allWords.clear();
        for (Category cat : solvedCategories) {
            for (String w : cat.getWords()) {
                allWords.add(w);
            }
        }
        allWords.addAll(unsolved);
        updateWordsPanel();
    }

    private void deselectAll() {
        if (gameOver) return;
        selectedWords.clear();
        updateWordsPanel();
    }

    private Color getDifficultyColor(int difficulty) {
        switch (difficulty) {
            case 1:
                return new Color(234, 179, 8);        // Yellow (easiest)
            case 2:
                return new Color(34, 197, 94);        // Green
            case 3:
                return new Color(59, 130, 246);       // Blue
            case 4:
                return new Color(168, 85, 247);       // Purple (most difficult)
            default:
                return new Color(251, 191, 36);       // Default yellow
        }
    }

    private void submitGuess() {
        if (gameOver) return;
        if (selectedWords.size() != 4) return;

        Category matchedCategory = null;
        for (Category cat : categories) {
            if (!solvedCategories.contains(cat) && cat.matches(selectedWords)) {
                matchedCategory = cat;
                break;
            }
        }

        if (matchedCategory != null) {
            solvedCategories.add(matchedCategory);
            selectedWords.clear();
            timeLeft += 2;
            hintQueue.clear();
            hintQueue.offer(" ");
            hintLabel.setText(" ");

            Color difficultyColor = getDifficultyColor(matchedCategory.getDifficulty());
            GlowingButton catLabel = new GlowingButton(matchedCategory.getName() + ": " +
                String.join(", ", matchedCategory.getWords()), difficultyColor, true);
            catLabel.setPreferredSize(new Dimension(800, 60));
            catLabel.setMaximumSize(new Dimension(800, 60));
            catLabel.setFont(new Font("Arial", Font.BOLD, 18));
            catLabel.setForeground(new Color(15, 23, 42));
            catLabel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

            solvedPanel.add(catLabel);
            solvedPanel.add(Box.createRigidArea(new Dimension(0, 10)));

            if (solvedCategories.size() == categories.size()) {
                endGame();
            }
        } else {
            int maxMatch = 0;
            for (Category cat : categories) {
                if (!solvedCategories.contains(cat)) {
                    int match = cat.getMatchCount(selectedWords);
                    maxMatch = Math.max(maxMatch, match);
                }
            }

            if (maxMatch == 3) {
                hintQueue.clear();
                String hint = "One away! You're so close!";
                hintQueue.offer(hint);
                hintLabel.setText(hint);
            } else if (maxMatch == 2) {
                hintQueue.clear();
                String hint = "Two away! Keep trying!";
                hintQueue.offer(hint);
                hintLabel.setText(hint);
            } else {
                hintQueue.clear();
                hintQueue.offer(" ");
                hintLabel.setText(" ");
            }

            mistakes++;
            mistakesLabel.setText("Mistakes: " + mistakes + "/" + mistakesLimit);
            selectedWords.clear();

            if (mistakes >= mistakesLimit) {
                endGame();
            }
        }

        updateWordsPanel();
        revalidate();
        repaint();
    }

    private void startTimer() {
        timer = new javax.swing.Timer(1000, e -> {
            timeLeft--;
            int minutes = timeLeft / 60;
            int seconds = timeLeft % 60;
            timerLabel.setText(String.format("%d:%02d", minutes, seconds));

            if (timeLeft <= 0) {
                timer.stop();
                endGame();
            }
        });
        timer.start();
    }

    private void endGame() {
        if (gameOver) return;
        gameOver = true;
        timer.stop();

        long timeTaken = (System.currentTimeMillis() - startTime) / 1000;
        ArrayList<String> achievements = new ArrayList<>();
        int achievementScore = 0;
        boolean levelCompleted = solvedCategories.size() == categories.size();

        if (levelCompleted) {
            if (timeTaken < 60) {
                achievementScore += addAchievement(achievements, "Speed Demon", 500);
            }
            if (mistakes == 0) {
                achievementScore += addAchievement(achievements, "Perfect Game", 1000);
            }
            achievementScore += addAchievement(achievements, "Master Mind", 750);
            if (timeLeft >= 30) {
                achievementScore += addAchievement(achievements, "Time Lord", 300);
            }
        }

        int baseScore = solvedCategories.size() * 100;
        int timeBonus = Math.max(0, timeLeft * 10);
        int totalScore = baseScore + timeBonus + achievementScore - (mistakes * 50);

        if (!levelCompleted && mistakes >= 4) {
            totalScore = 0;
        }

        // Only save score if level was completed successfully
        if (levelCompleted && totalScore > 0) {
            Score score = new Score(playerName, totalScore, level, 1, achievements);
            game.getGameData().addScore(score);
        }

        // Play appropriate sound effect and stop BGM
        soundManager.stopBackgroundMusic();
        if (levelCompleted) {
            soundManager.playWinSound();
        } else {
            soundManager.playGameOverSound();
        }

        showGameCompleteDialog(totalScore, achievements, levelCompleted);
    }

    private int addAchievement(ArrayList<String> achievements, String name, int value) {
        String sign = value >= 0 ? "+" : "";
        achievements.add(name + " (" + sign + value + ")");
        return value;
    }

    private void showGameCompleteDialog(int score, ArrayList<String> achievements, boolean levelCompleted) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Code Cluster", true);
        dialog.setSize(500, levelCompleted ? 650 : 520);
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();

                Color centerColor = new Color(30, 58, 138); 
                Color edgeColor = new Color(15, 23, 42); 
                
                java.awt.geom.Point2D center = new java.awt.geom.Point2D.Float(w / 2f, h / 2f);
                float radius = (float) Math.max(w, h);
                float[] dist = {0.0f, 1.0f};
                Color[] colors = {centerColor, edgeColor};
                RadialGradientPaint p = new RadialGradientPaint(center, radius, dist, colors);
                
                g2.setPaint(p);
                g2.fillRect(0, 0, w, h);
                g2.dispose();
            }
        };
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(40, 30, 40, 30));

        String titleText = levelCompleted ? (level == 5 ? "Game Complete!" : "Level Complete!") : "GAME OVER!";
        
        JLabel titleLabel = new JLabel(titleText, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                FontMetrics fm = g2.getFontMetrics(getFont());
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = fm.getAscent();
                
                Color glow = levelCompleted ? new Color(56, 189, 248) : new Color(239, 68, 68);
                
                for (int i = 1; i <= 6; i++) {
                    g2.setColor(new Color(glow.getRed(), glow.getGreen(), glow.getBlue(), 30 - i * 4));
                    g2.drawString(getText(), x - i, y);
                    g2.drawString(getText(), x + i, y);
                    g2.drawString(getText(), x, y - i);
                    g2.drawString(getText(), x, y + i);
                }
                
                if (!levelCompleted) {
                    g2.setColor(glow);
                    g2.drawString(getText(), x - 1, y - 1);
                    g2.drawString(getText(), x + 1, y - 1);
                    g2.drawString(getText(), x - 1, y + 1);
                    g2.drawString(getText(), x + 1, y + 1);
                    g2.setColor(new Color(15, 23, 42)); 
                } else {
                    g2.setColor(new Color(224, 242, 254));
                }
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 46));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setMaximumSize(new Dimension(500, 70));
        mainPanel.add(titleLabel);

        if (!levelCompleted) {
            JLabel subLabel = new JLabel("Out of Mistakes", SwingConstants.CENTER);
            subLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
            subLabel.setForeground(Color.WHITE);
            subLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            mainPanel.add(subLabel);
            mainPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        } else {
            mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            JLabel scoreLabel = new JLabel("Score: " + score, SwingConstants.CENTER) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    FontMetrics fm = g2.getFontMetrics(getFont());
                    int x = (getWidth() - fm.stringWidth(getText())) / 2;
                    int y = fm.getAscent();
                    
                    Color glow = new Color(251, 191, 36); 
                    for (int i = 1; i <= 4; i++) {
                        g2.setColor(new Color(glow.getRed(), glow.getGreen(), glow.getBlue(), 40 - i * 8));
                        g2.drawString(getText(), x - i, y);
                        g2.drawString(getText(), x + i, y);
                        g2.drawString(getText(), x, y - i);
                        g2.drawString(getText(), x, y + i);
                    }
                    g2.setColor(new Color(253, 230, 138));
                    g2.drawString(getText(), x, y);
                    g2.dispose();
                }
            };
            scoreLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
            scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            scoreLabel.setMaximumSize(new Dimension(500, 50));
            mainPanel.add(scoreLabel);
            mainPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        }

        DarkRoundedPanel infoPanel = new DarkRoundedPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        infoPanel.setMaximumSize(new Dimension(420, 300));

        if (!levelCompleted) {
            JPanel p1 = new JPanel(new GridBagLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(0, 0, 0, 80));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                    g2.dispose();
                }
            };
            p1.setOpaque(false);
            p1.setMaximumSize(new Dimension(360, 60));
            p1.setPreferredSize(new Dimension(360, 60));
            
            JLabel scoreTextLabel = new JLabel("Score: " + score, SwingConstants.CENTER);
            scoreTextLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
            scoreTextLabel.setForeground(new Color(239, 68, 68)); 
            p1.add(scoreTextLabel);

            infoPanel.add(p1);
            infoPanel.add(Box.createRigidArea(new Dimension(0, 25)));
        }

        JLabel achTitle = new JLabel("Achievements:", SwingConstants.CENTER);
        achTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        achTitle.setForeground(Color.WHITE);
        achTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        infoPanel.add(achTitle);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        if (achievements.isEmpty()) {
            JLabel noneLabel = new JLabel("None", SwingConstants.CENTER);
            noneLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
            noneLabel.setForeground(new Color(156, 163, 175));
            noneLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            infoPanel.add(noneLabel);
        } else {
            ImageIcon starIcon = null;
            try {
                ImageIcon original = new ImageIcon("star.png");
                if (original.getIconWidth() > 0) {
                    Image img = original.getImage();
                    
                    java.awt.image.ImageFilter filter = new java.awt.image.RGBImageFilter() {
                        @Override
                        public final int filterRGB(int x, int y, int rgb) {
                            int r = (rgb >> 16) & 0xFF;
                            int g = (rgb >> 8) & 0xFF;
                            int b = rgb & 0xFF;
                            if (r >= 250 && g >= 250 && b >= 250) {
                                return 0x00FFFFFF & rgb;
                            }
                            return rgb;
                        }
                    };
                    java.awt.image.ImageProducer ip = new java.awt.image.FilteredImageSource(img.getSource(), filter);
                    img = java.awt.Toolkit.getDefaultToolkit().createImage(ip);
                    
                    img = img.getScaledInstance(24, 24, Image.SCALE_SMOOTH);
                    starIcon = new ImageIcon(img);
                }
            } catch (Exception e) {}

            for (String ach : achievements) {
                JPanel itemPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10)) {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(new Color(255, 255, 255, 15));
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                        g2.setColor(new Color(255, 255, 255, 30));
                        g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                        g2.dispose();
                    }
                };
                itemPanel.setOpaque(false);
                itemPanel.setMaximumSize(new Dimension(360, 45));
                
                JLabel achItem = new JLabel(ach);
                if (starIcon != null) {
                    achItem.setIcon(starIcon);
                    achItem.setIconTextGap(10);
                } else {
                    String iconStr = "⭐";
                    if (ach.contains("Speed Demon")) iconStr = "⚡";
                    else if (ach.contains("Master Mind")) iconStr = "🧠";
                    else if (ach.contains("Time Lord")) iconStr = "⏳";
                    else if (ach.contains("Perfect Game")) iconStr = "🏆";
                    achItem.setText(iconStr + "  " + ach);
                }
                
                achItem.setFont(new Font("Segoe UI", Font.PLAIN, 18));
                achItem.setForeground(Color.WHITE);
                itemPanel.add(achItem);
                
                infoPanel.add(itemPanel);
                infoPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        }

        mainPanel.add(infoPanel);
        mainPanel.add(Box.createVerticalGlue());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setOpaque(false);

        if (levelCompleted && level < 5) {
            GlowingButton nextButton = new GlowingButton("Next Level", new Color(34, 197, 94), false);
            nextButton.setPreferredSize(new Dimension(190, 55));
            nextButton.setMinimumSize(new Dimension(190, 55));
            nextButton.setMaximumSize(new Dimension(190, 55));
            nextButton.setFont(new Font("Segoe UI", Font.BOLD, 20));
            nextButton.setForeground(Color.WHITE);
            nextButton.addActionListener(e -> {
                soundManager.playClick();
                dialog.dispose();
                game.nextLevel();
            });
            buttonPanel.add(nextButton);
        } else if (!levelCompleted) {
            GlowingButton tryAgainButton = new GlowingButton("Try Again", new Color(239, 68, 68), false);
            tryAgainButton.setPreferredSize(new Dimension(190, 55));
            tryAgainButton.setMinimumSize(new Dimension(190, 55));
            tryAgainButton.setMaximumSize(new Dimension(190, 55));
            tryAgainButton.setFont(new Font("Segoe UI", Font.BOLD, 20));
            tryAgainButton.setForeground(Color.WHITE);
            tryAgainButton.addActionListener(e -> {
                soundManager.playClick();
                dialog.dispose();
                game.startGame(playerName);
            });
            buttonPanel.add(tryAgainButton);
        }

        GlowingButton menuButton = new GlowingButton("Main Menu", new Color(150, 150, 150), false);
        menuButton.setPreferredSize(new Dimension(190, 55));
        menuButton.setMinimumSize(new Dimension(190, 55));
        menuButton.setMaximumSize(new Dimension(190, 55));
        menuButton.setFont(new Font("Segoe UI", Font.BOLD, 20));
        menuButton.setForeground(Color.WHITE);
        menuButton.addActionListener(e -> {
            soundManager.playClick();
            dialog.dispose();
            game.showPanel("menu");
        });
        buttonPanel.add(menuButton);

        mainPanel.add(buttonPanel);

        dialog.setContentPane(mainPanel);
        dialog.setVisible(true);
    }
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();

        Color centerColor = new Color(30, 58, 138); 
        Color edgeColor = new Color(15, 23, 42); 
        
        java.awt.geom.Point2D center = new java.awt.geom.Point2D.Float(w / 2f, h / 2f);
        float radius = (float) Math.max(w, h);
        float[] dist = {0.0f, 1.0f};
        Color[] colors = {centerColor, edgeColor};
        RadialGradientPaint p = new RadialGradientPaint(center, radius, dist, colors);
        
        g2.setPaint(p);
        g2.fillRect(0, 0, w, h);
        g2.dispose();
    }
}