import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Random;
import javax.swing.*;
import java.io.BufferedReader; 
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class PacMan extends JPanel implements ActionListener, KeyListener {
    private final String SAVE_FILE = "pacman_save.txt";
    class Block {
        int x;
        int y;
        int width;
        int height;
        Image image;

        int startX;
        int startY;
        char direction = 'U'; // U D L R
        int velocityX = 0;
        int velocityY = 0;

        Block(Image image, int x, int y, int width, int height) {
            this.image = image;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.startX = x;
            this.startY = y;
        }

        void updateDirection(char direction) {
            char prevDirection = this.direction;
            this.direction = direction;
            updateVelocity();
            this.x += this.velocityX;
            this.y += this.velocityY;
            
            // Використовуємо перевірку лише на стіни, оскільки повна перевірка для Pacman
            // відбувається у методі move() після обробки введення
            for (Block wall : walls) {
                if (collision(this, wall)) {
                    this.x -= this.velocityX;
                    this.y -= this.velocityY;
                    this.direction = prevDirection;
                    updateVelocity();
                }
            }
        }

        void updateVelocity() {
            if (this.direction == 'U') {
                this.velocityX = 0;
                this.velocityY = -tileSize/4;
            }
            else if (this.direction == 'D') {
                this.velocityX = 0;
                this.velocityY = tileSize/4;
            }
            else if (this.direction == 'L') {
                this.velocityX = -tileSize/4;
                this.velocityY = 0;
            }
            else if (this.direction == 'R') {
                this.velocityX = tileSize/4;
                this.velocityY = 0;
            }
        }

        void reset() {
            this.x = this.startX;
            this.y = this.startY;
        }
    }

    private int rowCount = 21;
    private int columnCount = 19;
    private int tileSize =32;
    private int boardWidth = columnCount * tileSize;
    private int boardHeight = rowCount * tileSize;

    private Image wallImage;
    private Image blueGhostImage;
    private Image orangeGhostImage;
    private Image pinkGhostImage;
    private Image redGhostImage;

    private Image pacmanUpImage;
    private Image pacmanDownImage;
    private Image pacmanLeftImage;
    private Image pacmanRightImage;

    
    // ОНОВЛЕНО: Цілі для рівнів
    int currentLevel = 1; 
    int[] levelScoreGoals = {0, 500, 800, 1000}; 
    boolean gameWon = false; 

    //X = wall, O = skip, P = pac man, ' ' = food
    //Ghosts: b = blue, o = orange, p = pink, r = red
    private String[] tileMap = {
        "XXXXXXXXXXXXXXXXXXX",
        "X        X        X",
        "X XX XXX X XXX XX X",
        "X                 X",
        "X XX X XXXXX X XX X",
        "X    X       X    X",
        "XXXX XXXX XXXX XXXX",
        "OOOX X       X XOOO",
        "XXXX X XXrXX X XXXX",
        "O       bpo       O",
        "XXXX X XXXXX X XXXX",
        "OOOX X       X XOOO",
        "XXXX X XXXXX X XXXX",
        "X        X        X",
        "X XX XXX X XXX XX X",
        "X  X     P     X  X",
        "XX X X XXXXX X X XX",
        "X    X   X   X    X",
        "X XXXXXX X XXXXXX X",
        "X                 X",
        "XXXXXXXXXXXXXXXXXXX" 
    };

    HashSet<Block> walls;
    HashSet<Block> foods;
    HashSet<Block> ghosts;
    Block pacman;

    Timer gameLoop;
    char[] directions = {'U', 'D', 'L', 'R'}; //up down left right
    Random random = new Random();
    int score = 0;
    int lives = 3;
    boolean gameOver = false;
    boolean gamePaused = false; 

    PacMan() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setBackground(Color.BLACK);
        addKeyListener(this);
        setFocusable(true);

        //load images
        wallImage = new ImageIcon(getClass().getResource("./wall.png")).getImage();
        blueGhostImage = new ImageIcon(getClass().getResource("./blueGhost.png")).getImage();
        orangeGhostImage = new ImageIcon(getClass().getResource("./orangeGhost.png")).getImage();
        pinkGhostImage = new ImageIcon(getClass().getResource("./pinkGhost.png")).getImage();
        redGhostImage = new ImageIcon(getClass().getResource("./redGhost.png")).getImage();

        pacmanUpImage = new ImageIcon(getClass().getResource("./pacmanUp.png")).getImage();
        pacmanDownImage = new ImageIcon(getClass().getResource("./pacmanDown.png")).getImage();
        pacmanLeftImage = new ImageIcon(getClass().getResource("./pacmanLeft.png")).getImage();
        pacmanRightImage = new ImageIcon(getClass().getResource("./pacmanRight.png")).getImage();

        loadMap();
        for (Block ghost : ghosts) {
            char newDirection = directions[random.nextInt(4)];
            ghost.updateDirection(newDirection);
        }
        //how long it takes to start timer, milliseconds gone between frames
        gameLoop = new Timer(50, this); //20fps (1000/50)
        // gameLoop.start(); // Запуск перенесено в App.java або loadGame()
    }

    public void loadMap() {
        walls = new HashSet<Block>();
        foods = new HashSet<Block>();
        ghosts = new HashSet<Block>();

        // Load initial map elements (walls, food, and base ghosts)
        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < columnCount; c++) {
                String row = tileMap[r];
                char tileMapChar = row.charAt(c);

                int x = c*tileSize;
                int y = r*tileSize;

                // ... (Wall and Food loading remains the same) ...
                if (tileMapChar == 'X') { //block wall
                    Block wall = new Block(wallImage, x, y, tileSize, tileSize);
                    walls.add(wall);
                }
                else if (tileMapChar == 'b') { //blue ghost
                    Block ghost = new Block(blueGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                }
                else if (tileMapChar == 'o') { //orange ghost
                    Block ghost = new Block(orangeGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                }
                else if (tileMapChar == 'p') { //pink ghost
                    Block ghost = new Block(pinkGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                }
                else if (tileMapChar == 'r') { //red ghost
                    Block ghost = new Block(redGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                }
                else if (tileMapChar == 'P') { //pacman
                    pacman = new Block(pacmanRightImage, x, y, tileSize, tileSize);
                }
                else if (tileMapChar == ' ') { //food
                    Block food = new Block(null, x + 14, y + 14, 4, 4);
                    foods.add(food);
                }
            }
        }

        // --- NEW DIFFICULTY SCALING: Adding extra ghosts ---

        int ghostStartX = 9 * tileSize; // Central column for spawning
        int ghostStartY = 10 * tileSize; // Central row for spawning (Ghost House area)

        // Calculate how many extra ghosts to add
        // Level 1: 0 extra (total 4 base ghosts)
        // Level 2: 1 extra (total 5 ghosts)
        // Level 3: 2 extra (total 6 ghosts)
        int extraGhosts = currentLevel - 1; 

        // Define images for the extra ghosts (we'll cycle through the existing images)
        Image[] ghostImages = {blueGhostImage, orangeGhostImage, pinkGhostImage, redGhostImage};
        int imageIndex = 0;

        for (int i = 0; i < extraGhosts; i++) {
            // Use a different image each time
            Image imageToAdd = ghostImages[imageIndex % ghostImages.length]; 
            imageIndex++;

            // Shift the starting position slightly so they don't spawn directly on top of each other
            // This makes them start slightly staggered within the ghost house.
            int spawnX = ghostStartX + (i * tileSize / 4);
        
            Block newGhost = new Block(imageToAdd, spawnX, ghostStartY, tileSize, tileSize);
            ghosts.add(newGhost);
        }
   }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        g.drawImage(pacman.image, pacman.x, pacman.y, pacman.width, pacman.height, null);

        for (Block ghost : ghosts) {
            g.drawImage(ghost.image, ghost.x, ghost.y, ghost.width, ghost.height, null);
        }

        for (Block wall : walls) {
            g.drawImage(wall.image, wall.x, wall.y, wall.width, wall.height, null);
        }

        g.setColor(Color.WHITE);
        for (Block food : foods) {
            g.fillRect(food.x, food.y, food.width, food.height);
        }
        //score and lives
        g.setFont(new Font("Arial", Font.PLAIN, 18));
        
        g.setColor(Color.WHITE);
        // Combined stats line (Level, Lives, Score)
        g.drawString("Level: " + String.valueOf(currentLevel) + " | x" + String.valueOf(lives) + " Score: " + String.valueOf(score), tileSize/2, tileSize/2);
        
        // Instructions
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        g.drawString("SPACE=Pause | S=Save | L=Load", tileSize/2, boardHeight - 10);

        if (gameOver) {
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.drawString("GAME OVER", boardWidth/2 - 100, boardHeight/2);
            g.drawString("Score: " + String.valueOf(score), boardWidth/2 - 70, boardHeight/2 + 40);
        }
        else if (gameWon) {
            g.setColor(Color.GREEN);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.drawString("VICTORY!", boardWidth/2 - 90, boardHeight/2);
            g.drawString("FINAL SCORE: " + String.valueOf(score), boardWidth/2 - 140, boardHeight/2 + 40);
        }
        else if (gamePaused) { // Draw pause message
            g.setColor(Color.YELLOW);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.drawString("PAUSED", boardWidth/2 - 70, boardHeight/2);
            g.setFont(new Font("Arial", Font.PLAIN, 20));
            g.drawString("Press SPACE to Resume", boardWidth/2 - 120, boardHeight/2 + 40);
        }
    }

    public void move() {
        // 1. Рух Pac-Man та обробка зіткнень зі стінами та кордонами
        pacman.x += pacman.velocityX;
        pacman.y += pacman.velocityY;

        boolean pacmanHitBoundary = false;

        // Перевірка зіткнень зі стінами
        for (Block wall : walls) {
            if (collision(pacman, wall)) {
                pacmanHitBoundary = true; 
                break;
            }
        }

        // ПОВНА Перевірка кордонів вікна (для запобігання виходу за межі)
        if (pacman.x < 0 || 
            pacman.x + pacman.width > boardWidth ||
            pacman.y < 0 ||
            pacman.y + pacman.height > boardHeight) {
            
            pacmanHitBoundary = true;
        }

        if (pacmanHitBoundary) {
            // Відкочуємо рух
            pacman.x -= pacman.velocityX;
            pacman.y -= pacman.velocityY;
            // Pacman stops if hits wall or boundary
            pacman.velocityX = 0;
            pacman.velocityY = 0;
        }

        // 2. Рух привидів
        for (Block ghost : ghosts) {
            // Перевірка тунелю (залишимо оригінальну логіку, якщо вона потрібна)
            if (ghost.y == tileSize*9 && ghost.direction != 'U' && ghost.direction != 'D') {
                ghost.updateDirection('U');
            }
            
            ghost.x += ghost.velocityX;
            ghost.y += ghost.velocityY;

            // Прапорець для перевірки, чи потрібна зміна напрямку
            boolean ghostHitBoundary = false;

            // Перевірка зіткнень зі стінами
            for (Block wall : walls) {
                if (collision(ghost, wall)) {
                    ghostHitBoundary = true;
                    break; 
                }
            }
            
            // ПОВНА Перевірка кордонів вікна для привидів
            if (ghost.x <= 0 || 
                ghost.x + ghost.width >= boardWidth || 
                ghost.y <= 0 || 
                ghost.y + ghost.height >= boardHeight) 
            {
                ghostHitBoundary = true;
            }

            if (ghostHitBoundary) {
                // Відкочуємо рух
                ghost.x -= ghost.velocityX;
                ghost.y -= ghost.velocityY;
                
                // Змінюємо напрямок на випадковий
                char newDirection = directions[random.nextInt(4)];
                ghost.updateDirection(newDirection);
            }
        }

        // 3. Зіткнення Pac-Man з привидами
        for (Block ghost : ghosts) {
            if (collision(pacman, ghost)) {
                lives--;
                if (lives <= 0) {
                    gameOver = true;
                    return;
                }
                // Скидаємо позиції після втрати життя
                resetPositions();
                return;
            }
        }

         // 4. Зіткнення Pac-Man з їжею
        Block foodEaten = null;
        for (Block food : foods) {
            if (collision(pacman, food)) {
                foodEaten = food;
                score += 10;
            }
       }
        foods.remove(foodEaten);

        // 5. ЛОГІКА ПЕРЕХОДУ НА РІВЕНЬ (За рахунком)
       if (score >= levelScoreGoals[currentLevel]) {
            currentLevel++; 
        
            if (currentLevel > levelScoreGoals.length - 1) { 
                gameWon = true; 
                return;
            }

            loadMap(); 
            resetPositions();
            return; 
        }

        // 6. ЛОГІКА ПЕРЕЗАПУСКУ РІВНЯ (Якщо вся їжа з'їдена, але рахунок недостатній)
        if (foods.isEmpty()) {
            loadMap();
            resetPositions();
        }
    }

    public boolean collision(Block a, Block b) {
        return  a.x < b.x + b.width &&
                a.x + a.width > b.x &&
                a.y < b.y + b.height &&
                a.y + a.height > b.y;
    }

    public void resetPositions() {
        pacman.reset();
        pacman.velocityX = 0;
        pacman.velocityY = 0;
        for (Block ghost : ghosts) {
            ghost.reset();
            char newDirection = directions[random.nextInt(4)];
            ghost.updateDirection(newDirection);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver && !gamePaused && !gameWon) { 
            move();
        }
        repaint();

        if (gameOver || gameWon) { 
            gameLoop.stop();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {
        // Обробка Game Over/Restart
        if (gameOver) {
            loadMap();
            resetPositions();
            lives = 3;
            score = 0;
            gameOver = false;
            gameLoop.start();
            return; 
        }

         // Обробка паузи
         if (e.getKeyCode() == KeyEvent.VK_SPACE) { 
            if (!gameWon) { 
                gamePaused = !gamePaused;
                if (gamePaused) {
                    gameLoop.stop();
                } else {
                    gameLoop.start();
                }
                return; 
            }
        }

        if (gamePaused) return; // Ігноруємо рух, якщо пауза

        // Обробка Save/Load
        if (e.getKeyCode() == KeyEvent.VK_S) {
            saveGame();
            gamePaused = true;
            gameLoop.stop();
            return;
        }
        else if (e.getKeyCode() == KeyEvent.VK_L) {
            loadGame();
            return;
        }
        
        // Обробка руху
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            pacman.updateDirection('U');
        }
        else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            pacman.updateDirection('D');
        }
        else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            pacman.updateDirection('L');
        }
        else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            pacman.updateDirection('R');
        }

        // Зміна зображення
        if (pacman.direction == 'U') {
            pacman.image = pacmanUpImage;
        }
        else if (pacman.direction == 'D') {
            pacman.image = pacmanDownImage;
        }
        else if (pacman.direction == 'L') {
            pacman.image = pacmanLeftImage;
        }
        else if (pacman.direction == 'R') {
            pacman.image = pacmanRightImage;
        }
    }

    public void saveGame() {
        StringBuilder state = new StringBuilder();
        state.append(currentLevel).append(",");
        state.append(score).append(",");
        state.append(lives).append(",");
    
        // Позиція Pac-Man
        state.append(pacman.x).append(",");
        state.append(pacman.y).append(",");
    
        // Позиції привидів
        for (Block ghost : ghosts) {
            state.append(ghost.x).append(",");
            state.append(ghost.y).append(",");
        }
 
        // Стан їжі: кількість та координати
        state.append(foods.size()).append(",");
        for (Block food : foods) {
            state.append(food.x).append(",");
            state.append(food.y).append(",");
        }

        try (FileWriter writer = new FileWriter(SAVE_FILE)) {
            writer.write(state.toString());
            System.out.println("Game saved successfully!");
        } catch (IOException e) {
        System.out.println("Error saving game: " + e.getMessage());
        }
    }

    public boolean loadGame() {
        try (BufferedReader reader = new BufferedReader(new FileReader(SAVE_FILE))) {
            String line = reader.readLine();
            if (line == null) {
                System.out.println("Save file is empty.");
                return false;
            }

            String[] data = line.split(",");
            int index = 0;

            // 1. Завантаження основного стану
            currentLevel = Integer.parseInt(data[index++]);
            score = Integer.parseInt(data[index++]);
            lives = Integer.parseInt(data[index++]);
        
            loadMap(); // Перезавантажуємо мапу
        
            // 2. Завантаження позиції Pac-Man
            int pacmanX = Integer.parseInt(data[index++]);
            int pacmanY = Integer.parseInt(data[index++]);
            pacman.x = pacmanX;
            pacman.y = pacmanY;
        
            // 3. Завантаження позицій привидів та ІНІЦІАЛІЗАЦІЯ ШВИДКОСТІ
            for (Block ghost : ghosts) {
                int ghostX = Integer.parseInt(data[index++]);
                int ghostY = Integer.parseInt(data[index++]);
            
                ghost.x = ghostX;
                ghost.y = ghostY;
            
                char newDirection = directions[random.nextInt(4)];
                ghost.updateDirection(newDirection); 
            }

            // 4. Завантаження стану їжі
            int foodCount = Integer.parseInt(data[index++]);
            HashSet<Block> newFoods = new HashSet<>();
            for (int i = 0; i < foodCount; i++) {
                int foodX = Integer.parseInt(data[index++]);
                int foodY = Integer.parseInt(data[index++]);

                Block food = new Block(null, foodX, foodY, 4, 4);
                newFoods.add(food);
            }
        
            foods = newFoods; // Замінюємо множину їжі

            // Скидаємо прапорці стану та запускаємо цикл
            gameOver = false;
            gameWon = false;
            gamePaused = false;
            gameLoop.start();
            System.out.println("Game loaded successfully!");
 
            return true;

        } catch (IOException | NumberFormatException | ArrayIndexOutOfBoundsException e) {
            System.out.println("No saved game found or error loading game: " + e.getMessage());
            return false;
        }
    }
}