/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mongodbutils;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 *
 * @author NBosua
 */
public class MongoDBImportTool extends Application {

    final String cssDragDrop = "-fx-border-color: green;\n"
            + "-fx-border-insets: 5;\n"
            + "-fx-border-width: 1;\n"
            + "-fx-border-collapse: collapse;\n"
            + "-fx-border-style: dashed;\n";

    final TextArea logField = new TextArea();
    String newLine = System.getProperty("line.separator");
    MongodbConnection mc = new MongodbConnection();
    boolean result;
    String strdbName = "";
    String strCollName = "";
    HBox rightArea = new HBox();

    @Override
    public void start(Stage primaryStage) {
        try {
            primaryStage.setTitle("mongoDB import tool");
            Group root = new Group();
            Scene scene = new Scene(root, 800, 600, Color.WHITE);

            BorderPane border = new BorderPane();

            border.prefWidthProperty().bind(scene.widthProperty());
            border.prefHeightProperty().bind(scene.heightProperty());

            //border.setTop();
            //border.setLeft();
            border.setCenter(addGridPane());
            border.setRight(addRightPane());
            border.setBottom(addBottomPane());

            root.getChildren().add(border);

            primaryStage.setScene(scene);
            primaryStage.show();
        } finally {
            mc.disconnect();
        }
    }

    public HBox addBottomPane() {
        HBox bottomArea = new HBox();
        bottomArea.setPadding(new Insets(15, 12, 15, 12));
        logField.prefWidthProperty().bind(bottomArea.widthProperty());
        bottomArea.getChildren().add(logField);

        return bottomArea;
    }

    public HBox addRightPane() {
        //HBox rightArea = new HBox();
        rightArea.setPadding(new Insets(15, 12, 15, 12));

        rightArea.setStyle(cssDragDrop);

        Text upperRight = new Text("Drag and drop files here");
        upperRight.setFont(Font.font("Tahoma", FontWeight.NORMAL, 16));

        rightArea.getChildren().add(upperRight);
        rightArea.setAlignment(Pos.CENTER);

        rightArea.setOnDragOver(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                Dragboard db = event.getDragboard();
                if (db.hasFiles()) {
                    event.acceptTransferModes(TransferMode.COPY);
                } else {
                    event.consume();
                }
            }
        });

        // Dropping over surface
        rightArea.setOnDragDropped(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                Dragboard db = event.getDragboard();
                boolean success = false;
                if (db.hasFiles()) {
                    success = true;
                    String filePath = null;
                    for (File file : db.getFiles()) {
                        filePath = file.getAbsolutePath();

                        logField.appendText("procssing file " + filePath + "...");
                        //logField.appendText(newLine);

                        if (!filePath.endsWith(".xls")) {
                            logField.appendText("error processing file, file type not supported, please use .xls files only");
                            logField.appendText(newLine);
                            continue;
                        }

                        Filehandler fh = new Filehandler();
                        try {
                            result = fh.processFile(filePath, mc, strdbName, strCollName);
                            if (result) {
                                logField.appendText("file processed successfully");
                                logField.appendText(newLine);
                            } else {
                                logField.appendText("error processing file");
                                logField.appendText(newLine);
                            }
                        } catch (IOException ex) {
                            Logger.getLogger(MongoDBImportTool.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
                event.setDropCompleted(success);
                event.consume();
            }
        });

        rightArea.setVisible(false);

        return rightArea;
    }

    public GridPane addGridPane() {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        Text scenetitle = new Text("Connection Details");
        scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        grid.add(scenetitle, 0, 0, 2, 1);

        Label serverName = new Label("Address:");
        grid.add(serverName, 0, 1);
        TextField serverTextField = new TextField("localhost");
        grid.add(serverTextField, 1, 1);

        Label portName = new Label("Port:");
        grid.add(portName, 0, 2);
        TextField portTextField = new TextField("27017");
        grid.add(portTextField, 1, 2);

        Label dbName = new Label("Database Name:");
        grid.add(dbName, 0, 3);
        TextField dbNameTextField = new TextField();
        grid.add(dbNameTextField, 1, 3);

        Label collName = new Label("Collection Name:");
        grid.add(collName, 0, 4);
        TextField collNameTextField = new TextField();
        grid.add(collNameTextField, 1, 4);

        Button btnConnect = new Button("Connect");
        HBox hbBtn = new HBox(10);
        hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
        hbBtn.getChildren().add(btnConnect);
        grid.add(hbBtn, 1, 5);

        btnConnect.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent e) {

                //Validations
                if (dbNameTextField.getText().toString().contains(" ")) {
                    Alert alert = new Alert(AlertType.INFORMATION);
                    alert.setTitle("Information Dialog");
                    alert.setHeaderText(null);
                    alert.setContentText("The database name cannot contain any spaces, please correct.");
                    alert.show();
                    return;
                }
                if (collNameTextField.getText().toString().contains(" ")) {
                    Alert alert = new Alert(AlertType.INFORMATION);
                    alert.setTitle("Information Dialog");
                    alert.setHeaderText(null);
                    alert.setContentText("The collection name cannot contain any spaces, please correct.");
                    alert.show();
                    return;
                }

                btnConnect.setDisable(true);

                logField.appendText("connecting...");

                if (mc.connect(serverTextField.getText(), Integer.parseInt(portTextField.getText()), dbNameTextField.getText(), "", "", false)) {
                    logField.appendText("connection established");
                    logField.appendText(newLine);
                    strdbName = dbNameTextField.getText().toString();
                    strCollName = collNameTextField.getText().toString();

                    rightArea.setVisible(true);
                } else {
                    logField.appendText("error connecting");
                    logField.appendText(newLine);
                }

                /*
                 mc.disconnect();
                 logField.appendText("disconnected");
                 logField.appendText(newLine);
                 */
            }
        });

        return grid;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
