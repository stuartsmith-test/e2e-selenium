package automation.ui;

import automation.BaseTest;
import automation.pages.CartPage;
import automation.pages.CheckoutPage;
import automation.pages.HomePage;
import automation.utils.ApiUtils;
import automation.utils.DbUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * UI Tests for the shopping cart "Add to Cart" workflow.
 * 
 * These tests validate the end-to-end user experience from homepage 
 * through product selection, cart management, and checkout completion.
 * 
 * Uses the Page Object Model (HomePage, CartPage, CheckoutPage) to 
 * encapsulate UI interactions and locators.
 * 
 * Ported from e2e-playwright/tests/ui/test_add_to_cart_req.py.
 */
public class AddToCartTest extends BaseTest {

    /**
     * Test 1: Homepage Load and Product Display
     * 
     * Validates that the homepage renders successfully and seeded 
     * product data (like "Koala") is visible on initial load.
     * 
     * This is a smoke test to ensure the app is running and reachable.
     */
    @Test
    public void testHomepageLoadsAndShowsItems() {
        // Arrange: Initialize page object
        HomePage homePage = new HomePage(driver, baseUrl);

        // Act: Open homepage
        homePage.open();

        // Assert: Verify seeded product name is visible
        homePage.assertTextVisible("Koala");
        
        System.out.println("✅ Homepage test passed");
    }

    /**
     * Test 2: Add to Cart with Visual Feedback and Counter Update
     * 
     * Validates the core add-to-cart workflow:
     *   1. Reset cart to clean state via API.
     *   2. Verify cart counter starts at 0.
     *   3. Add an item via the UI.
     *   4. Verify success notification appears.
     *   5. Verify cart counter increments to 1.
     * 
     * This test ensures the UI correctly reflects API state changes.
     */
    @Test
    public void testAddToCartShowsMessageAndUpdatesCount() {
        // Arrange: Reset cart via API to clean state
        ApiUtils.resetCart(baseUrl);
        
        // Initialize page object and open homepage
        HomePage homePage = new HomePage(driver, baseUrl);
        homePage.open();

        // Assert: Cart starts empty
        assertEquals(0, homePage.getCartCount(), "Cart should start at 0");

        // Act: Add first item to cart
        homePage.addFirstProductToCart();

        // Assert: Verify success feedback
        homePage.assertSuccessNotificationVisible();
        
        // Assert: Verify cart counter updated
        homePage.assertCartCount(1);

        System.out.println("✅ Add to cart test passed");
    }

    /**
     * Test 3: Button Disables at Maximum Quantity
     * 
     * Validates business logic that prevents over-ordering:
     *   1. Add the same item 10 times (maximum allowed quantity).
     *   2. Verify the "Add to Cart" button becomes disabled.
     *   3. Verify the UI displays "Maximum quantity reached" message.
     * 
     * Critical Note: Between each add, we wait for the success notification 
     * to appear AND disappear before clicking again. This prevents "Stale Element" 
     * errors caused by the notification pushing the page down (DOM shift).
     * 
     * See HomePage.assertSuccessNotificationHidden() for details.
     */
    @Test
    public void testAddButtonDisablesAtMaxQuantity() {
        // Arrange: Reset cart and open homepage
        ApiUtils.resetCart(baseUrl);
        HomePage homePage = new HomePage(driver, baseUrl);
        homePage.open();

        // Act: Click "Add to Cart" 10 times (max quantity)
        for (int i = 0; i < 10; i++) {
            // Click the button
            homePage.addFirstProductToCart();
            
            // Wait for the notification to appear
            homePage.assertSuccessNotificationVisible();
            
            // CRITICAL FIX: Wait for the notification to disappear before clicking again.
            // The notification pushes page content down (layout shift), which can cause 
            // the button to become stale. This ensures the DOM is stable.
            homePage.assertSuccessNotificationHidden();
        }

        // Assert: Button is disabled at max quantity
        assertTrue(
            homePage.isAddToCartButtonDisabled(1),
            "Add to cart button should be disabled at max quantity"
        );
        
        // Assert: Max quantity message is displayed
        homePage.assertTextVisible("Maximum quantity reached");
        
        System.out.println("✅ Max quantity test passed");
    }

    /**
     * Test 4: End-to-End Workflow: Add Item → View Cart → Checkout
     * 
     * A full integration test validating the complete user journey:
     *   1. Reset cart via API.
     *   2. Open homepage and retrieve product name (before DOM changes).
     *   3. Add item to cart via UI.
     *   4. Navigate to cart page.
     *   5. Verify product details in cart.
     *   6. Proceed to checkout.
     *   7. Verify checkout confirmation.
     * 
     * Critical Note: Product name is read BEFORE adding to cart to avoid 
     * DOM mutations and stale references. This ensures reliable assertions 
     * on the cart and checkout pages.
     */
    @Test
    public void testE2EAddToCartAndCheckout() throws Exception {
        // Arrange: Reset cart to clean state
        ApiUtils.resetCart(baseUrl);
        
        // Initialize homepage and open it
        HomePage homePage = new HomePage(driver, baseUrl);
        homePage.open();

        // CRITICAL: Read product name BEFORE any interactions.
        // DOM changes can cause stale element references if we read after clicking.
        String productName = homePage.getProductNameByItemId(1);
        
        // Sanity check: Verify we successfully retrieved the product name
        if (productName == null) {
            throw new RuntimeException(
                "Failed to read product name from HomePage. " +
                "Check HomePage.getProductNameByItemId() XPath logic."
            );
        }

        // Act: Add item to cart
        homePage.addFirstProductToCart();
        
        // Assert: Cart counter updated
        homePage.assertCartCount(1);

        // Navigate to cart page
        CartPage cartPage = homePage.goToCart();

        // Assert: Product is in cart with correct quantity
        // Now 'productName' is safely populated (e.g., "Koala")
        cartPage.assertProductInCart(productName);
        cartPage.assertProductQuantity(productName, 1);

        // DATABASE VALIDATION
        // Verify the backend (SQLite) actually recorded the item
        int dbQuantity = DbUtils.getCartQuantity(dbPath, 1);
        assertEquals(1, dbQuantity, "Database should show 1 item in cart");

        // Act: Proceed to checkout
        CheckoutPage checkoutPage = cartPage.clickCheckout();

        // Assert: Checkout confirmation is displayed
        checkoutPage.assertThankYouMessageVisible();
        checkoutPage.assertPageTitle();

        System.out.println("✅ E2E checkout test passed");
    }
}