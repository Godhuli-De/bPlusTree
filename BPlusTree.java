import java.lang.*;
import java.util.*;

/**
 * Author: Asif Qureshi, Cole Keller, Goldhuli, Sharjeel
 * This class is the implementation of the B+ tree data structure.
 * This B+ tree implementation support mainly four operations which are:
 * Search, Insert, Delete, Update.
 */
public class BPlusTree {
    private BPlusIndexNode root;
    private BPlusLeafNode leafHead;

    /**
     * Default constructor
     */
    public BPlusTree() {
        root = null;
        leafHead = null;
    }

    /**
     * Getter method for root
     *
     * @return root node
     */
    public BPlusIndexNode getRoot() {
        return root;
    }

    /**
     * Getter method for leafHead
     *
     * @return leafHead node
     */
    public BPlusLeafNode getLeafHead() {
        return leafHead;
    }

    /**
     * Get all non-null entry form the tree.
     *
     * @return all the entry reside in this tree.
     */
    public ArrayList<Product> getAllEntry() {
        ArrayList<Product> entries = new ArrayList<>();

        //we have to start from leaf head which is point to the left most leaf node...
        BPlusLeafNode leafNode = leafHead;

        //then traverse all its rightest siblings to get all entry...
        while (leafNode != null) {
            for (Product Product : leafNode.products) {
                if (Product != null) entries.add(Product);
            }
            leafNode = leafNode.rightSibling;
        }

        return entries;
    }

    /**
     * This method determines if the B+ tree is empty or not.
     *
     * @return true if empty false otherwise
     */
    public boolean isEmpty() {
        return leafHead == null;
    }

    /**
     * Search the product which associated with the specified id.
     * If a product with the id exist then return the associated product description.
     * Else return null.
     *
     * @param id: specified id
     * @return If a product with the id exist then return the associated product description, else return null.
     */
    public ArrayList<Product> getNextTen(String id) {
        // if tree is empty
        if (isEmpty()) {
            return null;
        }

        // getting the leaf node which may hold the id...
        BPlusLeafNode leafNode;
        if (root == null) leafNode = leafHead;
        else leafNode = getPossibleLeafNode(id);

        // doing binary search in leaf node entries which may hold the id...
        Product[] products = leafNode.products;
        //System.out.println(Arrays.toString(products));
        int index = binarySearch(products, leafNode.numberOfEntry, id);

        if(index < 0){
            // having negative index means entry with the id doesn't exist
            return null;
        }else{
            ArrayList<Product> result=new ArrayList<>();

            while(result.size()!=10){
                index++;
                if(index<products.length){
                    // there are next product in the current leaf node

                    if(products[index]!=null){
                        result.add(products[index]);
                    }

                }else{
                    //no next product in the current leaf node

                    leafNode=leafNode.rightSibling;
                    if(leafNode==null){
                        //end of the tree no more next product
                        break;
                    }else{
                        // going to next leaf node
                        products = leafNode.products;
                        index=-1;
                    }
                }
            }

            return result;
        }

    }


    /**
     * Search the product which associated with the specified id.
     * If a product with the id exist then return the associated product description.
     * Else return null.
     *
     * @param id: specified id
     * @return If a product with the id exist then return the associated product description, else return null.
     */
    public String search(String id) {

        // if tree is empty
        if (isEmpty()) {
            return null;
        }

        // getting the leaf node which may hold the id...
        BPlusLeafNode leafNode;
        if (root == null) leafNode = leafHead;
        else leafNode = getPossibleLeafNode(id);

        // doing binary search in leaf node entries which may hold the id...
        Product[] products = leafNode.products;
        //System.out.println(Arrays.toString(products));
        int index = binarySearch(products, leafNode.numberOfEntry, id);

        // having negative index means entry with the id doesn't exist
        return index < 0 ? null : products[index].description;
    }

