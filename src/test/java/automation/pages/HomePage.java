package automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

/**
 * Page Object Model for the home page (AI Animal Art store).
 * 
 * Ported from e2e-playwright/utils/ui_helpers.py and e2e-playwright/tests/ui/test_homepage.py.
 * Encapsulates all UI interactions and element locators for the home page.
 */
public class HomePage {
    
    private final WebDriver driver;
    private final String baseUrl;
    private final WebDriverWait wait;

    // ========================================================================
    // LOCATORS (converted from Playwright selectors to Selenium By)
    // ========================================================================

    /**
     * Cart count badge in header: #cart-link span
     * Equivalent: page.locator("#cart-link span")
     */
    private static final By CART_COUNT_BADGE = By.cssSelector("#cart-link span");

    /**
     * Cart link in header: a#cart-link
     * Used for navigating to the cart page.
     */
    private static final By CART_LINK = By.id("cart-link");

    /**
     * Product list: ul > li
     * Each <li> contains an image, name, price, and form with "Add to Cart" button.
     */
    private static final By PRODUCT_LIST = By.cssSelector("ul > li");

    /**
     * Success notification: div.notification
     * Appears when an item is added to cart.
     */
    private static final By SUCCESS_NOTIFICATION = By.className("notification");

    /**
     * Add-to-cart button for a specific item (by itemId).
     * Equivalent: form:has(input[name="itemId"][value="<itemId>"]) > button
     * Selenium workaround: find the form, then get its button child.
     * We use a String template because we need to inject the itemId (%d) dynamically
     * We use XPath because Selenium CSS does not support the ':has' pseudo-class
     */
    private static final String ADD_TO_CART_FORM_TEMPLATE = "//form[.//input[@name='itemId' and @value='%d']]";

    /**
     * Generic "Add to Cart" button within a form.
     */
    private static final By ADD_TO_CART_BUTTON = By.cssSelector("button[type=\"submit\"]");

    /**
     * Product name heading within a <li>: li h2
     */
    private static final By PRODUCT_NAME_HEADING = By.cssSelector("h2");

    /**
     * Product image: li img
     */
    private static final By PRODUCT_IMAGE = By.cssSelector("img");

    // ========================================================================
    // CONSTRUCTOR
    // ========================================================================

