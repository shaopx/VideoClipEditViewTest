cmake_minimum_required(VERSION 3.4.1)

#cpp文件
add_library(MagicBeautify
            SHARED
            src/main/jni/MagicJni.cpp)

#lib引用
find_library(log-lib log)
find_library(jnigraphics-lib jnigraphics)
find_library(GLESv2-lib GLESv2)

#链接lib
target_link_libraries(MagicBeautify
                        ${log-lib}
                        ${jnigraphics-lib}
                        ${GLESv2-lib})