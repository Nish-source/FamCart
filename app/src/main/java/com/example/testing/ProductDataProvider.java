package com.example.testing;

import com.example.testing.models.Product;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class ProductDataProvider {

    private static final List<Product> allProducts = new ArrayList<>();

    public interface ProductLoadListener {
        void onProductsLoaded(List<Product> products);
    }

    public static void loadProducts(ProductLoadListener listener) {

        DatabaseReference ref = FirebaseDatabase
                .getInstance()
                .getReference("products");

        ref.get().addOnCompleteListener(task -> {

            allProducts.clear();

            if (task.isSuccessful() && task.getResult() != null) {

                for (DataSnapshot snapshot : task.getResult().getChildren()) {

                    Product product = snapshot.getValue(Product.class);

                    if (product != null) {

                        product.setProductId(snapshot.getKey());

                        allProducts.add(product);
                    }
                }
            }

            listener.onProductsLoaded(allProducts);
        });
    }

    public static List<Product> searchProducts(String query) {

        List<Product> results = new ArrayList<>();

        if (query == null || query.trim().isEmpty()) {
            return allProducts;
        }

        String lower = query.toLowerCase();

        for (Product p : allProducts) {

            if (
                    (p.getName() != null &&
                            p.getName().toLowerCase().contains(lower))

                            ||

                            (p.getCategory() != null &&
                                    p.getCategory().toLowerCase().contains(lower))

                            ||

                            (p.getDescription() != null &&
                                    p.getDescription().toLowerCase().contains(lower))
            ) {

                results.add(p);
            }
        }

        return results;
    }

    public static Product getProductById(String id) {

        for (Product p : allProducts) {

            if (p.getProductId() != null &&
                    p.getProductId().equals(id)) {

                return p;
            }
        }

        return null;
    }
}