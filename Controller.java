import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Scanner;

/**
 * Author: Asif Qureshi, Cole Keller, Goldhuli, Sharjeel
 * Controller class handle GUI manipulation according to the current state of the B+ Tree.
 * It's control UI.fxml.
 */
public class Controller implements Initializable {

    /**
     * Binding fxml components
     */
    @FXML
    TextField productField, descriptionField;
    @FXML
    RadioButton searchRadio, insertRadio, deleteRadio, updateRadio;
    @FXML
    Button exitButton, executeButton;
    @FXML
    Label resultText, reportText;
    @FXML
    TreeView<String> treeView;


    Stage stage;
    BPlusTree bPlusTree;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        //Grouped all radio buttons
        ToggleGroup group = new ToggleGroup();
        searchRadio.setToggleGroup(group);
        insertRadio.setToggleGroup(group);
        deleteRadio.setToggleGroup(group);
        updateRadio.setToggleGroup(group);

        //On load Search radio button selected by default
        searchRadio.setSelected(true);

        //Search radio button selected means no need for description
        descriptionField.setDisable(true);

        //initiate B+ tree
        bPlusTree = new BPlusTree();

        //load data from file
        loadData();

        //built the tree virtually
        buildTree();

        //add action event to different elements
        addActions();

    }

    /**
     * Helper method to add action event in GUI component
     */
    private void addActions() {

        //exit will be handled by close method
        exitButton.setOnAction(event -> close());

        //description field will only available for update and insert
        updateRadio.setOnAction(event -> descriptionField.setDisable(false));
        searchRadio.setOnAction(event -> descriptionField.setDisable(true));
        deleteRadio.setOnAction(event -> descriptionField.setDisable(true));
        insertRadio.setOnAction(event -> descriptionField.setDisable(false));

        executeButton.setOnAction(event -> {
            String id = productField.getText().toUpperCase();
            String description = descriptionField.getText();

            //Check all available field are filled up or not...
            if (id.equals("") || (!descriptionField.isDisabled() && description.equals(""))) {

                //show alert to fill all fields...
                ButtonType yes = new ButtonType("yes", ButtonBar.ButtonData.OK_DONE);
                Alert alert = new Alert(Alert.AlertType.WARNING,
                        "Please fill up the field first!", yes);
                alert.show();
            } else {

                //all available field are filled up...

                if (searchRadio.isSelected()) {
                    String productDescription = bPlusTree.search(id);
                    if (productDescription != null) { //successful
                        StringBuilder nextParts=new StringBuilder();
                        for (Product product : bPlusTree.getNextTen(id)) {
                            nextParts.append(product).append("\n");
                        }
                        resultText.setText("Product " + id + " was found.\nProduct Description: " + productDescription +"\n\n" +
                                "Next parts:\n"+ nextParts);
                    } else {  //failed
                        resultText.setText("Product " + id + " not found.");
                    }
                } else if (insertRadio.isSelected()) {
                    if (bPlusTree.insert(id, description)) { //successful
                        resultText.setText("Product " + id + " was added.");
                        buildTree();
                    } else { //failed
                        resultText.setText("Unable to add product, product " + id + " already exist");
                    }
                } else if (deleteRadio.isSelected()) {
                    if (bPlusTree.delete(id)) { //successful
                        resultText.setText("Product " + id + " was deleted.");
                        buildTree();
                    } else { //failed
                        resultText.setText("Unable to delete product " + id + " product doesn't exist");
                    }
                } else {
                    if (bPlusTree.update(id, description)) { //successful
                        resultText.setText("Product " + id + " was updated.");
                        buildTree();
                    } else { //failed
                        resultText.setText("Unable to update product " + id + " product doesn't exist");
                    }
                }
            }
        });
    }

    /**
     * Helper method to build the B+ tree virtually
     */
    private void buildTree() {

        //calling drawTree which recursively build the whole tree...
        TreeItem<String> root = drawTree(bPlusTree.getRoot());
        root.setValue("Root");
        treeView.setRoot(root);
        root.setExpanded(true);
    }

    /**
     * A recursive method to build the tree virtually.
     * This method recursively traverse the whole B+ tree and add tree node in a Top-to-Bottom approach.
     *
     * @param node B+ Tree Node
     * @return TreeItem object
     */
    private TreeItem<String> drawTree(BPlusNode node) {
        if (node == null) {

            //base case 1 : if reached to a null node, then return empty item.

            return new TreeItem<>("Empty");

        } else if (node instanceof BPlusLeafNode) {

            // base case 2: if reached to a leaf node, then return leaf node items.

            TreeItem<String> treeItem = new TreeItem<>();
            BPlusLeafNode cur = (BPlusLeafNode) node;

            for (Product product : cur.getProducts()) {
                if (product != null) treeItem.getChildren().add(new TreeItem<>(product.toString()));
            }

            return treeItem;

        } else {

            //recursive case: call the method again until reached a base case...

            TreeItem<String> treeItem = new TreeItem<>("x");

            BPlusIndexNode cur = (BPlusIndexNode) node;

            int i = 0;
            for (i = 0; i < cur.getIndexPointers().length - 1; i++) {
                TreeItem<String> child = drawTree(cur.getIndexPointers()[i]);
                child.setValue("Index-" + i);
                treeItem.getChildren().add(child);

                if (cur.getProductIDs()[i] == null) treeItem.getChildren().add(new TreeItem<>("null"));
                else treeItem.getChildren().add(new TreeItem<>(cur.getProductIDs()[i]));
            }
            TreeItem<String> child = drawTree(cur.getIndexPointers()[i]);
            child.setValue("Index-" + i);
            treeItem.getChildren().add(child);

            return treeItem;
        }
    }

    /**
     * Setter method for stage.
     *
     * @param stage primary stage
     */
    public void setStage(Stage stage) {
        this.stage = stage;

        // all close operation will be handled by close method...
        this.stage.setOnCloseRequest(event -> {
            event.consume();
            close();
        });
    }

    /**
     * On any close operation this method will be called.
     * It asks user - Do they want to save changes to file?
     * If answer is "No", then close the program.
     * If answer is "Yes", then save the data to file and close the program.
     */
    public void close() {

        //asks user - Do they want to save changes to file?
        ButtonType yes = new ButtonType("yes", ButtonBar.ButtonData.OK_DONE);
        ButtonType no = new ButtonType("no", ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Do you want to save changes to file?", yes, no);
        Optional<ButtonType> result = alert.showAndWait();

        if (result.orElse(no) == yes) {

            //If answer is "Yes", then save the data to file and close the program

            System.out.println("yes");
            saveData();
            Platform.exit();
        } else {

            //If answer is "No", then close the program
            System.out.println("no");
            Platform.exit();
        }
    }

    /**
     * Helper method to load data from AutoPartFile.txt into the B+ tree.
     */
    private void loadData() {
        try {
            Scanner scanner = new Scanner(new FileReader("AutoPartFile.txt"));
            while (scanner.hasNextLine()) {
                String id = scanner.next();
                String description = scanner.nextLine().trim();
                bPlusTree.insert(id, description);
            }
            System.out.println("Data loaded.");
        } catch (FileNotFoundException e) {
            System.out.println("AutoPartFile.txt file not fount.");
        }
    }

    /**
     * Helper method to save data into AutoPartFile.txt into the B+ tree.
     */
    private void saveData() {
        try {
            PrintWriter out = new PrintWriter("AutoPartFile.txt");
            for (Product Product : bPlusTree.getAllEntry()) {
                out.println(Product.getId() + "\t\t" + Product.getDescription());
            }
            out.close();
            System.out.println("Data saved.");
        } catch (FileNotFoundException e) {
            System.out.println("Unable to save.");
        }
    }
}

