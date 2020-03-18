
package mkdroidrecorder;

interface ThreadAction {
  /**
   * Execute {@code runnable} action on implementer {@code Thread}
   */
  void execute(Runnable action);
}

