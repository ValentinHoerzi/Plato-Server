/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package platoServerPackage;

import java.net.URL;
import java.sql.DatabaseMetaData;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

/**
 * FXML Controller class
 *
 * @author Valentin
 */
public class Controller implements Initializable {

    @FXML
    private Button buttonStartServer;
    @FXML
    private ListView<String> listView;
    private ObservableList<String> observeListView;
    @FXML
    private Label labelTopLeftCorner;
    @FXML
    private Label labelTopRightCorner;
    @FXML
    private Label labelDownLeft;
    @FXML
    private Label labelDownRight;
    @FXML
    private Label labelShowRuntime;
    @FXML
    private Label labelShowClients;
    
    
    
    public ObservableList<String> getObserveListView()
    {
        return observeListView;
    }

    /**
     * Initializes the controller class.
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        observeListView = FXCollections.observableArrayList();
        listView.setItems(observeListView);
    }

    @FXML
    private void handleButtonStart(ActionEvent event)
    {
        Thread runningThread = new Thread(new Server());
        runningThread.start();
        buttonStartServer.setDisable(true);
    }

}
