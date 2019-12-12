FROM mhus/apache-karaf:4.2.6_02
MAINTAINER Mike Hummel <mh@mhus.de>

#ENV KARAF_UID 0
ENV JAVA_MAX_MEM 2048m

ADD target/vault-assembly.tar.gz /docker/assembly.tar.gz
COPY deploy /docker/deploy
#COPY karaf /opt/karaf/

#RUN /docker/install.sh

