FROM mhus/apache-karaf:4.2.6_03
MAINTAINER Mike Hummel <mh@mhus.de>

#ENV KARAF_UID 0
ENV JAVA_MAX_MEM 2048m

COPY target/assembly.tar.gz /docker/assembly.tar.gz
COPY deploy /docker/deploy
#COPY karaf /docker/profiles/default/

# Start once
RUN /docker/environment.sh
RUN export INSTALL_FORCE=1 && /docker/install.sh

