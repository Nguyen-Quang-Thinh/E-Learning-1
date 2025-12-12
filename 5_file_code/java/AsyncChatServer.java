import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

public class AsyncChatServer {
    private static final int PORT = 9999;

    public static void main(String[] args) {
        try {
            // 1. Tạo ServerSocketChannel (Non-blocking)
            ServerSocketChannel serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false); // Quan trọng: Chuyển sang chế độ Bất đồng bộ
            serverChannel.bind(new InetSocketAddress("127.0.0.1", PORT));

            // TỐI ƯU HÓA TCP (Theo bài tập cũ): Tái sử dụng địa chỉ
            serverChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);

            // 2. Tạo Selector (Người quản lý sự kiện - Event Loop)
            Selector selector = Selector.open();
            
            // Đăng ký Server vào Selector để lắng nghe kết nối mới (OP_ACCEPT)
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("[JAVA SERVER] Dang chay Async Server tren port " + PORT + "...");

            while (true) {
                // 3. Chờ sự kiện xảy ra (Code sẽ dừng tại đây nếu không có gì mới)
                selector.select();

                // Lấy danh sách các sự kiện (kết nối mới, có tin nhắn mới...)
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectedKeys.iterator();

                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();

                    if (key.isAcceptable()) {
                        // ==> CÓ NGƯỜI MỚI KẾT NỐI
                        handleAccept(serverChannel, selector);
                    } else if (key.isReadable()) {
                        // ==> CÓ TIN NHẮN ĐẾN
                        handleRead(key);
                    }
                    // Xóa sự kiện đã xử lý xong
                    iterator.remove();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Xử lý kết nối mới
    private static void handleAccept(ServerSocketChannel serverChannel, Selector selector) throws IOException {
        SocketChannel clientChannel = serverChannel.accept();
        clientChannel.configureBlocking(false); // Client này cũng phải Non-blocking
        
        // TỐI ƯU HÓA: Bật TCP_NODELAY
        clientChannel.setOption(StandardSocketOptions.TCP_NODELAY, true);
        
        // Đăng ký client này vào Selector để theo dõi khi nào nó gửi tin (OP_READ)
        clientChannel.register(selector, SelectionKey.OP_READ);
        
        System.out.println("[SERVER] Ket noi moi tu: " + clientChannel.getRemoteAddress());
        
        // Gửi lời chào
        String welcome = "Chao mung ban den Java NIO Chat!\n";
        clientChannel.write(ByteBuffer.wrap(welcome.getBytes()));
    }

    // Xử lý đọc tin nhắn và Broadcast
    private static void handleRead(SelectionKey key) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int bytesRead = -1;

        try {
            bytesRead = clientChannel.read(buffer);
        } catch (IOException e) {
            // Client ngắt kết nối đột ngột
            bytesRead = -1;
        }

        if (bytesRead == -1) {
            System.out.println("[SERVER] Client " + clientChannel.getRemoteAddress() + " da thoat.");
            clientChannel.close();
            key.cancel();
            return;
        }

        // Đọc dữ liệu từ buffer
        buffer.flip();
        String message = new String(buffer.array(), 0, bytesRead, StandardCharsets.UTF_8).trim();
        System.out.println("[NHAN] " + message);

        // ==> BROADCAST: Gửi tin nhắn cho tất cả các Client khác
        broadcast(key.selector(), clientChannel, message);
    }

    private static void broadcast(Selector selector, SocketChannel sender, String message) throws IOException {
        String msgToSend = "Client " + sender.getRemoteAddress() + ": " + message + "\n";
        ByteBuffer buffer = ByteBuffer.wrap(msgToSend.getBytes(StandardCharsets.UTF_8));

        // Duyệt qua tất cả các key đang đăng ký trong Selector
        for (SelectionKey key : selector.keys()) {
            Channel targetChannel = key.channel();

            // Chỉ gửi cho SocketChannel (Client) và không gửi lại cho chính người gửi
            if (targetChannel instanceof SocketChannel && targetChannel != sender) {
                SocketChannel dest = (SocketChannel) targetChannel;
                // Lưu ý: Trong thực tế cần check buffer đầy chưa, nhưng demo thì write luôn
                dest.write(buffer);
                buffer.rewind(); // Tua lại buffer để gửi cho người tiếp theo
            }
        }
    }
}