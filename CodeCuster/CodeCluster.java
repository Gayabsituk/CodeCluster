import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
import javax.swing.border.*;

public class CodeCluster extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private GameData gameData;
    private String currentPlayer;
    private int currentLevel = 1;

    public CodeCluster() {
        gameData = new GameData();
        gameData.loadData();

        setTitle("Code Cluster");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        mainPanel.add(new MainMenuPanel(this), "menu");
        mainPanel.add(new NameSelectionPanel(this), "nameSelection");
        mainPanel.add(new SettingsPanel(this), "settings");
        mainPanel.add(new LeaderboardPanel(this), "leaderboard");

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

class GameData {
    private LinkedList<String> playerNames;
    private TreeMap<String, ArrayList<Score>> scores;
    private HashMap<Integer, ArrayList<Category>> levelData;
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
        // Level 1 - 4 categories
        ArrayList<Category> level1 = new ArrayList<>();
        level1.add(new Category("Java Keywords", new String[]{"STATIC", "VOID", "CLASS", "PUBLIC"}));
        level1.add(new Category("Data Structures", new String[]{"ARRAY", "STACK", "QUEUE", "TREE"}));
        level1.add(new Category("Primitive Types", new String[]{"INT", "BOOLEAN", "CHAR", "DOUBLE"}));
        level1.add(new Category("Common Methods", new String[]{"GET", "SET", "PUSH", "POP"}));
        levelData.put(1, level1);

        // Level 2 - 4 categories
        ArrayList<Category> level2 = new ArrayList<>();
        level2.add(new Category("OOP Concepts", new String[]{"POLYMORPHISM", "INHERITANCE", "ENCAPSULATION", "ABSTRACTION"}));
        level2.add(new Category("Loop Keywords", new String[]{"FOR", "WHILE", "DO", "FOREACH"}));
        level2.add(new Category("String Methods", new String[]{"LENGTH", "SUBSTRING", "CONCAT", "TRIM"}));
        level2.add(new Category("Boolean Operators", new String[]{"AND", "OR", "NOT", "XOR"}));
        levelData.put(2, level2);

        // Level 3 - 5 categories
        ArrayList<Category> level3 = new ArrayList<>();
        level3.add(new Category("Java Collections", new String[]{"LIST", "MAP", "SET", "HASHMAP"}));
        level3.add(new Category("Exception Handling", new String[]{"TRY", "CATCH", "THROW", "FINALLY"}));
        level3.add(new Category("Access Modifiers", new String[]{"PRIVATE", "PROTECTED", "PACKAGE", "DEFAULT"}));
        level3.add(new Category("Wrapper Classes", new String[]{"INTEGER", "LONG", "FLOAT", "CHARACTER"}));
        level3.add(new Category("Keywords", new String[]{"FINAL", "SUPER", "THIS", "EXTENDS"}));
        levelData.put(3, level3);

        // Level 4 - 5 categories
        ArrayList<Category> level4 = new ArrayList<>();
        level4.add(new Category("Design Patterns", new String[]{"SINGLETON", "FACTORY", "OBSERVER", "DECORATOR"}));
        level4.add(new Category("Testing Terms", new String[]{"JUNIT", "MOCK", "ASSERT", "TEST"}));
        level4.add(new Category("Thread States", new String[]{"NEW", "RUNNABLE", "BLOCKED", "WAITING"}));
        level4.add(new Category("Memory Areas", new String[]{"HEAP", "STACK", "METASPACE", "POOL"}));
        level4.add(new Category("Synchronization", new String[]{"LOCK", "SYNCHRONIZED", "VOLATILE", "ATOMIC"}));
        levelData.put(4, level4);

        // Level 5 - 6 categories
        ArrayList<Category> level5 = new ArrayList<>();
        level5.add(new Category("Spring Framework", new String[]{"BEAN", "AUTOWIRED", "COMPONENT", "SERVICE"}));
        level5.add(new Category("SQL Keywords", new String[]{"SELECT", "INSERT", "UPDATE", "DELETE"}));
        level5.add(new Category("Git Commands", new String[]{"COMMIT", "PUSH", "PULL", "MERGE"}));
        level5.add(new Category("HTTP Methods", new String[]{"GET", "POST", "PUT", "PATCH"}));
        level5.add(new Category("JSON Operations", new String[]{"PARSE", "STRINGIFY", "SERIALIZE", "DESERIALIZE"}));
        level5.add(new Category("Build Tools", new String[]{"MAVEN", "GRADLE", "ANT", "NPM"}));
        levelData.put(5, level5);
    }

    public ArrayList<Category> getLevelCategories(int level) {
        return levelData.get(level);
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
    public MainMenuPanel(CodeCluster game) {
        setLayout(new BorderLayout());
        setBackground(new Color(99, 102, 241));

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(50, 100, 50, 100));

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
        centerPanel.add(Box.createRigidArea(new Dimension(0, 60)));

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
        buttonsPanel.setOpaque(false);

        JButton startButton = createMenuButton("Start Game", new Color(255, 255, 255));
        startButton.addActionListener(e -> game.showPanel("nameSelection"));
        buttonsPanel.add(startButton);
        buttonsPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        JButton settingsButton = createMenuButton("Settings", new Color(255, 255, 255, 50));
        settingsButton.addActionListener(e -> game.showPanel("settings"));
        buttonsPanel.add(settingsButton);
        buttonsPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        JButton leaderboardButton = createMenuButton("Leaderboard", new Color(255, 255, 255, 50));
        leaderboardButton.addActionListener(e -> game.showPanel("leaderboard"));
        buttonsPanel.add(leaderboardButton);
        buttonsPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        JButton exitButton = createMenuButton("Exit", new Color(239, 68, 68, 50));
        exitButton.addActionListener(e -> System.exit(0));
        buttonsPanel.add(exitButton);

        centerPanel.add(buttonsPanel);
        centerPanel.add(Box.createVerticalGlue());

        add(centerPanel, BorderLayout.CENTER);
    }

    private JButton createMenuButton(String text, Color bg) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 24));
        button.setBackground(bg);
        button.setForeground(bg.equals(Color.WHITE) ? Color.BLACK : Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(400, 70));
        button.setMaximumSize(new Dimension(400, 70));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }
}

