FROM maven:3-openjdk-18 AS builder
WORKDIR /build
COPY . . 
RUN mkdir -p target/frontend
RUN mvn package -Pproduction

FROM azul/zulu-openjdk-alpine:18
ENV APP_HOME=/usr/local/budget-book/
RUN mkdir -m u=rwx -p $APP_HOME
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
RUN chown appuser $APP_HOME

USER appuser
WORKDIR $APP_HOME
COPY --from=builder /build/target/budget-book.jar .
COPY deployment/config config

CMD ["java", "-jar", "budget-book.jar"]