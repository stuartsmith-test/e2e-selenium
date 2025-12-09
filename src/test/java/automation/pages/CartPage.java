package automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.Select;
import java.time.Duration;
import java.util.List;

/**
 * Page Object Model for the shopping cart page.
 * 
 * Encapsulates all interactions with the /cart page.
 */
public class CartPage {
    
    private final WebDriver driver;
    private final String baseUrl;
    private final WebDriverWait wait;

    // ========================================================================
    // LOCATORS
    // ========================================================================

    /**
     * Cart table: table element
     */
    private static final By CART_TABLE = By.tagName("table");

    /**
     * Table rows: tbody > tr
     */
    private static final By CART_ITEMS = By.cssSelector("tbody > tr");

    /**
     * Product name cell (first td in a row): tr > td:nth-child(1)
     */
    private static final By PRODUCT_NAME_CELL = By.cssSelector("td:nth-child(1)");

    /**
     * Quantity cell (second td in a row): tr > td:nth-child(2)
     */
    private static final By QUANTITY_CELL = By.cssSelector("td:nth-child(2)");

    /**
     * Price cell (third td in a row): tr > td:nth-child(3)
     */
    private static final By PRICE_CELL = By.cssSelector("td:nth-child(3)");

    /**
     * Total price display: h2 (contains "Total Price: $X.XX")
     */
    private static final By TOTAL_PRICE_HEADING = By.cssSelector("h2");

    /**
     * Checkout button: button#checkout-button
     */
    private static final By CHECKOUT_BUTTON = By.id("checkout-button");

    /**
     * Back to shop link: a#shop-link
     */
    private static final By SHOP_LINK = By.id("shop-link");

    // ========================================================================
    // CONSTRUCTOR
    // ========================================================================

    /**
     * Initialize the CartPage with a WebDriver and base URL.
     * 
     * @param driver The Selenium WebDriver instance.
     * @param baseUrl The base URL of the application.
     */
    public CartPage(WebDriver driver, String baseUrl) {
        this.driver = driver;
        this.baseUrl = baseUrl;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        
        // Verify we're on the cart page
        waitForCartPageLoad();
    }

