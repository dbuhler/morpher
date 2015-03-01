package dbuhler.morpher.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import dbuhler.morpher.validators.InputValidatorFloat;
import dbuhler.morpher.validators.InputValidatorInt;
import dbuhler.morpher.Morpher;
import dbuhler.morpher.Player;
import dbuhler.morpher.R;
import dbuhler.morpher.dialogs.OpenProjectDialog;
import dbuhler.morpher.dialogs.SaveProjectDialog;
import dbuhler.morpher.dialogs.SpinnerWaitDialog;
import dbuhler.morpher.lines.Line;

/**
 * This activity lets the user morph the images and manage their morpher project.
 *
 * @author  Dan Buhler
 * @version 2015-02-15
 */
public final class PlayActivity extends Activity implements OpenProjectDialog.OnItemClickListener,
                                                            SaveProjectDialog.OnClickListener,
                                                            SpinnerWaitDialog.OnNotifyListener
{
    private static final int SAVE_REQUEST = 0;
    private static final int LOAD_REQUEST = 1;

    private ImageView   imageView;
    private EditText    editNumFrames;
    private EditText    editFrameRate;
    private EditText    editParamA;
    private EditText    editParamB;
    private EditText    editParamP;
    private ImageButton buttonPlayFw;
    private ImageButton buttonPlayBw;
    private Morpher     morpher;
    private Player      player;
    private String      projectName;

    /**
     * Called when the activity is starting. Inflates the activity's UI and initializes the project
     * data from the activity's intent.
     *
     * @param savedInstanceState Contains the data in onSaveInstanceState(Bundle) if applicable.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        imageView    = (ImageView)   findViewById(R.id.imageView);
        buttonPlayFw = (ImageButton) findViewById(R.id.buttonPlayFw);
        buttonPlayBw = (ImageButton) findViewById(R.id.buttonPlayBw);

        initializeTextFields();
        initializeProject(getIntent());
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
        getMenuInflater().inflate(R.menu.menu_play, menu);
        return super.onCreateOptionsMenu(menu);
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
            case R.id.actionMorph: createFrames(); break;
            case R.id.actionEdit:  editLines();    break;
            case R.id.actionNew:   newProject();   break;
            case R.id.actionOpen:  openProject();  break;
            case R.id.actionSave:  saveProject();  break;
            case R.id.actionClose: finish();       break;
            default: return super.onOptionsItemSelected(item);
        }

        return true;
    }

    /**
     * Called when the user selects a project from the OpenProjectDialog. Loads this project in the
     * current activity.
     *
     * @param projectName The name of the project that the user has selected.
     */
    @Override
    public void onItemClick(String projectName)
    {
        loadProject(projectName);
    }

    /**
     * Called when the user selects OK in the SaveProjectDialog. Saves the current project under the
     * entered project name.
     *
     * @param projectName The project name the user has entered.
     */
    @Override
    public void onClick(final String projectName)
    {
        this.projectName = projectName;
        SpinnerWaitDialog<PlayActivity> dialog = new SpinnerWaitDialog<>(this);
        dialog.setTitle(R.string.dialogWaitSaveTitle);
        dialog.setMessage(R.string.dialogWaitSaveMessage);
        dialog.waitFor(SAVE_REQUEST, new Runnable()
        {
            @Override
            public void run()
            {
                morpher.saveToFile(projectName);
            }
        });
    }

    /**
     * Called when the runnable the SpinnerWaitDialog is waiting for is finished, i.e. when the
     * morpher project has been loaded.
     *
     * @param requestId An ID for identifying what has been waiting for.
     */
    @Override
    public void onNotify(int requestId)
    {
        if (requestId == LOAD_REQUEST)
        {
            int n = morpher.getNumFrames();

            if (n > 2)
            {
                // This project has already created frames.
                player.setNumSteps(n - 1);
                findViewById(R.id.layoutControls).setVisibility(View.VISIBLE);
            }

            imageView.setImageBitmap(morpher.getFrame(0));
        }
    }

    /**
     * Displays the next frame in paused mode.
     *
     * @param view The view that triggered this event.
     */
    public void buttonFrameNext_Click(View view)
    {
        pausePlayback();
        player.showNextFrame();
    }

    /**
     * Displays the previous frame in paused mode.
     *
     * @param view The view that triggered this event.
     */
    public void buttonFramePrev_Click(View view)
    {
        pausePlayback();
        player.showPrevFrame();
    }

    /**
     * Starts or pauses the animation in forward direction.
     *
     * @param view The view that triggered this event.
     */
    public void buttonPlayFw_Click(View view)
    {
        switch (player.getPlayDirection())
        {
            case FORWARDS:
                pausePlayback();
                break;

            case BACKWARDS:
                pausePlayback();
                // Falls through.

            default:
                buttonPlayFw.setImageResource(R.drawable.ic_action_pause);
                player.playForwards(Integer.parseInt(editFrameRate.getText().toString()));
        }
    }

    /**
     * Starts or pauses the animation in backward direction.
     *
     * @param view The view that triggered this event.
     */
    public void buttonPlayBw_Click(View view)
    {
        switch (player.getPlayDirection())
        {
            case BACKWARDS:
                pausePlayback();
                break;

            case FORWARDS:
                pausePlayback();
                // Falls through.

            default:
                buttonPlayBw.setImageResource(R.drawable.ic_action_pause);
                player.playBackwards(Integer.parseInt(editFrameRate.getText().toString()));
        }
    }

    /**
     * Displays the frame with the given index.
     *
     * @param index The index of the frame to be displayed.
     */
    public void showFrame(int index)
    {
        imageView.setImageBitmap(morpher.getFrame(index));
    }

    /**
     * Resets the play buttons to have their default icons (no pause button).
     */
    public void resetPlayButtons()
    {
        buttonPlayFw.setImageResource(R.drawable.ic_button_play_fw);
        buttonPlayBw.setImageResource(R.drawable.ic_button_play_bw);
    }

    /**
     * Initializes the activity's text fields with default values and adds action listeners for
     * input validation.
     */
    private void initializeTextFields()
    {
        editNumFrames = (EditText) findViewById(R.id.editNumFrames);
        editFrameRate = (EditText) findViewById(R.id.editFrameRate);
        editParamA    = (EditText) findViewById(R.id.editParamA);
        editParamB    = (EditText) findViewById(R.id.editParamB);
        editParamP    = (EditText) findViewById(R.id.editParamP);

        // Set default values.
        editNumFrames.setText(String.valueOf(Morpher.DEFAULT_NUM_FRAMES));
        editFrameRate.setText(String.valueOf(Player.DEFAULT_FRAME_RATE));
        editParamA.setText(String.valueOf(Morpher.DEFAULT_PARAM_A));
        editParamB.setText(String.valueOf(Morpher.DEFAULT_PARAM_B));
        editParamP.setText(String.valueOf(Morpher.DEFAULT_PARAM_P));

        // Add input validators.
        editNumFrames.setOnEditorActionListener(new InputValidatorInt(
                this, Morpher.MIN_NUM_FRAMES, Morpher.MAX_NUM_FRAMES));
        editFrameRate.setOnEditorActionListener(new InputValidatorInt(
                this, Player.MIN_FRAME_RATE, Player.MAX_FRAME_RATE));
        editParamA.setOnEditorActionListener(new InputValidatorFloat(
                this, Morpher.MIN_PARAM_A, Morpher.MAX_PARAM_A));
        editParamB.setOnEditorActionListener(new InputValidatorFloat(
                this, Morpher.MIN_PARAM_B, Morpher.MAX_PARAM_B));
        editParamP.setOnEditorActionListener(new InputValidatorFloat(
                this, Morpher.MIN_PARAM_P, Morpher.MAX_PARAM_P));
    }

    /**
     * Initializes the morpher project from the given intent.
     *
     * @param intent The intent containing initialization data.
     */
    private void initializeProject(Intent intent)
    {
        player = new Player(this);

        if (intent.hasExtra("projectName"))
        {
            loadProject(intent.getStringExtra("projectName"));
        }
        else
        {
            // This is a fresh project.
            projectName = null;
            Uri imageUri1 = intent.getParcelableExtra("imageUri1");
            Uri imageUri2 = intent.getParcelableExtra("imageUri2");
            Parcelable[] pLines1 = intent.getParcelableArrayExtra("lines1");
            Parcelable[] pLines2 = intent.getParcelableArrayExtra("lines2");

            int n = pLines1.length;
            Line[] lines1 = new Line[n];
            Line[] lines2 = new Line[n];

            for (int i = 0; i < n; ++i)
            {
                lines1[i] = (Line) pLines1[i];
                lines2[i] = (Line) pLines2[i];
            }

            morpher = new Morpher(this, imageUri1, imageUri2, lines1, lines2);
            imageView.setImageBitmap(morpher.getFrame(0));
        }
    }

    /**
     * Loads the project data from an existing project.
     *
     * @param projectName The name of the project to load.
     */
    private void loadProject(final String projectName)
    {
        this.projectName = projectName;
        final Context context = this;
        SpinnerWaitDialog<PlayActivity> dialog = new SpinnerWaitDialog<>(this);
        dialog.setTitle(R.string.dialogWaitLoadTitle);
        dialog.setMessage(R.string.dialogWaitLoadMessage);
        dialog.waitFor(LOAD_REQUEST, new Runnable()
        {
            @Override
            public void run()
            {
                morpher = new Morpher(context, projectName);
            }
        });
    }

    /**
     * Creates the morphing frames for the user-specified parameters and enables the player.
     */
    private void createFrames()
    {
        int   n = Integer.parseInt(editNumFrames.getText().toString()) + 1;
        float a = Float.parseFloat(editParamA.getText().toString());
        float b = Float.parseFloat(editParamB.getText().toString());
        float p = Float.parseFloat(editParamP.getText().toString());

        morpher.createFrames(n, a, b, p);
        player.setNumSteps(n);

        findViewById(R.id.layoutControls).setVisibility(View.VISIBLE);
    }

    /**
     * Starts the drawing activity where the user can edit the lines for this project.
     */
    private void editLines()
    {
        Intent intent = new Intent(this, DrawActivity.class);
        intent.putExtra("imageUri1", morpher.getImageUri1());
        intent.putExtra("imageUri2", morpher.getImageUri2());
        intent.putExtra("lines1",    morpher.getLines1());
        intent.putExtra("lines2",    morpher.getLines2());
        startActivity(intent);
        finish();
    }

    /**
     * Starts the activity for a new project.
     */
    private void newProject()
    {
        startActivity(new Intent(this, NewActivity.class));
        finish();
    }

    /**
     * Displays a dialog for choosing an existing project.
     */
    private void openProject()
    {
        new OpenProjectDialog().show(getFragmentManager(), "OpenProjectDialog");
    }

    /**
     * Displays a dialog for saving the current project.
     */
    private void saveProject()
    {
        Bundle bundle = new Bundle();
        bundle.putString("projectName", projectName);
        SaveProjectDialog dialog = new SaveProjectDialog();
        dialog.setArguments(bundle);
        dialog.show(getFragmentManager(), "SaveProjectDialog");
    }

    /**
     * Pauses the player and resets its play buttons to have their default icons (no pause button).
     */
    private void pausePlayback()
    {
        player.pause();
        resetPlayButtons();
    }
}