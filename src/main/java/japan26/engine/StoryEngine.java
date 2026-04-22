package japan26.engine;

import japan26.model.DialogueLine;
import japan26.model.StoryScene;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;
import java.util.Deque;
import java.util.Map;

/**
 * Drives story playback.  Holds the ordered queue of scenes and tracks which
 * scene and which line is currently active.
 *
 * Generics note: the internal queue is typed as Deque<StoryScene>, giving us
 * O(1) polling and the ability to peek at what's coming next.
 */
public class StoryEngine {

    private final Deque<StoryScene> sceneQueue = new ArrayDeque<>();
    private StoryScene   currentScene;
    private int          lineIndex = 0;
    private boolean      finished  = false;
    private final Map<String, String> selectedChoices = new HashMap<>();

    // Listeners so the UI can react without polling
    private Runnable onLineChanged;
    private Runnable onSceneChanged;
    private Runnable onStoryFinished;

    // ── Loading ───────────────────────────────────────────────────────────────

    public void loadStory(List<StoryScene> scenes) {
        sceneQueue.clear();
        sceneQueue.addAll(scenes);
        lineIndex = 0;
        finished  = false;
        selectedChoices.clear();
        advanceToNextScene();
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    /**
     * Move to the next line, or the next scene if the current one is exhausted.
     * Call this when the player clicks / presses Space.
     */
    public void advance() {
        if (finished) return;
        DialogueLine current = getCurrentLine();
        if (current != null && current.hasChoices()) return;

        lineIndex++;
        if (lineIndex >= currentScene.getLines().size()) {
            // Scene is done – check for minigame hook or move on
            if (currentScene.hasMinigame()) {
                SceneManager.launchMinigame(currentScene.getFollowUpMinigame(), this::afterMinigame);
                return;
            }
            advanceToNextScene();
        } else {
            if (onLineChanged != null) onLineChanged.run();
        }
    }

    private void afterMinigame() {
        advanceToNextScene();
    }

    private void advanceToNextScene() {
        if (sceneQueue.isEmpty()) {
            finished = true;
            if (onStoryFinished != null) onStoryFinished.run();
            return;
        }
        currentScene = sceneQueue.poll();
        lineIndex    = 0;
        if (onSceneChanged != null) onSceneChanged.run();
    }

    // ── State queries ─────────────────────────────────────────────────────────

    public DialogueLine getCurrentLine() {
        if (currentScene == null || finished) return null;
        return currentScene.getLines().get(lineIndex);
    }

    public String getCurrentLineText() {
        DialogueLine line = getCurrentLine();
        if (line == null) return "";
        if (line.hasChoiceResponses()) {
            return line.getTextForChoice(selectedChoices.get(line.getChoiceId()));
        }
        return line.getText();
    }

    public void choose(String option) {
        DialogueLine line = getCurrentLine();
        if (line == null || !line.hasChoices()) return;
        selectedChoices.put(line.getChoiceId(), option);
        advance();
    }

    public StoryScene getCurrentScene() { return currentScene; }
    public boolean    isFinished()      { return finished; }

    // ── Listener wiring ───────────────────────────────────────────────────────

    public void setOnLineChanged(Runnable r)    { onLineChanged    = r; }
    public void setOnSceneChanged(Runnable r)   { onSceneChanged   = r; }
    public void setOnStoryFinished(Runnable r)  { onStoryFinished  = r; }
}
