# CimIM Base TTIM(基于开源TTIM的二次开发)

## **项目截图**

截图1：

![image](https://github.com/danxinzhicheng/CimIM/raw/master/Screenshot/Screenshot_1.png)

截图2：

![image](https://github.com/danxinzhicheng/CimIM/raw/master/Screenshot/Screenshot_2.png)

截图3：

![image](https://github.com/danxinzhicheng/CimIM/raw/master/Screenshot/Screenshot_3.png)

截图4：

![image](https://github.com/danxinzhicheng/CimIM/raw/master/Screenshot/Screenshot_4.png)

截图5：

![image](https://github.com/danxinzhicheng/CimIM/raw/master/Screenshot/Screenshot_5.png)

截图6：

![image](https://github.com/danxinzhicheng/CimIM/raw/master/Screenshot/Screenshot_6.png)

截图7：

![image](https://github.com/danxinzhicheng/CimIM/raw/master/Screenshot/Screenshot_7.png)

截图8：

![image](https://github.com/danxinzhicheng/CimIM/raw/master/Screenshot/Screenshot_8.png)

截图9：

![image](https://github.com/danxinzhicheng/CimIM/raw/master/Screenshot/Screenshot_9.png)


## **修改及增加功能点涉及：**
### 1.  修改UI界面

基于原有蘑菇街TeamTalk项目，界面修改为蓝色扁平基调，头像为圆形头像

### 2.  完全重构底层业务通信方式

聊天底层的通信，由原有TCP(基于netty框架)+protobuf,改为UDP(UDT库)+protobuf.
-    UDT库是基于UDP协议的数据传输的C++库，支持海量数据传输，保证了发送报文的时序性。
-    本项目采用Android推荐方式 Cmake，编译C++源码成udt.so，通过JNi，使java代码方便调用UDT的接口。
-    发送报文使用protobuf。其和json,xml一样，进行网络传输的一种数据序列化标准，谷歌出品，值得信赖，灵活高效，可以跨平台。感兴趣的盆友了解一下。
-    考虑到udp接收报文太大的时候（比如语音，文件）会自动分包，将收到的包保存到队列中，再重新组成完整的序列包。
-    cpp源码路径：[app\src\main\cpp\libudt](https://github.com/danxinzhicheng/CimIM/tree/master/app/src/main/cpp/libudt)，具体编译配置参见目录下的CmakeList
-    java代码路径：[app\src\main\java\com\cooyet\im\imservice\manager\IMUdtManager.java](https://github.com/danxinzhicheng/CimIM/blob/master/app/src/main/java/com/cooyet/im/imservice/manager/IMUdtManager.java)

### 3. 语音对讲

- 语音对讲类路径：[app\src\main\java\com\cooyet\im\imservice\manager\talk](https://github.com/danxinzhicheng/CimIM/tree/master/app/src/main/java/com/cooyet/im/imservice/manager/talk)
- 语音对讲界面见截图9
- 功能说明：
- [x] - UI交互参照微信，用户A请求对讲，用户B接收对讲;
- [x] - 用户A讲话时,用户B不能讲话;
- [x] - 整个请求-接收-对讲-挂断流程通过UDT通信;
- [x] - 语音采集暂时用PCM格式数据，一种未做编解码的原始语音数据，便于以后对声音的处理;
- [x] - 聊天界面使用WindowManager显示，可以在应用任意界面接收语音请求；
- [x] - 接收语音的字节采用双缓冲机制，一定程度避免接收字节的不连续，网络延迟造成对语音质量造成的影响。


### 4. 基于高德地图，加入位置标记的发送（态势）

- 态势具体类路径：[app\src\main\java\com\cooyet\im\ui\fragment\StateFragment.java](https://github.com/danxinzhicheng/CimIM/blob/master/app/src/main/java/com/cooyet/im/ui/fragment/StateFragment.java)

- 态势界面见截图5.

- 功能说明：

- [x] 手动触摸地图，可以标记车，人。，在画笔的状态下，可以画出红色轨迹。
- [x] 点击发送，屏幕上位置坐标转换为经纬度坐标，发送给好友。（暂未实现）
- [x] 缓存屏幕标记到本地，下次进入应用可以显示上一次的标记

4. 基于高德地图，加入聊天中发送位置功能（参照微信）
- 发送位置类路径：[app\src\main\java\com\cooyet\im\ui\activity\LocationPickerActivity.java](https://github.com/danxinzhicheng/CimIM/blob/master/app/src/main/java/com/cooyet/im/ui/activity/LocationPickerActivity.java)
- 发送位置界面见截图8.
- 功能说明

- [x] UI交互参照微信，使用高德地图热点搜索功能。
- [x] 发送暂时以图片方式发送（把当前地图截图，对此图片添加附带经纬度信息，使接收方判断是否是发送位置信息还是正常发送的图片）
