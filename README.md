# SGP Analyzer

A quantitative pricing engine for Same Game Parlay (SGP) analysis. Computes player correlations from historical NFL data, runs Monte Carlo simulations, and calculates fair odds + expected value against sportsbook prices.

## Tech Stack

- **Java 21** / **Spring Boot 3.3**
- **PostgreSQL 16** — player stats, correlations, game logs
- **Redis 7** — correlation caching
- **Apache Commons Math** — Pearson correlation, statistical distributions
- **Flyway** — database migrations
- **springdoc-openapi** — Swagger UI

## Prerequisites

- **Java 21** — `brew install openjdk@21`
- **Docker & Docker Compose** — for PostgreSQL and Redis

Set `JAVA_HOME` to JDK 21 (required for Lombok compatibility):

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home
```

> **Tip:** Add this to your `~/.zshrc` so it persists across terminal sessions.

## Getting Started

### 1. Start PostgreSQL and Redis

```bash
docker compose up -d
```

This starts:
- PostgreSQL on `localhost:5432` (db: `sgp_analyzer`, user: `sgp`)
- Redis on `localhost:6379`

### 2. Build and Run

```bash
./mvnw clean spring-boot:run
```

The app starts on **http://localhost:8080**. Flyway automatically runs the database migration on first startup.

### 3. Open Swagger UI

Browse to **http://localhost:8080/swagger-ui.html** to see all endpoints and try them interactively.

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/v1/parlays/analyze` | Analyze a same-game parlay for correlation, fair odds, and EV |
| `GET`  | `/api/v1/parlays/best` | Get the top most strongly correlated player pairs |
| `POST` | `/api/v1/ev` | Calculate expected value from fair probability and sportsbook odds |
| `GET`  | `/api/v1/correlations` | Look up correlation between two players |
| `GET`  | `/api/v1/teams/{team}/correlations` | Get the full correlation matrix for a team |

### Example: Analyze a Parlay

```bash
curl -X POST http://localhost:8080/api/v1/parlays/analyze \
  -H "Content-Type: application/json" \
  -d '{
    "legs": [
      {
        "player": "Patrick Mahomes",
        "market": "PASSING_YARDS",
        "line": 275.5,
        "selection": "OVER"
      },
      {
        "player": "Travis Kelce",
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
  -d '{
    "fairProbability": 0.3827,
    "sportsbookOdds": 350
  }'
```

### Example: Look Up Correlation

```bash
curl "http://localhost:8080/api/v1/correlations?playerA=Patrick%20Mahomes&playerB=Travis%20Kelce&marketA=PASSING_YARDS&marketB=RECEIVING_YARDS"
```

## Project Structure

```
src/main/java/com/sgpanalyzer/
├── SgpAnalyzerApplication.java        # Entry point
├── client/                            # External API clients (TODO)
│   ├── EspnClient.java
│   ├── OddsApiClient.java
│   └── SportsDataClient.java
├── config/                            # Spring configuration
│   ├── OpenApiConfig.java
│   ├── RedisConfig.java
│   └── SchedulingConfig.java
├── controller/                        # REST controllers
│   ├── CorrelationController.java
│   ├── ExpectedValueController.java
│   ├── ParlayController.java
│   └── TeamController.java
├── exception/                         # Error handling
│   ├── GlobalExceptionHandler.java
│   └── ResourceNotFoundException.java
├── model/
│   ├── dto/                           # Request/response objects
│   ├── entity/                        # JPA entities
│   └── enums/                         # Market, Selection, Recommendation
├── repository/                        # Spring Data JPA repositories
└── service/                           # Business logic (TODO stubs)
    ├── CorrelationRebuildScheduler.java
    ├── CorrelationService.java
    ├── DataIngestionService.java
    ├── ExpectedValueService.java
    ├── MonteCarloSimulationService.java
    └── ParlayAnalysisService.java
```

## Configuration

Key settings in `src/main/resources/application.yml`:

| Property | Default | Description |
|----------|---------|-------------|
| `server.port` | `8080` | Server port |
| `sgp.simulation.default-iterations` | `100000` | Monte Carlo iterations |
| `sgp.correlation.rebuild-cron` | `0 0 3 * * *` | Nightly rebuild schedule (3 AM) |
| `sgp.correlation.cache-ttl-hours` | `1` | Redis cache TTL for correlations |

### External API Keys

Set these environment variables before running (required once clients are implemented):

```bash
export ODDS_API_KEY=your-key-here
export SPORTS_DATA_API_KEY=your-key-here
```

## Useful Commands

```bash
# Compile only
./mvnw clean compile

# Run tests
./mvnw test

# Package as JAR
./mvnw clean package -DskipTests
java -jar target/sgp-analyzer-0.0.1-SNAPSHOT.jar

# Build Docker image
docker build -t sgp-analyzer .

# Stop infrastructure
docker compose down
```
