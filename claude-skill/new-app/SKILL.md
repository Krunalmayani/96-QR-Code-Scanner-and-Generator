---
name: new-app
description: Create a new Android app repo from the user's GitHub template (package id + app name), push it, and tell them where it is. Use when the user says "new app", "create new app", or gives an app name and package id.
---

# new-app

Creates a new Android project from the `android-template` GitHub template repo.

Inputs (ask only if missing): repo name, package id (prefix `com.app.vv.`), app name.

Steps:

1. Check `gh auth status`. If not logged in, tell the user to run `gh auth login` and stop.
2. From the user's projects folder, run:
   `gh repo create <repo> --template <github-user>/android-template --private --clone`
   (get `<github-user>` with `gh api user -q .login`).
3. `cd <repo> && chmod +x scripts/rename.sh && ./scripts/rename.sh <package.id> "<App Name>"`
4. `git add -A && git commit -m "Initial setup: <App Name>" && git push -u origin HEAD`
5. Report the repo URL and local path in one or two lines. Remind them of the
   "Going live checklist" in README.md only if they are about to publish.

Do not create the project any other way, and do not edit the template repo itself.
