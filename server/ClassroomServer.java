package server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

import common.Message;

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
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String identity = in.readLine();
                if (identity.startsWith("STUDENT:")) {
                    String studentName = identity.substring(8);
                    StudentHandler handler = new StudentHandler(clientSocket, studentName);
                    students.add(handler);
                    String studentKey = "s_" + studentName;
                    if (!memoryCache.containsKey(studentKey))
                        memoryCache.put(studentKey, handler); // 加入 cache
                    new Thread(handler).start();
                    System.out.println(studentName + " 已進入教室");
                } else if (identity.startsWith("TEACHER:")) {
                    TeacherHandler handler = new TeacherHandler(clientSocket);
                    teachers.add(handler);
                    new Thread(handler).start();
                    System.out.println("老師已進入教室");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 學生處理器
    static class StudentHandler implements Runnable {
        private final Socket socket;
        private final String studentName;
        private PrintWriter out;
        private BufferedReader in;

        public StudentHandler(Socket socket, String studentName) {
            this.socket = socket;
            this.studentName = studentName;
        }

        public void run() {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                String showStr = "";
                String input;
                while ((input = in.readLine()) != null) {
                    // 根據收到的請求做相對應的處理
                    if (input.equalsIgnoreCase("QUIT")) {
                        System.out.println(studentName + " 已離開教室");
                        memoryCache.remove("s_" + studentName);
                        break;
                    } else if (input.equalsIgnoreCase("REQUEST_DRINK")) {
                        showStr = String.format("%s在喝水", studentName);
                        System.out.println(showStr);
                        out.println(showStr);
                    } else if (input.equalsIgnoreCase("REQUEST_PHONE")) {
                        showStr = String.format("%s在滑手機", studentName);
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
    }

    // 導師處理器 (可按照需求修改)
    // static class TeacherHandler implements Runnable {
    // private final Socket socket;
    // private PrintWriter out;
    // private BufferedReader in;

    // public TeacherHandler(Socket socket) {
    // this.socket = socket;
    // }

    // public void run() {
    // try {
    // out = new PrintWriter(socket.getOutputStream(), true);
    // in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    // String input;
    // while ((input = in.readLine()) != null) {
    // // 根據收到的請求做相對應的處理
    // if (input.equalsIgnoreCase("QUIT")) {
    // System.out.println("導師 已離開教室");
    // break;
    // } else if (input.equalsIgnoreCase("REQUEST_BROADCAST")) {
    // // 寫死的廣播訊息
    // String broadcastMsg = "這是一個廣播訊息";
    // // 廣播給所有學生
    // for (StudentHandler student : students) {
    // student.sendMessage("[廣播] " + broadcastMsg);
    // }
    // out.println("同意: 廣播訊息已發送");
    // } else if (input.equalsIgnoreCase("REQUEST_COUNT")) {
    // out.println("同意: 當前學生人數: " + students.size());
    // } else {
    // System.out.println(" 發送未知命令: " + input);
    // out.println("未知命令");
    // }
    // }
    // } catch (IOException e) {
    // System.out.println("老師連線異常中斷");
    // } finally {
    // teachers.remove(this);
    // try {
    // socket.close();
    // } catch (IOException e) {
    // }
    // }
    // }
    // }

    // 導師處理器 (改用Message 傳送 接收)
    // static class TeacherHandlerNew implements Runnable {
    static class TeacherHandler implements Runnable {
        private final Socket socket;
        private PrintWriter out;
        private BufferedReader in;

        // public TeacherHandlerNew(Socket socket) {
        public TeacherHandler(Socket socket) {
            this.socket = socket;
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
                            out.println("老師您的廣播訊息已發送給在場所有學生");
                            break;
                        case "count":
                            out.println("目前教室學生人數：" + students.size());
                            break;
                        case "find":
                            String targetName = input.substring(5).trim();
                            StudentHandler targetStudent = findStudentByName(targetName);
                            if (targetStudent != null) {
                                // 你可以選擇發送訊息給該學生
                                targetStudent.sendMessage("老師點名你了！");
                                out.println("已找到學生 " + targetName + " 並發送訊息");
                            } else {
                                out.println("找不到學生 " + targetName);
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
                // teachers.remove(this);
                System.out.println("導師連線已關閉，已從教室移除");
                try {
                    socket.close();
                } catch (IOException e) {
                    System.err.println("關閉導師 socket 發生錯誤: " + e.getMessage());
                }
            }
        }
    }

    static StudentHandler findStudentByName(String name) {
        Object obj = memoryCache.get("s_" + name);
        if (obj instanceof StudentHandler) {
            return (StudentHandler) obj;
        }
        return null;
    }
}
