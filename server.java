import java.net.*;
import java.io.*;
import java.util.*;
public class server{
    public static void main(String[] args){
        try{
            Scanner sc=new Scanner(System.in);
            ServerSocket server=new ServerSocket(7000);
            System.out.println("Server waiting....");
            Socket s=server.accept();
            System.out.println("connetion is done");
            DataInputStream in=new DataInputStream(s.getInputStream());
            DataOutputStream out=new DataOutputStream(s.getOutputStream());
            while(true){
                String mes=in.readUTF();
                System.out.println("the message is:"+mes);
                String reply=sc.nextLine();
                out.writeUTF(reply);
                out.flush();

            }
        }
        catch(Exception e){
            System.out.println("Error");
        }
    }
}