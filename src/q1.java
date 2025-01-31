import java.awt.image.*;
import java.io.*;
import javax.imageio.*;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

// Custom enum type for the orientation
enum Orientation {up, down, left, right};

// 16 snowmen, r=50, 4096x4096, fill off
// t1 30.2
// t2 25.86
// t4 24.56
// t8 22.12

// 24 snowmen r=200, 1920x1080, fill on
// t1 38.6
// t2 35.87
// t4 36.62
// t8 

// 360 snowmen, r=300, 1920x1080, fill off
// 1 thread  - 8.27 - 3.66
// 2 threads - 6.87 - 2.93
// 3 threads - 6.78 - 2.91
// 4 threads - 7.13 - 3.04
// 5 threads - 6.71 - 2.87
// 6 threads - 6.89 - 2.93
// 7 threads - 6.6  - 2.85
// 8 threads - 6.86 - 3.05

// 32 snowmen, r=100, 1920x1080, fill on
// 1 thread  - 8.33 - 7.96 - 8.55 
// 2 threads - 6.82 - 6.67 - 6.45 
// 3 threads - 6.69 - 5.99 - 5.91 
// 4 threads - 5.72 - 6.02 - 6.29 
// 5 threads - 5.62 - 6.03 - 5.68 
// 6 threads - 5.66 - 5.78 - 5.53 
// 7 threads - 5.34 - 5.60 - 5.56 
// 8 threads - 5.81 - 6.15 - 5.09


public class q1 {

    // Parameters
    public static int t;
    public static int n;
    public static int width;
    public static int height;

    public static void main(String[] args) {
        try {
            width = Integer.parseInt(args[0]);         // Width = first command line parameter
            height = Integer.parseInt(args[1]);        // Height = second command line parameter
            t = Integer.parseInt(args[2]);             // #Threads = third command line parameter
            n = Integer.parseInt(args[3]);             // #Snowmen = fourth command line parameter

            // Create a blank image
            // BufferedImage outputimage = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);

            // Create a list for storing drawn/in-progress snowmen, wihtout sync issues
            // ArrayList<SnowmanDetails> drawn = new ArrayList<SnowmanDetails>();

            // Instantiate the SnowmanThread class so we can reference its run() method below
            SnowmanThread snowmanThread;

            // Create some long variables for timing execution
            long timeBefore, timeAfter;
            Thread[] threads;
            int nTests = 100;
            long[][] results = new long[t][nTests];

            for(int i=1; i<=t; i++) {
                for(int k=0; k<nTests; k++) {
                    threads = new Thread[i];                   // Create a Thread array of t threads
                    snowmanThread = new SnowmanThread(new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB),
                        new ArrayList<SnowmanDetails>(), n/i);
                    for(int j=0; j<i; j++)                     // Instantiate t threads using snowmanThread for the runnable
                        threads[j] = new Thread(snowmanThread);
                    timeBefore = System.currentTimeMillis();   // Get start time
                    for(Thread t : threads)                    // Start all the threads
                        t.start();
                    for(Thread t : threads)                    // Join all the threads
                        t.join();
                    timeAfter = System.currentTimeMillis();    // Get finish time

                    System.out.println(timeAfter-timeBefore); // Print out time taken in ms
                    results[i-1][k]=(timeAfter-timeBefore);
                }
            }
            System.out.println(1+" thread - "+mean(results[0]));
            for(int i=1; i<t; i++) {
                System.out.println((i+1)+" threads - "+mean(results[i]));
            }


            // Thread[] threads = new Thread[t];          // Create a Thread array of t threads
            // for(int i=0; i<t; i++)                     // Instantiate t threads using snowmanThread for the runnable
            //     threads[i] = new Thread(snowmanThread);
            // timeBefore = System.currentTimeMillis();   // Get start time
            // for(Thread t : threads)                    // Start all the threads
            //     t.start();
            // for(Thread t : threads)                    // Join all the threads
            //     t.join();
            // timeAfter = System.currentTimeMillis();    // Get finish time

            // System.out.println(timeAfter-timeBefore); // Print out time taken in ms

            // Write the image to a .png file
            // File outputfile = new File("outputimage.png");
            // ImageIO.write(outputimage, "png", outputfile);

        } catch (Exception e) {                        // Catch errors
            System.out.println("ERROR " +e);           // And print them to the console
            e.printStackTrace();                       // Also print the stack trace
        }
    }

    public static double mean(long[] arr) {
        if(arr.length == 0)
            return -1.0;
        long sum = 0;
        for(long l : arr) {
            sum += l;
        }
        // double mean = sum / arr.length;
        return ((double)sum) / ((double)arr.length);
    }
}

