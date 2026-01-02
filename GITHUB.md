# GITHUB.md - Git & GitHub Workflow

> **READ THIS ENTIRE FILE BEFORE ANY GIT OPERATION.**

---

## Quick Reference

| Item | Format | Example |
|------|--------|---------|
| **Branch** | `<issue>-<description>` | `42-server-lifecycle` |
| **Commit** | `#<issue> <lowercase description>` | `#42 add server start endpoint` |
| **PR Title** | `#<issue> <Title Case>` | `#42 Add Server Start Endpoint` |
| **PR Body** | Must include `Closes #<issue>` | `Closes #42` |
| **Base** | `main` | — |
| **Merge** | Squash merge only | — |

---

## ⛔ Common Mistakes — AVOID THESE

```
❌ feature/42-server-lifecycle     → No prefixes like feature/, fix/, etc.
❌ 42_server_lifecycle             → No underscores, use hyphens
❌ 42-Server-Lifecycle             → No uppercase in branch names
❌ server-lifecycle                → Must START with issue number
❌ #42: add endpoint               → No colon after issue number
❌ #42 Add Server Endpoint         → Commits are lowercase (except proper nouns)
❌ #42 add server endpoint.        → No trailing period
❌ Implement feature               → PR title must have issue number
❌ #42 implement feature           → PR title must be Title Case
```

---

## Branch Naming

**Format:** `<issue-number>-<short-description>`

**Rules:**
- Lowercase only
- Hyphens for spaces (no underscores)
- Must start with issue number
- Maximum 50 characters
- No prefixes (`feature/`, `fix/`, `bugfix/`, etc.)

```
✅ 42-server-lifecycle
✅ 17-add-user-authentication
✅ 3-fix-startup-crash
```

---

## Commit Messages

**Format:** `#<issue-number> <description>`

**Rules:**
- Start with `#` + issue number + space
- NO colon or hyphen after issue number
- Lowercase description (except proper nouns)
- No trailing period
- Maximum 72 characters

```
✅ #42 add server start endpoint
✅ #5 add Docker container support
✅ #3 fix null pointer in startup sequence
```

---

## Pull Requests

### PR Title

**Format:** `#<issue-number> <Title In Title Case>`

```
✅ #42 Implement Server Lifecycle Management
✅ #17 Add User Authentication
✅ #3 Fix Null Pointer in Startup Sequence
```

> **Why Title Case?** PR titles become the squash commit message on `main`. They appear in release notes and should look polished.

### PR Description

Must include `Closes #<issue>` to auto-close the issue on merge.

Example:
```
Implements the server lifecycle management feature.

Closes #42
```

### PR Requirements

- [ ] Base branch: `main`
- [ ] One issue per PR (never combine issues)
- [ ] CI must pass before requesting review
- [ ] Branch must be up to date with `main`
- [ ] Use squash merge

---

## Labels

### Type (pick one)

| Label | Use for |
|-------|---------|
| `type::feature` | New functionality |
| `type::bug` | Bug fixes |
| `type::chore` | Maintenance, refactoring, dependencies |
| `type::docs` | Documentation only |

### Area (pick one or more)

<!-- Customize these for your project -->

| Label | Use for |
|-------|---------|
| `area::backend` | API, services, business logic |
| `area::frontend` | UI, views, components |
| `area::infra` | CI/CD, Docker, configuration |

---

## Milestones

- Structure: Version-based (`v0.1.0`, `v0.2.0`, `v1.0.0`)
- Creation: Collaborative — discuss scope before creating
- Milestones align with Git tags and releases

---

## CI Pipeline

Every PR validates:

1. Branch name format
2. Commit message format
3. PR title format
4. Code formatting (linter/formatter)
5. Tests + coverage

### Before Creating a PR

Run tests locally:
```bash
# Replace with your project's test command
./run-tests.sh
```

### Before Requesting Review

- [ ] All CI checks pass (green checkmark)
- [ ] No merge conflicts with `main`
- [ ] Coverage requirements met

**Do NOT present a PR to the user until CI passes.**

---

## Complete Workflow

### New Feature/Fix

```bash
# 1. Get the issue
gh issue view <number>

# 2. Start fresh from main
git checkout main && git pull

# 3. Create branch (lowercase, issue number first)
git checkout -b <issue>-<description>

# 4. Do the work...

# 5. Commit (lowercase message)
git add .
git commit -m "#<issue> <lowercase description>"

# 6. Push
git push -u origin <branch>

# 7. Create PR (Title Case title)
gh pr create --title "#<issue> <Title Case>" --body "Description here.

Closes #<issue>"

# 8. Wait for CI to pass

# 9. Present PR to user
```

### Fixing CI Failures

```bash
# 1. Read the error carefully
# 2. Fix locally
# 3. Commit the fix
git add . && git commit -m "#<issue> fix <what you fixed>"
# 4. Push
git push
# 5. Wait for CI again
# 6. If still failing after 2 attempts → STOP and ask
```

---

## Git Hooks (Optional)

If this project uses git hooks, set them up with:

```bash
./setup-hooks.sh
```

Hooks provide immediate feedback but can be bypassed with `--no-verify`. CI cannot be bypassed.

---

## When Validation Fails

If you see a validation error or CI failure:

**Required actions:**

1. **STOP** what you are doing
2. **READ** this file (GITHUB.md) completely
3. **READ** CLAUDE.md completely
4. **IDENTIFY** which rule you violated
5. **FIX** the violation
6. **VERIFY** locally before pushing again

Do NOT retry without understanding what went wrong.
