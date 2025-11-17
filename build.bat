docker rmi docker.io/denchiquero/payment-gateway:latest
docker rmi docker.io/denchiquero/payment-service:latest
docker rmi docker.io/denchiquero/order-service:latest
docker rmi docker.io/denchiquero/frontend:latest
docker rmi docker.io/denchiquero/report-service:latest

docker build -t denchiquero/payment-gateway:latest ./payment-gateway 
docker push denchiquero/payment-gateway:latest
docker build -t denchiquero/order-service:latest ./order-service 
docker push denchiquero/order-service:latest
docker build -t denchiquero/payment-service:latest ./payment-service 
docker push denchiquero/payment-service:latest
docker build -t denchiquero/frontend:latest ./frontend 
docker push denchiquero/frontend:latest
docker build -t denchiquero/report-service:latest ./report-service 
docker push denchiquero/report-service:latest