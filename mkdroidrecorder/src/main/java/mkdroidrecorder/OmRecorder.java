/**
 * Create by MKDroid on 12/03/20
 */
package mkdroidrecorder;

import java.io.File;

/**
 * Create by MKDroid on 12/03/20
 */
public final class OmRecorder {
  private OmRecorder() {
  }

  public static Recorder pcm(PullTransport pullTransport, File file) {
    return new Pcm(pullTransport, file);
  }

  public static Recorder wav(PullTransport pullTransport, File file) {
    return new Wav(pullTransport, file);
  }
}
