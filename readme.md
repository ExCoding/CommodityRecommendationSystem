# 电商推荐系统

## 搭建环境

mongo

docker run --name mongo -p 27017:27017 -d mongo

docker exec -it mongo /bin/bash

```
> db.Product.findOne()
{
	"_id" : ObjectId("5f3a8fbd59c7aa3d40623527"),
	"productId" : 3982,
	"name" : "Fuhlen 富勒 M8眩光舞者时尚节能无线鼠标(草绿)(眩光.悦动.时尚炫舞鼠标 12个月免换电池 高精度光学寻迹引擎 超细微接收器10米传输距离)",
	"imageUrl" : "https://images-cn-4.ssl-images-amazon.com/images/I/31QPvUDNavL._SY300_QL70_.jpg",
	"categories" : "外设产品|鼠标|电脑/办公",
	"tags" : "富勒|鼠标|电子产品|好用|外观漂亮"
}
> db.Product.count()
96
```

Redis

docker run  -d --name redis -p 6379:6379 redis:4.0.2

docker exec -it redis /bin/bash

Zookeeper

docker run --privileged=true -d --name zookeeper --publish 2181:2181  -d  zookeeper:3.4.10

Kafka

docker run -d --name kafka --publish 9092:9092 --link zookeeper --env KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181  \
--env KAFKA_ADVERTISED_HOST_NAME=192.168.17.140 --env KAFKA_ADVERTISED_PORT=9092 --volume /etc/localtime:/etc/localtime wurstmeister/kafka:2.11-2.0.1

docker pull wurstmeister/kafka:2.11-2.1.0

docker exec -it kafka sh

cd /opt/kafka_2.11-2.0.1 && ./bin/kafka-console-producer.sh --broker-list 192.168.17.140:9092 --topic recommender 

输入：4867|457976|5.0|434345

./bin/kafka-topics.sh --list --zookeeper 192.168.17.140:2181



