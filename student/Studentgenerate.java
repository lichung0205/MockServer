package student;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

// Studentgenerate 現在包含 Student 類別的定義
public class Studentgenerate {

    // 嵌套靜態類別來表示學生
    static class Student {
        private int id;
        private String name;
        private int attendanceStatus; // 0 代表未到, 1 代表簽到
        private int activityChoice;   // 儲存簽到學生隨機選擇的活動狀態

        // 建構子
        public Student(int id) {
            this.id = id;
            this.name = id + "號同學";

            // Step 1: 模擬簽到或未到 (10% 未到機率)
            Random random = new Random();
            int attendanceRoll = random.nextInt(10); // 生成 0 到 9 的隨機數

            if (attendanceRoll == 0) { // 0 的機率是 1/10，即 10%
                this.attendanceStatus = 0; // 未到
                this.activityChoice = -1; // 未到的學生沒有活動選擇
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

        public void displayFullStatus() {
            System.out.print(name);
            if (attendanceStatus == 0) {
                System.out.println(" 狀態：未到");
            } else { // 簽到 (attendanceStatus == 1)
                System.out.print(" 狀態：簽到，選擇了：");
                switch (activityChoice) {
                    case 1:
                        System.out.println("喝水");
                        break;
                    case 2:
                        System.out.println("滑手機");
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

        for (int i = 1; i <= numberOfStudents; i++) {
            Student student = new Student(i);
            students.add(student);
        }

        System.out.println("--- 學生簽到與狀態報告 (本地模擬) ---");
        for (Student student : students) {
            student.displayFullStatus(); // 會顯示詳細活動狀態
        }
        System.out.println("\n--- 開始連接伺服器並發送學生資料 ---");

        List<Thread> clientThreads = new ArrayList<>();
        for (Student student : students) {
            // StudentClient 仍然只傳遞姓名和簽到狀態，不含活動
            StudentClient client = new StudentClient(
                student.getName(),
                student.getAttendanceStatus(),
                student.getActivityChoice() 
            );
            Thread clientThread = new Thread(client);
            clientThreads.add(clientThread);
            clientThread.start();

            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("啟動客戶端時被中斷。");
            }
        }

        for (Thread thread : clientThreads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("等待客戶端執行緒時被中斷。");
            }
        }

        System.out.println("\n--- 所有學生資料已處理或嘗試發送完畢 ---");
    }
}