# CookMode Android — Build Plan v1

A sequential execution checklist optimized for **fast launch**.

**Philosophy:** Cooking Mode is the product. Ship that first, polish everything else around it. Tests protect core logic.

---

## Quick Reference: All Milestones

| Step | Milestone |
|------|-----------|
| 42 | ✓ Models + Database Working (with tests) |
| 72 | ✓ Cooking Mode Functional (with tests) |
| 102 | ✓ Full Recipe CRUD (with tests) |
| 125 | ✓ Timers Work (with tests) |
| **145** | **🚀 ALPHA — Internal Testing** |
| **182** | **🚀 BETA — Monetization Working** |
| **215** | **🎉 LAUNCH** |

---

## Timeline

| Build | Target | Step | What's Working |
|-------|--------|------|----------------|
| **Alpha** | End of Week 1 | 145 | Add recipes, Cooking Mode, timers, tests passing |
| **Beta** | End of Week 2 | 182 | Pro unlock, paywall, polish |
| **Launch** | Week 2-3 | 215 | Play Store release |

---

## Project Configuration

**Package ID:** `io.designtoswiftui.cookmode`
**Developer:** Cocoa Academy

---

## Phase 1: Project Setup

### 1.1 — Create Android Studio Project
- [ ] 1. Create new project: "CookMode" (Empty Activity with Jetpack Compose)
- [ ] 2. Set minimum SDK to API 24
- [ ] 3. Set Kotlin DSL for build configuration
- [ ] 4. Set screen orientation to Portrait only
- [ ] 5. Set app theme to dark only

### 1.2 — Configure Project Structure
- [ ] 6. Create package: `models/`
- [ ] 7. Create package: `data/`
- [ ] 8. Create package: `data/repository/`
- [ ] 9. Create package: `ui/`
- [ ] 10. Create package: `ui/home/`
- [ ] 11. Create package: `ui/recipe/`
- [ ] 12. Create package: `ui/cooking/`
- [ ] 13. Create package: `ui/paywall/`
- [ ] 14. Create package: `ui/components/`
- [ ] 15. Create package: `viewmodels/`
- [ ] 16. Create package: `timer/`
- [ ] 17. Create package: `util/`

### 1.3 — Configure Test Structure
- [ ] 18. Create test package: `unit/`
- [ ] 19. Create test package: `helpers/`
- [ ] 20. Create androidTest package: `ui/`
- [ ] 21. Add test dependencies (JUnit, Mockk, Turbine, Compose Testing)

### 1.4 — Configuration Files
- [ ] 22. Update `.gitignore`
- [ ] 23. Create `README.md`
- [ ] 24. Create `CLAUDE.md`
- [ ] 25. Create `/docs` folder with specs

---

## Phase 2: Data Models

### 2.1 — Create Core Models
- [ ] 26. Create `models/Recipe.kt`
  - @Entity: id, title, imageUri, prepTime, servings, createdAt

- [ ] 27. Create `models/Ingredient.kt`
  - @Entity: id, recipeId (FK), amount, unit, name, orderIndex

- [ ] 28. Create `models/Step.kt`
  - @Entity: id, recipeId (FK), instruction, timerSeconds (nullable), orderIndex

- [ ] 29. Create `models/RecipeWithDetails.kt`
  - Data class combining Recipe + List<Ingredient> + List<Step>

### 2.2 — Configure Room
- [ ] 30. Add Room dependencies to build.gradle.kts
- [ ] 31. Create `data/AppDatabase.kt`
- [ ] 32. Create `data/RecipeDao.kt`
- [ ] 33. Create `data/IngredientDao.kt`
- [ ] 34. Create `data/StepDao.kt`
- [ ] 35. Configure Hilt + DatabaseModule

### 2.3 — Repository
- [ ] 36. Create `data/repository/RecipeRepository.kt`
- [ ] 37. Implement `suspend fun saveRecipe(recipe, ingredients, steps)`
- [ ] 38. Implement `fun getAllRecipes(): Flow<List<Recipe>>`
- [ ] 39. Implement `suspend fun getRecipeWithDetails(id): RecipeWithDetails`
- [ ] 40. Implement `suspend fun deleteRecipe(id)`
- [ ] 41. Implement `fun getRecipeCount(): Flow<Int>`

