### Name
* Gabe Cardenas
---

### Project Title
* Japan 26

---

### What are you building?

* A story-driven experience IN JAPAN
* You go around japan and see the sights, playing minigames and solving... mysteries?
* Text dialogue and narrative included.

---

### Why is this a Java 2 Project?
* I'll use advanced concepts like generics
* It will utalize more complex class structures and data structures which we previously didn't know about in Java 1.
* Reusability and composition with be present. (Trust)


### Minimum Viable Product (MVP)

* Self-contained story sequence (Start, Middle, End)
* 3 Minigame Tasks between story beats
* Photographs and art to complement story

---

### Stretch Goals (Optional)

* Adding more minigames then planned
* Adding Videos

---
### Solo or Pair
* Solo
---

### AI Use Plan
* I plan to use AI mainly for JavaFX since we didn't cover that in this class. 
* I'll probably use AI to cover parts of JAVA that we haven't learned in great
* detail. (Shouldn't be too many.)
* (Any algorithms or data stuff that we don't know about.)
* The different classes and setups will be be manually coded for the most part. 

---

### How To Run

This project now runs as a Swing app (no JavaFX/Maven runtime needed).

Requirements:
- Java JDK installed (`javac` and `java` available in terminal PATH)
- Windows PowerShell or Command Prompt

From the project root:

```powershell
.\run.ps1
```

Other options:
- `.\run.bat` (cmd/batch wrapper)
- `.\run.ps1 -NoClean` (faster reruns, skips deleting `out`)

If PowerShell blocks script execution, run:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File ".\run.ps1"
```

Manual fallback (without scripts):

```powershell
if (Test-Path out) { Remove-Item -Recurse -Force out }
New-Item -ItemType Directory out | Out-Null
$files = Get-ChildItem -Recurse -Filter *.java "src/main/java" | ForEach-Object { $_.FullName }
javac -d out $files
java -cp "out;src/main/resources" japan26.Main
```
