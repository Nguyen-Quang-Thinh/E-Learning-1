import socket
import threading
import sys

# Cấu hình kết nối tới Server
HOST = '127.0.0.1'
PORT = 9999  # Phải trùng với cổng trong file chat_server_async.py

def receive_messages(sock):
    """Hàm chạy ngầm để nhận tin nhắn từ Server liên tục"""
    while True:
        try:
            data = sock.recv(1024)
            if not data:
                print("\n[Mất kết nối với Server]")
                break
            # In tin nhắn nhận được ra màn hình
            print(data.decode(), end='') 
        except:
            print("\n[Lỗi kết nối]")
            break

def main():
    client = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    try:
        client.connect((HOST, PORT))
        print(f"==> Đã kết nối tới Chat Server tại {PORT}!")
        print("==> Gõ tin nhắn và nhấn Enter để gửi.")
        
        # Tạo một luồng riêng chỉ để NHẬN tin nhắn
        thread = threading.Thread(target=receive_messages, args=(client,))
        thread.daemon = True # Tự tắt khi chương trình chính tắt
        thread.start()
        
        # Luồng chính dùng để GỬI tin nhắn (Nhập từ bàn phím)
        while True:
            msg = input() # Đợi bạn nhập
            if msg.lower() == 'exit':
                break
            client.sendall(msg.encode())
            
    except ConnectionRefusedError:
        print("LỖI: Không tìm thấy Server! Hãy chắc chắn bạn đã chạy file Server trước.")
    finally:
        client.close()

if __name__ == "__main__":
    main()