//package com.cooyet.im.imservice.network;
//
//import com.cooyet.im.config.SysConstant;
//import com.cooyet.im.protobuf.base.DataBuffer;
//import com.cooyet.im.protobuf.base.Header;
//import com.cooyet.im.utils.Logger;
//import com.google.protobuf.GeneratedMessageLite;
//
//import java.net.InetSocketAddress;
//import java.util.concurrent.ThreadFactory;
//
//import io.netty.bootstrap.Bootstrap;
//import io.netty.channel.Channel;
//import io.netty.channel.ChannelFuture;
//import io.netty.channel.ChannelInitializer;
//import io.netty.channel.ChannelOption;
//import io.netty.channel.SimpleChannelInboundHandler;
//import io.netty.channel.nio.NioEventLoopGroup;
//import io.netty.channel.udt.UdtChannel;
//import io.netty.channel.udt.nio.NioUdtProvider;
//import io.netty.handler.logging.LogLevel;
//import io.netty.handler.logging.LoggingHandler;
//import io.netty.util.concurrent.DefaultThreadFactory;
//
//public class SocketThread extends Thread {
//    private Bootstrap clientBootstrap = null;
//    private ChannelFuture channelFuture = null;
//    private Channel channel = null;
//    private String strHost = null;
//    private int nPort = 0;
//    private static Logger logger = Logger.getLogger(SocketThread.class);
//
//    public SocketThread(String strHost, int nPort, SimpleChannelInboundHandler handler) {
//        this.strHost = strHost;
//        this.nPort = nPort;
//        init(handler);
//    }
//
//    @Override
//    public void run() {
//        doConnect();
//    }
//
//    private void init(final SimpleChannelInboundHandler handler) {
//        final ThreadFactory connectFactory = new DefaultThreadFactory("connect");
//        final NioEventLoopGroup connectGroup = new NioEventLoopGroup(1, connectFactory, NioUdtProvider.BYTE_PROVIDER);
////        final Bootstrap boot = new Bootstrap();
//
//        clientBootstrap.group(connectGroup).channelFactory(NioUdtProvider.BYTE_CONNECTOR).handler(new ChannelInitializer<UdtChannel>() {
//            @Override
//            protected void initChannel(UdtChannel udtChannel) throws Exception {
//                udtChannel.pipeline().addLast(
//                        new LoggingHandler(LogLevel.INFO),
//                        handler);
//            }
//        });
//        clientBootstrap.option(ChannelOption.SO_KEEPALIVE, true);
//        clientBootstrap.option(ChannelOption.SO_TIMEOUT, 5000);
//    }
//
//    public boolean doConnect() {
//        try {
//            if ((null == channel || (null != channel && !channel.isActive()))
//                    && null != this.strHost && this.nPort > 0) {
//                // Start the connection attempt.
//                channelFuture = clientBootstrap.connect(new InetSocketAddress(
//                        strHost, nPort)).sync();
//                // Wait until the connection attempt succeeds or fails.
//                channel = channelFuture.awaitUninterruptibly().channel();
//                if (!channelFuture.isSuccess()) {
//                    channelFuture.cause().printStackTrace();
////                    clientBootstrap.releaseExternalResources();
//                    // ReconnectManager.getInstance().setOnRecconnecting(false);
//                    // ReconnectManager.getInstance().setLogining(false);
//                    return false;
//                }
//            }
//
//            // Wait until the connection is closed or the connection attemp
//            // fails.
//            channelFuture.channel().closeFuture().awaitUninterruptibly();
//            // Shut down thread pools to exit.
////            clientBootstrap.releaseExternalResources();
//            return true;
//
//        } catch (Exception e) {
//            logger.e("do connect failed. e: %s", e.getStackTrace().toString());
//            return false;
//        }
//    }
//
//    public Channel getChannel() {
//        return channel;
//    }
//
//    public void close() {
//        if (null == channelFuture)
//            return;
//        if (null != channelFuture.channel()) {
//            channelFuture.channel().close();
//        }
//        channelFuture.cancel(true);
//    }
//
//
//    //    // todo check
//    @Deprecated
//    public boolean isClose() {
//        if (channelFuture != null && channelFuture.channel() != null) {
//            return !channelFuture.channel().isActive();
//        }
//        return true;
//    }
//
//    /**
//     * @param requset
//     * @param header
//     * @return
//     */
//    public boolean sendRequest(GeneratedMessageLite requset, Header header) {
//
//        DataBuffer headerBuffer = header.encode();
//        DataBuffer bodyBuffer = new DataBuffer();
//        int bodySize = requset.getSerializedSize();
//        bodyBuffer.writeBytes(requset.toByteArray());
//
//        DataBuffer buffer = new DataBuffer(SysConstant.PROTOCOL_HEADER_LENGTH + bodySize);
//        buffer.writeDataBuffer(headerBuffer);
//        buffer.writeDataBuffer(bodyBuffer);
//
//        if (null != buffer && null != channelFuture.channel()) {
//            /**底层的状态要提前判断，netty抛出的异常上层catch不到*/
////            Channel currentChannel = channelFuture.channel();
////            boolean isW = currentChannel.isWritable();
////            boolean isC = currentChannel.isConnected();
////            if (!(isW && isC)) {
////                throw new RuntimeException("#sendRequest#channel is close!");
////            }
//            channelFuture.channel().write(buffer.getOrignalBuffer());
//            logger.d("packet#send ok");
//            return true;
//        } else {
//            logger.e("packet#send failed");
//            return false;
//        }
//    }
//
//}
