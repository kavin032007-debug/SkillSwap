# 🔄 SkillSwap &mdash; Money-Free Skill Barter Marketplace

> Built with **Pure Java 17+ (OOP)**, standard `com.sun.net.httpserver.HttpServer`, and **zero external runtime dependencies**.  
> Features algorithmic 2-way, 3-way, and 4-way barter cycle discovery, depth-limited DFS, dynamic inline SVG visualizations, and robust state locking.

---

## 🌟 Overview
College students possess valuable skills (coding, musical instruments, languages, photography, design) but often lack disposable income. **SkillSwap** is a direct and multi-way barter exchange platform where students trade knowledge without money.

While direct barter often fails due to the *double coincidence of wants* (Asha has what Ravi wants, but Ravi doesn't have what Asha wants), SkillSwap's graph engine discovers **3-way and 4-way circular exchanges**:
> **Asha** learns Excel from **Ravi**, **Ravi** learns Photography from **Meena**, and **Meena** learns Guitar from **Asha**.

---

## 🚀 Key Features

| Feature | Description |
| :--- | :--- |
| **F1. Authentication** | PIN-based login with cryptographic salting and SHA-256 hashing. Cookie-based session tokens. |
| **F2. Skill Profiles** | Add/remove skills offered (*Teaches*) and desired (*Wants*), with category, level (`BEGINNER`, `INTERMEDIATE`, `ADVANCED`), and weekly availability hours. |
| **F3. Cycle Discovery** | Depth-limited DFS algorithm discovers 2-way direct and 3/4-way cyclic swaps involving the logged-in student. Renders interactive inline **SVG barter flow diagrams**. |
| **F4. Swap Proposals** | Propose trades. All members in the cycle must `ACCEPT`. Any `DECLINE` cancels the proposal. Proposals expire automatically after 48 hours. |
| **F5. Privacy Lock & Reveal** | Contacts remain strictly hidden while a proposal is pending. Upon 100% mutual acceptance, student contact info and schedule notes are revealed. |
| **F6. Peer Ratings** | Following swap completion, students review their teachers with 1–5 stars and written feedback. Ratings directly calibrate match ranking. |
| **F7. Almost-Matches** | *Stretch feature*: Detects skills that, if added to your desired list, would immediately unlock new barter exchanges. |

---

## 🏗️ Pure Java OOP Architecture

- **Zero Runtime Dependencies**: Uses exclusively the standard Java 17 runtime (`com.sun.net.httpserver`, `java.util.concurrent`, `java.security`).
- **Encapsulation & Immutability**: Domain entities (`Skill`, `Member`, `SwapLink`, `SwapCycle`, `SwapProposal`) enforce defensive copying and validated state transitions.
- **Strategy & Composite Patterns**: Pluggable scoring engine (`ShortestFirstScoring`, `RatingScoring`, `AvailabilityScoring`, `CompositeScoring`).
- **Repository Pattern**: Thread-safe memory caches backed by file persistence (`data/members.json`, `data/proposals.json`).
- **Two-Phase Concurrency Locking**: Active participants are locked to prevent conflicting simultaneous barter commitments.
- See [`ARCHITECTURE.md`](file:///Users/kavin/Desktop/SKILL%20SWAP/ARCHITECTURE.md) for deep technical design notes.

---

## ⚡ Quickstart

### 1. Build and Run Locally
```bash
# 1. Compile and package the standalone executable JAR
./build.sh

# 2. Run automated test suite
./test.sh

# 3. Launch the server (defaults to port 8080)
./run.sh
```
Open **[http://localhost:8080](http://localhost:8080)** in your web browser.

### 2. Configure Port
```bash
PORT=9000 ./run.sh
```

### 3. Build & Test with Maven (Optional)
```bash
mvn clean package
mvn test
java -jar target/skillswap.jar
```

### 4. Deploy with Docker (Render / Railway / Cloud Run)
```bash
# Build Docker image
docker build -t skillswap .

# Run container
docker run -p 8080:8080 skillswap
```

---

## 👥 Pre-Seeded Demo Accounts
SkillSwap automatically seeds **12 diverse students** on first startup with pre-configured overlapping barter cycles:

| Student | Teaches | Wants to Learn | Demo Role |
| :--- | :--- | :--- | :--- |
| **Asha** | Guitar *(Adv)*, Python *(Int)* | Excel *(Beg)*, UI Design *(Beg)* | **3-Way Cycle** (with Ravi & Meena); 2-way with Carlos |
| **Ravi** | Excel *(Adv)*, Data Analysis *(Int)* | Photography *(Beg)*, Spanish *(Beg)* | **3-Way Cycle** (teaches Asha, learns from Meena) |
| **Meena** | Photography *(Adv)*, Spanish *(Int)*| Guitar *(Beg)*, Chess *(Beg)* | **3-Way Cycle** (teaches Ravi, learns from Asha) |
| **Carlos**| UI Design *(Adv)*, Figma *(Int)* | Python *(Beg)* | **2-Way Direct Swap** with Asha |
| **Diana** | French *(Adv)*, Writing *(Int)* | Data Analysis *(Beg)* | Multi-way swaps with Evan & Ravi |
| **Evan**  | Chess *(Adv)*, Calculus *(Int)* | French *(Beg)* | Multi-way swaps with Diana & Meena |

> 🔑 **Default Demo PIN for all accounts**: `1234`  
> You can also use the **1-Click Quick Demo Login** buttons on the landing page!

---

## 🌐 Routes & Endpoints

| Method | Route | Description |
| :--- | :--- | :--- |
| `GET` | `/` | Home landing page with platform explanation and stats |
| `GET` | `/signup` / `POST /signup` | Student registration with PIN hashing |
| `GET` | `/login` / `POST /login` | Authenticate and issue secure session cookie |
| `POST`| `/logout` | Invalidate active session |
| `GET` | `/profile` | View profile and manage taught/wanted skills |
| `POST`| `/profile/skills/add` | Add skill with level and category |
| `POST`| `/profile/skills/remove` | Remove skill |
| `POST`| `/profile/availability` | Update weekly barter hours and notes |
| `GET` | `/swaps` | Ranked barter discovery with inline SVG cycle diagrams & almost-matches |
| `POST`| `/proposals` | Propose an exchange |
| `GET` | `/proposals` | View active/pending proposals and revealed contact info |
| `POST`| `/proposals/{id}/accept` | Accept proposal |
| `POST`| `/proposals/{id}/decline` | Decline and cancel proposal |
| `POST`| `/proposals/{id}/cancel` | Cancel own pending proposal |
| `POST`| `/proposals/{id}/complete` | Mark active swap completed |
| `POST`| `/ratings` | Submit peer review for teacher |
| `GET` | `/members` | Community member directory |
| `POST`| `/demo/reset` | Reseed clean demo data |

---

## 🧪 Automated Testing
Run `./test.sh` to execute the full unit test suite without needing Maven:
- **`CycleFinderTest`**: 2-way direct, 3-way circular, disjoint skills, rotation deduplication, level compatibility rules.
- **`ProposalLifecycleTest`**: Creation, consensus state machine, locking prevention, decline cancellation, 48-hour expiration, contact confidentiality.
- **`ScoringStrategyTest`**: Shortest-first weighting, availability harmonizing, peer review reputation scoring, composite strategies.
