import java.io.*;
import java.net.*;
import java.util.HashSet;
import java.util.Set;

public class ChatServer {
    private static Set<String> userNames = new HashSet<>(); // Lưu tên người dùng
    private static Set<PrintWriter> writers = new HashSet<>(); // Lưu danh sách các luồng xuất của client

    public static void main(String[] args) throws Exception {
        System.out.println("Chat server is running...");
        ServerSocket listener = new ServerSocket(12345);
        try {
            while (true) {
                new Handler(listener.accept()).start();
            }
        } finally {
            listener.close();
        }
    }

    // Gửi danh sách người dùng tới tất cả các client
    private static void sendUserList() {
        StringBuilder userListMessage = new StringBuilder("USERLIST");
        for (String user : userNames) {
            userListMessage.append(",").append(user);
        }
        for (PrintWriter writer : writers) {
            writer.println(userListMessage.toString());
        }
    }

    private static class Handler extends Thread {
        private String name;
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                // Tạo luồng vào và ra cho client
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Yêu cầu tên người dùng cho đến khi tên hợp lệ
                while (true) {
                    out.println("SUBMITNAME");
                    name = in.readLine();
                    if (name == null) {
                        return;
                    }
                    synchronized (userNames) {
                        if (!name.isEmpty() && !userNames.contains(name)) {
                            userNames.add(name);
                            break;
                        }
                    }
                }

                // Thông báo kết nối thành công và gửi danh sách người dùng
                writers.add(out);
                for (PrintWriter writer : writers) {
                    writer.println(name + " đã tham gia cuộc trò chuyện");
                }

                // Gửi danh sách người dùng cập nhật
                sendUserList();

                // Đọc các tin nhắn từ client và gửi cho tất cả các client khác
                String message;
                while ((message = in.readLine()) != null) {
                    for (PrintWriter writer : writers) {
                        writer.println(name + ": " + message);
                    }
                }
            } catch (IOException e) {
                System.out.println(e);
            } finally {
                // Người dùng rời khỏi, loại bỏ người dùng và luồng xuất
                if (name != null) {
                            for (PrintWriter writer : writers) {
                                writer.println(name + " đã rời khỏi cuộc trò chuyện");
                            }
                    userNames.remove(name);
                    sendUserList(); // Gửi lại danh sách người dùng sau khi có người rời phòng
                }
                if (out != null) {
                    writers.remove(out);
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    System.out.println(e);
                }
            }
        }
    }

}