import java.awt.image.*;
import java.io.*;
import java.util.Random;

import javax.imageio.*;

public class q1 {

    // Parameters
    public static int t;
    public static int n;
    public static int width=4096;
    public static int height=4096;

    // Constants
    public static final int RED=0xffff0000;
    public static final int WHITE=0xffffffff;
    public static final int MIN_SIZE=8;
    public static final int MAX_SIZE=400;
    public static final int NUM_OF_SNOWMEN=6;
    public static final double SCALE = 0.66;

    // My custom enum type for the orientation
    enum Orientation {up, down, left, right};

    public static void main(String[] args) {
        try {
            // once we know what size we want we can creat an empty image
            BufferedImage outputimage = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
            for(int i=0; i<NUM_OF_SNOWMEN; i++) {
                drawRandomSnowman(outputimage);
            }
            
            // Write out the image
            File outputfile = new File("outputimage.png");
            ImageIO.write(outputimage, "png", outputfile);

        } catch (Exception e) {
            System.out.println("ERROR " +e);
            e.printStackTrace();
        }
    }
    
    // This function will draw a snowman of random size and orientation
    // The snowman is guaranteed to be within the bounds of the image
    public static void drawRandomSnowman(BufferedImage img) {
        int x, y;
        Orientation o;
        Random rng = new Random();
        int size = rng.nextInt(MIN_SIZE, MAX_SIZE);
        switch(size%4) {                               // Generate random direction using modulo arithmetic
            case 0:
                o=Orientation.up;
                x = rng.nextInt(size, width-size);     // Ensure rng co-ords prevent out of bounds errors
                y = rng.nextInt((int)(size+(2*size*SCALE)+(2*size*SCALE*SCALE)), height-size);
                break;
            case 1:
                o=Orientation.down;
                x = rng.nextInt(size, width-size);     // Ensure rng co-ords prevent out of bounds errors
                y = rng.nextInt(size, (int)(height-(size+(2*size*SCALE)+(2*size*SCALE*SCALE))));
                break;
            case 2:
                o=Orientation.left;
                x = rng.nextInt((int)(size+(2*size*SCALE)+(2*size*SCALE*SCALE)), width-size);
                y = rng.nextInt(size, height-size);    // Ensure rng co-ords prevent out of bounds errors
                break;
            case 3:
                o=Orientation.right;
                x = rng.nextInt(size, (int)(width-(size+(2*size*SCALE)+(2*size*SCALE*SCALE))));
                y = rng.nextInt(size, height-size);    // Ensure rng co-ords prevent out of bounds errors
                break;
            default:
                o=Orientation.up;                      // Default snowman is facing up, in the center of
                x = width/2;                           // the canvas
                y = height/2;
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
        int x = 0, y = r;
        int d = 3 - 2 * r;

        drawPixels(img, xc, yc, x, y, colour);         // draw the 8 initial pixels
        while (y >= x){                                // while(in the top right octant)
            if (d > 0) {                               //   Evaluate the decision param
                y--;                                   //     Decrement the y co-ord if d indicates to do so
                d = d + 4 * (x - y) + 10;              //     Update the decision param for next pixel
            }
            else {
                d = d + 4 * x + 6;                     //     Update the decision param for next pixel
            }
            x++;                                       //   Shift focus to the desired pixel
            drawPixels(img, xc, yc, x, y, colour);     //   Draw this pixel and its equivalent in the
        }                                              //   7 other octants
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