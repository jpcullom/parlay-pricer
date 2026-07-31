# ParlayPricer — Correlated Monte Carlo

A quantitative pricing engine for Same Game Parlay (SGP) analysis. Estimates true joint probabilities using correlated Monte Carlo simulations, computes fair odds via Cholesky decomposition, and identifies mispriced parlay markets.

## What This Solves

Sportsbooks price SGPs assuming player outcomes are **independent**:

```
P(Cousins Over 275 AND London Over 75) = P(Cousins) × P(London) = 0.60 × 0.55 = 33%
```

But a QB throwing a lot **causes** his receivers to accumulate yards — they're correlated. The true joint probability is higher (~38%), which means the sportsbook is underpricing the parlay. This API finds those mispricings.

## Tech Stack

- **Java 21** / **Spring Boot 3.3**
- **PostgreSQL 16** — game logs, player statistics, correlations
- **Redis 7** — correlation caching
- **Apache Commons Math** — Pearson correlation, Cholesky decomposition
- **ESPN API** — real-world NFL player game log data
- **Flyway** — database migrations
- **springdoc-openapi** — Swagger UI

---

## How It Works

### 1. Data Ingestion (ESPN API)

The ESPN client fetches real game-by-game player stats:

```
POST /api/v1/data/ingest?team=ATL&season=2024
```

This pulls every skill-position player on the roster, fetches their game logs, and stores rows like:

| game_id | player | market | stat_value |
|---------|--------|--------|-----------|
| ATL-WK1 | Kirk Cousins | PASSING_YARDS | 315 |
| ATL-WK1 | Drake London | RECEIVING_YARDS | 105 |
| ATL-WK1 | Bijan Robinson | RUSHING_YARDS | 78 |

### 2. Correlation Computation (Pearson's r)

After ingestion, rebuild correlations from the raw game log data:

```
POST /api/v1/correlations/rebuild
```

For every pair of (player, market) combinations that share at least 5 games, we compute the Pearson correlation coefficient:

$$r = \frac{\sum (x_i - \bar{x})(y_i - \bar{y})}{\sqrt{\sum(x_i - \bar{x})^2} \cdot \sqrt{\sum(y_i - \bar{y})^2}}$$

**Example** — given 12 games of Cousins passing and London receiving:

```
Cousins: [315, 185, 275, 340, 220, 330, 165, 285, 298, 305, 198, 352]
London:  [105,  42,  82, 118,  58, 112,  35,  88,  92,  95,  48, 125]
```

When Cousins is above his average (272 yds), London is almost always above his average (83 yds). Result: **r ≈ 0.95** (strong positive correlation).

| r value | Meaning | Example |
|---------|---------|---------|
| +0.9 | Very strong positive | QB passing ↔ WR receiving |
| +0.5 | Moderate positive | RB rushing ↔ RB touchdowns |
| 0.0 | No relationship | QB passing ↔ opposing RB |
| -0.7 | Strong negative | QB passing yards ↔ RB rushing yards (pass-heavy vs run-heavy) |

### 3. Monte Carlo Simulation (Cholesky Decomposition)

The simulation generates 100,000 "fake games" where player stats are **correlated** — not independent.

#### The Problem

Generating random numbers is easy. Generating random numbers that **move together** with a specific correlation requires linear algebra.

#### The Solution: Cholesky Decomposition

Given a correlation matrix:

$$C = \begin{bmatrix} 1.0 & 0.95 \\\\ 0.95 & 1.0 \end{bmatrix}$$

The Cholesky decomposition finds a lower-triangular matrix **L** such that:

$$C = L \times L^T$$

$$L = \begin{bmatrix} 1.0 & 0 \\\\ 0.95 & 0.312 \end{bmatrix}$$

#### The Simulation Loop

```
wins = 0

for i in 1..100,000:
    // 1. Generate INDEPENDENT random numbers (standard normal)
    z = [random(), random()]  →  e.g., [0.85, -0.32]

    // 2. Make them CORRELATED by multiplying by L
    correlated[0] = 1.0 × 0.85  + 0 × (-0.32)    = 0.85
    correlated[1] = 0.95 × 0.85 + 0.312 × (-0.32) = 0.71

    // 3. Scale to real-world yards (mean + stdDev × correlated)
    cousinsYards = 272 + 58 × 0.85 = 321.3
    londonYards  =  83 + 28 × 0.71 = 102.9

    // 4. Check: did both legs hit?
    cousins > 275.5? → YES ✓
    london  > 75.5?  → YES ✓
    wins++

probability = wins / 100,000 = 0.3827  (38.27%)
```

Because the random numbers are **correlated**, both "Over" bets hit simultaneously more often than independent math predicts — exposing sportsbook mispricing.

### 4. Expected Value Calculation

Once we have the fair probability:

$$EV = (P_{fair} \times decimal_{sportsbook}) - 1$$

**Example:**
- Fair probability: 38.27% → fair odds: +161
- Sportsbook offers: +350 (decimal 4.50)
- EV = (0.3827 × 4.50) - 1 = **+0.72**

That's $0.72 profit per $1 wagered over the long run.

---

## Prerequisites

- **Java 21** — `brew install openjdk@21`
- **Docker & Docker Compose** — for PostgreSQL and Redis

