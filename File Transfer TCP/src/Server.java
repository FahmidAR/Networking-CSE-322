import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Queue;
import java.util.Vector;

public class Server {

    static HashMap<Integer, Boolean> LogOnOf = new HashMap<Integer, Boolean>();
    static HashMap<Integer, Vector<FileInfo>> LogFile = new HashMap<Integer, Vector<FileInfo>>();
    static HashMap<Integer, ArrayList<String>> LogMsg = new HashMap<Integer, ArrayList<String>>();

    static int FileNoS = 1;
    static int MAX_BUFFER_SIZE =4096*4096*16;
    static int CURRENT_BUFFER_SIZE = 0;
    static String dirName = "Server_Files";

    public static void main(String[] args) throws IOException, ClassNotFoundException {


        File theDir = new File(dirName);
        if (!theDir.exists() ){
            theDir.mkdirs();
        }

        int TotalClinet = 1 ;

        ServerSocket welcomeSocket = new ServerSocket(6666);

        while(!welcomeSocket.isClosed()) {
            System.out.println("\nWaiting for connection ^_^");
            Socket socket = welcomeSocket.accept();
            System.out.println("Connection established SUCCESSFULLY");

            System.out.println("Clinet "+TotalClinet+" Port no = "+socket.getPort());
            System.out.println("Clinet "+TotalClinet+" IP no = "+socket.getInetAddress());

            // open thread
            Thread worker = new Worker(socket,TotalClinet);
            worker.start();
            TotalClinet++;


        }

    }
}
