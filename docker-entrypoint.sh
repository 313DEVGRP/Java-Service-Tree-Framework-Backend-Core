#!/bin/sh

set -e

GC_OPTS=${GC_OPTS:="-XX:+UseNUMA -XX:+UseG1GC"}
MEM_OPTS=${MEM_OPTS:="-Xms5120m -Xmx5120m"}
NET_OPTS=${NET_OPTS:="-Dsun.net.inetaddr.ttl=0 -Dsun.net.inetaddr.negative.ttl=0 -Djava.net.preferIPv4Stack=true"}
MONITOR_ELK_OPTS="-javaagent:/elastic-apm-agent.jar -Delastic.apm.service_name=middle-proxy -Delastic.apm.application_packages=com.arms -Delastic.apm.server_urls=http://apm-server:8200"

JVM_OPTS=${JAVA_OPTS:="-server $GC_OPTS $MEM_OPTS $NET_OPTS $MONITOR_ELK_OPTS"}

#spring boot start
exec java -Duser.timezone=Asia/Seoul -Djava.security.egd=file:/dev/./urandom -jar $JVM_OPTS -Dspring.profiles.active=live javaServiceTreeFramework.jar $@