package neil;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.Scanner;

import communication.LoginInfo;
import communication.Message;
import enums.AuthType;

public class GmToolsApp {

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
            receiverThread.setDaemon(true); // 設為守護執行緒，隨主執行緒退出
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

        Properties props = new Properties();

        // 預設設定檔路徑（可以改成參數傳入）
        String configPath = "./config/teacher.properties";

        try (InputStreamReader reader = new InputStreamReader(
                new FileInputStream(configPath), StandardCharsets.UTF_8)) {
            props.load(reader);
        } catch (IOException e) {
            System.err.println("讀取設定檔失敗：" + configPath + "錯誤：" + e.getMessage());
            throw e;
        }

        // 讀取 server.port
        String serverip = props.getProperty("server.ip", "100.0.0.1");
        String serverport = props.getProperty("server.port", "9527");
        int port = Integer.parseInt(serverport);

        // 讀取 server.gm 並轉小寫，方便比對
        String myid = props.getProperty("login.id", "jack");
        String mynam = props.getProperty("login.name", "jack");

        // 測試輸出
        socket = new Socket(serverip, port);
        out = new PrintWriter(
                new OutputStreamWriter(socket.getOutputStream(), java.nio.charset.StandardCharsets.UTF_8),
                true);
        in = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), java.nio.charset.StandardCharsets.UTF_8));

        // 身份驗證
        LoginInfo info = new LoginInfo(AuthType.TEACHER, myid, mynam);
        out.println(info.toJson());
        System.out.println("已連接至教室 (輸入 'q' 退出)");
    }

    private static void closeConnection() {
        running = false;
        try {
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
            if (socket != null) {
                socket.close();
            }
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
        System.out.println("2. 教室資訊");
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
