FROM alloy/tomcat:latest

ENV APPLICATION_ID g2mailboxservice 
ENV APP_ID $APPLICATION_ID

COPY ./service-implementation/build/libs/g2mailboxservice*war /opt/liaison/components/g2-service/g2mailboxservice.war

RUN chown -R $APPLICATION_USER /opt/liaison/components/g2-service




