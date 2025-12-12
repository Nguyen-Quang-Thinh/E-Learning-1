import asyncio

async def tcp_echo_client(message):
    print(f"[CLIENT] Đang kết nối...")
    reader, writer = await asyncio.open_connection('127.0.0.1', 8888)

    print(f'[CLIENT] Gửi tin nhắn: {message}')
    writer.write(message.encode())
    await writer.drain()

    # Đợi phản hồi
    data = await reader.read(100)
    print(f'[CLIENT] Server phản hồi: {data.decode()}')

    print('[CLIENT] Đóng kết nối')
    writer.close()
    await writer.wait_closed()

if __name__ == '__main__':
    # Bạn có thể đổi nội dung tin nhắn ở đây
    asyncio.run(tcp_echo_client('Hello Asyncio World!'))