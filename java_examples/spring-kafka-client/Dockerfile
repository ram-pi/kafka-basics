FROM rampi88/kafka-multitool:v1
COPY ./target/app.jar app.jar
#CMD ["java", "-javaagent:jmx_prometheus_javaagent-0.20.0.jar=9191:prometheus_config.yml", "-jar", "app.jar", "--spring.config.location", "application.properties"]
CMD ["java", "-jar", "app.jar"]