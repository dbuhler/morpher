package dbuhler.morpher.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Images.Media;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;

import dbuhler.morpher.R;
import dbuhler.morpher.dialogs.AspectRatioDialog;
import dbuhler.morpher.dialogs.SpinnerWaitDialog;

/**
 * This activity lets the user choose the images for morphing. If the images have different aspect
 * ratios, then the user must specify which one to apply to both images before they can proceed. The
 * images are then scaled to the size of the image views and stored in the application's cache.
 *
 * @author  Dan Buhler
 * @version 2015-02-15
 */
public final class NewActivity extends Activity implements AspectRatioDialog.OnClickListener,
                                                           SpinnerWaitDialog.OnNotifyListener
{
    private View      imageButton1;
    private View      imageButton2;
    private ImageView imageView1;
    private ImageView imageView2;
    private Uri       imageUri1;
    private Uri       imageUri2;
    private boolean   showAcceptButton = false;

    /**
     * Called when the activity is starting. Inflates the activity's UI.
     *
     * @param savedInstanceState Contains the data in onSaveInstanceState(Bundle) if applicable.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new);

        imageButton1 = findViewById(R.id.imageButton1);
        imageButton2 = findViewById(R.id.imageButton2);
        imageView1   = (ImageView) findViewById(R.id.lineView1);
        imageView2   = (ImageView) findViewById(R.id.lineView2);
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
        getMenuInflater().inflate(R.menu.menu_new, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Prepare the activity's options menu to be displayed. This is called right before the menu is
     * shown, every time it is shown. Sets the visibility of the accept button.
     *
     * @param menu The options menu as last shown or first initialized by onCreateOptionsMenu().
     * @return True if the menu is to be displayed; false if it will not be shown.
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        menu.findItem(R.id.actionDone).setVisible(showAcceptButton);
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
            case R.id.actionDone:
                checkAspectRatio();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Called when an activity exits. Processes the results of ACTION_OPEN_DOCUMENT activities that
     * are triggered by the image buttons and views, i.e. hides the image button after an image has
     * been selected and loads it into the appropriate image view.
     *
     * @param requestCode The integer request code originally supplied to startActivityForResult().
     * @param resultCode  The integer result code returned by the child activity via setResult().
     * @param resultData  An Intent, which can return result data to the caller.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData)
    {
        if (resultCode == Activity.RESULT_OK && resultData != null)
        {
            switch (requestCode)
            {
                case R.id.imageButton1:
                    imageButton1.setVisibility(View.GONE);
                    imageView1.setVisibility(View.VISIBLE);
                    // Fall through.

                case R.id.lineView1:
                    imageUri1 = resultData.getData();
                    imageView1.setImageURI(imageUri1);
                    break;

                case R.id.imageButton2:
                    imageButton2.setVisibility(View.GONE);
                    imageView2.setVisibility(View.VISIBLE);
                    // Fall through.

                case R.id.lineView2:
                    imageUri2 = resultData.getData();
                    imageView2.setImageURI(imageUri2);
                    break;

                default:
                    super.onActivityResult(requestCode, resultCode, resultData);
                    return;
            }

            // If both images have been loaded, show the accept button.
            if (imageUri1 != null && imageUri2 != null)
            {
                showAcceptButton = true;
                invalidateOptionsMenu();
            }
        }

        super.onActivityResult(requestCode, resultCode, resultData);
    }

    /**
     * Called when a button in the AspectRatioDialog is clicked.
     *
     * @param which The button that was clicked.
     */
    @Override
    public void onClick(int which)
    {
        switch (which)
        {
            case AspectRatioDialog.BUTTON_LEFT:
                cacheImages(imageView1.getWidth(), imageView1.getHeight());
                break;

            case AspectRatioDialog.BUTTON_RIGHT:
                cacheImages(imageView2.getWidth(), imageView2.getHeight());
                break;
        }
    }

    /**
     * Called when the runnable the SpinnerWaitDialog is waiting for is finished, i.e. when the
     * images have been scaled and stored.
     *
     * @param requestId An ID for identifying what has been waiting for.
     */
    @Override
    public void onNotify(int requestId)
    {
        Intent intent = new Intent(this, DrawActivity.class);
        intent.putExtra("imageUri1", imageUri1);
        intent.putExtra("imageUri2", imageUri2);
        startActivity(intent);
        finish();
    }

    /**
     * Starts an ACTION_OPEN_DOCUMENT activity that lets the user browse for an image file.
     *
     * @param view The view that triggered this event.
     */
    public void loadImage_Click(View view)
    {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, view.getId());
    }

    /**
     * Checks whether both image views have the same dimensions, i.e. whether both images have the
     * same aspect ratio. If not, prompts the user to choose which aspect ratio to apply.
     */
    private void checkAspectRatio()
    {
        if (imageView1.getWidth()  != imageView2.getWidth() ||
            imageView1.getHeight() != imageView2.getHeight())
        {
            // Aspect ratios are different.
            new AspectRatioDialog().show(getFragmentManager(), "AspectRatioDialog");
        }
        else
        {
            // Both aspect ratios are equal.
            cacheImages(imageView1.getWidth(), imageView1.getHeight());
        }
    }

    /**
     * Scales the images to the given dimensions and stores them as bitmaps in the application's
     * cache directory. Then starts the next activity, passing on the bitmaps' URIs.
     *
     * @param sizeX The width of the scaled images.
     * @param sizeY The height of the scaled images.
     */
    private void cacheImages(final int sizeX, final int sizeY)
    {
        SpinnerWaitDialog<NewActivity> dialog = new SpinnerWaitDialog<>(this);
        dialog.setTitle(R.string.dialogWaitNewTitle);
        dialog.setMessage(R.string.dialogWaitNewMessage);
        dialog.waitFor(0, new Runnable()
        {
            @Override
            public void run()
            {
                imageUri1 = createScaledBitmapFromUri(imageUri1, sizeX, sizeY, "image1");
                imageUri2 = createScaledBitmapFromUri(imageUri2, sizeX, sizeY, "image2");
            }
        });
    }

    /**
     * Creates a bitmap from the image with the given URI, scaled to the given dimensions, which is
     * then stored under the given file name in the application's cache directory. Returns the
     * bitmap's URI.
     *
     * @param uri The URI of the original image.
     * @param sizeX The width of the scaled bitmap.
     * @param sizeY The height of the scaled bitmap.
     * @param fileName The name under which to store the scaled bitmap.
     * @return The URI of the scaled bitmap.
     */
    private Uri createScaledBitmapFromUri(Uri uri, int sizeX, int sizeY, String fileName)
    {
        File file = new File(getCacheDir(), fileName);

        try
        {
            Bitmap bitmap = Bitmap.createScaledBitmap(
                    Media.getBitmap(getContentResolver(), uri), sizeX, sizeY, true);

            FileOutputStream bitmapOut = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, bitmapOut);
            bitmapOut.flush();
            bitmapOut.close();

            return Uri.fromFile(file);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }
}