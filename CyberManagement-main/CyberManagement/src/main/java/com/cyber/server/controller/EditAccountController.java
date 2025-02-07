package com.cyber.server.controller;

import com.cyber.server.database.DatabaseConnection;
import com.cyber.server.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class EditAccountController {

    @FXML private TextField usernameField;
    @FXML private TextField passwordField;
    @FXML private Button saveButton;

    private User selectedUser;
    private Stage stage;

    public void setSelectedUser(User selectedUser) {
        this.selectedUser = selectedUser;
        usernameField.setText(selectedUser.getUsername());
        passwordField.setText(selectedUser.getPassword());
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void editAccount() {
        String newUsername = usernameField.getText();
        String newPassword = passwordField.getText();

        if (newUsername.isEmpty() || newPassword.isEmpty()) {
            showAlert("Both username and password must be filled.");
            return;
        }

        // Update the user in the database
        String updateQuery = "UPDATE users SET username = ?, password = ? WHERE user_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(updateQuery)) {

            statement.setString(1, newUsername);
            statement.setString(2, newPassword);
            statement.setInt(3, selectedUser.getUser_id());

            int affectedRows = statement.executeUpdate();
            if (affectedRows > 0) {
                showAlert("Account updated successfully!");
                stage.close(); // Close the edit window
            } else {
                showAlert("Failed to update account. Please try again.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error updating account: " + e.getMessage());
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
