package jack;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.google.gson.Gson;

import communication.LoginInfo;
import communication.Message;
import enums.AuthType;

public class ClassroomServer {

    // private static final int PORT = 9527;
    private static ServerSocket serverSocket;
    private static final List<StudentHandler> students = new CopyOnWriteArrayList<>();
    private static final List<TeacherHandler> teachers = new CopyOnWriteArrayList<>();
    private static final Map<String, Object> memoryCache = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        try {

            Properties props = new Properties();

            // 預設設定檔路徑（可以改成參數傳入）
            String configPath = "./config/classroom.properties";

            try (FileInputStream fis = new FileInputStream(configPath)) {
                props.load(fis);
            } catch (IOException e) {
                System.err.println("讀取設定檔失敗：" + configPath + "錯誤：" + e.getMessage());
                return;
            }

            // 讀取 server.port
            String portStr = props.getProperty("server.port", "9527");
            int port = Integer.parseInt(portStr);

            // 讀取 server.gm 並轉小寫，方便比對
            String gmListStr = props.getProperty("server.gm", "").toLowerCase();
            List<String> allowedGms = Arrays.asList(gmListStr.split("\\s*,\\s*"));

            // 測試輸出
            System.out.println("允許的 GM 名稱: " + allowedGms);
            serverSocket = new ServerSocket(port);
            System.out.printf("教室伺服器 %s 已啟動..", portStr);
            // 如果需要，這邊可以設計 server 端的選單或教師功能
            while (true) {
                Socket clientSocket = serverSocket.accept();
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String loginData = in.readLine();
                LoginInfo info = LoginInfo.fromJson(loginData);
                String id = info.getId();
                String name = info.getName();
                // 學生登入 student
                if (info.getAuthType().equals(AuthType.STUDENT)) {
                    StudentHandler handler = new StudentHandler(clientSocket, id, name);
                    students.add(handler);
                    String studentKey = "s_" + info.getId();
                    if (!memoryCache.containsKey(studentKey)) {
                        memoryCache.put(studentKey, new StudentInfo(handler)); // 加入 cache

                    }
                    new Thread(handler).start();
                    System.out.printf("%s 號 %s 已進入教室\n", info.getId(), info.getName());
                } // 導師登入 gmtools
                else if (info.getAuthType().equals(AuthType.TEACHER)) {
                    if (!allowedGms.contains(id)) {
                        throw new RuntimeException(String.format("%s不是被允許的GM", id));
                    }

                    TeacherHandler handler = new TeacherHandler(clientSocket, id, name);
                    teachers.add(handler);
                    out.println(String.format("%s 老師您已登入伺服器", name));
                    new Thread(handler).start();
                    System.out.printf("%s 老師已進入教室\n", info.getName());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class StudentInfo {

        private final StudentHandler handler;
        private boolean checkedIn;
        private String lastActivity = "尚無行為紀錄";

        public StudentInfo(StudentHandler handler) {
            this.handler = handler;
            this.checkedIn = false;
        }

        public StudentHandler getHandler() {
            return handler;
        }

        public boolean isCheckedIn() {
            return checkedIn;
        }

        public void setCheckedIn(boolean checkedIn) {
            this.checkedIn = checkedIn;
        }

        public String getLastActivity() {
            return lastActivity;
        }

        public void setLastActivity(String lastActivity) {
            this.lastActivity = lastActivity;
        }

    }

    // 學生處理器
    static class StudentHandler implements Runnable {

        private final Socket socket;
        private final String id;
        private final String name;
        private PrintWriter out;
        private BufferedReader in;

        public StudentHandler(Socket socket, String id, String name) {
            this.socket = socket;
            this.id = id;
            this.name = name;
        }

        public void run() {
            String studentName = String.format("%s %s", id, name);
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                String showStr, input;
                while ((input = in.readLine()) != null) {
                    // 根據收到的請求做相對應的處理
                    if (input.equalsIgnoreCase("QUIT")) {
                        showStr = getShowString(studentName, "已離開教室");
                        System.out.println(showStr);
                        broadcastToTeachers(showStr);
                        memoryCache.remove("s_" + id);
                        break;
                    } else if (input.startsWith("ATTENDANCE")) {
                        String[] checkin = input.split(":");
                        if (checkin.length == 2) {
                            if (checkin[1].equalsIgnoreCase("Y")) {
                                String key = "s_" + id;
                                Object obj = memoryCache.get(key);
                                if (obj instanceof StudentInfo) {
                                    ((StudentInfo) obj).setCheckedIn(true);
                                }
                                showStr = getShowString(studentName, "已簽到");
                                System.out.println(showStr);
                                out.println(showStr);
                                broadcastToTeachers(showStr);
                                System.out.println("簽到成功");
                            }
                        }
                    } else if (input.equalsIgnoreCase("SLEEP")) {
                        showStr = getShowString(studentName, "在趴睡");
                        broadcastToTeachers(showStr);
                        updateActivity(id, translateAction(input));
                        System.out.println("在趴睡");
                        out.println("在趴睡");
                    } else if (input.equalsIgnoreCase("TALKING")) {
                        showStr = getShowString(studentName, "在講話");
                        broadcastToTeachers(showStr);
                        updateActivity(id, translateAction(input));
                        System.out.println("在講話");
                        out.println("在講話");
                    } else if (input.equalsIgnoreCase("REQUEST_DRINK")) {
                        showStr = getShowString(studentName, "在喝水");
                        System.out.println(showStr);
                        updateActivity(id, translateAction(input));
                        System.out.println(showStr);
                        out.println(showStr);
                    } else if (input.equalsIgnoreCase("REQUEST_PHONE")) {
                        showStr = getShowString(studentName, "在滑手機");
                        System.out.println(showStr);
                        updateActivity(id, translateAction(input));
                        out.println(showStr);
                    } else {
                        System.out.println(studentName + " 發送未知命令: " + input);
                        System.out.println("未知命令");
                        out.println("未知命令");
                    }
                }
            } catch (IOException e) {
                System.out.println(studentName + " 連線異常中斷");
            } finally {
                students.remove(this);
                try {
                    socket.close();
                } catch (IOException e) {
                    // 忽略
                }
            }
        }

        private void updateActivity(String id, String act) {
            Object obj = memoryCache.get("s_" + id);
            if (obj instanceof StudentInfo) {
                ((StudentInfo) obj).setLastActivity(act);
            }
        }

        private String translateAction(String input) {
            switch (input.toUpperCase()) {
                case "SLEEP":
                    return "在趴睡";
                case "TALKING":
                    return "在講話";
                case "REQUEST_DRINK":
                    return "在喝水";
                case "REQUEST_PHONE":
                    return "在滑手機";
                default:
                    return "不明行為";
            }
        }

        public void sendMessage(String message) {
            if (out != null) {
                out.println("老師說: " + message);
            }
        }

        public void forceDisconnect() {
            try {
                if (out != null) {
                    out.println("QUIT:已離開教室");
                }
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
            }
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        private String getShowString(String name, String doing) {
            return String.format("%s %s", name, doing);
        }

        private void broadcastToTeachers(String message) {
            for (TeacherHandler teacher : teachers) {
                teacher.sendMessage("[通知] " + message);
            }
        }
    }

    // 導師處理器 (改用Message 傳送 接收)
    // static class TeacherHandlerNew implements Runnable {
    static class TeacherHandler implements Runnable {

        private final Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String id;
        private String name;

        // public TeacherHandlerNew(Socket socket) {
        public TeacherHandler(Socket socket, String id, String name) {
            this.socket = socket;
            this.id = id;
            this.name = name;
        }

        public String getId() {
            return this.id;
        }

        public void run() {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String input;
                while ((input = in.readLine()) != null) {
                    // 根據收到的請求做相對應的處理
                    Message msg = Message.fromJson(input);
                    switch (msg.getType()) {
                        // 廣播
                        case "broadcast":
                            // 廣播訊息
                            String broadcastMsg = msg.getContent();
                            // 廣播給所有學生
                            for (StudentHandler student : students) {
                                student.sendMessage("[廣播] " + broadcastMsg);
                            }
                            System.out.println(name + "老師您的廣播訊息已發送給在場所有學生");
                            out.println(name + "老師您的廣播訊息已發送給在場所有學生");
                            break;
                        case "count":
                            System.out.println("目前教室學生人數：" + students.size());
                            out.println("目前教室學生人數：" + students.size());
                            for (StudentHandler student : students) {
                                String key = "s_" + student.getId();
                                Object obj = memoryCache.get(key);
                                if (obj instanceof StudentInfo) {
                                    StudentInfo info = (StudentInfo) obj;
                                    System.out.println(String.format("%s %s [%s] [%s]", student.getId(), student.getName(),
                                            info.isCheckedIn() ? "已簽到" : "未簽到", info.getLastActivity()));
                                    out.println(String.format("%s %s [%s] [%s]", student.getId(), student.getName(),
                                            info.isCheckedIn() ? "已簽到" : "未簽到", info.getLastActivity()));
                                }
                            }
                            break;
                        case "students":
                            List<Map<String, String>> studentList = new ArrayList<>();
                            for (StudentHandler student : students) {
                                String key = "s_" + student.getId();
                                Object obj = memoryCache.get(key);
                                if (obj instanceof StudentInfo) {
                                    StudentInfo info = (StudentInfo) obj;
                                    Map<String, String> studentData = new HashMap<>();
                                    studentData.put("id", student.getId());
                                    studentData.put("name", student.getName());
                                    studentData.put("checkedIn", String.valueOf(info.isCheckedIn()));
                                    studentData.put("activity", info.getLastActivity());
                                    studentList.add(studentData);
                                }
                            }
                            String json = new Gson().toJson(studentList);
                            System.out.println(json);
                            out.println(json);
                            break;
                        case "find":
                            // String targetName = input.substring(5).trim();
                            String targetName = msg.getTarget();
                            StudentHandler targetStudent = findStudentById(targetName);
                            if (targetStudent != null) {
                                // 你可以選擇發送訊息給該學生
                                targetStudent.sendMessage(name + "老師點名你了！");
                                System.out.println("已找到學生 " + targetName + " 並發送訊息");
                                out.println("已找到學生 " + targetName + " 並發送訊息");
                            } else {
                                System.out.println("找不到學生 " + targetName);
                                out.println("找不到學生 " + targetName);
                            }
                            break;
                        case "memo":
                            StudentHandler s = findStudentById(msg.getTarget());
                            if (s != null) {
                                // 你可以選擇發送訊息給該學生
                                s.sendMessage(msg.getContent());
                                System.out.println("已將訊息傳遞給學生 " + msg.getTarget());
                                out.println("已將訊息傳遞給學生 " + msg.getTarget());
                            } else {
                                System.out.println("找不到學生 " + msg.getTarget());
                                out.println("找不到學生 " + msg.getTarget());
                            }
                            break;
                        case "clearroom":
                            for (StudentHandler student : students) {
                                student.sendMessage("[警告] 教室將在 30 秒後清場，請儘速保存資料並離開。");
                            }
                            System.out.println("清場通知已發送，將於 30 秒後強制斷線。");
                            new Thread(() -> {
                                try {
                                    for (int i = 30; i >= 1; i--) {
                                        String countdownMsg = "[倒數] 教室將於 " + i + " 秒後清場";
                                        for (StudentHandler student : students) {
                                            student.sendMessage(countdownMsg);
                                        }
                                        System.out.println(countdownMsg);
                                        out.println(countdownMsg);
                                        Thread.sleep(1000);
                                    }
                                    for (StudentHandler student : students) {
                                        student.forceDisconnect();
                                    }
                                    students.clear();
                                } catch (InterruptedException ignored) {
                                }
                            }).start();
                            break;
                        case "quit":
                            for (StudentHandler student : students) {
                                student.sendMessage("[廣播] 導師 已離開教室 ");
                            }
                            System.out.println("導師 已離開教室");
                            out.println("導師 已離開教室");
                            break;
                        default:
                            System.out.println("未知指令");
                            out.println("未知指令");
                    }
                }
            } catch (IOException e) {
                System.out.println("老師連線異常中斷");
            } finally {
                teachers.remove(this);
                System.out.println("導師連線已關閉，已從教室移除");
                try {
                    socket.close();
                } catch (IOException e) {
                    System.err.println("關閉導師 socket 發生錯誤: " + e.getMessage());
                }
            }
        }

        public void sendMessage(String message) {
            if (out != null) {
                out.println(message);
            }
        }
    }

    static StudentHandler findStudentById(String Id) {
        Object obj = memoryCache.get("s_" + Id);
        if (obj instanceof StudentInfo) {
            return ((StudentInfo) obj).getHandler();
        }
        return null;
    }
}
