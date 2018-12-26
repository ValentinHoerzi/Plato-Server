/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package platoServerPackage;

import java.net.*;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.*;
import javafx.event.ActionEvent;
import javafx.fxml.*;
import javafx.scene.control.*;

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

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        observeListView = FXCollections.observableArrayList();
        listView.setItems(observeListView);
        labelShowClients.setText("0");
        labelShowRuntime.setText("00:00:00");
    }

    @FXML
    private void handleButtonStart(ActionEvent event)
    {
        Thread runningThread = new Thread(new Server());
        runningThread.start();
        buttonStartServer.setDisable(true);

        //<editor-fold defaultstate="collapsed" desc="Displaying Time">
        Thread controllTime = new Thread(new Runnable() {
            int second = 0;
            int secondI = 0;
            int minute = 0;
            int minuteI = 0;
            int hourI = 0;
            int hour = 0;

            @Override
            public void run()
            {
                //(hourIhour:minuteIminute:secondIsecond)
                while (true)
                {
                    try
                    {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex)
                    {
                        ex.printStackTrace();
                    }
                    second++;
                    if (second % 10 == 0)
                    {
                        second = 0;
                        secondI++;
                    }
                    if (secondI == 6)
                    {
                        secondI = 0;
                        minute++;
                    }
                    if (minute != 0 && minute % 10 == 0)
                    {
                        minute = 0;
                        minuteI++;
                    }
                    if (minuteI == 6)
                    {
                        minuteI = 0;
                        hour++;
                    }
                    if (hour != 0 && hour % 10 == 0)
                    {
                        hour = 0;
                        hourI++;
                    }
                    Platform.runLater(() -> labelShowRuntime.setText(String.valueOf(hourI) + String.valueOf(hour) + ":" + String.valueOf(minuteI) + String.valueOf(minute) + ":" + String.valueOf(secondI) + String.valueOf(second)));
                }
            }
        });
        controllTime.start();
        //</editor-fold>
    }

    public void writeOnListView(String string)
    {
        observeListView.add(string);
    }
}
