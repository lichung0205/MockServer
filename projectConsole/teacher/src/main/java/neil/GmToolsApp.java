package neil;

import java.io.*;
import java.net.*;
import java.util.*;
import communication.LoginInfo;
import communication.Message;
import enums.AuthType;

public class GmToolsApp {
    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 9527;
    private static Socket socket;
    private static PrintWriter out;
    private static BufferedReader in;
    private static volatile boolean running = true;

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            initializeConnection();

            // 啟動接收伺服器消息的執行緒
            Thread receiverThread = new Thread(() -> {
                try {
                    String serverMessage;
                    while (running && (serverMessage = in.readLine()) != null) {
                        System.out.println("\n[伺服器回應] " + serverMessage);
                    }
                } catch (IOException e) {
                    if (running) {
                        System.out.println("\n與伺服器連線中斷: " + e.getMessage());
                    }
                } finally {
                    running = false;
                }
            });
            receiverThread.setDaemon(true);  // 設為守護執行緒，隨主執行緒退出
            receiverThread.start();

            // 主循環處理用戶輸入
            while (running) {
                showMenu(scanner);
            }

        } catch (Exception e) {
            System.err.println("程式錯誤: " + e.getMessage());
        } finally {
            closeConnection();
            System.out.println("應用程式已關閉");
        }
    }

    private static void initializeConnection() throws IOException {
        socket = new Socket(SERVER_IP, SERVER_PORT);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        // 身份驗證
        LoginInfo info = new LoginInfo(AuthType.TEACHER, "neil", "尼奧");
        out.println(info.toJson());
        System.out.println("已連接至教室 (輸入 'q' 退出)");
    }

    private static void closeConnection() {
        running = false;
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.err.println("關閉連線時出錯: " + e.getMessage());
        }
    }

    private static void doAction(Message message) {
        if (out != null && !socket.isClosed()) {
            out.println(message.toJson());
        } else {
            System.out.println("錯誤: 連線已中斷");
        }
    }

    private static void showMenu(Scanner scanner) {
        System.out.println("\n===== 功能選單 =====");
        System.out.println("1. 廣播訊息");
        System.out.println("2. 統計人數");
        System.out.println("3. 尋找學員");
        System.out.println("4. 留言給學員");
        System.out.println("5. 教室清場");
        System.out.println("q. 離開");
        System.out.print("請選擇: ");

        String choice = scanner.nextLine().trim().toLowerCase();
        String target, content;

        try {
            switch (choice) {
                case "1":
                    System.out.print("請輸入廣播訊息: ");
                    content = getNonEmptyInput(scanner);
                    doAction(new Message("broadcast", "", content));
                    break;
                case "2":
                    doAction(new Message("count", "", ""));
                    break;
                case "3":
                    System.out.print("請輸入學員代號: ");
                    target = getNonEmptyInput(scanner);
                    doAction(new Message("find", target, ""));
                    break;
                case "4":
                    System.out.print("請輸入學員代號: ");
                    target = getNonEmptyInput(scanner);
                    System.out.print("請輸入留言內容: ");
                    content = getNonEmptyInput(scanner);
                    doAction(new Message("memo", target, content));
                    break;
                case "5":
                    doAction(new Message("clearroom", "", ""));
                    break;
                case "q":
                    doAction(new Message("quit", "", ""));
                    running = false;
                    break;
                default:
                    System.out.println("無效選擇，請重新輸入");
            }
        } catch (Exception e) {
            System.out.println("操作失敗: " + e.getMessage());
        }
    }

    private static String getNonEmptyInput(Scanner scanner) {
        String input;
        while ((input = scanner.nextLine().trim()).isEmpty()) {
            System.out.print("輸入不能為空，請重新輸入: ");
        }
        return input;
    }
}