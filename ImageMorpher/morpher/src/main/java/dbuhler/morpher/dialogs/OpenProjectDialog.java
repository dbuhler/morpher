package dbuhler.morpher.dialogs;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Arrays;

import dbuhler.morpher.R;

/**
 * This class allows to create a dialog fragment that lists all the projects that are found in the
 * device's internal storage. When the user selects a project, the calling activity's onItemClick
 * listener is triggered.
 *
 * @author  Dan Buhler
 * @version 2015-02-15
 */
public final class OpenProjectDialog extends DialogFragment
{
    /**
     * The calling activity must implement this interface in order to be able to retrieve the name
     * of the project the user has selected.
     */
    public interface OnItemClickListener
    {
        /**
         * Called when the user selects a project from the OpenProjectDialog.
         *
         * @param projectName The name of the project that the user has selected.
         */
        void onItemClick(String projectName);
    }

    /**
     * Creates and returns a new dialog instance. The dialog's list view is populated with all the
     * project names found in the device's internal storage.
     *
     * @param savedInstanceState The last saved instance state of the fragment, or null if new.
     * @return A new dialog instance to be displayed by the fragment.
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        @SuppressLint("InflateParams")
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_open, null);

        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity())
                .setIcon(R.drawable.ic_action_open)
                .setTitle(R.string.dialogOpenTitle)
                .setView(view);

        String[] projectNames = getProjectNames();

        if (projectNames.length == 0)
        {
            // No projects found in the internal storage.
            dialog.setMessage(R.string.dialogOpenMessageEmpty);
        }
        else
        {
            initializeListView(view, projectNames);
            dialog.setMessage(R.string.dialogOpenMessage);
        }

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

    /**
     * Retrieves and returns a list of project names from the device's internal storage. The project
     * names correspond to the file names found in the application's default internal directory.
     *
     * @return A list of project names.
     */
    private String[] getProjectNames()
    {
        String[] projectNames = getActivity().getFilesDir().list();
        Arrays.sort(projectNames);
        return projectNames;
    }

    /**
     * Sets up the dialog's list view that contains the names of the available projects and links
     * the view's onItemClick listener with the interface's onItemClick listener, passing the name
     * of the selected project as an argument.
     *
     * @param view         The dialog's layout view.
     * @param projectNames The names to be displayed in the list.
     */
    private void initializeListView(View view, String[] projectNames)
    {
        ListView listView = (ListView) view;
        listView.setAdapter(new ArrayAdapter<>(getActivity(), R.layout.list_item, projectNames));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                if (getActivity() instanceof OnItemClickListener)
                {
                    String projectName = ((TextView) view).getText().toString();
                    ((OnItemClickListener) getActivity()).onItemClick(projectName);
                }
                dismiss();
            }
        });
    }
}