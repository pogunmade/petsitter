# syntax=docker/dockerfile:1

FROM --platform=$BUILDPLATFORM maven:3.9.9-eclipse-temurin-17 AS builder
  WORKDIR /workdir
     COPY pom.xml /workdir/pom.xml
      RUN mvn dependency:go-offline
     COPY src /workdir/src
      RUN mvn verify
      RUN mkdir -p target/extract
  WORKDIR target/extract
      RUN java -Djarmode=layertools -jar ../*.jar extract

FROM eclipse-temurin:17-jre-focal
         RUN addgroup --gid 5000 container \
             && adduser --ingroup container --uid 5000 --disabled-password container
        USER container
     WORKDIR /workdir
      EXPOSE 8080
#       VOLUME /tmp
         ARG EXTRACT_PATH=/workdir/target/extract
        COPY --from=builder --chown=container:container ${EXTRACT_PATH}/dependencies/ ./
        COPY --from=builder --chown=container:container ${EXTRACT_PATH}/spring-boot-loader/ ./
        COPY --from=builder --chown=container:container ${EXTRACT_PATH}/snapshot-dependencies/ ./
        COPY --from=builder --chown=container:container ${EXTRACT_PATH}/application/ ./
  ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
