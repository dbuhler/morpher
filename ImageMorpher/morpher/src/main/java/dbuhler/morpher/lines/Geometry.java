package dbuhler.morpher.lines;

import android.graphics.PointF;

/**
 * This class provides some geometric helper functions.
 *
 * @author  Dan Buhler
 * @version 2015-02-15
 */
public final class Geometry
{
    /**
     * Calculates and returns the squared euclidean distance between the given two points.
     *
     * @param x1 The x-coordinate of the 1st point.
     * @param y1 The y-coordinate of the 1st point.
     * @param x2 The x-coordinate of the 2nd point.
     * @param y2 The y-coordinate of the 2nd point.
     * @return The squared euclidean distance between (x1, y1) and (x2, y2).
     */
    public static float distanceSquared(float x1, float y1, float x2, float y2)
    {
        return (x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1);
    }

    /**
     * Calculates and returns the euclidean distance between the given two points.
     *
     * @param x1 The x-coordinate of the 1st point.
     * @param y1 The y-coordinate of the 1st point.
     * @param x2 The x-coordinate of the 2nd point.
     * @param y2 The y-coordinate of the 2nd point.
     * @return The euclidean distance between (x1, y1) and (x2, y2).
     */
    public static float distance(float x1, float y1, float x2, float y2)
    {
        return (float) Math.sqrt(distanceSquared(x1, y1, x2, y2));
    }

    /**
     * Calculates and returns the euclidean distance between the given two points.
     *
     * @param p1 The 1st point.
     * @param p2 The 2nd point.
     * @return The euclidean distance between p1 and p2.
     */
    public static float distance(PointF p1, PointF p2)
    {
        return distance(p1.x, p1.y, p2.x, p2.y);
    }

    /**
     * Calculates and returns the distance from the line segment to the given point. Note that if a
     * point lies past a line segment's endpoint, the distance to the line segment is equal to the
     * distance to this endpoint.
     *
     * @param p The point to which the distance is calculated.
     * @return The distance between the line segment and the point p.
     */
    public static float distance(PointF p, Line line)
    {
        if (line.x1 == line.x2 && line.y1 == line.y2)
        {
            return Geometry.distance(p.x, p.y, line.x1, line.y1);
        }

        float t = ((p.x - line.x1) * (line.x2 - line.x1) + (p.y - line.y1) * (line.y2 - line.y1))
                / ((line.x2 - line.x1) * (line.x2 - line.x1) + (line.y2 - line.y1) * (line.y2 - line.y1));

        if (t <= 0.0f)
        {
            return Geometry.distance(p.x, p.y, line.x1, line.y1);
        }

        if (t >= 1.0f)
        {
            return Geometry.distance(p.x, p.y, line.x2, line.y2);
        }

        float x = line.x1 + t * (line.x2 - line.x1);
        float y = line.y1 + t * (line.y2 - line.y1);

        return Geometry.distance(p.x, p.y, x, y);
    }
}