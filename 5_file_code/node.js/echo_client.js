const net = require('net');

const PORT = 8888;
const HOST = '127.0.0.1';

const client = new net.Socket();

// Tối ưu hóa TCP (Bài tập yêu cầu)
client.setNoDelay(true);

client.connect(PORT, HOST, () => {
    console.log('[CLIENT] Đã kết nối tới Server!');
    const msg = 'Hello Node.js Async!';
    console.log(`[GỬI]: ${msg}`);
    client.write(msg);
});

client.on('data', (data) => {
    console.log(`[SERVER TRẢ LỜI]: ${data.toString()}`);
    client.end(); // Nhận xong thì tự ngắt kết nối
});

client.on('close', () => {
    console.log('[CLIENT] Đã ngắt kết nối.');
});