
package mkdroidrecorder;

import java.io.File;

/**
 * Create by MKDroid on 12/03/20
 */
final class Pcm extends AbstractRecorder {
  public Pcm(PullTransport pullTransport, File file) {
    super(pullTransport, file);
  }
}