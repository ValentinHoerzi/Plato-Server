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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Valentin
 */
public class ServerThread implements Runnable {

    private final Socket clientSocket;
    private final BufferedReader inputStreamReader;
    private final PrintWriter printWriter;
    private final List<ServerThread> listOfClients;
    private final List<String> namesInNetwork;
    private String userName;
    private final Map<String, List<ServerThread>> availableRooms;
    private boolean inRoom = false;
    private String currentRoom = "";
    private final Controller mainController = Main.getController();

    ServerThread(Socket clientSocket, List<ServerThread> RunnableList, Map<String, List<ServerThread>> RoomMap, List<String> listNames) throws IOException {
        this.clientSocket = clientSocket;
        this.listOfClients = RunnableList;
        this.availableRooms = RoomMap;
        this.namesInNetwork = listNames;

        this.printWriter = new PrintWriter(this.clientSocket.getOutputStream(), true);
        this.inputStreamReader = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
    }

    @Override
    public void run() //needs to be tested
    {

        //Starts reading a message sent by the client
        String message = null;
        try {
            message = inputStreamReader.readLine();
        } catch (IOException ex) {
            mainController.writeOnListView("Error at reading String at ServerThread (last edit: line 60)");
            System.err.println("Error at reading Stirng at ServerThread (last edit: line 60)");
            ex.printStackTrace();
        }

        //Repeats reading messages sent by the client
        while (true) {

            //handleCommands returns true, if the clients operates any operation which leads to the shutdown of the client and thus loosing the communication
            if (handleCommands(message)) {
                try {
                    clientSocket.close();
                } catch (IOException ex) {
                    mainController.writeOnListView("Error at closing Socket at ServerThread (last edit: line 76)");
                    System.err.println("Error at closing Socket at ServerThread (last edit: line 76)");
                }
                return;
            }

            //If reading the message throws an error (Client closing its window, Connection Lost) the server disconnects with the client in order to prevent further errors
            try {
                message = inputStreamReader.readLine();
            } catch (IOException ex) {
                namesInNetwork.remove(userName);
                listOfClients.forEach(w -> w.printWriter.println(namesInNetwork));
                listOfClients.remove(this);
                try {
                    clientSocket.close();

                    mainController.writeOnListView("User disconnected at " + Main.getTime());
                    System.out.println("User disconnected at " + Main.getTime());

                    listOfClients.forEach(s -> s.printWriter.println(userName + " disconnected"));
                    return;
                } catch (IOException ex1) {
                    mainController.writeOnListView("Error at closing Client at ServerThread");
                    System.err.println("Error at closing Client at ServerThread");
                    ex1.printStackTrace();
                }

                mainController.writeOnListView("Error at reading Stirng at ServerThread");
                System.err.println("Error at reading Stirng at ServerThread");
                ex.printStackTrace();
            }
        }
    }

    private boolean handleCommands(String input) //this method has to be overwritten and cleaned
    {
        if (input.startsWith("<login=")) {
            userName = input.split("=")[1].replace(">", "");
            listOfClients.forEach(clients -> clients.printWriter.println("-" + userName + "- just joined the chat"));
            namesInNetwork.add(userName);
            listOfClients.forEach(clients -> clients.printWriter.println(namesInNetwork));

        } else if (input.startsWith("<logout>")) {
            listOfClients.forEach(clients -> clients.printWriter.println("-" + userName + "- left"));
            namesInNetwork.remove(userName);
            listOfClients.forEach(clients -> clients.printWriter.println(namesInNetwork));
            listOfClients.remove(this);
            mainController.writeOnListView("User disconnected at " + Main.getTime());
            System.out.println("User disconnected at " + Main.getTime());
            return true;

        } else if (input.startsWith("<getClients>")) {
            listOfClients.forEach(clients -> printWriter.println(clients.userName));

        } 
        //<editor-fold defaultstate="collapsed" desc="Private Message Section"> 
        else if (input.startsWith("<send=")) {
            boolean found = false;
            String username = input.split("=")[1].split(">")[0];
            for (ServerThread user : listOfClients) {
                if (user.userName.equals(username)) {
                    user.printWriter.println("{Private Message} " + userName + ": " + input.split(">")[1].split(";")[0]);
                    found = true;
                }
            }
            if (!found) {
                username = input.split(">")[1].split(";")[1];
                for (ServerThread user : listOfClients) {
                    if (user.userName.equals(username)) {
                        user.printWriter.println("Couldn't find that user");
                        found = true;
                    }
                }
            }
            //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="Room Section">
        } else if (input.startsWith("<join=")) {
            if (!inRoom) {
                boolean found = false;
                currentRoom = input.split("=")[1].split(">")[0];
                for (String string : availableRooms.keySet()) {
                    if (string.equals(currentRoom)) {
                        found = true;
                        availableRooms.get(currentRoom).add(this);
                    }
                }
                if (!found) {
                    List<ServerThread> temp = new ArrayList<>();
                    temp.add(this);
                    availableRooms.putIfAbsent(currentRoom, temp);
                }
                availableRooms.get(currentRoom).forEach((ServerThread s) -> s.printWriter.println(userName + " just joined the room" + " [" + currentRoom + "]"));
                inRoom = true;
            } else {
                String username = input.split("=")[1].split(">")[1];

                listOfClients.stream().filter((s) -> (s.userName.equals(username))).forEachOrdered((s)
                        -> {
                    s.printWriter.println("You already are in a room");
                });
            }
        } else if (input.startsWith("<leave>")) {
            availableRooms.get(currentRoom).forEach(s -> s.printWriter.println(userName + " left the room" + " [" + currentRoom + "]"));

            availableRooms.get(currentRoom).remove(this);

            if (availableRooms.get(currentRoom).isEmpty()) {
                availableRooms.remove(currentRoom);
            }
            currentRoom = "";
            inRoom = false;
        } else if (input.startsWith("<getRooms>")) {
            String name = input.split(">")[1];
            Set<String> keySet = availableRooms.keySet();
            if (keySet.isEmpty()) {
                for (ServerThread user : listOfClients) {
                    if (user.userName.equals(name)) {
                        user.printWriter.println("No Rooms available");
                    }
                    return false;
                }
            }

            listOfClients.stream().filter((user) -> (user.userName.equals(name))).forEachOrdered((ServerThread user)
                    -> {
                keySet.stream().map((string)
                        -> {
                    user.printWriter.println("--" + string + "--");
                    return string;
                }).forEachOrdered((string)
                        -> {
                    Collections.sort(availableRooms.get(string), (ServerThread o1, ServerThread o2) -> o1.userName.compareTo(o2.userName));
                    availableRooms.get(string).forEach((k) -> user.printWriter.println("-> " + k.userName));
                });
            });
        } //</editor-fold>
        else {
            if (inRoom) {
                availableRooms.get(currentRoom).forEach(client -> {
                    if (!client.equals(this)) {
                        client.printWriter.println("[" + currentRoom + "] " + userName + ": " + input);
                    }
                });

            } else {
                listOfClients.forEach(s -> s.printWriter.println(userName + ": " + input));
            }
        }
        return false;
    }
}
