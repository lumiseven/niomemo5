package code.lumiseven.test.bio;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 测试
 * 1. 启动BIO服务端
 * 2. telnet 客户端连接(Ctrl+] quit 退出)
 * 3. 客户端发送消息 服务端接收并打印出来
 */
public class BIOServer {

    public static void main(String[] args) throws Exception {
        /**
         * 使用线程池
         * 如果有客户端连接 就创建一个线程 与之通讯
         */
        ExecutorService newCachedThreadPool = Executors.newCachedThreadPool();

        // 创建 ServerSocket 并绑定端口
        ServerSocket serverSocket = new ServerSocket(4000);

        while (true) {
            System.out.println("线程信息 id =" + Thread.currentThread().getId() + " 名字=" + Thread.currentThread().getName());
            //监听，等待客户端连接
            System.out.println("等待连接....");
            final Socket socket = serverSocket.accept();
            System.out.println("连接到一个客户端");

            // 开启一个新线程与之通讯
            newCachedThreadPool.execute(new Runnable() {
                public void run() {
                    // 与客户端通讯
                    handler(socket);
                }
            });
        }
    }

    private static void handler(Socket socket) {
        System.out.println("线程信息 id =" + Thread.currentThread().getId() + " 名字=" + Thread.currentThread().getName());
        byte[] bytes = new byte[1024];
        try {
            InputStream inputStream = socket.getInputStream();

            // 循环读取客户端发送的数据
            while (true) {
                System.out.println("线程信息 id =" + Thread.currentThread().getId() + " 名字=" + Thread.currentThread().getName());

                System.out.println("read....");

                int read = inputStream.read(bytes);
                if (read == -1)
                    break;
                System.out.println(new String(bytes, 0, read));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.out.println("关闭与client的连接");
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
