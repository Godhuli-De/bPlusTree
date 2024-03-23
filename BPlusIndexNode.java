/**
 * Author: Asif Qureshi, Cole Keller, Goldhuli, Sharjeel
 * Represent inner nodes of B+ tree.
 * It will store ID and indexing pointers.
 */
public class BPlusIndexNode extends BPlusNode {


    public static final int MAX_ENTRY = 4;
    public static final int MIN_ENTRY = 2;

    public int numberOfEntry;
    public String[] productIDs;
    public BPlusNode[] indexPointers;
    public BPlusIndexNode leftSibling;
    public BPlusIndexNode rightSibling;

    /**
     * Constructor to initialize a BPlusIndexNode with specified productIDs
     *
     * @param productIDs: initialize with specified productIDs
     */
    public BPlusIndexNode(String[] productIDs) {
        this.numberOfEntry = 0;
        this.productIDs = productIDs;
        this.indexPointers = new BPlusNode[MAX_ENTRY + 1];
    }

    /**
     * Constructor to initialise a BPlusIndexNode with specified productIDs and indexPointers
     *
     * @param productIDs:    specified productIDs
     * @param indexPointers: specified indexPointers
     */
    public BPlusIndexNode(String[] productIDs, BPlusNode[] indexPointers) {
        this.numberOfEntry = searchEmpty(indexPointers);
        this.productIDs = productIDs;
        this.indexPointers = indexPointers;
    }

    /**
     * Getter for productIDs
     *
     * @return productIDs
     */
    public String[] getProductIDs() {
        return productIDs;
    }

    /**
     * Getter for indexPointers
     *
     * @return indexPointers
     */
    public BPlusNode[] getIndexPointers() {
        return indexPointers;
    }

    /**
     * Add index pointer to the end of the indexPointers
     * The indexPointer can point to an index node or leaf node.
     *
     * @param indexPointer: indexPointer to be added.
     */
    public void addIndexPointer(BPlusNode indexPointer) {
        indexPointers[numberOfEntry] = indexPointer;
        numberOfEntry++;
    }

    /**
     * The method will provide the index of the pointer in the indexPointers.
     * If the pointer cannot be located, the function will return -1.
     */
    public int getIndexOfPointer(BPlusNode pointer) {
        for (int i = 0; i < indexPointers.length; i++) {
            if (indexPointers[i] == pointer) {
                return i;
            }
        }
        return -1;
    }

    /**
     * This method takes a pointer to a Node object and an integer index,
     * and inserts the pointer into the indexPointers instance variable at
     * the specified index. The insertion may cause some pointers to be shifted to the right of the index.
     */
    public void insertPointer(BPlusNode pointer, int index) {
        for (int i = numberOfEntry - 1; i >= index; i--) {
            indexPointers[i + 1] = indexPointers[i];
        }
        this.indexPointers[index] = pointer;
        this.numberOfEntry++;
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
     * This method determines if the BPlusIndexNode is capable of
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
     * This method determines if the BPlusIndexNode is considered full.
     */
    public boolean isFull() {
        return numberOfEntry == MAX_ENTRY + 1;
    }

    /**
     * This method inserts the pointer at the beginning of the indexPointers.
     */
    public void prependPointer(BPlusNode pointer) {
        for (int i = numberOfEntry - 1; i >= 0; i--) {
            indexPointers[i + 1] = indexPointers[i];
        }
        indexPointers[0] = pointer;
        numberOfEntry++;
    }

    /**
     * This method sets productIDs[index] to null.
     */
    public void removeKey(int index) {
        productIDs[index] = null;
    }

    /**
     * This method sets indexPointers[index] to null.
     */
    public void removePointer(int index) {
        indexPointers[index] = null;
        numberOfEntry--;
    }

    /**
     * This method removes pointer from the indexPointers.
     */
    public void removePointer(BPlusNode pointer) {
        for (int i = 0; i < indexPointers.length; i++) {
            if (indexPointers[i] == pointer) {
                indexPointers[i] = null;
                numberOfEntry--;
            }
        }
    }

    /**
     * This method search on an array of pointers
     * and returns the index of the first null entry found.
     */
    private int searchEmpty(BPlusNode[] pointers) {
        for (int i = 0; i < pointers.length; i++) {
            if (pointers[i] == null) {
                return i;
            }
        }
        return -1;
    }
}

