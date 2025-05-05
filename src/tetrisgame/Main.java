package tetrisgame;

public class Main {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                TetrisGame game = new TetrisGame();
                game.setVisible(true);
            }
        });
    }
}