package quanlynet;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApplication extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/quanlynet/view/main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        scene.getStylesheets().add(getClass().getResource("/quanlynet/css/style.css").toExternalForm());
        stage.setTitle("Quản Lý Quán Net");
        stage.setScene(scene);
        stage.setMaximized(true); // Hiển thị toàn màn hình
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
