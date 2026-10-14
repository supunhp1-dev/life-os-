# LifeOS — Personal Life Planner & Organizer (Android + Room SQLite)

A calm, privacy-first personal operating system for Android that brings together weekly planning, time-blocked events, to-dos, personal finance, bills, and life records, powered by a local Room SQLite database and styled with an organic Sage & Moss aesthetic.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> The following decisions were confirmed during interactive planning:

- **Full LifeOS Suite**: Implement the four core modules in mobile navigation:
  1. **Plan & Calendar**: Day timeline, week strip, timed events with subtasks, and category tags.
  2. **Tasks & To-Dos**: Prioritized to-do list with due dates, costs, subtasks, and quick completion.
  3. **Money & Costs**: Monthly budget tracker, bill tracking with recurring cycles and partial payments, expense log, and spending trends.
  4. **Life Records (Vault)**: Important warranties, insurance, documents, and renewal deadlines with upcoming expiry indicators.
- **Visual Aesthetic (Sage & Moss)**: Calming, organic theme inspired by paper planners:
  - Warm Paper background (`#F8F7F3`)
  - Deep Forest Green primary accent (`#385B49`)
  - Soft Sage containers (`#E3EDE5`) and Muted Ink typography (`#26342C`)
  - Category pastel accents: Work (Sage Green), Health (Rose Terracotta), Home (Warm Amber), Personal (Soft Sky Blue), Study (Lavender).
- **Initial Starter Data**: Pre-populate the SQLite database on first launch with realistic sample events, tasks, bills, and warranties so the user can immediately experience the full planner.

---

## 1. Overview & Core Concept

- **What It Does**: A complete, native Android personal planning application that organizes daily schedules, actionable tasks, household expenses & recurring bills, and critical document records in one unified offline SQLite database.
- **Target Audience**: Individuals who want an intentional, thoughtful planner without intrusive cloud sync, accounts, or subscription paywalls.
- **Key Value**: 100% offline, reactive with Kotlin Coroutines and Room `Flow`, instant startup, and zero data leakage.

---

## 2. User Experience & Visual Design

### Key User Flows

1. **Daily & Weekly Planning**:
   - The user opens the **Plan** screen to see their weekly horizon, current week intention banner, and daily timeline with hour blocks (7 AM – 10 PM).
   - Tapping an hour slot or "+ Add Event" opens a bottom sheet to set time, category, planned costs, recurrence, and event-specific subtasks.
   - Conflicting events are visually detected and flagged.

2. **Tasks & Actionable To-Dos**:
   - The **Tasks** tab lets the user filter between Open, Completed, and All tasks, or sort by due date, priority (High / Medium / Low), or cost.
   - One-tap checkbox marks a task done with haptic feedback and celebratory visual cues.
   - Tasks can be standalone or linked to calendar events.

3. **Money & Expense Management**:
   - The **Money** screen features a visual Monthly Budget progress card showing spent vs. remaining budget.
   - **Bills & Due Dates**: Tracks upcoming bills with "Mark Paid" (which automatically records an expense and rolls recurring bills forward to the next cycle) and "Partial Payment".
   - **Expense Log**: Instant recording of day-to-day purchases with category tags and 6-month spending trends.

4. **Life Records (Vault)**:
   - The **Vault** holds warranties, insurance policies, identification records, and equipment service logs.
   - Color-coded badges highlight items that are "Expired" (Red), "Due Soon" (Amber, < 30 days), or "Active" (Green).

5. **Universal Quick-Add & Search**:
   - A floating action button (FAB) or top bar icon allows quick entry of Events, Tasks, Expenses, Bills, or Vault Records.
   - Top search bar provides instant querying across all Room tables.

### Visual Identity & Theme Tokens

- **Background**: `#F8F7F3` (Paper) / Dark Mode `#1A1D1A`
- **Surface**: `#FFFFFF` (Card) / Dark Mode `#242824`
- **Primary / Accent**: `#385B49` (Deep Moss Green)
- **Primary Container**: `#E3EDE5` (Soft Sage)
- **Text On Surface**: `#26342C` (Ink)
- **Muted Text**: `#788279`
- **Category Colors**:
  - Work: `#6D9072` (Forest Sage)
  - Personal: `#5B7B9E` (Slate Blue)
  - Health: `#C27A70` (Terracotta Rose)
  - Home: `#C69E5E` (Warm Sand Amber)
  - Study: `#8E7CB0` (Muted Lavender)

---

## 3. Key Product Decisions & Trade-Offs

- **Room Persistence vs. Raw SQLite / SharedPreferences**:
  - *Chosen*: Room with Kotlin Symbol Processing (KSP) and Kotlin `Flow`.
  - *Why*: Compile-time query verification, automatic background threading via `suspend` functions, and direct integration with Jetpack Compose `collectAsStateWithLifecycle()`.
- **Single Database with Specialized DAOs**:
  - *Chosen*: A centralized `LifeOSDatabase` housing entities for `events`, `tasks`, `expenses`, `bills`, `vault_records`, and `app_settings`.
  - *Why*: Enables foreign relationships (e.g. tasks linked to events, expenses generated from bills) and cross-table search.
- **Navigation Architecture**:
  - *Chosen*: Material 3 `NavigationBar` with 4 canonical destinations (`Plan`, `Tasks`, `Money`, `Vault`), with smooth state retention and clean back-handling.