    /**
     * Method to insert a new entry in the B+ tree.
     *
     * @param id:          product id
     * @param description: product description
     * @return true if successful false otherwise
     */
    public boolean insert(String id, String description) {

        if (search(id) != null) {
            // A node with this id already exist.
            return false;
        }

        //if tree is empty...
        if (isEmpty()) {
            // first leaf node of the B+ tree will be created...
            leafHead = new BPlusLeafNode(new Product(id, description));

        } else {

            // getting the leaf node will hold the entry...
            BPlusLeafNode leafNode;
            if (root == null) leafNode = leafHead;
            else leafNode = getPossibleLeafNode(id);

            boolean success = leafNode.insertProduct(new Product(id, description));

            // if not that means leaf node is full
            if (!success) {

                // add the new entry and sort all the entries in the leaf node...
                leafNode.products[leafNode.numberOfEntry] = new Product(id, description);
                leafNode.numberOfEntry++;
                sortEntries(leafNode.products);

                // split the sorted entries by half
                int mid = leafNode.products.length / 2;
                Product[] half = splitEntries(leafNode, mid);

                // check if leaf nodes parent exist...
                if (leafNode.parent != null) {

                    //if parent exist...
                    // update the id of the parent
                    String newParentId = half[0].id;
                    leafNode.parent.productIDs[leafNode.parent.numberOfEntry - 1] = newParentId;
                    Arrays.sort(leafNode.parent.productIDs, 0, leafNode.parent.numberOfEntry);

                } else {

                    // if parent doesn't exist new parent must be with mid-id which will be an index node
                    String[] parentIds = new String[BPlusIndexNode.MAX_ENTRY];
                    parentIds[0] = half[0].id;
                    BPlusIndexNode parent = new BPlusIndexNode(parentIds);
                    leafNode.parent = parent;
                    parent.addIndexPointer(leafNode);
                }

                // creating new leaf node for other half...
                BPlusLeafNode newLeafNode = new BPlusLeafNode(half, leafNode.parent);

                // update pointers...
                int pointerIndex = leafNode.parent.getIndexOfPointer(leafNode) + 1;
                leafNode.parent.insertPointer(newLeafNode, pointerIndex);

                // update siblings...
                newLeafNode.rightSibling = leafNode.rightSibling;
                if (newLeafNode.rightSibling != null) {
                    newLeafNode.rightSibling.leftSibling = newLeafNode;
                }
                leafNode.rightSibling = newLeafNode;
                newLeafNode.leftSibling = leafNode;

                if (root != null) {

                    //if index node are full we have to split them...
                    BPlusIndexNode in = leafNode.parent;
                    while (in != null) {
                        if (in.isFull()) {
                            splitIndexNode(in);
                        } else {
                            break;
                        }
                        in = in.parent;
                    }
                } else {
                    // update root
                    root = leafNode.parent;
                }
            }
        }

        return true;
    }

