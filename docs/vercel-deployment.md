# Deploy the API with GitHub Actions and Vercel

Pushes to `main` run Maven verification with H2, then deploy the source to
Vercel, where `Dockerfile.vercel` builds the production container. Pull requests
run verification only. Manual workflow runs deploy only when run on `main`.

`vercel.json` selects the Container framework and disables Vercel's automatic
Git deployments. GitHub Actions controls deployment after tests pass.

## 1. Create or link the Vercel project

From the repository root, run:

```sh
npx --yes vercel@latest login
npx --yes vercel@latest link
```

Select your Vercel account/team and create a project named `content-calendar`,
or link your existing project. Use this repository root as the project directory.
Linking does not deploy the app. It creates ignored local project metadata in
`.vercel/project.json` containing `orgId` and `projectId`.

In the Vercel project settings:

| Setting | Value |
| --- | --- |
| Framework Preset | Container (also configured by `vercel.json`) |
| Root Directory | Repository root (`.`) |
| Production Branch, if connecting Git | `main` |
| Build / Output / Install overrides | Leave unset; use the container build |

The container runtime is currently beta. The app uses the root
`Dockerfile.vercel`, runs Java 17, and reads the `PORT` environment variable.

## 2. Add Vercel environment variables

Open the project's **Settings → Environment Variables**, select **Production**,
and add:

| Name | Value |
| --- | --- |
| `SPRING_PROFILES_ACTIVE` | `production` |
| `PORT` | `8080` |
| `NEON_DATABASE_PASSWORD` | Your current Neon database password |

The Neon JDBC URL and username remain in `application-production.properties`.
The password is resolved from the environment at runtime. No `.env` file is
required and the database password is not needed by GitHub Actions.

## 3. Add GitHub Actions secrets

Create a Vercel access token at <https://vercel.com/account/tokens>, scoped to
the account/team that owns the project.

In GitHub, open `licai-lin/spring-boot-content-calendar`, then
**Settings → Secrets and variables → Actions → New repository secret**:

| Secret | Where to obtain its value |
| --- | --- |
| `VERCEL_TOKEN` | The Vercel access token |
| `VERCEL_ORG_ID` | `orgId` from local `.vercel/project.json` |
| `VERCEL_PROJECT_ID` | `projectId` from local `.vercel/project.json` |

The deploy job uses the GitHub environment `production`. You can create it under
**Settings → Environments** to configure branch restrictions or reviewers, or
allow GitHub to create it automatically on the first run.

## 4. Commit and push

Review the changed files, then commit the intended application and deployment
changes and push to `main`. Include the JSON seed data and H2 profile from the
database setup along with the new deployment files. Do not commit tokens,
database passwords, `.vercel`, or build output.

```sh
git status
git diff
# Stage the files you reviewed, then:
git commit -m "Configure Neon production deployment with GitHub Actions and Vercel"
git push origin main
```

In GitHub's **Actions** tab, open **Test and deploy**. The jobs run in order:

1. **Test and package** executes `./mvnw verify`; tests use local H2.
2. **Deploy production to Vercel** uploads source and asks Vercel to build and
   deploy the container. The deployment command prints the resulting URL.

If the source was pushed before you added secrets, add them and rerun the failed
workflow. Subsequent pushes to `main` update the same production project.

## 5. Check the hosted API

Open your Vercel project URL with these paths:

```text
/actuator/health
/api/content
/api/content/filter/Building
```

Production connects to Neon and loads `data/content.json` if the content table is
empty. Existing content is preserved. H2 Console is disabled, health details
are hidden, and only the health and info Actuator endpoints are exposed.

## Local commands

Local H2 with its console, without any secrets:

```sh
./mvnw spring-boot:run
```

For local Neon, set `NEON_DATABASE_PASSWORD` in your terminal or IDE and run:

```sh
./mvnw spring-boot:run -Dspring-boot.run.profiles=production
```

For a local container build and an H2 smoke test:

```sh
docker build -f Dockerfile.vercel -t content-calendar .
docker run --rm -p 8080:8080 -e SPRING_PROFILES_ACTIVE=h2 content-calendar
```

The image defaults to production when the profile is not overridden. The runtime
image contains the application JAR and a Java runtime, without Maven or source.

## References

- [Vercel container configuration and limitations](https://vercel.com/kb/guide/docker-on-vercel-vs-render)
- [GitHub Actions with Vercel](https://vercel.com/kb/guide/how-can-i-use-github-actions-with-vercel)
- [Disable native Git deployments](https://vercel.com/docs/project-configuration/git-configuration#turning-off-all-automatic-deployments)
