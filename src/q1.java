import java.awt.image.*;
import java.io.*;
import javax.imageio.*;

public class q1 {

    // Parameters
    public static int t;
    public static int n;
    public static int width=4096;
    public static int height=4096;

    // My own added test params, which will be variable in future
    public static int testRadius=300;
    public static int testX=width/2;
    public static int testY=height/2;
    public static final int RED=0xffff0000;
    public static final int WHITE=0xffffffff;
    enum Orientation {up, down, left, right};
    public static double scale = 0.66;

    public static void main(String[] args) {
        try {
            // once we know what size we want we can creat an empty image
            BufferedImage outputimage = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
            drawSnowman(outputimage, testX, testY, testRadius, Orientation.up, WHITE);

            // ------------------------------------
            // Your code would go here
            
            // The easiest mechanisms for getting and setting pixels are the
            // BufferedImage.setRGB(x,y,value) and getRGB(x,y) functions.
            // Consult the javadocs for other methods.

            // The getRGB/setRGB functions return/expect the pixel value in ARGB format, one byte per channel.  For example,
            //  int p = img.getRGB(x,y);
            // With the 32-bit pixel value you can extract individual colour channels by shifting and masking:
            //  int red = ((p>>16)&0xff);
            //  int green = ((p>>8)&0xff);
            //  int blue = (p&0xff);
            // If you want the alpha channel value it's stored in the uppermost 8 bits of the 32-bit pixel value
            //  int alpha = ((p>>24)&0xff);
            // Note that an alpha of 0 is transparent, and an alpha of 0xff is fully opaque.
            
            // ------------------------------------
            
            // Write out the image
            File outputfile = new File("outputimage.png");
            ImageIO.write(outputimage, "png", outputfile);

        } catch (Exception e) {
            System.out.println("ERROR " +e);
            e.printStackTrace();
        }
    }
    
    public static void drawSnowman(BufferedImage img, int xc, int yc, int r0, Orientation o, int colour) {
        int r1 = (int)(r0*scale);
        int r2 = (int)(r1*scale);
        drawCircle(img, xc, yc, r0, colour);
        switch (o) {
            case up:
                drawCircle(img, xc, yc-(r0+r1), r1, colour);
                drawCircle(img, xc, yc-(r0+(2*r1)+r2), r2, colour);
                break;
            case down:
                drawCircle(img, xc, yc+(r0+r1), r1, colour);
                drawCircle(img, xc, yc+(r0+(2*r1)+r2), r2, colour);
                break;
            case left:
                drawCircle(img, xc-(r0+r1), yc, r1, colour);
                drawCircle(img, xc-(r0+(2*r1)+r2), yc, r2, colour);
                break;
            case right:
                drawCircle(img, xc+(r0+r1), yc, r1, colour);
                drawCircle(img, xc+(r0+(2*r1)+r2), yc, r2, colour);
                break;
            default:
                break;
        }
    }

    // This function draws a circle of radius r with centre (xc,yc), using
    // Bresenham's algorithm. The fucntion itself is adapted from an example
    // I found on GeeksForGeeks.org
    public static void drawCircle(BufferedImage img, int xc, int yc, int r, int colour){
        int x = 0, y = r;
        int d = 3 - 2 * r;

        drawPixels(img, xc, yc, x, y, colour);
        while (y >= x){
            // update decision param and y coord
            if (d > 0) {
                y--; 
                d = d + 4 * (x - y) + 10;
            }
            else {
                d = d + 4 * x + 6;
            }
            x++;
            drawPixels(img, xc, yc, x, y, colour);
        }
    }

    public static void drawPixels(BufferedImage img, int xc, int yc, int x, int y, int colour){
        for(int i=-x; i<=x; i++) {
            img.setRGB(xc+i, yc+y, colour);
            img.setRGB(xc+i, yc-y, colour);
        }
        for(int i=-y; i<=y; i++) {
            img.setRGB(xc+i, yc+x, colour);
            img.setRGB(xc+i, yc-x, colour);
        }
    }
}