using System.Net.Sockets;
using System.Text;

class Program
{
    static async Task Main(string[] args)
    {
        string host = "127.0.0.1";
        int port = 9999;

        try
        {
            TcpClient client = new TcpClient();
            
            Console.WriteLine("[CLIENT] Dang ket noi...");
            await client.ConnectAsync(host, port);

            // --- TỐI ƯU HÓA TCP ---
            client.NoDelay = true; // Gửi tin nhắn tức thì

            Console.WriteLine($"==> Da ket noi toi Server {host}:{port}");
            Console.WriteLine("==> Go tin nhan va Enter. Go 'exit' de thoat.");

            NetworkStream stream = client.GetStream();

            // Chạy Task nhận tin nhắn song song
            var receiveTask = ReceiveMessagesAsync(stream);

            // Luồng chính dùng để gửi tin nhắn
            using (StreamWriter writer = new StreamWriter(stream, Encoding.UTF8))
            {
                writer.AutoFlush = true; // Quan trọng: Tự động đẩy dữ liệu đi ngay

                while (true)
                {
                    Console.Write("[Ban]: ");
                    string? msg = Console.ReadLine();

                    if (string.IsNullOrWhiteSpace(msg)) continue;
                    if (msg.ToLower() == "exit") break;

                    // Gửi lên server
                    await writer.WriteLineAsync(msg);
                }
            }
            
            client.Close();
        }
        catch (Exception ex)
        {
            Console.WriteLine($"Loi ket noi: {ex.Message}");
        }
    }

    static async Task ReceiveMessagesAsync(NetworkStream stream)
    {
        using (StreamReader reader = new StreamReader(stream, Encoding.UTF8))
        {
            try
            {
                while (true)
                {
                    // Chờ nhận tin nhắn từ Server
                    string? msg = await reader.ReadLineAsync();
                    if (msg == null) break;

                    // Xóa dòng "[Ban]: " hiện tại để in tin nhắn đến cho đẹp
                    // (Làm màu mè console một chút)
                    Console.Write("\r" + new string(' ', Console.WindowWidth) + "\r");
                    
                    Console.WriteLine(msg);
                    
                    // In lại dấu nhắc
                    Console.Write("[Ban]: ");
                }
            }
            catch
            {
                Console.WriteLine("\n[Mat ket noi voi Server]");
            }
        }
    }
}