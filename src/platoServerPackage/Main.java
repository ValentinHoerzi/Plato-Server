/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package platoServerPackage;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 *
 * @author Valentin
 */
public class Main extends Application {

    //Idee: Block user?
    //Idee: Anzeige: Wie viele Nutzer connected sind
    //Idee: Uhrzeit i.wo anzeigen
    private static Controller controller;

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("View.fxml"));
        Parent root = loader.load();
        controller = loader.getController();
        Scene scene = new Scene(root);

        //Adds icon as Programmicon
        stage.getIcons().add(new Image(Main.class.getResourceAsStream("/Images/icon.png")));
        //Sets The Programm Title
        stage.setTitle("Server for Plato Clients");
        //Exits Programm when Window closed
        stage.setOnCloseRequest((WindowEvent e) -> {Platform.exit();System.exit(0);});
        //Sets up stage
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static Controller getController() {
        return controller;
    }

    public static String getTime() {
        return new SimpleDateFormat("dd.MM.yyyy HH:mm").format(Calendar.getInstance().getTime());
    }
}
