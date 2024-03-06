FROM openjdk:11-jre
MAINTAINER 313DEVGRP <313@313.co.kr>

RUN apt-get update
RUN apt-get -y -q install libpcap0.8 wget procps

RUN wget http://www.313.co.kr/nexus/repository/ple-releases/313devgrp/elastic-apm-agent/1.47.1/elastic-apm-agent-1.47.1.jar
RUN mv elastic-apm-agent-1.47.1.jar ./elastic-apm-agent.jar

VOLUME /tmp

ARG ENTRY_FILE
COPY ${ENTRY_FILE} docker-entrypoint.sh

ARG JAR_FILE
COPY ${JAR_FILE} javaServiceTreeFramework.jar

RUN chmod +x /docker-entrypoint.sh

ENTRYPOINT ["sh","/docker-entrypoint.sh"]
CMD ["start"]