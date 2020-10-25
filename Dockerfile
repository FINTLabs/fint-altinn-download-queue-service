FROM gradle:6.7.0-jdk8 as builder
USER root
COPY . .
ARG apiVersion
RUN gradle --no-daemon -PapiVersion=${apiVersion} build

FROM gcr.io/distroless/java:8
ENV JAVA_TOOL_OPTIONS -XX:+ExitOnOutOfMemoryError
COPY --from=builder /home/gradle/src/main/resources/*.wsdl /data/
COPY --from=builder /home/gradle/build/libs/fint-altinn-download-queue-service-*.jar /data/fint-altinn-download-queue-service.jar
CMD ["/data/fint-altinn-download-queue-service.jar"]

