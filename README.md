# E2E Automation: Java + Selenium Hybrid Framework

![Java](https://img.shields.io/badge/Java-21-orange) ![Selenium](https://img.shields.io/badge/Selenium-4-green) ![JUnit5](https://img.shields.io/badge/JUnit-5-red) ![Build Status](https://github.com/stuartsmith-test/e2e-selenium/actions/workflows/maven-test.yml/badge.svg)

## Overview
This repository is a personal initiative to explore **Java-based test automation patterns**.

The project was generated using an AI-First workflow (Copilot/Claude) to demonstrate the rapid porting of a [Python/Playwright architecture](https://github.com/stuartsmith-test/e2e-playwright) into a Java/Selenium environment. It showcases how domain-aware AI agents can accelerate framework setup and test generation.

### Technical Scope
* **Hybrid Framework:** Combines **Selenium WebDriver** (UI interactions) with **Rest Assured** (API-based test data setup).
* **Page Object Model (POM):** Encapsulates locators and behaviors in dedicated classes (`HomePage`, `CartPage`) to maintain clean test logic.
* **Database Support:** Includes JDBC utilities for querying the application's SQLite database to verify backend state.
* **Automated CI Environment:** A GitHub Actions workflow that handles the full application lifecycle—cloning the SUT (System Under Test), starting the Node.js server, and running headless tests.

---

## Prerequisites

Before running these tests, ensure you have the necessary runtime environment.

### 1. Java Development Kit (JDK) 21
This project requires Java 21.
* **Check version:** `java -version`
* **Install:** [Download JDK 21](https://www.oracle.com/java/technologies/downloads/#java21) or use a package manager (e.g., `brew install openjdk@21`).

### 2. Node.js (for the App Under Test)
The test application is a Node.js web app. You need Node to run it locally.
* **Recommended:** Node v16 or higher.
* **Check version:** `node -v`

*(Note: You do not need to install Maven manually. This project includes a Maven Wrapper).*

---

## Setup & Configuration

### 1. Clone this Repository
```bash
git clone https://github.com/stuartsmith-test/e2e-selenium.git
cd e2e-selenium
```

### 2. Prepare the App Under Test
The tests run against the [Test Automation Foundations](https://github.com/stuartsmith-test/test-automation-foundations-728391) app (forked from LinkedIn Learning).

**Clone the app into a separate folder** (or use the one created by your Python project):
```bash
# You can clone this anywhere, but keep track of the path!
git clone https://github.com/stuartsmith-test/test-automation-foundations-728391.git app-under-test
cd app-under-test
npm ci  # Clean install of dependencies
```

**Start the App:**
```bash
npm start
# The app will launch at http://localhost:3000
```
*(Leave this terminal window running)*

### 3. Configure Test Properties
This project uses a configuration template to avoid committing local file paths.

1.  Navigate to `src/test/resources/`.
2.  Locate `config.properties.example`.
3.  **Rename (or copy)** it to `config.properties`.
4.  Open `config.properties` and update the `db.path`.

**Important:** You must provide the **Absolute Path** to the `shop.db` file located inside the `app-under-test` folder you just cloned.

*Windows Example:*
```properties
base.url=http://localhost:3000
db.path=C:/Users/YourUser/Projects/app-under-test/shop.db
```

*Mac/Linux Example:*
```properties
base.url=http://localhost:3000
db.path=/Users/YourUser/dev/app-under-test/shop.db
```

> **Note for CI/CD:** The `config.properties` file is ignored by Git. The `BaseTest` class contains fallback logic to handle environments where this file is missing (like GitHub Actions).

---

## Running Tests

This project uses the **Maven Wrapper** (`mvnw`), which automatically handles the Maven version for you.

### Execute All Tests

**Mac / Linux / Git Bash:**
```bash
./mvnw test
```

**Windows (Command Prompt / PowerShell):**
```powershell
.\mvnw.cmd test
```

### Run Specific Tests
To run only the End-to-End cart scenario (single test method):

**Mac / Linux / Git Bash:**
```bash
./mvnw -Dtest="AddToCartTest#testE2EAddToCartAndCheckout" test
```

**Windows (Command Prompt / PowerShell):**
```powershell
.\mvnw.cmd -Dtest="AddToCartTest#testE2EAddToCartAndCheckout" test
```

### Debugging (Headless Mode)
To run tests without the visible browser window (Headless):

**Mac / Linux / Git Bash:**
```bash
./mvnw test -Dheadless=true
```

**Windows:**
```powershell
.\mvnw.cmd test -Dheadless=true
```

---

## ☁️ Running in GitHub Codespaces (or Headless Linux)

If you are running this in a cloud environment like GitHub Codespaces, you may need to perform two extra setup steps because Selenium (unlike Playwright) does not bundle its own browser.

### 1. Install Java 21
Codespaces may default to Java 11 or 17. Verify with `java -version`.
If needed, install Java 21 using SDKMAN (pre-installed in Codespaces):
```bash
sdk install java 21.0.2-tem
```

### 2. Install Google Chrome
Selenium requires the actual browser binary to be present on the system.
```bash
wget https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb
sudo apt-get update
sudo apt-get install -y ./google-chrome-stable_current_amd64.deb
```

### 3. Run in Headless Mode
Since there is no monitor, you must tell Maven to run without a UI:
```bash
./mvnw test -Dheadless=true
```

---

## Comparison: Python vs. Java
A quick reference for the architectural mapping between the source project and this port.

| Component | Python Implementation | Java Implementation |
| :--- | :--- | :--- |
| **Test Runner** | `pytest` | `JUnit 5` |
| **Setup/Teardown** | `conftest.py` (Fixtures) | `BaseTest.java` (Inheritance) |
| **API Client** | `playwright.request` | `Rest Assured` |
| **UI Interaction** | `page.get_by_role(...)` | `driver.findElement(By.xpath(...))` |
| **Project Build** | `pip` / `requirements.txt` | `Maven` / `pom.xml` |

---

## License
This project is open source and available under the [MIT License](LICENSE).