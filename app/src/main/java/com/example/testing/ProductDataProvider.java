package com.example.testing;

import com.example.famcart.R;
import com.example.testing.models.Product;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides the local product catalog.
 * Products are loaded once and filtered locally for search.
 */
public class ProductDataProvider {

    private static List<Product> allProducts;

    public static List<Product> getAllProducts() {
        if (allProducts == null) {
            allProducts = new ArrayList<>();

            // Dairy, Milk & Eggs
            allProducts.add(new Product("p1", "Yakult Probiotic Drink", "Yakult probiotic health drink for gut health. Contains live Lactobacillus casei strain Shirota.", "Dairy & Milk", "65ml (Pack of 5)", 90, 110, 4.5, R.drawable.yakultfavv));
            allProducts.add(new Product("p2", "Amul Taaza Milk", "Amul Taaza Toned Milk. Pasteurized and homogenized. Rich source of calcium and protein.", "Dairy & Milk", "500ml", 28, 30, 4.3, R.drawable.amul_milk));
            allProducts.add(new Product("p3", "Epigamia Greek Yogurt", "Epigamia Greek Yogurt – Strawberry flavour. High protein, no preservatives.", "Dairy & Milk", "90g", 45, 55, 4.6, R.drawable.epigamia));

            // Bread & Bakery
            allProducts.add(new Product("p4", "English Oven White Bread", "English Oven premium white bread. Soft and fresh for everyday sandwiches.", "Bread & Bakery", "400g", 45, 50, 4.2, R.drawable.bread_english));
            allProducts.add(new Product("p5", "Unibic Cookies Choco Chip", "Unibic choco chip cookies. Crunchy with real chocolate chips. Perfect tea-time snack.", "Bread & Bakery", "300g", 99, 120, 4.4, R.drawable.unibic_cookies));
            allProducts.add(new Product("p6", "Britannia Elaichi Rusk", "Britannia Toastea Premium Elaichi Rusk. Rich aroma and crunchy texture.", "Bread & Bakery", "290g", 55, 65, 4.1, R.drawable.rusk));

            // Cold Drinks & Juices
            allProducts.add(new Product("p7", "Coca-Cola", "Coca-Cola Classic. Refreshing carbonated beverage. Serve chilled for best taste.", "Cold Drinks & Juices", "750ml", 40, 45, 4.3, R.drawable.coca_cola));
            allProducts.add(new Product("p8", "Mogu Mogu Lychee", "Mogu Mogu Lychee flavoured drink with Nata de Coco. Fun textured refreshing drink.", "Cold Drinks & Juices", "300ml", 60, 70, 4.5, R.drawable.ic_mogu_mogu));
            allProducts.add(new Product("p9", "Amul Kool Milkshake", "Amul Kool Koko milkshake. Chocolate flavoured milk-based drink.", "Cold Drinks & Juices", "200ml", 25, 30, 4.0, R.drawable.amul_kool));

            // Snacks
            allProducts.add(new Product("p10", "Amul Ice Cream", "Amul Vanilla Magic ice cream. Creamy and delicious. Made with real milk.", "Snacks", "750ml", 260, 299, 4.7, R.drawable.amul_icecreams));

            // Personal Care
            allProducts.add(new Product("p11", "Bath Essentials Kit", "Complete bath essentials kit with soap, shampoo, and conditioner.", "Personal Care", "1 Kit", 349, 450, 4.2, R.drawable.bath_essentials));

            // Pharmacy
            allProducts.add(new Product("p12", "Pharmacy Doorstep Delivery", "Get your medicines delivered to your doorstep. Quality healthcare products.", "Pharmacy", "Per Order", 0, 0, 4.8, R.drawable.pharmacy_doorstep));
        }
        return allProducts;
    }

    public static List<Product> getProductsByCategory(String category) {
        List<Product> filtered = new ArrayList<>();
        for (Product p : getAllProducts()) {
            if (p.getCategory().equalsIgnoreCase(category)) {
                filtered.add(p);
            }
        }
        return filtered;
    }

    public static Product getProductById(String productId) {
        for (Product p : getAllProducts()) {
            if (p.getProductId().equals(productId)) {
                return p;
            }
        }
        return null;
    }

    public static List<Product> searchProducts(String query) {
        List<Product> results = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) {
            return getAllProducts();
        }
        String lowerQuery = query.toLowerCase().trim();
        for (Product p : getAllProducts()) {
            if (p.getName().toLowerCase().contains(lowerQuery) ||
                p.getCategory().toLowerCase().contains(lowerQuery) ||
                p.getDescription().toLowerCase().contains(lowerQuery)) {
                results.add(p);
            }
        }
        return results;
    }
}