class NameSelectionPanel extends JPanel {
    public NameSelectionPanel(CodeCluster game) {
        setLayout(new BorderLayout());
        setBackground(new Color(99, 102, 241));

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(50, 100, 50, 100));

        JLabel titleLabel = new JLabel("Welcome Player!");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 48));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Choose your name to continue");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        subtitleLabel.setForeground(Color.WHITE);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        centerPanel.add(Box.createVerticalGlue());
        centerPanel.add(titleLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        centerPanel.add(subtitleLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 40)));

        JPanel whitePanel = new JPanel();
        whitePanel.setLayout(new BoxLayout(whitePanel, BoxLayout.Y_AXIS));
        whitePanel.setBackground(Color.WHITE);
        whitePanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        whitePanel.setMaximumSize(new Dimension(500, 400));

        JTextField nameField = new JTextField();
        nameField.setFont(new Font("Arial", Font.PLAIN, 20));
        nameField.setMaximumSize(new Dimension(440, 50));

        JButton submitButton = new JButton("Start Playing");
        submitButton.setFont(new Font("Arial", Font.BOLD, 20));
        submitButton.setBackground(new Color(168, 85, 247));
        submitButton.setForeground(Color.WHITE);
        submitButton.setMaximumSize(new Dimension(440, 50));
        submitButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (!name.isEmpty()) {
                game.getGameData().addPlayerName(name);
                game.startGame(name);
            }
        });

        whitePanel.add(new JLabel("Enter your name:"));
        whitePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        whitePanel.add(nameField);
        whitePanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Show saved names
        LinkedList<String> savedNames = game.getGameData().getPlayerNames();
        if (!savedNames.isEmpty()) {
            JLabel savedLabel = new JLabel("Or select a saved name:");
            whitePanel.add(savedLabel);
            whitePanel.add(Box.createRigidArea(new Dimension(0, 10)));

            JScrollPane scrollPane = new JScrollPane();
            JPanel namesPanel = new JPanel();
            namesPanel.setLayout(new BoxLayout(namesPanel, BoxLayout.Y_AXIS));

            for (String name : savedNames) {
                JButton nameButton = new JButton(name);
                nameButton.setMaximumSize(new Dimension(440, 40));
                nameButton.setAlignmentX(Component.LEFT_ALIGNMENT);
                nameButton.addActionListener(e -> game.startGame(name));
                namesPanel.add(nameButton);
                namesPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            }

            scrollPane.setViewportView(namesPanel);
            scrollPane.setMaximumSize(new Dimension(440, 150));
            whitePanel.add(scrollPane);
            whitePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        whitePanel.add(submitButton);

        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.setOpaque(false);
        wrapperPanel.add(Box.createHorizontalGlue(), BorderLayout.WEST);
        wrapperPanel.add(whitePanel, BorderLayout.CENTER);
        wrapperPanel.add(Box.createHorizontalGlue(), BorderLayout.EAST);

        centerPanel.add(Box.createVerticalGlue());
        centerPanel.add(wrapperPanel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 30)));

        JButton backButton = new JButton("??? Back to Menu");
        backButton.setFont(new Font("Arial", Font.PLAIN, 16));
        backButton.setForeground(Color.WHITE);
        backButton.setContentAreaFilled(false);
        backButton.setBorderPainted(false);
        backButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        backButton.addActionListener(e -> game.showPanel("menu"));
        centerPanel.add(backButton);

        centerPanel.add(Box.createVerticalGlue());

        add(centerPanel, BorderLayout.CENTER);
    }
}