    /**
     * Delete the product which associated with the specified id.
     * If a product with the id exist then delete the associated entry.
     *
     * @param id: specified id
     * @return true if successful false otherwise
     */
    public boolean delete(String id) {
        if (isEmpty()) {
            // tree empty
            return false;
        } else {

            // getting the leaf node which may hold the id...
            BPlusLeafNode leafNode;
            if (root == null) leafNode = leafHead;
            else leafNode = getPossibleLeafNode(id);

            // doing binary search in leaf node entries which may hold the id...
            int index = binarySearch(leafNode.products, leafNode.numberOfEntry, id);

            if (index < 0) {
                // if id is not found in the tree
                return false;
            } else {

                // delete successful
                leafNode.deleteProduct(index);

                // checking deficiencies
                if (leafNode.hasLowerThenMinimum()) {

                    //System.out.println("here");

                    BPlusLeafNode sibling;
                    BPlusIndexNode parent = leafNode.parent;

                    // checking the left sibling first, then the right sibling...
                    if (leafNode.leftSibling != null &&
                            leafNode.leftSibling.canLend() &&
                            leafNode.leftSibling.parent == leafNode.parent) {

                        //System.out.println("here1");

                        sibling = leafNode.leftSibling;
                        Product temp = sibling.products[sibling.numberOfEntry - 1];

                        //shifting entry from sibling to sibling...
                        leafNode.insertProduct(temp);
                        sortEntries(leafNode.products);
                        sibling.deleteProduct(sibling.numberOfEntry - 1);

                        // updating parent id...
                        int pointerIndex = getIndexOfPointer(parent.indexPointers, leafNode);
                        if (!(temp.id.compareTo(parent.productIDs[pointerIndex - 1]) >= 0)) {
                            parent.productIDs[pointerIndex - 1] = leafNode.products[0].id;
                        }

                    } else if (leafNode.rightSibling != null &&
                            leafNode.rightSibling.canLend() &&
                            leafNode.rightSibling.parent == leafNode.parent) {

                        //System.out.println("here2");

                        sibling = leafNode.rightSibling;
                        Product temp = sibling.products[0];

                        // shifting entry from sibling
                        leafNode.insertProduct(temp);
                        sibling.deleteProduct(0);
                        sortEntries(sibling.products);

                        // updating parent id...
                        int pointerIndex = getIndexOfPointer(parent.indexPointers, leafNode);
                        if (!(temp.id.compareTo(parent.productIDs[pointerIndex]) <= 0)) {
                            parent.productIDs[pointerIndex] = sibling.products[0].id;
                        }
                    } else if (leafNode.leftSibling != null &&
                            leafNode.leftSibling.canMerge() &&
                            leafNode.leftSibling.parent == leafNode.parent) {

                        //System.out.println("here3");

                        // merging...
                        sibling = leafNode.leftSibling;
                        int pointerIndex = getIndexOfPointer(parent.indexPointers, leafNode);

                        // delete id and child pointer from parent
                        parent.removeKey(pointerIndex - 1);
                        parent.removePointer(leafNode);

                        // updating sibling pointer...
                        sibling.rightSibling = leafNode.rightSibling;

                        // checking deficiencies in parent...
                        if (parent.hasLowerThenMinimum()) {
                            fixDeficiency(parent);
                        }

                    } else if (leafNode.rightSibling != null &&
                            leafNode.rightSibling.canMerge() &&
                            leafNode.rightSibling.parent == leafNode.parent) {

                        //System.out.println("here4");

                        sibling = leafNode.rightSibling;
                        int pointerIndex = getIndexOfPointer(parent.indexPointers, leafNode);

                        // delete id and child pointer from parent
                        parent.removeKey(pointerIndex);
                        parent.removePointer(pointerIndex);

                        // updating sibling pointer...
                        sibling.leftSibling = leafNode.leftSibling;
                        if (sibling.leftSibling == null) {
                            leafHead = sibling;
                        }

                        if (parent.hasLowerThenMinimum()) {
                            fixDeficiency(parent);
                        }
                    }else{

                        sortEntries(leafNode.products);
                    }

                } else if (this.root == null && this.leafHead.numberOfEntry == 0) {

                    //System.out.println("here5");
                    // if tree gets empty...
                    this.leafHead = null;
                } else {

                    //System.out.println("here6");
                    //sort entries after deletion
                    sortEntries(leafNode.products);
                }

                return true;
            }
        }
    }

    /**
     * Method to update an entry in the B+ tree.
     *
     * @param id:          product id
     * @param description: product description
     * @return true if successful false otherwise
     */
    public boolean update(String id, String description) {

        // if tree is empty
        if (isEmpty()) {
            return false;
        }

        // getting the leaf node which may hold the id...
        BPlusLeafNode leafNode;
        if (root == null) leafNode = leafHead;
        else leafNode = getPossibleLeafNode(id);

        // doing binary search in leaf node entries which may hold the id...
        Product[] products = leafNode.products;
        int index = binarySearch(products, leafNode.numberOfEntry, id);

        // If index negative, the id doesn't exist in B+ tree
        if (index < 0) {
            return false;
        } else {
            products[index].description = description;
            return true;
        }
    }

    /**
     * Helper method to do binarySearch on products.
     */
    private int binarySearch(Product[] products, int numPairs, String t) {
        Comparator<Product> c = Comparator.comparing(o -> o.id);
        return Arrays.binarySearch(products, 0, numPairs, new Product(t, ""), c);
    }

