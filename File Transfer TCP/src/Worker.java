import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.Vector;

public class Worker extends Thread  {
    Socket socket; int n; int id;

    static int MAX_CHUNK_SIZE = 200;
    static int MIN_CHUNK_SIZE = 102;
    static int CHUNK_SIZE = 200;
    boolean pRun = true ;

    public Worker(Socket socket,int n )
    {
        this.socket = socket;
        this.n = n;
        this.id = 420;
    }

    public void run()
    {
        // buffers
        try {

            String fName="404";
            Boolean FIleFlagUP =  false;
            //ObjectOutputStream out = new ObjectOutputStream(this.socket.getOutputStream());
            //ObjectInputStream in = new ObjectInputStream(this.socket.getInputStream());
            DataOutputStream out = new DataOutputStream(this.socket.getOutputStream());
            DataInputStream in = new DataInputStream(this.socket.getInputStream());

            while(!this.socket.isClosed()) {
                try {



                    System.out.println("Server Waiting fo Command...");
                    String messageFromClient = in.readUTF();

                    /*if(messageFromClient == null){
                        // Notify via terminal, close connection
                        System.out.println("Client"+id+" disconnected thus Socket closing...");
                        Server.LogOnOf.put(id,false);
                        out.close();

                        try {
                            this.socket.close();
                        } catch (Throwable ignored) {}
                        break;
                    }*/

                    System.out.println(messageFromClient);

                    String[] words = messageFromClient.split("\\?");

                    if (words[0].equals("exit")) {

                        Server.LogOnOf.put(id,false);
                        out.close();

                        try {
                            this.socket.close();
                        } catch (Throwable ignored) {}

                        break;
                    }
                    if (words[0].equals("viewfileall"))
                    {
                        String log = "# Files of All users #\n";

                        if(words[1].equalsIgnoreCase("SP"))
                        {
                            int idd = Integer.parseInt(words[2]);
                            FileInfo temp = new FileInfo();

                            for (int j = 0; j < Server.LogFile.get(idd).size(); j++)
                            {
                                temp =Server.LogFile.get(idd).get(j);

                                if(temp.PublicStatus)
                                {
                                    log += String.valueOf(temp.FilenNo)+" "+temp.FileName
                                            +" "+String.valueOf(temp.FileSize)+" Public\n";
                                }
                                else
                                {
                                    if(idd==id)
                                    {
                                        if(!temp.PublicStatus)
                                        {
                                            log += String.valueOf(temp.FilenNo)+" "+temp.FileName
                                                    +" "+String.valueOf(temp.FileSize)+" Private\n";
                                        }
                                    }
                                }

                            }
                        }
                        else
                        {
                            for (int idd : Server.LogFile.keySet())
                            {
                                log += String.valueOf(idd)+" : \n";
                                log +="Fileno File-Name FIle-Size Status \n";

                                FileInfo temp = new FileInfo();

                                for (int j = 0; j < Server.LogFile.get(idd).size(); j++)
                                {
                                    temp =Server.LogFile.get(idd).get(j);

                                    if(temp.PublicStatus)
                                    {
                                        log += String.valueOf(temp.FilenNo)+" "+temp.FileName
                                                +" "+String.valueOf(temp.FileSize)+" Public\n";
                                    }
                                    else
                                    {
                                        if(idd==id)
                                        {
                                            if(!temp.PublicStatus)
                                            {
                                                log += String.valueOf(temp.FilenNo)+" "+temp.FileName
                                                        +" "+String.valueOf(temp.FileSize)+" Private\n";
                                            }
                                        }
                                    }

                                }
                            }
                        }

                        //out.writeObject(log);
                        out.writeUTF(log);
                        out.flush();
                        continue;


                    }
                    if (words[0].equals("viewfileme"))
                    {
                        String log ="## my id = "+id+"##\n";
                        log += "# Public Files of mine #\n";
                        log +="Fileno File-Name FIle-Size Status\n";

                        //System.out.println(Server.LogFile.get(id).firstElement().FileName);

                        FileInfo temp = new FileInfo();

                        for (int i = 0; i < Server.LogFile.get(id).size(); i++)
                        {
                            temp =Server.LogFile.get(id).get(i);

                            if(temp.PublicStatus)
                            {
                                log += String.valueOf(temp.FilenNo)+" "+temp.FileName+" "+
                                        String.valueOf(temp.FileSize)+" Public\n";
                            }
                        }

                        log += "# Private Files of mine #\n";
                        log +="Fileno File-Name FIle-Size Status\n";

                        for (int i = 0; i < Server.LogFile.get(id).size(); i++)
                        {
                            temp =Server.LogFile.get(id).get(i);

                            if(!temp.PublicStatus)
                            {
                                log += String.valueOf(temp.FilenNo)+" "+temp.FileName
                                        +" "+String.valueOf(temp.FileSize)+" Public\n";
                            }
                        }

                        //out.writeObject(log);
                        out.writeUTF(log);
                        out.flush();
                        continue;


                    }
                    if (words[0].equals("viewlog")) {

                        String log = "# Status of Users #\n";

                        for (int i : Server.LogOnOf.keySet()) {
                            //System.out.println("key: " + i + " value: " + Server.LogOnOf.get(i));
                            log += String.valueOf(i);
                            if(Server.LogOnOf.get(i))
                            {
                                log += " Online\n";
                            }
                            else
                            {
                                log += " Offline\n";
                            }
                        }

                        //out.writeObject(log);
                        out.writeUTF(log);
                        out.flush();

                        continue;
                    }
                    if (words[0].equals("viewlogonline")) {

                        String log = "# Status of Users #\n";

                        for (int i : Server.LogOnOf.keySet()) {
                            //System.out.println("key: " + i + " value: " + Server.LogOnOf.get(i));

                            if(Server.LogOnOf.get(i))
                            {
                                log += String.valueOf(i)+"\n";
                            }
                        }

                        //out.writeObject(log);
                        out.writeUTF(log);
                        out.flush();

                        continue;
                    }
                    if (words[0].equals("reqfile"))
                    {
                        String ReqID = String.valueOf(id)+"-"+words[1];

                        for (int i : Server.LogMsg.keySet()) {

                            if(i!=id)
                            {
                                String log =ReqID+" " ;

                                Boolean flag = true ;

                                for(String s:words)
                                {
                                    if(flag)
                                    {
                                        flag = false;
                                        continue;
                                    }

                                    log+=s+" ";

                                }

                                log+=", Unread ";

                                Server.LogMsg.get(i).add(log);
                            }

                        }

                        continue;

                    }
                    if (words[0].equals("showreqfile"))
                    {
                        String log = "## Requested File List ##\n";
                        log +="ReqNo Descriptions\n";

                        for(String s : Server.LogMsg.get(id)) {
                            log +=s.toString()+"\n";
                        }

                        out.writeUTF(log);
                        out.flush();

                        for(int i=0;i<Server.LogMsg.get(id).size();i++)
                        {
                            String s =Server.LogMsg.get(id).get(i).replace("Unread","Read");
                            Server.LogMsg.get(id).set(i,s);
                        }

                        continue;

                    }

                    if (words[0].equals("download")) {

                        File f = new File(words[1]);
                        long bytes = f.length();

                        out.writeUTF(String.valueOf(MAX_CHUNK_SIZE)+"?"+bytes);
                        out.flush();

                        sendFile(this.socket,words[1],out,in,(int)bytes);

                        System.out.println("FILE SENDING process done, BE Happy ...\n");

                    }
                    if (words[0].equals("upload")||words[0].equals("upreqfile")) {

                        FIleFlagUP =true;

                        CHUNK_SIZE = MIN_CHUNK_SIZE + (int)(Math.random() * MAX_CHUNK_SIZE );
                        System.out.println("Chunk Size = "+CHUNK_SIZE+" File no = "+String.valueOf(Server.FileNoS));

                        String Permi = "NO";

                        File f = new File(words[1]);
                        String fn = f.getName();
                        System.out.println("File Name = "+fn);
                        System.out.println("File Size = "+words[2]);

                        int fileSIze = Integer.parseInt(words[2]);

                        Server.CURRENT_BUFFER_SIZE +=fileSIze;
                        System.out.println(Server.CURRENT_BUFFER_SIZE);
                        System.out.println(Server.MAX_BUFFER_SIZE);

                        if(Server.CURRENT_BUFFER_SIZE<Server.MAX_BUFFER_SIZE)
                        {
                            Permi = "Yes";
                        }

                        out.writeUTF(CHUNK_SIZE +" "+ String.valueOf(Server.FileNoS)+" "+Permi);
                        out.flush();

                        if(Permi.equalsIgnoreCase("Yes"))
                        {

                            fName = Server.dirName+"/"+id;
                            File theDir = new File(Server.dirName+"/"+id);
                            if (!theDir.exists()){
                                theDir.mkdirs();
                            }

                            fn= Server.dirName+"/"+id+"/"+String.valueOf(Server.FileNoS)+"-"+fn;
                            System.out.println("Modified File Name = "+fn);

                            if(pRun)
                            {
                                ServerSocket welcomeSocket2 = new ServerSocket(6667);

                                //while(!welcomeSocket2.isClosed()) {
                                System.out.println("\nWaiting for connection ^_^");
                                Socket socket2 = welcomeSocket2.accept();
                                System.out.println("Connection established SUCCESSFULLY");

                                System.out.println("Clinet  Port no = "+socket.getPort());
                                System.out.println("Clinet  IP no = "+socket.getInetAddress());

                                // open thread
                                Thread worker2 = new Worker2(socket2,fn,fileSIze,CHUNK_SIZE);
                                worker2.start();

                            }
                            else
                            {
                                saveFile(in,out,fn,fileSIze);
                            }


                            //}
                            //saveFile(in,out,fn,fileSIze);
                            Server.CURRENT_BUFFER_SIZE -=fileSIze;





                            System.out.println("File Recieving Precess done Successfully..");
                            //in = new ObjectInputStream(this.socket.getInputStream());

                            if (words[0].equals("upreqfile")) {
                                String reqId = words[4];

                                for (int i = 0; i < Server.LogMsg.get(id).size(); i++) {

                                    if (Server.LogMsg.get(id).get(i).startsWith(reqId)) {
                                        Server.LogMsg.get(id).remove(i);
                                        //System.out.println(Server.LogMsg.get(id));
                                    }

                                }

                                System.out.println("Req " + reqId + " have been full filled and remove from " + id + " 's Queue ");

                                String[] result = reqId.split("\\-");
                                String r = reqId;

                                int idnoti = Integer.parseInt(result[0]);

                                //System.out.println(result[0]+ " "+result[1]);

                                Server.LogMsg.get(idnoti).add("## Your req no " + r + " fullfilled by id no " + id + " \n");
                            }


                            FileInfo tf = new FileInfo();
                            tf.FilenNo = Server.FileNoS;
                            Server.FileNoS++;
                            tf.FileName = fn;
                            if (words[3].equalsIgnoreCase("Private")) {
                                tf.PublicStatus = false;
                            } else {
                                tf.PublicStatus = true;
                            }
                            tf.FileSize = Integer.parseInt(words[2]);

                            Server.LogFile.get(id).add(tf);

                        }

                        FIleFlagUP = false;
                        //System.out.println(Server.LogFile.get(id).firstElement().FileName);

                        continue;
                    }
                    if (words[0].equals("ID")) {

                        System.out.println("Client no : "+n+
                                " and ID no : " + words[1]);

                        System.out.println("Client socket no : "+socket.getRemoteSocketAddress());

                        id =Integer.parseInt(words[1]);

                        for (int i : Server.LogOnOf.keySet()) {

                            if(id == i && Server.LogOnOf.get(i))
                            {
                                out.writeUTF("Deny");
                                out.close();

                                try {
                                    this.socket.close();
                                } catch (Throwable ignored) {}

                                break;
                            }
                        }

                        out.writeUTF("OK");

                        Server.LogOnOf.put(id,true);
                        Server.LogFile.put(id, new Vector<FileInfo>()) ;
                        Server.LogMsg.put(id, new ArrayList<String>()) ;

                        File theDir = new File(Server.dirName+"/"+id);
                        if (!theDir.exists()){
                            theDir.mkdirs();
                        }

                        /*FileInfo filee = new FileInfo();
                        filee.FileName="Bingooooo";
                        filee.FilenNo=1;
                        filee.PublicStatus=true;
                        filee.FileSize=420;
                        Server.LogFile.get(id).add(filee);*/
                    }
                    else
                    {
                        System.out.println("Client no "+n+"" +
                                " [ "+ id + "] : " + messageFromClient);
                    }

                    //Thread.sleep(1000);

                } catch (IOException e) {
                    System.out.println("Client "+id+" disconnected thus that Socket closing...");
                    Server.LogOnOf.put(id,false);

                    if(FIleFlagUP)
                    {
                        File theDir = new File(fName);
                        if (!theDir.exists()){
                            theDir.mkdirs();
                        }
                    }

                    try {
                        this.socket.close();
                    } catch (Throwable ignored) {}
                    break;
                }
            }

            in.close();
            out.close();



        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static  void saveFile(DataInputStream dis ,DataOutputStream dos,String FileName,int fSize) throws IOException {
        //DataInputStream dis = new DataInputStream(clientSock.getInputStream());
        FileOutputStream fos = new FileOutputStream(FileName);
        byte[] buffer = new byte[CHUNK_SIZE];
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

            byte[] buffer = new byte[MAX_CHUNK_SIZE];
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
