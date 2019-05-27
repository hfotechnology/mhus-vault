====
    Copyright 2018 Mike Hummel

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


cherryVersion=1.0.2-SNAPSHOT
feature:repo-add activemq 5.12.1
feature:repo-add mvn:de.mhus.cherry.vault/vault-feature/$cherryVersion/xml/features

feature:install cherry-vault

#cp .../etc/mongo-ds.xml deploy/
cp .../etc/datasource* deploy/
cp .../etc/api-access.xml deploy/
cp .../etc/api-access-sources.xml deploy/
cp .../etc/aaa/grouppmapping/* sop/aaa/groupmapping

If you need a user:
cp .../etc/aaa/accounts/* sop/aaa/accounts



Examples
=========

crypt:cipher -ip -is -s CherryVaultLocalSource -d "Test Rsa Key" RSA-BC-01 create


Create test group and target:

crypt:cipher -ip -is -s CherryVaultLocalSource -d "Test Rsa Key" RSA-BC-01 create|cut -m array -f Ident|setvar rsaPrivId rsaPubId
crypt:signer -ip -is -s CherryVaultLocalSource DSA-BC-01 create|cut -m array -f Ident|setvar dsaPrivId dsaPubId
xdb:create VaultGroup name=test secretgeneratorname=password allowupdate=true targets.add=test writeacl.add=* enabled=true maximportlength=100
xdb:create VaultTarget name=test conditionnames=true processorname=cipher.rsa processorconfig.keyId=$rsaPubId processorconfig.signId=$dsaPrivId processorconfig.signService=DSA-BC-01 readacl.add=*


cvc create test

Result: dbc02d77-0e70-4e30-9a28-110f55300e11

service:invoke de.mhus.cherry.vault.api.CherryVaultApi getSecret dbc02d77-0e70-4e30-9a28-110f55300e11 test

or

http://localhost:8181/rest/vault/dbc02d77-0e70-4e30-9a28-110f55300e11:test

Create:
http://localhost:8181/rest/vault?_method=POST&_group=test

Update:
http://localhost:8181/rest/vault?_method=PUT&_secretId=dfc3fbb9-253b-43bd-9ff8-95ec109518be

Delete:
http://localhost:8181/rest/vault?_method=DELETE&_secretId=dfc3fbb9-253b-43bd-9ff8-95ec109518be


view VaultEntry 5a6258d7a7afa2b2303f18c7



Cleanup:

xdb:delete VaultGroup "()"
xdb:delete VaultTarget "()"
xdb:delete VaultEntry "()"
xdb:delete VaultKey "()"
