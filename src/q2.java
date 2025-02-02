import java.util.Arrays;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

public class q2 {

    public static int k;
    public static int j;

    public static void main(String[] args) {
        try {
            k = Integer.parseInt(args[0]);
            j = Integer.parseInt(args[1]);
            TreeMap<Integer, Integer> snakesAndLadders = new TreeMap<Integer, Integer>();
            snakesAndLadders.put(3,92);
            snakesAndLadders.put(27,9);
            Player p = new Player(snakesAndLadders);
            Adder a = new Adder(snakesAndLadders, k);
            Remover r = new Remover(snakesAndLadders, j);
            Thread pT = new Thread(p);
            Thread aT = new Thread(a);
            Thread rT = new Thread(r);
            pT.start();
            aT.start();
            rT.start();
            pT.join();
            aT.join();
            rT.join();
        } catch (Exception e) {                        // Catch errors
            System.out.println("ERROR " +e);           // And print them to the console
            e.printStackTrace();                       // Also print the stack trace
        }
    }
}

class Player implements Runnable{

    private TreeMap<Integer, Integer> snakesAndLadders;
    private ThreadLocalRandom rng;
    private int position;

    public Player(TreeMap<Integer, Integer> snakesAndLadders) {
        this.snakesAndLadders = snakesAndLadders;
        this.rng = ThreadLocalRandom.current();
        this.position = 0;
    }

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
                System.out.println(position);
            }
        } catch (Exception e) {                        // Catch errors
            System.out.println("ERROR " +e);           // And print them to the console
            e.printStackTrace();                       // Also print the stack trace
        }
    }

    public boolean makeMove(int steps) {
        position += steps;
        if(position >= 99) {
            return true;
        } else {
            position = checkForSnl(position);
            return false;
        }
    }

    public synchronized int checkForSnl(int pos) {
        if(snakesAndLadders.containsKey(pos))
            return snakesAndLadders.get(pos);
        return pos;
    }
}

class Adder implements Runnable{

    private TreeMap<Integer, Integer> snakesAndLadders;
    private int sleepTime;
    private ThreadLocalRandom rng;
    
    public Adder(TreeMap<Integer, Integer> snakesAndLadders, int sleepTime) {
        this.snakesAndLadders = snakesAndLadders;
        this.sleepTime = sleepTime;
        this.rng = ThreadLocalRandom.current();
    }

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
            
        } catch (Exception e) {                        // Catch errors
            System.out.println("ERROR " +e);           // And print them to the console
            e.printStackTrace();                       // Also print the stack trace
            System.out.println(snakesAndLadders.entrySet().toString());
        }
        
    }

    public synchronized void placeLadder() {
        int[] openSpots = new int[98-snakesAndLadders.keySet().size()];
        int j=0, top=0, bottom=0, bottomIndex=0;
        boolean safe = false;
        for(int i=1; i<99; i++) {
            if(!snakesAndLadders.containsKey(i) && !snakesAndLadders.containsValue(i))
                openSpots[j++] = i;
        }
        while(!safe) {
            safe = true;
            bottomIndex = rng.nextInt(openSpots.length);
            bottom = openSpots[bottomIndex];
            top = openSpots[rng.nextInt(bottomIndex, openSpots.length)];
            if((int)top/10 <= (int)bottom/10)
                safe = false;
        }
        snakesAndLadders.put(bottom, top);
        System.out.println("Ladder Placed at ("+bottom+", "+top+")");
    }

    public synchronized void placeSnake() {
        int[] openSpots = new int[89-snakesAndLadders.keySet().size()];
        int j=0, top=0, bottom=0, topIndex=0;
        boolean safe = false;
        for(int i=1; i<90; i++) {
            if(!snakesAndLadders.containsKey(i) && !snakesAndLadders.containsValue(i))
                openSpots[j++] = i;
        }
        while(!safe) {
            safe = true;
            topIndex = rng.nextInt(openSpots.length);
            top = openSpots[topIndex];
            if(top<=9 || topIndex == 0) {
                safe = false;
                continue;
            }
            bottom = openSpots[rng.nextInt(topIndex)];
            if((int)top/10 <= (int)bottom/10)
                safe = false;
        }
        snakesAndLadders.put(top, bottom);
        System.out.println("Snake placed at ("+top+", "+bottom+")");
    }
}

class Remover implements Runnable{

    private TreeMap<Integer, Integer> snakesAndLadders;
    private int sleepTime;
    private ThreadLocalRandom rng;
    
    public Remover(TreeMap<Integer, Integer> snakesAndLadders, int sleepTime) {
        this.snakesAndLadders = snakesAndLadders;
        this.sleepTime = sleepTime;
        this.rng = ThreadLocalRandom.current();
    }

    @Override
    public void run() {
        try {
            while(true) {
                removeSnakeOrLadder();
                Thread.sleep(sleepTime);
            }
        } catch (Exception e) {                        // Catch errors
            System.out.println("ERROR " +e);           // And print them to the console
            e.printStackTrace();                       // Also print the stack trace
            System.out.println(snakesAndLadders.entrySet().toString());
        }
    }

    public synchronized void removeSnakeOrLadder() {
        int size = snakesAndLadders.keySet().size();
        if(size != 0) {
            int removeIndex = rng.nextInt(size);
            Object removeKey = snakesAndLadders.keySet().toArray()[removeIndex]; // Causing big bad exceptions
            System.out.println("Ladder/Snake removed at ("+removeKey+", "+snakesAndLadders.get(removeKey)+")");
            snakesAndLadders.remove(removeKey);
        }
    }
}