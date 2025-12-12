import asyncio

async def handle_echo(reader, writer):
    # Lấy địa chỉ của client
    addr = writer.get_extra_info('peername')
    print(f"[SERVER] Kết nối mới từ: {addr}")

    try:
        while True:
            # await: Tạm dừng ở đây chờ dữ liệu, nhưng Server vẫn rảnh để tiếp khách khác
            data = await reader.read(100) 
            
            if not data:
                break # Client ngắt kết nối

            message = data.decode()
            print(f"[SERVER] Nhận từ {addr}: {message}")

            # Gửi lại (Echo)
            print(f"[SERVER] Phản hồi cho {addr}: {message}")
            writer.write(data)
            await writer.drain() # Đợi bộ đệm xả hết
    except Exception as e:
        print(f"Lỗi: {e}")
    finally:
        print(f"[SERVER] Đóng kết nối {addr}")
        writer.close()
        await writer.wait_closed()

async def main():
    # Khởi tạo server lắng nghe trên cổng 8888
    server = await asyncio.start_server(handle_echo, '127.0.0.1', 8888)

    addr = server.sockets[0].getsockname()
    print(f'[SERVER] Asyncio Echo Server đang chạy tại {addr}')

    async with server:
        await server.serve_forever()

if __name__ == '__main__':
    try:
        asyncio.run(main())
    except KeyboardInterrupt:
        pass