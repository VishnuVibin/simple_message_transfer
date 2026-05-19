import java.net.*;
import java.io.*;
import java.util.*;
public class client{
    public static void main(String[] args){
        try{
            
            Scanner sc=new Scanner(System.in);
            Socket s=new Socket("localHost",7000);
            System.out.println("Server connected");
            DataOutputStream out=new DataOutputStream(s.getOutputStream());
            DataInputStream in=new DataInputStream(s.getInputStream());
            while(true){
                String name=sc.nextLine();
                if(name.equals("exit")){
                    break;
                }
                else{
                    out.writeUTF(name);
                    out.flush();
                    String reply=in.readUTF();
                    System.out.println("theServer says:"+reply);
                }
                
            }
        }
        catch(Exception e){
            System.out.println("Error");
        }
    }
}