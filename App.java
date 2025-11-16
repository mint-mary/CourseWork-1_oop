import javax.swing.JFrame;

public class App {
    public static void main(String[] args) throws Exception {
        int rowCount = 21;
        int columnCount = 19;
        int tileSize = 32;
        int boardWidth = columnCount * tileSize;
        int boardHeight = rowCount * tileSize;

        JFrame frame = new JFrame("Pac Man");
        frame.setSize(boardWidth, boardHeight);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        PacMan pacmanGame = new PacMan();
        frame.add(pacmanGame);
        frame.pack();
        pacmanGame.requestFocus();
        frame.setVisible(true);

        int dialogResult = javax.swing.JOptionPane.showConfirmDialog(
            frame, 
            "Do you want to load the last saved game?", 
            "Load Game", 
            javax.swing.JOptionPane.YES_NO_OPTION);
        
        if (dialogResult == javax.swing.JOptionPane.YES_OPTION) {
            pacmanGame.loadGame();
        } else {
            pacmanGame.gameLoop.start(); 
        }
    }
}