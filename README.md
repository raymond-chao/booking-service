## Deployment & Rollback

Every CI build pushed two tags to Docker Hub:
- `booking-service:<commit-sha>` - immutable, identifies the exact build
- - `booking-service:latest` — moving pointer to the most recent build
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
