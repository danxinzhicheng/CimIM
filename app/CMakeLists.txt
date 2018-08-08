#指定需要CMAKE的最小版本
cmake_minimum_required(VERSION 3.4.1)

#C 的编译选项是 CMAKE_C_FLAGS
# 指定编译参数，可选
#SET(CMAKE_CXX_FLAGS "-Wno-error=format-security -Wno-error=pointer-sign")

#设置生成的so动态库最后输出的路径
#set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${PROJECT_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI})

#设置头文件搜索路径（和此txt同个路径的头文件无需设置），可选
#INCLUDE_DIRECTORIES(${CMAKE_CURRENT_SOURCE_DIR}/../include)

#指定用到的系统库或者NDK库或者第三方库的搜索路径，可选.
#LINK_DIRECTORIES(/usr/local/lib)


#添加子目录,将会调用子目录中的CMakeLists.txt
ADD_SUBDIRECTORY(src/main/cpp/libsecurity)
ADD_SUBDIRECTORY(src/main/cpp/libspeex)
ADD_SUBDIRECTORY(src/main/cpp/libudt)