    /**
     * Wait for the cart page to fully load.
     */
    private void waitForCartPageLoad() {
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(CART_TABLE));
            System.out.println("✅ Cart page loaded");
        } catch (Exception e) {
            System.err.println("⚠️ Cart page load timed out: " + e.getMessage());
        }
    }

    // ========================================================================
    // CART CONTENT ASSERTIONS
    // ========================================================================

    /**
     * Get all items currently in the cart.
     * 
     * @return A list of rows (WebElements) from the cart table.
     */
    public List<WebElement> getCartItems() {
        try {
            return driver.findElements(CART_ITEMS);
        } catch (Exception e) {
            System.err.println("⚠️ Failed to get cart items: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Assert that a product with the given name appears in the cart.
     * 
     * @param productName The name of the product (e.g., "Koala", "Dog").
     * @throws AssertionError If the product is not found in the cart.
     */
    public void assertProductInCart(String productName) {
        try {
            WebElement row = driver.findElement(
                By.xpath("//tr[contains(., '" + productName + "')]")
            );
            System.out.println("✅ Product '" + productName + "' found in cart");
        } catch (Exception e) {
            throw new AssertionError(
                "Product '" + productName + "' not found in cart: " + e.getMessage()
            );
        }
    }

    /**
     * Get the quantity of a specific product in the cart.
     * 
     * @param productName The name of the product.
     * @return The quantity as an integer, or -1 if not found.
     */
    public int getProductQuantity(String productName) {
        try {
        // 1. Find the row for the product
        WebElement row = driver.findElement(
            By.xpath("//tr[td[1][contains(text(), '" + productName + "')]]")
        );
        
        // 2. Find the Dropdown inside the quantity cell
        WebElement quantityCell = row.findElement(QUANTITY_CELL);
        WebElement dropdown = quantityCell.findElement(By.tagName("select"));
        
        // 3. Read the currently selected option
        Select select = new Select(dropdown);
        String selectedText = select.getFirstSelectedOption().getText().strip();
        
        // 4. Parse it into a variable (Cleaner & better for debugging)
        int quantity = Integer.parseInt(selectedText);
        
        System.out.println("📦 Quantity for '" + productName + "': " + quantity);
        return quantity;
        
    } catch (Exception e) {
        System.err.println("⚠️ Failed to get quantity for '" + productName + "': " + e.getMessage());
        return -1;
        }
    }

    /**
     * Assert that a product appears in the cart with a specific quantity.
     * 
     * @param productName The name of the product.
     * @param expectedQuantity The expected quantity.
     * @throws AssertionError If the quantity does not match.
     */
    public void assertProductQuantity(String productName, int expectedQuantity) {
        int actual = getProductQuantity(productName);
        if (actual != expectedQuantity) {
            throw new AssertionError(
                "Quantity mismatch for '" + productName + "'. Expected: " + expectedQuantity 
                + ", Actual: " + actual
            );
        }
        System.out.println("✅ Product quantity assertion passed: '" + productName + "' = " + expectedQuantity);
    }

    /**
     * Get the total price displayed on the cart page.
     * Parses the text "Total Price: $XX.XX"
     * 
     * @return The total price as a double, or -1 if not found.
     */
    public double getTotalPrice() {
        try {
            WebElement totalHeading = wait.until(
                ExpectedConditions.presenceOfElementLocated(TOTAL_PRICE_HEADING)
            );
            String totalText = totalHeading.getText(); // e.g., "Total Price: $45.99"
            
            // Extract numeric value
            String priceValue = totalText.replaceAll("[^0-9.]", "");
            double price = Double.parseDouble(priceValue);
            System.out.println("💰 Total price: $" + price);
            return price;
        } catch (Exception e) {
            System.err.println("⚠️ Failed to get total price: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Assert that the cart is empty (no items displayed).
     */
    public void assertCartEmpty() {
        List<WebElement> items = getCartItems();
        if (!items.isEmpty()) {
            throw new AssertionError(
                "Expected empty cart, but found " + items.size() + " items"
            );
        }
        System.out.println("✅ Cart is empty");
    }

    // ========================================================================
    // NAVIGATION & ACTIONS
    // ========================================================================

    /**
     * Click the Checkout button to proceed to checkout.
     * 
     * @return A CheckoutPage instance for continued interaction.
     */
    public CheckoutPage clickCheckout() {
        try {
            WebElement checkoutBtn = wait.until(
                ExpectedConditions.elementToBeClickable(CHECKOUT_BUTTON)
            );
            checkoutBtn.click();
            System.out.println("✅ Clicked Checkout button");
            return new CheckoutPage(driver, baseUrl);
        } catch (Exception e) {
            throw new AssertionError("Failed to click Checkout button: " + e.getMessage());
        }
    }

    /**
     * Click the "Back to Shop" link to return to the home page.
     * 
     * @return A HomePage instance for continued interaction.
     */
    public HomePage clickBackToShop() {
        try {
            WebElement shopLink = wait.until(
                ExpectedConditions.elementToBeClickable(SHOP_LINK)
            );
            shopLink.click();
            System.out.println("✅ Clicked Back to Shop link");
            return new HomePage(driver, baseUrl);
        } catch (Exception e) {
            throw new AssertionError("Failed to click Back to Shop: " + e.getMessage());
        }
    }

    /**
     * Update the quantity for a product and wait for the price to change.
     * * Handles the specific behavior where the app dynamically recalculates the total
     * price immediately after the dropdown selection changes.
     * * @param productName The name of the product to update.
     * @param newQuantity The new quantity to select (0-10).
     */
    public void setProductQuantity(String productName, int newQuantity) {
        try {
            // 1. Get current price so we can wait for it to change
            double oldPrice = getTotalPrice();
            
            // 2. Find the row and the dropdown
            // (Using the safer XPath to ensure we get the specific product's row)
            WebElement row = driver.findElement(
                By.xpath("//tr[td[1][contains(text(), '" + productName + "')]]")
            );
            WebElement dropdown = row.findElement(QUANTITY_CELL).findElement(By.tagName("select"));
            
            // 3. Change the selection
            Select select = new Select(dropdown);
            select.selectByVisibleText(String.valueOf(newQuantity));
            
            // 4. Wait for the price to update
            // We wait until the H2 text NO LONGER matches the old price.
            // This confirms the app processed the change.
            wait.until(ExpectedConditions.not(
                ExpectedConditions.textToBePresentInElementLocated(
                    TOTAL_PRICE_HEADING, String.valueOf(oldPrice)
                )
            ));
            
            System.out.println("✅ Updated quantity for '" + productName + "' to " + newQuantity);
            
        } catch (Exception e) {
            throw new AssertionError(
                "Failed to set quantity for '" + productName + "': " + e.getMessage()
            );
        }
    }
}
