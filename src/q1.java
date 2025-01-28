import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import java.util.Random;

public class q1 {

    // Parameters
    public static int t=4;
    public static int n=4;
    public static int width=4096;
    public static int height=4096;

    public static void main(String[] args) {
        try {
            // Create a blank image
            BufferedImage outputimage = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);

            // Instantiate the SnowmanThread class so we can reference its run() method below
            SnowmanThread snowmanThread = new SnowmanThread(outputimage, n/t);
            Thread[] threads = new Thread[t];
            for(int i=0; i<t; i++)                     // Create t threads using snowmanThread for the runnable
                threads[i] = new Thread(snowmanThread);
            for(Thread t : threads)                    // Start all the threads
                t.start();
            for(Thread t : threads)                    // Join all the threads
                t.join();

            // Write the image to a .png file
            File outputfile = new File("outputimage.png");
            ImageIO.write(outputimage, "png", outputfile);

        } catch (Exception e) {                        // Catch errors
            System.out.println("ERROR " +e);           // And print them to the console
            e.printStackTrace();                       // Also print the stack trace
        }
    }
}

class SnowmanThread implements Runnable {

    // Private vars
    private BufferedImage img;
    private int numOfSnowmen;
    private int maxSize;

    // Constants
    public static final int MIN_SIZE = 8;
    public static final double SCALE = 0.66;

    // Custom enum type for the orientation
    enum Orientation {up, down, left, right};

    public SnowmanThread(BufferedImage img, int numOfSnowmen) {
        super();                                       // Use default Runnable constructor
        this.img = img;                                // Initialise private img reference
        this.numOfSnowmen = numOfSnowmen;              // Initialise private numOfSnowmen

        // Calculate max possible size of snowman that could fit on the canvas and set maxSize
        // In hindsight this creates snowmen that are far too large and will lead to lots of
        // other threads repeatedly re-generating potential snowmen, but the math is satisfying
        int min = (img.getWidth() < img.getHeight() ? img.getWidth() : img.getHeight());
        this.maxSize = (int)(min/((2*SCALE*SCALE)+(2*SCALE)+(2)));
    }

    @Override
    public void run() {                                // Overriden run method defines what the thread will do
        for(int i=0; i<numOfSnowmen; i++)              // when .start() is called on a thread that was created
            drawRandomSnowman(this.img);               // using this runnable as its constuctor argument
    }

    // This function will draw a snowman of random size and orientation
    // The snowman is guaranteed to be within the bounds of the image
    public void drawRandomSnowman(BufferedImage img) {
        int x, y;
        Orientation o;
        Random rng = new Random();
        int size = rng.nextInt(MIN_SIZE, maxSize);
        switch(size%4) {                               // Generate random direction using modulo arithmetic
            case 0:
                o=Orientation.up;                      // Ensure rng co-ords prevent out of bounds errors
                x = rng.nextInt(size, img.getWidth()-size);
                y = rng.nextInt((int)(size+(2*size*SCALE)+(2*size*SCALE*SCALE)), img.getHeight()-size);
                break;
            case 1:
                o=Orientation.down;                    // Ensure rng co-ords prevent out of bounds errors
                x = rng.nextInt(size, img.getWidth()-size);
                y = rng.nextInt(size, (int)(img.getHeight()-(size+(2*size*SCALE)+(2*size*SCALE*SCALE))));
                break;
            case 2:
                o=Orientation.left;                    // Ensure rng co-ords prevent out of bounds errors
                x = rng.nextInt((int)(size+(2*size*SCALE)+(2*size*SCALE*SCALE)), img.getWidth()-size);
                y = rng.nextInt(size, img.getHeight()-size);
                break;
            case 3:
                o=Orientation.right;                   // Ensure rng co-ords prevent out of bounds errors
                x = rng.nextInt(size, (int)(img.getWidth()-(size+(2*size*SCALE)+(2*size*SCALE*SCALE))));
                y = rng.nextInt(size, img.getHeight()-size);
                break;
            default:
                o=Orientation.up;                      // Default snowman is facing up, in the center of
                x = img.getWidth()/2;                  // the canvas
                y = img.getHeight()/2;
                break;
        }
        int colour = rng.nextInt(0x00ffffff);    // Generate random colour
        colour = (colour|0xff000000);                  // Ensure colour is opaque
        drawSnowman(img, x, y, size, o, colour);       // Draw the snowman
    }

    // This function draws a snowman given the center and radius of the base, a colour,
    // an orientation and a BufferedImage to draw on
    public static void drawSnowman(BufferedImage img, int xc, int yc, int r0, Orientation o, int colour) {
        int r1 = (int)(r0*SCALE);                      // Calculate secondary radius
        int r2 = (int)(r1*SCALE);                      // Calculate tertiary radius
        drawCircle(img, xc, yc, r0, colour);           // Draw the base circle
        switch (o) {
            case up:                                   // Draw secondary and tertiary circles above
                drawCircle(img, xc, yc-(r0+r1), r1, colour);
                drawCircle(img, xc, yc-(r0+(2*r1)+r2), r2, colour);
                break;
            case down:                                 // Draw secondary and tertiary circles below
                drawCircle(img, xc, yc+(r0+r1), r1, colour);
                drawCircle(img, xc, yc+(r0+(2*r1)+r2), r2, colour);
                break;
            case left:                                 // Draw secondary and tertiary circles on left
                drawCircle(img, xc-(r0+r1), yc, r1, colour);
                drawCircle(img, xc-(r0+(2*r1)+r2), yc, r2, colour);
                break;
            case right:                                // Draw secondary and tertiary circles on right
                drawCircle(img, xc+(r0+r1), yc, r1, colour);
                drawCircle(img, xc+(r0+(2*r1)+r2), yc, r2, colour);
                break;
            default:
                break;
        }
    }

    // This function draws a circle of radius r with centre (xc,yc), using
    // Bresenham's algorithm. The function itself is adapted from an example
    // I found on GeeksForGeeks.org
    public static void drawCircle(BufferedImage img, int xc, int yc, int r, int colour){
        int x = 0;                                     // Start position is (0,r)
        int y = r;                                     // The top of the circle
        int d = 3-(2*r);                               // With an appropriate decision var

        drawPixels(img, xc, yc, x, y, colour);         // Draw the 8 initial pixels
        while(y>=x){                                   // While(in the top right octant)
            if(d>0) {                                  // Evaluate the decision param
                y--;                                   // Decrement the y co-ord if d indicates to do so
                d = d+4*(x-y)+10;                      // Update the decision param for next pixel
            }
            else {
                d = d+(4*x)+6;                         // Update the decision param for next pixel
            }
            x++;                                       // Shift focus to the desired pixel
            drawPixels(img, xc, yc, x, y, colour);     // Draw this pixel and its equivalent in the
        }                                              // 7 other octants
    }

    // This function is a helpter function for the drawCircle() function. Its
    // purpose is to draw pixels iteratively and fill the inside of the circle
    public static void drawPixels(BufferedImage img, int xc, int yc, int x, int y, int colour){
        for(int i=-x; i<=x; i++) {
            img.setRGB(xc+i, yc+y, colour);            // Draw horizontal line from (-x,y) to (x,y)
            img.setRGB(xc+i, yc-y, colour);            // Draw horizontal line from (-x,-y) to (x,-y)
        }
        for(int i=-y; i<=y; i++) {
            img.setRGB(xc+i, yc+x, colour);            // Draw horizontal line from (-y,x) to (y,x)
            img.setRGB(xc+i, yc-x, colour);            // Draw horizontal line from (-y,-x) to (y,-x)
        }
    }
}