#!/bin/sh

#packetbeat start
#./packetbeat-7.4.2-linux-x86_64/packetbeat -e -c /packetbeat-7.4.2-linux-x86_64/packetbeat.yml &

#topbeat start
#./topbeat-1.3.1-x86_64/topbeat -e -c /topbeat-1.3.1-x86_64/topbeat.yml &

#metricbeat start
#./metricbeat-7.4.2-linux-x86_64/metricbeat -e -c /metricbeat-7.4.2-linux-x86_64/metricbeat.yml &

#heartbeat start
#./heartbeat-7.4.2-linux-x86_64/heartbeat -e -c /heartbeat-7.4.2-linux-x86_64/heartbeat.yml &

#filebeat start
#./filebeat-7.4.2-linux-x86_64/filebeat -e -c /filebeat-7.4.2-linux-x86_64/filebeat.yml &

set -e

GC_OPTS=${GC_OPTS:="-XX:+UseNUMA -XX:+UseG1GC"}
MEM_OPTS=${MEM_OPTS:="-Xms4096m -Xmx4096m"}
NET_OPTS=${NET_OPTS:="-Dsun.net.inetaddr.ttl=0 -Dsun.net.inetaddr.negative.ttl=0 -Djava.net.preferIPv4Stack=true"}
#MONITOR_ELK_OPTS="-javaagent:/elastic-apm-agent.jar -Delastic.apm.service_name=middle-proxy -Delastic.apm.application_packages=proxy.api -Delastic.apm.server_urls=http://192.168.25.46:8200"
JVM_OPTS="-server $GC_OPTS $MEM_OPTS $NET_OPTS"

#JAVA_OPTS=${JAVA_OPTS:="-server $GC_OPTS $MEM_OPTS $NET_OPTS $MONITOR_ELK_OPTS"}

#spring boot start
#exec java -Djava.security.egd=file:/dev/./urandom -jar $JAVA_OPTS -Dspring.profiles.active=live javaServiceTreeFramework.jar $@
exec java -Djava.security.egd=file:/dev/./urandom -jar $JVM_OPTS -Dspring.profiles.active=live javaServiceTreeFramework.jar $@