import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

// Create enum type for describing the contents of a cell
enum CellStatus {empty, snakeTail, snakeHead, ladderBase, ladderTop};

public class q2 {

    public static int k;
    public static int j;
    public static int s;

    public static void main(String[] args) {
        try {
            // Parse command line arguments
            k = Integer.parseInt(args[0]);
            j = Integer.parseInt(args[1]);
            s = Integer.parseInt(args[2]);

            // Initialise the board with 100 empty cells
            Cell[][] board = new Cell[10][10];
            for(int i=0; i<10; i++) {
                for(int j=0; j<10; j++) {
                    board[i][j] = new Cell((i*10)+j);
                }
            }

            // Initialise board randomly with 10 ladders and 9 snakes
            Adder initialiser = new Adder(board, k, System.currentTimeMillis());
            initialiser.placeSnake();
            for(int i=0; i<9; i++) {
                initialiser.placeLadder();
                initialiser.placeSnake();
            }

            // Change timestamps for initial adds to 000000000
            List<String> initLog = initialiser.getLog();
            List<String> fixedInitLog = new ArrayList<String>();
            for(String s : initLog) {
                fixedInitLog.add(s.replaceFirst("[0-9]* ", "000000000 "));
            }

            // Instantiate a Player, Adder and Remover. Initialise Adder log accordingly.
            // Create 3 threads using these Runnable objects
            Player p = new Player(board, System.currentTimeMillis());
            Adder a = new Adder(board, k, System.currentTimeMillis());
            a.setLog(fixedInitLog);
            Remover r = new Remover(board, j, System.currentTimeMillis());
            Thread pT = new Thread(p);
            Thread aT = new Thread(a);
            Thread rT = new Thread(r);

            // Start these threads then sleep for s seconds
            pT.start();
            aT.start();
            rT.start();
            Thread.sleep(s*1000);

            // Interrupt and join all threads
            pT.interrupt();
            aT.interrupt();
            rT.interrupt();
            pT.join();
            aT.join();
            rT.join();

            // Combine, sort and print all logs
            List<String> allLogs = new ArrayList<String>();
            allLogs.addAll(p.getLog());
            allLogs.addAll(a.getLog());
            allLogs.addAll(r.getLog());
            allLogs.sort(Comparator.comparing((String s) -> Integer.valueOf(s.substring(0, 9))));
            for(String s : allLogs)
                System.out.println(s);

        } catch (Exception e) {                        // Catch errors
            System.out.println("ERROR " +e);           // And print them to the console
            e.printStackTrace();                       // Also print the stack trace
        }
    }
}

// The Cell class is used to represent a single square on the snakes and ladders board.
class Cell {
    private CellStatus status;                         // Status describes the cell contents
    private int position;                              // Position is where the cell lies on the board
    private int destination;                           // Destination is the connected cell if status != empty

    public Cell(int position) {                        // Simple constructor
        this.status = CellStatus.empty;
        this.position = position;
        this.destination = 0;
    }

    public CellStatus getStatus() { return status; }   // Getter and setter for status
    public void setStatus(CellStatus status) { this.status = status; }

    public int getPosition() { return position; }      // Getter and setter for position
    public void setPosition(int position) { this.position = position; }
    
    public int getDestination() { return destination; }// Getter and setter for destination
    public void setDestination(int destination) { this.destination = destination; }
}

// The Player class models the behaviour of a snakes and ladders player
class Player implements Runnable{

    private Cell[][] board;                            // Reference to the board 2D cell array
    private ThreadLocalRandom rng;                     // Thread-safe rng for random ops
    private int position;                              // Current position of player on the board
    private long startTime;                            // Value of system clock when thread was started
    private String timeDiff;                           // String rep of time since thread start
    private List<String> log;                          // Log of actions taken

    public Player(Cell[][] board, long startTime) {    // Simple constructor
        this.board = board;
        this.rng = ThreadLocalRandom.current();
        this.position = 0;
        this.startTime = startTime;
        this.log = new ArrayList<String>();
    }

    public List<String> getLog() { return this.log; }  // Getter for the log

    @Override
    public void run() {
        try {
            boolean won;
            while(true) {                              // Roll 1-6 (inclusive), update position and won boolean
                won = makeMove(rng.nextInt(1, 7));
                if(!won)                               // 20-50ms sleep (inclusive) after non-winning move
                    Thread.sleep(rng.nextLong(20, 51));
                else {
                    Thread.sleep(100);          // 100ms sleep on win
                    won = false;                       // reset won boolean
                    position = 0;                      // start at first cell again
                }
            }
        } catch (Exception e) {                        // Catch interrupts and log them to console
            System.out.println("Thread "+Thread.currentThread().threadId()+": "+e);
        }
    }

