cmake_minimum_required(VERSION 3.22.1)

project("cloud_core")

set(CMAKE_CXX_STANDARD 20)


if(DEFINED SHARED_SOURCES_CMAKE_PATH)

include(${SHARED_SOURCES_CMAKE_PATH})

else()

include("../../../shared-core/sources.cmake")

endif()

if(DEFINED CLOUD_SOURCES_CMAKE_PATH)

include(${CLOUD_SOURCES_CMAKE_PATH})

else()

include("../../../core/sources.cmake")

endif()

add_library(${CMAKE_PROJECT_NAME} SHARED
    ${SHARED_SOURCES}
    ${CLOUD_SOURCES}
    ${SHARED_JAVA_BINDING_SOURCES}
    ${CLOUD_JAVA_BINDING_SOURCES}
)

target_include_directories(${CMAKE_PROJECT_NAME} PRIVATE ${SHARED_INCLUDE_DIRS} ${CLOUD_INCLUDE_DIRS} ${SHARED_JAVA_BINDING_INCLUDE_DIRS} ${CLOUD_JAVA_BINDING_INCLUDE_DIRS})

target_link_libraries(${CMAKE_PROJECT_NAME}
    android
    log)
