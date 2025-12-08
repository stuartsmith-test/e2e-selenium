package automation.utils;

import io.restassured.RestAssured;
import io.restassured.response.Response;

/**
 * API utility class for interacting with the shopping cart endpoints.
 * 
 * Ported from e2e-playwright/utils/api_helpers.py.
 * Uses Rest Assured for HTTP requests.
 */
public class ApiUtils {

    /**
     * Reset the cart via POST /reset-cart.
     * 
     * Equivalent to Python's reset_cart(api_request_context).
     * 
     * @param baseUrl The base URL of the application (e.g., http://localhost:3000)
     * @return The response from the server.
     * @throws AssertionError If the response status is not OK (2xx).
     */
    public static Response resetCart(String baseUrl) {
        Response response = RestAssured
            .given()
                .baseUri(baseUrl)
            .when()
                .post("/reset-cart");

        // Assert success (equivalent to Python's `assert response.ok`)
        if (!isSuccessStatus(response.getStatusCode())) {
            throw new AssertionError(
                "Reset cart failed. Status: " + response.getStatusCode() + 
                ". Body: " + response.getBody().asString()
            );
        }

        System.out.println("✅ Cart reset successfully (status: " + response.getStatusCode() + ")");
        return response;
    }

    /**
     * Add a single item to the cart via POST /add-to-cart.
     * 
     * Equivalent to Python's add_to_cart(api_request_context, item_id).
     * 
     * @param baseUrl The base URL of the application.
     * @param itemId The ID of the item to add.
     * @return The response from the server.
     * @throws AssertionError If the response status is not OK (2xx).
     */
    public static Response addToCart(String baseUrl, int itemId) {
        Response response = RestAssured
            .given()
                .baseUri(baseUrl)
                .formParam("itemId", itemId)
            .when()
                .post("/add-to-cart");

        // Assert success
        if (!isSuccessStatus(response.getStatusCode())) {
            throw new AssertionError(
                "Add to cart failed for item " + itemId + 
                ". Status: " + response.getStatusCode() + 
                ". Body: " + response.getBody().asString()
            );
        }

        System.out.println("✅ Item " + itemId + " added to cart (status: " + response.getStatusCode() + ")");
        return response;
    }

    /**
     * Helper method: determine if HTTP status code indicates success.
     * Accepts 2xx (success) and 3xx (redirect) as valid for this app.
     * 
     * @param statusCode The HTTP status code to check.
     * @return true if status is 2xx or 3xx; false otherwise.
     */
    private static boolean isSuccessStatus(int statusCode) {
        return statusCode >= 200 && statusCode < 400;
    }

    /**
     * Get the cart count (number of items) via a GET request to /cart.
     * 
     * Optional utility for future API assertions.
     * 
     * @param baseUrl The base URL of the application.
     * @return The response containing the cart page HTML.
     */
    public static Response getCart(String baseUrl) {
        return RestAssured
            .given()
                .baseUri(baseUrl)
            .when()
                .get("/cart");
    }
}