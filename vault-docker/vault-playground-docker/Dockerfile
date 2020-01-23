FROM mhus/apache-karaf:4.2.6_04
MAINTAINER Mike Hummel <mh@mhus.de>

#ENV KARAF_UID 0
ENV JAVA_MAX_MEM 2048m

USER $APP_UID
COPY target/assembly.tar.gz /docker/assembly.tar.gz
COPY profiles /docker/profiles

# Start once
RUN /docker/assembly.sh
RUN export DEPLOY_FORCE=1 && /docker/deploy.sh
USER 0