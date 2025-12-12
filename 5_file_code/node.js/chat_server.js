const net = require('net');

const PORT = 9999;
const HOST = '127.0.0.1';

// Danh sách chứa các client đang online
let clients = [];

const server = net.createServer((socket) => {
    // Định danh client
    socket.name = `${socket.remoteAddress}:${socket.remotePort}`;
    
    // Thêm vào danh sách
    clients.push(socket);
    
    console.log(`[SERVER] ${socket.name} đã tham gia.`);
    broadcast(`==> ${socket.name} da tham gia phong chat!\n`, socket);

    // Xử lý khi nhận tin nhắn
    socket.on('data', (data) => {
        const message = data.toString().trim();
        // Kiểm tra nếu tin nhắn rỗng thì bỏ qua
        if (message.length === 0) return;

        console.log(`[LOG] ${socket.name}: ${message}`);
        
        // Gửi cho tất cả người khác
        broadcast(`Client ${socket.name} noi: ${message}\n`, socket);
    });

    // Xử lý khi client thoát
    socket.on('end', () => {
        clients.splice(clients.indexOf(socket), 1); // Xóa khỏi danh sách
        console.log(`[SERVER] ${socket.name} đã thoát.`);
        broadcast(`==> ${socket.name} da roi khoi phong chat.\n`, socket);
    });

    // Xử lý lỗi
    socket.on('error', (err) => {
        // Lỗi thường gặp là client bị tắt đột ngột (Connection reset)
        clients.splice(clients.indexOf(socket), 1);
        console.log(`[LỖI] ${socket.name} ngắt kết nối bất thường.`);
    });
});

// Hàm gửi tin nhắn cho tất cả TRỪ người gửi (sender)
function broadcast(message, sender) {
    clients.forEach((client) => {
        // Kiểm tra client còn sống và không phải là người gửi
        if (client !== sender && client.writable) {
            client.write(message);
        }
    });
}

server.listen(PORT, HOST, () => {
    console.log(`[NODEJS] Chat Server đang chạy trên port ${PORT}...`);
});