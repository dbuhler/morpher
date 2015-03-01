package dbuhler.morpher.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import dbuhler.morpher.R;

/**
 * This class allows to create a dialog fragment that lets the user choose between keeping the left
 * or the right image's aspect ratio.
 *
 * @author  Dan Buhler
 * @version 2015-02-15
 */
public final class AspectRatioDialog extends DialogFragment
{
    public static final int BUTTON_LEFT  = DialogInterface.BUTTON_NEGATIVE;
    public static final int BUTTON_RIGHT = DialogInterface.BUTTON_POSITIVE;

    /**
     * The calling activity must implement this interface in order to be able to listen to the
     * button click event.
     */
    public interface OnClickListener
    {
        /**
         * Called when a button in the AspectRatioDialog is clicked.
         *
         * @param which The button that was clicked.
         */
        void onClick(int which);
    }

    /**
     * Creates and returns a new dialog instance.
     *
     * @param savedInstanceState The last saved instance state of the fragment, or null if new.
     * @return A new dialog instance to be displayed by the fragment.
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                if (getActivity() instanceof OnClickListener)
                {
                    ((OnClickListener) getActivity()).onClick(which);
                }
            }
        };

        return new AlertDialog.Builder(getActivity())
                .setIcon(R.drawable.ic_warning)
                .setTitle(R.string.dialogAspectTitle)
                .setMessage(R.string.dialogAspectMessage)
                .setNegativeButton(R.string.dialogAspectButtonL, onClickListener)
                .setPositiveButton(R.string.dialogAspectButtonR, onClickListener)
                .create();
    }
}