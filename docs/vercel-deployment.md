# Guide 2: Deploy Spring Boot to Vercel through GitHub

This guide follows the process that worked for this project:

1. Push the latest source to GitHub.
2. Import the repository into Vercel and create a project.
3. Add the environment variables in Vercel and deploy.
4. Copy the Vercel token, Team ID, and Project ID into GitHub Actions secrets.
5. Update the production description and push to `main` to test automatic deployment.

Complete [Guide 1: Neon PostgreSQL integration](neon-database-setup.md) first.
No local `.env` file or Vercel CLI linking is required for this dashboard-based setup.

## 1. Understand the deployment files

The repository already contains these files:

| File | Purpose |
| --- | --- |
| `Dockerfile.vercel` | Builds the Spring Boot JAR and runs it in a Java container |
| `.dockerignore` | Excludes unnecessary files from the Docker build context |
| `vercel.json` | Selects the Container preset and disables automatic native Git deployments |
| `.github/workflows/ci.yml` | Runs tests and packages the app for pushes and pull requests to `main` |
| `.github/workflows/deploy.yml` | Deploys the tested commit after successful CI on `main` |
| `src/main/resources/application-production.properties` | Configures Neon, the production description, and the server port |

### How the Dockerfile works

The build stage packages the app:

```dockerfile
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn --batch-mode --no-transfer-progress -DskipTests package
```

The runtime stage copies the generated JAR, renames it `app.jar`, and runs it:

