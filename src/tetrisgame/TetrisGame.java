package tetrisgame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Random;

public class TetrisGame extends JFrame {
    
    // Constantes para el juego
    private static final int BLOCK_SIZE = 30; // Tamaño de cada bloque
    private static final int BOARD_WIDTH = 10; // Ancho del tablero en bloques
    private static final int BOARD_HEIGHT = 20; // Alto del tablero en bloques
    private static final int CANVAS_WIDTH = BLOCK_SIZE * BOARD_WIDTH; // Ancho del canvas
    private static final int CANVAS_HEIGHT = BLOCK_SIZE * BOARD_HEIGHT; // Alto del canvas
    private static final int INIT_DELAY = 1000; // Retraso inicial en milisegundos
    private static final int MIN_DELAY = 100; // Retraso mínimo en milisegundos
    private static final int DELAY_DECREMENT = 50; // Reducción del retraso
    private static final int DELAY_DECREMENT_INTERVAL = 10; // Cada cuántas piezas reducir el retraso

    // Variables del juego
    private Timer timer;
    private boolean isStarted = false;
    private boolean isPaused = false;
    private boolean isGameOver = false;
    private int currentScore = 0;
    private int highScore = 0;
    private JLabel scoreLabel;
    private JLabel recordLabel;
    private JButton startButton;
    private GameCanvas canvas;
    private int currentDelay;
    private int pieceCount;
    
    // Constructor
    public TetrisGame() {
        setTitle("Juego de Tetris");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        
        // Cargar récord guardado si existe
        loadHighScore();
        
        // Inicializar componentes
        initComponents();
        
        pack();
        setLocationRelativeTo(null);
    }
    
