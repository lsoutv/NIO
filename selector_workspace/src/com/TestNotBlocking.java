package com;

import org.junit.Test;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 *  TCP
 */
public class TestNotBlocking {

    //客户端
    @Test
    public void client() throws IOException {
        //获取通道
        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1",9898));
        //切换成为阻塞模式
        socketChannel.configureBlocking(false);
        //创建一个缓冲区,存放传送的数据
        ByteBuffer buf = ByteBuffer.allocate(1024);
        //发送数据给服务端
        buf.put("Hello".getBytes());
        buf.flip();
        socketChannel.write(buf);
        //关闭通道
        socketChannel.close();
    }

    //服务端
    @Test
    public void server() throws IOException {
        //1、获取通道
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        //2、切换成非阻塞模式
        serverSocketChannel.configureBlocking(false);
        //3、绑定连接
        serverSocketChannel.bind(new InetSocketAddress(9898));
        //4、获取选择器
        Selector selector = Selector.open();
        //5、将通道注册到选择器上，并指定为“监听接收”
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        //6、轮询式的获取选择器上已经“准备就绪”的事件
        while(selector.select()>0){
            //7、获取当前选择器中所有注册的“选择键（已经准备就绪的监听事件）”
            Iterator<SelectionKey> selectionKeys = selector.selectedKeys().iterator();
            //8、获取准备“就绪的事件”
            while(selectionKeys.hasNext()){
                //9、判断具体是什么事件
                SelectionKey selectionKey = selectionKeys.next();
                if (selectionKey.isAcceptable()){
                    //10、若“准备就绪”，获取客户端连接
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    //11、切换成为阻塞模式
                    socketChannel.configureBlocking(false);
                    //12、将该通道注册在选择器上
                    socketChannel.register(selector,SelectionKey.OP_READ);
                }else if (selectionKey.isReadable()){
                    //13、获取“读就绪”的事件
                    SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                    //14、读取数据
                    ByteBuffer buf = ByteBuffer.allocate(1024);
                    int len = -1;
                    while((len=socketChannel.read(buf))!=-1){
                        buf.flip();
                        System.out.println(new String(buf.array()));
                        buf.clear();
                    }
                }
            }
            selectionKeys.remove();//没有迭代循环之后 移除当前的迭代
        }
    }
}
