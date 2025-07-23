package gmtools;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import common.Message;

public class GmToolsApp {
    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 9527;
    private static Socket socket;
    private static PrintWriter out;
    private static BufferedReader in;
    private static boolean running = true;

    public static void main(String[] args) {
        try {
            initializeConnection();
            AtomicLong speakTime = new AtomicLong(System.currentTimeMillis());

            // 啟動專門接收 server 消息的執行緒
            new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = in.readLine()) != null) {
                        // Server返回消息
                        System.out.println("\n" + serverMessage);
                        speakTime.set(System.currentTimeMillis()); // 安全地更新值
                    }
                } catch (IOException e) {
                    System.out.println("與伺服器連線中斷");
                }
            }).start();

            Scanner scanner = new Scanner(System.in);
            // 主迴圈：持續顯示選單
            while (running) {
                long diffTime = System.currentTimeMillis() - speakTime.get(); // 安全地讀取值
                // 三秒都沒回訊息才顯示菜單
                if (diffTime > 5000) {
                    showMenu(scanner);
                    speakTime.set(System.currentTimeMillis()); // 安全地更新值
                }
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
            out.println("TEACHER:Neil");
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

    // 統一由這來發送
    private static void doAction(Message message) {
        out.println(message.toJson());
    }

    // 顯示功能選單並依使用者輸入進行相對動作
    private static void showMenu(Scanner scanner) {

        System.out.println("\n===== 功能選單 =====");
        System.out.println("1. 廣播");
        System.out.println("2. 統計人數");
        System.out.println("3. 請同學來找老師");
        System.out.println("4. 留話給學員");
        System.out.println("q. 離開");
        System.out.print("請選擇功能: ");
        String target = "";
        String content = "";
        // 用輸入功能卡迴圈循環
        String choice = scanner.nextLine().trim().toLowerCase();
        switch (choice) {
            case "1":
                System.out.println("請輸入要廣播的訊息");
                content = scanner.nextLine().trim();
                doAction(new Message("broadcast", target, content));
                break;
            case "2":
                doAction(new Message("count", target, content));
                break;
            case "3":
                System.out.println("請輸入您要尋找的學員代號");
                target = scanner.nextLine().trim().toLowerCase();
                doAction(new Message("find", target, content));
                break;
            case "4":
                System.out.println("請輸入學員代號");
                target = scanner.nextLine().trim().toLowerCase();
                System.out.println("請輸入您想傳達的訊息");
                content = scanner.nextLine().trim();
                doAction(new Message("find", target, content));
                break;
            case "q":
                doAction(new Message("quit", target, content));
                break;
            default:
                System.out.println("無效的選擇，請重新輸入");
        }

    }
}