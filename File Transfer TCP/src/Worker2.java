import java.io.*;
import java.net.Socket;
import java.text.DecimalFormat;

public class Worker2 extends Thread {


        Socket socket;
        String FileName ;
        int fileLen ;
        int cSize ;

    public Worker2(Socket socket, String fileName, int fileLen,int cSize) {
        this.socket = socket;
        this.FileName = fileName;
        this.fileLen = fileLen;
        this.cSize= cSize;
    }
        @Override
        public void run() {
            try {

                DataOutputStream dos2 = new DataOutputStream(this.socket.getOutputStream());
                DataInputStream dis2 = new DataInputStream(this.socket.getInputStream());

                FileOutputStream fos = new FileOutputStream(FileName);
                byte[] buffer = new byte[cSize];
                final DecimalFormat df = new DecimalFormat("0.00");
                int filesize = fileLen;
                //int filesize = 1331220 ;

                int read = 0;
                int cno= 1;
                int totalRead = 0;
                int remaining = filesize;
                double p ;
                //while((read = dis.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
                //while((read = dis.read(buffer)) > 0) {
                while (true){
                    read = dis2.read(buffer, 0, Math.min(buffer.length, remaining));
                    totalRead += read;
                    remaining -= read;

                    p = (1.0*totalRead/filesize)*100;

                    //System.out.println("read " +cno+" no chunk , total "+ totalRead + " bytes sent , "+ df.format(p)+"%");
                    cno++;

                    fos.write(buffer, 0, read);

                    if(p==100)
                    {
                        dos2.writeUTF("complete");
                        System.out.println("File recived Successfully .. ");
                        break;
                    }
                    else
                    {
                        dos2.writeUTF("got");
                    }
                }

                String msg = null;
                while (msg==null){
                    msg = dis2.readUTF();
                }

                if(msg.equalsIgnoreCase("Complete"))
                {
                    System.out.println("FIle Uploaded , All byte recieved ");

                    if(totalRead == filesize)
                    {
                        dos2.writeUTF("complete");
                    }
                    else
                    {
                        dos2.writeUTF("notcomplete");

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
                dos2.close();
                dis2.close();

                try {
                    this.socket.close();
                } catch (Throwable ignored) {}


            } catch (IOException e) {
                e.printStackTrace();
            }
        }


}