    // This function calculates the consequences of a given dice roll
    public boolean makeMove(int steps) {
        position += steps;                             // Make move
        // Set timestamp for the log in ms, with leading zeroes to fill 9 total digits, as a string
        timeDiff = Long.toUnsignedString(1000000000+System.currentTimeMillis()-startTime).substring(1);

        if(position >= 99) {
            log.add(timeDiff+" Player wins");          // If you win, log it
            return true;                               // Return indicates that you won
        } else {
            int newPos;
            log.add(timeDiff+" Player "+position);     // If you don't win, log the new position
            if((newPos = checkForSnl(position)) != position) { // Check for snakes or ladders
                log.add(timeDiff+" Player "+position+" "+newPos); // Log snake or ladder usage
                position = newPos;                     // Update position if snake or ladder used
            }
            return false;                              // Return indicates that you didn't win
        }
    }

    // This function checks the board at a specific location for snakes and ladders
    // It is essential that board access is synchronized across all threads
    public synchronized int checkForSnl(int pos) {
        switch(board[pos/10][pos%10].getStatus()) {
            case snakeTail:                            // If at tail of snake, return next position
                return board[pos/10][pos%10].getDestination();
            case ladderBase:                           // If at base of ladder, return next position
                return board[pos/10][pos%10].getDestination();
            default:                                   // Else return current position
                return pos;
        }
    }
}

// The Adder class provides funcitonality for adding snakes and ladders to the board
class Adder implements Runnable{

    private Cell[][] board;                            // Pointer to the shared 2D Cell array          
    private int sleepTime;                             // Sleep time between actions in ms             
    private ThreadLocalRandom rng;                     // Thread-safe rng for Random ops               
    private long startTime;                            // Value of system clock when thread was started
    private String timeDiff;                           // String rep of time since thread start        
    private List<String> log;                          // Log of actions taken                         

    public Adder(Cell[][] board, int sleepTime, long startTime) { // Simple constructor
        this.board = board;
        this.sleepTime = sleepTime;
        this.rng = ThreadLocalRandom.current();
        this.startTime = startTime;
        this.log = new ArrayList<String>();
    }

    // Getter and Setter for the log attribute
    public List<String> getLog() { return log; }
    public void setLog(List<String> log) { this.log = log; }

    @Override
    public void run() {
        try {
            while(true) {                              // Forever
                if(rng.nextBoolean()) {
                    placeLadder();                     //   0.5 chance to place ladder
                } else {
                    placeSnake();                      //   0.5 chance to place snake
                }
                Thread.sleep(sleepTime);               // Then sleep for sleep time
            }
        } catch (Exception e) {                        // Catch interrupts and log them to console
            System.out.println("Thread "+Thread.currentThread().threadId()+": "+e);
        }
        
    }

    // This function places a random ladder on the board (if there is space available)
    public synchronized void placeLadder() {
        // Collect all empty cells into a list, and remove the start and end cells
        List<Cell> emptyCells = Arrays.stream(board).flatMap(r -> Arrays.stream(r))
            .filter(c -> c.getStatus() == CellStatus.empty)
            .collect(Collectors.toList());
        emptyCells.removeFirst();
        emptyCells.removeLast();

        // If the resulting list is empty, there is nowhere to place a ladder, so abort
        if(emptyCells.isEmpty())
            return;
        
        // Initialise my loop variables to prevent compiler complaints below
        int baseIndex=0;
        Cell top=new Cell(0), base = new Cell(0);
        boolean safe = false;

        // Select a pair of cells, taking the "top" cell from a higher index
        // Check if they're compatible (not in the same row), if not then repeat the process
        while(!safe) {
            safe = true;
            baseIndex = rng.nextInt(emptyCells.size()-1);  // -1 ensures top cell cannot be the base
            base = emptyCells.get(baseIndex);              // select a top cell from a higher index
            top = emptyCells.get(rng.nextInt(baseIndex+1, emptyCells.size()));
            if((int)top.getPosition()/10 <= (int)base.getPosition()/10) // check compatibility
                safe = false;
        }

        // Create a ladder between base and top cells
        base.setStatus(CellStatus.ladderBase);
        top.setStatus(CellStatus.ladderTop);
        base.setDestination(top.getPosition());

        // Set timestamp as before and log the action taken
        timeDiff = Long.toUnsignedString(1000000000 + System.currentTimeMillis()-startTime).substring(1);
        log.add(timeDiff+" Adder ladder "+base.getPosition()+" "+top.getPosition());
    }

