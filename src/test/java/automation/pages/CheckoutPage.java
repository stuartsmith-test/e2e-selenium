package automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

/**
 * Page Object Model for the checkout page.
 * 
 * Encapsulates interactions with the /checkout confirmation page.
 */
public class CheckoutPage {
    
    private final WebDriver driver;
    private final String baseUrl;
    private final WebDriverWait wait;

    // ========================================================================
    // LOCATORS
    // ========================================================================

    /**
     * Total price display: div.total-price
     */
    private static final By TOTAL_PRICE_DISPLAY = By.className("total-price");

    /**
     * Thank you message: div.thank-you-message or similar
     */
    private static final By THANK_YOU_MESSAGE = By.cssSelector(".thank-you-message, [class*='thank']");

    /**
     * Checkout container div
     */
    private static final By CHECKOUT_CONTAINER = By.className("checkout-container");

    // ========================================================================
    // CONSTRUCTOR
    // ========================================================================

    /**
     * Initialize the CheckoutPage with a WebDriver and base URL.
     * 
     * @param driver The Selenium WebDriver instance.
     * @param baseUrl The base URL of the application.
     */
    public CheckoutPage(WebDriver driver, String baseUrl) {
        this.driver = driver;
        this.baseUrl = baseUrl;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        
        // Verify we're on the checkout page
        waitForCheckoutPageLoad();
    }

    /**
     * Wait for the checkout page to fully load.
     */
    private void waitForCheckoutPageLoad() {
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(CHECKOUT_CONTAINER));
            System.out.println("✅ Checkout page loaded");
        } catch (Exception e) {
            System.err.println("⚠️ Checkout page load timed out: " + e.getMessage());
        }
    }

    // ========================================================================
    // ASSERTIONS
    // ========================================================================

    /**
     * Get the total price displayed on the checkout page.
     * 
     * @return The total price as a string (e.g., "Total price: $45.99").
     */
    public String getTotalPriceText() {
        try {
            WebElement priceDisplay = wait.until(
                ExpectedConditions.presenceOfElementLocated(TOTAL_PRICE_DISPLAY)
            );
            String text = priceDisplay.getText();
            System.out.println("💰 Checkout total: " + text);
            return text;
        } catch (Exception e) {
            System.err.println("⚠️ Failed to get total price: " + e.getMessage());
            return null;
        }
    }

    /**
     * Assert that the thank-you message is visible.
     * Expected text: "Thanks for your order!"
     */
    public void assertThankYouMessageVisible() {
        try {
            WebElement message = wait.until(
                ExpectedConditions.visibilityOfElementLocated(THANK_YOU_MESSAGE)
            );
            String text = message.getText();
            System.out.println("✅ Thank you message visible: " + text);
        } catch (Exception e) {
            throw new AssertionError(
                "Thank you message not visible: " + e.getMessage()
            );
        }
    }

    /**
     * Assert that the page title is "Checkout".
     */
    public void assertPageTitle() {
        String title = driver.getTitle();
        if (!title.equals("Checkout")) {
            throw new AssertionError(
                "Expected page title 'Checkout', got '" + title + "'"
            );
        }
        System.out.println("✅ Page title is 'Checkout'");
    }
}
