package dbuhler.morpher.lines;

import android.graphics.PointF;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * This class represents a line segment in 2D defined by two points.
 *
 * @author  Dan Buhler
 * @version 2015-02-15
 */
public class Line implements Parcelable, Serializable
{
    public float x1;
    public float y1;
    public float x2;
    public float y2;

    /**
     * Creates a line segment from (x1, y1) to (x2, y2).
     *
     * @param x1 The x-coordinate of the line segment's 1st endpoint.
     * @param y1 The y-coordinate of the line segment's 1st endpoint.
     * @param x2 The x-coordinate of the line segment's 2nd endpoint.
     * @param y2 The y-coordinate of the line segment's 2nd endpoint.
     */
    public Line(float x1, float y1, float x2, float y2)
    {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    /**
     * Creates a line segment from p1 to p2.
     *
     * @param p1 The line segment's 1st endpoint.
     * @param p2 The line segment's 2nd endpoint.
     */
    public Line(PointF p1, PointF p2)
    {
        this(p1.x, p1.y, p2.x, p2.y);
    }

    /**
     * Creates a line segment from the given parcel.
     *
     * @param in The parcel that contains the line segment's coordinates.
     */
    private Line(Parcel in)
    {
        x1 = in.readFloat();
        y1 = in.readFloat();
        x2 = in.readFloat();
        y2 = in.readFloat();
    }

    /**
     * Required for the line segment to be parcelable.
     */
    public static final Parcelable.Creator<Line> CREATOR = new Parcelable.Creator<Line>()
    {
        @Override
        public Line createFromParcel(Parcel source)
        {
            return new Line(source);
        }

        @Override
        public Line[] newArray(int size)
        {
            return new Line[size];
        }
    };

    /**
     * Writes this line segment to the specified parcel.
     *
     * @param out   The parcel to write the line segment's coordinates into.
     * @param flags Additional flags about how the line segment should be written.
     */
    @Override
    public final void writeToParcel(Parcel out, int flags)
    {
        out.writeFloat(x1);
        out.writeFloat(y1);
        out.writeFloat(x2);
        out.writeFloat(y2);
    }

    /**
     * Required for the line segment to be parcelable.
     *
     * @return Always 0.
     */
    @Override
    public final int describeContents()
    {
        return 0;
    }

    /**
     * Sets the line segment's 1st endpoint to p.
     *
     * @param p The line segment's new 1st endpoint.
     */
    public final void setP1(PointF p)
    {
        x1 = p.x;
        y1 = p.y;
    }

    /**
     * Sets the line segment's 2nd endpoint to p.
     *
     * @param p The line segment's new 2nd endpoint.
     */
    public final void setP2(PointF p)
    {
        x2 = p.x;
        y2 = p.y;
    }

    /**
     * Calculates and returns the line segment's midpoint, i.e. the midpoint of its endpoints.
     *
     * @return The line segment's midpoint.
     */
    public final PointF getMidpoint()
    {
        return new PointF((x1 + x2) / 2, (y1 + y2) / 2);
    }

    /**
     * Translate the line such that the given coordinates become the line segment's new midpoint.
     *
     * @param x The x-coordinate of the line segment's midpoint after the translation.
     * @param y The y-coordinate of the line segment's midpoint after the translation.
     */
    public final void moveTo(float x, float y)
    {
        PointF m = getMidpoint();

        float dx = x - m.x;
        float dy = y - m.y;

        x1 += dx;
        y1 += dy;
        x2 += dx;
        y2 += dy;
    }
}