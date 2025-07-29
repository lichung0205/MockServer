package student;

import common.AuthType;
import common.LoginInfo;
import java.io.*; // 仍需保留，因為接收執行緒可能會用到短暫休眠來避免忙等
import java.net.*;
import java.util.concurrent.TimeUnit;

public class StudentClient implements Runnable {

    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 9527;

    private String id;
    private String name;
    private String clientStudentName;
    private int clientAttendanceStatus; // 0 代表未到, 1 代表簽到
    private int clientActivityChoice; // 儲存活動選擇

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private volatile boolean running = true; // 控制客戶端主執行緒的運行

    // 建構子
    public StudentClient(String id, String name, int attendanceStatus, int activityChoice) {
        this.id = id;
        this.name = name;
        this.clientAttendanceStatus = attendanceStatus;
        this.clientActivityChoice = activityChoice;

        clientStudentName = String.format("%s %s", id, name);
        System.out.println(clientStudentName + " 初始化。");
    }

    @Override
    public void run() {
        try {
            initializeConnection();
            sendIdentityToServer(); // 先發送身份

            if (clientAttendanceStatus == 1) {
                sendAttendanceStatusToServer(clientAttendanceStatus); // 發送簽到狀態 (例如 "到")
                sendActivityChoiceToServer(clientActivityChoice); // 發送活動選擇 (例如 "喝水")
            } else {
                sendAttendanceStatusToServer(clientAttendanceStatus); // 發送簽到狀態 (例如 "未到")
                // 對於未到的學生，我們可以讓它短暫停留後自動關閉，或者等待伺服器明確指令。
                // 為了避免未到的學生無限期佔用資源，這裡暫時讓它發送狀態後短暫等待就關閉。
                // 如果您希望未到的學生也無限期等待伺服器關閉指令，請刪除以下兩行。
                TimeUnit.SECONDS.sleep(1);
                // running = false; // 未到的學生發送完狀態後自行結束
            }

            // 啟動一個單獨的執行緒來持續接收伺服器消息
            Thread receiverThread = new Thread(() -> {

                try {
                    String serverMessage;
                    while (running && (serverMessage = in.readLine()) != null) {
                        System.out.println(clientStudentName + " 收到伺服器消息: " + serverMessage);
                        // 這裡可以根據伺服器消息來觸發動作，例如簽退指令
                        if (serverMessage.trim().equalsIgnoreCase("QUIT_CLIENT") ||
                                serverMessage.trim().equalsIgnoreCase("SERVER_CLOSE")) {
                            System.out.println(clientStudentName + " 收到伺服器關閉指令，準備斷開。");
                            running = false; // 設置為 false，將導致主執行緒和接收執行緒停止
                            break; // 退出循環
                        }
                    }
                } catch (IOException e) {
                    // 當伺服器關閉連線時，這裡會拋出 IOException
                    if (running) { // 只有在仍然預期運行時才報告錯誤
                        System.err.println(clientStudentName + " 伺服器消息接收中斷: " + e.getMessage());
                    }
                } finally {
                    // running = false; // 無論是正常收到指令還是異常斷開，都停止運行
                }
            });
            receiverThread.start();

            // 主執行緒等待 `running` 變為 false。
            // 這樣客戶端會持續運行，直到接收到伺服器發送的關閉指令，
            // 或者底層 socket 因伺服器關閉或其他網路問題而斷開。
            while (running) {
                // 短暫休眠以避免佔用過多 CPU
                TimeUnit.MILLISECONDS.sleep(100);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println(clientStudentName + " 客戶端主執行緒被中斷。");
        } catch (Exception e) {
            System.err.println(clientStudentName + " 客戶端應用程式發生錯誤: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // closeConnection();
            System.out.println(clientStudentName + " 客戶端連線已關閉。");
        }
    }

    private void initializeConnection() throws IOException {

        // System.out.println(clientStudentName + " 嘗試連接伺服器...");
        try {
            socket = new Socket(SERVER_IP, SERVER_PORT);
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println(clientStudentName + " 已連接到伺服器: " + SERVER_IP + ":" + SERVER_PORT);
        } catch (IOException e) {
            System.err.println(clientStudentName + " 連接伺服器失敗: " + e.getMessage());
            running = false; // 連接失敗，停止運行
            throw e; // 重新拋出異常，讓外層 catch 捕獲並終止 run() 方法
        }
    }

    private void sendIdentityToServer() {
        if (out != null) {
            // out.println("STUDENT:" + clientStudentName);
            LoginInfo info = new LoginInfo(AuthType.STUDENT, id, name);
            out.println(info.toJson());
            // System.out.println(clientStudentName + " 已發送身份識別。");
        } else {
            System.err.println(clientStudentName + " 輸出流未初始化，無法發送身份。");
        }
    }

    private void sendAttendanceStatusToServer(int status) {
        if (out != null) {
            String statusString = (status == 1) ? "簽到" : "未到";
            out.println("ATTENDANCE:" + statusString);
            System.out.println(clientStudentName + " 已發送簽到狀態: " + statusString);
        } else {
            System.err.println(clientStudentName + " 輸出流未初始化，無法發送簽到狀態。");
        }
    }

    private void sendActivityChoiceToServer(int choice) {
        if (out != null) {
            String activityString = getActivityString(choice);
            out.println( activityString);
            System.out.println(clientStudentName + " 已發送活動選擇: " + activityString);
        } else {
            System.err.println(clientStudentName + " 輸出流未初始化，無法發送活動選擇。");
        }
    }

    // 輔助方法：根據活動數字獲取活動字串
    private String getActivityString(int choice) {
        switch (choice) {
            case 1:
                return "喝水";
            case 2:
                return "滑手機";
            case 3:
                return "發呆";
            default:
                return "未知";
        }
    }

    private void closeConnection() {
        running = false; // 確保設置為 false
        try {
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("關閉 " + clientStudentName + " 的連線時出錯: " + e.getMessage());
        }
    }
}