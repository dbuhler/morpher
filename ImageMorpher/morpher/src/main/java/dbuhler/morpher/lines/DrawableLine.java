package dbuhler.morpher.lines;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;

/**
 * This class represents a line segment that can be drawn on a canvas.
 *
 * @author  Dan Buhler
 * @version 2015-02-15
 */
public final class DrawableLine extends Line
{
    private final static int   STANDARD_LINE_COLOR      = Color.BLACK;
    private final static int   SELECTED_LINE_COLOR      = Color.RED;
    private final static int   STANDARD_FILL_COLOR      = Color.WHITE;
    private final static int   STANDARD_LINE_THICKNESS  = 3;
    private final static int   SELECTED_LINE_THICKNESS  = 3;
    private final static float STANDARD_ENDPOINT_RADIUS = 10.0f;
    private final static float SELECTED_ENDPOINT_RADIUS = 20.0f;
    private final static float CIRCLE_CENTER_RADIUS     = 4.0f;
    private final static float LINE_GRAB_RADIUS         = 50.0f;

    private LineState state;

    /**
     * Creates a drawable line from (x1, y1) to (x2, y2).
     *
     * @param x1 The x-coordinate of the line segment's 1st endpoint.
     * @param y1 The y-coordinate of the line segment's 1st endpoint.
     * @param x2 The x-coordinate of the line segment's 2nd endpoint.
     * @param y2 The y-coordinate of the line segment's 2nd endpoint.
     */
    public DrawableLine(float x1, float y1, float x2, float y2)
    {
        super(x1, y1, x2, y2);
        state = LineState.DEFAULT;
    }

    /**
     * Creates a drawable line from p1 to p2.
     *
     * @param p1 The line segment's 1st endpoint.
     * @param p2 The line segment's 2nd endpoint.
     */
    public DrawableLine(PointF p1, PointF p2)
    {
        super(p1, p2);
        state = LineState.DEFAULT;
    }

    /**
     * Creates a drawable line from the given line object.
     *
     * @param line A line from which a drawable line is created.
     */
    public DrawableLine(Line line)
    {
        this(line.x1, line.y1, line.x2, line.y2);
    }

    /**
     * Returns the line's current state, which indicates how the line is currently used.
     *
     * @return The line's current state.
     */
    public final LineState getState()
    {
        return state;
    }

    /**
     * Sets the line's state to the given value.
     *
     * @param state The line's new state.
     */
    public final void setState(LineState state)
    {
        this.state = state;
    }

    /**
     * Checks and returns whether the given point is close enough to the line (<= LINE_GRAB_RADIUS)
     * to grab the line. If it is, the line's state is changed to reflect which part of the line is
     * grabbed (i.e. one of its endpoints or the entire line).
     *
     * @param p The position the user clicked on.
     * @return Whether the line is grabbed from position p.
     */
    public final boolean grabAt(PointF p)
    {
        PointF p1 = new PointF(x1, y1);
        PointF p2 = new PointF(x2, y2);

        if (Geometry.distance(p, p2) <= LINE_GRAB_RADIUS)
        {
            state = LineState.MOVE_P2;
            return true;
        }

        if (Geometry.distance(p, p1) <= LINE_GRAB_RADIUS)
        {
            state = LineState.MOVE_P1;
            return true;
        }

        if (Geometry.distance(p, this) <= LINE_GRAB_RADIUS)
        {
            state = LineState.MOVE;
            return true;
        }

        return false;
    }

    /**
     * Draws the line segment on the given canvas, using either SELECTED_LINE_COLOR if the line is
     * selected, or STANDARD_LINE_COLOR otherwise. If the line is not in CREATE state, its endpoints
     * are drawn as small circles.
     *
     * @param canvas The canvas on which is drawn.
     */
    public final void draw(Canvas canvas)
    {
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);

        if (state == LineState.DEFAULT)
        {
            paint.setStrokeWidth(STANDARD_LINE_THICKNESS);
            paint.setColor(STANDARD_LINE_COLOR);
        }
        else
        {
            paint.setStrokeWidth(SELECTED_LINE_THICKNESS);
            paint.setColor(SELECTED_LINE_COLOR);
        }

        canvas.drawLine(x1, y1, x2, y2, paint);

        if (state != LineState.CREATE)
        {
            drawEndpoints(canvas);
        }
    }

    /**
     * Draws the line segment's endpoints on the given canvas. If one of the line's endpoints is
     * being moved, this endpoint's circle is drawn larger.
     *
     * @param canvas The canvas on which is drawn.
     */
    private void drawEndpoints(Canvas canvas)
    {
        switch (state)
        {
            case MOVE_P1:
                drawCircle(canvas, x1, y1, SELECTED_ENDPOINT_RADIUS, false);
                drawCircle(canvas, x2, y2, STANDARD_ENDPOINT_RADIUS, true);
                break;

            case MOVE_P2:
                drawCircle(canvas, x1, y1, STANDARD_ENDPOINT_RADIUS, false);
                drawCircle(canvas, x2, y2, SELECTED_ENDPOINT_RADIUS, true);
                break;

            default:
                drawCircle(canvas, x1, y1, STANDARD_ENDPOINT_RADIUS, false);
                drawCircle(canvas, x2, y2, STANDARD_ENDPOINT_RADIUS, true);
        }
    }

    /**
     * Draws a circle on the given canvas with centre p and radius r. If drawCenter is true, the
     * circle's centre is drawn as a small disk.
     *
     * @param canvas The canvas on which is drawn.
     * @param x The x-coordinate of the circle's centre.
     * @param y The y-coordinate of the circle's centre.
     * @param r The circle's radius.
     * @param drawCenter Whether the circle's centre is drawn.
     */
    private void drawCircle(Canvas canvas, float x, float y, float r, boolean drawCenter)
    {
        Paint paint = new Paint();

        // Draw the circle's inside.
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(STANDARD_FILL_COLOR);
        canvas.drawCircle(x, y, r, paint);

        if (state == LineState.DEFAULT)
        {
            paint.setStrokeWidth(STANDARD_LINE_THICKNESS);
            paint.setColor(STANDARD_LINE_COLOR);
        }
        else
        {
            paint.setStrokeWidth(SELECTED_LINE_THICKNESS);
            paint.setColor(SELECTED_LINE_COLOR);
        }

        if (drawCenter)
        {
            canvas.drawCircle(x, y, CIRCLE_CENTER_RADIUS, paint);
        }

        // Draw the circle's outline.
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(x, y, r, paint);
    }
}