    /**
     * Helper method to get possible leaf node for a id
     */
    private BPlusLeafNode getPossibleLeafNode(String id) {

        // start from the root
        String[] keys = this.root.productIDs;
        int i;

        // traversing entries to get path to appropriate leaf node
        for (i = 0; i < this.root.numberOfEntry - 1; i++) {
            if (id.compareTo(keys[i]) < 0) {
                break;
            }
        }

        //recursively search until a leaf node found...
        BPlusNode child = this.root.indexPointers[i];
        if (child instanceof BPlusLeafNode) {
            return (BPlusLeafNode) child;
        } else {
            return getPossibleLeafNode((BPlusIndexNode) child, id);
        }
    }

    private BPlusLeafNode getPossibleLeafNode(BPlusIndexNode node, String id) {
        // start from the root
        String[] keys = node.productIDs;
        int i;

        // traversing entries to get path to appropriate leaf node
        for (i = 0; i < node.numberOfEntry - 1; i++) {
            if (id.compareTo(keys[i]) < 0) {
                break;
            }
        }

        //recursively search until a leaf node found...
        BPlusNode childBPlusNode = node.indexPointers[i];
        if (childBPlusNode instanceof BPlusLeafNode) {
            return (BPlusLeafNode) childBPlusNode;
        } else {
            return getPossibleLeafNode((BPlusIndexNode) node.indexPointers[i], id);
        }
    }

    /**
     * Helper method to get index of the pointer
     */
    private int getIndexOfPointer(BPlusNode[] pointers, BPlusLeafNode node) {
        int i;
        i = 0;
        while (i < pointers.length) {
            if (pointers[i] == node) {
                return i;
            }
            i++;
        }
        return i;
    }

    /**
     * Helper method to get mid-point.
     */
    private int getMidPoint() {
        return (int) Math.ceil((BPlusIndexNode.MAX_ENTRY + 1) / 2.0) - 1;
    }

