package automation.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Database utility class for interacting with the SQLite shop.db.
 * 
 * Ported from e2e-playwright/utils/dbHelpers.py.
 * Uses standard Java SQL (java.sql.Connection, DriverManager, ResultSet).
 */
public class DbUtils {

    /**
     * Establish a new SQLite connection to the given database path.
     * 
     * @param dbPath The absolute or relative path to shop.db (e.g., "C:/Users/.../shop.db")
     * @return A new SQLite Connection.
     * @throws SQLException If the connection fails.
     */
    public static Connection getConnection(String dbPath) throws SQLException {
        String jdbcUrl = "jdbc:sqlite:" + dbPath;
        return DriverManager.getConnection(jdbcUrl);
    }

    /**
     * Execute a SELECT query and return the first row as a Map (or null if no rows).
     * 
     * Equivalent to Python's fetch_one(query, params).
     * 
     * @param dbPath The absolute or relative path to shop.db.
     * @param query SQL SELECT statement with optional ? placeholders.
     * @param params Query parameters (in order matching ? placeholders). Can be empty.
     * @return A Map with column name → value, or null if no rows found.
     * @throws SQLException If the query fails.
     */
    public static Map<String, Object> fetchOne(String dbPath, String query, Object... params) 
            throws SQLException {
        try (Connection conn = getConnection(dbPath);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            // Bind parameters
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            
            // Execute and retrieve first row
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return resultSetToMap(rs);
                }
            }
        }
        return null;
    }

    /**
     * Execute a SELECT query and return all rows as a List of Maps.
     * 
     * Equivalent to Python's fetch_all(query, params).
     * 
     * @param dbPath The absolute or relative path to shop.db.
     * @param query SQL SELECT statement with optional ? placeholders.
     * @param params Query parameters (in order matching ? placeholders). Can be empty.
     * @return A List of Maps, each Map representing a row. Empty list if no rows found.
     * @throws SQLException If the query fails.
     */
    public static List<Map<String, Object>> fetchAll(String dbPath, String query, Object... params) 
            throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        
        try (Connection conn = getConnection(dbPath);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            // Bind parameters
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            
            // Execute and collect all rows
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    results.add(resultSetToMap(rs));
                }
            }
        }
        return results;
    }

    /**
     * Execute a write (INSERT/UPDATE/DELETE) and commit.
     * 
     * Equivalent to Python's execute_query(query, params).
     * 
     * @param dbPath The absolute or relative path to shop.db.
     * @param query SQL INSERT/UPDATE/DELETE statement with optional ? placeholders.
     * @param params Query parameters (in order matching ? placeholders). Can be empty.
     * @throws SQLException If the query fails.
     */
    public static void executeQuery(String dbPath, String query, Object... params) 
            throws SQLException {
        try (Connection conn = getConnection(dbPath);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            // Bind parameters
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            
            // Execute and commit (auto-commit enabled by default)
            pstmt.executeUpdate();
        }
    }

    /**
     * Delete all rows from the given table (simple test cleanup helper).
     * 
     * Equivalent to Python's reset_table(table_name).
     * 
     * @param dbPath The absolute or relative path to shop.db.
     * @param tableName The name of the table to clear (e.g., "cart").
     * @throws SQLException If the query fails.
     */
    public static void resetTable(String dbPath, String tableName) throws SQLException {
        executeQuery(dbPath, "DELETE FROM " + tableName);
    }

    /**
     * Return the quantity for an item in the cart (or 0 if not present).
     * 
     * Equivalent to Python's get_cart_quantity(item_id).
     * 
     * @param dbPath The absolute or relative path to shop.db.
     * @param itemId The ID of the item.
     * @return The quantity in the cart, or 0 if the item is not in the cart.
     * @throws SQLException If the query fails.
     */
    public static int getCartQuantity(String dbPath, int itemId) throws SQLException {
        Map<String, Object> row = fetchOne(
            dbPath, 
            "SELECT quantity FROM cart WHERE item_id = ?", 
            itemId
        );
        
        if (row != null && row.containsKey("quantity")) {
            Object qty = row.get("quantity");
            if (qty instanceof Integer) {
                return (Integer) qty;
            } else if (qty instanceof Long) {
                return ((Long) qty).intValue();
            }
        }
        return 0;
    }

    /**
     * Return the display name for an item, or null if not found.
     * 
     * Equivalent to Python's get_item_name(item_id).
     * 
     * @param dbPath The absolute or relative path to shop.db.
     * @param itemId The ID of the item.
     * @return The item's name (e.g., "Koala"), or null if not found.
     * @throws SQLException If the query fails.
     */
    public static String getItemName(String dbPath, int itemId) throws SQLException {
        Map<String, Object> row = fetchOne(
            dbPath, 
            "SELECT name FROM items WHERE id = ?", 
            itemId
        );
        
        if (row != null && row.containsKey("name")) {
            return (String) row.get("name");
        }
        return null;
    }

    /**
     * Return the total count of items in the cart.
     * 
     * Useful for validating cart state without checking individual items.
     * 
     * @param dbPath The absolute or relative path to shop.db.
     * @return The sum of all quantities in the cart, or 0 if cart is empty.
     * @throws SQLException If the query fails.
     */
    public static int getCartTotal(String dbPath) throws SQLException {
        Map<String, Object> row = fetchOne(
            dbPath, 
            "SELECT SUM(quantity) AS total FROM cart"
        );
        
        if (row != null && row.containsKey("total")) {
            Object total = row.get("total");
            if (total instanceof Integer) {
                return (Integer) total;
            } else if (total instanceof Long) {
                return ((Long) total).intValue();
            }
        }
        return 0;
    }

    /**
     * Convert a ResultSet row into a Map for easy column access by name.
     * 
     * Helper method used internally by fetchOne/fetchAll.
     * 
     * @param rs The ResultSet positioned at a valid row.
     * @return A Map with column name → value (as Object).
     * @throws SQLException If metadata retrieval fails.
     */
    private static Map<String, Object> resultSetToMap(ResultSet rs) throws SQLException {
        Map<String, Object> map = new HashMap<>();
        int columnCount = rs.getMetaData().getColumnCount();
        
        for (int i = 1; i <= columnCount; i++) {
            String columnName = rs.getMetaData().getColumnName(i);
            Object value = rs.getObject(i);
            map.put(columnName, value);
        }
        
        return map;
    }
}
