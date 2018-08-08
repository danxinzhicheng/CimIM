#AUX_SOURCE_DIRECTORY(. SRC_LIST)
list(APPEND SRC_LIST
       api.cpp
       buffer.cpp
       cache.cpp
       ccc.cpp
       channel.cpp
       common.cpp
       core.cpp
       epoll.cpp
       list.cpp
       md5.cpp
       packet.cpp
       queue.cpp
       window.cpp
       udt_jni.cpp
       )

find_library( # Defines the name of the path variable that stores the
              # location of the NDK library.
              log-lib

              # Specifies the name of the NDK library that
              # CMake needs to locate.
              log )
#target_link_libraries(${log-lib} )

#ADD_DEFINITIONS(-fPIC -Wall -Wextra -finline-functions -O3 -fno-strict-aliasing -fvisibility=hidden -Dos=arm)
ADD_DEFINITIONS(-fPIC -Wall -Wextra -finline-functions -O3 -fno-strict-aliasing -fvisibility=hidden -DLINUX)

INCLUDE_DIRECTORIES(. ${CMAKE_CURRENT_SOURCE_DIR}/../include)

#LINK_DIRECTORIES(.)
ADD_LIBRARY(udt SHARED ${SRC_LIST} ${log-lib})

#TARGET_LINK_LIBRARIES(udt)
 
