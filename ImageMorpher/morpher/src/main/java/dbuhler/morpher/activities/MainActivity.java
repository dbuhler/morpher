package dbuhler.morpher.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import dbuhler.morpher.R;
import dbuhler.morpher.dialogs.OpenProjectDialog;

/**
 * This is the application's main activity. It displays the main menu, which allows to start a new
 * project or to load an existing project.
 *
 * @author  Dan Buhler
 * @version 2015-02-15
 */
public final class MainActivity extends Activity implements OpenProjectDialog.OnItemClickListener
{
    /**
     * Called when the activity is starting. Inflates the activity's UI.
     *
     * @param savedInstanceState Contains the data in onSaveInstanceState(Bundle) if applicable.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /**
     * Called when the user selects a project from the OpenProjectDialog. Opens this project in
     * another activity.
     *
     * @param projectName The name of the project that the user has selected.
     */
    @Override
    public void onItemClick(String projectName)
    {
        Intent intent = new Intent(this, PlayActivity.class);
        intent.putExtra("projectName", projectName);
        startActivity(intent);
    }

    /**
     * Starts the activity for a new project.
     *
     * @param view The view that triggered this event.
     */
    public void buttonNew_Click(View view)
    {
        startActivity(new Intent(this, NewActivity.class));
    }

    /**
     * Displays a dialog for choosing an existing project.
     *
     * @param view The view that triggered this event.
     */
    public void buttonOpen_Click(View view)
    {
        new OpenProjectDialog().show(getFragmentManager(), "OpenProjectDialog");
    }

    /**
     * Closes this activity and thus the application.
     *
     * @param view The view that triggered this event.
     */
    public void buttonExit_Click(View view)
    {
        finish();
    }
}