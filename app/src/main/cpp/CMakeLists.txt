cmake_minimum_required(VERSION 3.22.1)

project("proxmark3-tools")

# Tambahkan library native C++ di sini
add_library(
    proxmark3-tools
    SHARED
    native-lib.cpp
)

find_library(
    log-lib
    log
)

target_link_libraries(
    proxmark3-tools
    ${log-lib}
)
