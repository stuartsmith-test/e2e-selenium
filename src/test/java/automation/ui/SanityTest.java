package automation.ui;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SanityTest {

    @Test
    public void testLaunchBrowser() {
        // 1. Setup the driver
        // Note: Selenium 4.6+ automatically manages the driver download for you!
        // No need to manually download chromedriver.exe anymore.
        WebDriver driver = new ChromeDriver();

        try {
            // 2. Execute Test Steps
            driver.get("https://www.google.com");
            String title = driver.getTitle();
            
            // 3. Verification
            System.out.println("Page Title is: " + title);
            assertTrue(title.contains("Google"), "Title should contain 'Google'");

        } finally {
            // 4. Cleanup (Always close the browser!)
            driver.quit();
        }
    }
}
