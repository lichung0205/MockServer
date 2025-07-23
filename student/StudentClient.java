package student;
// 引入 IOException

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
                // showMenu(scanner);ccccccccccccccccccccccccccccccccccccccccccccccccccc
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

    public class SchoolSimulation {

    // 如果 out, in, socket 是全局的，它們需要在這裡聲明
    // private static PrintWriter out;
    // private static BufferedReader in;
    // private static Socket socket;

    // 將 Student 定義為 SchoolSimulation 的靜態內部類別，以便在同一個檔案中
    static class Student {
        private int id;
        private String name;
        private int attendanceStatus; // 0 代表未到, 1 代表簽到
        private int activityChoice;   // 儲存簽到學生隨機選擇的活動狀態 (1, 2, 3)

        // 建構子
        public Student(int id) {
            this.id = id;
            this.name = id + "號同學";

            // Step 1: 模擬簽到或未到 (10% 未到機率)
            Random random = new Random();
            int attendanceRoll = random.nextInt(10); // 生成 0 到 9 的隨機數

            if (attendanceRoll == 0) { // 0 的機率是 1/10，即 10%
                this.attendanceStatus = 0; // 未到
                this.activityChoice = -1; // 未到的學生沒有活動選擇，設為 -1 或其他標記值
            } else {
                this.attendanceStatus = 1; // 簽到
                // Step 2: 如果簽到，才有後續的活動選擇 (1, 2, 3)
                this.activityChoice = random.nextInt(3) + 1; // 生成 1, 2, 3 之間的隨機數
            }
        }

        // 取得學生姓名
        public String getName() {
            return name;
        }

        // 取得簽到狀態
        public int getAttendanceStatus() {
            return attendanceStatus;
        }

        // 取得活動選擇
        public int getActivityChoice() {
            return activityChoice;
        }

        // 顯示學生完整狀態的方法
        public void displayFullStatus() {
            System.out.print(name);
            if (attendanceStatus == 0) {
                System.out.println(" 狀態：未到");
            } else { // 簽到 (attendanceStatus == 1)
                System.out.print(" 狀態：簽到，選擇了：");
                switch (activityChoice) {
                    case 1:
                        System.out.println("喝水");
                        // 如果 out 是全局的，並且已經初始化，可以在這裡使用
                        // if (out != null) out.println("REQUEST_DRINK");
                        break;
                    case 2:
                        System.out.println("滑手機");
                        // if (out != null) out.println("REQUEST_PHONE");
                        break;
                    case 3:
                        System.out.println("發呆");
                        break;
                    default:
                        System.out.println("無效的活動選擇。");
                }
            }
        }
    } // Student 類別結束

    public static void main(String[] args) {
        List<Student> students = new ArrayList<>();
        int numberOfStudents = 30;

        // 生成 30 名學生並初始化他們的簽到和活動狀態
        for (int i = 1; i <= numberOfStudents; i++) {
            Student student = new Student(i);
            students.add(student);
        }

        System.out.println("--- 學生簽到與活動狀態報告 ---");
        // 遍歷所有學生，顯示他們的完整狀態
        for (Student student : students) {
            student.displayFullStatus();
        }

        // 您可以在這裡添加額外的統計或處理邏輯
        // 例如：計算簽到人數、每種活動的參與人數等

        // 如果有網路連線，可以在這裡處理關閉連線的邏輯
        // try {
        //     if (out != null) out.close();
        //     if (in != null) in.close();
        //     if (socket != null) socket.close();
        // } catch (IOException e) {
        //     System.err.println("關閉連線時出錯: " + e.getMessage());
        // }
    }
}
}
