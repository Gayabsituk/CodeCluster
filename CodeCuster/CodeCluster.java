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
    }

    public boolean isSoundEnabled() {
        return soundEnabled;
    }
}

class GameData {
    private LinkedList<String> playerNames;
    private TreeMap<String, ArrayList<Score>> scores;
    private HashMap<Integer, ArrayList<ArrayList<Category>>> levelData;
    private boolean sfxEnabled;
    private Stack<GameState> gameHistory;

    public GameData() {
        playerNames = new LinkedList<>();
        scores = new TreeMap<>();
        levelData = new HashMap<>();
        sfxEnabled = true;
        gameHistory = new Stack<>();
        initializeLevelData();
    }

    private void initializeLevelData() {
        // Level 1 - 2 alternative sets of 4 categories
        ArrayList<ArrayList<Category>> level1Sets = new ArrayList<>();
        ArrayList<Category> level1a = new ArrayList<>();
        level1a.add(new Category("Java Keywords", new String[]{"STATIC", "VOID", "CLASS", "PUBLIC"}));
        level1a.add(new Category("Data Structures", new String[]{"ARRAY", "STACK", "QUEUE", "TREE"}));
        level1a.add(new Category("Primitive Types", new String[]{"INT", "BOOLEAN", "CHAR", "DOUBLE"}));
        level1a.add(new Category("Common Methods", new String[]{"GET", "SET", "PUSH", "POP"}));
        level1Sets.add(level1a);

        ArrayList<Category> level1b = new ArrayList<>();
        level1b.add(new Category("Control Keywords", new String[]{"IF", "ELSE", "SWITCH", "CASE"}));
        level1b.add(new Category("Collection Types", new String[]{"LIST", "SET", "MAP", "QUEUE"}));
        level1b.add(new Category("Numeric Types", new String[]{"BYTE", "SHORT", "INT", "LONG"}));
        level1b.add(new Category("Common Operators", new String[]{"PLUS", "MINUS", "MULTIPLY", "DIVIDE"}));
        level1Sets.add(level1b);
        levelData.put(1, level1Sets);

        // Level 2 - 2 alternative sets of 4 categories
        ArrayList<ArrayList<Category>> level2Sets = new ArrayList<>();
        ArrayList<Category> level2a = new ArrayList<>();
        level2a.add(new Category("OOP Concepts", new String[]{"POLYMORPHISM", "INHERITANCE", "ENCAPSULATION", "ABSTRACTION"}));
        level2a.add(new Category("Loop Keywords", new String[]{"FOR", "WHILE", "DO", "FOREACH"}));
        level2a.add(new Category("String Methods", new String[]{"LENGTH", "SUBSTRING", "CONCAT", "TRIM"}));
        level2a.add(new Category("Boolean Operators", new String[]{"AND", "OR", "NOT", "XOR"}));
        level2Sets.add(level2a);

        ArrayList<Category> level2b = new ArrayList<>();
        level2b.add(new Category("Array Methods", new String[]{"SORT", "FILL", "COPY", "BINARYSEARCH"}));
        level2b.add(new Category("Exception Types", new String[]{"IOEXCEPTION", "NULLPOINTER", "ARITHMETIC", "INDEXOUTOFBOUNDS"}));
        level2b.add(new Category("Access Modifiers", new String[]{"PUBLIC", "PRIVATE", "PROTECTED", "DEFAULT"}));
        level2b.add(new Category("Inheritance Keywords", new String[]{"EXTENDS", "IMPLEMENTS", "SUPER", "THIS"}));
        level2Sets.add(level2b);
        levelData.put(2, level2Sets);

        // Level 3 - 2 alternative sets of 5 categories
        ArrayList<ArrayList<Category>> level3Sets = new ArrayList<>();
        ArrayList<Category> level3a = new ArrayList<>();
        level3a.add(new Category("Java Collections", new String[]{"LIST", "MAP", "SET", "HASHMAP"}));
        level3a.add(new Category("Exception Handling", new String[]{"TRY", "CATCH", "THROW", "FINALLY"}));
        level3a.add(new Category("Access Modifiers", new String[]{"PRIVATE", "PROTECTED", "PACKAGE", "DEFAULT"}));
        level3a.add(new Category("Wrapper Classes", new String[]{"INTEGER", "LONG", "FLOAT", "CHARACTER"}));
        level3a.add(new Category("Keywords", new String[]{"FINAL", "SUPER", "THIS", "EXTENDS"}));
        level3Sets.add(level3a);

        ArrayList<Category> level3b = new ArrayList<>();
        level3b.add(new Category("Stream Methods", new String[]{"FILTER", "MAP", "COLLECT", "FOR_EACH"}));
        level3b.add(new Category("Thread States", new String[]{"NEW", "RUNNABLE", "BLOCKED", "TERMINATED"}));
        level3b.add(new Category("Numeric Wrappers", new String[]{"DOUBLE", "FLOAT", "INTEGER", "LONG"}));
        level3b.add(new Category("String Builders", new String[]{"APPEND", "INSERT", "DELETE", "TOSTRING"}));
        level3b.add(new Category("Concurrency", new String[]{"SYNCHRONIZED", "VOLATILE", "LOCK", "ATOMIC"}));
        level3Sets.add(level3b);
        levelData.put(3, level3Sets);

        // Level 4 - 2 alternative sets of 5 categories
        ArrayList<ArrayList<Category>> level4Sets = new ArrayList<>();
        ArrayList<Category> level4a = new ArrayList<>();
        level4a.add(new Category("Design Patterns", new String[]{"SINGLETON", "FACTORY", "OBSERVER", "DECORATOR"}));
        level4a.add(new Category("Testing Terms", new String[]{"JUNIT", "MOCK", "ASSERT", "TEST"}));
        level4a.add(new Category("Thread States", new String[]{"NEW", "RUNNABLE", "BLOCKED", "WAITING"}));
        level4a.add(new Category("Memory Areas", new String[]{"HEAP", "STACK", "METASPACE", "POOL"}));
        level4a.add(new Category("Synchronization", new String[]{"LOCK", "SYNCHRONIZED", "VOLATILE", "ATOMIC"}));
        level4Sets.add(level4a);

        ArrayList<Category> level4b = new ArrayList<>();
        level4b.add(new Category("Build Tools", new String[]{"MAVEN", "GRADLE", "ANT", "NPM"}));
        level4b.add(new Category("Web Concepts", new String[]{"HTTP", "HTTPS", "REST", "SOAP"}));
        level4b.add(new Category("Database Terms", new String[]{"SQL", "INDEX", "JOIN", "TRANSACTION"}));
        level4b.add(new Category("Caching", new String[]{"MEMCACHED", "REDIS", "GUAVA", "CACHE"}));
        level4b.add(new Category("Testing Frameworks", new String[]{"TESTNG", "SPOCK", "CUCUMBER", "MOCKITO"}));
        level4Sets.add(level4b);
        levelData.put(4, level4Sets);

        // Level 5 - 2 alternative sets of 6 categories
        ArrayList<ArrayList<Category>> level5Sets = new ArrayList<>();
        ArrayList<Category> level5a = new ArrayList<>();
        level5a.add(new Category("Spring Framework", new String[]{"BEAN", "AUTOWIRED", "COMPONENT", "SERVICE"}));
        level5a.add(new Category("SQL Keywords", new String[]{"SELECT", "INSERT", "UPDATE", "DELETE"}));
        level5a.add(new Category("Git Commands", new String[]{"COMMIT", "PUSH", "PULL", "MERGE"}));
        level5a.add(new Category("HTTP Methods", new String[]{"GET", "POST", "PUT", "PATCH"}));
        level5a.add(new Category("JSON Operations", new String[]{"PARSE", "STRINGIFY", "SERIALIZE", "DESERIALIZE"}));
        level5a.add(new Category("Build Tools", new String[]{"MAVEN", "GRADLE", "ANT", "NPM"}));
        level5Sets.add(level5a);

        ArrayList<Category> level5b = new ArrayList<>();
        level5b.add(new Category("Cloud Platforms", new String[]{"AZURE", "AWS", "GCP", "HEROKU"}));
        level5b.add(new Category("CI/CD", new String[]{"JENKINS", "GITHUB", "GITLAB", "AZUREDEVOPS"}));
        level5b.add(new Category("Container Tools", new String[]{"DOCKER", "KUBERNETES", "PODMAN", "SWARM"}));
        level5b.add(new Category("API Methods", new String[]{"GET", "POST", "DELETE", "PATCH"}));
        level5b.add(new Category("Data Formats", new String[]{"JSON", "XML", "YAML", "CSV"}));
        level5b.add(new Category("Security", new String[]{"OAUTH", "JWT", "SSL", "TLS"}));
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

    public Category(String name, String[] words) {
        this.name = name;
        this.words = words;
    }

    public String getName() {
        return name;
    }

    public String[] getWords() {
        return words;
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
        setBackground(new Color(99, 102, 241));

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(30, 100, 30, 100));

        JLabel titleLabel = new JLabel("CODE CLUSTER");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 72));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Find 4 words that share a category!");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 24));
        subtitleLabel.setForeground(Color.WHITE);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        centerPanel.add(Box.createVerticalGlue());
        centerPanel.add(titleLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        centerPanel.add(subtitleLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 80)));

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
        buttonsPanel.setOpaque(false);
        buttonsPanel.setMaximumSize(new Dimension(400, 400));
        buttonsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton startButton = createMenuButton("Start Game", new Color(34, 197, 94));
        startButton.addActionListener(e -> {
            soundManager.playClick();
            game.showPanel("nameSelection");
        });
        buttonsPanel.add(startButton);
        buttonsPanel.add(Box.createRigidArea(new Dimension(0, 25)));

        JButton settingsButton = createMenuButton("Settings", new Color(59, 130, 246));
        settingsButton.addActionListener(e -> {
            soundManager.playClick();
            game.showPanel("settings");
        });
        buttonsPanel.add(settingsButton);
        buttonsPanel.add(Box.createRigidArea(new Dimension(0, 25)));

        JButton leaderboardButton = createMenuButton("Leaderboard", new Color(249, 115, 22));
        leaderboardButton.addActionListener(e -> {
            soundManager.playClick();
            game.showPanel("leaderboard");
        });
        buttonsPanel.add(leaderboardButton);
        buttonsPanel.add(Box.createRigidArea(new Dimension(0, 25)));

        JButton exitButton = createMenuButton("Exit", new Color(239, 68, 68));
        exitButton.addActionListener(e -> {
            soundManager.playClick();
            System.exit(0);
        });
        buttonsPanel.add(exitButton);

        centerPanel.add(buttonsPanel);
        centerPanel.add(Box.createVerticalGlue());

        add(centerPanel, BorderLayout.CENTER);
    }

    private JButton createMenuButton(String text, Color bg) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 24));
        button.setBackground(bg);
        button.setForeground(Color.BLACK);
        button.setFocusable(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setPreferredSize(new Dimension(400, 70));
        button.setMinimumSize(new Dimension(400, 70));
        button.setMaximumSize(new Dimension(400, 70));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }
}



