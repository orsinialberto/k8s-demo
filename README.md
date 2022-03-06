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
kubectl apply -f persistence-volume/mysql-persistence-volume.yaml
kubectl apply -f deployment/mysql-deployment.yaml
```

2. create a pod how to run mysql command
```shell
kubectl run -it --rm --image=mysql:5.6 --restart=Never mysql-client -- mysql -h mysql -ppassword
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

## ZOOKEEPER & KAFKA

1. create zookeeper deployment

```shell
kubectl apply -f deployment/zookeeper-deployment.yaml
```

2. create zookeeper service

```shell
kubectl apply -f service/zookeeper-service.yaml
```

3. create kafka load balancer

```shell
kubectl apply -f service/kafka-service.yaml
```

4. create kafka deployment (note KAFKA_ADVERTISED_HOST_NAME & KAFKA_ADVERTISED_PORT should be equal to loadbalancer url and port)

```shell
kubectl apply -f deployment/kafka-deployment.yaml
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
cd ../..
kubectl apply -f deployment/k3d-deployment.yaml 
kubectl get pods -o wide
``` 

6.  apply k8s service file

```shell
kubectl apply -f service/k3d-service.yaml
kubectl describe svc demo-albe
```

7. apply k8s ingress

```shell
kubectl apply -f ingress/k3d-ingress.yaml
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