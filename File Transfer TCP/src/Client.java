import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.text.DecimalFormat;
import java.util.Scanner;

public class Client {

    static int MAX_CHUNK_SIZE = 2000;
    static int MIN_CHUNK_SIZE = 1024;
    static int CHUNK_SIZE = 2000;


    static String dirNameC = "Client_Files";
    static int FileNoSC = 1;



    public static void main(String[] args) throws IOException, ClassNotFoundException {

        Boolean pRun =true;

        Socket socket = new Socket("localhost", 6666);
        System.out.println("Connection established");
        System.out.println("Remote port: " + socket.getPort());
        System.out.println("Remote IP: " + socket.getInetAddress());
        System.out.println("Local port: " + socket.getLocalPort());
        System.out.println("Local IP: " + socket.getLocalAddress());

        // buffers
        //ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        //ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        DataInputStream in = new DataInputStream(socket.getInputStream());


        Scanner userInput = new Scanner(System.in);
        Scanner userInput2 = new Scanner(System.in);
        Scanner userInput3 = new Scanner(System.in);

        String FileName = new String();
        String FileType = new String();
        int choose ;

        String s = new String();

        boolean flag1 = true;


        while(true) {

            if(flag1)
            {
                System.out.print("Enter Your ID = ");
                choose = userInput.nextInt();
                out.writeUTF("ID?"+choose);
                out.flush();

                String msg = in.readUTF();

                if(msg.equalsIgnoreCase("deny"))
                {
                    System.out.println("This Client have been logged in out Other Device");
                    System.out.println("Access denied , Sokect Shut down ... ");
                    out.close();
                    System.exit(0);
                }

                flag1 =false;
                continue;
            }
            System.out.println("\n\n+=================================+");
            System.out.println("|         Client Menu ^_^         |");
            System.out.println("+=================================+");
            System.out.println(" 1. View ALL User List ");
            System.out.println(" 2. View ONLINE User List ");
            System.out.println(" 3. View ALL user FILE ");
            System.out.println(" 4. View MY uploaded List ");
            System.out.println(" 5. Upload File ");
            System.out.println(" 6. Download File ");
            System.out.println(" 7. Request a File ");
            System.out.println(" 8. View File request and notifications ");
            System.out.println(" 9. Upload requested File");
            System.out.println(" 10. Log Out ");
            System.out.println(" 11. View Specific user FILE ");
            System.out.println(" Press other Chat ");
            System.out.println("+=================================+\n");

            System.out.print(" \nEnter you choice = ");

            choose = userInput.nextInt();

            if ( choose == 1)
            {
                out.writeUTF("viewlog");
                out.flush();

                //String msg = (String) in.readObject();
                String msg = (String) in.readUTF();
                System.out.println(msg);
            }
            else if ( choose == 2)
            {
                out.writeUTF("viewlogonline");
                out.flush();

                //String msg = (String) in.readObject();
                String msg = (String) in.readUTF();
                System.out.println(msg);
            }
            else if ( choose == 3)
            {
                //out.flush();
                out.writeUTF("viewfileall?ALL");
                out.flush();

                //String msg = (String) in.readObject();
                String msg = (String) in.readUTF();
                System.out.println(msg);
            }
            else if ( choose == 4)
            {
                out.writeUTF("viewfileme");
                out.flush();

                //String msg = (String) in.readObject();
                String msg = (String) in.readUTF();
                System.out.println(msg);
            }
            else if ( choose == 5)
            {
                System.out.println("Enter File Name : ");
                FileName = userInput2.nextLine();
                System.out.println("Enter File Type (Public/Private):");
                FileType = userInput3.nextLine();
                File f = new File(FileName);
                long bytes = f.length();
                out.writeUTF("upload?"+FileName+"?"+bytes+"?"+FileType);
                out.flush();

                String msg = in.readUTF();
                String[] words = msg.split("\\s+");
                CHUNK_SIZE = Integer.parseInt(words[0]);
                int fileNo = Integer.parseInt(words[1]);
                String permission = words[2];

                if(permission.equalsIgnoreCase("Yes")) {

                    System.out.println("Chunk Size = " + CHUNK_SIZE + " File no = " + String.valueOf(fileNo));

                    if(pRun) {

                        Socket socketc1 = new Socket("localhost", 6667);
                        System.out.println("Connection established");

                        Thread workerClient1 = new WorkerClient2(socketc1, FileName, (int) bytes, CHUNK_SIZE);
                        workerClient1.start();
                    }
                    else
                    {
                        sendFile(socket, FileName, out, in, (int) bytes);
                    }

                    //sendFile(socket, FileName, out, in, (int) bytes);

                    System.out.println("FILE SENDING process done, BE Happy ...\n");
                }
                else
                {
                    System.out.println("Server is Currenly Busy , Try again later...\n");
                }
                //out = new ObjectOutputStream(socket.getOutputStream());
            }
            else if ( choose == 6)
            {
                System.out.println("Enter File Name : ");
                FileName = userInput2.nextLine();


                out.writeUTF("download?"+FileName);

                File theDir = new File(dirNameC+"/My_Downloads");
                if (!theDir.exists()){
                    theDir.mkdirs();
                }

                File f = new File(FileName);
                //long bytes = f.length();
                String fn = f.getName();

                fn= dirNameC+"/My_Downloads/"+String.valueOf(FileNoSC)+"-"+fn;
                System.out.println("Modified File Name = "+fn);

                String messageFromServer = in.readUTF();
                String[] words = messageFromServer.split("\\?");

                MAX_CHUNK_SIZE = Integer.parseInt(words[0]);
                long bytes = Integer.parseInt(words[1]) ;

                saveFile(in,out,fn,(int)bytes);
                FileNoSC++;
                System.out.println("File Recieving Precess done Successfully..");


            }
            else if ( choose == 7)
            {
                System.out.println("Enter File Name : ");
                FileName = userInput2.nextLine();
                System.out.println("Enter Short Discription :");
                String FileDes = userInput3.nextLine();

                out.writeUTF("reqfile?"+FileName+"?"+FileDes);
                out.flush();
            }
            else if ( choose == 8)
            {
                out.writeUTF("showreqfile");
                out.flush();

                String msg = (String) in.readUTF();
                System.out.println(msg);
            }
            else if ( choose == 9)
            {
                System.out.println("Enter File Name : ");
                FileName = userInput2.nextLine();
                // System.out.println("Enter File Type (Public/Private):");
                //FileType = userInput3.nextLine();
                FileType ="Public";
                File f = new File(FileName);
                long bytes = f.length();
                System.out.println("Enter File Req no (Roll-Name):");
                String FileReqNo = userInput3.nextLine();

                out.writeUTF("upreqfile?"+FileName+"?"+bytes+"?"+FileType+"?"+FileReqNo);
                out.flush();

                String msg = in.readUTF();
                String[] words = msg.split("\\s+");
                CHUNK_SIZE = Integer.parseInt(words[0]);
                int fileNo = Integer.parseInt(words[1]);
                String permission = words[2];

                if(permission.equalsIgnoreCase("Yes")) {

                    System.out.println("Chunk Size = " + CHUNK_SIZE + " File no = " + String.valueOf(fileNo));

                    sendFile(socket, FileName, out, in, (int) bytes);

                    System.out.println("REQ FILE SENDING process done, BE Happy ...\n");
                }
                else
                {
                    System.out.println("Server is Currenly Busy , Try again later...\n");
                }
                //out = new ObjectOutputStream(socket.getOutputStream());
            }
            else if ( choose == 10)
            {
                out.writeUTF("exit");
                out.flush();

                //String msg = (String) in.readObject();
                String msg = (String) in.readUTF();
                System.out.println(msg);

                System.out.println("This Client have been logged out");

                out.close();
                System.exit(0);


            }
            else if ( choose == 11)
            {
                //out.flush();
                System.out.print("Enter User ID : ");
                String S = userInput2.nextLine();

                out.writeUTF("viewfileall?SP?"+S);
                out.flush();

                //String msg = (String) in.readObject();
                String msg = (String) in.readUTF();
                System.out.println(msg);
            }
            else
            {
                s = userInput2.nextLine();
                out.writeUTF(s);
                out.flush();
            }
        }
    }

