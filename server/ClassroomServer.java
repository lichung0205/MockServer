package server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class ClassroomServer {
    private static final int PORT = 9527;
    private static ServerSocket serverSocket;
    private static final List<StudentHandler> students = new CopyOnWriteArrayList<>();
    private static final List<TeacherHandler> teachers = new CopyOnWriteArrayList<>();

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
    static class TeacherHandler implements Runnable {
        private final Socket socket;
        private PrintWriter out;
        private BufferedReader in;

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
                    if (input.equalsIgnoreCase("QUIT")) {
                        System.out.println("導師 已離開教室");
                        break;
                    } else if (input.equalsIgnoreCase("REQUEST_BROADCAST")) {
                        // 寫死的廣播訊息
                        String broadcastMsg = "這是一個廣播訊息";
                        // 廣播給所有學生
                        for (StudentHandler student : students) {
                            student.sendMessage("[廣播] " + broadcastMsg);
                        }
                        out.println("同意: 廣播訊息已發送");
                    } else if (input.equalsIgnoreCase("REQUEST_COUNT")) {
                        out.println("同意: 當前學生人數: " + students.size());
                    } else {
                        System.out.println(" 發送未知命令: " + input);
                        out.println("未知命令");
                    }
                }
            } catch (IOException e) {
                System.out.println("老師連線異常中斷");
            } finally {
                teachers.remove(this);
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
