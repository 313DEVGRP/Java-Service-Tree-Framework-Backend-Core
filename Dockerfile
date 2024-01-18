FROM openjdk:11-jre
MAINTAINER 313DEVGRP <313@313.co.kr>

RUN apt-get update
RUN apt-get -y -q install libpcap0.8 wget procps
#
#
#RUN wget http://www.313.co.kr/nexus/content/groups/public/313devgrp/packetbeat/7.4.2-linux/packetbeat-7.4.2-linux-x86_64.tar.gz
#RUN tar zxvf packetbeat-7.4.2-linux-x86_64.tar.gz
#RUN wget http://www.313.co.kr/nexus/content/repositories/StandardProject/313devgrp/packetbeat/1.0.313/packetbeat-1.0.313.yml
#RUN mv packetbeat-1.0.313.yml ./packetbeat-7.4.2-linux-x86_64/packetbeat.yml
#
#
#RUN wget http://www.313.co.kr/nexus/content/groups/public/313devgrp/topbeat/1.3.1/topbeat-1.3.1-x86_64.tar.gz
#RUN tar zxvf topbeat-1.3.1-x86_64.tar.gz
#RUN wget http://www.313.co.kr/nexus/content/repositories/StandardProject/313devgrp/topbeat/1.0.313/topbeat-1.0.313.yml
#RUN mv topbeat-1.0.313.yml ./topbeat-1.3.1-x86_64/topbeat.yml
#
#
#RUN wget http://www.313.co.kr/nexus/content/groups/public/313devgrp/metricbeat/7.4.2-linux/metricbeat-7.4.2-linux-x86_64.tar.gz
#RUN tar zxvf metricbeat-7.4.2-linux-x86_64.tar.gz
#RUN wget http://www.313.co.kr/nexus/content/repositories/StandardProject/313devgrp/metricbeat/1.0.313/metricbeat-1.0.313.yml
#RUN mv metricbeat-1.0.313.yml ./metricbeat-7.4.2-linux-x86_64/metricbeat.yml
#
#
#RUN wget http://www.313.co.kr/nexus/content/groups/public/313devgrp/heartbeat/7.4.2-linux/heartbeat-7.4.2-linux-x86_64.tar.gz
#RUN tar zxvf heartbeat-7.4.2-linux-x86_64.tar.gz
#RUN wget http://www.313.co.kr/nexus/content/repositories/StandardProject/313devgrp/heartbeat/1.0.313/heartbeat-1.0.313.yml
#RUN mv heartbeat-1.0.313.yml ./heartbeat-7.4.2-linux-x86_64/heartbeat.yml
#
#
#RUN wget http://www.313.co.kr/nexus/content/groups/public/313devgrp/filebeat/7.4.2-linux/filebeat-7.4.2-linux-x86_64.tar.gz
#RUN tar zxvf filebeat-7.4.2-linux-x86_64.tar.gz
#RUN wget http://www.313.co.kr/nexus/content/repositories/StandardProject/313devgrp/filebeat/1.0.313/filebeat-1.0.313.yml
#RUN mv filebeat-1.0.313.yml ./filebeat-7.4.2-linux-x86_64/filebeat.yml
#
#
#RUN wget http://www.313.co.kr/nexus/content/groups/public/313devgrp/elastic-apm-agent/1.18.1/elastic-apm-agent-1.18.1.jar
#RUN mv elastic-apm-agent-1.18.1.jar ./elastic-apm-agent.jar

VOLUME /tmp

ARG ENTRY_FILE
COPY ${ENTRY_FILE} docker-entrypoint.sh

ARG JAR_FILE
COPY ${JAR_FILE} javaServiceTreeFramework.jar

RUN chmod +x /docker-entrypoint.sh

ENTRYPOINT ["sh","/docker-entrypoint.sh"]
CMD ["start"]