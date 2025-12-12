const net = require('net');
const readline = require('readline');

const PORT = 9999;
const HOST = '127.0.0.1';

const client = new net.Socket();

// Thiết lập giao diện nhập liệu từ bàn phím
const rl = readline.createInterface({
  input: process.stdin,
  output: process.stdout
});

client.connect(PORT, HOST, () => {
    console.log('------------------------------------------------');
    console.log(`Đã kết nối tới Chat Server tại ${HOST}:${PORT}`);
    console.log("Gõ tin nhắn và nhấn Enter để gửi. Gõ 'exit' để thoát.");
    console.log('------------------------------------------------');
    
    // Bắt đầu cho phép nhập
    promptUser();
});

// Sự kiện nhận dữ liệu từ Server
client.on('data', (data) => {
    // Xóa dòng hiện tại (để tin nhắn đến không bị chèn vào dòng đang gõ)
    process.stdout.clearLine();
    process.stdout.cursorTo(0);
    
    console.log(data.toString().trim());
    
    // Hiện lại dấu nhắc để nhập tiếp
    promptUser();
});

client.on('close', () => {
    console.log('\n[Mất kết nối với Server]');
    process.exit(0);
});

client.on('error', (err) => {
    console.log(`\n[LỖI KẾT NỐI]: ${err.message}`);
});

// Hàm xử lý nhập liệu
function promptUser() {
    rl.question('[Bạn]: ', (message) => {
        if (message.toLowerCase() === 'exit') {
            client.end();
            rl.close();
            process.exit(0);
        } else {
            // Gửi tin nhắn lên server
            client.write(message);
            // Tiếp tục vòng lặp nhập liệu (Nhưng chờ readline xử lý xong)
            // Lưu ý: rl.question sẽ tự lock luồng nhập cho đến khi Enter, 
            // nên ta gọi lại promptUser bên trong callback này là đúng logic.
        }
    });
}