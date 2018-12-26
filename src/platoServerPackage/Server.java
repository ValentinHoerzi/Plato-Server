/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package platoServerPackage;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Valentin Hörzi valentin.hoerzi@gmail.com
 */
public class Server implements Runnable {

    private static final int PORT_NUMBER = 4711;

    private static final Controller mainController = Main.getController();

    public static void start()
    {
        //<editor-fold defaultstate="collapsed" desc="Variables">

        List<ServerThread> ClientList = new ArrayList<>();
        Map<String, List<ServerThread>> RoomMap = new TreeMap<>();
        List<String> listNames = new ArrayList<>();

        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="Booting Server">
        mainController.writeOnListView("Booting Server");
        System.out.println("Booting Server");

        ServerSocket serverSocket = null;
        try
        {
            serverSocket = new ServerSocket(PORT_NUMBER);
        } catch (IOException ex)
        {
            mainController.writeOnListView("Error at initiating Server Socket at Server");
            System.err.println("Error at initiating Server Socket at Server");
            ex.printStackTrace();
        }

        mainController.writeOnListView("Booting Successful");
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
                mainController.writeOnListView("Error at eccepting Socket at Server");
                System.err.println("Error at eccepting Socket at Server");
                ex.printStackTrace();
            }

            mainController.writeOnListView("User connected at " + Main.getTime() + "- From IP: " + clientSocket.getInetAddress().toString());
            System.out.println("User connected at " + Main.getTime());

            ServerThread serverthread = null;
            try
            {
                serverthread = new ServerThread(clientSocket, ClientList, RoomMap, listNames);
            } catch (IOException ex)
            {
                mainController.writeOnListView("Error at Creating ServerThread at Server");
                System.err.println("Error at Creating ServerThread at Server");
                ex.printStackTrace();
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
