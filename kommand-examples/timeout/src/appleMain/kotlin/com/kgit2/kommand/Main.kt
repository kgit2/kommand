package com.kgit2.kommand

import com.kgit2.kommand.process.Command
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.Clock

fun main() = runBlocking(Dispatchers.Default) {
    // Sleep with regular
    val start = Clock.System.now()
    val status = Command("sleep").arg("5").status()
    println("status: $status elapsed: ${Clock.System.now() - start}")

    // Sleep with timeout detection and timeout determination
    val start2 = Clock.System.now()
    val child = Command("sleep").arg("5").spawn()
    val childJob = async(Dispatchers.IO) {
        runCatching {
            child.wait()
        }.onFailure {
            println("child result: $it")
        }.getOrNull()
    }
    runCatching {
        withTimeout(3000) {
            childJob.await()
        }
    }.onSuccess {
        println("status: $it elapsed: ${Clock.System.now() - start2}")
    }.onFailure {
        child.kill()
        println("status: $it elapsed: ${Clock.System.now() - start2}")
    }

    // Sleep with timeout detection and determination that it will not timeout
    val start3 = Clock.System.now()
    val child2 = Command("sleep").arg("2").spawn()
    val childJob2 = async(Dispatchers.IO) {
        runCatching {
            child2.wait()
        }.onFailure {
            println("child result: $it")
        }.getOrNull()
    }
    runCatching {
        withTimeout(3000) {
            childJob2.await()
        }
    }.onSuccess {
        println("status: $it elapsed: ${Clock.System.now() - start3}")
    }.onFailure {
        child2.kill()
        println("status: $it elapsed: ${Clock.System.now() - start3}")
    }

    Unit
}
