# k8s-demo

1. create package of java project

```shell
mvn clean package 
```

2. build docker image

```shell
docker build -t  aorsini/web-app . 
```

3. create custom registry with k3d*

```shell 
k3d registry create myregistry.localhost --port 12345 
```

4. to use k3d registry into k3d cluster add this to /etc/hosts

```shell 
127.0.0.1 k3d-myregistry.localhost 
```

5. create k8s cluster with k3d

```shell 
k3d cluster create newcluster --registry-use k3d-myregistry.localhost:12345 --api-port 6550 -p "8081:80@loadbalancer" --agents 2 
```

6. tag docker image as follow

```shell 
docker tag aorsini/web-app:latest localhost:12345/web-app:latest 
```

7. push image to custom repository

```shell 
docker push localhost:12345/{NOME_IMMAGINE}:latest 
```

8. apply k8s deployment file

```shell 
kubectl apply -f k3d-deployment.yaml 
kubectl get pods -o wide
``` 

9.  apply k8s service file

```shell
kubectl apply -f k3d-service.yaml
kubectl describe svc demo-albe
```

10. apply k8s ingress

```shell
kubectl apply -f k3d-ingress.yaml
kubectl describe ingress demo-albe
```

11. try to use web application

```shell 
curl localhost:8081/speak-out 
```


*k3d installation guide at this link: https://k3d.io/v5.3.0/