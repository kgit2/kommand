#!/usr/bin/env just --justfile

prepare:
    brew install mingw-w64
    brew tap messense/macos-cross-toolchains
    brew install x86_64-unknown-linux-gnu aarch64-unknown-linux-gnu

clean:
    ./gradlew clean

macosX64Test:
    ./gradlew :cleanMacosX64Test :macosX64Test :jvmTest
    leaks -atExit -- build/bin/macosX64/debugTest/test.kexe

macosArm64Test:
    ./gradlew :cleanMacosArm64Test :macosArm64Test :jvmTest
    leaks -atExit -- build/bin/macosArm64/debugTest/test.kexe

linuxX64Test:
    ./gradlew :cleanLinuxX64Test :linuxX64Test :jvmTest

linuxArm64Test:
    ./gradlew :cleanLinuxArm64TestBinaries :linuxArm64TestBinaries

mingwX64Test:
    ./gradlew :cleanMingwX64Test :mingwX64Test :jvmTest

linuxX64TestDocker: linuxX64Test
    # if need ignore the error exit code, then prefix -
    # don't remove the working directory
    docker run --rm --name linuxX64Test \
        -v .:/kommand \
        -w /kommand \
        --platform linux/amd64 \
        -m 256m \
        --cpus=1 \
        ubuntu:22.04 \
        /kommand/build/bin/linuxX64/debugTest/test.kexe

linuxArm64TestDocker: linuxArm64Test
    # if need ignore the error exit code, then prefix -
    # don't remove the working directory
    docker run --rm --name linuxArm64Test \
        -v .:/kommand \
        -w /kommand \
        --platform linux/arm64 \
        -m 256m \
        --cpus=1 \
        ubuntu:22.04 \
        /kommand/build/bin/linuxArm64/debugTest/test.kexe

buildKommandCore:
    cd kommand-core && just all

# for publish

publishToSonatype:
    ./gradlew publishToSonatype

closeSonatype:
    ./gradlew findSonatypeStagingRepository closeSonatypeStagingRepository

releaseSonatype:
    ./gradlew findSonatypeStagingRepository releaseSonatypeStagingRepository

autoPublish: macosX64Test buildKommandCore publishToSonatype closeSonatype releaseSonatype

ciPublish SONATYPE_USERNAME SONATYPE_PASSWORD GPG_KEY_ID GPG_PASSPHRASE: macosX64Test buildKommandCore
    ./gradlew publishToSonatype \
        closeSonatypeStagingRepository \
        releaseSonatypeStagingRepository \
        -PossrhUsername="{{ SONATYPE_USERNAME }}" \
        -PossrhPassword="{{ SONATYPE_PASSWORD }}" \
        -Psigning.keyId="{{ GPG_KEY_ID }}" \
        -Psigning.password="{{ GPG_PASSPHRASE }}" \
        -Psigning.secretKeyRingFile="$HOME/.gradle/secret.gpg"

importKey:
    gpg --batch --import $HOME/.gradle/secret.gpg

generateFRP GPG_KEY_ID:
    @gpg --list-keys --with-colons --keyid-format LONG --fingerprint {{GPG_KEY_ID}} | awk -F: '$1 == "fpr" || $1 == "fp2" {print $10}' | head -n 1

deleteKey GPG_KEY_ID:
    #!/bin/sh
    GPG_FINGERPRINT=$(just generateFRP {{GPG_KEY_ID}})
    gpg --batch --yes --delete-secret-keys $GPG_FINGERPRINT
    gpg --batch --yes --delete-keys $GPG_FINGERPRINT
