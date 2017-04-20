# Sets the minimum version of CMake required to build the native
# library. You should either keep the default value or only pass a
# value of 3.4.0 or lower.

#CMake版本信息
cmake_minimum_required(VERSION 3.4.1)

set(lib_src_DIR ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI})

include_directories(
        ${CMAKE_SOURCE_DIR}/include
)
# build application's shared lib
add_library(native-lib SHARED
            native-lib.cpp)

add_library(libopencv_java3_lib SHARED IMPORTED)
set_target_properties(libopencv_java3_lib PROPERTIES IMPORTED_LOCATION
                             ${lib_src_DIR}/libopencv_java3.so)


target_link_libraries(native-lib
log
  android
libopencv_java3_lib
  )