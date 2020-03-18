package mkdroid.filepicker;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import mkdroid.filepicker.utils.Orientation;

/**
 * Created by MKDroid on 12/03/20.
 */

public abstract class BaseFilePickerActivity extends AppCompatActivity {

  @SuppressLint("SourceLockedOrientationActivity")
  protected void onCreate(Bundle savedInstanceState, int layout) {
    super.onCreate(savedInstanceState);
    setTheme(PickerManager.getInstance().getTheme());
    setContentView(layout);

    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    //set orientation
    Orientation orientation = PickerManager.getInstance().getOrientation();
      if (orientation == Orientation.PORTRAIT_ONLY) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
      } else if (orientation == Orientation.LANDSCAPE_ONLY) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
      }

    initView();
  }

  protected abstract void initView();
}
