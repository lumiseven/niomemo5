package code.lumiseven.test.nio.groupchat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

public class GroupChatServer {

    // 定义属性
    private Selector selector;
    private ServerSocketChannel listenChannel;
    private static final int PORT = 6667;

    public GroupChatServer() {
        try {
            // 1. 得到选择器
            selector = Selector.open();
            // 2. 获取 serverSocketChannel
            listenChannel = ServerSocketChannel.open();
            // 3. 绑定端口
            listenChannel.bind(new InetSocketAddress(PORT));
            // 4. 配置非阻塞模式
            listenChannel.configureBlocking(false);
            // 5. 将 serverSocketChannel 注册到 selector
            listenChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 监听
    public void listen() {
        System.out.println("监听线程: " + Thread.currentThread().getName());
        try {
            while (true) {
                int count = selector.select();// 可以设置时间阻塞 也可以直接返回
                if (count <= 0) {
                    System.out.println("等待连接...");
                    continue;
                }
                // 有事件处理
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    // 取出 selectionKey
                    SelectionKey key = iterator.next();

                    // 接入事件
                    if (key.isAcceptable()) {
                        SocketChannel sc = listenChannel.accept();
                        sc.configureBlocking(false);
                        // 将 sc 注册到 selector
                        sc.register(selector, SelectionKey.OP_READ);

                        // 提示客户端上线
                        System.out.println(sc.getRemoteAddress() + " 上线 ");
                    }
                    if (key.isReadable()) {
                        // 通道发送read事件 即通道是可读的状态 则读取并处理数据
                        readData(key);
                    }
                    // 删除当前的 selectionKey 防止重复处理
                    iterator.remove();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 读取客户端消息
    private void readData(SelectionKey key) {
        SocketChannel channel = null;
        try {
            // 通过selectionKey 获取到channel
            channel = (SocketChannel) key.channel();

            // 创建buffer
            ByteBuffer buffer = ByteBuffer.allocate(1024);

            // 假设数据量不超过 1024 字节
            int count = channel.read(buffer);

            if (count > 0) {
                String msg = new String(buffer.array());// 获取整个buffer中的字节 并且转化为字符串
                // 输出该消息
                System.out.println("from 客户端: " + msg);
                // 向其他的客户端发送消息(注意去除自己)
                sendInfoToOtherClients(msg, channel);
            }
        } catch (IOException e) {
            try {
                System.out.println(channel.getRemoteAddress() + " 离线了..");
                // 取消注册
                key.cancel();
                // 关闭通道
                channel.close();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }
    }

    // 转发消息给其它客户(通道)
    private void sendInfoToOtherClients(String msg, SocketChannel self) throws IOException {
        System.out.println("服务器转发消息中...");
        System.out.println("服务器转发数据给客户端线程: " + Thread.currentThread().getName());
        // 遍历 所有注册到selector 上的 SocketChannel, 并排除 self
        for (SelectionKey key: selector.keys()) {
            // 通过 key 取出对应的 SocketChannel
            SocketChannel targetChannel = (SocketChannel) key.channel();

            // 排除自己
            if (targetChannel != self) {
                // 将msg存储到buffer
                ByteBuffer wrap = ByteBuffer.wrap(msg.getBytes(StandardCharsets.UTF_8));
                targetChannel.write(wrap);
            }
        }
    }

    public static void main(String[] args) {
        GroupChatServer groupChatServer = new GroupChatServer();
        groupChatServer.listen();
    }
}
