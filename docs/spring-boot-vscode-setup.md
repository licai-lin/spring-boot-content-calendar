# Spring Boot Development in VS Code

A practical guide to the tools needed for this project on macOS.

## 1. The complete tool setup

```text
Spring Boot development
├── Installed on your computer
│   └── Java Development Kit (JDK)
│       ├── Compiles Java code
│       └── Runs Java applications
│
├── Installed inside VS Code
│   ├── Extension Pack for Java — Microsoft
│   │   ├── Language Support for Java by Red Hat
│   │   │   └── Completion, error checking, navigation, and refactoring
│   │   ├── Debugger for Java
│   │   │   └── Breakpoints, stepping through code, and inspecting variables
│   │   ├── Test Runner for Java
│   │   │   └── Run and debug Java tests
│   │   ├── Maven for Java
│   │   │   └── Maven project and build integration
│   │   ├── Gradle for Java
│   │   │   └── Gradle integration; this project uses Maven instead
│   │   └── Project Manager for Java
│   │       └── Browse Java projects, packages, and dependencies
│   │
│   └── Spring Boot Extension Pack
│       ├── Spring Boot Tools
│       │   └── Spring assistance and configuration completion/validation
│       ├── Spring Initializr Java Support
│       │   └── Generate new Spring Boot projects
│       └── Spring Boot Dashboard
│           └── Find, start, stop, and debug Spring Boot applications
│
└── Included in this project
    ├── pom.xml
    │   └── Declares dependencies, Java target version, and build configuration
    ├── mvnw / mvnw.cmd
    │   └── Maven Wrapper launchers for macOS/Linux and Windows
    └── .mvn/wrapper/
        └── Configures the Maven version used by the wrapper
```

**Install the JDK and the two extension packs.** Each pack installs its included
extensions automatically. Seeing the pack itself alongside its individual
extensions is normal. You do not need another similarly named Java/Spring bundle.

The JDK supplies Java itself. Extensions add editor features. Maven downloads
project dependencies and builds the application. The Maven Wrapper downloads and
runs the configured Maven version, so a separate global Maven installation is
normally unnecessary. Its first run requires network access.

## 2. Your observed setup

```text
content-calendar setup
├── Terminal
│   └── OpenJDK 21.0.10 detected during the setup check
├── VS Code project settings
│   └── JavaSE-17 selected in your screenshot
├── pom.xml
│   └── <java.version>17</java.version>
├── Extension Pack for Java
│   └── Installed, as shown in your screenshot
├── Spring Boot Extension Pack
│   └── Installed, as shown in your screenshot
└── Maven Wrapper
    └── Present in the project root
```

These are setup observations, not confirmation that the application has started.

Multiple JDKs can coexist. The Java version selected for the VS Code project can
differ from the Java used by your terminal. The version in `pom.xml` controls the
project's Java target; it does not install a JDK or change your terminal's Java.

## 3. Check the extensions

```text
VS Code
└── Extensions (Command + Shift + X)
    ├── Search: Extension Pack for Java
    │   └── Choose the Microsoft pack
    └── Search: Spring Boot Extension Pack
        └── Choose the pack containing the three Spring extensions above
```

An installed extension normally shows a gear button. An extension that has not
been installed shows an **Install** button. The numbered badge on a pack indicates
how many extensions it contains, not how many installation steps remain.

## 4. Check the JDK

```text
VS Code
└── Command Palette (Command + Shift + P)
    └── Java: Configure Java Runtime
        └── Select content-calendar
            └── Inspect the project JDK and its installation path
```

Your screenshot showed a JDK under the following location:

```text
~/.gradle/jdks/
├── eclipse_adoptium-17-aarch64-os_x.2/
│   └── jdk-17.0.20+8/
│       └── Contents/
│           └── Home/              ← JDK home selected by VS Code
├── OpenJDK17U...tar.gz             ← Downloaded installation archive
├── CACHEDIR.TAG                   ← Cache metadata
└── ...lock                        ← Download/cache coordination files
```

If using **Find a local JDK…**, select the JDK's `Contents/Home` directory,
not the `.tar.gz` archive. The `.gradle/jdks/` storage location does not make this
a Gradle project; this project is configured with Maven.

In the VS Code terminal, run:

```sh
java -version
javac -version
./mvnw -v
```

These check the terminal's Java runtime, Java compiler, and the Java/Maven versions
used by the wrapper. They can differ from the project JDK shown in VS Code.

## 5. Open, run, debug, and test

```text
Daily workflow
├── Open the project
│   ├── File → Open Folder… → content-calendar
│   ├── Open the folder containing pom.xml
│   └── Wait for Java project import and dependency downloads to finish
├── Run the application
│   ├── Open Spring Boot Dashboard
│   ├── Find content-calendar
│   └── Click Run
├── Debug the application
│   ├── Set a breakpoint beside a Java source line
│   └── Start the application with Debug in Spring Boot Dashboard
├── Run tests
│   └── Use VS Code's Testing view or the terminal command below
└── Create a future project
    └── Command Palette → Spring Initializr: Create a Maven Project
```

Run these commands from the project root as needed:

```sh
# Start the application; press Control + C to stop it.
./mvnw spring-boot:run

# Run tests.
./mvnw test

# Build the application JAR, including tests.
./mvnw package
```

A successful startup confirms the application can build and run. If startup fails,
read the first relevant error in the terminal or output panel. A dependency or
application configuration error does not necessarily mean an extension is missing.

## 6. Official references

- [Spring Boot in VS Code](https://code.visualstudio.com/docs/java/java-spring-boot)
- [Extension Pack for Java and included extensions](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack)
- [Spring Boot Extension Pack and included extensions](https://marketplace.visualstudio.com/items?itemName=vmware.vscode-boot-dev-pack)
- [Managing Java projects and configuring JDKs](https://code.visualstudio.com/docs/java/java-project)