- **Edge-to-Edge & Accessibility**:
  - *Chosen*: `enableEdgeToEdge()` with strict `WindowInsets.safeDrawing` handling and minimum 48.dp interactive touch targets.

---

## 4. Technical Architecture & Data Strategy

### System Architecture Diagram

```
┌──────────────────────────────────────────────────────────────────┐
│                   LifeOS Jetpack Compose UI                      │
├─────────────────┬────────────────┬───────────────┬───────────────┤
│   PlanScreen    │  TasksScreen   │  MoneyScreen  │  VaultScreen  │
│(Timeline/Events)│(To-Dos/Filter) │(Budget/Bills) │(Warranties/Doc│
└────────┬────────┴────────┬───────┴───────┬───────┴───────┬───────┘
         │                 │               │               │
         ▼                 ▼               ▼               ▼
┌──────────────────────────────────────────────────────────────────┐
│              LifeOSViewModel (StateFlow & Intent Handlers)       │
└────────────────────────────────┬─────────────────────────────────┘
                                 │
                                 ▼
┌──────────────────────────────────────────────────────────────────┐
│              LifeOSRepository (Clean Data Abstraction)           │
└────────────────────────────────┬─────────────────────────────────┘
                                 │
                                 ▼
┌──────────────────────────────────────────────────────────────────┐
│               Room SQLite Database (KSP Generated)               │
├───────────────┬──────────────┬───────────────┬───────────────────┤
│   EventDao    │   TaskDao    │  FinanceDao   │     VaultDao      │
│ (Events/Repeat│(Tasks/Status)│(Bills/Expenses│(Warranties/Records│
└───────────────┴──────────────┴───────────────┴───────────────────┘
```

### Database Entities & Schema

1. **`EventEntity`**:
   - `id`: Long (Auto-generated Primary Key)
   - `title`: String
   - `date`: String (`YYYY-MM-DD`)
   - `startTime`: String (`HH:mm`)
   - `endTime`: String (`HH:mm`)
   - `category`: String ("Work", "Personal", "Health", "Home", "Study")
   - `description`: String
   - `location`: String
   - `plannedCost`: Double
   - `repeat`: String ("none", "daily", "weekly", "monthly")
   - `isAllDay`: Boolean
   - `tags`: List<String> (via `TypeConverter`)

2. **`TaskEntity`**:
   - `id`: Long (Auto-generated Primary Key)
   - `title`: String
   - `date`: String (`YYYY-MM-DD`)
   - `time`: String (`HH:mm`, optional)
   - `priority`: String ("High", "Medium", "Low")
   - `isDone`: Boolean
   - `cost`: Double
   - `eventId`: Long? (optional link to `EventEntity`)
   - `notes`: String
   - `tags`: List<String>

3. **`BillEntity`**:
   - `id`: Long (Auto-generated Primary Key)
   - `title`: String
   - `amount`: Double
   - `paidAmount`: Double
   - `due`: String (`YYYY-MM-DD`)
   - `isPaid`: Boolean
   - `repeat`: String ("none", "weekly", "monthly", "yearly")
   - `category`: String

4. **`ExpenseEntity`**:
   - `id`: Long (Auto-generated Primary Key)
   - `title`: String
   - `date`: String (`YYYY-MM-DD`)
   - `amount`: Double
   - `category`: String
   - `notes`: String
   - `billId`: Long? (optional link to `BillEntity`)

5. **`VaultEntity`**:
   - `id`: Long (Auto-generated Primary Key)
   - `title`: String
   - `type`: String ("Warranty", "Document", "Insurance", "Health", "Asset")
   - `purchaseDate`: String
   - `expiryDate`: String
   - `reminderDays`: Int
   - `notes`: String
   - `tags`: List<String>

6. **`SettingsEntity`**:
   - `key`: String (Primary Key)
   - `value`: String (e.g. `currency="LKR"`, `monthlyBudget="100000"`, `weekIntention="A little progress, with room to breathe."`)

---

## 5. Implementation Roadmap

1. **Database & Persistence Layer**:
   - Create Room entities, TypeConverters (for `List<String>`), and DAOs (`EventDao`, `TaskDao`, `FinanceDao`, `VaultDao`, `SettingsDao`).
   - Create `LifeOSDatabase` with fallback to destructive migration and asynchronous seed callback on first creation.
   - Build `LifeOSRepository` exposing unified data streams.
2. **Theme & Design System**:
   - Configure Sage & Moss palette in `Color.kt` and `Theme.kt`.
   - Setup M3 typography with accessible sizing.
3. **ViewModel & State Management**:
   - Implement `LifeOSViewModel` exposing UI states: selected date, week view data, filtered tasks, bills & expense metrics, and search queries.
   - Implement CRUD operations and business logic (e.g., recurring bill auto-advancement on full payment).
4. **UI Implementation**:
   - **PlanScreen**: Week horizon carousel, daily timeline layout, event cards with category pills.
   - **TasksScreen**: Filter chips (Open/Completed), priority indicators, task completion toggles.
   - **MoneyScreen**: Monthly budget indicator, bill status cards with "Pay" & "Partial Pay" buttons, expense list.
   - **VaultScreen**: Warranty & document cards with countdown badges (due soon / expired).
   - **QuickAddDialog**: Unified modal for quickly saving any record into Room.
   - **SearchDialog**: Search across events, tasks, bills, and vault records.
5. **Verification**:
   - Compile applet with `compile_applet` and verify zero compiler or Room KSP errors.
   - Confirm layout responsiveness and touch targets.
