# KUBERNETES DEMO

A simple example of how to deploy a web-app, connected to mysql, in k8s.

## K3D

1. create custom registry with k3d*

```shell 
k3d registry create myregistry.localhost --port 12345 
```

2. to use k3d registry into k3d cluster add this to /etc/hosts

```shell 
127.0.0.1 k3d-myregistry.localhost 
``` 

3. create k8s cluster with k3d

```shell 
k3d cluster create newcluster --registry-use k3d-myregistry.localhost:12345 --api-port 6550 -p "8081:80@loadbalancer" --agents 2 
```

*k3d installation guide at this link: https://k3d.io/v5.3.0/

## MYSQL 

1. create mysql persistence volume

```shell
kubectl apply -f mysql/00-mysql-namespace.yml
kubectl apply -f mysql/01-mysql-persistence-volume.yaml -n mysql
kubectl apply -f mysql/02-mysql-deployment.yaml -n mysql  
```

2. create a pod how to run mysql command
```shell
kubectl run -it --rm --image=mysql:5.6 --restart=Never mysql-client -- mysql -h mysql.mysql -ppassword
```

3. create schema and table necessary to run web-app

```shell
mysql> CREATE DATABASE k3d_demo_schema;
mysql> USE k3d_demo_schema;
mysql> CREATE TABLE `customer` (
  `id` varchar(255) NOT NULL,
  `base` text,
  `registered_at` datetime(3) DEFAULT NULL,
  `updated_at` datetime(3) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
   PRIMARY KEY (`id`)
 ) ENGINE=InnoDB;
```

## ZOOKEEPER & KAFKA WITH STRIMZI

1. create cluster kafka

```shell
kubectl apply --namespace=kafka -R -f kafka
```

If this error appears:

error: unable to recognize "deploy/mykafka.yaml": no matches for kind "Kafka" in version "kafka.strimzi.io/v1beta2"

follow this command:

```shell
cd ~/Downloads
wget https://github.com/strimzi/strimzi-kafka-operator/releases/download/0.22.1/strimzi-0.22.1.tar.gz
tar xzvf strimzi-0.22.1.tar.gz
cd ./strimzi-0.22.1/install

kubectl replace -f ./cluster-operator/
kubectl replace -f ./strimzi-admin/
kubectl replace -f ./topic-operator/
kubectl replace -f ./user-operator/

# is there `v1beta2` support?
kubectl get crd kafkas.kafka.strimzi.io -o jsonpath="{.spec.versions[*].name}{'\n'}"
v1beta2 v1beta1 v1alpha1

wget https://github.com/strimzi/strimzi-kafka-operator/releases/download/0.22.1/api-conversion-0.22.1.tar.gz
tar xzvf api-conversion-0.22.1.tar.gz
cd ./api-conversion-0.22.1

# convert existing Strimzi managed resources to `v1beta2`.
# don't worry if some error appears
bin/api-conversion.sh convert-resource --all-namespaces

# Upgrading CRDs to v1beta2
# don't worry if some error appears
bin/api-conversion.sh crd-upgrade --debug

kubectl delete namespace kafka

kubectl apply --namespace=kafka -R -f kafka
```

Example of command: 

```shell
kubectl get all -n kafka

kubectl -n kafka run kafka-producer -ti --image=strimzi/kafka:0.17.0-kafka-2.4.0 --rm=true --restart=Never -- bin/kafka-console-producer.sh --broker-list my-cluster-kafka-bootstrap:9092 --topic my-topic

kubectl -n kafka run kafka-consumer -ti --image=strimzi/kafka:0.17.0-kafka-2.4.0 --rm=true --restart=Never -- bin/kafka-console-consumer.sh --bootstrap-server my-cluster-kafka-bootstrap:9092 --topic my-topic --from-beginning
```

## WEB-APP

1. create package of java project

```shell
cd app/k3d-web-app/
mvn clean package -DskipTests=true
```

2. build docker image

```shell
docker build -t  aorsini/web-app . 
```

3. tag docker image as follow

```shell 
docker tag aorsini/web-app:latest localhost:12345/web-app:latest 
```

4. push image to custom repository

```shell 
docker push localhost:12345/web-app:latest 
```

5. apply k8s deployment file

```shell 
cd -
kubectl apply -f web-app/01-demo-albe-deployment.yaml 
kubectl get pods -o wide
``` 

6.  apply k8s service file

```shell
kubectl apply -f web-app/02-demo-albe-service.yaml
kubectl describe svc demo-albe
```

7. apply k8s ingress

```shell
kubectl apply -f web-app/03-demo-albe-ingress.yaml
kubectl describe ingress demo-albe
```
8.  try to use web application

```shell 
curl localhost:8081/speak-out
curl localhost:8081/actuator/health
curl localhost:8081/customers 
```

## DASHBOARD

1. deploy the dashboard UI

```shell
kubectl apply -f https://raw.githubusercontent.com/kubernetes/dashboard/v2.5.0/aio/deploy/recommended.yaml
kubectl proxy
```

2. grant a token

```shell
kubectl create -f dashboard/sa-dashboard.yml -f dashboard/crb-dashboard.yml
kubectl -n kubernetes-dashboard describe secret admin-user-token | grep '^token'
```

3. visit the dashboard

```shell
http://localhost:8001/api/v1/namespaces/kubernetes-dashboard/services/https:kubernetes-dashboard:/proxy/#/workloads?namespace=default
```