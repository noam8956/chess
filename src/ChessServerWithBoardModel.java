import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.concurrent.*;

// Chess Server
class ChessServer {
    public static void main(String[] args) {
        ExecutorService pool = Executors.newCachedThreadPool(); // Supports multiple players dynamically
        ChessGame game = new ChessGame(); // Game logic
        
        try (ServerSocket serverSocket = new ServerSocket(5000)) { // Auto-select available port
            System.out.println("Chess Server started on port: " + serverSocket.getLocalPort());
            
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Player connected.");
                Thread clientThread = new Thread(new ClientHandler(socket, game));
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ClientHandler implements Runnable {
    private Socket socket;
    private ChessGame game;

    public ClientHandler(Socket socket, ChessGame game) {
        this.socket = socket;
        this.game = game;
    }
    
    @Override
    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            Scanner scanner = new Scanner(System.in);
            //wait for 2 clients to connect before starting the game

            System.out.println("game started");
            
            while (true) {
                synchronized (game) {
                    System.out.println(game.getBoardVisual());
                }
                String clientMove = in.readLine();
                if (clientMove == null || clientMove.equals("exit")) break;
                
                synchronized (game) {
                    if (game.isValidMove(clientMove)) {
                        game.makeMove(clientMove);
                        System.out.println("Opponent moved: " + clientMove);
                    } else {
                        System.out.println("Invalid move received.");
                    }
                }
                
                System.out.print("Your move: ");
                String move = scanner.nextLine();
                out.println(move);
                if (move.equals("exit")) break;
            }
            
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ChessGame {
    private char[][] board;
    
    public ChessGame() {
        resetBoard();
    }
    
    private void resetBoard() {
        board = new char[][] {

            {'R', 'N', 'B', 'Q', 'K', 'B', 'N', 'R'},
            {'P', 'P', 'P', 'P', 'P', 'P', 'P', 'P'},
            {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
            {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
            {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
            {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
            {'p', 'p', 'p', 'p', 'p', 'p', 'p', 'p'},
            {'r', 'n', 'b', 'q', 'k', 'b', 'n', 'r'}
        };
    }
    
    public boolean isValidMove(String move) {
        return move.matches("[a-h][1-8]-[a-h][1-8]");
    }
    
    public void makeMove(String move) {
        int fromCol = move.charAt(0) - 'a';
        int fromRow = 8 - (move.charAt(1) - '0');
        int toCol = move.charAt(3) - 'a';
        int toRow = 8 - (move.charAt(4) - '0');
        
        board[toRow][toCol] = board[fromRow][fromCol];
        board[fromRow][fromCol] = ' ';
    }
    
    public String getBoardVisual() {
        StringBuilder sb = new StringBuilder();
        sb.append("  a b c d e f g h ");
        sb.append("  ---------------- ");
        for (int i = 0; i < 8; i++) {
            sb.append((8 - i) + "|");
            for (int j = 0; j < 8; j++) {
                sb.append(board[i][j]).append(' ');
            }
            sb.append("|" + (8 - i) + " ");
        }
        sb.append("  ---------------- ");
        sb.append("  a b c d e f g h ");
        return sb.toString();
    }
}




class ChessClient {
    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost", 5000);
            System.out.println("Connected to Chess Server.");
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            Scanner scanner = new Scanner(System.in);

            while (true) {
                System.out.print("Your move: ");
                String move = scanner.nextLine();
                out.println(move);
                if (move.equals("exit")) break;

                String serverMove = in.readLine();
                if (serverMove == null || serverMove.equals("exit")) break;
                System.out.println("Opponent moved: " + serverMove);
            }

            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