    /**
     * Helper method to fix deficiency.
     */
    private void fixDeficiency(BPlusIndexNode in) {

        BPlusIndexNode sibling;
        BPlusIndexNode parent = in.parent;

        // start from root node
        if (this.root == in) {
            for (int i = 0; i < in.indexPointers.length; i++) {
                if (in.indexPointers[i] != null) {
                    if (in.indexPointers[i] instanceof BPlusIndexNode) {
                        this.root = (BPlusIndexNode) in.indexPointers[i];
                        this.root.parent = null;
                    } else if (in.indexPointers[i] instanceof BPlusLeafNode) {
                        this.root = null;
                    }
                }
            }
        } else if (in.leftSibling != null && in.leftSibling.canLend()) {
            sibling = in.leftSibling;
        } else if (in.rightSibling != null && in.rightSibling.canLend()) {
            sibling = in.rightSibling;

            String siblingFirstId = sibling.productIDs[0];
            BPlusNode pointer = sibling.indexPointers[0];

            in.productIDs[in.numberOfEntry - 1] = parent.productIDs[0];
            in.indexPointers[in.numberOfEntry] = pointer;

            // update parent id with sibling first id
            parent.productIDs[0] = siblingFirstId;

            // removing id and pointer from sibling...
            sibling.removePointer(0);
            Arrays.sort(sibling.productIDs);
            sibling.removePointer(0);
            shiftDown(in.indexPointers);

        } else if (in.rightSibling != null && in.rightSibling.canMerge()) {
            sibling = in.rightSibling;

            // copying rightmost id in parent to beginning of sibling id and delete id from parent
            sibling.productIDs[sibling.numberOfEntry - 1] = parent.productIDs[parent.numberOfEntry - 2];
            Arrays.sort(sibling.productIDs, 0, sibling.numberOfEntry);
            parent.productIDs[parent.numberOfEntry - 2] = null;

            // copying in's child pointer over to sibling's list of child pointers
            for (int i = 0; i < in.indexPointers.length; i++) {
                if (in.indexPointers[i] != null) {
                    sibling.prependPointer(in.indexPointers[i]);
                    in.indexPointers[i].parent = sibling;
                    in.removePointer(i);
                }
            }

            // removing child pointer from grandparent to deficient node...
            parent.removePointer(in);

            // removing left sibling...
            sibling.leftSibling = in.leftSibling;
        }

        // looking for deficiency a level up...
        if (parent != null && parent.hasLowerThenMinimum()) {
            fixDeficiency(parent);
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

    /**
     * This method shifts a set of pointers that have null values at the beginning of the set.
     */
    private void shiftDown(BPlusNode[] pointers) {
        BPlusNode[] newPointers = new BPlusNode[pointers.length];
        for (int i = 1; i < pointers.length; i++) {
            newPointers[i - 1] = pointers[i];
        }
    }

    /**
     * Helper method to sort entries.
     */
    private void sortEntries(Product[] dictionary) {
        Arrays.sort(dictionary, (o1, o2) -> {
            if (o1 == null && o2 == null) {
                return 0;
            }
            if (o1 == null) {
                return 1;
            }
            if (o2 == null) {
                return -1;
            }
            return o1.compareTo(o2);
        });
    }

    /**
     * Helper to split child pointers.
     */
    private BPlusNode[] splitChildPointers(BPlusIndexNode in, int split) {
        BPlusNode[] pointers = in.indexPointers;
        BPlusNode[] halfPointers = new BPlusNode[pointers.length];

        for (int i = split + 1; i < pointers.length; i++) {
            halfPointers[i - split - 1] = pointers[i];
            in.removePointer(i);
        }

        return halfPointers;
    }

    /**
     * Helper method to split entries of leaf node.
     */
    private Product[] splitEntries(BPlusLeafNode leafNode, int split) {
        Product[] dictionary = leafNode.products;
        Product[] half = new Product[dictionary.length];

        for (int i = split; i < dictionary.length; i++) {
            half[i - split] = dictionary[i];
            leafNode.deleteProduct(i);
        }

        return half;
    }

    /**
     * Helper method to split index node
     */
    private void splitIndexNode(BPlusIndexNode indexNode) {
        BPlusIndexNode parent = indexNode.parent;

        // splitting ids and pointers of the indexNode in half
        int mid = getMidPoint();
        String newParentKey = indexNode.productIDs[mid];
        String[] halfKeys = splitIds(indexNode.productIDs, mid);
        BPlusNode[] halfPointers = splitChildPointers(indexNode, mid);

        // changing degree of original indexNode...
        indexNode.numberOfEntry = searchEmpty(indexNode.indexPointers);

        // creating new sibling internal node and add half of ids and pointers...
        BPlusIndexNode sibling = new BPlusIndexNode(halfKeys, halfPointers);
        for (BPlusNode pointer : halfPointers) {
            if (pointer != null) {
                pointer.parent = sibling;
            }
        }

        // making internal nodes siblings of one another...
        sibling.rightSibling = indexNode.rightSibling;
        if (sibling.rightSibling != null) {
            sibling.rightSibling.leftSibling = sibling;
        }
        indexNode.rightSibling = sibling;
        sibling.leftSibling = indexNode;

        if (parent == null) {
            // creating new root node and add mid ids and pointers
            String[] keys = new String[BPlusIndexNode.MAX_ENTRY];
            keys[0] = newParentKey;
            BPlusIndexNode newRoot = new BPlusIndexNode(keys);
            newRoot.addIndexPointer(indexNode);
            newRoot.addIndexPointer(sibling);
            this.root = newRoot;

            // add pointers from children to parent
            indexNode.parent = newRoot;
            sibling.parent = newRoot;

        } else {

            // add key to parent
            parent.productIDs[parent.numberOfEntry - 1] = newParentKey;
            Arrays.sort(parent.productIDs, 0, parent.numberOfEntry);

            // update pointer to new sibling
            int pointerIndex = parent.getIndexOfPointer(indexNode) + 1;
            parent.insertPointer(sibling, pointerIndex);
            sibling.parent = parent;
        }
    }

    /**
     * Helper method to split ids
     */
    private String[] splitIds(String[] ids, int split) {
        String[] half = new String[BPlusIndexNode.MAX_ENTRY];
        ids[split] = null;

        for (int i = split + 1; i < ids.length; i++) {
            half[i - split - 1] = ids[i];
            ids[i] = null;
        }

        return half;
    }
}

