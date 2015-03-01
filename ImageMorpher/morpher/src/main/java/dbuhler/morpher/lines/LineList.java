package dbuhler.morpher.lines;

import android.graphics.PointF;

import java.util.ArrayList;

/**
 * This class represents a searchable collection of lines.
 *
 * @author  Dan Buhler
 * @version 2015-02-15
 */
public final class LineList extends ArrayList<DrawableLine>
{
    /**
     * Creates a new empty line list.
     */
    public LineList()
    {
        super();
    }

    /**
     * Searches the collection for a line that can be grabbed from the given position p. If such a
     * line exists, it returns the one that is closest to the end of the list. Otherwise, it returns
     * null.
     *
     * @param p The position where the user wants to grab a line.
     * @return A line that can be grabbed from this position, or null.
     */
    public final DrawableLine find(PointF p)
    {
        for (int i = size(); i > 0; --i)
        {
            if (get(i - 1).grabAt(p))
            {
                return get(i - 1);
            }
        }

        return null;
    }
}