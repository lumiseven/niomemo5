package code.lumiseven.test.nio.zerocopy;

import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

public class NewIOClient {
    public static void main(String[] args) throws Exception {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.connect(new InetSocketAddress("localhost", 7001));

        String filename = "pom.xml";

        // 得到一个文件 channel
        FileChannel fileChannel = new FileInputStream(filename).getChannel();

        // 准备发送
        long startTime = System.currentTimeMillis();

        /**
         * 在 linux 下一个 transferTo 方法就能完成传输
         * 在 windows 下 一次调用 transferTo 只能发送8m 需要考虑分段传输
         * transferTo 底层使用的就是 zerocopy
         */
        long transferCount = fileChannel.transferTo(0, fileChannel.size(), socketChannel);

        System.out.println("发送的总字节数 = " + transferCount + " 耗时 = " + (System.currentTimeMillis() - startTime));

        // 关闭
        fileChannel.close();
    }
}
