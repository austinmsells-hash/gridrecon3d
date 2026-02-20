# How to avoid losing project context

Because chat history may not always be accessible in the future, keep these items:
1) Save this zip file (source code + docs).
2) Put the project in a Git repo (recommended):
   - Install Git
   - `git init`
   - `git add .`
   - `git commit -m "Step 9 baseline"`
3) Keep PROJECT_STATE.md as the single source of truth.
4) When you request changes, include:
   - which zip version you are using
   - what screen/feature
   - expected behavior
