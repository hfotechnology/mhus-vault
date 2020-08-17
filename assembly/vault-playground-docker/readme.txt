====
    Copyright (C) 2020 Mike Hummel (mh@mhus.de)

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

            http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
====

Create docker image:

mvn dockerfile:build


Publish docker image:

docker push "mhus/vault-playground:1.6.3-SNAPSHOT"


Create Docker playground as single node with in memory db:

docker run -it --name vault-playground \
 -h vault \
 -v ~/.m2:/home/user/.m2 \
 -p 8182:8181 \
 -p 15006:5005 \
 mhus/vault-playground:6.3.1-SNAPSHOT debug

docker rm vault-playground