    private static  void saveFile(DataInputStream dis ,DataOutputStream dos,String FileName,int fSize) throws IOException {
        //DataInputStream dis = new DataInputStream(clientSock.getInputStream());
        FileOutputStream fos = new FileOutputStream(FileName);
        byte[] buffer = new byte[MAX_CHUNK_SIZE];
        final DecimalFormat df = new DecimalFormat("0.00");
        int filesize = fSize;
        //int filesize = 1331220 ;

        int read = 0;
        int cno= 1;
        int totalRead = 0;
        int remaining = filesize;
        double p ;
        //while((read = dis.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
        //while((read = dis.read(buffer)) > 0) {
        while (true){
            read = dis.read(buffer, 0, Math.min(buffer.length, remaining));
            totalRead += read;
            remaining -= read;

            p = (1.0*totalRead/filesize)*100;

            System.out.println("read " +cno+" no chunk , total "+ totalRead + " bytes sent , "+ df.format(p)+"%");
            cno++;

            fos.write(buffer, 0, read);

            if(p==100)
            {
                dos.writeUTF("complete");
                System.out.println("File recived Successfully .. ");
                break;
            }
            else
            {
                dos.writeUTF("got");
            }
        }

        String msg = null;
        while (msg==null){
            msg = dis.readUTF();
        }

        if(msg.equalsIgnoreCase("Complete"))
        {
            System.out.println("FIle Uploaded , All bye recieved ");

            if(totalRead == filesize)
            {
                dos.writeUTF("complete");
            }
            else
            {
                dos.writeUTF("notcomplete");

                File myfff = new File(FileName);
                if (myfff.delete()) {
                    System.out.println("Deleted the aborted file: " + myfff.getName());
                } else {
                    System.out.println("Failed to delete the aborted file");
                }

            }
        }
        if(msg.equalsIgnoreCase("TimeOut"))
        {
            fos.close();
            File myfff = new File(FileName);
            if (myfff.delete()) {
                System.out.println("Deleted the aborted file: " + myfff.getName());
            } else {
                System.out.println("Failed to delete the aborted file");
            }
        }

        fos.close();
        //dis.close();
    }

