package japan26.engine;

import japan26.minigame.PlayerRewards;
import japan26.model.DialogueLine;
import japan26.model.StoryScene;
import japan26.ui.SettingsState;

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

        stepToNextLine();
    }

    private void stepToNextLine() {
        lineIndex++;
        // Auto-skip lines whose resolved text is empty (silent conditional beats).
        if (lineIndex < currentScene.getLines().size()) {
            String resolved = getCurrentLineText();
            if (resolved != null && resolved.isBlank()
                    && !currentScene.getLines().get(lineIndex).hasChoices()) {
                applyCurrentLineTriggers();
                lineIndex++;
            }
        }
        if (lineIndex >= currentScene.getLines().size()) {
            // Scene is done – check for minigame hook or move on
            if (currentScene.hasChoiceMinigame()) {
                String choiceId = currentScene.getChoiceMinigameChoiceId();
                String selected = selectedChoices.get(choiceId);
                String minigameKey = currentScene.resolveChoiceMinigame(selected);
                if (minigameKey != null) {
                    SceneManager.launchMinigame(minigameKey, this::afterMinigame);
                    return;
                }
            }
            if (currentScene.hasMinigame()) {
                SceneManager.launchMinigame(currentScene.getFollowUpMinigame(), this::afterMinigame);
                return;
            }
            advanceToNextScene();
        } else {
            applyCurrentLineTriggers();
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
        applyCurrentLineTriggers();
        if (onSceneChanged != null) onSceneChanged.run();
    }

    private void applyCurrentLineTriggers() {
        DialogueLine line = getCurrentLine();
        if (line == null || !line.hasChoiceResponses()) return;
        if ("state_grad_unlock".equals(line.getChoiceId())) {
            SettingsState.unlockPlayerPreset(7);
        }
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
        if ("drumsticks_offer".equals(line.getChoiceId())
                && "Give him Phil's drumsticks".equals(option)
                && PlayerRewards.has("phil collins drumsticks")) {
            SettingsState.unlockPlayerPreset(11);
        }
        stepToNextLine();
    }

    public StoryScene getCurrentScene()              { return currentScene; }
    public boolean    isFinished()                   { return finished; }
    public String     getSelectedChoice(String id)   { return selectedChoices.get(id); }

    // ── Listener wiring ───────────────────────────────────────────────────────

    public void setOnLineChanged(Runnable r)    { onLineChanged    = r; }
    public void setOnSceneChanged(Runnable r)   { onSceneChanged   = r; }
    public void setOnStoryFinished(Runnable r)  { onStoryFinished  = r; }
}
