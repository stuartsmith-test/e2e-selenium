# E2E Automation: Java + Selenium Hybrid Framework

![Java](https://img.shields.io/badge/Java-21-orange) ![Selenium](https://img.shields.io/badge/Selenium-4-green) ![JUnit5](https://img.shields.io/badge/JUnit-5-red) ![Build Status](https://github.com/stuartsmith-test/e2e-selenium/actions/workflows/maven-test.yml/badge.svg)

## Overview
This repository is a personal initiative to explore **Java-based test automation patterns**.

It serves as an architectural port of my [Playwright/Python Framework](https://github.com/stuartsmith-test/e2e-playwright). The goal of this project was to take a functioning automation suite and replicate its core capabilities—API state seeding, Page Object Model, and database checks—using the **Selenium/Java** ecosystem.

For details on the AI-assisted development process used to create this framework, please refer to the [original project's README](https://github.com/stuartsmith-test/e2e-playwright#ai-assisted-qa).

### Technical Scope
* **Hybrid Framework:** Combines **Selenium WebDriver** (UI interactions) with **Rest Assured** (API-based test data setup).
* **Page Object Model (POM):** Encapsulates locators and behaviors in dedicated classes (`HomePage`, `CartPage`) to maintain clean test logic.
* **Database Support:** Includes JDBC utilities for querying the application's SQLite database to verify backend state.
* **Automated CI Environment:** A GitHub Actions workflow that handles the full application lifecycle—cloning the SUT (System Under Test), starting the Node.js server, and running headless tests.

---

## Prerequisites

Before running these tests, ensure you have the necessary tools installed.

### 1. Java Development Kit (JDK) 21
This project requires Java 21.
* **Check version:** `java -version`
* **Install:** [Download JDK 21](https://www.oracle.com/java/technologies/downloads/#java21) or use a package manager (e.g., `brew install openjdk@21`).

### 2. Maven
* **Check version:** `mvn -version`
* **Install:** [Download Maven](https://maven.apache.org/download.cgi).

### 3. Node.js (for the App Under Test)
The test application is a Node.js web app. You need Node to run it locally.
* **Recommended:** Node v16 or higher.
* **Check version:** `node -v`

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

### Execute All Tests
Run the entire suite using Maven:
```bash
mvn test
```

### Run Specific Tests
To run only the End-to-End cart scenario:
```bash
mvn -Dtest=AddToCartTest test
```

### Debugging
The tests run in **Headed Mode** (browser visible) by default when running locally.
To run in **Headless Mode** (no browser UI):
```bash
mvn test -Dheadless=true
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