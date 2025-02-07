package com.cyber.server.controller;

import com.cyber.server.database.DatabaseConnection;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.HBox;
import com.cyber.server.model.User;
import com.cyber.server.validation.UserNameValidator;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;



import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CustomerController {

    @FXML
    private TableView<User> userTable;
    @FXML
    private TableColumn<User, String> usernameColumn;
    @FXML
    private TableColumn<User, String> passwordColumn;
    @FXML
    private TableColumn<User, Double> balanceColumn;
    @FXML
    private TableColumn<User, String> createdDateColumn;
    @FXML
    private TableColumn<Object, Void> actionColumn;
    @FXML
    private TableColumn<Object, Void> addColumn;

    private ObservableList<User> userList;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");



    public void initialize() {

        userList = FXCollections.observableArrayList();


        usernameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUsername()));
        passwordColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPassword()));
        balanceColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getBalance()).asObject());
        // Đảm bảo kiểu của cột createdDateColumn là TableColumn<User, LocalDateTime>
        createdDateColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getCreatedDateColumn()).asString());
// Thiết lập cách hiển thị ngày giờ
        createdDateColumn.setCellValueFactory(cellData -> {
            LocalDateTime dateTime = cellData.getValue().getCreatedDateColumn();
            String formattedDate = (dateTime != null) ? dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) : "N/A";
            return new SimpleStringProperty(formattedDate);
        });

        // Action buttons column (Edit and Delete)
        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("✏️ Edit");
            private final Button deleteButton = new Button("❌ Delete");
            {
                editButton.setOnAction(event -> editAccount((User) getTableView().getItems().get(getIndex())));
                deleteButton.setOnAction(event -> deleteAccount((User) getTableView().getItems().get(getIndex())));
                HBox buttons = new HBox(10, editButton, deleteButton);
                buttons.setAlignment(Pos.CENTER);
                setGraphic(buttons);
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : getGraphic());
            }
        });

        // Add buttons column (Deposit and Add account)
        addColumn.setCellFactory(param -> new TableCell<>() {
            private final Button addMoneyButton = new Button("💰 Deposit");
            {
                addMoneyButton.setOnAction(event -> depositMoney((User) getTableView().getItems().get(getIndex())));
                HBox buttons = new HBox(10, addMoneyButton);
                buttons.setAlignment(Pos.CENTER);
                setGraphic(buttons);
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : getGraphic());
            }
        });

        loadUsersFromDatabase(); // Gọi hàm load dữ liệu từ database vào bảng
        userTable.setItems(userList);
    }

    private void loadUsersFromDatabase() {
        userList.clear();

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("USE netcafedb;");
            ResultSet rs = statement.executeQuery("SELECT * FROM users");

            while (rs.next()) {
                User user = new User();
                user.setUser_id(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setBalance(rs.getDouble("balance"));

                java.sql.Timestamp timestamp = rs.getTimestamp("create_date");
                if (timestamp != null) {
                    user.setCreatedDateColumn(timestamp.toLocalDateTime()); // Sử dụng LocalDateTime trực tiếp
                }



                userList.add(user);
            }

            userTable.setItems(userList); // Cập nhật lại bảng

            // **Thiết lập lại CellFactory cho actionColumn và addColumn**
            actionColumn.setCellFactory(param -> new TableCell<>() {
                private final Button editButton = new Button("✏️ Edit");
                private final Button deleteButton = new Button("❌ Delete");
                {
                    editButton.setOnAction(event -> editAccount((User) getTableView().getItems().get(getIndex())));
                    deleteButton.setOnAction(event -> deleteAccount((User) getTableView().getItems().get(getIndex())));
                    HBox buttons = new HBox(10, editButton, deleteButton);
                    buttons.setAlignment(Pos.CENTER);
                    setGraphic(buttons);
                }
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : getGraphic());
                }
            });

            addColumn.setCellFactory(param -> new TableCell<>() {
                private final Button addMoneyButton = new Button("💰 Deposit");
                {
                    addMoneyButton.setOnAction(event -> depositMoney((User) getTableView().getItems().get(getIndex())));
                    HBox buttons = new HBox(10, addMoneyButton);
                    buttons.setAlignment(Pos.CENTER);
                    setGraphic(buttons);
                }
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : getGraphic());
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi tải dữ liệu từ database: " + e.getMessage());
        }
    }





    @FXML
    private void searchAccount() {
        // Display a dialog to enter the first letter of the name
        TextInputDialog searchDialog = new TextInputDialog();
        searchDialog.setTitle("Search Account");
        searchDialog.setHeaderText("Enter the first letter of the username:");
        searchDialog.setContentText("First letter:");

        searchDialog.showAndWait().ifPresent(letter -> {
            if (letter != null && !letter.trim().isEmpty()) {
                char searchLetter = letter.trim().toUpperCase().charAt(0); // Get the first character and convert to uppercase
                ObservableList<User> filteredList = FXCollections.observableArrayList();

                // Filter the user list based on the first letter of the name (split first and last names)
                for (User user : userList) {
                    String[] nameParts = user.getUsername().split("\\s+"); // Split the name by spaces
                    if (nameParts.length > 0 && nameParts[0].toUpperCase().charAt(0) == searchLetter) {
                        filteredList.add(user);
                    }
                }

                if (!filteredList.isEmpty()) {
                    userTable.setItems(filteredList); // Update table with the filtered list
                } else {
                    showAlert("No account found starting with the letter " + searchLetter);
                }
            }
        });
    }

    @FXML
    private void depositMoney(User selectedUser) {
        if (selectedUser != null) {
            TextInputDialog dialog = new TextInputDialog("0");
            dialog.setTitle("Deposit Money");
            dialog.setHeaderText("Enter the amount to deposit for: " + selectedUser.getUsername());
            dialog.setContentText("Amount:");
            dialog.showAndWait().ifPresent(amount -> {
                try {
                    double money = Double.parseDouble(amount);
                    selectedUser.setBalance(selectedUser.getBalance() + money);
                    userTable.refresh();
                } catch (NumberFormatException e) {
                    showAlert("Please enter a valid amount.");
                }
            });
        } else {
            showAlert("Please select an account to deposit money.");
        }
    }

    private void editAccount(User selectedUser) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/cyber/server/view/EditAccount.fxml"));
            Stage stage = new Stage();
            Scene scene = new Scene(loader.load());
            EditAccountController controller = loader.getController();
            controller.setSelectedUser(selectedUser);
            controller.setStage(stage);
            stage.setTitle("Edit Account");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error opening edit account window.");
        }
    }

    @FXML
    private void addAccount() {
        try {
            // Adjust the path and check the resource loading.
            URL fxmlUrl = getClass().getResource("/com/cyber/server/view/AddAccount.fxml");
            if (fxmlUrl == null) {
                showAlert("Could not find AddAccount.fxml file.");
                return; // Exit if FXML is not found
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Stage stage = new Stage();
            Scene scene = new Scene(loader.load());
            AddAccountController controller = loader.getController();
            if (controller != null) {
                controller.setStage(stage); // Pass the stage reference
                stage.setTitle("Add Account");
                stage.setScene(scene);
                stage.show();
            } else {
                showAlert("Failed to load AddAccountController.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error opening add account window: " + e.getMessage());
        }
    }

    private void deleteAccount(User selectedUser) {
        if (selectedUser != null) {
            Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
            confirmDialog.setTitle("Delete Account");
            confirmDialog.setHeaderText("Are you sure you want to delete this account?");
            confirmDialog.setContentText("Username: " + selectedUser.getUsername());
            confirmDialog.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try (Connection connection = DatabaseConnection.getConnection();
                         PreparedStatement statement = connection.prepareStatement("DELETE FROM users WHERE user_id = ?")) {
                        statement.setInt(1, selectedUser.getUser_id());
                        statement.executeUpdate();
                    } catch (Exception e) {
                        e.printStackTrace();
                        showAlert("Error deleting account.");
                    }

                    loadUsersFromDatabase();
                }
            });
        } else {
            showAlert("Please select an account to delete.");
        }
    }


    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
