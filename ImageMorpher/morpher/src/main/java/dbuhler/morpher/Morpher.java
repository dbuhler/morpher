package dbuhler.morpher;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore.Images.Media;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import dbuhler.morpher.lines.Line;

/**
 * This class handles the calculations for the morphing and creating of the frames. It owns all the
 * data needed for morphing and allows to save and load a morphing project.
 *
 * @author  Dan Buhler
 * @version 2015-02-15
 */
public final class Morpher
{
    public static final int   DEFAULT_NUM_FRAMES = 20;
    public static final float DEFAULT_PARAM_A    = 0.01f;
    public static final float DEFAULT_PARAM_B    = 2.0f;
    public static final float DEFAULT_PARAM_P    = 0.0f;
    public static final int   MIN_NUM_FRAMES     = 1;
    public static final int   MAX_NUM_FRAMES     = 100;
    public static final float MIN_PARAM_A        = 0.001f;
    public static final float MAX_PARAM_A        = 0.1f;
    public static final float MIN_PARAM_B        = 0.5f;
    public static final float MAX_PARAM_B        = 2.0f;
    public static final float MIN_PARAM_P        = 0.0f;
    public static final float MAX_PARAM_P        = 1.0f;

    private Context  context;
    private Uri      imageUri1;
    private Uri      imageUri2;
    private Bitmap   image1;
    private Bitmap   image2;
    private Bitmap[] frames;
    private Line[]   lines1;
    private Line[]   lines2;
    private int      sizeX;
    private int      sizeY;
    private int      numLines;
    private int[]    colors1;
    private int[]    colors2;

    /**
     * Creates a new morpher and initializes its data.
     *
     * @param context The calling activity's context.
     * @param imageUri1 The 1st image's URI.
     * @param imageUri2 The 2nd image's URI.
     * @param lines1 The 1st image's lines.
     * @param lines2 The 2nd image's lines.
     */
    public Morpher(Context context, Uri imageUri1, Uri imageUri2, Line[] lines1, Line[] lines2)
    {
        this.context   = context;
        this.imageUri1 = imageUri1;
        this.imageUri2 = imageUri2;
        this.lines1    = lines1;
        this.lines2    = lines2;

        image1 = createBitmapFromUri(imageUri1);
        image2 = createBitmapFromUri(imageUri2);
        frames = new Bitmap[2];
        frames[0] = image1;
        frames[1] = image2;

        initialize();
    }

    /**
     * Creates a new morpher from an existing project file.
     *
     * @param context The calling activity's context.
     * @param fileName The project's file name.
     */
    public Morpher(Context context, String fileName)
    {
        this.context = context;
        loadFromFile(fileName);
        initialize();
    }

    /**
     * Initializes the morpher's working data.
     */
    private void initialize()
    {
        sizeX    = image1.getWidth();
        sizeY    = image1.getHeight();
        numLines = lines1.length;
        colors1  = new int[sizeX * sizeY];
        colors2  = new int[sizeX * sizeY];
        image1.getPixels(colors1, 0, sizeX, 0, 0, sizeX, sizeY);
        image2.getPixels(colors2, 0, sizeX, 0, 0, sizeX, sizeY);
    }

    /**
     * @return The 1st image's URI.
     */
    public Uri getImageUri1()
    {
        return imageUri1;
    }

    /**
     * @return The 2nd image's URI.
     */
    public Uri getImageUri2()
    {
        return imageUri2;
    }

    /**
     * @return The 1st image's lines.
     */
    public Line[] getLines1()
    {
        return lines1;
    }

    /**
     * @return The 2nd image's lines.
     */
    public Line[] getLines2()
    {
        return lines2;
    }

    /**
     * @return The number of frames (including the two original images).
     */
    public int getNumFrames()
    {
        if (frames == null)
        {
            return 0;
        }

        return frames.length;
    }

    /**
     * @param index The index of a frame.
     * @return The bitmap of the frame with the given index.
     */
    public final Bitmap getFrame(int index)
    {
        return frames[index];
    }