class SnowmanDetails {

    // Constants
    public static final int MIN_SIZE = 8;              // Minimum snowman base radius
    public static final double SCALE = 0.66;           // Scaling factor for each circle in snowman

    // Private variables
    private int[] xs;
    private int[] ys;
    private int[] rs;
    private Orientation o;
    private int boundaryX;
    private int boundaryY;
    private int boundaryR;

    public SnowmanDetails(int x, int y, int r, Orientation o) {
        this.xs = new int[3];                          // Create array for x co-ords
        this.ys = new int[3];                          // Create array for y co-ords
        this.rs = new int[3];                          // Create array for radii
        this.o = o;                                    // Store orientation
        this.xs[0] = x;                                // Store base x co-ord
        this.ys[0] = y;                                // Store base y co-ord
        this.rs[0] = r;                                // Store base radius
        this.rs[1] = (int)(r*SCALE);                   // Calculate secondary radius
        this.rs[2] = (int)(r*SCALE*SCALE);             // Calculate tertiary radius
        boundaryR = rs[0]+rs[1]+rs[2];
        switch (o) {
            case up:                                   // Calculate secondary and tertiary
                xs[1] = x;                             // center co-ords for up orientation
                xs[2] = x;
                ys[1] = y-(r+rs[1]);
                ys[2] = ys[1]-(rs[1]+rs[2]);
                boundaryX=x;
                boundaryY=y-(rs[1]+rs[2]);
                break;
            case down:                                 // Calculate secondary and tertiary
                xs[1] = x;                             // center co-ords for up orientation
                xs[2] = x;
                ys[1] = y+(r+rs[1]);
                ys[2] = ys[1]+(rs[1]+rs[2]);
                boundaryX=x;
                boundaryY=y+(rs[1]+rs[2]);
                break;
            case left:                                 // Calculate secondary and tertiary
                xs[1] = x-(r+rs[1]);                   // center co-ords for up orientation
                xs[2] = xs[1]-(rs[1]+rs[2]);
                ys[1] = y;
                ys[2] = y;
                boundaryX=x-(rs[1]+rs[2]);
                boundaryY=y;
                break;
            case right:                                // Calculate secondary and tertiary
                xs[1] = x+(r+rs[1]);                   // center co-ords for up orientation
                xs[2] = xs[1]+(rs[1]+rs[2]);
                ys[1] = y;
                ys[2] = y;
                boundaryX=x+(rs[1]+rs[2]);
                boundaryY=y;
                break;
            default:
                break;
        }
    }

    public int getX(int i) { return xs[i]; }           // Indexed getter for all X co-ords
    public void setX(int x, int i) { xs[i] = x; }      // Indexed setter for all X co-ords

    public int getY(int i) { return ys[i]; }           // Indexed getter for all Y co-ords
    public void setY(int y, int i) { ys[i] = y; }      // Indexed setter for all Y co-ords

    public int getR(int i) { return rs[i]; }           // Indexed getter for all Radii
    public void setR(int r, int i) { rs[i] = r; }      // Indexed setter for all Radii

    public Orientation getO() { return o; }            // Getter for Orientation
    public void setO(Orientation o) { this.o = o; }    // Setter for Orientation

    public int getBoundaryX() { return boundaryX; }            // Getter for Orientation
    public void setBoundaryX(int bX) { this.boundaryX = bX; }    // Setter for Orientation
    
    public int getBoundaryY() { return boundaryY; }            // Getter for Orientation
    public void setBoundaryY(int bY) { this.boundaryY = bY; }    // Setter for Orientation
    
    public int getBoundaryR() { return boundaryR; }            // Getter for Orientation
    public void setBoundaryR(int bR) { this.boundaryR = bR; }    // Setter for Orientation
}

class SnowmanThread implements Runnable {

    // Private vars
    private BufferedImage img;
    private ArrayList<SnowmanDetails> drawn;
    private int numOfSnowmen;
    private int maxSize;

    // Constants
    public static final int MIN_SIZE = 8;
    public static final double SCALE = 0.66;

    public SnowmanThread(BufferedImage img, ArrayList<SnowmanDetails> drawn, int numOfSnowmen) {
        super();                                       // Use default Runnable constructor
        this.img = img;                                // Initialise private img reference
        this.drawn = drawn;
        this.numOfSnowmen = numOfSnowmen;              // Initialise private numOfSnowmen

        // Calculate max possible size of snowman that could fit on the canvas and set maxSize
        int min = (img.getWidth() < img.getHeight() ? img.getWidth() : img.getHeight());
        this.maxSize = (int)(0.5*min/((2*SCALE*SCALE)+(2*SCALE)+(2)));
    }

