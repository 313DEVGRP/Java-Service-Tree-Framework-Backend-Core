#!/bin/sh

set -e

GC_OPTS=${GC_OPTS:="-XX:+UseNUMA -XX:+UseG1GC"}
MEM_OPTS=${MEM_OPTS:="-Xms5120m -Xmx5120m"}
NET_OPTS=${NET_OPTS:="-Dsun.net.inetaddr.ttl=0 -Dsun.net.inetaddr.negative.ttl=0 -Djava.net.preferIPv4Stack=true"}
MONITOR_ELK_OPTS="-javaagent:/elastic-apm-agent.jar -Delastic.apm.service_name=backend-core -Delastic.apm.application_packages=com.arms -Delastic.apm.server_urls=http://apm-server:8200"

# Spring 프로파일 환경 변수 설정
SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-live}

if [ "$SPRING_PROFILES_ACTIVE" = 'live' ]; then
    JVM_OPTS=${JAVA_OPTS:="-server $GC_OPTS $MEM_OPTS $NET_OPTS"}
else
    JVM_OPTS=${JAVA_OPTS:="-server $GC_OPTS $MEM_OPTS $NET_OPTS $MONITOR_ELK_OPTS"}
fi

#spring boot start
exec java -Duser.timezone=Asia/Seoul -Djava.security.egd=file:/dev/./urandom -jar $JVM_OPTS -Dspring.profiles.active=$SPRING_PROFILES_ACTIVE javaServiceTreeFramework.jar $@