class NameSelectionPanel extends JPanel {
    public NameSelectionPanel(CodeCluster game, SoundManager soundManager) {
        setLayout(new BorderLayout());
        setBackground(new Color(99, 102, 241));

        // Main container with vertical centering
        JPanel mainContainer = new JPanel();
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));
        mainContainer.setOpaque(false);

        // Title section
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));

        JLabel titleLabel = new JLabel("Welcome Player!");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 48));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Choose your name to continue");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        subtitleLabel.setForeground(Color.WHITE);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        titlePanel.add(titleLabel);
        titlePanel.add(Box.createRigidArea(new Dimension(0, 15)));
        titlePanel.add(subtitleLabel);

        mainContainer.add(Box.createVerticalGlue());
        mainContainer.add(titlePanel);
        mainContainer.add(Box.createRigidArea(new Dimension(0, 50)));

        // White panel with input fields - horizontally centered
        JPanel whitePanel = new JPanel();
        whitePanel.setLayout(new BoxLayout(whitePanel, BoxLayout.Y_AXIS));
        whitePanel.setBackground(Color.WHITE);
        whitePanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        whitePanel.setMaximumSize(new Dimension(500, 400));

        JLabel enterLabel = new JLabel("Enter your name:");
        enterLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        whitePanel.add(enterLabel);
        whitePanel.add(Box.createRigidArea(new Dimension(0, 10)));

        JTextField nameField = new JTextField();
        nameField.setFont(new Font("Arial", Font.PLAIN, 20));
        nameField.setMaximumSize(new Dimension(440, 50));
        whitePanel.add(nameField);
        whitePanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Show saved names
        LinkedList<String> savedNames = game.getGameData().getPlayerNames();
        if (!savedNames.isEmpty()) {
            JLabel savedLabel = new JLabel("Or select a saved name:");
            savedLabel.setFont(new Font("Arial", Font.PLAIN, 16));
            whitePanel.add(savedLabel);
            whitePanel.add(Box.createRigidArea(new Dimension(0, 10)));

            JScrollPane scrollPane = new JScrollPane();
            JPanel namesPanel = new JPanel();
            namesPanel.setLayout(new BoxLayout(namesPanel, BoxLayout.Y_AXIS));
            namesPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

            for (String name : savedNames) {
                JButton nameButton = new JButton(name);
                nameButton.setMaximumSize(new Dimension(440, 40));
                nameButton.setAlignmentX(Component.CENTER_ALIGNMENT);
                nameButton.addActionListener(e -> {
                    soundManager.playClick();
                    game.startGame(name);
                });
                namesPanel.add(nameButton);
                namesPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            }

            scrollPane.setViewportView(namesPanel);
            scrollPane.setMaximumSize(new Dimension(440, 150));
            whitePanel.add(scrollPane);
            whitePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        JButton submitButton = new JButton("Start Playing");
        submitButton.setFont(new Font("Arial", Font.BOLD, 20));
        submitButton.setBackground(new Color(168, 85, 247));
        submitButton.setForeground(Color.WHITE);
        submitButton.setMaximumSize(new Dimension(440, 50));
        submitButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        submitButton.addActionListener(e -> {
            soundManager.playClick();
            String name = nameField.getText().trim();
            if (!name.isEmpty()) {
                game.getGameData().addPlayerName(name);
                game.startGame(name);
            }
        });
        whitePanel.add(submitButton);

        // Horizontal centering wrapper
        JPanel horizontalWrapper = new JPanel(new BorderLayout());
        horizontalWrapper.setOpaque(false);
        horizontalWrapper.add(Box.createHorizontalGlue(), BorderLayout.WEST);
        horizontalWrapper.add(whitePanel, BorderLayout.CENTER);
        horizontalWrapper.add(Box.createHorizontalGlue(), BorderLayout.EAST);

        mainContainer.add(horizontalWrapper);
        mainContainer.add(Box.createRigidArea(new Dimension(0, 40)));

        // Back button
        JButton backButton = new JButton("Back to Menu");
        backButton.setFont(new Font("Arial", Font.PLAIN, 16));
        backButton.setForeground(Color.WHITE);
        backButton.setContentAreaFilled(false);
        backButton.setBorderPainted(false);
        backButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        backButton.addActionListener(e -> {
            soundManager.playClick();
            game.showPanel("menu");
        });
        mainContainer.add(backButton);

        mainContainer.add(Box.createVerticalGlue());

        add(mainContainer, BorderLayout.CENTER);
    }
}

