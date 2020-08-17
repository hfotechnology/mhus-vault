#
# Copyright (C) 2020 Mike Hummel (mh@mhus.de)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#         http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

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