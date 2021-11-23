FROM azul/zulu-openjdk:11
LABEL maintainer="junyoung@smarf.kr"
WORKDIR /opt/skec-sms-service
RUN mkdir log
RUN ls .
VOLUME ["/opt/skec-sms-service/log"]
COPY ./config /opt/skec-sms-service/config
RUN ls ./config
COPY ./target/skec-sms-service-0.1.0-SNAPSHOT-standalone.jar /opt/skec-sms-service/app.jar
COPY ./run.sh /opt/skec-sms-service/entrypoint.sh
RUN chmod 755 entrypoint.sh
ENV JVM_PROFILE prod
ENV JVM_ENCODING UTF-8
ENV JVM_MINIMUM_MEMORY 256m
ENV JVM_MAXIMUM_MEMORY 2g
EXPOSE 9000
ENTRYPOINT ["./entrypoint.sh"]