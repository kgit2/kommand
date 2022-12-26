# syntax=docker/dockerfile:1
FROM ubuntu:jammy
RUN apt-get update && apt-get install -y \
    curl \
    iputils-ping \
    && rm -rf /var/lib/apt/lists/*
