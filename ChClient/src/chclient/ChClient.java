package chclient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class ChClient {
final Socket sock;  
    final BufferedReader input; 
    final BufferedWriter output; 
    final BufferedReader userInput; 
    public ChClient(String host, int port) throws IOException {
        sock = new Socket(host, port); // создаем сокет
        input = new BufferedReader(new InputStreamReader(sock.getInputStream()));
        output = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
        userInput = new BufferedReader(new InputStreamReader(System.in));
        new Thread(new Receiver()).start();// поток,отчечающий за прием сообщений
    }
 
    public void run() {
        System.out.println("Можете вводить сообщения (для выхода нажмите Enter):");
        while (sock.isConnected()) {
            String userString = null;
            try {
                userString = userInput.readLine(); // читаем строку от пользователя
            } catch (IOException ignored) {} 
            if (userString == null || userString.length() == 0) {
                close(); // закрываем соединение
                break; 
            } else { 
                try {
                    output.write(userString); 
                    output.write("\n"); //добавляем "новою строку",чтобы readLine() сервера сработал
                    output.flush(); // отправляем
                } catch (IOException e) {
                    close(); // в любой ошибке - закрываем.
                }
            }
        }
    }
 
    public synchronized void close() {//метод синхронизирован, чтобы исключить двойное закрытие.
        if (!sock.isClosed()) {
            try {
                sock.close(); // закрываем
                System.exit(0); // выходим
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }
    public static void main(String[] args) {
        try {
            new ChClient("localhost", 2000).run(); 
        } catch (IOException e) { 
            System.out.println("Нет соединения. Возможно сервер не включен?"); 
        }
    }
    private class Receiver implements Runnable{
        public void run() {
            while (!sock.isClosed()) { 
                String message = null;
                try {
                    message = input.readLine(); 
                } catch (IOException e) { 
                    System.out.println("Потеря соединения"); 
                    close(); 
                }
                System.out.println("Server: " + message);
            }
        }
    }
}

