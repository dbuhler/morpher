package dbuhler.morpher.lines;

/**
 * This enum lists the different states a line can be in, indicating how a line is currently used.
 *
 * @author  Dan Buhler
 * @version 2015-02-15
 */
public enum LineState
{
    DEFAULT,
    CREATE,
    SELECT,
    MOVE,
    MOVE_P1,
    MOVE_P2
}