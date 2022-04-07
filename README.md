# KUBERNETES DEMO

An example of how to deploy a web-app, connected to mysql, kafka, elasticsearch and activemq in k8s.

## K3D

Create a local registry where to save web-app image. Then create k8s cluster with k3d

1. Create custom registry with k3d*

```shell 
k3d registry create myregistry.localhost --port 12345 
127.0.0.1 k3d-myregistry.localhost 
```

1. Create k8s cluster with k3d: add the registry to use and the loadbalancer mapping port (8081:80)

```shell 
k3d cluster create newcluster --registry-use k3d-myregistry.localhost:12345 --api-port 6550 -p "8081:80@loadbalancer" --agents 2 
```

*k3d installation guide at this link: https://k3d.io/v5.3.0/

## MYSQL 5.6

Create mysql cluster as explained in this page https://kubernetes.io/docs/tasks/run-application/run-single-instance-stateful-application/

1. create mysql persistence volume and mysql deployment

```shell
kubectl apply -f mysql/00-mysql-namespace.yml
kubectl -n mysql-ns apply -f mysql/01-mysql-persistence-volume.yaml
kubectl -n mysql-ns apply -f mysql/02-mysql-deployment.yaml 
kubectl -n mysql-ns get all
```

2. create a pod how to run mysql cli
```shell
kubectl run -it --rm --image=mysql:5.6 --restart=Never mysql-client -- mysql -h mysql.mysql-ns -proot
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
 mysql> exit
```
![alt text](https://github.com/orsinialberto/k8s-demo/blob/main/graph/mysql-ns.png)

## ZOOKEEPER & KAFKA WITH STRIMZI

1. create cluster kafka

```shell
kubectl apply --namespace=kafka-ns -R -f kafka
```

Example of command: 

```shell
kubectl -n kafka-ns get all

kubectl -n kafka-ns run kafka-producer -ti --image=strimzi/kafka:0.17.0-kafka-2.4.0 --rm=true --restart=Never -- bin/kafka-console-producer.sh --broker-list my-cluster-kafka-bootstrap:9092 --topic my-topic

kubectl -n kafka-ns run kafka-consumer -ti --image=strimzi/kafka:0.17.0-kafka-2.4.0 --rm=true --restart=Never -- bin/kafka-console-consumer.sh --bootstrap-server my-cluster-kafka-bootstrap:9092 --topic my-topic --from-beginning
```
![alt text](https://github.com/orsinialberto/k8s-demo/blob/main/graph/kafka-ns.png)

## ELASTICSEARCH 7.6.2

Create elasticsearch cluster as explained in this page https://www.elastic.co/guide/en/cloud-on-k8s/current/k8s-deploy-eck.html

1. Create elasticsearch cluster
   
```shell

# create operator

kubectl create -f https://download.elastic.co/downloads/eck/2.1.0/crds.yaml
kubectl apply -f https://download.elastic.co/downloads/eck/2.1.0/operator.yaml
kubectl -n elastic-system logs -f statefulset.apps/elastic-operator  

# create cluster

kubectl apply -f elasticsearch/01_namespace.yml
kubectl -n elasticsearch-ns apply -f elasticsearch/02_elasticsearch.yml
kubectl -n elasticsearch-ns get elasticsearch

# generate certificate for web app

kubectl -n elasticsearch-ns exec -it elasticsearch-es-default-0 -- /bin/bash
./bin/elasticsearch-certutil cert --ca-cert /usr/share/elasticsearch/config/http-certs/ca.crt --ca-key /usr/share/elasticsearch/config/http-certs/tls.key cert --out /tmp/elastic-certificates.p12 --pass ""
exit

# update web-app config with certificate and password

kubectl cp elasticsearch-ns/elasticsearch-es-default-0:/tmp/elastic-certificates.p12 elastic-certificates.p12
mv elastic-certificates.p12 app/k3d-web-app/elasticsearch/
kubectl -n elasticsearch-ns get secret elasticsearch-es-elastic-user -o go-template='{{.data.elastic | base64decode}}'

# copy password in web-app application.yml (property elasticsearch.password)
# create an index called 'customer' which is necessary for web-app execution
```

2. Create Kibana client

```shell
kubectl -n elasticsearch-ns apply -f elasticsearch/kibana.yml
kubectl -n elasticsearch-ns port-forward service/kibana-kb-http 5601
kubectl -n elasticsearch-ns get secret elasticsearch-es-elastic-user -o=jsonpath='{.data.elastic}' | base64 --decode; echo
```

![alt text](https://github.com/orsinialberto/k8s-demo/blob/main/graph/elasticsearch-ns.png)

## ACTIVE MQ 5.15.9

1. Create cluster ActiveMq

```shell
kubectl apply -f activemq/activemq.yaml

kubectl -n activemq-ns  get all
```
![alt text](https://github.com/orsinialberto/k8s-demo/blob/main/graph/activemq-ns.png)

## WEB-APP

1. create image of the java project

```shell
# create package of java project
cd app/k3d-web-app/
mvn clean package -DskipTests=true

# build docker image
docker build -t  aorsini/web-app . 

# tag docker image 
docker tag aorsini/web-app:latest localhost:12345/web-app:latest 

# push image to the repository
docker push localhost:12345/web-app:latest 
```

2. create web-app cluster

```shell 
# create a namespace
# create web-app pod
# expose web-app on port 8080 in cluster with service
# expose web-app on port 80 out of the cluster with ingress (k3d map port 8081:80)
cd -
kubectl apply -f app/k8s/web-app.yaml 

kubectl -n web-app-ns get all
kubectl -n web-app-ns get ingress
``` 

3.  try to use web application

```shell 
curl localhost:8081/speak-out
curl localhost:8081/actuator/health
curl localhost:8081/customers 
```
![alt text](https://github.com/orsinialberto/k8s-demo/blob/main/graph/web-app-ns.png)

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
