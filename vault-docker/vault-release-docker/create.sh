#!/bin/bash
#
# Copyright 2018 Mike Hummel
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

if [ "$1" = "clean" ]; then
    docker build --no-cache -t vault-playground-stage1 .
else
    docker build -t vault-playground-stage1 .
fi  

docker rm vault-playground-stage1
docker run --name vault-playground-stage1 -e PREVENT_ENVIRONMENT=1 -v ~/.m2:/home/user/.m2 vault-playground-stage1
docker commit vault-playground-stage1 vault-playground

