# Booking Service

A Spring Boot booking service that talks to a separate customer-service. CI runs on
GitHub Actions, and the service is deployed on Railway with separate **staging** and
**production** environments.

## Deployed service

- Production: https://booking-service-production-cab8.up.railway.app

## Development workflow

This project uses **GitHub Flow**: one long-lived branch (`main`) plus short-lived
feature branches merged through pull requests.

### Branch → PR → main → deploy

1. Create a feature branch off `main` (e.g. `feature/add-health-check`).
2. Open a **pull request** into `main`. This triggers CI
   (`.github/workflows/ci.yml`), which runs the test suite (`mvn test`).
   On a PR **only the tests run** — no image is published.
3. CI must pass and the PR is reviewed by another team member before it can merge.
4. Merging to `main` builds a Docker image and pushes it to Docker Hub, tagged with
   both the **commit SHA** and `latest`.
5. The **same image** is then deployed on Railway:
   - **staging** runs `chaoraymond/booking-service:latest`
   - once verified in staging, **production** is promoted by pointing it at the
     exact same build by its SHA tag (`chaoraymond/booking-service:<sha>`)

   Only the environment variables differ between the two environments.

```
feature ──PR (tests)──> main ──CI builds image──> Docker Hub
                                                      │
                               staging (:latest) ◄────┤
                                                      │
                           production (:<sha>) ◄───────┘   (same image, promoted)
```

### Why GitHub Flow?

- **`main` is always deployable.** Every change goes through a PR with passing CI,
  so `main` stays releasable — which is what makes it safe to ship to staging.
- **The PR is the safety checkpoint.** Tests run and another team member reviews
  before code reaches `main`, and nothing reaches production without first being
  verified in staging.
- **It fits the team size and release cadence.** A heavier **Git Flow** (separate
  `develop`, `release`, and `hotfix` branches) would add ceremony with no benefit
  for a small team deploying continuously. Pure **trunk-based** development
  (committing straight to `main`) would remove the PR/CI gate and risk shipping
  untested code. GitHub Flow sits in between: one simple pipeline, one review gate,
  fast feedback.

## Deployment & rollback

Every CI build on `main` pushes two tags to Docker Hub:
- `chaoraymond/booking-service:<commit-sha>` — immutable, identifies the exact build
- `chaoraymond/booking-service:latest` — moving pointer to the most recent build

Staging and production run the **same image**; only environment variables differ,
and production is pinned to a specific SHA so you always know exactly what is live.

### Roll back to a previous version

1. Find the SHA of a known-good build (GitHub commit history or Docker Hub tags).
2. In Railway, point the production service at that image tag and redeploy:
   `chaoraymond/booking-service:<good-sha>`
   — or use **Deployments → pick an earlier successful deploy → Redeploy**.

To pull that exact image locally:
```bash
docker pull chaoraymond/booking-service:<good-sha>
```

## Merge conflict

A merge conflict occurred in the test files when two team members changed the same
tests on separate branches at the same time. One version used **hardcoded dates**,
the other used **relative/current dates**. We resolved the conflict by keeping the
version with relative dates, because hardcoded dates would eventually break as time
passes and make the tests fail for no real reason.
