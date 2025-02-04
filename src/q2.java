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
            Adder a = new Adder(board, k, System.currentTimeMillis(), fixedInitLog);
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

    private Cell[][] board;
    private ThreadLocalRandom rng;
    private int position;
    private long startTime;
    private String timeDiff;
    private List<String> log;

    public Player(Cell[][] board, long startTime) {
        this.board = board;
        this.rng = ThreadLocalRandom.current();
        this.position = 0;
        this.startTime = startTime;
        this.log = new ArrayList<String>();
    }

    public List<String> getLog() { return this.log; }

    @Override
    public void run() {
        try {
            boolean won;
            while(true) {
                won = makeMove(rng.nextInt(1, 7));
                if(!won)
                    Thread.sleep(rng.nextLong(20, 51));
                else {
                    Thread.sleep(100);
                    won = false;
                    position = 0;
                }
            }
        } catch (Exception e) {                        // Catch interrupts
            System.out.println("Thread "+Thread.currentThread().threadId()+": "+e);
        }
    }

    public boolean makeMove(int steps) {
        position += steps;
        timeDiff = Long.toUnsignedString(1000000000 + System.currentTimeMillis()-startTime).substring(1);
        if(position >= 99) {
            log.add(timeDiff+" Player wins");
            return true;
        } else {
            int newPos;
            log.add(timeDiff+" Player "+position);
            if((newPos = checkForSnl(position)) != position) {
                log.add(timeDiff+" Player "+position+" "+newPos);
                position = newPos;
            }
            return false;
        }
    }

    public synchronized int checkForSnl(int pos) {
        switch(board[pos/10][pos%10].getStatus()) {
            case snakeTail:
                return board[pos/10][pos%10].getDestination();
            case ladderBase:
                return board[pos/10][pos%10].getDestination();
            default:
                return pos;
        }
    }
}

// The Adder class provides funcitonality for adding snakes and ladders to the board
class Adder implements Runnable{

    private Cell[][] board;
    private int sleepTime;
    private ThreadLocalRandom rng;
    private long startTime;
    private String timeDiff;
    private List<String> log;
    
    public Adder(Cell[][] board, int sleepTime, long startTime, List<String> log) {
        this.board = board;
        this.sleepTime = sleepTime;
        this.rng = ThreadLocalRandom.current();
        this.startTime = startTime;
        this.log = log;
    }

    public Adder(Cell[][] board, int sleepTime, long startTime) {
        this.board = board;
        this.sleepTime = sleepTime;
        this.rng = ThreadLocalRandom.current();
        this.startTime = startTime;
        this.log = new ArrayList<String>();
    }
    
    public List<String> getLog() { return this.log; }

    @Override
    public void run() {
        try {
            while(true) {
                if(rng.nextBoolean()) {
                    placeLadder();
                } else {
                    placeSnake();
                }
                Thread.sleep(sleepTime);
            }
        } catch (Exception e) {                        // Catch interrupts
            System.out.println("Thread "+Thread.currentThread().threadId()+": "+e);
        }
        
    }

    public synchronized void placeLadder() {
        List<Cell> emptyCells = Arrays.stream(board).flatMap(r -> Arrays.stream(r))
            .filter(c -> c.getStatus() == CellStatus.empty)
            .collect(Collectors.toList());
        emptyCells.removeFirst();
        emptyCells.removeLast();

        if(emptyCells.isEmpty())
            return;
        
        int baseIndex=0;
        Cell top=new Cell(0), base = new Cell(0);
        boolean safe = false;

        while(!safe) {
            safe = true;
            baseIndex = rng.nextInt(emptyCells.size()-1);  // -1 ensures top element cannot be picked
            base = emptyCells.get(baseIndex);
            top = emptyCells.get(rng.nextInt(baseIndex+1, emptyCells.size()));
            if((int)top.getPosition()/10 <= (int)base.getPosition()/10)
                safe = false;
        }

        base.setStatus(CellStatus.ladderBase);
        top.setStatus(CellStatus.ladderTop);
        base.setDestination(top.getPosition());
        timeDiff = Long.toUnsignedString(1000000000 + System.currentTimeMillis()-startTime).substring(1);
        log.add(timeDiff+" Adder ladder "+base.getPosition()+" "+top.getPosition());
    }

    public synchronized void placeSnake() {
        List<Cell> emptyCells = Arrays.stream(board).flatMap(r -> Arrays.stream(r))
            .filter(c -> c.getStatus() == CellStatus.empty)
            .collect(Collectors.toList());
        emptyCells.removeFirst();
        emptyCells.removeLast();

        if(emptyCells.isEmpty())
            return;
        
        int tailIndex=0;
        Cell tail=new Cell(0), head = new Cell(0);
        boolean safe = false;
        
        while(!safe) {
            safe = true;
            tailIndex = rng.nextInt(1, emptyCells.size()); // 1 Origin prevents first element selection
            tail = emptyCells.get(tailIndex);
            head = emptyCells.get(rng.nextInt(tailIndex));
            if((int)tail.getPosition()/10 <= (int)head.getPosition()/10)
                safe = false;
        }

        tail.setStatus(CellStatus.snakeTail);
        head.setStatus(CellStatus.snakeHead);
        tail.setDestination(head.getPosition());
        timeDiff = Long.toUnsignedString(1000000000 + System.currentTimeMillis()-startTime).substring(1);
        log.add(timeDiff+" Adder snake "+tail.getPosition()+" "+head.getPosition());
    }
}

// The Remover class provides funcitonality for removing snakes and ladders from the board
class Remover implements Runnable{

    private Cell[][] board;
    private int sleepTime;
    private ThreadLocalRandom rng;
    private long startTime;
    private String timeDiff;
    private List<String> log;
    
    public Remover(Cell[][] board, int sleepTime, long startTime) {
        this.board = board;
        this.sleepTime = sleepTime;
        this.rng = ThreadLocalRandom.current();
        this.startTime = startTime;
        this.log = new ArrayList<String>();
    }

    public List<String> getLog() { return this.log; }

    @Override
    public void run() {
        try {
            while(true) {
                removeSnakeOrLadder();
                Thread.sleep(sleepTime);
            }
        } catch (Exception e) {                        // Catch interrupts
            System.out.println("Thread "+Thread.currentThread().threadId()+": "+e);
        }
    }

    public synchronized void removeSnakeOrLadder() {
        List<Cell> occupiedCells = Arrays.stream(board).flatMap(r -> Arrays.stream(r))
            .filter(c -> (c.getStatus() == CellStatus.snakeTail || c.getStatus() == CellStatus.ladderBase))
            .collect(Collectors.toList());
        if(occupiedCells.isEmpty())
            return;

        Cell startCell = occupiedCells.get(rng.nextInt(occupiedCells.size()));
        Cell endCell = board[startCell.getDestination()/10][startCell.getDestination()%10];
        endCell.setStatus(CellStatus.empty);
        startCell.setStatus(CellStatus.empty);
        startCell.setDestination(0);
        timeDiff = Long.toUnsignedString(1000000000 + System.currentTimeMillis()-startTime).substring(1);
        if(startCell.getPosition() < endCell.getPosition())
            log.add(timeDiff+" Remover ladder "+startCell.getPosition()+" "+endCell.getPosition());
        else
            log.add(timeDiff+" Remover snake "+startCell.getPosition()+" "+endCell.getPosition());
    }
}