# Booking Service

A Spring Boot booking service that talks to a separate customer-service. CI runs on
GitHub Actions; the app is deployed on Railway with separate **staging** and
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
   On a PR **only the tests run**, no image is published.
3. CI must pass and the PR is reviewed before it can merge.
4. Merging to `main`:
    CI builds a Docker image and pushes it to Docker Hub, tagged with the
     commit SHA **and** `latest`.
    Railway auto-deploys `main` to the **staging** environment.
5. After the change is verified in staging, production is deployed **deliberately**
   by running the **"Deploy to production"** workflow
   (`.github/workflows/deploy-production.yml`) manually from the GitHub Actions tab.

```
feature branch ──PR (tests)──> main ──auto──> staging ──manual workflow──> production
```

### Why GitHub Flow?

- **`main` is always deployable.** Every change goes through a PR with passing CI,
  so `main` stays releasable — which is exactly what makes auto-deploying it to
  staging safe.
- **The PR is the safety checkpoint.** Tests run before code reaches `main`, and
  nothing reaches production without first being observed in staging.
- **It fits the team size and release cadence.** A heavier **Git Flow** (separate
  `develop`, `release`, and `hotfix` branches) would add ceremony with no benefit
  for a small team deploying continuously. Pure **trunk-based** development
  (committing straight to `main`) would remove the PR/CI gate and risk shipping
  untested code. GitHub Flow sits in between: one simple pipeline, one review gate,
  fast feedback.

## Deployment & rollback

Every CI build on `main` pushes two tags to Docker Hub:
- `booking-service:<commit-sha>` — immutable, identifies the exact build
- `booking-service:latest` — moving pointer to the most recent build

### Roll back to a previous version

1. Find the SHA of a known-good build (GitHub commit history or Docker Hub tags).
2. Pull and run that exact image:
   ```bash
   docker pull <user>/booking-service:<good-sha>
   docker run <user>/booking-service:<good-sha>
   ```
3. (Optional) Re-point `latest` at the good build so deploys pick it up:
   ```bash
   docker pull <user>/booking-service:<good-sha>
   docker tag  <user>/booking-service:<good-sha> <user>/booking-service:latest
   docker push <user>/booking-service:latest
   ```

On Railway you can also roll back from the dashboard:
**Deployments → pick an earlier successful deploy → Redeploy**.
