package student;

import java.io.*;
import java.net.*;
import java.util.concurrent.TimeUnit;

public class StudentClient implements Runnable {

    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 9527;

    private String clientStudentName;
    private int clientAttendanceStatus; // 0 代表未到, 1 代表簽到
    private int clientActivityChoice;   // 新增：儲存活動選擇

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private volatile boolean running = true;

    // 建構子：現在接收學生姓名、簽到狀態和活動選擇
    public StudentClient(String studentName, int attendanceStatus, int activityChoice) {
        this.clientStudentName = studentName;
        this.clientAttendanceStatus = attendanceStatus;
        this.clientActivityChoice = activityChoice;
    }

    @Override
    public void run() {
        try {
            initializeConnection();

            sendIdentityToServer(); // 先發送身份

            // 如果學生已簽到，才發送狀態和活動
            if (clientAttendanceStatus == 1) {
                sendAttendanceStatusToServer(clientAttendanceStatus); // 發送簽到狀態 (例如 "到")
                sendActivityChoiceToServer(clientActivityChoice);     // 發送活動選擇 (例如 "喝水")
            } else {
                // 如果學生未到，只發送未到狀態
                sendAttendanceStatusToServer(clientAttendanceStatus); // 發送簽到狀態 (例如 "未到")
            }


            new Thread(() -> {
                try {
                    String serverMessage;
                    while (running && (serverMessage = in.readLine()) != null) {
                        System.out.println(clientStudentName + " 收到伺服器消息: " + serverMessage);
                    }
                } catch (IOException e) {
                    if (running) {
                        System.err.println(clientStudentName + " 伺服器消息接收中斷: " + e.getMessage());
                    }
                } finally {
                    running = false;
                }
            }).start();

            // 客戶端等待一段時間，讓伺服器有機會回覆，然後結束連線
            TimeUnit.SECONDS.sleep(3);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println(clientStudentName + " 客戶端主執行緒被中斷。");
        } catch (Exception e) {
            System.err.println(clientStudentName + " 客戶端應用程式發生錯誤: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeConnection();
            System.out.println(clientStudentName + " 客戶端連線已關閉。");
        }
    }

    private void initializeConnection() throws IOException {
        System.out.println(clientStudentName + " 嘗試連接伺服器...");
        try {
            socket = new Socket(SERVER_IP, SERVER_PORT);
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            System.out.println(clientStudentName + " 已連接到伺服器: " + SERVER_IP + ":" + SERVER_PORT);
        } catch (IOException e) {
            System.err.println(clientStudentName + " 連接伺服器失敗: " + e.getMessage());
            running = false;
            throw e;
        }
    }

    private void sendIdentityToServer() {
        if (out != null) {
            out.println("STUDENT:" + clientStudentName);
            System.out.println(clientStudentName + " 已發送身份識別。");
        } else {
            System.err.println(clientStudentName + " 輸出流未初始化，無法發送身份。");
        }
    }

    // 發送簽到狀態
    private void sendAttendanceStatusToServer(int status) {
        if (out != null) {
            String statusString = (status == 1) ? "到" : "未到";
            out.println("ATTENDANCE:" + statusString);
            System.out.println(clientStudentName + " 已發送簽到狀態: " + statusString);
        } else {
            System.err.println(clientStudentName + " 輸出流未初始化，無法發送簽到狀態。");
        }
    }

    // 發送活動選擇
    private void sendActivityChoiceToServer(int choice) {
        if (out != null) {
            String activityString;
            switch (choice) {
                case 1: activityString = "喝水"; break;
                case 2: activityString = "滑手機"; break;
                case 3: activityString = "發呆"; break;
                default: activityString = "未知"; break;
            }
            out.println("ACTIVITY:" + activityString);
            System.out.println(clientStudentName + " 已發送活動選擇: " + activityString);
        } else {
            System.err.println(clientStudentName + " 輸出流未初始化，無法發送活動選擇。");
        }
    }

    private void closeConnection() {
        running = false;
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