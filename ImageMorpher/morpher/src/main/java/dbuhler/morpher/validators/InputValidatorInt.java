package dbuhler.morpher.validators;

import android.content.Context;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

/**
 * This class provides a validator for integer values entered in a text view.
 *
 * @author  Dan Buhler
 * @version 2015-02-15
 */
public final class InputValidatorInt implements TextView.OnEditorActionListener
{
    private Context context;
    private int min;
    private int max;

    /**
     * Creates a new validator ensuring a value lies between min and max.
     *
     * @param context The calling activity's context.
     * @param min The minimum value.
     * @param max The maximum value.
     */
    public InputValidatorInt(Context context, int min, int max)
    {
        this.context = context;
        this.min     = min;
        this.max     = max;
    }

    /**
     * Called when an action is being performed.
     *
     * @param v        The view that was clicked.
     * @param actionId Identifier of the action.
     * @param event    The triggering event, or null.
     * @return True if you have consumed the action, else false.
     */
    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
    {
        if (actionId == EditorInfo.IME_ACTION_DONE)
        {
            int value = Integer.parseInt(v.getText().toString());

            value = Math.max(min, value);
            value = Math.min(max, value);
            v.setText(String.valueOf(value));
            v.clearFocus();

            ((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(v.getWindowToken(), 0);
            return true;
        }

        return false;
    }
}