    @Override
    public void run() {                                // Overriden run method defines what the thread will do
        for(int i=0; i<numOfSnowmen; i++)              // when .start() is called on a thread that was created
            drawRandomSnowman(this.img);               // using this runnable as its constuctor argument
    }

    // Adds a SnowmanDetails object to the shared list, indicating it is drawn
    // or in progress. Differentiating between drawn and in progress is irrelevant
    public void setDrawn(SnowmanDetails s) {
        drawn.add(s);                                  // Add the proposed snowman at the end of the lsit
    }

    // Function to check all 9 possible pairs of circles for intersection
    // between two given snowmen
    public boolean intersects(SnowmanDetails s0, SnowmanDetails s1) {
        if(!intersects(s0.getBoundaryX(), s1.getBoundaryX(), s0.getBoundaryY(), s1.getBoundaryY(), s0.getBoundaryR()+5, s1.getBoundaryR()+5))
            return false;                              // Only continue to detailed check if they're close
        for(int i=0; i<3; i++) {                       // For each pair of circles across the two snowmen
            for(int j=0; j<3; j++) {                   // If the circles intersect, return true
                if(intersects(s0.getX(i), s1.getX(j), s0.getY(i), s1.getY(j), s0.getR(i), s1.getR(j))) {
                    return true;
                }
            }
        }
        return false;                                  // If no check returned true, then return false
    }

    // Simple distance formula function to check if two circles intersect. This
    // also returns true for the case where one circle is fully inside the other
    public boolean intersects(int x0, int x1, int y0, int y1, int r0, int r1) {
        int xDist = x0 - x1;
        int yDist = y0 - y1;
        int rSum = r0 + r1;                            // The +1 here ensures that 1 pixel rounding issues
        return (xDist*xDist)+(yDist*yDist) <= (rSum*rSum)+1; // don't influence the "decision" parameter in 
    }                                                  // Bresenham's circle algorithm, preventing overlaps

    // Function for checking if proposed snowman will overlap any drawn or in
    // progress snowmen. If not the snowmen is added to the drawn list, and true
    // is returned to indicate that drawing can proceed.
    public synchronized boolean checkDrawable(BufferedImage img, SnowmanDetails s0, int colour) {
        for(SnowmanDetails s1 : drawn) {               // For each drawn or in progress snowman
            if(intersects(s0, s1))                     // if proposed snowman intersects a list entry
                return false;                          // indicate that the proposed snowman is undrawable
        }
        setDrawn(s0);                                  // If safe to draw, add it to the list
        return true;                                   // and indicate that it is safe to draw
    }

    // This function will draw a snowman of random size and orientation
    // The snowman is guaranteed to be within the bounds of the image
    public void drawRandomSnowman(BufferedImage img) {
        int x=0, y=0, size=0;                          // Assign default values so
        Orientation o = Orientation.up;                // compiler doesn't complain
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        int colour = rng.nextInt(0x00800000, 0x00ffffff); // Generate random visible colour
        colour = (colour|0xff000000);                  // Ensure colour is opaque
        boolean successfullyDrawn = false;             // Boolean for exiting loop
        while(!successfullyDrawn) {
            size = rng.nextInt(MIN_SIZE, maxSize);
            switch(size%4) {                           // Generate random direction using modulo arithmetic
                case 0:
                    o=Orientation.up;                  // Ensure rng co-ords prevent out of bounds errors
                    x = rng.nextInt(size, img.getWidth()-size);
                    y = rng.nextInt((int)(size+(2*size*SCALE)+(2*size*SCALE*SCALE)), img.getHeight()-size);
                    break;
                case 1:
                    o=Orientation.down;                // Ensure rng co-ords prevent out of bounds errors
                    x = rng.nextInt(size, img.getWidth()-size);
                    y = rng.nextInt(size, (int)(img.getHeight()-(size+(2*size*SCALE)+(2*size*SCALE*SCALE))));
                    break;
                case 2:
                    o=Orientation.left;                // Ensure rng co-ords prevent out of bounds errors
                    x = rng.nextInt((int)(size+(2*size*SCALE)+(2*size*SCALE*SCALE)), img.getWidth()-size);
                    y = rng.nextInt(size, img.getHeight()-size);
                    break;
                case 3:
                    o=Orientation.right;               // Ensure rng co-ords prevent out of bounds errors
                    x = rng.nextInt(size, (int)(img.getWidth()-(size+(2*size*SCALE)+(2*size*SCALE*SCALE))));
                    y = rng.nextInt(size, img.getHeight()-size);
                    break;
                default:
                    o=Orientation.up;                  // Default snowman is facing up, in the center of
                    x = img.getWidth()/2;              // the canvas
                    y = img.getHeight()/2;
                    break;
            }
            successfullyDrawn = checkDrawable(img, new SnowmanDetails(x, y, size, o), colour);
        }
        drawSnowman(img, new SnowmanDetails(x, y, size, o), colour, true); // Draw the snowman
    }

