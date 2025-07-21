package student;

import java.io.*;
import java.net.*;
import java.util.*;

public class StudentClient {
    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 9527;
    private static final String studentName = "28號同學";
    private static Socket socket;
    private static PrintWriter out;
    private static BufferedReader in;
    private static boolean running = true;

    public static void main(String[] args) {
        try {
            initializeConnection();

            // 啟動專門接收 server 消息的執行緒
            new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = in.readLine()) != null) {
                        // Server返回消息
                        System.out.println("\n" + serverMessage);
                    }
                } catch (IOException e) {
                    System.out.println("與伺服器連線中斷");
                }
            }).start();

            Scanner scanner = new Scanner(System.in);
            // 主迴圈：持續顯示選單
            while (running) {
                showMenu(scanner);
            }

            // 離開前關閉連線
            closeConnection();
            scanner.close();
            System.out.println("應用程式已關閉");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 初始化與 server 的連線
    private static void initializeConnection() {
        try {
            socket = new Socket(SERVER_IP, SERVER_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // 送出身份識別
            out.println("STUDENT:" + studentName);
            System.out.println("已連接到伺服器: " + SERVER_IP + ":" + SERVER_PORT);
        } catch (IOException e) {
            System.err.println("連接伺服器失敗: " + e.getMessage());
            System.exit(1);
        }
    }

    // 關閉連線的方法
    private static void closeConnection() {
        try {
            if (out != null)
                out.close();
            if (in != null)
                in.close();
            if (socket != null)
                socket.close();
        } catch (IOException e) {
            System.err.println("關閉連線時出錯: " + e.getMessage());
        }
    }

    // 顯示功能選單並依使用者輸入進行相對動作
    private static void showMenu(Scanner scanner) {
        System.out.println("\n===== 功能選單 =====");
        System.out.println("1. 喝水");
        System.out.println("2. 滑手機");
        System.out.println("q. 離開");
        System.out.print("請選擇功能: ");
        String choice = scanner.nextLine().trim().toLowerCase();
        switch (choice) {
            case "1":
                out.println("REQUEST_DRINK");
                break;
            case "2":
                out.println("REQUEST_PHONE");
                break;
            case "q":
                out.println("QUIT");
                running = false;
                break;
            default:
                System.out.println("無效的選擇，請重新輸入");
        }
    }
}
