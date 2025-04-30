FROM ubuntu:latest
ARG MVN_VERSION

WORKDIR /InsideAgentBot
COPY ./out/Bot/InsideAgentDev-${MVN_VERSION}.jar app.jar
COPY ./out/Bot/config/* ./config/

RUN apt-get update \
    && apt-get install -y \
    wget \
    maven \
    && rm -rf /var/lib/apt/lists/*
RUN wget https://download.oracle.com/java/24/latest/jdk-24_linux-x64_bin.deb
RUN dpkg -i jdk-24_linux-x64_bin.deb
RUN rm jdk-24_linux-x64_bin.deb

ENTRYPOINT ["java","-jar","app.jar"]