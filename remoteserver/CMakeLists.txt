###############################
#1.cmake verson，指定cmake版本
cmake_minimum_required(VERSION 3.4.1)

#2.配置so路径
set(lib_src_DIR ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI})
include_directories(
     ${CMAKE_SOURCE_DIR}/src/main/jni/include
)

#3.导入第三方so
add_library(nano SHARED IMPORTED)
#3.1 设置要链接的so的相对路径
set_target_properties(nano PROPERTIES IMPORTED_LOCATION ${lib_src_DIR}/libnano.so)

find_library( log-lib log )

#4. 指定编译生成so名和类型
add_library(RemoteServiceJNI SHARED
         ${CMAKE_SOURCE_DIR}/src/main/jni/RemoteServiceJNI.c)


#特别注意，一个so对应一个link，生成在intermediates/cmake/目录下
target_link_libraries(
 RemoteServiceJNI
 nano
 log
 android
)


