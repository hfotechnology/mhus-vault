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

rm -r repository/*

for f in $(cd  ~/.m2/repository;find . -type d -name \*SNAPSHOT\*)
do 
  echo Import $f
  mkdir -p repository/$f
  cp -r ~/.m2/repository/$f/* repository/$f
done

if [ "$1" = "clean" ]; then
	docker build --no-cache -t vault-playground .
else
	docker build -t vault-playground .
fi	
