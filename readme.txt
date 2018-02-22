
cherryVersion=1.0.1-SNAPSHOT
feature:repo-add mvn:de.mhus.cherry.vault/cherry-vault-feature/$cherryVersion/xml/features

feature:install cherry-vault

cp .../etc/mongo-ds.xml deploy/
cp .../etc/api-access.xml deploy/
cp .../etc/api-access-sources.xml deploy/
cp .../etc/aaa/grouppmapping/* sop/aaa/groupmapping

If you need a user:
cp .../etc/aaa/accounts/* sop/aaa/accounts

Example:

crypta:cipher -i -s CherryVaultLocalSource -d "Test Rsa Key" RSA-1 create
Public key Id: f62af2f1-7acc-4b2e-9d77-1ea74c656953

crypta:signer -i -s CherryVaultLocalSource DSA-1 create
Private Key Id: c49ac4bb-8379-4956-a278-a426af19e829

xdb:use -g -s cherryvault save

xdb:create VaultGroup name=test secretgeneratorname=password allowupdate=true targets.add=test writeacl.add=* enabled=true maximportlength=100
xdb:create VaultTarget name=test conditionnames=true processorname=cipher.rsa processorconfig.keyId=f62af2f1-7acc-4b2e-9d77-1ea74c656953 processorconfig.signId=c49ac4bb-8379-4956-a278-a426af19e829 processorconfig.signService=DSA-1 readacl.add=*

service:invoke de.mhus.cherry.vault.api.CherryVaultApi createSecret test
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
