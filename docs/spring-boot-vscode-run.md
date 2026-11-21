# Guide 2: Run Your Spring Boot Application in VS Code

A practical follow-up to [Spring Boot Development in VS Code](spring-boot-vscode-setup.md),
using this project's `ContentCalendarApplication` on macOS.

## 1. Which button matches IntelliJ's Run "Application"?

**Click the Run triangle beside `content-calendar` under Apps in Spring Boot Dashboard.**
Hovering over that button shows the tooltip **Run**.

```text
Spring Boot Dashboard
└── Apps
    └── content-calendar
        ├── ▶ Run          ← Click this to start the application
        └── Debug          ← Use this when you want breakpoints
```

Use the application row under **Apps**. The `contentCalendarApplication` entry
under **Beans** describes a Spring-managed object; it is not the start button.

The Java and Spring extensions are already working in the screenshots from this
setup. No additional extension is needed to run the application.

## 2. Other ways to start the same application

Open:

```text
src/main/java/com/your_domain_name/content_calendar_server_rest/
└── ContentCalendarApplication.java
```

Above `main()`, VS Code displays clickable **Run | Debug** links:

```java
// Click the Run link VS Code displays above this method.
public static void main(String[] args) {
    SpringApplication.run(ContentCalendarApplication.class, args);
}
```

You can also use the editor's top-right dropdown and select **Run Java**.
Choose one starting method per session; starting another copy can cause a port conflict.

| IntelliJ action | VS Code equivalent |
| --- | --- |
| Run "Application" | Dashboard Run, Run above `main()`, or Run Java |
| Debug "Application" | Dashboard Debug, Debug above `main()`, or Debug Java |
| Modify Run Configuration | Customize `.vscode/launch.json` when needed |
| Run "Application" with Coverage | No matching action in this Java application menu; coverage uses separate tooling |

For a normal start, the Java extension discovers the main class automatically.
You do not need to create `launch.json`. See the
[official Java run and debug guide](https://code.visualstudio.com/docs/java/java-debugging).

Coverage measures which code executes during a run. It is optional and is not
required to start this application. VS Code's Testing view supports test coverage
when the testing extension provides it; that is a separate workflow from launching
the application. See [VS Code test coverage](https://code.visualstudio.com/docs/debugtest/testing#_test-coverage).

## 3. Read the startup output

After clicking Run, VS Code opens a terminal such as:

```text
Run: ContentCalendarApplication
```

The long Java command, package name, and Spring Boot banner are normal startup
output. The observed run showed Spring Boot `4.2.0` and Java `17.0.20`.

```text
Startup output
├── Spring Boot banner
│   └── Spring Boot has begun starting
├── Starting ContentCalendarApplication using Java ...
│   └── Identifies the application and Java runtime
├── Tomcat started on port 8080 ...
│   └── The embedded web server is listening
└── Started ContentCalendarApplication in ... seconds
    └── Application startup has completed
```

The final two messages confirm successful startup. The banner alone does not:
an error can still occur afterward. Exact log wording can vary by version.

Keep the application running while opening its URLs. A web server normally keeps
the terminal occupied until you stop it.

## 4. Open the application in your browser

Visit **http://localhost:8080/**.

```text
http://localhost:8080/
├── http       → The request protocol
├── localhost  → Your own computer
├── 8080       → The application's web server port
└── /          → The root URL path
```

In this session, the browser displayed a **404 / Whitelabel Error Page**.
That is expected for the project's current source: it has the application entry
point, but no controller or home page for `/`.

```text
Browser requests GET /
└── Spring Boot receives the request
    └── No controller mapping or static page matches /
        └── Returns HTTP 404 and the default error page
```

This means the server responded, but the requested resource does not exist.
It does not mean that a VS Code extension is missing or that Java failed to start.
Once you add a controller endpoint, visit the path mapped by that controller.
Starting Spring Boot does not automatically create a home page.

## 5. Stop, restart, or debug

```text
Daily controls
├── Stop
│   ├── Use Stop for the running app in Spring Boot Dashboard
│   └── Or focus its terminal and press Control + C
├── Restart after editing code
│   ├── Save your changes
│   ├── Stop the running application
│   └── Click Run again, then refresh the browser
└── Debug
    ├── Stop the existing run
    ├── Click beside an executable source line to set a breakpoint
    ├── Start with Debug
    └── Trigger the code, for example by visiting its endpoint
```

Normal Run does not pause at breakpoints. Debug allows you to pause, inspect
variables, and step through code. Dashboard controls are documented in the
[official Spring Boot guide](https://code.visualstudio.com/docs/java/java-spring-boot#_run-the-application).

## 6. Quick troubleshooting

| What you see | What it means / what to do |
| --- | --- |
| `Started ContentCalendarApplication` and a 404 at `/` | Expected for the current project; add a mapped endpoint or home page when ready. |
| Browser says connection refused | Check that the application is still running and use the port shown in its logs. |
| Port 8080 already in use | Another process may already be listening. Stop an earlier copy before starting again. |
| `APPLICATION FAILED TO START` | Read the error and suggested action below that message; the banner alone did not confirm startup. |
| Breakpoints do not pause execution | Start with Debug and make sure the requested action reaches that source line. |

## 7. Terminal alternative

From the project root, the folder containing `pom.xml`, run:

```sh
./mvnw spring-boot:run
```

Press **Control + C** to stop it. This is an alternative to the Dashboard Run
button; you do not need to run both. It uses the terminal's Java environment,
which can differ from the project JDK configured in VS Code.
