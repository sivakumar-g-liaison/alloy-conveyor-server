FROM registry-master.at4d.liacloud.com/alloy/tomcat:1.0.2

ENV APPLICATION_ID g2mailboxservice

COPY "service-implementation/build/libs/g2mailboxservice-*.war" "/opt/liaison/service/g2mailboxservice.war"

RUN chown -R $ALLOY_USER: /opt/liaison/service
RUN chown -R $ALLOY_USER: /opt/liaison/service-conf

# copy the baseline properties to keep env specific properties lighter
# This is supposed to use ENV ${ALLOY_USER} but for some reason it isn't working. Hence using absolute value from alloy-centos/Dockerfile
# No need to copy the base properties since that comes from k8s config map
COPY --chown=alloy ./service-implementation/build/resources/main/${APPLICATION_ID}.properties /opt/liaison/service-conf/properties/${APPLICATION_ID}.properties
COPY --chown=alloy ./service-implementation/build/resources/main/${APPLICATION_ID}.sh /opt/liaison/service-conf/properties/${APPLICATION_ID}.sh
COPY --chown=alloy ./service-implementation/build/resources/main/thread-shutdown.conf /opt/liaison/service-conf/properties/thread-shutdown.conf