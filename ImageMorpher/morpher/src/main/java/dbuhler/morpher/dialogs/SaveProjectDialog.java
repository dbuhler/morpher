package dbuhler.morpher.dialogs;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import dbuhler.morpher.R;

/**
 * This class allows to create a dialog fragment that prompts the user for a project name. When the
 * user has entered a name and hits OK, the calling activity's onClick listener is triggered.
 *
 * @author  Dan Buhler
 * @version 2015-02-15
 */
public final class SaveProjectDialog extends DialogFragment
{
    /**
     * The calling activity must implement this interface in order to be able to retrieve the name
     * the user has entered.
     */
    public interface OnClickListener
    {
        /**
         * Called when the user selects OK in the SaveProjectDialog.
         *
         * @param projectName The project name the user has entered.
         */
        void onClick(String projectName);
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
        @SuppressLint("InflateParams")
        final View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_save, null);

        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity())
                .setIcon(R.drawable.ic_action_save)
                .setTitle(R.string.dialogSaveTitle)
                .setMessage(R.string.dialogSaveMessage)
                .setView(view);

        String projectName = getArguments().getString("projectName");

        if (projectName != null)
        {
            ((TextView) view).setText(projectName);
        }

        // Add an OK button.
        dialog.setPositiveButton(R.string.buttonOK, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                if (getActivity() instanceof OnClickListener)
                {
                    String projectName = ((TextView) view).getText().toString();
                    projectName = projectName.replaceAll("[^a-zA-Z0-9\\.-]+", "_");
                    ((OnClickListener) getActivity()).onClick(projectName);
                }
                dismiss();
            }
        });

        // Add a cancel button.
        dialog.setNegativeButton(R.string.buttonCancel, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.cancel();
            }
        });

        return dialog.create();
    }
}