/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package platoServerPackage;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javafx.application.Platform;
import javax.swing.text.EditorKit;

/**
 *
 * @author Valentin
 */
public class Server implements Runnable{

    private static final int PORT_NUMBER = 12345;

    public static void start()
    {
       //<editor-fold defaultstate="collapsed" desc="Variables">
        
        List<ServerThread> ClientList = new ArrayList<>();
        Map<String, List<ServerThread>> RoomMap = new TreeMap<>();
        List<String> listNames = new ArrayList<>();
        //</editor-fold>
       //<editor-fold defaultstate="collapsed" desc="Booting Server">
        
        Platform.runLater(() -> Main.getController().getObserveListView().addAll("Booting Server"));
        System.out.println("Booting Server");
        ServerSocket serverSocket = null;
            try
            {
                serverSocket = new ServerSocket(PORT_NUMBER);
            } catch (IOException ex)
            {
                Platform.runLater(() -> Main.getController().getObserveListView().add("Error at initiating Server Socket at Server : 37"));
                System.err.println("Error at initiating Server Socket at Server : 37");
            }
        Platform.runLater(() -> Main.getController().getObserveListView().add("Booting Successful"));
        System.out.println("Booting Successful");
        //</editor-fold>
        while (true)
        {
            Socket clientSocket = null;
                try
                {
                    clientSocket = serverSocket.accept();
                } catch (IOException ex)
                {
                    Platform.runLater(() -> Main.getController().getObserveListView().add("Error at eccepting Socket at Server : 51"));
                    System.err.println("Error at eccepting Socket at Server : 51");
                }
            Platform.runLater(() -> Main.getController().getObserveListView().add("User connected at "+Main.getTime()));
            System.out.println("User connected at "+Main.getTime());
            ServerThread serverthread = null;
                try
                {
                    serverthread = new ServerThread(clientSocket, ClientList, RoomMap, listNames);
                } catch (IOException ex)
                {
                    Platform.runLater(() -> Main.getController().getObserveListView().add("Error at Creating ServerThread at Server : 60"));
                    System.err.println("Error at Creating ServerThread at Server : 60");
                }
            ClientList.add(serverthread);
            Thread thread = new Thread(serverthread);
            thread.start();
        }
    }

    @Override
    public void run()
    {
       start();
    }
}
