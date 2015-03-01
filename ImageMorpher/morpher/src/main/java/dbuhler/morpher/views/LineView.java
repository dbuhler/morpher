package dbuhler.morpher.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.AttributeSet;

import dbuhler.morpher.lines.DrawableLine;
import dbuhler.morpher.lines.Line;
import dbuhler.morpher.lines.LineList;
import dbuhler.morpher.lines.LineState;

/**
 * This is a modified ImageView that includes functionality to draw lines on it.
 *
 * @author  Dan Buhler
 * @version 2015-02-15
 */
public final class LineView extends FitImageView
{
    private DrawableLine selectedLine;
    private LineList     lines = new LineList();

    public LineView(Context context)
    {
        super(context);
    }

    public LineView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public LineView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
    }

    /**
     * Draws the lines onto the view's canvas. Uses COLOR_DEFAULT for lines stored in the lines list
     * and COLOR_SELECT for the currently selected line.
     *
     * @param canvas The view's canvas on which will be drawn.
     */
    @Override
    protected void onDraw(@NonNull Canvas canvas)
    {
        super.onDraw(canvas);

        for (DrawableLine line : lines)
        {
            if (line != selectedLine)
            {
                line.draw(canvas);
            }
        }

        if (selectedLine != null)
        {
            selectedLine.draw(canvas);
        }
    }

    /**
     * @return The number of lines in this view.
     */
    public int getNumLines()
    {
        return lines.size();
    }

    /**
     * @return The view's lines.
     */
    public Line[] getLines()
    {
        return lines.toArray(new Line[lines.size()]);
    }

    /**
     * Sets the view's lines from the given array of parcels.
     *
     * @param lines An array of parcels that contain lines.
     */
    public void setLines(Parcelable[] lines)
    {
        for (Parcelable line : lines)
        {
            this.lines.add(new DrawableLine((Line) line));
        }
    }

    /**
     * Returns the line with the given index from the view's lines.
     *
     * @param index The index of the line to return.
     * @return The line with the given index.
     */
    public DrawableLine getLine(int index)
    {
        return lines.get(index);
    }

    /**
     * Adds the given line to the lines that get drawn, but without adding it to the view's lines.
     * This is a line that is still being drawn and therefore only temporary.
     *
     * @param line A line that is being drawn.
     */
    public void drawLine(DrawableLine line)
    {
        line.setState(LineState.CREATE);
        selectedLine = line;
        invalidate();
    }

    /**
     * Adds the given line to the view's lines. This is a finished line that has been drawn and is
     * now selected.
     *
     * @param line A line that has been drawn.
     */
    public void addLine(DrawableLine line)
    {
        line.setState(LineState.SELECT);
        lines.add(line);
        selectedLine = line;
        invalidate();
    }

    /**
     * Removes the line at the given position from the view's lines.
     *
     * @param index The index of the element to be removed.
     */
    public void removeLine(int index)
    {
        selectedLine = null;
        lines.remove(index);
        invalidate();
    }

    /**
     * Marks the line at the given position in the view's lines as selected. This also changes the
     * line's state to SELECTED unless it already had a state different from DEFAULT.
     *
     * @param index The position of the line that is selected.
     */
    public void selectLine(int index)
    {
        DrawableLine line = lines.get(index);

        if (line.getState() == LineState.DEFAULT)
        {
            line.setState(LineState.SELECT);
        }

        selectedLine = line;
        invalidate();
    }

    /**
     * Clears the previous selection and sets the previously selected line's state to DEFAULT.
     */
    public void clearSelection()
    {
        if (selectedLine != null)
        {
            selectedLine.setState(LineState.DEFAULT);
            selectedLine = null;
            invalidate();
        }
    }

    /**
     * Searches the view's lines for a line that can be grabbed from the given position p. If such a
     * line exists, it returns its index. Otherwise, it returns -1.
     *
     * @param p The position where the user wants to grab a line.
     * @return The index of a line that can be grabbed, or -1.
     */
    public int findLine(PointF p)
    {
        DrawableLine line = lines.find(p);

        if (line != null)
        {
            return lines.indexOf(line);
        }

        return -1;
    }
}