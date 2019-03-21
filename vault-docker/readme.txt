
cd vault-playground
./create.sh

docker tag vault-playground "mhus/vault-playground:1.0.2"
docker tag vault-playground "mhus/vault-playground:latest"

docker push "mhus/vault-playground:1.0.2"
docker push "mhus/vault-playground:latest"


docker run -it --name vault -v ~/.m2:/home/user/.m2 -p 8181:8181 vault-playground