
cd vault-playground
./create.sh

docker tag vault-playground "mhus/vault-playground:1.0.1"
docker tag vault-playground "mhus/vault-playground:latest"

docker push "mhus/vault-playground:1.0.1"
docker push "mhus/vault-playground:latest"
