package automation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public abstract class BaseTest {
    protected WebDriver driver;
    protected String baseUrl;
    protected String dbPath;

    /**
     * Load configuration from config.properties.
     * Falls back to defaults if properties file is missing.
     */
    protected Properties loadConfig() {
        Properties properties = new Properties();
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (input == null) {
                System.err.println("⚠️ config.properties not found. Using defaults.");
                properties.setProperty("base.url", "http://localhost:3000");
            } else {
                properties.load(input);
            }
        } catch (IOException e) {
            System.err.println("⚠️ Error loading config.properties: " + e.getMessage());
            properties.setProperty("base.url", "http://localhost:3000");
        }
        return properties;
    }

    /**
     * Setup: Initialize WebDriver and load base URL.
     * Runs before each test (@BeforeEach).
     */
    @BeforeEach
    public void setUp() {
        Properties config = loadConfig();
        baseUrl = config.getProperty("base.url", "http://localhost:3000");

        // Load DB Path from config, or fallback to a default relative path
        dbPath = config.getProperty("db.path", "app-under-test/shop.db");

        // Configure Chrome options (headless mode for CI, headed for local debugging)
        ChromeOptions options = new ChromeOptions();
        
        // Uncomment for headless mode (recommended for CI):
        // options.addArguments("--headless");
        // options.addArguments("--disable-gpu");
        
        // Additional options for stability
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-blink-features=AutomationControlled");

        // Initialize ChromeDriver (Selenium 4.6+ manages chromedriver automatically)
        driver = new ChromeDriver(options);

        // Set implicit wait (optional; Playwright-equivalent is explicit waits in tests)
        driver.manage().timeouts().implicitlyWait(
            java.time.Duration.ofSeconds(10)
        );

        System.out.println("✅ Browser initialized. Base URL: " + baseUrl);
    }

    /**
     * Teardown: Close the browser.
     * Runs after each test (@AfterEach).
     */
    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
            System.out.println("✅ Browser closed.");
        }
    }

    /**
     * Navigate to the base URL (equivalent to Playwright's go_home).
     */
    protected void navigateToHome() {
        driver.get(baseUrl);
        driver.navigate().refresh();  // Ensure page is fully loaded
    }

    /**
     * Navigate to a specific path relative to baseUrl.
     * Example: navigateTo("/cart") → http://localhost:3000/cart
     */
    protected void navigateTo(String path) {
        driver.get(baseUrl + path);
    }
}