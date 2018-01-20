

install -s mvn:de.mhus.osgi/mhu-osgi-crypt-api/1.3.2-SNAPSHOT
install -s mvn:de.mhus.osgi/mhu-osgi-crypt-bc/1.3.2-SNAPSHOT
install -s mvn:de.mhus.osgi/mhu-karaf-crypt/1.3.2-SNAPSHOT

install -s mvn:de.mhus.cherry.vault/cherry-vault-api/1.0.0-SNAPSHOT
install -s mvn:de.mhus.cherry.vault/cherry-vault-impl/1.0.0-SNAPSHOT


Example:

crypta:cipher -i -s CherryVaultLocalSource -d "Test Rsa Key" RSA-1 create
Public key Id: f62af2f1-7acc-4b2e-9d77-1ea74c656953

crypta:signer -i -s CherryVaultLocalSource DSA-1 create
Private Key Id: c49ac4bb-8379-4956-a278-a426af19e829

xdb:create VaultGroup name=test secretgeneratorname=password allowupdate=true targets.add=test writeacl.add=*
xdb:create VaultTarget name=test conditionnames=true processorname=cipher.rsa processorconfig.keyId=f62af2f1-7acc-4b2e-9d77-1ea74c656953 processorconfig.signId=c49ac4bb-8379-4956-a278-a426af19e829 processorconfig.sign=DSA-1 readacl.add=*

service:invoke de.mhus.cherry.vault.api.CherryVaultApi createSecret test
Result: dbc02d77-0e70-4e30-9a28-110f55300e11

service:invoke de.mhus.cherry.vault.api.CherryVaultApi getSecret dbc02d77-0e70-4e30-9a28-110f55300e11 test







view VaultEntry 5a6258d7a7afa2b2303f18c7



Cleanup:

xdb:delete VaultGroup "()"
xdb:delete VaultTarget "()"
xdb:delete VaultEntry "()"