### 2.4 — Model Tests
- [ ] 42. Create `unit/RecipeTests.kt`
- [ ] 43. Test: Recipe creates with correct defaults
- [ ] 44. Test: Ingredients sort by orderIndex
- [ ] 45. Test: Steps sort by orderIndex
- [ ] 46. Create `unit/RecipeRepositoryTests.kt` (with in-memory DB)
- [ ] 47. Test: Save and retrieve recipe
- [ ] 48. Test: Delete removes recipe and children
- [ ] 49. Test: Recipe count returns correct value
- [ ] 50. Run tests — all must pass

**✅ MILESTONE: Models + Database Working (Step 42)**

---

## Phase 3: Cooking Mode (Core Feature)

### 3.1 — Cooking State
- [ ] 51. Create `ui/cooking/CookingState.kt`
  - data class: currentStepIndex, steps, isTimerRunning, timerSecondsRemaining

- [ ] 52. Create `viewmodels/CookingViewModel.kt`
- [ ] 53. Implement `fun loadRecipe(recipeId: Long)`
- [ ] 54. Implement `fun nextStep()`
- [ ] 55. Implement `fun previousStep()`
- [ ] 56. Implement `fun startTimer()`
- [ ] 57. Implement `fun pauseTimer()`
- [ ] 58. Implement `fun resetTimer()`
- [ ] 59. StateFlow: `uiState: CookingState`
- [ ] 60. Computed: currentStep, isFirstStep, isLastStep, progress

### 3.2 — Cooking State Tests
- [ ] 61. Create `unit/CookingViewModelTests.kt`
- [ ] 62. Test: Initial state is step 0
- [ ] 63. Test: nextStep increments index
- [ ] 64. Test: nextStep does nothing on last step
- [ ] 65. Test: previousStep decrements index
- [ ] 66. Test: previousStep does nothing on first step
- [ ] 67. Test: loadRecipe populates steps in order
- [ ] 68. Test: Timer starts with correct duration
- [ ] 69. Test: Timer pause preserves remaining time
- [ ] 70. Test: Timer reset restores original duration
- [ ] 71. Test: isFirstStep/isLastStep computed correctly
- [ ] 72. Run tests — all must pass

### 3.3 — Cooking Mode UI
- [ ] 73. Create `ui/cooking/CookingScreen.kt`
- [ ] 74. Step progress indicator ("2 of 6")
- [ ] 75. Large step instruction text (centered)
- [ ] 76. Timer display (MM:SS) when step has timer
- [ ] 77. Play/Pause timer button
- [ ] 78. Back / Next Step buttons
- [ ] 79. Keep screen on (FLAG_KEEP_SCREEN_ON)
- [ ] 80. Exit button (X) with confirmation
- [ ] 81. Step list drawer (tap icon to see all steps)
- [ ] 82. Step transition animations

**✅ MILESTONE: Cooking Mode Functional (Step 72)**

---

## Phase 4: Recipe Management

### 4.1 — Home Screen
- [ ] 83. Create `viewmodels/HomeViewModel.kt`
- [ ] 84. StateFlow: recipes, searchQuery
- [ ] 85. Create `ui/home/HomeScreen.kt`
- [ ] 86. Recipe list (LazyColumn)
- [ ] 87. Each item: icon, title, prep time, chevron
- [ ] 88. Search bar
- [ ] 89. FAB to add recipe
- [ ] 90. Empty state ("Add your first recipe")

### 4.2 — Add/Edit Recipe Screen
- [ ] 91. Create `viewmodels/EditRecipeViewModel.kt`
- [ ] 92. Create `ui/recipe/EditRecipeScreen.kt`
- [ ] 93. Image picker (optional photo)
- [ ] 94. Title field
- [ ] 95. Prep time field
- [ ] 96. Servings field
- [ ] 97. Ingredients section (add/remove/reorder)
- [ ] 98. Steps section (add/remove/reorder)
- [ ] 99. Timer toggle per step (with duration picker)
- [ ] 100. Save button
- [ ] 101. Delete recipe option (edit mode only)

### 4.3 — Recipe Validation Tests
- [ ] 102. Create `unit/EditRecipeViewModelTests.kt`
- [ ] 103. Test: Empty title fails validation
- [ ] 104. Test: No steps fails validation
- [ ] 105. Test: Valid recipe passes validation
- [ ] 106. Test: Edit mode loads existing data
- [ ] 107. Test: Ingredients reorder correctly
- [ ] 108. Test: Steps reorder correctly
- [ ] 109. Run tests — all must pass

**✅ MILESTONE: Full Recipe CRUD (Step 102)**

---

## Phase 5: Timer Service

