package server;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class Server extends Thread
{
	private static final int port  = 2000; // открываемый порт сервера
        private String TEMPL_MSG  = "The client '%d' sent me message : \n\t";
	private String TEMPL_CONN = "The client '%d' closed the connection";

	private  Socket socket;
	private  int    num;
	public static List<Server> Clients=new ArrayList<>();
        BufferedReader dis;
        PrintWriter dos;
	public Server(Socket s) throws IOException {
            this.socket=s;
            // Определяем входной и выходной потоки сокета
            // для обмена данными с клиентом 
            InputStream  sin  = socket.getInputStream();
            OutputStream sout = socket.getOutputStream();

            dis = new BufferedReader(new InputStreamReader(sin));;
            dos = new PrintWriter(sout);
        }
	public void setNum(int num)
	{
            // Определение значений
            this.num    = num;
	}
        public int getNum() {
            return this.num;
        }
	public void run()
	{
            int n = 0;//номер клиента, отправившего сокет
		try {
			String line = null;
			while(true) {
				// Ожидание сообщения от клиента
                                line = dis.readLine();
				System.out.println(String.format(TEMPL_MSG, num) + line);
				if (line.equalsIgnoreCase("quit")) {
					// завершаем соединение
					socket.close();
					System.out.println(String.format(TEMPL_CONN, num));
					break;
				}
                                for(Server c: Clients) {
                                    if (c.socket.equals(socket))
                                        n=c.getNum();
                                }
                                for(Server c: Clients) {
                                    if (!(c.socket.equals(socket)))//не отсылаем сообщение самому себе
                                    {
                                        c.dos.println("Client"+n+": "+line);
                                        c.dos.flush();
                                    }
                                }
			}
		} catch(Exception e) {
			System.out.println("Exception : " + e);
	    }
	}
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public static void main(String[] ar)
	{
		ServerSocket srvSocket = null;
		try {
			try {
				int i = 0; // Счётчик подключений
				// Подключение сокета к localhost
				srvSocket = new ServerSocket(port);
	
				System.out.println("Server started\n\n");
	
				while(true) {
					// ожидание подключения
					Socket socket = srvSocket.accept();
					System.err.println("Client accepted");
					// Стартуем обработку клиента в отдельном потоке
                                        Server client=new Server(socket);
                                        client.setNum(i++);
					Clients.add(client);
                                        client.start();
				}
			} catch(Exception e) {
				System.out.println("Exception : " + e);
			}
		} finally {
			try {
				if (srvSocket != null)
					srvSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.exit(0);
	}
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
}
