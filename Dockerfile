FROM public.ecr.aws/docker/library/gradle:8.7-jdk17-alpine as builder

RUN mkdir /app
COPY . /app
RUN chmod +x /app/gradlew
WORKDIR /app

RUN apk add --no-cache curl
RUN ./gradlew clean build -x test

FROM public.ecr.aws/docker/library/amazoncorretto:17-alpine3.15-jdk

COPY --from=builder /app/build/libs/*-all.jar /app/app.jar

WORKDIR /app

ENV PORT 80

RUN apk add --no-cache curl tzdata

ENV TZ America/Sao_Paulo
ENV MINIREDIS_PORT 6379
ENV MINIREDIS_THREAD_POOL_SIZE $(nproc)

EXPOSE 6379

CMD ["java", "-jar", "app.jar"]