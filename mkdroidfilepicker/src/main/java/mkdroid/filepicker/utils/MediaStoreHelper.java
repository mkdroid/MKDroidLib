package mkdroid.filepicker.utils;

import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;

import java.util.Comparator;
import java.util.List;

import mkdroid.filepicker.FilePickerConst;
import mkdroid.filepicker.cursors.DocScannerTask;
import mkdroid.filepicker.cursors.loadercallbacks.FileMapResultCallback;
import mkdroid.filepicker.cursors.loadercallbacks.FileResultCallback;
import mkdroid.filepicker.cursors.loadercallbacks.PhotoDirLoaderCallbacks;
import mkdroid.filepicker.models.Document;
import mkdroid.filepicker.models.FileType;
import mkdroid.filepicker.models.PhotoDirectory;

public class MediaStoreHelper {

  public static void getPhotoDirs(FragmentActivity activity, Bundle args, FileResultCallback<PhotoDirectory> resultCallback) {
    if(activity.getSupportLoaderManager().getLoader(FilePickerConst.MEDIA_TYPE_IMAGE)!=null)
      activity.getSupportLoaderManager().restartLoader(FilePickerConst.MEDIA_TYPE_IMAGE, args, new PhotoDirLoaderCallbacks(activity, resultCallback));
    else
      activity.getSupportLoaderManager().initLoader(FilePickerConst.MEDIA_TYPE_IMAGE, args, new PhotoDirLoaderCallbacks(activity, resultCallback));
  }

  public static void getVideoDirs(FragmentActivity activity, Bundle args, FileResultCallback<PhotoDirectory> resultCallback) {
    if(activity.getSupportLoaderManager().getLoader(FilePickerConst.MEDIA_TYPE_VIDEO)!=null)
      activity.getSupportLoaderManager().restartLoader(FilePickerConst.MEDIA_TYPE_VIDEO, args, new PhotoDirLoaderCallbacks(activity, resultCallback));
    else
      activity.getSupportLoaderManager().initLoader(FilePickerConst.MEDIA_TYPE_VIDEO, args, new PhotoDirLoaderCallbacks(activity, resultCallback));
  }

  public static void getDocs(FragmentActivity activity,
                             List<FileType> fileTypes,
                             Comparator<Document> comparator,
                             FileMapResultCallback fileResultCallback)
  {
    new DocScannerTask(activity, fileTypes, comparator, fileResultCallback).execute();
  }
}