package automation.ui;

import automation.BaseTest;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

// 1. EXTEND BaseTest to inherit the "driver" and setup logic
public class SanityTest extends BaseTest {

    @Test
    public void testFrameworkSetup() {
        // 2. No more "new ChromeDriver()" - it happens in @BeforeEach of BaseTest
        
        // 3. Verify Config Loading
        // (BaseTest should have read the base.url from config.properties)
        String baseUrl = System.getProperty("base.url", "NOT_FOUND"); 
        // Note: If your BaseTest stores it in a variable, you might access it directly.
        // If your BaseTest loads it into System Properties, the line above works.
        // If unsure, just try navigating:
        
        driver.get("https://google.com"); // We will swap this for the real app URL next
        
        System.out.println("Browser opened successfully using BaseTest!");
        assertTrue(driver.getTitle().contains("Google"));
    }
}