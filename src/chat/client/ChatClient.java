package chat.client;

import java.net.*;
import java.io.*;
import java.security.*;

public class ChatClient implements Runnable {

    private Socket socket = null;
    private Thread thread = null;
    private DataInputStream console = null;
    private DataOutputStream streamOut = null;
    private ChatClientThread client = null;
    private KeyPairGenerator keyGen = null;
    private SecureRandom random = null;
    private KeyPair pair = null;
    private Signature dsa = null;

    public ChatClient(String serverName, int serverPort) {
        try {
            keyGen = KeyPairGenerator.getInstance("DSA");
            random = SecureRandom.getInstance("SHA1PRNG", "SUN");
            keyGen.initialize(1024, random);
            pair = keyGen.generateKeyPair();
            dsa.initSign(pair.getPrivate());

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        System.out.println("Establishing connection to server...");

        try {
            // Establishes connection with server (name and port)
            socket = new Socket(serverName, serverPort);
            System.out.println("Connected to server: " + socket);
            start();
        } catch (UnknownHostException uhe) {
            // Host unkwnown
            System.out.println("Error establishing connection - host unknown: " + uhe.getMessage());
        } catch (IOException ioexception) {
            // Other error establishing connection
            System.out.println("Error establishing connection - unexpected exception: " + ioexception.getMessage());
        }

    }

    public void run() {
        while (thread != null) {
            try {
                // Sends message from console to server
                streamOut.writeUTF(console.readLine());
                streamOut.flush();
            } catch (IOException ioexception) {
                System.out.println("Error sending string to server: " + ioexception.getMessage());
                stop();
            }
        }
    }

    public void handle(String msg) {
        // Receives message from server
        if (msg.equals(".quit")) {
            // Leaving, quit command
            System.out.println("Exiting...Please press RETURN to exit ...");
            stop();
        } else // else, writes message received from server to console
        {
            System.out.println(msg);
        }
    }

    // Inits new client thread
    public void start() throws IOException {
        console = new DataInputStream(System.in);
        streamOut = new DataOutputStream(socket.getOutputStream());
        if (thread == null) {
            client = new ChatClientThread(this, socket);
            thread = new Thread(this);
            thread.start();
        }
    }

    // Stops client thread
    public void stop() {
        if (thread != null) {
            thread.stop();
            thread = null;
        }
        try {
            if (console != null) {
                console.close();
            }
            if (streamOut != null) {
                streamOut.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException ioe) {
            System.out.println("Error closing thread...");
        }
        client.close();
        client.stop();
    }

    public static void main(String args[]) {
        ChatClient client = null;
        if (args.length != 2) // Displays correct usage syntax on stdout
        {
            System.out.println("Usage: java ChatClient host port");
        } else // Calls new client
        {
            client = new ChatClient(args[0], Integer.parseInt(args[1]));
        }
    }

}
