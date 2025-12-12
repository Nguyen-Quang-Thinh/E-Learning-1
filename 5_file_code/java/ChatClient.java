import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class ChatClient {
    private static final String HOST = "127.0.0.1";
    private static final int PORT = 9999;

    public static void main(String[] args) {
        try {
            Socket socket = new Socket(HOST, PORT);
            // Tối ưu hóa Client
            socket.setTcpNoDelay(true);

            System.out.println("==> Da ket noi toi Server Java NIO!");
            System.out.println("==> Go tin nhan va Enter de gui (Go 'exit' de thoat)");

            // --- LUỒNG 1: NHẬN TIN NHẮN TỪ SERVER ---
            Thread readThread = new Thread(() -> {
                try {
                    InputStream input = socket.getInputStream();
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = input.read(buffer)) != -1) {
                        String msg = new String(buffer, 0, bytesRead);
                        System.out.print(msg);
                    }
                } catch (IOException e) {
                    System.out.println("\n[Ngat ket noi voi Server]");
                }
            });
            readThread.start();

            // --- LUỒNG 2 (CHÍNH): GỬI TIN NHẮN ---
            OutputStream output = socket.getOutputStream();
            Scanner scanner = new Scanner(System.in);
            
            while (true) {
                String msg = scanner.nextLine();
                if ("exit".equalsIgnoreCase(msg)) {
                    break;
                }
                output.write(msg.getBytes());
                output.flush();
            }

            socket.close();
            System.exit(0);

        } catch (IOException e) {
            System.out.println("Khong the ket noi toi Server: " + e.getMessage());
        }
    }
}