### 5.1 — Background Timer
- [ ] 110. Create `timer/TimerService.kt` (Foreground Service)
- [ ] 111. Create notification channel
- [ ] 112. Show ongoing notification with time remaining
- [ ] 113. Handle timer completion notification (sound + vibrate)
- [ ] 114. Bind service to CookingViewModel

### 5.2 — Timer Tests
- [ ] 115. Create `unit/TimerServiceTests.kt`
- [ ] 116. Test: Timer schedules correctly
- [ ] 117. Test: Timer cancels on pause
- [ ] 118. Test: Timer completion triggers callback
- [ ] 119. Test: Multiple timers don't conflict
- [ ] 120. Run tests — all must pass

### 5.3 — Timer Polish
- [ ] 121. Timer continues when app backgrounded
- [ ] 122. Timer continues when screen locked
- [ ] 123. Tapping notification returns to Cooking Mode
- [ ] 124. Multiple sequential timers work correctly
- [ ] 125. Timer alarm sound selection (default system)

### 5.4 — Permissions
- [ ] 126. Request notification permission (Android 13+)
- [ ] 127. Request POST_NOTIFICATIONS gracefully
- [ ] 128. Handle permission denied state
- [ ] 129. Add foreground service permission to manifest
- [ ] 130. Test on Android 13+ and older

**✅ MILESTONE: Timers Work (Step 125)**

---

## Phase 6: Navigation + Polish

### 6.1 — Navigation
- [ ] 131. Add Navigation Compose dependency
- [ ] 132. Create `ui/navigation/NavGraph.kt`
- [ ] 133. Routes: Home, EditRecipe, Cooking
- [ ] 134. Home → tap recipe → Cooking Mode
- [ ] 135. Home → FAB → EditRecipe (new)
- [ ] 136. Home → long press recipe → EditRecipe (edit)
- [ ] 137. Cooking → X → Home (with confirmation)

### 6.2 — UI Tests
- [ ] 138. Create `ui/RecipeFlowUITests.kt` (androidTest)
- [ ] 139. Test: Add recipe flow completes
- [ ] 140. Test: Recipe appears in list after adding
- [ ] 141. Test: Tap recipe opens Cooking Mode
- [ ] 142. Test: Next/Back buttons navigate steps
- [ ] 143. Test: Exit confirmation appears
- [ ] 144. Test: Delete removes recipe from list
- [ ] 145. Run UI tests — all must pass

### 6.3 — UI Polish
- [ ] 146. Recipe icons/emojis (auto-assign or pick)
- [ ] 147. Swipe to delete on home screen
- [ ] 148. Loading states
- [ ] 149. Error states
- [ ] 150. Haptic feedback on timer complete
- [ ] 151. Step transition animations
- [ ] 152. Test full flow end-to-end

### 6.4 — Alpha Prep
- [ ] 153. Set versionCode to 1
- [ ] 154. Set versionName to "0.1.0"
- [ ] 155. Run all unit tests — must pass
- [ ] 156. Run all UI tests — must pass
- [ ] 157. Generate signed APK/AAB
- [ ] 158. Upload to internal testing track
- [ ] 159. Smoke test on device

**✅ 🚀 ALPHA — Internal Testing (Step 145)**

---

## Phase 7: Monetization

### 7.1 — Premium Manager
- [ ] 160. Create `data/PremiumManager.kt`
- [ ] 161. Store purchase state in DataStore
- [ ] 162. StateFlow: `isPremium`
- [ ] 163. Implement `fun unlock()`

### 7.2 — Premium Tests
- [ ] 164. Create `unit/PremiumManagerTests.kt`
- [ ] 165. Test: Default state is not premium
- [ ] 166. Test: Unlock sets premium true
- [ ] 167. Test: Premium state persists
- [ ] 168. Test: Recipe limit enforced when not premium
- [ ] 169. Test: Recipe limit removed when premium
- [ ] 170. Run tests — all must pass

### 7.3 — Free Tier Limits
- [ ] 171. Check recipe count before adding (limit: 5)
- [ ] 172. Show upgrade prompt when limit reached
- [ ] 173. Free tier still gets full Cooking Mode

### 7.4 — Paywall Screen
- [ ] 174. Create `ui/paywall/PaywallScreen.kt`
- [ ] 175. Hero image
- [ ] 176. Feature list (unlimited recipes, scaling, etc.)
- [ ] 177. Price display ($4.99 one-time)
- [ ] 178. Purchase button
- [ ] 179. Restore purchases link