    // This function draws a snowman given the center and radius of the base, a colour,
    // an orientation and a BufferedImage to draw on
    public static void drawSnowman(BufferedImage img, SnowmanDetails s, int colour, boolean fill) {
        int x = s.getX(0);                           // Fetch base x value
        int y = s.getY(0);                           // Fetch base y value
        int r0 = s.getR(0);                          // Fetch base radius
        int r1 = s.getR(1);                          // Calculate secondary radius
        int r2 = s.getR(2);                          // Calculate tertiary radius
        drawCircle(img, x, y, r0, colour, fill);       // Draw the base circle
        switch (s.getO()) {
            case up:                                   // Draw secondary and tertiary circles above
                drawCircle(img, x, s.getY(1), r1, colour, fill);
                drawCircle(img, x, s.getY(2), r2, colour, fill);
                break;
            case down:                                 // Draw secondary and tertiary circles below
                drawCircle(img, x, s.getY(1), r1, colour, fill);
                drawCircle(img, x, s.getY(2), r2, colour, fill);
                break;
            case left:                                 // Draw secondary and tertiary circles on left
                drawCircle(img, s.getX(1), y, r1, colour, fill);
                drawCircle(img, s.getX(2), y, r2, colour, fill);
                break;
            case right:                                // Draw secondary and tertiary circles on right
                drawCircle(img, s.getX(1), y, r1, colour, fill);
                drawCircle(img, s.getX(2), y, r2, colour, fill);
                break;
            default:
                break;
        }
    }

    // This function draws a circle of radius r with centre (xc,yc), using
    // Bresenham's algorithm. The function itself is adapted from an example
    // I found on GeeksForGeeks.org
    public static void drawCircle(BufferedImage img, int xc, int yc, int r, int colour, boolean fill){
        int x = 0;                                     // Start position is (0,r)
        int y = r;                                     // The top of the circle
        int d = 3-(2*r);                               // With an appropriate decision var

        drawPixels(img, xc, yc, x, y, colour, fill);   // Draw the 8 initial pixels
        while(y>=x){                                   // While(in the top right octant)
            if(d>0) {                                  // Evaluate the decision param
                y--;                                   // Decrement the y co-ord if d indicates to do so
                d = d+4*(x-y)+10;                      // Update the decision param for next pixel
            }
            else {
                d = d+(4*x)+6;                         // Update the decision param for next pixel
            }
            x++;                                       // Shift focus to the desired pixel
            drawPixels(img, xc, yc, x, y, colour, fill); // Draw this pixel and its equivalent in the other octants
        }
    }

    // This function is a helpter function for the drawCircle() function. Its
    // purpose is to draw pixels iteratively and fill the inside of the circle
    public static void drawPixels(BufferedImage img, int xc, int yc, int x, int y, int colour, boolean fill){
        if(fill) {                                     // fill boolean controls the circle fill
            for(int i=-x; i<=x; i++) {
                img.setRGB(xc+i, yc+y, colour);        // Draw horizontal line from (-x,y) to (x,y)
                img.setRGB(xc+i, yc-y, colour);        // Draw horizontal line from (-x,-y) to (x,-y)
            }
            for(int i=-y; i<=y; i++) {
                img.setRGB(xc+i, yc+x, colour);        // Draw horizontal line from (-y,x) to (y,x)
                img.setRGB(xc+i, yc-x, colour);        // Draw horizontal line from (-y,-x) to (y,-x)
            }
        } else {
            img.setRGB(xc-x, yc+y, colour);            // Draw edge point (-x,y)
            img.setRGB(xc+x, yc+y, colour);            // Draw edge point (x,y)
            img.setRGB(xc-x, yc-y, colour);            // Draw edge point (-x,-y)
            img.setRGB(xc+x, yc-y, colour);            // Draw edge point (x,-y)
            img.setRGB(xc-y, yc+x, colour);            // Draw edge point (-y,x)
            img.setRGB(xc+y, yc+x, colour);            // Draw edge point (y,x)
            img.setRGB(xc-y, yc-x, colour);            // Draw edge point (-y,-x)
            img.setRGB(xc+y, yc-x, colour);            // Draw edge point (y,-x)
        }
    }
}