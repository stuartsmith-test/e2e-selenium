package automation.db;

import automation.BaseTest;
import automation.utils.DbUtils;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DbSanityTest extends BaseTest {

    @Test
    public void testDatabaseConnection() throws Exception {
        System.out.println("🔌 Connecting to Database at: " + dbPath);

        // 1. Query the 'items' table (This table should always have data)
        List<Map<String, Object>> items = DbUtils.fetchAll(dbPath, "SELECT * FROM items");
        
        // 2. Print results
        System.out.println("✅ Connection Successful!");
        System.out.println("📦 Found " + items.size() + " products in the catalog.");
        
        // 3. Simple Assertion
        assertTrue(items.size() > 0, "The database should contain products.");
        
        // Optional: Print the first item name just to be sure
        System.out.println("First Product: " + items.get(0).get("name"));
    }
}