### 7.5 — Google Play Billing
- [ ] 180. Add Play Billing library
- [ ] 181. Create `data/BillingManager.kt`
- [ ] 182. Query product details
- [ ] 183. Launch purchase flow
- [ ] 184. Handle purchase success
- [ ] 185. Handle purchase failure
- [ ] 186. Verify purchase on app start
- [ ] 187. Restore purchases flow

### 7.6 — Pro Features
- [ ] 188. Ingredient scaling UI (½×, 1×, 2×, custom)
- [ ] 189. Scaling logic in CookingViewModel
- [ ] 190. Gate scaling behind premium check
- [ ] 191. "Import from clipboard" button (basic text parsing)
- [ ] 192. Gate import behind premium check

### 7.7 — Beta Prep
- [ ] 193. Test purchase flow with test cards
- [ ] 194. Test restore flow
- [ ] 195. Test free tier limits
- [ ] 196. Run all tests — must pass
- [ ] 197. Upload to closed testing track

**✅ 🚀 BETA — Monetization Working (Step 182)**

---

## Phase 8: Launch Prep

### 8.1 — Polish
- [ ] 198. Review all screens match design
- [ ] 199. Test on multiple screen sizes
- [ ] 200. Test on Android 8, 10, 12, 14
- [ ] 201. Fix any crashes
- [ ] 202. Add analytics (Firebase)
- [ ] 203. Add crash reporting (Crashlytics)

### 8.2 — Accessibility
- [ ] 204. Content descriptions on all buttons
- [ ] 205. Test with TalkBack
- [ ] 206. Test with font scaling (all sizes)
- [ ] 207. Ensure timer alerts work with accessibility

### 8.3 — Store Assets
- [ ] 208. App icon (512x512)
- [ ] 209. Feature graphic (1024x500)
- [ ] 210. Screenshots (phone) — at least 4
- [ ] 211. Short description (80 chars)
- [ ] 212. Full description
- [ ] 213. Privacy policy URL
- [ ] 214. Create promo video (optional)

### 8.4 — Play Store Submission
- [ ] 215. Complete store listing
- [ ] 216. Set up pricing (free with IAP)
- [ ] 217. Configure in-app product ($4.99)
- [ ] 218. Content rating questionnaire
- [ ] 219. Data safety form
- [ ] 220. Target audience declaration
- [ ] 221. Run all tests one final time
- [ ] 222. Upload release AAB
- [ ] 223. Submit for review
- [ ] 224. Address any review feedback
- [ ] 225. Release to production
- [ ] 226. Announce launch
- [ ] 227. Monitor reviews + crashes
- [ ] 228. Plan v1.1 based on feedback

**✅ 🎉 LAUNCH (Step 215)**

---

## Summary

| Week | Goal | Steps | Release |
|------|------|-------|---------|
| 1 | Core + Cooking Mode + Tests | 1-159 | Alpha (Internal) |
| 2 | Monetization + Polish | 160-197 | Beta (Closed) |
| 2-3 | Launch | 198-228 | Production |

**Total: 228 steps**

---

## Test Coverage Summary

| Area | Unit Tests | UI Tests |
|------|------------|----------|
| Models | Recipe, Ingredient, Step | — |
| Repository | CRUD operations | — |
| Cooking Mode | State machine, navigation | Step flow |
| Recipe CRUD | Validation, reordering | Add/edit/delete flow |
| Timer | Schedule, cancel, complete | — |
| Premium | Limits, unlock, persist | — |
| Full Flow | — | End-to-end |

---

## Key Dependencies

```kotlin
dependencies {
    // Compose
    implementation(platform("androidx.compose:compose-bom:2024.01.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose")
    implementation("androidx.navigation:navigation-compose")
    
    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    
    // Hilt
    implementation("com.google.dagger:hilt-android:2.50")
    ksp("com.google.dagger:hilt-compiler:2.50")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    
    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    
    // Billing
    implementation("com.android.billingclient:billing-ktx:6.1.0")
    
    // Images
    implementation("io.coil-kt:coil-compose:2.5.0")
    
    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("io.mockk:mockk:1.13.9")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("app.cash.turbine:turbine:1.0.0")
    testImplementation("androidx.room:room-testing:2.6.1")
    
    // UI Testing
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
```

---

## Post-Launch Features (v1.1+)

- Voice control ("next step", "start timer")
- Import from URL (parse recipe websites)
- Tablet / landscape Cooking Mode
- Wear OS timer companion
- Widget showing active timer
- Recipe sharing (export to text/PDF)
- Collections / folders
- Cloud sync (requires backend)
