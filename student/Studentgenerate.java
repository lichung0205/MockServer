package student;

import java.util.ArrayList;
import java.util.Collections; // 新增導入，用於隨機打亂列表
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit; // 新增導入，用於時間延遲

public class Studentgenerate {

    // Student 內部類別保持不變
    static class Student {
        private int id;
        private String name;
        private int attendanceStatus; // 0 代表未到, 1 代表簽到
        private int activityChoice;   // 儲存簽到學生隨機選擇的活動狀態

        public Student(int id) {
            this.id = id;
            this.name = id + "號同學";

            Random random = new Random();
            int attendanceRoll = random.nextInt(10); // 10% 未到機率

            if (attendanceRoll == 0) { 
                this.attendanceStatus = 0; // 未到
                this.activityChoice = -1; // 未到的學生沒有活動選擇
            } else {
                this.attendanceStatus = 1; // 簽到
                this.activityChoice = random.nextInt(3) + 1; // 1, 2, 3 之間的隨機數
            }
        }

        public String getName() {
            return name;
        }

        public int getAttendanceStatus() {
            return attendanceStatus;
        }

        public int getActivityChoice() {
            return activityChoice;
        }

        public void displayFullStatus() {
            System.out.print(name);
            if (attendanceStatus == 0) {
                System.out.println(" 狀態：未到");
            } else {
                System.out.print(" 狀態：簽到，選擇了：");
                switch (activityChoice) {
                    case 1: System.out.println("喝水"); break;
                    case 2: System.out.println("滑手機"); break;
                    case 3: System.out.println("發呆"); break;
                    default: System.out.println("無效的活動選擇。");
                }
            }
        }
    }

    public static void main(String[] args) {
        List<Student> students = new ArrayList<>();
        int numberOfStudents = 30; // 學生總數

        for (int i = 1; i <= numberOfStudents; i++) {
            Student student = new Student(i);
            students.add(student);
        }

        System.out.println("--- 學生簽到與狀態報告 (本地模擬，隨機打亂前) ---");
        for (Student student : students) {
            student.displayFullStatus();
        }
        System.out.println("\n--- 開始連接伺服器並發送學生資料 (隨機順序，隨機延遲) ---");

        // 核心修改：將學生列表隨機打亂，模擬學生隨機進入教室的順序
        Collections.shuffle(students); 

        // 由於我們暫時不實現自動簽退，客戶端將持續運行，因此不需要收集和等待 clientThreads
        // List<Thread> clientThreads = new ArrayList<>(); 
        Random randomDelayGenerator = new Random(); // 獨立的隨機數生成器用於延遲

        for (Student student : students) {
            // 為每個學生創建並啟動一個 StudentClient 執行緒
            StudentClient client = new StudentClient(
                student.getName(),
                student.getAttendanceStatus(),
                student.getActivityChoice() 
            );
            Thread clientThread = new Thread(client);
            // clientThreads.add(clientThread); // 暫時不需要收集執行緒
            clientThread.start();

            try {
                // 在每個客戶端啟動之間，隨機延遲 0.1 到 1.5 秒
                // 模擬學生在不同時間點進入教室並開始連接
                int delayMillis = 100 + randomDelayGenerator.nextInt(1401); // 100ms 到 1500ms
                TimeUnit.MILLISECONDS.sleep(delayMillis);
            } catch (InterruptedException e) {
                // 處理執行緒中斷異常
                Thread.currentThread().interrupt();
                System.err.println("啟動客戶端時被中斷。");
            }
        }

        System.out.println("\n--- 所有學生客戶端已啟動。它們將保持連線直到伺服器關閉或手動停止客戶端 ---");
    }
}