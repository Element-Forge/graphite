# CLAUDE.md - Project Instructions

> **READ THIS ENTIRE FILE BEFORE EVERY TASK.**

---

## ⛔ STOP RULES

**STOP and ASK the user before proceeding when:**

1. **Authentication fails** → Do NOT create workarounds. Ask.
2. **Permission denied** → Ask user to elevate permissions.
3. **A command fails twice** → Do NOT keep retrying. Ask.
4. **You're unsure which issue a task belongs to** → Ask.
5. **You need to create a temporary/helper script** → Ask first.
6. **CI fails and you don't understand why** → Ask.
7. **You need to install a new dependency** → Ask first.
8. **The task scope seems larger than the issue describes** → Ask.

**When in doubt, ASK. Asking is the correct action, not a failure.**

---

## 🚫 NEVER

- Hardcode tokens, passwords, secrets, or API keys into ANY file
- Extract credentials from command output and reuse them
- Create "workaround" scripts to bypass auth or permission issues
- Combine multiple issues into one branch or PR
- Continue after repeated failures without asking
- Guess email addresses, usernames, or IDs — ask or search
- Use `--watch` or other blocking/long-running flags without permission
- Push directly to `main` or the default branch
- Modify CI/CD configuration without explicit permission
- Delete or force-push branches without asking

---

## ✅ BEFORE EVERY TASK

1. Read this file (CLAUDE.md)
2. Read [GITHUB.md](GITHUB.md) for Git workflow rules
3. Identify the issue number you're working on
4. Confirm you understand the requirements (ask if unclear)
5. Create a correctly-named branch (see GITHUB.md)
6. Work on ONE issue only

---

## Task Workflow

### Starting a New Task

```
1. gh issue view <number>           # Understand the issue
2. git checkout main && git pull    # Start from latest main
3. git checkout -b <issue>-<desc>   # Create branch (see GITHUB.md)
4. # Do the work
5. git add . && git commit          # Commit (see GITHUB.md for format)
6. git push -u origin <branch>      # Push
7. gh pr create                     # Create PR (see GITHUB.md for format)
8. # Wait for CI to pass
9. # Present PR to user only after CI passes
```

### When CI Fails

```
1. Read the failure message carefully
2. Fix the issue locally
3. Commit and push
4. Wait for CI again
5. If it fails 2+ times and you don't understand why → STOP and ASK
```

### When Authentication Fails

```
1. STOP — do not create workarounds
2. Tell the user what failed
3. Ask how to authenticate
4. Do NOT extract tokens and hardcode them
```

---

## Code Style

<!-- 
Add your project-specific code style rules here. Examples:

### Language/Framework
- Formatting rules
- Naming conventions
- File organization

### Database
- Migration patterns
- Naming conventions

### Testing
- Test file location
- Naming conventions
- Coverage requirements
-->

---

## Project-Specific Commands

<!-- 
Add commands Claude should know. Examples:

| Task | Command |
|------|---------|
| Run tests | `npm test` |
| Format code | `npm run format` |
| Build | `npm run build` |
| Start dev server | `npm run dev` |
-->

---

## File Organization

<!-- 
Describe your project structure. Example:

```
src/
├── components/    # UI components
├── services/      # Business logic
├── utils/         # Helper functions
└── types/         # Type definitions
```
-->

---

## .gitignore Style

- Files/directories: start with `/`
- Directories: end with `/`
- Lines: sorted alphabetically
- Order: directories before files
