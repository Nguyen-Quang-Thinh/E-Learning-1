package main

import (
	"bufio"
	"fmt"
	"net"
	"os"
	"strings"
)

const (
	HOST = "localhost"
	PORT = "9999"
	TYPE = "tcp"
)

func main() {
	conn, err := net.Dial(TYPE, HOST+":"+PORT)
	if err != nil {
		fmt.Println("Không thể kết nối tới Server:", err)
		return
	}
	defer conn.Close()

	// --- TỐI ƯU HÓA TCP ---
	if tcpConn, ok := conn.(*net.TCPConn); ok {
		tcpConn.SetNoDelay(true) // Gửi tin tức thì
	}

	fmt.Println("------------------------------------------------")
	fmt.Println("Đã kết nối tới Go Chat Server!")
	fmt.Println("Gõ tin nhắn và nhấn Enter. Gõ 'exit' để thoát.")
	fmt.Println("------------------------------------------------")

	// --- LUỒNG 1: NHẬN TIN (Chạy ngầm) ---
	go func() {
		scanner := bufio.NewScanner(conn)
		for scanner.Scan() {
			fmt.Println(scanner.Text())
		}
		// Nếu vòng lặp kết thúc nghĩa là mất kết nối
		fmt.Println("\n[Mất kết nối với Server]")
		os.Exit(0)
	}()

	// --- LUỒNG 2 (CHÍNH): GỬI TIN ---
	consoleScanner := bufio.NewScanner(os.Stdin)
	fmt.Print("[Bạn]: ") // Dấu nhắc ban đầu

	for consoleScanner.Scan() {
		text := consoleScanner.Text()

		if strings.ToLower(text) == "exit" {
			fmt.Println("Đang thoát...")
			break
		}

		// Gửi lên server (thêm \n để server biết hết câu)
		fmt.Fprintf(conn, "%s\n", text)

		// In lại dấu nhắc
		// Lưu ý: Do bất đồng bộ, dấu nhắc này có thể bị trôi khi có tin nhắn đến
		// nhưng đây là cách đơn giản nhất cho Demo.
	}
}
