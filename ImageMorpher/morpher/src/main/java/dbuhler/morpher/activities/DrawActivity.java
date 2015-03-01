package dbuhler.morpher.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import dbuhler.morpher.lines.DrawableLine;
import dbuhler.morpher.lines.Geometry;
import dbuhler.morpher.lines.LineAction;
import dbuhler.morpher.R;
import dbuhler.morpher.views.LineView;

/**
 * This activity lets the user draw lines on the images in order to specify how the content of the
 * two images relate to each other in terms of their shapes. Drawn lines can also be moved, changed
 * and deleted.
 *
 * @author  Dan Buhler
 * @version 2015-02-15
 */
public final class DrawActivity extends Activity implements OnTouchListener
{
    private static final float MIN_DRAW_RADIUS = 20.0f;

    private LineView   lineView1;
    private LineView   lineView2;
    private Uri        imageUri1;
    private Uri        imageUri2;
    private Menu       menu;
    private LineAction action;
    private PointF     initial;
    private PointF     current;
    private PointF     offset;
    private boolean    isDrawing;
    private int        index;

    /**
     * Called when the activity is starting. Inflates the activity's UI and initializes the line
     * views with the images (and lines if applicable) from the previous activity's intent.
     *
     * @param savedInstanceState Contains the data in onSaveInstanceState(Bundle) if applicable.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);

        initializeLineViews(getIntent());

        initial = new PointF();
        current = new PointF();
        offset  = new PointF();
        action  = LineAction.DRAW;
        index   = -1;
    }

    /**
     * Initialize the contents of the activity's options menu.
     *
     * @param menu The options menu of the activity.
     * @return True if the menu is to be displayed; false if it will not be shown.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_draw, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Prepare the activity's options menu to be displayed. This is called right before the menu is
     * shown, every time it is shown. Updates the state of the delete button as well as the tool
     * submenu to reflect changes made in onOptionsItemSelected().
     *
     * @param menu The options menu as last shown or first initialized by onCreateOptionsMenu().
     * @return True if the menu is to be displayed; false if it will not be shown.
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        MenuItem item = menu.findItem(R.id.actionDelete);

        // For some reason, using setVisibility() makes the other items change their width. Hence,
        // instead of making the button invisible, it is getting disabled and its icon replaced by
        // a blank, transparent image.
        if (index >= 0)
        {
            item.setEnabled(true);
            item.setIcon(R.drawable.ic_action_delete);
        }
        else
        {
            item.setEnabled(false);
            item.setIcon(R.drawable.ic_action_blank);
        }

        // Update tool submenu to represent current selection.
        switch (action)
        {
            case DRAW: checkItem(menu.findItem(R.id.actionToolDraw)); break;
            case EDIT: checkItem(menu.findItem(R.id.actionToolEdit)); break;
        }

        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * This hook is called whenever an item in the options menu is selected.
     *
     * @param item The menu item that was selected.
     * @return False to allow normal menu processing to proceed, true to consume it here.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.actionDelete:   deleteLines();            break;
            case R.id.actionToolDraw: action = LineAction.DRAW; break;
            case R.id.actionToolEdit: action = LineAction.EDIT; break;
            case R.id.actionDone:     finishDrawing();          break;
            default:                  return super.onOptionsItemSelected(item);
        }

        invalidateOptionsMenu();
        return true;
    }

    /**
     * Called when a touch event is dispatched to a view. Handles the user interaction with the
     * line views, i.e. drawing, selecting and editing lines.
     *
     * @param v The view the touch event has been dispatched to.
     * @param event The MotionEvent object containing full information about the event.
     * @return True if the listener has consumed the event, false otherwise.
     */
    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN: actionDown((LineView) v, event); break;
            case MotionEvent.ACTION_MOVE: actionMove((LineView) v, event); break;
            case MotionEvent.ACTION_UP:   actionUp();                      break;
            default:                      return false;
        }

        return true;
    }

    /**
     * Initializes the line views with their images and lines if applicable.
     *
     * @param intent The intent that holds the image URIs and (optionally) lines.
     */
    private void initializeLineViews(Intent intent)
    {
        lineView1 = (LineView) findViewById(R.id.lineView1);
        lineView2 = (LineView) findViewById(R.id.lineView2);

        lineView1.setOnTouchListener(this);
        lineView2.setOnTouchListener(this);

        imageUri1 = intent.getParcelableExtra("imageUri1");
        imageUri2 = intent.getParcelableExtra("imageUri2");

        if (intent.hasExtra("lines1") &&
            intent.hasExtra("lines2"))
        {
            lineView1.setLines(intent.getParcelableArrayExtra("lines1"));
            lineView2.setLines(intent.getParcelableArrayExtra("lines2"));
        }

        lineView1.setImageURI(imageUri1);
        lineView2.setImageURI(imageUri2);
    }

    /**
     * Checks the given item from the tool submenu and replaces the submenu's icon with the checked
     * item's icon
     *
     * @param item The item that is to be checked.
     */
    private void checkItem(MenuItem item)
    {
        item.setChecked(true);
        menu.findItem(R.id.actionTool).setIcon(item.getIcon());
    }

    /**
     * Handles the touch down event on a line view. This initializes and prepares a new action,
     * which includes clearing the current selection. If the EDIT action is active and the user
     * targeted a line, this line will be selected.
     *
     * @param v The view the touch event has been dispatched to.
     * @param event The MotionEvent object containing full information about the event.
     */
    private void actionDown(LineView v, MotionEvent event)
    {
        lineView1.clearSelection();
        lineView2.clearSelection();

        isDrawing = false;
        initial.x = current.x = event.getX();
        initial.y = current.y = event.getY();
        index     = -1;

        invalidateOptionsMenu();

        if (action == LineAction.EDIT)
        {
            selectLines(v);
        }
    }

    /**
     * Handles the touch move event on a line view. This updates the current coordinates and either
     * draws or edits a line, depending on the active line action.
     *
     * @param v The view the touch event has been dispatched to.
     * @param event The MotionEvent object containing full information about the event.
     */
    private void actionMove(LineView v, MotionEvent event)
    {
        // Update the current position but prevent it from going out of bounds.
        current.x = Math.min(Math.max(event.getX(), 0.0f), v.getWidth()  - 1);
        current.y = Math.min(Math.max(event.getY(), 0.0f), v.getHeight() - 1);

        switch (action)
        {
            case DRAW: drawLines(); break;
            case EDIT: editLine(v); break;
        }
    }

    /**
     * Handles the touch up event on a line view. This finishes either the drawing or editing of
     * a line, depending on what the user was doing.
     */
    private void actionUp()
    {
        switch (action)
        {
            case DRAW: addLines();     break;
            case EDIT: releaseLines(); break;
        }
    }

    /**
     * Displays the line the user is drawing on both line views. However, the user must have moved
     * at least MIN_DRAW_RADIUS before the drawing is registered.
     */
    private void drawLines()
    {
        if (!isDrawing && Geometry.distance(current, initial) > MIN_DRAW_RADIUS)
        {
            isDrawing = true;
        }

        if (isDrawing)
        {
            DrawableLine line = new DrawableLine(initial, current);
            lineView1.drawLine(line);
            lineView2.drawLine(line);
        }
    }

    /**
     * Finishes the line the user has drawn and adds it to both line views.
     */
    private void addLines()
    {
        if (isDrawing)
        {
            DrawableLine line1 = new DrawableLine(initial.x, initial.y, current.x, current.y);
            DrawableLine line2 = new DrawableLine(initial.x, initial.y, current.x, current.y);
            lineView1.addLine(line1);
            lineView2.addLine(line2);
            index = lineView1.getNumLines() - 1;
            invalidateOptionsMenu();
        }
    }

    /**
     * Selects the line the user targeted in the given line view as well as its counterpart in the
     * other line view.
     *
     * @param v The view the user is interacting with.
     */
    private void selectLines(LineView v)
    {
        index = v.findLine(current);

        if (index >= 0)
        {
            // Remember the offset from the line's midpoint.
            // This is needed in case the user wants to move the line.
            offset.set(
                    v.getLine(index).getMidpoint().x - initial.x,
                    v.getLine(index).getMidpoint().y - initial.y);
            lineView1.selectLine(index);
            lineView2.selectLine(index);
            invalidateOptionsMenu();
        }
    }

    /**
     * Moves the given view's line or one of its endpoints to the user's finger. Note that the
     * line's counterpart in the other view is not affected.
     *
     * @param v The view the user is interacting with.
     */
    private void editLine(LineView v)
    {
        if (index >= 0)
        {
            DrawableLine line = v.getLine(index);

            switch (line.getState())
            {
                case MOVE:    line.moveTo(current.x + offset.x, current.y + offset.y); break;
                case MOVE_P1: line.setP1(current); break;
                case MOVE_P2: line.setP2(current); break;
            }

            v.invalidate();
        }
    }

    /**
     * Finishes a line manipulation by resetting the selection and therefore setting both line's
     * state to SELECT. This has the effect that if the user lets go of a line, the visual effect
     * of the line being dragged disappears.
     */
    private void releaseLines()
    {
        if (index >= 0)
        {
            lineView1.clearSelection();
            lineView2.clearSelection();
            lineView1.selectLine(index);
            lineView2.selectLine(index);
        }
    }

    /**
     * Removes the selected line pair from the two line views.
     */
    private void deleteLines()
    {
        if (index >= 0)
        {
            lineView1.removeLine(index);
            lineView2.removeLine(index);

            // No selection, disable the delete button.
            index = -1;
            invalidateOptionsMenu();
        }
    }

    /**
     * Starts the next activity, passing on the image URIs and created line arrays.
     */
    private void finishDrawing()
    {
        Intent intent = new Intent(this, PlayActivity.class);
        intent.putExtra("imageUri1", imageUri1);
        intent.putExtra("imageUri2", imageUri2);
        intent.putExtra("lines1", lineView1.getLines());
        intent.putExtra("lines2", lineView2.getLines());
        startActivity(intent);
        finish();
    }
}