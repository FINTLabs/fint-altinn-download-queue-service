FROM gradle:6.7.0-jdk8 as builder
USER root
COPY . .
RUN gradle --no-daemon build

FROM gcr.io/distroless/java:8
ENV JAVA_TOOL_OPTIONS -XX:+ExitOnOutOfMemoryError
WORKDIR /app
COPY --from=builder /home/gradle/src/main/resources/*.wsdl /app/
COPY --from=builder /home/gradle/build/libs/fint-altinn-download-queue-service.jar /app/fint-altinn-download-queue-service.jar
CMD ["/app/fint-altinn-download-queue-service.jar"]
