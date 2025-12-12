package main

import (
	"bufio"
	"fmt"
	"net"
	"strings"
	"sync"
)

// Cấu hình
const (
	HOST = "localhost"
	PORT = "9999"
	TYPE = "tcp"
)

// Quản lý danh sách client an toàn với Mutex
var (
	clients = make(map[net.Conn]string) // Map lưu kết nối và tên
	mu      sync.Mutex                  // Khóa an toàn cho Map
)

func main() {
	listen, err := net.Listen(TYPE, HOST+":"+PORT)
	if err != nil {
		fmt.Println("Lỗi khởi tạo Server:", err)
		return
	}
	defer listen.Close()

	fmt.Println("[GO SERVER] Đang chạy trên " + HOST + ":" + PORT)
	fmt.Println("[GO SERVER] Sử dụng Goroutines để xử lý đa luồng...")

	for {
		conn, err := listen.Accept()
		if err != nil {
			fmt.Println("Lỗi chấp nhận kết nối:", err)
			continue
		}

		// --- TỐI ƯU HÓA TCP (Theo yêu cầu) ---
		// Ép kiểu về TCPConn để chỉnh options
		if tcpConn, ok := conn.(*net.TCPConn); ok {
			tcpConn.SetNoDelay(true) // Tắt Nagle Algorithm
			tcpConn.SetKeepAlive(true)
		}

		// Tạo một Goroutine riêng cho client này (Giống như phân thân)
		go handleClient(conn)
	}
}

func handleClient(conn net.Conn) {
	defer conn.Close()

	// Lấy địa chỉ client
	clientAddr := conn.RemoteAddr().String()
	fmt.Printf("[SERVER] Kết nối mới: %s\n", clientAddr)

	// Thêm vào danh sách (cần khóa Mutex)
	mu.Lock()
	clients[conn] = clientAddr
	mu.Unlock()

	// Gửi thông báo cho mọi người
	broadcastMessage(conn, "==> "+clientAddr+" đa tham gia phong chat!\n")

	reader := bufio.NewReader(conn)
	for {
		// Đọc tin nhắn (Chờ đến khi có ký tự xuống dòng)
		message, err := reader.ReadString('\n')
		if err != nil {
			// Client ngắt kết nối
			break
		}

		message = strings.TrimSpace(message)
		if len(message) > 0 {
			fmt.Printf("[%s]: %s\n", clientAddr, message)
			// Gửi tin nhắn cho các client khác
			broadcastMessage(conn, "Client "+clientAddr+" noi: "+message+"\n")
		}
	}

	// Xử lý khi client thoát
	mu.Lock()
	delete(clients, conn)
	mu.Unlock()

	fmt.Printf("[SERVER] %s đã thoát.\n", clientAddr)
	broadcastMessage(nil, "==> "+clientAddr+" đa roi khoi phong chat.\n")
}

// Hàm gửi tin nhắn cho tất cả (trừ người gửi)
func broadcastMessage(sender net.Conn, message string) {
	mu.Lock()
	defer mu.Unlock()

	for conn := range clients {
		if conn != sender {
			// Gửi tin nhắn
			conn.Write([]byte(message))
		}
	}
}
