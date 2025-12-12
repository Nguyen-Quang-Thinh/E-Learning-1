const net = require('net');

const PORT = 8888;
const HOST = '127.0.0.1';

// Tạo server
const server = net.createServer((socket) => {
    // Lấy thông tin địa chỉ người kết nối
    const clientAddress = `${socket.remoteAddress}:${socket.remotePort}`;
    console.log(`[SERVER] Kết nối mới từ: ${clientAddress}`);

    // Sự kiện khi nhận dữ liệu (Data event)
    socket.on('data', (data) => {
        const msg = data.toString().trim();
        console.log(`[NHẬN TỪ ${clientAddress}]: ${msg}`);

        // Gửi lại (Echo)
        socket.write(data); 
        console.log(`[PHẢN HỒI]: ${msg}`);
    });

    // Sự kiện khi client ngắt kết nối
    socket.on('end', () => {
        console.log(`[SERVER] Đóng kết nối: ${clientAddress}`);
    });

    // Xử lý lỗi (để server không bị crash nếu client rớt mạng đột ngột)
    socket.on('error', (err) => {
        console.log(`[LỖI] ${clientAddress}: ${err.message}`);
    });
});

// Bắt đầu lắng nghe
server.listen(PORT, HOST, () => {
    console.log(`[NODEJS] Echo Server đang chạy tại ${HOST}:${PORT}`);
});