```dockerfile
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /workspace/target/content-calendar-0.0.1-SNAPSHOT.jar app.jar
ENV SPRING_PROFILES_ACTIVE=production
ENV PORT=8080
USER 10001:10001
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

`RUN mvn ... package` builds the JAR. `COPY --from=build ... app.jar` copies it
into the runtime image. `ENTRYPOINT` runs the JAR when a container starts; it
does not build the application. GitHub Actions runs tests before the deployment
build, which skips tests during packaging.

Keep the exact filename `Dockerfile.vercel` at the repository root, beside
`pom.xml`. Vercel detects it without an extra Dockerfile path setting.

The Vercel configuration is:

```json
{
  "$schema": "https://openapi.vercel.sh/vercel.json",
  "framework": "container",
  "git": {
    "deploymentEnabled": false
  }
}
```

The initial dashboard deployment is manual. Later automatic deployments are
controlled by GitHub Actions, so they wait for tests to pass.

## 2. Push the latest source to GitHub first

Review and stage the intended changes, including the deployment files, database
configuration, H2 profile, and JSON seed data. Keep actual passwords and access
tokens out of the commit.

```sh
git status
git diff
# Stage the files you reviewed using git add <file>.
git commit -m "Configure Neon and Vercel deployment"
git push origin main
```

The repository used here is:

```text
licai-lin/spring-boot-content-calendar
```

If Actions starts immediately, the test job can pass while deployment fails
because the three Vercel secrets have not been configured yet. Finish the setup
below, then trigger another run.

## 3. Import the repository into Vercel

On Vercel, choose **Add New → Project**, find the GitHub repository, and click
**Import**. Authorize access to the repository if prompted.

Use these settings on the New Project screen:

| Setting | Value used for this project |
| --- | --- |
| Vercel Team | `Zero Builders` |
| Project Name | `spring-boot-content-calendar` |
| Git branch | `main` |
| Root Directory | `./` |
| Application Preset | `Container`, set by `vercel.json` |
| Build Command | `None`; override off |
| Output Directory | `None`; override off |
| Install Command | `None`; override off |

Leave all three **Build and Output Settings** overrides off. The Dockerfile
provides the build and startup commands.

## 4. Add the Vercel environment variables

Expand **Environment Variables** before deploying and add three separate entries.
Select **Production** as the environment for each entry.

| Key | Value |
| --- | --- |
| `SPRING_PROFILES_ACTIVE` | `production` |
| `PORT` | `8080` |
| `NEON_DATABASE_PASSWORD` | Your actual Neon database password, without quotes |

The database URL and username are already in
`application-production.properties`. Its password property is:

```properties
spring.datasource.password=${NEON_DATABASE_PASSWORD}
```

The port property is:

```properties
server.port=${PORT:9090}
```

| Environment | Selected port |
| --- | --- |
| Local production run without `PORT` | 9090 |
| Docker image default (`ENV PORT=8080`) | 8080 |
| Vercel with `PORT=8080` | 8080 internally |

A runtime environment variable can override the Dockerfile's `ENV` default.
The hosted public URL uses HTTPS without an explicit `:8080` suffix.

Click **Deploy**. Wait for the container build and application startup to complete.

## 5. Confirm the first deployment works

The successfully deployed project uses this domain:

```text
https://spring-boot-content-calendar.vercel.app
```

Open:

```text
https://spring-boot-content-calendar.vercel.app/actuator/health
https://spring-boot-content-calendar.vercel.app/api/content
https://spring-boot-content-calendar.vercel.app/api/content/filter/GraphQL
```

The GraphQL filter returned the matching article in the working deployment.
The application connects to Neon and seeds `data/content.json` when the table
is empty. Existing content is left unchanged on normal sequential restarts.

## 6. Get the three GitHub secret values from vercel.com

Only `VERCEL_TOKEN` is an access token. The other two values identify the team
and project.

### VERCEL_TOKEN

1. Open [Vercel Account Settings → Tokens](https://vercel.com/account/tokens).
2. Create a token named, for example, `Content Calendar`.
3. Choose a scope that grants access to the team owning this project,
   `Zero Builders`.
4. Select an expiration and click **Create**.
5. Copy the generated token value. If you already created and saved the
   `Content Calendar` token, use that value.

### VERCEL_ORG_ID

1. Select **Zero Builders** in Vercel's team switcher.
2. Open the **team's Settings → General**.
3. Find **Team ID** and copy the full value, including the `team_` prefix.

For this team-owned project, `VERCEL_ORG_ID` is the Team ID. Use the ID rather
than the display name or team URL slug.

### VERCEL_PROJECT_ID

1. Open the **spring-boot-content-calendar** project on Vercel.
2. Open the **project's Settings → General**.
3. Find **Project ID** and copy the full value, including the `prj_` prefix.

## 7. Add repository secrets in GitHub

Open the GitHub repository, then go to:

**Settings → Secrets and variables → Actions → Secrets → New repository secret**

Create each entry separately under **Repository secrets**:

| Secret name | Value from Vercel |
| --- | --- |
| `VERCEL_TOKEN` | Generated access token |
| `VERCEL_ORG_ID` | Team ID (`team_...`) |
| `VERCEL_PROJECT_ID` | Project ID (`prj_...`) |

Use the exact secret names above. Do not put them under the **Variables** tab.
The database password stays in Vercel; the workflow does not need it in GitHub.

The workflow uses a GitHub environment named `production`. GitHub can create
that environment on its first run. Repository secrets remain available to the
job; adding environment-specific secrets is not required for this setup.

## 8. Update the description and push to main

This small change tests the entire automatic deployment process. In
`src/main/resources/application-production.properties`, change:

```properties
cc.about=Production about message
```

to:

```properties
cc.about=Content Calendar integrated with Neon PostgreSQL for production.
```

The new description is already in this repository. For another deployment test,
make a new wording change and save it, then commit and push:

```sh
git add src/main/resources/application-production.properties
git commit -m "Update production description for Neon PostgreSQL"
git push origin main
```

If the description was already committed and there is no new change, trigger
**Actions → CI → Run workflow → main** instead. Successful manual CI on
`main` also triggers CD; deployment cannot be manually started without CI.

## 9. Watch GitHub Actions and verify the update

In GitHub's **Actions** tab, the two workflows run in order:

1. **CI** (`ci.yml`) runs the **Test and package** job: Maven `verify` on
   Java 17, using H2 for tests.
2. **CD - Deploy to Vercel** (`deploy.yml`) listens for CI completion through
   `workflow_run`. Its deployment job runs only when CI succeeded for a push
   or manual CI run on `main` in this repository. Failed or cancelled CI
   skips deployment. Pull request CI never deploys.

CD checks out the exact commit SHA that passed CI before uploading source
through the Vercel CLI. Vercel then builds `Dockerfile.vercel` and updates
the production deployment. Both workflow files must be committed to the
repository's default branch (`main`) for this setup.

Once both workflows are green, open:

```text
https://spring-boot-content-calendar.vercel.app/
```

The root endpoint returns the application properties, including the updated
`about` value. Check `/api/content/filter/GraphQL` again to confirm the API
still reads Neon content.

Future pushes to `main` follow the same test-and-deploy process, except when all
changed files are under `docs/`. CI uses `paths-ignore: ['docs/**']` for
pushes and pull requests. Documentation-only changes start no CI run, so they
also start no CD run. A change containing files outside `docs/` still triggers
CI. To deploy manually, run **CI** on `main` and let successful CI trigger CD.

## Troubleshooting

| Problem | Check |
| --- | --- |
| Missing `VERCEL_*` secret | Add all three under GitHub repository secrets with the exact names |
| Vercel token cannot access the project | Check that the token scope, Team ID, and Project ID refer to the same team/project |
| Neon password cannot be resolved | Add `NEON_DATABASE_PASSWORD` to Vercel's Production environment, then redeploy |
| Container cannot be detected | Check root directory `./`, filename `Dockerfile.vercel`, and that the file was pushed |
| App listens on the wrong port | Check Vercel `PORT=8080` and `server.port=${PORT:9090}` |
| No new workflow starts | Push a change outside `docs/` to `main`, or manually run **CI** on `main` |
| CD skips deployment | Check CI succeeded for a push or manual run on `main`; PR, failed, and cancelled runs do not deploy |
| Native Vercel Git deployment does not start | Expected: `git.deploymentEnabled=false`; GitHub Actions performs deployment |
| First deployment fails | Inspect Vercel build/runtime logs for the failing build command or startup error |

## References

- [Vercel container configuration](https://vercel.com/kb/guide/docker-on-vercel-vs-render)
- [GitHub Actions with Vercel](https://vercel.com/kb/guide/how-can-i-use-github-actions-with-vercel)
- [Find a Vercel Team ID](https://vercel.com/docs/accounts#find-your-team-id)
- [Vercel project settings](https://vercel.com/docs/project-configuration/general-settings)
- [Disable native Git deployments](https://vercel.com/docs/project-configuration/git-configuration#turning-off-all-automatic-deployments)

- [GitHub workflow_run trigger](https://docs.github.com/en/actions/reference/workflows-and-actions/events-that-trigger-workflows#workflow_run)
