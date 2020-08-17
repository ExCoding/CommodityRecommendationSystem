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



