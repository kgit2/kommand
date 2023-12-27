#!/usr/bin/env just --justfile

prepare:
    brew install mingw-w64
    brew tap messense/macos-cross-toolchains
    brew install x86_64-unknown-linux-gnu aarch64-unknown-linux-gnu

clean:
    ./gradlew clean

link-test target:
    ./gradlew {{target}}TestBinaries

linuxX64Test:
    just link-test linuxX64
    # ignore the error exit code
    -docker run -itd --name linuxX64Test \
        -v ./build/bin/linuxX64:/kommand/build/bin/linuxX64 \
        -v ./kommand-core/target/x86_64-unknown-linux-gnu/release/kommand-echo:/kommand/kommand-core/target/x86_64-unknown-linux-gnu/release/kommand-echo \
        -w /kommand \
        -e HTTP_PROXY=host.docker.internal:6152 \
        -e HTTPS_PROXY=host.docker.internal:6152 \
        -e ALL_PROXY=host.docker.internal:6153 \
        --platform linux/amd64 \
        -m 256m \
        --cpus=1 \
        ubuntu \
        bash
    sleep 1
    -docker exec linuxX64Test build/bin/linuxX64/debugTest/test.kexe
    docker rm -f linuxX64Test

linuxArm64Test:
    just link-test linuxArm64
    # ignore the error exit code
    -docker run -itd --name linuxArm64Test \
        -v ./build/bin/linuxArm64:/kommand/build/bin/linuxArm64 \
        -v ./kommand-core/target/aarch64-unknown-linux-gnu/release/kommand-echo:/kommand/kommand-core/target/aarch64-unknown-linux-gnu/release/kommand-echo \
        -w /kommand \
        -e HTTP_PROXY=host.docker.internal:6152 \
        -e HTTPS_PROXY=host.docker.internal:6152 \
        -e ALL_PROXY=host.docker.internal:6153 \
        --platform linux/arm64 \
        -m 256m \
        --cpus=1 \
        ubuntu \
        bash
    sleep 1
    -docker exec linuxArm64Test build/bin/linuxArm64/debugTest/test.kexe
    docker rm -f linuxArm64Test

macosX64Test:
    ./gradlew :cleanMacosX64Test :macosX64Test
    leaks -atExit -- build/bin/macosX64/debugTest/test.kexe

macosArm64Test:
    ./gradlew :cleanMacosArm64Test :macosArm64Test
    leaks -atExit -- build/bin/macosArm64/debugTest/test.kexe

windowsX64Test:
    ./gradlew mingwX64Test

build:
    cd kommand-core && just all

publishToSonatype:
    ./gradlew publishToSonatype

closeSonatype:
    ./gradlew findSonatypeStagingRepository closeSonatypeStagingRepository

releaseSonatype:
    ./gradlew findSonatypeStagingRepository releaseSonatypeStagingRepository

autoPublish: build publishToSonatype closeSonatype releaseSonatype

leaks:
    ./gradlew :cleanMacosX64Test :macosX64Test
    leaks -atExit -- build/bin/macosX64/debugTest/test.kexe

teamcity:
    #-v <path to logs directory>:/opt/teamcity/logs
    docker run --name teamcity-server-instance \
    -v ./:/data/teamcity_server/kommand \
    -p 8111:8111 \
    jetbrains/teamcity-server
