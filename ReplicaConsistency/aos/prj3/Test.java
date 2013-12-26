package aos.prj3;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class Test {

	  public static void main(String args[]) throws Exception {
		    Socket socket1;
		    int portNumber = 1777;
		    String str = "";

		    socket1 = new Socket(InetAddress.getLocalHost(), portNumber);

		    ObjectInputStream ois = new ObjectInputStream(socket1.getInputStream());

		    ObjectOutputStream oos = new ObjectOutputStream(socket1.getOutputStream());

		    str = "initialize";
		    oos.writeObject(str);

		    while ((str = (String) ois.readObject()) != null) {
		      System.out.println(str);
		      oos.writeObject("bye");

		      if (str.equals("bye bye"))
		        break;
		    }

		    ois.close();
		    oos.close();
		    socket1.close();
		  }
}
