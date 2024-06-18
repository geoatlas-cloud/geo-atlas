FROM adoptopenjdk/openjdk8

ENV SPRING_OUTPUT_ANSI_ENABLED=ALWAYS \
    JAVA_OPTS="-Xms1024M -Xmx2048M" \
    TZ=Asia/Shanghai \
    LANG=C.UTF-8

ADD /target/*.jar /usr/app/tiles-api-app.jar

WORKDIR /usr/app

RUN sh -c 'touch tiles-api-app.jar'

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar tiles-api-app.jar"]