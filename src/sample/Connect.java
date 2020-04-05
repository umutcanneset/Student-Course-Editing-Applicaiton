package sample;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;

import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.concurrent.atomic.AtomicReference;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Callback;

public class Connect implements Initializable {


    @FXML
    private Button showButton;
    @FXML
    private ChoiceBox<String> urlSelection;
    @FXML
    private Button connectButton;

    @FXML
    private Button insertButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button updateButton;

    @FXML
    private TextField urlText;
    @FXML
    private TextField usernameText;

    @FXML
    private VBox vbox;
    @FXML
    private TextField passwordText;


    @FXML
    private ChoiceBox<String> tableTexts;

    @FXML
    private ChoiceBox<String> urlTexts;

    private Connection connection;
    private ObservableList<ObservableList> data, courseCodeObs;

    @FXML
    private TableView dataTable;

    @FXML
    private TableColumn column1;

    @FXML
    private TableColumn column2;

    @FXML
    private TableColumn column3;

    @FXML
    private TableColumn column4;

    @FXML
    private TableColumn column5;

    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    Alert alert2 = new Alert(Alert.AlertType.ERROR);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        insertButton.setVisible(false);
        deleteButton.setVisible(false);
        updateButton.setVisible(false);
        dataTable.setVisible(false);
        updateButton.setDisable(true);
        deleteButton.setDisable(true);
        connectButton.setStyle("-fx-border-color: #ff0000; -fx-border-width: 5px;");
        connectButton.setOnAction(e -> initializeDB());

        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            connection = DriverManager.getConnection("jdbc:mysql://localhost", "root", passwordText.getText());
            listSchemas();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }


        courseCodeObs = FXCollections.observableArrayList();


    }

    private void initializeDB() {

            insertButton.setVisible(true);
            deleteButton.setVisible(true);
            updateButton.setVisible(true);


        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();

            connection = DriverManager.getConnection(urlSelection.getValue(), usernameText.getText(), passwordText.getText());
            if (connection.isValid(0)) {
                connectButton.setText("Connected");
                //urlText.setDisable(true);
                usernameText.setDisable(true);
                passwordText.setDisable(true);
                urlSelection.setDisable(true);
                connectButton.setStyle("-fx-background-color: #00ff00; ");




                DatabaseMetaData metaData = connection.getMetaData();

                String[] types = {"TABLE"};
                Statement s = connection.createStatement();
                ResultSet rs;
                rs = metaData.getTables(null, null, "%", types);


                while (rs.next()) {
                    String tableName = rs.getString(3);
                    tableTexts.getItems().add(tableName);
                    tableTexts.setValue(tableName);
                }

                buildData();
                tableTexts.setOnAction(e -> {


                    buildData();
                    vbox.getChildren().clear();
                });

                insertButton.setOnAction(event -> {
                    insertData();
                });


                dataTable.setOnMouseClicked(event -> {
                    if (event.getClickCount() >= 1) {
                        updateButton.setDisable(false);
                        deleteButton.setDisable(false);
                    }

                });


                updateButton.setOnAction(event ->

                        updateData());
                deleteButton.setOnAction(event ->

                        deleteData()
                );
            }




        } catch (Exception ex) {
            insertButton.setVisible(false);
            deleteButton.setVisible(false);
            updateButton.setVisible(false);
            ex.printStackTrace();
            alert2.setTitle("Login Process:");
            alert2.setContentText(ex.getMessage());
            alert2.setHeaderText("Fail on login!");
            alert2.showAndWait();
        }
    }

    private void listSchemas(){
        ArrayList<String> urls = new ArrayList<>();
        try {
            ResultSet resultSet = connection.getMetaData().getCatalogs();
            while (resultSet.next())
                urls.add("jdbc:mysql://localhost/"+resultSet.getString("TABLE_CAT"));

        } catch (Exception ex) {
           alert2.setTitle("Connection Problem!");
           alert2.setContentText(ex.getMessage());
           alert2.showAndWait();
        }
        ObservableList schemaNames = FXCollections.observableArrayList(urls);
        urlSelection.setItems(schemaNames);

    }

    public void buildData() {


        Connection c;
        dataTable.setVisible(true);
        data = FXCollections.observableArrayList();
        updateButton.setDisable(true);
        deleteButton.setDisable(true);

        String deger = tableTexts.getValue();
        try {
            c = connection;

            PreparedStatement ps = c.prepareStatement("SELECT * FROM " + deger);


            ResultSet rs = ps.executeQuery();

            dataTable.getColumns().clear();
            for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
                final int j = i;
                TableColumn col = new TableColumn(rs.getMetaData().getColumnName(i + 1));
                col.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ObservableList, String>, ObservableValue<String>>() {
                    public ObservableValue<String> call(TableColumn.CellDataFeatures<ObservableList, String> param) {
                        return new SimpleStringProperty(param.getValue().get(j).toString());
                    }
                });

                dataTable.getColumns().addAll(col);

            }

            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    row.add(rs.getString(i));
                }

                data.add(row);


            }


            dataTable.getItems().clear();
            dataTable.setItems(data);
        } catch (Exception ex) {
            ex.printStackTrace();
            alert2.setTitle("Build Process:");
            alert2.setContentText(ex.getMessage());
            alert2.setHeaderText("Fail on build!");
            alert2.showAndWait();
        }
    }

    public void insertData() {


        vbox.getChildren().clear();
        String deger = tableTexts.getValue();
        try {
            Connection c = connection;

            PreparedStatement ps = c.prepareStatement("SELECT * FROM " + deger);


            ResultSet rs = ps.executeQuery();
            Button finalInsertButton = new Button("Insert");


            for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
                final int j = i;
                TableColumn col = new TableColumn(rs.getMetaData().getColumnName(i + 1));
                col.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ObservableList, String>, ObservableValue<String>>() {
                    public ObservableValue<String> call(TableColumn.CellDataFeatures<ObservableList, String> param) {
                        return new SimpleStringProperty(param.getValue().get(j).toString());
                    }
                });


            }
            for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                Text newText = new Text(rs.getMetaData().getColumnName(i));
                newText.setId("textid" + i);
                TextField newField = new TextField();
                newField.setId("textfieldid" + i);

                vbox.getChildren().add(newText);
                vbox.getChildren().add(newField);

            }
            vbox.getChildren().addAll(finalInsertButton);
            ObservableList<String> row = FXCollections.observableArrayList();


            finalInsertButton.setOnAction(e -> {


                try {

                    for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                        TextField textField = (TextField) vbox.lookup("#" + "textfieldid" + i);
                        if (textField != null) {
                            row.add((textField.getText()));
                        }
                    }


                    String setString = "";
                    String setValues = "";
                    for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                        if (i != rs.getMetaData().getColumnCount()) {
                            setString = setString + rs.getMetaData().getColumnName(i) + ", ";
                            setValues = setValues + (row.get(i - 1)) + "', '";
                        } else if (i == rs.getMetaData().getColumnCount()) {
                            setString = setString + rs.getMetaData().getColumnName(i);
                            setValues = setValues + row.get(i - 1) + "'";
                        }
                    }


                    String st = "INSERT INTO " + deger + "(" + setString + ") VALUES ('" + setValues + ");";

                    PreparedStatement ps1 = c.prepareStatement(st);
                    ps1.execute();


                    alert.setTitle("Insert Process:");
                    alert.setContentText("Data has been inserted.");
                    alert.setHeaderText("Insert Successful!");
                    alert.showAndWait();

                } catch (SQLException ex) {
                    ex.printStackTrace();
                    alert2.setTitle("Insert Process:");
                    alert2.setContentText(ex.getMessage());
                    alert2.setHeaderText("Fail on insert!");
                    alert2.showAndWait();
                }


                vbox.getChildren().clear();
                buildData();
            });

        } catch (Exception ex) {
            ex.printStackTrace();
            alert2.setTitle("Insert Process:");
            alert2.setContentText(ex.getMessage());
            alert2.setHeaderText("Fail on insert!");
            alert2.showAndWait();
        }
    }

    public void updateData() {


        ObservableList<Node> updateList;
        updateList = dataTable.getSelectionModel().getSelectedItems();


        vbox.getChildren().clear();
        String deger = tableTexts.getValue();
        try {
            Connection c = connection;

            PreparedStatement ps = null;

            ps = c.prepareStatement("SELECT * FROM " + deger);


            ResultSet rs = ps.executeQuery();
            Button finalUpdateButton = new Button("Update");

            for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
                final int j = i;
                TableColumn col = new TableColumn(rs.getMetaData().getColumnName(i + 1));
                col.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ObservableList, String>, ObservableValue<String>>() {
                    public ObservableValue<String> call(TableColumn.CellDataFeatures<ObservableList, String> param) {
                        return new SimpleStringProperty(param.getValue().get(j).toString());
                    }
                });


            }
            for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                Text newText = new Text(rs.getMetaData().getColumnName(i));
                newText.setId("textid" + i);
                TextField newField = new TextField();
                newField.setId("textfieldid" + i);

                vbox.getChildren().add(newText);
                vbox.getChildren().add(newField);

            }
            vbox.getChildren().addAll(finalUpdateButton);
            ObservableList<String> row = FXCollections.observableArrayList();
            ObservableList<String> column = FXCollections.observableArrayList();
            ObservableList<String> keys = FXCollections.observableArrayList();


            String c1 = String.valueOf(updateList.get(0));

            c1 = c1.replaceAll("\\[", "").replaceAll("\\]", "");

            String[] result = c1.split(",");

            DatabaseMetaData dmd = connection.getMetaData();
            String url = dmd.getURL();
            String dbName = (url.substring(url.lastIndexOf("/") + 1));

            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet primaryKeys = metaData.getPrimaryKeys(dbName, null, deger);
            while (primaryKeys.next()) {
                keys.add(primaryKeys.getString("COLUMN_NAME"));
            }

            AtomicReference<Character> c2 = new AtomicReference<>((char) 0);
            for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                TextField textField = (TextField) vbox.lookup("#" + "textfieldid" + i);


                c2.set(result[i - 1].charAt(0));

                if ((c2.get() != ' ')) {
                    textField.setText(result[i - 1]);


                } else {
                    textField.setText(result[i - 1].substring(1));
                }


            }
            for (int i = 1; i <= keys.size(); i++) {
                for (int j = 1; j <= rs.getMetaData().getColumnCount(); j++) {


                    if (keys.get(i - 1).equalsIgnoreCase(rs.getMetaData().getColumnName(j))) {
                        TextField textField = (TextField) vbox.lookup("#" + "textfieldid" + j);
                        textField.setDisable(true);


                    }

                }
            }


            finalUpdateButton.setOnAction(event -> {

                try {


                    for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                        TextField textField = (TextField) vbox.lookup("#" + "textfieldid" + i);
                        column.add(rs.getMetaData().getColumnName(i));
                        if (textField != null) {
                            row.add((textField.getText()));
                        }
                    }

                    for (int i = 1; i <= keys.size(); i++) {
                        for (int j = 1; j <= column.size(); j++) {


                            if (keys.get(i - 1).equalsIgnoreCase(column.get(j - 1))) {
                                TextField textField = (TextField) vbox.lookup("#" + "textfieldid" + j);
                                textField.setDisable(true);


                            }

                        }
                    }

                    String setString = "";
                    String setValues = "";
                    String setKey = "";

                    for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                        if (i != rs.getMetaData().getColumnCount()) {


                            setValues = setValues + column.get(i - 1) + " = '" + (row.get(i - 1)) + "', ";


                        } else if (i == rs.getMetaData().getColumnCount()) {
                            setString = setString + rs.getMetaData().getColumnName(i);
                            setValues = setValues + column.get(i - 1) + " = '" + (row.get(i - 1) + "'");


                        }

                    }
                    String ab;
                    for (int i = 1; i <= keys.size(); i++) {
                        for (int j = 1; j <= column.size(); j++) {


                            if (i != keys.size()) {

                                if (keys.get(i - 1).equalsIgnoreCase(column.get(j - 1))) {
                                    c2.set(result[j - 1].charAt(0));
                                    if ((c2.get() != ' ')) {
                                        ab = (result[j - 1]);
                                    } else {
                                        ab = (result[j - 1].substring(1));
                                    }
                                    setKey = setKey + keys.get(i - 1) + " = '" + ab + "' AND ";
                                }

                            } else if (i == keys.size()) {
                                if (keys.get(i - 1).equalsIgnoreCase(column.get(j - 1))) {
                                    c2.set(result[j - 1].charAt(0));
                                    if ((c2.get() != ' ')) {
                                        ab = (result[j - 1]);
                                    } else {
                                        ab = (result[j - 1].substring(1));
                                    }
                                    setKey = setKey + keys.get(i - 1) + " = '" + ab + "' ";
                                }
                            }

                        }
                    }
                    String st = "UPDATE " + deger + " SET " + setValues + " WHERE " + setKey + ";";


                    PreparedStatement ps1 = c.prepareStatement(st);
                    ps1.execute();

                    alert.setTitle("Update Process:");
                    alert.setContentText("Data has been Updated");
                    alert.setHeaderText("Insert Successful!");
                    alert.showAndWait();

                } catch (SQLException ex) {
                    ex.printStackTrace();
                    alert2.setTitle("Update Process:");
                    alert2.setContentText(ex.getMessage());
                    alert2.setHeaderText("Fail on update!");
                    alert2.showAndWait();
                }


                vbox.getChildren().clear();
                buildData();
                updateButton.setDisable(true);
            });


        } catch (SQLException e) {
            e.printStackTrace();
        }


    }

    public void deleteData() {
        ObservableList<Node> deleteList;
        deleteList = dataTable.getSelectionModel().getSelectedItems();


        vbox.getChildren().clear();
        String deger = tableTexts.getValue();
        try {
            Connection c = connection;

            PreparedStatement ps = null;

            ps = c.prepareStatement("SELECT * FROM " + deger);


            ResultSet rs = ps.executeQuery();
            Button finalDeleteButton = new Button("Delete");

            for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
                //We are using non property style for making dynamic table
                final int j = i;
                TableColumn col = new TableColumn(rs.getMetaData().getColumnName(i + 1));
                col.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ObservableList, String>, ObservableValue<String>>() {
                    public ObservableValue<String> call(TableColumn.CellDataFeatures<ObservableList, String> param) {
                        return new SimpleStringProperty(param.getValue().get(j).toString());
                    }
                });


            }
            for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                Text newText = new Text(rs.getMetaData().getColumnName(i));
                newText.setId("textid" + i);
                TextField newField = new TextField();
                newField.setDisable(true);
                newField.setId("textfieldid" + i);

                vbox.getChildren().add(newText);
                vbox.getChildren().add(newField);

            }
            vbox.getChildren().addAll(finalDeleteButton);
            ObservableList<String> row = FXCollections.observableArrayList();
            ObservableList<String> column = FXCollections.observableArrayList();
            ObservableList<String> keys = FXCollections.observableArrayList();


            String c1 = String.valueOf(deleteList.get(0));

            c1 = c1.replaceAll("\\[", "").replaceAll("\\]", "");

            String[] result = c1.split(",");

            DatabaseMetaData dmd = connection.getMetaData();
            String url = dmd.getURL();
            String dbName = (url.substring(url.lastIndexOf("/") + 1));

            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet primaryKeys = metaData.getPrimaryKeys(dbName, null, deger);
            while (primaryKeys.next()) {
                keys.add(primaryKeys.getString("COLUMN_NAME"));
            }

            AtomicReference<Character> c2 = new AtomicReference<>((char) 0);
            for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                TextField textField = (TextField) vbox.lookup("#" + "textfieldid" + i);


                c2.set(result[i - 1].charAt(0));

                if ((c2.get() != ' ')) {
                    textField.setText(result[i - 1]);
                } else {
                    textField.setText(result[i - 1].substring(1));
                }


            }


            finalDeleteButton.setOnAction(event -> {

                try {


                    for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                        TextField textField = (TextField) vbox.lookup("#" + "textfieldid" + i);
                        column.add(rs.getMetaData().getColumnName(i));
                        if (textField != null) {
                            row.add((textField.getText()));
                        }
                    }

                    String setString = "";
                    String setValues = "";
                    String setKey = "";

                    for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                        if (i != rs.getMetaData().getColumnCount()) {


                            setValues = setValues + column.get(i - 1) + " = '" + (row.get(i - 1)) + "', ";


                        } else if (i == rs.getMetaData().getColumnCount()) {
                            setString = setString + rs.getMetaData().getColumnName(i);
                            setValues = setValues + column.get(i - 1) + " = '" + (row.get(i - 1) + "'");


                        }

                    }
                    String ab;
                    for (int i = 1; i <= keys.size(); i++) {
                        for (int j = 1; j <= column.size(); j++) {


                            if (i != keys.size()) {

                                if (keys.get(i - 1).equalsIgnoreCase(column.get(j - 1))) {
                                    c2.set(result[j - 1].charAt(0));
                                    if ((c2.get() != ' ')) {
                                        ab = (result[j - 1]);
                                    } else {
                                        ab = (result[j - 1].substring(1));
                                    }
                                    setKey = setKey + keys.get(i - 1) + " = '" + ab + "' AND ";
                                }

                            } else if (i == keys.size()) {
                                if (keys.get(i - 1).equalsIgnoreCase(column.get(j - 1))) {
                                    c2.set(result[j - 1].charAt(0));
                                    if ((c2.get() != ' ')) {
                                        ab = (result[j - 1]);
                                    } else {
                                        ab = (result[j - 1].substring(1));
                                    }
                                    setKey = setKey + keys.get(i - 1) + " = '" + ab + "' ";
                                }
                            }

                        }
                    }
                    String st = "DELETE FROM " + deger + " WHERE " + setKey + ";";


                    PreparedStatement ps1 = c.prepareStatement(st);
                    ps1.execute();


                    alert.setTitle("Delete Process:");
                    alert.setContentText("Data has been deleted");
                    alert.setHeaderText("Delete Successful!");
                    alert.showAndWait();

                } catch (SQLException ex) {
                    ex.printStackTrace();
                    alert2.setTitle("Delete Process:");
                    alert2.setContentText(ex.getMessage());
                    alert2.setHeaderText("Fail on delete!");
                    alert2.showAndWait();
                }


                vbox.getChildren().clear();
                buildData();
                deleteButton.setDisable(true);
            });


        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}