class SettingsPanel extends JPanel {
    public SettingsPanel(CodeCluster game, SoundManager soundManager) {
        setLayout(new BorderLayout());
        setBackground(new Color(99, 102, 241));

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(100, 200, 100, 200));

        JPanel whitePanel = new JPanel();
        whitePanel.setLayout(new BoxLayout(whitePanel, BoxLayout.Y_AXIS));
        whitePanel.setBackground(Color.WHITE);
        whitePanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        JLabel titleLabel = new JLabel("Settings");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 36));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JCheckBox sfxCheckBox = new JCheckBox("Sound Effects", game.getGameData().isSfxEnabled());
        sfxCheckBox.setFont(new Font("Arial", Font.PLAIN, 20));
        sfxCheckBox.addActionListener(e -> {
            game.getGameData().setSfxEnabled(sfxCheckBox.isSelected());
            soundManager.setSoundEnabled(sfxCheckBox.isSelected());
        });

        whitePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        whitePanel.add(titleLabel);
        whitePanel.add(Box.createRigidArea(new Dimension(0, 40)));
        sfxCheckBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        whitePanel.add(sfxCheckBox);
        whitePanel.add(Box.createRigidArea(new Dimension(0, 40)));

        JButton doneButton = new JButton("Done");
        doneButton.setFont(new Font("Arial", Font.BOLD, 20));
        doneButton.setBackground(new Color(168, 85, 247));
        doneButton.setForeground(Color.BLACK);
        doneButton.setMaximumSize(new Dimension(200, 50));
        doneButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        doneButton.addActionListener(e -> {
            soundManager.playClick();
            game.showPanel("menu");
        });

        whitePanel.add(doneButton);

        centerPanel.add(Box.createVerticalGlue());
        centerPanel.add(whitePanel);
        centerPanel.add(Box.createVerticalGlue());

        add(centerPanel, BorderLayout.CENTER);
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
        setBackground(new Color(99, 102, 241));

        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.setOpaque(false);
        wrapperPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        JPanel whitePanel = new JPanel(new BorderLayout());
        whitePanel.setBackground(Color.WHITE);
        whitePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Title
        JLabel titleLabel = new JLabel("Leaderboard", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 36));

        // Control Panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        controlPanel.setBackground(Color.WHITE);

        JLabel searchLabel = new JLabel("Search Player:");
        searchLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        searchField = new JTextField(15);
        searchField.setFont(new Font("Arial", Font.PLAIN, 14));
        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent e) {
                updateTable();
            }
        });

        JLabel levelLabel = new JLabel("Level:");
        levelLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        String[] levels = {"All Levels", "Level 1", "Level 2", "Level 3", "Level 4", "Level 5"};
        levelFilter = new JComboBox<>(levels);
        levelFilter.setFont(new Font("Arial", Font.PLAIN, 14));
        levelFilter.addActionListener(e -> updateTable());

        JLabel sortLabel = new JLabel("Sort:");
        sortLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        String[] sortOptions = {"Highest to Lowest", "Lowest to Highest"};
        sortOrder = new JComboBox<>(sortOptions);
        sortOrder.setFont(new Font("Arial", Font.PLAIN, 14));
        sortOrder.addActionListener(e -> updateTable());

        controlPanel.add(searchLabel);
        controlPanel.add(searchField);
        controlPanel.add(levelLabel);
        controlPanel.add(levelFilter);
        controlPanel.add(sortLabel);
        controlPanel.add(sortOrder);

        // Table
        String[] columns = {"Rank", "Player", "Score", "Level", "Date"};
        tableModel = new DefaultTableModel(new Object[0][0], columns) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 16));

        JScrollPane scrollPane = new JScrollPane(table);

        // Back Button
        JButton backButton = new JButton("Back to Menu");
        backButton.setFont(new Font("Arial", Font.BOLD, 18));
        backButton.setBackground(new Color(168, 85, 247));
        backButton.setForeground(Color.BLACK);
        backButton.addActionListener(e -> game.showPanel("menu"));

        whitePanel.add(titleLabel, BorderLayout.NORTH);
        whitePanel.add(controlPanel, BorderLayout.BEFORE_FIRST_LINE);
        whitePanel.add(scrollPane, BorderLayout.CENTER);
        whitePanel.add(backButton, BorderLayout.SOUTH);

        wrapperPanel.add(whitePanel, BorderLayout.CENTER);
        add(wrapperPanel, BorderLayout.CENTER);

        updateTable();
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
    private ArrayList<String> selectedWords;
    private ArrayList<Category> solvedCategories;
    private int mistakes = 0;
    private int timeLeft = 120;
    private javax.swing.Timer timer;
    private JPanel wordsPanel;
    private JPanel solvedPanel;
    private JLabel timerLabel;
    private JLabel mistakesLabel;
    private JLabel hintLabel;
    private long startTime;

    public GameBoardPanel(CodeCluster game, String playerName, int level) {
        this.game = game;
        this.soundManager = game.getSoundManager();
        this.playerName = playerName;
        this.level = level;
        this.categories = game.getGameData().getLevelCategories(level);
        this.selectedWords = new ArrayList<>();
        this.solvedCategories = new ArrayList<>();
        this.allWords = new ArrayList<>();
        this.startTime = System.currentTimeMillis();

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
    }

    private void setupUI() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        JLabel levelLabel = new JLabel("Level " + level + " - Player: " + playerName);
        levelLabel.setFont(new Font("Arial", Font.BOLD, 24));
        levelLabel.setForeground(Color.WHITE);

        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        statsPanel.setOpaque(false);

        timerLabel = new JLabel("2:00");
        timerLabel.setFont(new Font("Arial", Font.BOLD, 24));
        timerLabel.setForeground(Color.WHITE);

        mistakesLabel = new JLabel("Mistakes: 0/4");
        mistakesLabel.setFont(new Font("Arial", Font.PLAIN, 18));
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
        instructionLabel.setFont(new Font("Arial", Font.BOLD, 20));
        instructionLabel.setForeground(Color.WHITE);
        instructionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        hintLabel = new JLabel(" ");
        hintLabel.setFont(new Font("Arial", Font.BOLD, 18));
        hintLabel.setForeground(Color.YELLOW);
        hintLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        solvedPanel = new JPanel();
        solvedPanel.setLayout(new BoxLayout(solvedPanel, BoxLayout.Y_AXIS));
        solvedPanel.setOpaque(false);

        wordsPanel = new JPanel(new GridLayout(0, 4, 10, 10));
        wordsPanel.setOpaque(false);
        updateWordsPanel();

        JPanel buttonsPanel = new JPanel(new FlowLayout());
        buttonsPanel.setOpaque(false);

        JButton shuffleButton = new JButton("Shuffle");
        JButton deselectButton = new JButton("Deselect All");
        JButton submitButton = new JButton("Submit");
        JButton backButton = new JButton("Back to Menu");

        shuffleButton.addActionListener(e -> shuffleWords());
        deselectButton.addActionListener(e -> deselectAll());
        submitButton.addActionListener(e -> submitGuess());
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
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        centerPanel.add(buttonsPanel);

        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
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
                JButton wordButton = new JButton(word);
                wordButton.setFont(new Font("Arial", Font.BOLD, 14));

                if (selectedWords.contains(word)) {
                    wordButton.setBackground(new Color(34, 197, 94));
                    wordButton.setForeground(Color.GREEN);
                } else {
                    wordButton.setBackground(new Color(243, 244, 246));
                    wordButton.setForeground(Color.BLACK);
                }

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
        if (selectedWords.contains(word)) {
            selectedWords.remove(word);
        } else if (selectedWords.size() < 4) {
            selectedWords.add(word);
        }
        updateWordsPanel();
    }

    private void shuffleWords() {
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
        selectedWords.clear();
        updateWordsPanel();
    }

    private void submitGuess() {
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
            hintLabel.setText(" ");

            JPanel categoryPanel = new JPanel();
            categoryPanel.setBackground(new Color(251, 191, 36));
            categoryPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
            categoryPanel.setMaximumSize(new Dimension(800, 80));

            JLabel catLabel = new JLabel(matchedCategory.getName() + ": " +
                String.join(", ", matchedCategory.getWords()));
            catLabel.setFont(new Font("Arial", Font.BOLD, 16));
            categoryPanel.add(catLabel);

            solvedPanel.add(categoryPanel);
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
                hintLabel.setText("One away! You're so close!");
            } else if (maxMatch == 2) {
                hintLabel.setText("Two away! Keep trying!");
            } else {
                hintLabel.setText(" ");
            }

            mistakes++;
            mistakesLabel.setText("Mistakes: " + mistakes + "/4");
            selectedWords.clear();

            if (mistakes >= 4) {
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

        showGameCompleteDialog(totalScore, achievements, levelCompleted);
    }

    private int addAchievement(ArrayList<String> achievements, String name, int value) {
        String sign = value >= 0 ? "+" : "";
        achievements.add(name + " (" + sign + value + ")");
        return value;
    }

    private void showGameCompleteDialog(int score, ArrayList<String> achievements, boolean levelCompleted) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), levelCompleted ? "Level Complete!" : "Game Over", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        String title;
        if (levelCompleted) {
            title = level == 5 ? "Game Complete!" : "Level Complete!";
        } else {
            title = "Game Over!";
        }
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 36));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel scoreLabel = new JLabel("Score: " + score);
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 32));
        scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(titleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(scoreLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        if (levelCompleted && !achievements.isEmpty()) {
            JLabel achLabel = new JLabel("Achievements:");
            achLabel.setFont(new Font("Arial", Font.BOLD, 20));
            achLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(achLabel);

            for (String ach : achievements) {
                JLabel achItem = new JLabel("• " + ach);
                achItem.setFont(new Font("Arial", Font.PLAIN, 16));
                achItem.setAlignmentX(Component.CENTER_ALIGNMENT);
                panel.add(achItem);
            }
        }

        panel.add(Box.createRigidArea(new Dimension(0, 30)));

        JPanel buttonPanel = new JPanel(new FlowLayout());

        if (level < 5) {
            JButton nextButton = new JButton("Next Level");
            nextButton.setFont(new Font("Arial", Font.BOLD, 18));
            nextButton.addActionListener(e -> {
                dialog.dispose();
                game.nextLevel();
            });
            buttonPanel.add(nextButton);
        }

        JButton menuButton = new JButton("Main Menu");
        menuButton.setFont(new Font("Arial", Font.BOLD, 18));
        menuButton.addActionListener(e -> {
            dialog.dispose();
            game.showPanel("menu");
        });
        buttonPanel.add(menuButton);

        panel.add(buttonPanel);

        dialog.add(panel);
        dialog.setVisible(true);
    }
}