    // Inicializar componentes de la interfaz
    private void initComponents() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        
        // Panel para el canvas del juego
        canvas = new GameCanvas();
        canvas.setPreferredSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));
        mainPanel.add(canvas, BorderLayout.CENTER);
        
        // Panel lateral para controles y puntuación
        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
        sidePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Botón de inicio-reinicio
        startButton = new JButton("Iniciar Juego");
        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isStarted || isGameOver) {
                    startGame();
                } else {
                    restartGame();
                }
            }
        });
        sidePanel.add(startButton);
        sidePanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Etiqueta de puntuación
        scoreLabel = new JLabel("Puntaje: 0");
        scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidePanel.add(scoreLabel);
        sidePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Etiqueta de récord
        recordLabel = new JLabel("Récord: " + highScore);
        recordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidePanel.add(recordLabel);
        
        // Añadir instrucciones
        sidePanel.add(Box.createRigidArea(new Dimension(0, 30)));
        JLabel instructionsLabel = new JLabel("<html><h3>Controles:</h3>" +
                                            "← o A: Mover izquierda<br>" +
                                            "→ o D: Mover derecha<br>" +
                                            "↑ o W: Rotar derecha<br>" +
                                            "↓ o S: Rotar izquierda<br>" + 
                                            "P: Pausar/Continuar </html>");
        instructionsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidePanel.add(instructionsLabel);
        
        mainPanel.add(sidePanel, BorderLayout.EAST);
        
        // Configurar los controles de teclado
        setFocusable(true);
        addKeyListener(new TetrisKeyListener());
        
        add(mainPanel);
    }
    
    // Iniciar juego
    private void startGame() {
        if (isStarted) {
            return; // Si ya está iniciado, no hacer nada
        }
        
        isStarted = true;
        isGameOver = false;
        currentScore = 0;
        pieceCount = 0;
        currentDelay = INIT_DELAY;
        
        // Actualizar etiquetas
        scoreLabel.setText("Puntaje: 0");
        
        // Inicializar el tablero
        canvas.init();
        canvas.createNewPiece();
        
        // Iniciar el temporizador
        timer = new Timer(currentDelay, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isPaused && !isGameOver) {
                    canvas.update();
                }
            }
        });
        timer.start();
        
        startButton.setText("Reiniciar");
        requestFocus(); // Enfocar para que funcionen las teclas
    }
    
    private void restartGame() {
        if (timer != null) {
            timer.stop();
        }
        isStarted = false;
        isGameOver = false;
        isPaused = false;
        currentScore = 0;
        scoreLabel.setText("Puntaje: 0");

        canvas.init();
        startGame(); // Iniciar un nuevo juego
    }  
    
    // Finalizar juego
    private void gameOver() {
        timer.stop();
        isStarted = false;
        isGameOver = true;
        
        // Actualizar récord si es necesario
        if (currentScore > highScore) {
            highScore = currentScore;
            recordLabel.setText("Récord: " + highScore);
            saveHighScore();
        }
        
        startButton.setText("Iniciar Juego");
        
        // Mostrar mensaje de juego terminado
        JOptionPane.showMessageDialog(this, 
                "¡Juego terminado!\nPuntaje: " + currentScore, 
                "Tetris", JOptionPane.INFORMATION_MESSAGE);
    }
    
    // Incrementar puntuación
    private void addScore(int points) {
        currentScore += points;
        scoreLabel.setText("Puntaje: " + currentScore);
    }
    
    // Guardar récord
    private void saveHighScore() {
        try {
            File file = new File("highscore.dat");
            FileWriter writer = new FileWriter(file);
            writer.write(String.valueOf(highScore));
            writer.close();
        } catch (IOException e) {
            System.err.println("Error al guardar récord: " + e.getMessage());
        }
    }
    
    // Cargar récord
    private void loadHighScore() {
        try {
            File file = new File("highscore.dat");
            if (file.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line = reader.readLine();
                highScore = Integer.parseInt(line);
                reader.close();
            }
        } catch (Exception e) {
            System.err.println("Error al cargar récord: " + e.getMessage());
            highScore = 0;
        }
    }
    
    // Clase para manejar eventos de teclado
    private class TetrisKeyListener extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyChar() == 'p' || e.getKeyChar() == 'P') {
                if (isStarted && !isGameOver) {
                    isPaused = !isPaused;
                    canvas.repaint(); // Actualizar visualmente el estado de pausa
                }
            }
            int keyCode = e.getKeyCode();
            
            if (!isPaused && !isGameOver) {
                switch (keyCode) {
                    case KeyEvent.VK_LEFT:
                    case KeyEvent.VK_A:
                        canvas.movePieceLeft();
                        break;
                    case KeyEvent.VK_RIGHT:
                    case KeyEvent.VK_D:
                        canvas.movePieceRight();
                        break;
                    case KeyEvent.VK_UP:
                    case KeyEvent.VK_W:
                        canvas.rotatePieceRight();
                        break;
                    case KeyEvent.VK_DOWN:
                    case KeyEvent.VK_S:
                        canvas.rotatePieceLeft();
                        break;
                    case KeyEvent.VK_SPACE:
                        canvas.dropPiece();
                        break;
                }                
            }            

            
            canvas.repaint();
        }
    }
    
    // Alternar pausa
    private void togglePause() {
        isPaused = !isPaused;
    }
    
    // Clase Canvas para el juego
    private class GameCanvas extends JPanel {
        private int[][] board;
        private Shape currentPiece;
        private Point currentPos;
        
        public GameCanvas() {
            setBackground(Color.BLACK);
            board = new int[BOARD_HEIGHT][BOARD_WIDTH];
        }
        
        // Inicializar el tablero
        public void init() {
            for (int i = 0; i < BOARD_HEIGHT; i++) {
                for (int j = 0; j < BOARD_WIDTH; j++) {
                    board[i][j] = 0;
                }
            }
            repaint();
        }
        
        // Crear nueva pieza
        public void createNewPiece() {
            currentPiece = Shape.getRandomShape();
            currentPos = new Point(BOARD_WIDTH / 2 - currentPiece.getWidth() / 2, 0);
            
            // Verificar si hay espacio para la nueva pieza
            if (!canMove(currentPos.x, currentPos.y)) {
                gameOver();
            }
            
            // Incrementar contador de piezas y ajustar velocidad si es necesario
            pieceCount++;
            if (pieceCount % DELAY_DECREMENT_INTERVAL == 0 && currentDelay > MIN_DELAY) {
                currentDelay -= DELAY_DECREMENT;
                if (currentDelay < MIN_DELAY) {
                    currentDelay = MIN_DELAY;
                }
                if (timer != null) {
                    timer.setDelay(currentDelay);
                }
            }
        }
        
        // Actualizar estado del juego
        public void update() {
            if (canMove(currentPos.x, currentPos.y + 1)) {
                currentPos.y++;
            } else {
                placePiece();
                checkLines();
                createNewPiece();
            }
            repaint();
        }
        
        // Mover pieza a la izquierda
        public void movePieceLeft() {
            if (canMove(currentPos.x - 1, currentPos.y)) {
                currentPos.x--;
            }
        }
        
        // Mover pieza a la derecha
        public void movePieceRight() {
            if (canMove(currentPos.x + 1, currentPos.y)) {
                currentPos.x++;
            }
        }
        
        // Rotar pieza a la derecha
        public void rotatePieceRight() {
            Shape rotated = currentPiece.rotateRight();
            if (canPlace(rotated, currentPos.x, currentPos.y)) {
                currentPiece = rotated;
            }
        }
        
        // Rotar pieza a la izquierda
        public void rotatePieceLeft() {
            Shape rotated = currentPiece.rotateLeft();
            if (canPlace(rotated, currentPos.x, currentPos.y)) {
                currentPiece = rotated;
            }
        }
        
        // Dejar caer la pieza hasta el fondo
        public void dropPiece() {
            int newY = currentPos.y;
            while (canMove(currentPos.x, newY + 1)) {
                newY++;
            }
            if (newY > currentPos.y) {
                currentPos.y = newY;
                placePiece();
                checkLines();
                createNewPiece();
                repaint();
            }
        }
        
        // Verificar si la pieza puede moverse a la nueva posición
        private boolean canMove(int newX, int newY) {
            return canPlace(currentPiece, newX, newY);
        }
        
        // Verificar si una forma puede colocarse en una posición
        private boolean canPlace(Shape shape, int x, int y) {
            for (int i = 0; i < shape.getHeight(); i++) {
                for (int j = 0; j < shape.getWidth(); j++) {
                    if (shape.getShape()[i][j] != 0) {
                        int boardX = x + j;
                        int boardY = y + i;
                        
                        // Verificar límites del tablero
                        if (boardX < 0 || boardX >= BOARD_WIDTH || boardY < 0 || boardY >= BOARD_HEIGHT) {
                            return false;
                        }
                        
                        // Verificar colisión con otras piezas
                        if (boardY >= 0 && board[boardY][boardX] != 0) {
                            return false;
                        }
                    }
                }
            }
            return true;
        }
        
        // Colocar la pieza en el tablero
        private void placePiece() {
            for (int i = 0; i < currentPiece.getHeight(); i++) {
                for (int j = 0; j < currentPiece.getWidth(); j++) {
                    int value = currentPiece.getShape()[i][j];
                    if (value != 0) {
                        int boardX = currentPos.x + j;
                        int boardY = currentPos.y + i;
                        
                        if (boardY >= 0 && boardY < BOARD_HEIGHT && boardX >= 0 && boardX < BOARD_WIDTH) {
                            board[boardY][boardX] = value;
                        }
                    }
                }
            }
            
            // Añadir puntos por colocar una pieza (10 puntos por pieza)
            addScore(10);
        }
        
        // Verificar y eliminar líneas completas
        private void checkLines() {
            int linesCleared = 0;
            
            for (int i = BOARD_HEIGHT - 1; i >= 0; i--) {
                boolean lineIsFull = true;
                
                for (int j = 0; j < BOARD_WIDTH; j++) {
                    if (board[i][j] == 0) {
                        lineIsFull = false;
                        break;
                    }
                }
                
                if (lineIsFull) {
                    linesCleared++;
                    // Mover todas las líneas superiores hacia abajo
                    for (int k = i; k > 0; k--) {
                        for (int j = 0; j < BOARD_WIDTH; j++) {
                            board[k][j] = board[k-1][j];
                        }
                    }
                    // Limpiar la línea superior
                    for (int j = 0; j < BOARD_WIDTH; j++) {
                        board[0][j] = 0;
                    }
                    i++; // Volver a verificar la misma línea
                }
            }
            
            // Añadir puntos por líneas eliminadas (100 puntos por línea, con bonificación por múltiples líneas)
            if (linesCleared > 0) {
                int points = 100 * linesCleared * linesCleared; // Bonificación cuadrática por múltiples líneas
                addScore(points);
            }
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            // Dibujar fondo de la malla
            g.setColor(Color.DARK_GRAY);
            g.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
            
            // Dibujar la malla
            g.setColor(Color.GRAY);
            for (int i = 0; i <= BOARD_HEIGHT; i++) {
                g.drawLine(0, i * BLOCK_SIZE, CANVAS_WIDTH, i * BLOCK_SIZE);
            }
            for (int i = 0; i <= BOARD_WIDTH; i++) {
                g.drawLine(i * BLOCK_SIZE, 0, i * BLOCK_SIZE, CANVAS_HEIGHT);
            }
            
            // Dibujar las piezas en el tablero
            for (int i = 0; i < BOARD_HEIGHT; i++) {
                for (int j = 0; j < BOARD_WIDTH; j++) {
                    if (board[i][j] != 0) {
                        drawBlock(g, j, i, board[i][j]);
                    }
                }
            }
            
            // Dibujar la pieza actual
            if (currentPiece != null) {
                for (int i = 0; i < currentPiece.getHeight(); i++) {
                    for (int j = 0; j < currentPiece.getWidth(); j++) {
                        int value = currentPiece.getShape()[i][j];
                        if (value != 0) {
                            int x = currentPos.x + j;
                            int y = currentPos.y + i;
                            if (y >= 0) { // Solo dibujar si es visible
                                drawBlock(g, x, y, value);
                            }
                        }
                    }
                }
            }
            
            // Mostrar mensaje si el juego no ha comenzado
            if (!isStarted) {
                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 20));
                String msg = isGameOver ? "Juego Terminado" : "Presiona 'Iniciar Juego'";
                FontMetrics fm = g.getFontMetrics();
                int msgWidth = fm.stringWidth(msg);
                g.drawString(msg, (CANVAS_WIDTH - msgWidth) / 2, CANVAS_HEIGHT / 2);
            }
            
            // Mostrar mensaje de pausa
            if (isPaused) {
                g.setColor(new Color(0, 0, 0, 150)); // Fondo semitransparente
                g.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 20));
                String msg = "Presiona P para continuar";
                FontMetrics fm = g.getFontMetrics();
                int msgWidth = fm.stringWidth(msg);
                g.drawString(msg, (CANVAS_WIDTH - msgWidth) / 2, CANVAS_HEIGHT / 2);
            }
        
        }
        
        // Dibujar un bloque del tetris
        private void drawBlock(Graphics g, int x, int y, int colorValue) {
            Color colors[] = {
                Color.BLACK,        // 0 - Fondo (no usado)
                Color.CYAN,         // 1 - I
                Color.BLUE,         // 2 - J
                Color.ORANGE,       // 3 - L
                Color.YELLOW,       // 4 - O
                Color.GREEN,        // 5 - S
                Color.MAGENTA,      // 6 - T
                Color.RED           // 7 - Z
            };
            
            Color color = colors[colorValue];
            
            g.setColor(color);
            g.fillRect(x * BLOCK_SIZE + 1, y * BLOCK_SIZE + 1, BLOCK_SIZE - 2, BLOCK_SIZE - 2);
            
            g.setColor(color.brighter());
            g.drawLine(x * BLOCK_SIZE + 1, y * BLOCK_SIZE + 1, x * BLOCK_SIZE + 1, y * BLOCK_SIZE + BLOCK_SIZE - 2);
            g.drawLine(x * BLOCK_SIZE + 1, y * BLOCK_SIZE + 1, x * BLOCK_SIZE + BLOCK_SIZE - 2, y * BLOCK_SIZE + 1);
            
            g.setColor(color.darker());
            g.drawLine(x * BLOCK_SIZE + BLOCK_SIZE - 2, y * BLOCK_SIZE + 1, x * BLOCK_SIZE + BLOCK_SIZE - 2, y * BLOCK_SIZE + BLOCK_SIZE - 2);
            g.drawLine(x * BLOCK_SIZE + 1, y * BLOCK_SIZE + BLOCK_SIZE - 2, x * BLOCK_SIZE + BLOCK_SIZE - 2, y * BLOCK_SIZE + BLOCK_SIZE - 2);
        }
    }
    
    // Clase para representar las formas de Tetris
    private static class Shape {
        private int[][] shape;
        private int[][][] shapes; // Para rotación
        private int currentRotation;
        
        // Formas estándar de Tetris (0 = espacio vacío, 1-7 = tipos de bloques)
        private static final int[][][] SHAPES = {
            // I
            {
                {0, 0, 0, 0},
                {1, 1, 1, 1},
                {0, 0, 0, 0},
                {0, 0, 0, 0}
            },
            // J
            {
                {2, 0, 0},
                {2, 2, 2},
                {0, 0, 0}
            },
            // L
            {
                {0, 0, 3},
                {3, 3, 3},
                {0, 0, 0}
            },
            // O
            {
                {4, 4},
                {4, 4}
            },
            // S
            {
                {0, 5, 5},
                {5, 5, 0},
                {0, 0, 0}
            },
            // T
            {
                {0, 6, 0},
                {6, 6, 6},
                {0, 0, 0}
            },
            // Z
            {
                {7, 7, 0},
                {0, 7, 7},
                {0, 0, 0}
            }
        };
        
        private Shape(int[][] shape) {
            this.shape = new int[shape.length][shape[0].length];
            for (int i = 0; i < shape.length; i++) {
                System.arraycopy(shape[i], 0, this.shape[i], 0, shape[i].length);
            }
            
            // Precalcular rotaciones
            shapes = new int[4][][];
            shapes[0] = this.shape;
            currentRotation = 0;
            
            for (int i = 1; i < 4; i++) {
                shapes[i] = rotate(shapes[i-1]);
            }
        }
        
        // Obtener pieza aleatoria
        public static Shape getRandomShape() {
            Random rand = new Random();
            int idx = rand.nextInt(SHAPES.length);
            return new Shape(SHAPES[idx]);
        }
        
        // Obtener dimensiones
        public int getWidth() {
            return shape[0].length;
        }
        
        public int getHeight() {
            return shape.length;
        }
        
        // Obtener matriz de la forma
        public int[][] getShape() {
            return shape;
        }
        
        // Rotar a la derecha
        public Shape rotateRight() {
            currentRotation = (currentRotation + 1) % 4;
            shape = shapes[currentRotation];
            return this;
        }
        
        // Rotar a la izquierda
        public Shape rotateLeft() {
            currentRotation = (currentRotation + 3) % 4;
            shape = shapes[currentRotation];
            return this;
        }
        
        // Rotar matriz
        private int[][] rotate(int[][] matrix) {
            int n = matrix.length;
            int m = matrix[0].length;
            int[][] rotated = new int[m][n];
            
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < m; j++) {
                    rotated[j][n-1-i] = matrix[i][j];
                }
            }
            
            return rotated;
        }
    }
    
    // Método principal
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                TetrisGame game = new TetrisGame();
                game.setVisible(true);
            }
        });
    }
}