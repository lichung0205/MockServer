package server;

import common.AuthType;
import common.LoginInfo;
import common.Message;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class ClassroomServer {
    private static final int PORT = 9527;
    private static ServerSocket serverSocket;
    private static final List<StudentHandler> students = new CopyOnWriteArrayList<>();
    private static final List<TeacherHandler> teachers = new CopyOnWriteArrayList<>();
    private static final Map<String, Object> memoryCache = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("教室伺服器已啟動..");
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
                    if (!memoryCache.containsKey(studentKey))
                        memoryCache.put(studentKey, handler); // 加入 cache
                    new Thread(handler).start();
                    System.out.printf("%s 號 %s 已進入教室\n", info.getId(), info.getName());
                } // 導師登入 gmtools
                else if (info.getAuthType().equals(AuthType.TEACHER)) {
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

                String showStr = "";
                String input;
                while ((input = in.readLine()) != null) {
                    // 根據收到的請求做相對應的處理
                    if (input.equalsIgnoreCase("QUIT")) {
                        showStr = getShowString(studentName, "已離開教室");
                        System.out.println(showStr);
                        broadcastToTeachers(showStr);
                        memoryCache.remove("s_" + id);
                        break;
                    } else if (input.equalsIgnoreCase("CHECKIN")) {
                        String key = "s_" + id;
                        Object obj = memoryCache.get(key);
                        if (obj instanceof StudentInfo) {
                            ((StudentInfo) obj).setCheckedIn(true);
                        }
                        showStr = getShowString(studentName, "已簽到");
                        broadcastToTeachers(showStr);
                        out.println("簽到成功");
                    } else if (input.equalsIgnoreCase("SLEEP")) {
                        showStr = getShowString(studentName, "在趴睡");
                        broadcastToTeachers(showStr);
                        out.println("在趴睡");
                    } else if (input.equalsIgnoreCase("TALKING")) {
                        showStr = getShowString(studentName, "在講話");
                        broadcastToTeachers(showStr);
                        out.println("在講話");
                    } else if (input.equalsIgnoreCase("REQUEST_DRINK")) {
                        showStr = getShowString(studentName, "在喝水");
                        System.out.println(showStr);
                        out.println(showStr);
                    } else if (input.equalsIgnoreCase("REQUEST_PHONE")) {
                        showStr = getShowString(studentName, "在滑手機");
                        System.out.println(showStr);
                        out.println(showStr);
                    } else {
                        System.out.println(studentName + " 發送未知命令: " + input);
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

        public void sendMessage(String message) {
            if (out != null) {
                out.println("老師說: " + message);
            }
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
                            out.println(name + "老師您的廣播訊息已發送給在場所有學生");
                            break;
                        case "count":
                            out.println("目前教室學生人數：" + students.size());
                            break;
                        case "find":
                            // String targetName = input.substring(5).trim();
                            String targetName = msg.getTarget();
                            StudentHandler targetStudent = findStudentByName(targetName);
                            if (targetStudent != null) {
                                // 你可以選擇發送訊息給該學生
                                targetStudent.sendMessage(name + "老師點名你了！");
                                out.println("已找到學生 " + targetName + " 並發送訊息");
                            } else {
                                out.println("找不到學生 " + targetName);
                            }
                            break;
                        case "memo":
                            StudentHandler s = findStudentByName(msg.getTarget());
                            if (s != null) {
                                // 你可以選擇發送訊息給該學生
                                s.sendMessage(msg.getContent());
                                out.println("已將訊息傳遞給學生 " + msg.getTarget());
                            } else {
                                out.println("找不到學生 " + msg.getTarget());
                            }
                            break;
                        case "quit":
                            for (StudentHandler student : students) {
                                student.sendMessage("[廣播] 導師 已離開教室 ");
                            }
                            System.out.println("導師 已離開教室");
                            break;
                        default:
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

    static StudentHandler findStudentByName(String name) {
        Object obj = memoryCache.get("s_" + name);
        if (obj instanceof StudentInfo) {
            return ((StudentInfo) obj).getHandler();
        }
        return null;
    }
}
