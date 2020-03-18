package mkdroid.filepicker.cursors.loadercallbacks;

import java.util.List;
import java.util.Map;

import mkdroid.filepicker.models.Document;
import mkdroid.filepicker.models.FileType;

/**
 * Created by gabriel on 10/2/17.
 */

public interface FileMapResultCallback {
    void onResultCallback(Map<FileType, List<Document>> files);
}

