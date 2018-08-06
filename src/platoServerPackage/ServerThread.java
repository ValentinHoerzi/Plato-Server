/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package platoServerPackage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import javafx.application.Platform;

/**
 *
 * @author Valentin
 */
public class ServerThread implements Runnable {

    private final Socket clientSocket;
    private final BufferedReader reader;
    private final PrintWriter writer;
    private final Scanner scanner;
    private List<ServerThread> ClientList;
    private List<String> listNames;
    private String userName;
    private Map<String, List<ServerThread>> RoomMap;
    private boolean inRoom = false;
    private String roomName = "";

    ServerThread(Socket clientSocket, List<ServerThread> RunnableList, Map<String, List<ServerThread>> RoomMap, List<String> listNames) throws IOException
    {
        this.clientSocket = clientSocket;
        this.writer = new PrintWriter(this.clientSocket.getOutputStream(), true);
        this.reader = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
        this.scanner = new Scanner(System.in);
        this.ClientList = RunnableList;
        this.RoomMap = RoomMap;
        this.listNames = listNames;

    }

    @Override
    public void run()
    {

        String string = null;
        try
        {
            string = reader.readLine();
        } catch (IOException ex)
        {
            Platform.runLater(() -> Main.getController().getObserveListView().add("Error at reading String at ServerThread : 61"));
            System.err.println("Error at reading Stirng at ServerThread : 61");
        }

        while (true)
        {
            if (handleCommands(string))
            {
                try
                {
                    clientSocket.close();
                } catch (IOException ex)
                {
                    Platform.runLater(() -> Main.getController().getObserveListView().add("Error at closing Socket at ServerThread : 73"));
                    System.err.println("Error at closing Socket at ServerThread : 73");
                }
                return;
            }

            try
            {
                string = reader.readLine();
            } catch (IOException ex) //LINE IF USER CLOSES WINDOW
            {
                listNames.remove(userName);
                ClientList.forEach(w -> w.writer.println(listNames));
                ClientList.remove(this);
                try
                {
                    clientSocket.close();
                    System.out.println("User disconnected at " + Main.getTime());
                    Platform.runLater(() -> Main.getController().getObserveListView().add("User disconnected at " + Main.getTime()));
                    ClientList.forEach(s -> s.writer.println(userName + " disconnected "));
                    return;
                } catch (IOException ex1)
                {
                    Platform.runLater(() -> Main.getController().getObserveListView().add("Error at closing Client at ServerThread : 87"));
                    System.err.println("Error at closing Client at ServerThread : 87");
                }
                Platform.runLater(() -> Main.getController().getObserveListView().add("Error at reading Stirng at ServerThread : 89"));
                System.err.println("Error at reading Stirng at ServerThread : 89");
            }
        }
    }

    private boolean handleCommands(String input)
    {
        if (input.startsWith("<login=")) //geht
        {
            userName = input.split("=")[1].replace(">", "");
            ClientList.forEach(w -> w.writer.println("-" + userName + "- just joined the chat"));
            listNames.add(userName);
            ClientList.forEach(w -> w.writer.println(listNames));
        } else if (input.startsWith("<logout>")) //geht
        {
            ClientList.forEach(w -> w.writer.println("-" + userName + "- left"));
            listNames.remove(userName);
            ClientList.forEach(w -> w.writer.println(listNames));
            ClientList.remove(this);
            Platform.runLater(() -> Main.getController().getObserveListView().add("Connection canceled"));
            System.out.println("Connection canceled");
            return true;
        } else if (input.startsWith("<getClients>")) //geht
        {
            ClientList.forEach(w -> writer.println(w.userName));
        } else if (input.startsWith("<send=")) //geht
        {
            boolean found = false;
            String username = input.split("=")[1].split(">")[0];
            for (ServerThread user : ClientList)
            {
                if (user.userName.equals(username))
                {
                    user.writer.println("{Private Message} " + userName + ": " + input.split(">")[1].split(";")[0]);
                    found = true;
                }
            }
            if (!found)
            {
                username = input.split(">")[1].split(";")[1];
                for (ServerThread user : ClientList)
                {
                    if (user.userName.equals(username))
                    {
                        user.writer.println("Couldn't find that user");
                        found = true;
                    }
                }
            }
        } else if (input.startsWith("<join="))
        {
            if (!inRoom)
            {
                boolean found = false;
                roomName = input.split("=")[1].split(">")[0];
                for (String string : RoomMap.keySet())
                {
                    if (string.equals(roomName))
                    {
                        found = true;
                        RoomMap.get(roomName).add(this);
                    }
                }
                if (!found)
                {
                    List<ServerThread> temp = new ArrayList<>();
                    temp.add(this);
                    RoomMap.putIfAbsent(roomName, temp);
                }
                RoomMap.get(roomName).forEach((ServerThread s) -> s.writer.println(userName + " just joined the room" + " [" + roomName + "]"));
                inRoom = true;
            } else
            {
                String username = input.split("=")[1].split(">")[1];
                for (ServerThread s : ClientList)
                {
                    if (s.userName.equals(username))
                    {
                        s.writer.println("You already are in a room");
                    }
                }
            }
        } else if (input.startsWith("<leave>"))
        {
            RoomMap.get(roomName).forEach(s -> s.writer.println(userName + " left the room" + " [" + roomName + "]"));

            RoomMap.get(roomName).remove(this);

            if (RoomMap.get(roomName).isEmpty())
            {
                RoomMap.remove(roomName);
            }
            roomName = "";
            inRoom = false;
        } else if (input.startsWith("<getRooms>"))
        {
            String name = input.split(">")[1];
            Set<String> keySet = RoomMap.keySet();
            if (keySet.isEmpty())
            {
                for (ServerThread user : ClientList)
                {
                    if (user.userName.equals(name))
                    {
                        user.writer.println("No Rooms available");
                    }
                    return false;
                }
            }

            ClientList.stream().filter((user) -> (user.userName.equals(name))).forEachOrdered((ServerThread user) ->
            {
                keySet.stream().map((string) ->
                {
                    user.writer.println("--" + string + "--");
                    return string;
                }).forEachOrdered((string) ->
                {
                    Collections.sort(RoomMap.get(string), (ServerThread o1, ServerThread o2) -> o1.userName.compareTo(o2.userName));
                    RoomMap.get(string).forEach((k) -> user.writer.println("-> " + k.userName));
                });
            });
        } else
        {
            if (inRoom)
            {
                RoomMap.get(roomName).forEach(s ->
                {
                    if (!s.equals(this))
                    {
                        s.writer.println("[" + roomName + "] " + userName + ": " + input);
                    }
                });

            } else
            {
                ClientList.forEach(s -> s.writer.println(userName + ": " + input));
            }
        }
        return false;
    }
}
