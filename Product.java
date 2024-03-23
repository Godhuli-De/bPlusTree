/**
 * Author: Asif Qureshi, Cole Keller, Goldhuli, Sharjeel
 * Represent a product with product id and description.
 * To enable sorting of Product objects later on, this class implements the Comparable interface.
 */
public class Product implements Comparable<Product> {

    String id;
    String description;

    /**
     * Default Constructor
     *
     * @param id:          product id
     * @param description: product description
     */
    public Product(String id, String description) {
        this.id = id;
        this.description = description;
    }

    /**
     * Getter for ID
     *
     * @return product id
     */
    public String getId() {
        return id;
    }

    /**
     * Getter for description
     *
     * @return product description
     */
    public String getDescription() {
        return description;
    }

    /**
     * enable sorting of Product objects by their ID.
     */
    @Override
    public int compareTo(Product o) {
        return id.compareTo(o.id);
    }

    @Override
    public String toString() {
        return "[" + id +
                " : " + description +
                ']';
    }
}