    private static void sendFile(Socket socket,String file,DataOutputStream dos,DataInputStream dis,int b) throws IOException {

        //DataOutputStream dos = new DataOutputStream(s.getOutputStream());
        Boolean flagTimout = false ;
        final DecimalFormat df = new DecimalFormat("0.00");
        double p ;

        try {
            FileInputStream fis = new FileInputStream(file);
            socket.setSoTimeout(30000);

            byte[] buffer = new byte[CHUNK_SIZE];
            int no = 1;

            int read = 0;
            int totalRead = 0;
            int remaining = b ;

            //while (fis.read(buffer) > 0) {
            //while((read = fis.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
            while (true){
                //dos.write(buffer);
                read = fis.read(buffer, 0, Math.min(buffer.length, remaining));
                dos.write(buffer, 0, Math.min(buffer.length, remaining));

                totalRead += read;
                remaining -= read;

                p = (1.0*totalRead/b)*100;

                String s = null;
                while (s==null) {
                    s= dis.readUTF();
                }

                if (s.equalsIgnoreCase("got")) {
                    System.out.println("achknowledge recived for " + no + " no chunk , Progress = "+df.format(p)+"%");
                } else if (s.equalsIgnoreCase("complete")) {
                    System.out.println("achknowledge recived for " + no + " no FINAL chunk, Progress = "+df.format(p)+"%");
                    dos.writeUTF("Complete");

                    String ss = null;
                    while (ss==null) {
                        ss= dis.readUTF();
                    }

                    if(ss.equalsIgnoreCase("complete"))
                    {
                        System.out.println("Byte exchanged Successfuly");
                    }
                    else
                    {
                        System.out.println("Byte exchanged Not Successfuly");
                    }

                    break;
                }
                no++;
                dos.flush();
            }

            dos.flush();
            fis.close();
            //dos.close();
        }
        catch (SocketTimeoutException e)
        {
            System.out.println("Time out ...");
            flagTimout = true ;
            dos.writeUTF("TimeOut");
            dos.flush();
        }

        /*if(!flagTimout)
        {
            dos.writeUTF("NotTimeOut");
            dos.flush();
        }*/


    }
}
