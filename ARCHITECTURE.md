# SkillSwap Architecture & OOP Design Note

## 1. Executive Summary
**SkillSwap** is a zero-money peer learning and skill barter marketplace implemented purely in modern **Java 17+** using only standard library APIs (`com.sun.net.httpserver.HttpServer`). It features zero runtime external dependencies and enforces strict Object-Oriented Programming (OOP) paradigms.

---

## 2. Core OOP Principles Applied

### A. Encapsulation
- **Data Hiding**: Fields across all domain models (`Member`, `Skill`, `SwapLink`, `SwapProposal`, `Rating`) are `private` or `private final`. State is accessed and mutated strictly through validated domain methods.
- **Defensive Copying & Immutability**:
  - `Skill` is an immutable record-like class.
  - `SwapCycle` wraps links in `Collections.unmodifiableList()`.
  - `Member.getTeaches()` and `Member.getWants()` return defensive snapshot sets wrapped in `LinkedHashSet` to protect thread safety and prevent external rep-exposure.
  - Sensitive information (`pinSalt`, `pinHash`, contact information) is hidden and exposed only upon explicit authorized state (e.g., contact reveal only when all participants accept).

### B. Abstraction & Interface-Driven Design
The system cleanly separates contracts from implementation details:
- **`SwapFinder`**: Abstraction for barter cycle discovery engines.
  - Implementations: `DirectSwapFinder` (2-way), `CycleSwapFinder` (depth-limited DFS 2..N-way).
- **`ScoringStrategy`**: Abstraction for evaluating and ranking barter cycles.
  - Implementations: `ShortestFirstScoring`, `AvailabilityScoring`, `RatingScoring`, `CompositeScoring`.
- **`MemberRepository` & `ProposalRepository`**: Abstractions for entity persistence.
  - Decouples business services (`SwapService`, `AuthService`) from filesystem storage mechanics.

### C. Polymorphism
- Scoring algorithms are invoked polymorphically via `ScoringStrategy.score(cycle)`.
- Route handling leverages `HttpHandler.handle(exchange)` where `HomeHandler`, `AuthHandler`, `ProfileHandler`, `SwapDiscoveryHandler`, and `ProposalHandler` implement polymorphic request dispatching.

### D. Design Patterns Used
1. **Strategy Pattern**:
   - `ScoringStrategy` encapsulates exchange ranking logic. Allows swappable heuristics (e.g. prioritizing shorter cycles vs prioritizing high teacher reviews).
2. **Composite Pattern**:
   - `CompositeScoring` implements `ScoringStrategy` while aggregating multiple weighted `ScoringStrategy` instances, delegating to each and computing a normalized weighted mean.
3. **Repository Pattern**:
   - `MemberRepository` and `ProposalRepository` provide clean collection-oriented persistence facades with thread-safe in-memory caching and JSON file-backed persistence (`data/members.json`, `data/proposals.json`).
4. **State Machine / Lifecycle Transition**:
   - `SwapProposal` implements the finite-state machine transitions:
     `PENDING` -> `ACTIVE` (100% consensus) -> `COMPLETED`
     `PENDING` -> `EXPIRED` (past 48 hours)
     `PENDING` / `ACTIVE` -> `CANCELLED` (any decline or cancellation)

---

## 3. Algorithm: Depth-Limited DFS Barter Cycle Discovery

### A. Directed Barter Graph Construction
An exchange graph is constructed where members are nodes:
- A directed edge `Learner -> Teacher` with skill `S` exists if and only if:
  1. `Teacher` teaches skill `S`.
  2. `Learner` desires skill `S`.
  3. **Level Compatibility Rule**:
     - `BEGINNER` learner requires `INTERMEDIATE` or `ADVANCED` teacher.
     - `INTERMEDIATE` learner requires `INTERMEDIATE` or `ADVANCED` teacher.
     - `ADVANCED` learner requires `ADVANCED` teacher.
  4. Neither `Learner` nor `Teacher` is currently locked in an `ACTIVE` proposal.
  5. `Learner != Teacher` (no self-loops).

### B. Cycle Finding Traversal
```
Depth-Limited DFS:
1. Start at logged-in member M_0 with depth limit N (default 4).
2. Maintain:
   - path: list of visited members
   - links: sequence of directed SwapLink edges
   - visitedSet: member IDs in current recursion branch
3. At current member M_curr:
   - Iterate outgoing edges (M_curr learns S from M_next):
     - If M_next == M_0 and links.size() + 1 in [2..N]:
       -> Cycle closed! Normalize rotation and record.
     - Else if path.size() < N and M_next not in visitedSet and not M_next.isLocked():
       -> Recurse with M_next
       -> Backtrack
```

### C. Cycle Normalization & Deduplication
In cyclic barter, `A -> B -> C -> A` and `B -> C -> A -> B` represent the exact same mutual exchange.
To eliminate duplicate rotations:
1. Find the link whose learner has the lexicographically smallest `memberId`.
2. Rotate the cycle array so this link starts at index 0.
3. Generate a canonical string signature: `id0-[:skill0]->id1;id1-[:skill1]->id2;...`
4. Deduplicate using a `Set<String>` of canonical signatures.

---

## 4. Concurrency & Security Architecture
- **Thread Safety**: All state-mutating service methods (`acceptProposal`, `declineProposal`, `completeProposal`) synchronize on shared proposal instances or the service monitor, preventing concurrent accept race conditions.
- **Two-Phase Locking**:
  - When a proposal achieves all-member consensus, every participating member is atomically marked as locked (`member.lockInProposal(proposalId)`).
  - A locked member is automatically excluded from graph cycle construction, guaranteeing students cannot be double-booked across conflicting exchanges.
- **XSS Prevention**: Every user-provided string rendered into HTML is filtered through `SecurityUtil.escapeHtml()`.
- **PIN Hashing**: Passwords/PINs are salted with cryptographically secure pseudo-random bytes (`SecureRandom`) and hashed with `SHA-256`.
