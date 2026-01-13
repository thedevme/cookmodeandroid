# CookMode — Product Spec

## What this app is
CookMode is a cooking utility focused on **hands-free recipe execution**, not recipe discovery. Users add their own recipes, then tap "Cook" to enter a distraction-free, step-by-step mode designed for messy hands and quick glances.

The core value is removing friction mid-cook:
big text, embedded timers, minimal touches.

---

## Target user
- Cooks at home regularly
- Already has recipes (bookmarks, screenshots, family recipes)
- Frustrated by recipe apps that bury instructions in ads and life stories
- Wants a utility, not a social platform

---

## Core features (non-negotiable)
- Add and save recipes (title, image, prep time, servings, ingredients, steps)
- Cooking Mode:
  - One step at a time, large readable text
  - Embedded timer per step (optional)
  - Back/Next navigation
  - Screen stays on
  - Step progress indicator
- Recipe list with search
- Edit/delete recipes
- Timer notifications (even when backgrounded)

---

## What this app is NOT
- Not a recipe discovery app
- Not a social network
- Not a meal planner
- Not an ingredient shopping list (v1)

---

## Design principles
- Glanceable over information-dense
- Cooking Mode is the product; everything else supports it
- Dark theme, high contrast, large touch targets
- No ads in Cooking Mode (ever)
- Motion is functional (step transitions), not decorative

---

## Technical constraints
- Android API 24+ (Kotlin / Jetpack Compose)
- Room for local persistence
- No backend for v1 (local-only)
- Timers must survive backgrounding and screen lock
- Testable architecture

---

## Monetization

### Free tier
- Up to 5 saved recipes
- Cooking Mode (full functionality)
- Single timer per step

### Pro unlock ($4.99 one-time)
- Unlimited recipes
- Multiple timers per recipe
- Ingredient scaling (½×, 2×, custom servings)
- Import recipe from clipboard/URL (basic parsing)
- No ads anywhere

---

## Success criteria
The app feels:
- Fast to open, fast to start cooking
- Readable from arm's length
- Trustworthy (timers work, screen stays on)
- Worth paying for after 3-5 recipes

Users should feel helped, not upsold.
