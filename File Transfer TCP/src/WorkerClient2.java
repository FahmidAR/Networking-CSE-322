import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.text.DecimalFormat;

public class WorkerClient2 extends Thread {


    Socket socket;
    String FileName ;
    int fileLen ;
    int cSize ;

    public WorkerClient2(Socket socket, String fileName, int fileLen,int cSize) {
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

            Boolean flagTimout = false ;
            final DecimalFormat df = new DecimalFormat("0.00");
            double p ;

            try {
                FileInputStream fis = new FileInputStream(FileName);
                socket.setSoTimeout(30000);

                byte[] buffer = new byte[cSize];
                int no = 1;

                int read = 0;
                int totalRead = 0;
                int remaining = fileLen ;

                //while (fis.read(buffer) > 0) {
                //while((read = fis.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
                while (true){
                    //dos.write(buffer);
                    read = fis.read(buffer, 0, Math.min(buffer.length, remaining));
                    dos2.write(buffer, 0, Math.min(buffer.length, remaining));

                    totalRead += read;
                    remaining -= read;

                    p = (1.0*totalRead/fileLen)*100;

                    String s = null;
                    while (s==null) {
                        s= dis2.readUTF();
                    }

                    if (s.equalsIgnoreCase("got")) {
                        //System.out.println("achknowledge recived for " + no + " no chunk , Progress = "+df.format(p)+"%");
                    } else if (s.equalsIgnoreCase("complete")) {
                        //System.out.println("achknowledge recived for " + no + " no FINAL chunk, Progress = "+df.format(p)+"%");
                        dos2.writeUTF("Complete");

                        String ss = null;
                        while (ss==null) {
                            ss= dis2.readUTF();
                        }

                        if(ss.equalsIgnoreCase("complete"))
                        {
                           System.out.println("\nFile Bytes exchanged Successfuly");
                        }
                        else
                        {
                            System.out.println("\nFile Bytes exchanged Not Successfuly");
                        }

                        break;
                    }
                    no++;
                    dos2.flush();
                }

                dos2.flush();
                fis.close();
                //dos.close();
            }
            catch (SocketTimeoutException e)
            {
                System.out.println("Time out ...");
                flagTimout = true ;
                dos2.writeUTF("TimeOut");
                dos2.flush();
            }

        /*if(!flagTimout)
        {
            dos.writeUTF("NotTimeOut");
            dos.flush();
        }*/


            dos2.close();
            dis2.close();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
