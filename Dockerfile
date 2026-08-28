FROM europe-north1-docker.pkg.dev/cgr-nav/pull-through/nav.no/jre:openjdk-21@sha256:e0c34ea2a1c7d4e6e8a3f584aebb2ea412ee6845a6cac5b02061bba472707066
ENV TZ="Europe/Oslo"
COPY build/libs/app.jar app.jar
CMD ["-jar","app.jar"]