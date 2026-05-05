package com.example.testing;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.testing.models.Category;
import com.example.testing.models.Product;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides product catalog from Firebase with real-time updates.
 */
public class ProductDataProvider {

    private static final String TAG = "ProductDataProvider";
    private static List<Product> cachedProducts = new ArrayList<>();
    private static List<Category> cachedCategories = new ArrayList<>();
    private static ValueEventListener productListener;

    public interface ProductFetchListener {
        void onProductsFetched(List<Product> products);
        void onError(String error);
    }

    public interface CategoryFetchListener {
        void onCategoriesFetched(List<Category> categories);
        void onError(String error);
    }

    /**
     * Fetches all products and listens for real-time updates.
     */
    public static void fetchAllProducts(ProductFetchListener listener) {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        
        // Try various common node names
        String[] nodesToTry = {"products", "Products", "catalog", "items"};
        tryNextNode(rootRef, nodesToTry, 0, listener);
    }

    private static void tryNextNode(DatabaseReference rootRef, String[] nodes, int index, ProductFetchListener listener) {
        if (index >= nodes.length) {
            if (listener != null) listener.onError("Permission denied or products node not found");
            return;
        }

        String nodeName = nodes[index];
        DatabaseReference nodeRef = rootRef.child(nodeName);

        if (productListener != null) {
            // Remove previous listener if switching nodes or re-initializing
            nodeRef.removeEventListener(productListener);
        }

        productListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.hasChildren()) {
                    Log.d(TAG, "Successfully found products at node: " + nodeName);
                    Log.d("DATA_CHECK", "Total products found at /" + nodeName + ": " + snapshot.getChildrenCount());
                    processSnapshot(snapshot, listener);
                } else {
                    Log.d(TAG, "Node " + nodeName + " is empty or doesn't exist.");
                    tryNextNode(rootRef, nodes, index + 1, listener);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Node " + nodeName + " failed: " + error.getMessage());
                if (error.getCode() == DatabaseError.PERMISSION_DENIED) {
                    tryNextNode(rootRef, nodes, index + 1, listener);
                } else if (listener != null) {
                    listener.onError(error.getMessage());
                }
            }
        };

        nodeRef.addValueEventListener(productListener);
    }

    private static void processSnapshot(DataSnapshot snapshot, ProductFetchListener listener) {
        cachedProducts.clear();
        for (DataSnapshot child : snapshot.getChildren()) {
            try {
                Product p = child.getValue(Product.class);
                if (p != null) {
                    // Support both 'id' and 'productId' fields from Firebase
                    if (p.productId == null) p.productId = child.getKey();
                    cachedProducts.add(p);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error mapping product: " + e.getMessage());
            }
        }
        if (listener != null) {
            listener.onProductsFetched(new ArrayList<>(cachedProducts));
        }
    }

    public static void fetchAllCategories(CategoryFetchListener listener) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("categories");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cachedCategories.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        Category c = child.getValue(Category.class);
                        if (c != null) {
                            cachedCategories.add(c);
                        }
                    }
                }
                
                // Fallback: derive from products if dedicated node is empty
                if (cachedCategories.isEmpty()) {
                    deriveCategoriesFromProducts();
                }

                if (listener != null) listener.onCategoriesFetched(new ArrayList<>(cachedCategories));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "Categories node inaccessible: " + error.getMessage());
                // Fallback: derive from products on permission error too
                deriveCategoriesFromProducts();
                if (listener != null) listener.onCategoriesFetched(new ArrayList<>(cachedCategories));
            }
        });
    }

    private static void deriveCategoriesFromProducts() {
        cachedCategories.clear();
        java.util.Set<String> catNames = new java.util.HashSet<>();
        for (Product p : cachedProducts) {
            if (p.getCategory() != null) catNames.add(p.getCategory());
        }
        for (String name : catNames) {
            cachedCategories.add(new Category(name, null));
        }
    }

    public static List<Product> getCachedProducts() {
        return cachedProducts;
    }

    public static List<Product> getProductsByCategory(String category) {
        List<Product> filtered = new ArrayList<>();
        if (category == null) return filtered;
        
        String query = category.toLowerCase().trim();
        for (Product p : cachedProducts) {
            if (p.getCategory() != null) {
                String pCat = p.getCategory().toLowerCase().trim();
                // Match exact or contains for better robustness
                if (pCat.contains(query) || query.contains(pCat)) {
                    filtered.add(p);
                }
            }
        }
        return filtered;
    }

    public static void getProductById(String productId, ProductFetchListener listener) {
        // Try cache first
        for (Product p : cachedProducts) {
            if (productId.equals(p.productId)) {
                List<Product> list = new ArrayList<>();
                list.add(p);
                listener.onProductsFetched(list);
                return;
            }
        }

        // Fetch from Firebase if not in cache (assuming /products node)
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("products").child(productId);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Product p = snapshot.getValue(Product.class);
                if (p != null) {
                    if (p.productId == null) p.productId = snapshot.getKey();
                    List<Product> list = new ArrayList<>();
                    list.add(p);
                    listener.onProductsFetched(list);
                } else {
                    listener.onError("Product not found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onError(error.getMessage());
            }
        });
    }

    public static List<Product> searchProducts(String query) {
        List<Product> results = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>(cachedProducts);
        }
        String lowerQuery = query.toLowerCase().trim();
        for (Product p : cachedProducts) {
            if ((p.getName() != null && p.getName().toLowerCase().contains(lowerQuery)) ||
                (p.getCategory() != null && p.getCategory().toLowerCase().contains(lowerQuery)) ||
                (p.getDescription() != null && p.getDescription().toLowerCase().contains(lowerQuery))) {
                results.add(p);
            }
        }
        return results;
    }
}