    /**
     * Calculates and creates the frames for this morpher project. A progress bar is shown while the
     * frames are being created. The parameters a, b and p are used for the weight calculation.
     *
     * @param n The number of steps, i.e. total number of frames - 1.
     * @param a The morphing parameter a.
     * @param b The morphing parameter b.
     * @param p The morphing parameter p.
     */
    public final void createFrames(final int n, final float a, final float b, final float p)
    {
        final ProgressDialog dialog = new ProgressDialog(context);
        dialog.setCancelable(false);
        dialog.setTitle(R.string.dialogWaitMorphTitle);
        dialog.setMessage(context.getResources().getString(R.string.dialogWaitMorphMessage));
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setProgress(0);
        dialog.setMax(n - 1);
        dialog.show();

        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                frames    = new Bitmap[n + 1];
                frames[0] = image1;
                frames[n] = image2;

                final Line[][] lines = getLines(n);

                for (int k = 1; k < n; ++k)
                {
                    frames[k] = createFrame(lines, k, n, a, b, p);
                    dialog.incrementProgressBy(1);
                }
                dialog.dismiss();
            }
        }).start();
    }

    /**
     * Writes all relevant project data into a file with the given name, creates a folder of the
     * same name and stores all frames as bitmaps in this folder.
     *
     * @param fileName The file name for the project.
     */
    public void saveToFile(String fileName)
    {
        try
        {
            FileOutputStream fileOut = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            File dir = context.getDir(fileName, Context.MODE_PRIVATE);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);

            objectOut.writeInt(numLines);

            for (int i = 0; i < numLines; ++i)
            {
                objectOut.writeObject(lines1[i]);
                objectOut.writeObject(lines2[i]);
            }

            objectOut.writeInt(frames.length);

            for (int k = 0; k < frames.length; ++k)
            {
                FileOutputStream bitmapOut = new FileOutputStream(new File(dir, String.valueOf(k)));
                frames[k].compress(Bitmap.CompressFormat.PNG, 100, bitmapOut);
                bitmapOut.close();
            }

            objectOut.close();
            fileOut.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Loads all relevant project data from the file with the given name, and also loads the frames
     * from the folder with the same name if it exists.
     *
     * @param fileName The file name for the project.
     */
    private void loadFromFile(String fileName)
    {
        try
        {
            FileInputStream fileIn = context.openFileInput(fileName);
            ObjectInputStream objectIn = new ObjectInputStream(fileIn);

            numLines = objectIn.readInt();
            lines1   = new Line[numLines];
            lines2   = new Line[numLines];

            for (int i = 0; i < numLines; ++i)
            {
                lines1[i] = (Line) objectIn.readObject();
                lines2[i] = (Line) objectIn.readObject();
            }

            int numFrames = objectIn.readInt();
            frames = new Bitmap[numFrames];

            File dir = context.getDir(fileName, Context.MODE_PRIVATE);

            imageUri1 = Uri.fromFile(new File(dir, String.valueOf(0)));
            imageUri2 = Uri.fromFile(new File(dir, String.valueOf(numFrames - 1)));

            for (int k = 0; k < frames.length; ++k)
            {
                frames[k] = Media.getBitmap(context.getContentResolver(),
                                            Uri.fromFile(new File(dir, String.valueOf(k))));
            }

            objectIn.close();
            fileIn.close();

            image1 = frames[0];
            image2 = frames[numFrames - 1];
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Calculates and returns an array of line arrays for each frame, where the lines are linearly
     * interpolated between the original images.
     *
     * @param n The number of steps, i.e. total number of frames - 1.
     * @return An array of line arrays for each frame.
     */
    private Line[][] getLines(int n)
    {
        Line[][] lines = new Line[n + 1][numLines];

        for (int i = 0; i < numLines; ++i)
        {
            Line line1 = lines1[i];
            Line line2 = lines2[i];

            lines[0][i] = new Line(line1.x1, line1.y1, line1.x2, line1.y2);
            lines[n][i] = new Line(line2.x1, line2.y1, line2.x2, line2.y2);

            for (int k = 1; k < n; ++k)
            {
                float t = 1.0f * k / n;

                lines[k][i] = new Line(
                        line1.x1 + t * (line2.x1 - line1.x1),
                        line1.y1 + t * (line2.y1 - line1.y1),
                        line1.x2 + t * (line2.x2 - line1.x2),
                        line1.y2 + t * (line2.y2 - line1.y2));
            }
        }

        return lines;
    }

    /**
     * Creates the k-th frame with the given lines and morphing parameters a, b, and p.
     *
     * @param lines The line array for the frame to be created.
     * @param k     The index of the frame to be created.
     * @param n     The number of steps, i.e. total number of frames - 1.
     * @param a     The morphing parameter a.
     * @param b     The morphing parameter b.
     * @param p     The morphing parameter p.
     * @return The created frame as a bitmap.
     */
    private Bitmap createFrame(final Line[][] lines, final int k, final int n,
                                     final float a, final float b, final float p)
    {
        final int[][] colors = new int[2][];

        // Create the forward warping image.
        Thread thread1 = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                colors[0] = createColors(colors1, lines[0], lines[k], a, b, p);
            }
        });

        // Create the backward warping image.
        Thread thread2 = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                colors[1] = createColors(colors2, lines[n], lines[k], a, b, p);
            }
        });

        thread1.start();
        thread2.start();

        try
        {
            thread1.join();
            thread2.join();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }

        int[] newColors = crossDissolve(colors[0], colors[1], 1.0f * k / n);
        return Bitmap.createBitmap(newColors, 0, sizeX, sizeX, sizeY, Bitmap.Config.ARGB_8888);
    }

    /**
     * Creates the warped image from the given image, using the parameters a, b and p for
     * calculating the line weights.
     *
     * @param srcColors The source image for warping.
     * @param srcLines  The source lines for warping.
     * @param dstLines  The destination lines for warping.
     * @param a         The morphing parameter a.
     * @param b         The morphing parameter b.
     * @param p         The morphing parameter p.
     * @return The colour array of the destination image.
     */
    private int[] createColors(int[] srcColors, Line[] srcLines, Line[] dstLines,
                               float a, float b, float p)
    {
        int[] dstColors = new int[sizeX * sizeY];

        for (int x = 0; x < sizeX; ++x)
        {
            for (int y = 0; y < sizeY; ++y)
            {
                float deltaX      = 0.0f;
                float deltaY      = 0.0f;
                float totalWeight = 0.0f;

                for (int i = 0; i < numLines; ++i)
                {
                    float srcPX = srcLines[i].x1;
                    float srcPY = srcLines[i].y1;
                    float srcQX = srcLines[i].x2;
                    float srcQY = srcLines[i].y2;

                    float dstPX = dstLines[i].x1;
                    float dstPY = dstLines[i].y1;
                    float dstQX = dstLines[i].x2;
                    float dstQY = dstLines[i].y2;

                    float lengthSquared = (dstQX - dstPX) * (dstQX - dstPX)
                                        + (dstQY - dstPY) * (dstQY - dstPY);

                    float d = ((x - dstPX) * (dstPY - dstQY) + (y - dstPY) * (dstQX - dstPX))
                            / (float) Math.sqrt(lengthSquared);
                    float t = ((x - dstPX) * (dstQX - dstPX) + (y - dstPY) * (dstQY - dstPY))
                            / lengthSquared;

                    float length = (float) Math.sqrt((srcQX - srcPX) * (srcQX - srcPX)
                                                   + (srcQY - srcPY) * (srcQY - srcPY));

                    float srcX = srcPX + t * (srcQX - srcPX) + d * (srcPY - srcQY) / length;
                    float srcY = srcPY + t * (srcQY - srcPY) + d * (srcQX - srcPX) / length;

                    if (t < 0.0f)
                    {
                        d = (float) Math.sqrt((x - srcPX) * (x - srcPX)
                                            + (y - srcPY) * (y - srcPY));
                    }
                    else if (t > 1.0f)
                    {
                        d = (float) Math.sqrt((x - srcQX) * (x - srcQX)
                                            + (y - srcQY) * (y - srcQY));
                    }

                    float weight = getWeight(d, length, a, b, p);

                    deltaX      += weight * (srcX - x);
                    deltaY      += weight * (srcY - y);
                    totalWeight += weight;
                }

                deltaX /= totalWeight;
                deltaY /= totalWeight;

                int intSrcX = x + (int) deltaX;
                int intSrcY = y + (int) deltaY;

                // Avoid out of bounds values.
                if (intSrcX < 0) intSrcX = 0; else if (intSrcX >= sizeX) intSrcX = sizeX - 1;
                if (intSrcY < 0) intSrcY = 0; else if (intSrcY >= sizeY) intSrcY = sizeY - 1;

                dstColors[x + sizeX * y] = srcColors[intSrcX + sizeX * intSrcY];
            }
        }

        return dstColors;
    }

    /**
     * Calculates and returns the weight of a point with respect to a line, using their distance,
     * the line's length, and the parameters a, b, and p.
     *
     * @param d      The distance between point and line.
     * @param length The line's length.
     * @param a      The morphing parameter a.
     * @param b      The morphing parameter b.
     * @param p      The morphing parameter p.
     * @return The weight for a point with the given parameters.
     */
    private float getWeight(float d, float length, float a, float b, float p)
    {
        if (p == 0.0f)
        {
            if (b == 1.0f)
            {
                return 1.0f / (d + a);
            }

            if (b == 2.0f)
            {
                return 1.0f / ((d + a) * (d + a));
            }

            return 1.0f / (float) Math.pow((d + a), b);
        }

        return (float) Math.pow(Math.pow(length, p) / (d + a), b);
    }

    /**
     * Cross-dissolves the given colour arrays with the given ratio and returns the result.
     *
     * @param colors1 The colour array of the 1st image.
     * @param colors2 The colour array of the 2nd image.
     * @param ratio   The percentage of the 1st image.
     * @return The colour array of the cross-dissolved result.
     */
    private int[] crossDissolve(int[] colors1, int[] colors2, float ratio)
    {
        int[] colors = new int[sizeX * sizeY];

        for (int i = 0; i < colors.length; ++i)
        {
            int r1 = Color.red(colors1[i]);
            int r2 = Color.red(colors2[i]);
            int g1 = Color.green(colors1[i]);
            int g2 = Color.green(colors2[i]);
            int b1 = Color.blue(colors1[i]);
            int b2 = Color.blue(colors2[i]);

            colors[i] = Color.rgb(
                    r1 + (int) (ratio * (r2 - r1)),
                    g1 + (int) (ratio * (g2 - g1)),
                    b1 + (int) (ratio * (b2 - b1)));
        }

        return colors;
    }

    /**
     * Creates and returns a bitmap from the given URI.
     *
     * @param uri The URI of an image.
     * @return The created bitmap.
     */
    private Bitmap createBitmapFromUri(Uri uri)
    {
        try
        {
            return Media.getBitmap(context.getContentResolver(), uri);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }
}