class SettingsPanel extends JPanel {
    public SettingsPanel(CodeCluster game) {
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
        sfxCheckBox.addActionListener(e -> game.getGameData().setSfxEnabled(sfxCheckBox.isSelected()));

        whitePanel.add(titleLabel);
        whitePanel.add(Box.createRigidArea(new Dimension(0, 40)));
        whitePanel.add(sfxCheckBox);
        whitePanel.add(Box.createRigidArea(new Dimension(0, 40)));

        JButton doneButton = new JButton("Done");
        doneButton.setFont(new Font("Arial", Font.BOLD, 20));
        doneButton.setBackground(new Color(168, 85, 247));
        doneButton.setForeground(Color.WHITE);
        doneButton.setMaximumSize(new Dimension(200, 50));
        doneButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        doneButton.addActionListener(e -> game.showPanel("menu"));

        whitePanel.add(doneButton);

        centerPanel.add(Box.createVerticalGlue());
        centerPanel.add(whitePanel);
        centerPanel.add(Box.createVerticalGlue());

        add(centerPanel, BorderLayout.CENTER);
    }
}

class LeaderboardPanel extends JPanel {
    public LeaderboardPanel(CodeCluster game) {
        setLayout(new BorderLayout());
        setBackground(new Color(99, 102, 241));

        JPanel whitePanel = new JPanel(new BorderLayout());
        whitePanel.setBackground(Color.WHITE);
        whitePanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel titleLabel = new JLabel("Leaderboard", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 36));

        ArrayList<Score> allScores = game.getGameData().getFlatScores();
        allScores.sort((a, b) -> Integer.compare(b.getScore(), a.getScore()));

        String[] columns = {"Rank", "Player", "Score", "Level", "Date"};
        Object[][] data = new Object[allScores.size()][5];

        for (int i = 0; i < allScores.size(); i++) {
            Score score = allScores.get(i);
            data[i][0] = i + 1;
            data[i][1] = score.getPlayerName();
            data[i][2] = score.getScore();
            data[i][3] = "Level " + score.getLevel();
            data[i][4] = score.getDate();
        }

        JTable table = new JTable(data, columns);
        table.setFont(new Font("Arial", Font.PLAIN, 16));
        table.setRowHeight(40);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 18));

        JScrollPane scrollPane = new JScrollPane(table);

        JButton backButton = new JButton("Back to Menu");
        backButton.setFont(new Font("Arial", Font.BOLD, 18));
        backButton.setBackground(new Color(168, 85, 247));
        backButton.setForeground(Color.WHITE);
        backButton.addActionListener(e -> game.showPanel("menu"));

        whitePanel.add(titleLabel, BorderLayout.NORTH);
        whitePanel.add(scrollPane, BorderLayout.CENTER);
        whitePanel.add(backButton, BorderLayout.SOUTH);

        add(whitePanel, BorderLayout.CENTER);
        setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));
    }
}

class GameBoardPanel extends JPanel {
    private CodeCluster game;
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
                    wordButton.setBackground(new Color(55, 65, 81));
                    wordButton.setForeground(Color.WHITE);
                } else {
                    wordButton.setBackground(new Color(243, 244, 246));
                    wordButton.setForeground(Color.BLACK);
                }

                wordButton.addActionListener(e -> toggleWord(word));
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
                hintLabel.setText("???? One away! You're so close!");
            } else if (maxMatch == 2) {
                hintLabel.setText("???? Two away! Keep trying!");
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

        if (timeTaken < 60) {
            achievements.add("Speed Demon");
            achievementScore += 500;
        }
        if (mistakes == 0) {
            achievements.add("Perfect Game");
            achievementScore += 1000;
        }
        if (solvedCategories.size() == categories.size()) {
            achievements.add("Master Mind");
            achievementScore += 750;
        }
        if (timeLeft >= 30) {
            achievements.add("Time Lord");
            achievementScore += 300;
        }

        int baseScore = solvedCategories.size() * 100;
        int timeBonus = Math.max(0, timeLeft * 10);
        int totalScore = baseScore + timeBonus + achievementScore - (mistakes * 50);

        Score score = new Score(playerName, totalScore, level, 1, achievements);
        game.getGameData().addScore(score);

        showGameCompleteDialog(totalScore, achievements);
    }

    private void showGameCompleteDialog(int score, ArrayList<String> achievements) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Level Complete!", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel titleLabel = new JLabel(level == 5 ? "Game Complete!" : "Level Complete!");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 36));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel scoreLabel = new JLabel("Score: " + score);
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 32));
        scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(titleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(scoreLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        if (!achievements.isEmpty()) {
            JLabel achLabel = new JLabel("Achievements:");
            achLabel.setFont(new Font("Arial", Font.BOLD, 20));
            achLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(achLabel);

            for (String ach : achievements) {
                JLabel achItem = new JLabel("??? " + ach);
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


