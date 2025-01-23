package quanlynet.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class MainController {

    @FXML
    private StackPane contentArea;


    @FXML
    private void showUserView() {
        loadView("user-view.fxml");  // Đường dẫn đơn giản
    }

    private void loadView(String fxmlFileName) {
        try {
            // Sử dụng FXMLLoader và getClass().getResource() để tải FXML
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/quanlynet/view/" + fxmlFileName));
            Node view = fxmlLoader.load();
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