    // This function places a random snake on the board (if there is space available)
    public synchronized void placeSnake() {
        // Collect all empty cells into a list, and remove the start and end cells
        List<Cell> emptyCells = Arrays.stream(board).flatMap(r -> Arrays.stream(r))
            .filter(c -> c.getStatus() == CellStatus.empty)
            .collect(Collectors.toList());
        emptyCells.removeFirst();
        emptyCells.removeLast();

        // If the resulting list is empty, there is nowhere to place a snake, so abort
        if(emptyCells.isEmpty())
            return;
        
        // Initialise my loop variables to prevent compiler complaints below
        int tailIndex=0;
        Cell tail=new Cell(0), head = new Cell(0);
        boolean safe = false;
        
        // Select a pair of cells, taking the "tail" cell from a higher index
        // Check if they're compatible (not in the same row), if not then repeat the process
        while(!safe) {
            safe = true;
            tailIndex = rng.nextInt(1, emptyCells.size()); // 1 origin prevents tail being first Cell
            tail = emptyCells.get(tailIndex);
            head = emptyCells.get(rng.nextInt(tailIndex)); // Select a head Cell from any lower index
            if((int)tail.getPosition()/10 <= (int)head.getPosition()/10) // Check compatibility
                safe = false;
        }

        // Create a snake between tail and head cells
        tail.setStatus(CellStatus.snakeTail);
        head.setStatus(CellStatus.snakeHead);
        tail.setDestination(head.getPosition());

        // Set timestamp as before and log the action taken
        timeDiff = Long.toUnsignedString(1000000000 + System.currentTimeMillis()-startTime).substring(1);
        log.add(timeDiff+" Adder snake "+tail.getPosition()+" "+head.getPosition());
    }
}

// The Remover class provides funcitonality for removing snakes and ladders from the board
class Remover implements Runnable{

    private Cell[][] board;                            // Pointer to the shared 2D Cell array
    private int sleepTime;                             // Sleep time between actions in ms
    private ThreadLocalRandom rng;                     // Thread-safe rng for Random ops
    private long startTime;                            // Value of system clock when thread was started
    private String timeDiff;                           // String rep of time since thread start
    private List<String> log;                          // Log of actions taken
    
    public Remover(Cell[][] board, int sleepTime, long startTime) { // Simple constructor
        this.board = board;
        this.sleepTime = sleepTime;
        this.rng = ThreadLocalRandom.current();
        this.startTime = startTime;
        this.log = new ArrayList<String>();
    }

    public List<String> getLog() { return this.log; }  // Getter for the log

    @Override
    public void run() {
        try {               // NB Assignment does not specify that snake/ladder REMOVAL must be equal chance
            while(true) {                              // Forever
                removeSnakeOrLadder();                 // Remove snake or ladder
                Thread.sleep(sleepTime);               // Sleep for sleep time
            }
        } catch (Exception e) {                        // Catch interrupts and log them to console
            System.out.println("Thread "+Thread.currentThread().threadId()+": "+e);
        }
    }

    // This function removes a randomly selected snake or ladder from the board
    // It is yet again essential for this board modification to be synchronisde across threads
    public synchronized void removeSnakeOrLadder() {
        // Collect all snakeTail or ladderBase cells into a list
        List<Cell> occupiedCells = Arrays.stream(board).flatMap(r -> Arrays.stream(r))
            .filter(c -> (c.getStatus() == CellStatus.snakeTail || c.getStatus() == CellStatus.ladderBase))
            .collect(Collectors.toList());

        // If the resulting list is empty, there are no snakes or ladders to remove, so abort
        if(occupiedCells.isEmpty())
            return;

        // Select a random snakeTail or ladderBase cell from the list, and store its head/top too
        Cell startCell = occupiedCells.get(rng.nextInt(occupiedCells.size()));
        Cell endCell = board[startCell.getDestination()/10][startCell.getDestination()%10];

        // Remove the snake or ladder from the board
        endCell.setStatus(CellStatus.empty);
        startCell.setStatus(CellStatus.empty);
        startCell.setDestination(0);

        // Set timestamp as before and log the action taken
        timeDiff = Long.toUnsignedString(1000000000 + System.currentTimeMillis()-startTime).substring(1);
        if(startCell.getPosition() < endCell.getPosition())
            log.add(timeDiff+" Remover ladder "+startCell.getPosition()+" "+endCell.getPosition());
        else
            log.add(timeDiff+" Remover snake "+startCell.getPosition()+" "+endCell.getPosition());
    }
}