Set `JAVA_HOME` to JDK 21:

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home
```

## Getting Started

### 1. Start PostgreSQL and Redis

```bash
docker compose up -d
```

### 2. Build and Run

```bash
./mvnw clean spring-boot:run
```

The app starts on **http://localhost:8080**. Flyway runs migrations on first startup.

### 3. Ingest Real Data

```bash
# Pull 2024 Falcons game logs from ESPN
curl -X POST "http://localhost:8080/api/v1/data/ingest?team=ATL&season=2024"

# Compute correlations from the data
curl -X POST http://localhost:8080/api/v1/correlations/rebuild
```

### 4. Query Results

```bash
# Look up Cousins ↔ London correlation
curl "http://localhost:8080/api/v1/correlations?playerA=Kirk%20Cousins&playerB=Drake%20London&marketA=PASSING_YARDS&marketB=RECEIVING_YARDS"

# Full Falcons correlation matrix
curl http://localhost:8080/api/v1/teams/ATL/correlations

# Best correlated parlay pairs
curl http://localhost:8080/api/v1/parlays/best
```

### 5. Open Swagger UI

Browse to **http://localhost:8080/swagger-ui.html** for interactive API docs.

---

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/v1/data/ingest` | Ingest player game logs from ESPN for a team/season |
| `POST` | `/api/v1/correlations/rebuild` | Recompute all correlations from game log data |
| `GET`  | `/api/v1/correlations` | Look up correlation between two players |
| `GET`  | `/api/v1/teams/{team}/correlations` | Full NxN correlation matrix for a team |
| `GET`  | `/api/v1/parlays/best` | Top most strongly correlated player pairs |
| `POST` | `/api/v1/parlays/analyze` | Analyze a parlay for correlation, fair odds, and EV |
| `POST` | `/api/v1/ev` | Calculate expected value from probability + sportsbook odds |

### Example: Analyze a Parlay

```bash
curl -X POST http://localhost:8080/api/v1/parlays/analyze \
  -H "Content-Type: application/json" \
  -d '{
    "legs": [
      {
        "player": "Kirk Cousins",
        "market": "PASSING_YARDS",
        "line": 275.5,
        "selection": "OVER"
      },
      {
        "player": "Drake London",
        "market": "RECEIVING_YARDS",
        "line": 75.5,
        "selection": "OVER"
      }
    ],
    "sportsbookOdds": 350
  }'
```

### Example: Calculate Expected Value

```bash
curl -X POST http://localhost:8080/api/v1/ev \
  -H "Content-Type: application/json" \
  -d '{"fairProbability": 0.3827, "sportsbookOdds": 350}'
```

**Response:** `{ "fairOdds": 161, "expectedValue": 0.72 }`

---

## Project Structure

```
src/main/java/com/sgpanalyzer/
├── SgpAnalyzerApplication.java
├── client/
│   ├── EspnClient.java                # ESPN API — teams, rosters, game logs
│   ├── OddsApiClient.java             # The Odds API (TODO)
│   ├── SportsDataClient.java          # API-Sports (TODO)
│   └── dto/espn/                      # ESPN response DTOs
├── config/
│   ├── OpenApiConfig.java             # Swagger UI metadata
│   ├── RedisConfig.java               # Cache manager (1hr TTL)
│   └── SchedulingConfig.java          # @Scheduled support
├── controller/
│   ├── CorrelationController.java     # GET /correlations, POST /rebuild
│   ├── DataIngestionController.java   # POST /data/ingest
│   ├── ExpectedValueController.java   # POST /ev
│   ├── ParlayController.java          # POST /parlays/analyze, GET /best
│   └── TeamController.java            # GET /teams/{team}/correlations
├── exception/
│   ├── GlobalExceptionHandler.java    # 400/404/500 mapping
│   └── ResourceNotFoundException.java
├── model/
│   ├── dto/                           # Request/response objects
│   ├── entity/                        # JPA entities (GameLog, PlayerCorrelation, PlayerStatistic)
│   └── enums/                         # Market, Selection, Recommendation
├── repository/                        # Spring Data JPA
└── service/
    ├── CorrelationRebuildScheduler.java  # Nightly @Scheduled job
    ├── CorrelationService.java           # Pearson computation, matrix building
    ├── DataIngestionService.java         # ESPN → GameLog pipeline
    ├── ExpectedValueService.java         # Odds ↔ probability, EV formula
    ├── MonteCarloSimulationService.java  # Cholesky + 100k simulations
    └── ParlayAnalysisService.java        # Orchestrator: correlation → sim → EV
```

## Configuration

Key settings in `src/main/resources/application.yml`:

| Property | Default | Description |
|----------|---------|-------------|
| `server.port` | `8080` | Server port |
| `sgp.simulation.default-iterations` | `100000` | Monte Carlo simulation runs |
| `sgp.correlation.rebuild-cron` | `0 0 3 * * *` | Nightly rebuild (3 AM) |
| `sgp.correlation.cache-ttl-hours` | `1` | Redis TTL for correlation lookups |

## Useful Commands

```bash
# Compile
./mvnw clean compile

# Run tests
./mvnw test

# Package as JAR
./mvnw clean package -DskipTests
java -jar target/sgp-analyzer-0.0.1-SNAPSHOT.jar

# Build Docker image
docker build -t parlay-pricer .

# Stop infrastructure
docker compose down
```