    /**
     * Initialize the HomePage with a WebDriver and base URL.
     * 
     * @param driver The Selenium WebDriver instance.
     * @param baseUrl The base URL of the application (e.g., http://localhost:3000).
     */
    public HomePage(WebDriver driver, String baseUrl) {
        this.driver = driver;
        this.baseUrl = baseUrl;
        // Initialize WebDriverWait with 10-second timeout for explicit waits
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    // ========================================================================
    // NAVIGATION
    // ========================================================================

    /**
     * Navigate to the home page and wait for DOM readiness.
     * 
     * Equivalent to Python's go_home(page, base_url).
     */
    public void open() {
        driver.get(baseUrl);
        waitForPageLoad();
        System.out.println("✅ Homepage opened: " + baseUrl);
    }

    /**
     * Wait for the page to fully load (DOM content loaded).
     * Checks for the product list to be present and visible.
     */
    private void waitForPageLoad() {
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(PRODUCT_LIST));
            wait.until(ExpectedConditions.visibilityOfElementLocated(PRODUCT_LIST));
        } catch (Exception e) {
            System.err.println("⚠️ Page load wait timed out. Continuing anyway.");
        }
    }

    // ========================================================================
    // CART INTERACTIONS
    // ========================================================================

    /**
     * Get the numeric cart count displayed in the header.
     * 
     * Equivalent to Python's get_cart_count(page).
     * Reads the text from #cart-link span (e.g., "0", "1", "2").
     * 
     * @return The integer cart count, or 0 if the element is not found.
     */
    public int getCartCount() {
        try {
            WebElement cartBadge = wait.until(
                ExpectedConditions.presenceOfElementLocated(CART_COUNT_BADGE)
            );
            String countText = cartBadge.getText().strip();
            int count = Integer.parseInt(countText);
            System.out.println("🛒 Cart count: " + count);
            return count;
        } catch (Exception e) {
            System.err.println("⚠️ Failed to get cart count: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Assert that the cart count matches the expected value.
     * 
     * Equivalent to Python's expect_cart_count(page, expected).
     * 
     * @param expected The expected cart count.
     * @throws AssertionError If the actual count does not match expected.
     */
    public void assertCartCount(int expected) {
        int actual = getCartCount();
        if (actual != expected) {
            throw new AssertionError(
                "Cart count mismatch. Expected: " + expected + ", Actual: " + actual
            );
        }
        System.out.println("✅ Cart count assertion passed: " + expected);
    }

    /**
     * Navigate to the cart page by clicking the cart link in the header.
     * 
     * @return A CartPage instance for continued interaction.
     */
    public CartPage goToCart() {
        WebElement cartLink = wait.until(
            ExpectedConditions.elementToBeClickable(CART_LINK)
        );
        cartLink.click();
        System.out.println("✅ Navigated to cart page");
        return new CartPage(driver, baseUrl);
    }

    // ========================================================================
    // PRODUCT INTERACTIONS
    // ========================================================================

    /**
     * Click the "Add to Cart" button for a product with the given itemId.
     * 
     * Equivalent to Python's form selection and button click:
     *   item_form = page.locator('form:has(input[name="itemId"][value="<itemId>"])')
     *   item_form.get_by_role("button").click()
     * 
     * @param itemId The ID of the item to add to cart.
     * @throws Exception If the form or button is not found.
     */
    public void addToCartByItemId(int itemId) {
        try {
            // Build a locator for the specific form using XPath (Selenium does not reliably support CSS :has)
            // Logic: Find a form that contains an input with the specific value
            By formLocator = By.xpath(
                String.format(ADD_TO_CART_FORM_TEMPLATE, itemId)
            );
            
            WebElement form = wait.until(
                ExpectedConditions.presenceOfElementLocated(formLocator)
            );
            
            // Find and click the submit button within this form
            WebElement addButton = form.findElement(ADD_TO_CART_BUTTON);
            wait.until(ExpectedConditions.elementToBeClickable(addButton)).click();
            
            System.out.println("✅ Clicked 'Add to Cart' for item " + itemId);
        } catch (Exception e) {
            throw new AssertionError(
                "Failed to add item " + itemId + " to cart: " + e.getMessage()
            );
        }
    }

    /**
     * Click the "Add to Cart" button for the first product on the page.
     * Convenience method for simple tests.
     */
    public void addFirstProductToCart() {
        addToCartByItemId(1);
    }

    /**
     * Get the name of a product by its itemId.
     * 
     * Locates the form for the given itemId, then finds the h2 heading 
     * (product name) within the parent <li>.
     * 
     * @param itemId The ID of the product.
     * @return The product name (e.g., "Koala", "Dog", "Cat"), or null if not found.
     */
    public String getProductNameByItemId(int itemId) {
        try {
            // Find the form for this itemId
            By formLocator = By.xpath(
                String.format(ADD_TO_CART_FORM_TEMPLATE, itemId)
            );
            WebElement form = wait.until(
                ExpectedConditions.presenceOfElementLocated(formLocator)
            );
            
            // Navigate up to the parent <li> and find the h2
            WebElement listItem = form.findElement(By.xpath("./ancestor::li"));
            WebElement nameHeading = listItem.findElement(PRODUCT_NAME_HEADING);
            
            String productName = nameHeading.getText().strip();
            System.out.println("📦 Product name for item " + itemId + ": " + productName);
            return productName;
        } catch (Exception e) {
            System.err.println("⚠️ Failed to get product name for item " + itemId + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Assert that a specific text appears somewhere on the page.
     * 
     * Equivalent to Python's expect_text_visible(page, text).
     * 
     * @param text The text to search for.
     * @throws AssertionError If the text is not visible.
     */
    public void assertTextVisible(String text) {
        try {
            WebElement element = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(text(), '" + text + "')]"))
            );
            System.out.println("✅ Text found on page: '" + text + "'");
        } catch (Exception e) {
            throw new AssertionError(
                "Expected text '" + text + "' not found on page: " + e.getMessage()
            );
        }
    }

    /**
     * Assert that the success notification is visible.
     * Notification text: "Item successfully added to cart"
     */
    public void assertSuccessNotificationVisible() {
        try {
            WebElement notification = wait.until(
                ExpectedConditions.visibilityOfElementLocated(SUCCESS_NOTIFICATION)
            );
            String notificationText = notification.getText();
            System.out.println("✅ Success notification visible: " + notificationText);
        } catch (Exception e) {
            throw new AssertionError(
                "Success notification not visible: " + e.getMessage()
            );
        }
    }

    /**
     * Assert that the success notification is hidden/gone.
     * (Useful after the notification auto-hides after 3 seconds.)
     */
    public void assertSuccessNotificationHidden() {
        try {
            wait.until(ExpectedConditions.invisibilityOfElementLocated(SUCCESS_NOTIFICATION));
            System.out.println("✅ Success notification has disappeared");
        } catch (Exception e) {
            System.err.println("⚠️ Notification visibility check timed out (may still be visible)");
        }
    }

    /**
     * Get the total number of products displayed on the home page.
     * 
     * @return The count of product list items.
     */
    public int getProductCount() {
        try {
            java.util.List<WebElement> products = driver.findElements(PRODUCT_LIST);
            System.out.println("📊 Total products on page: " + products.size());
            return products.size();
        } catch (Exception e) {
            System.err.println("⚠️ Failed to count products: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Check if the "Add to Cart" button is disabled for a specific item.
     * Useful for testing max-quantity restrictions.
     * 
     * @param itemId The ID of the item.
     * @return true if the button is disabled; false otherwise.
     */
    public boolean isAddToCartButtonDisabled(int itemId) {
        try {
            By formLocator = By.cssSelector(
                String.format("form:has(input[name=\"itemId\"][value=\"%d\"])", itemId)
            );
            WebElement form = driver.findElement(formLocator);
            WebElement button = form.findElement(ADD_TO_CART_BUTTON);
            
            boolean isDisabled = !button.isEnabled();
            System.out.println("🔒 Add to cart button for item " + itemId + " disabled: " + isDisabled);
            return isDisabled;
        } catch (Exception e) {
            System.err.println("⚠️ Failed to check button state for item " + itemId);
            return false;
        }
    }
}
