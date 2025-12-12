import asyncio

# Danh sách lưu trữ các kết nối đang hoạt động
writers = []

async def handle_chat(reader, writer):
    addr = writer.get_extra_info('peername')
    
    # 1. Thêm người mới vào danh sách
    writers.append(writer)
    print(f"[SERVER] {addr} đã tham gia Chat Room.")
    
    # Thông báo cho mọi người biết có người mới
    message_join = f"==> {addr} da tham gia phong chat!\n"
    for w in writers:
        if w != writer: # Không gửi lại cho chính người vừa vào
            w.write(message_join.encode())
            await w.drain()

    try:
        while True:
            data = await reader.read(1024)
            if not data:
                break
            
            msg = data.decode().strip()
            print(f"[{addr}] noi: {msg}")
            
            # 2. BROADCAST: Gửi tin nhắn cho tất cả client khác
            response = f"Client {addr} noi: {msg}\n"
            for w in writers:
                if w != writer: # Không gửi lại cho người nói
                    w.write(response.encode())
                    await w.drain()
                    
    except Exception as e:
        print(f"Lỗi kết nối: {e}")
    finally:
        # 3. Dọn dẹp khi có người thoát
        print(f"[SERVER] {addr} đã thoát.")
        writers.remove(writer) # Xóa khỏi danh sách
        writer.close()
        await writer.wait_closed()

async def main():
    server = await asyncio.start_server(handle_chat, '127.0.0.1', 9999)
    print("Async Chat Server đang chạy trên cổng 9999...")
    async with server:
        await server.serve_forever()

if __name__ == '__main__':
    try:
        asyncio.run(main())
    except KeyboardInterrupt:
        pass