import java.util.*;

/**
 * Author: Asif Qureshi, Cole Keller, Goldhuli, Sharjeel
 * This class serves as a representation of the leaf nodes in the B+ tree,
 * which store key-value pairs. Leaf nodes do not have any children.
 * The minimum and maximum number of key-value pairs that a leaf node can
 * contain are determined by the max degree of the B+ tree, denoted by m.
 * The leaf nodes are connected in a doubly linked list, with each leaf node
 * having a left and right sibling.
 */
public class BPlusLeafNode extends BPlusNode {

    public static final int MAX_ENTRY = 16;
    public static final int MIN_ENTRY = 16;

    public int numberOfEntry;
    public BPlusLeafNode leftSibling;
    public BPlusLeafNode rightSibling;
    public Product[] products;

    /**
     * Constructor to initialize a leaf node with a product.
     * @param product: first dictionary pair insert into new node
     */
    public BPlusLeafNode(Product product) {
        super();
        this.products = new Product[MAX_ENTRY];
        this.numberOfEntry = 0;
        this.insertProduct(product);
    }

    /**
     * Constructor to initialize a leaf node.
     *
     * @param products: list of product
     * @param parent:   parent to be the leaf node
     */
    public BPlusLeafNode(Product[] products, BPlusIndexNode parent) {
        super();
        this.products = products;
        this.numberOfEntry = searchEmpty(products);
        this.parent = parent;
    }


    /**
     * Getter method for get all products.
     *
     * @return all products.
     */
    public Product[] getProducts() {
        return products;
    }

    /**
     * This method remove a product from this leaf node.
     *
     * @param index: the location of the product.
     */
    public void deleteProduct(int index) {
        //System.out.println(Arrays.toString(products));
        products[index] = null;
        //System.out.println(Arrays.toString(products));
        numberOfEntry--;
    }

    /**
     * This method attempts to insert a dictionary pair within the product list.
     *
     * @param product: product to be inserted
     * @return is successful
     */
    public boolean insertProduct(Product product) {
        if (this.isFull()) {
            return false;
        } else {
            this.products[numberOfEntry] = product;
            numberOfEntry++;
            Arrays.sort(this.products, 0, numberOfEntry);
            return true;
        }
    }

    /**
     * This straightforward method checks whether the Index Node has fallen below the minimum degree or not.
     * An Index Node is considered has lower then minimum when its current number of children is less than
     * the minimum allowed.
     */
    public boolean hasLowerThenMinimum() {
        return numberOfEntry < MIN_ENTRY;
    }

    /**
     * This method determines if the Node is considered full.
     */
    public boolean isFull() {
        return numberOfEntry == MAX_ENTRY - 1;
    }

    /**
     * This method determines if the leaf node is capable of
     * lending one of its entry to a deficient node.
     */
    public boolean canLend() {
        return numberOfEntry > MIN_ENTRY;
    }

    /**
     * This method determines if the BPlusIndexNode is capable of being
     * merged with.
     */
    public boolean canMerge() {
        return numberOfEntry == MIN_ENTRY;
    }

    /**
     * This method search on an array of product
     * and returns the index of the first null entry found.
     */
    private int searchEmpty(Product[] products) {
        for (int i = 0; i < products.length; i++) {
            if (products[i] == null) {
                return i;
            }
        }
        return -1;
    }
}

