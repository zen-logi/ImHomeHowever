FROM amazoncorretto:21-alpine

# AWS Lambda Web Adapter
COPY --from=public.ecr.aws/awsguru/aws-lambda-adapter:0.9.1 /lambda-adapter /opt/extensions/lambda-adapter

# Lambda Web Adapter 設定
ENV PORT=8080
ENV AWS_LWA_READINESS_CHECK_PATH=/

WORKDIR /app

COPY build/libs/*-all.jar app.jar

CMD ["java", "-jar", "app.jar"]