using System.Collections.Concurrent;
using System.Net;
using System.Net.Sockets;
using System.Text;

class Program
{
    // Danh sách Thread-safe để lưu các client đang kết nối
    // ConcurrentDictionary giúp tránh lỗi khi nhiều luồng cùng truy cập
    private static ConcurrentDictionary<TcpClient, string> clients = new ConcurrentDictionary<TcpClient, string>();

    static async Task Main(string[] args)
    {
        int port = 9999;
        TcpListener listener = new TcpListener(IPAddress.Any, port);
        listener.Start();

        Console.WriteLine($"[C# SERVER] Dang chay Async Server tren port {port}...");

        while (true)
        {
            // await: Chờ có kết nối mới mà không chặn luồng chính
            TcpClient client = await listener.AcceptTcpClientAsync();

            // --- TỐI ƯU HÓA TCP (Theo yêu cầu) ---
            client.NoDelay = true; // Tắt Nagle Algorithm để gửi nhanh
            client.ReceiveBufferSize = 4096;
            
            // Xử lý client này ở một Task riêng biệt (Fire-and-forget)
            // Code sẽ quay lại vòng lặp while ngay lập tức để đón khách mới
            _ = HandleClientAsync(client);
        }
    }

    static async Task HandleClientAsync(TcpClient client)
    {
        string clientEndPoint = client.Client.RemoteEndPoint?.ToString() ?? "Unknown";
        Console.WriteLine($"[SERVER] Ket noi moi tu: {clientEndPoint}");

        // Thêm vào danh sách
        clients.TryAdd(client, clientEndPoint);
        await BroadcastMessageAsync($"==> {clientEndPoint} da tham gia chat!", client);

        NetworkStream stream = client.GetStream();
        // Dùng StreamReader để đọc từng dòng text cho dễ
        using (StreamReader reader = new StreamReader(stream, Encoding.UTF8))
        {
            try
            {
                while (true)
                {
                    // Đọc bất đồng bộ
                    string? message = await reader.ReadLineAsync();

                    // Nếu message là null nghĩa là client đã ngắt kết nối
                    if (message == null) break;

                    Console.WriteLine($"[{clientEndPoint}]: {message}");
                    
                    // Gửi tin nhắn cho mọi người
                    await BroadcastMessageAsync($"Client {clientEndPoint}: {message}", client);
                }
            }
            catch (Exception ex)
            {
                // Lỗi thường gặp khi client tắt đột ngột
                Console.WriteLine($"[LOI] {clientEndPoint}: {ex.Message}");
            }
            finally
            {
                // Dọn dẹp
                clients.TryRemove(client, out _);
                client.Close();
                Console.WriteLine($"[SERVER] {clientEndPoint} da thoat.");
                await BroadcastMessageAsync($"==> {clientEndPoint} da roi khoi phong chat.", null);
            }
        }
    }

    static async Task BroadcastMessageAsync(string message, TcpClient? sender)
    {
        byte[] buffer = Encoding.UTF8.GetBytes(message + "\n"); // Thêm xuống dòng

        foreach (var clientPair in clients)
        {
            TcpClient client = clientPair.Key;

            // Không gửi lại cho chính người gửi (nếu có sender)
            if (client != sender && client.Connected)
            {
                try
                {
                    NetworkStream stream = client.GetStream();
                    // Gửi bất đồng bộ
                    await stream.WriteAsync(buffer, 0, buffer.Length);
                }
                catch
                {
                    // Nếu gửi lỗi thì bỏ qua
                }
            }
        }
    }
}