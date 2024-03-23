/**
 * Author: Asif Qureshi, Cole Keller, Goldhuli, Sharjeel
 * Node of the B+ tree
 */
public class BPlusNode {

    //In B+ tree all node parent must be an inner node
    BPlusIndexNode parent;

    /**
     * Default constructor
     */
    public BPlusNode() {
        this.parent = null;
    }

    /**
     * Getter method for parent.
     *
     * @return parent node
     */
    public BPlusIndexNode getParent() {
        return parent;
    }

    /**
     * Setter method for parent.
     */
    public void setParent(BPlusIndexNode parent) {
        this.parent = parent;
    }
}

