package com.tanjunchen.recommender.content

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import com.tanjunchen.recommender.common.util
import org.apache.spark.ml.feature.{HashingTF, IDF, Tokenizer}
import org.apache.spark.ml.linalg.SparseVector
import org.jblas.DoubleMatrix

/**
 * @Author tanjunchen
 * @Date 2020/8/27 21:36
 * @Version 1.0
 */

case class Product(productId: Int, name: String, imageUrl: String, categories: String, tags: String)

case class MongoConfig(uri: String, db: String)

// 定义标准推荐对象
case class Recommendation(productId: Int, score: Double)

// 定义商品相似度列表
case class ProductRecs(productId: Int, recs: Seq[Recommendation])

object ContentRecommender {
  def main(args: Array[String]): Unit = {
    val config = Map(
      "spark.cores" -> "local[*]",
      "mongo.uri" -> util.DataUtil.MONGO_DB_URI,
      "mongo.db" -> util.DataUtil.MONGO_DB_NAME
    )
    // 创建一个spark config
    val sparkConf = new SparkConf().setMaster(config("spark.cores")).setAppName("ContentRecommender")
    // 创建spark session
    val spark = SparkSession.builder().config(sparkConf).getOrCreate()

    import spark.implicits._
    implicit val mongoConfig = MongoConfig(config("mongo.uri"), config("mongo.db"))

    // 载入数据，做预处理
    val productTagsDF = spark.read
      .option("uri", mongoConfig.uri)
      .option("collection", util.DataUtil.MONGODB_PRODUCT_COLLECTION)
      .format("com.mongodb.spark.sql")
      .load()
      .as[Product]
      .map(
        x => (x.productId, x.name, x.tags.map(c => if (c == '|') ' ' else c))
      )
      .toDF("productId", "name", "tags")
      .cache()

    // TODO: 用TF-IDF提取商品特征向量
    // 1. 实例化一个分词器，用来做分词

    var tokenizer = new Tokenizer().setInputCol("tags").setOutputCol("words")
    // 使用分词器做转换 得到一个新的 words 的列 DF
    val wordsDataDF = tokenizer.transform(productTagsDF)
    // wordsDataDF.show()
    // +---------+--------------------+--------------------+--------------------+
    //|productId|                name|                tags|               words|
    //+---------+--------------------+--------------------+--------------------+
    //|   259637|                小狗钱钱|书 少儿图书 教育类 童书 不错 ...|[书, 少儿图书, 教育类, 童书...|
    //|     3982|Fuhlen 富勒 M8眩光舞者时...|  富勒 鼠标 电子产品 好用 外观漂亮|[富勒, 鼠标, 电子产品, 好用...|
    //|   260348|西尔斯亲密育儿百科(全球最权威最受...|书 育儿类 教育类 不错 内容丰富...|[书, 育儿类, 教育类, 不错,...|
    //|     6797|PHILIPS飞利浦HQ912/1...|飞利浦 剃须刀 家用电器 好用 外观漂亮|[飞利浦, 剃须刀, 家用电器, ...|
    //|   275707|             猜猜我有多爱你|书 教育类 不错 内容丰富 少儿图...|[书, 教育类, 不错, 内容丰富...|
    //|   286997|         千纤草黄瓜水500ml|化妆品 面部护理 千纤草 到货速度...|[化妆品, 面部护理, 千纤草, ...|
    //|     8195|Kingston 金士顿 Clas...|存储卡 金士顿 SD卡 容量挺大的...|[存储卡, 金士顿, sd卡, 容...|
    //|   294209|           不畏将来 不念过去|书 军事类 政治类 好看 有破损 ...|[书, 军事类, 政治类, 好看,...|
    //|    13316|Kingston 金士顿 Data...| 优盘 金士顿 好用 容量挺大的 速度快|[优盘, 金士顿, 好用, 容量挺...|
    //|   300265|Edifier漫步者 H180 耳...|耳机 耳塞式耳机 电子产品 漫步者...|[耳机, 耳塞式耳机, 电子产品,...|
    //|    13543|            蔡康永的说话之道|  蔡康永 写的真好 不贵 内容不错 书|[蔡康永, 写的真好, 不贵, 内...|
    //|   302217|Elizabeth Arden伊丽...|化妆品 伊丽莎白 香水 好用 很香...|[化妆品, 伊丽莎白, 香水, 好...|
    //|   314081|    时寒冰说:经济大棋局,我们怎么办|书 军事类 政治类 时寒冰 经管类...|[书, 军事类, 政治类, 时寒冰...|
    //|   323519|Rapoo 雷柏 1090光学鼠标...|电子产品 鼠标 外设 质量好 雷柏...|[电子产品, 鼠标, 外设, 质量...|
    //|    14103|SanDisk 闪迪 microS...|存储卡 SD卡 容量挺大的 闪迪 ...|[存储卡, sd卡, 容量挺大的,...|
    //|   326582|Mentholatum曼秀雷敦男士...|化妆品 男士 曼秀雷敦 好用 用起...|[化妆品, 男士, 曼秀雷敦, 好...|
    //|   333125|                素年锦时|书 有声读物 青春文学 文学 小说...|[书, 有声读物, 青春文学, 文...|
    //|    21643|L'OREAL PARIS巴黎欧莱...|    好用 护肤品 到货速度快 欧莱雅|[好用, 护肤品, 到货速度快, ...|
    //|   352021|Lenovo 联想 A820T T...|联想 手机 质量好 联想手机还不错...|[联想, 手机, 质量好, 联想手...|
    //|    32512|乐扣乐扣(lock&lock) 茶...|水壶 好用 乐扣乐扣 到货速度快 质量好|[水壶, 好用, 乐扣乐扣, 到货...|
    //+---------+--------------------+--------------------+--------------------+
    //only showing top 20 rows

    // 2. 定义一个HashingTF工具 计算TF
    val hashingTF = new HashingTF().setInputCol("words").setOutputCol("rawFeatures").setNumFeatures(300)
    val featurizedDataDF = hashingTF.transform(wordsDataDF)

    // 3. 定义一个IDF工具,计算 TF-IDF
    val idf = new IDF().setInputCol("rawFeatures").setOutputCol("features")
    // 训练一个idf模型
    val idfModel = idf.fit(featurizedDataDF)
    // 得到增加新列features的DF
    val rescaledDataDF = idfModel.transform(featurizedDataDF)

    // 对数据进行转换，得到RDD形式的features
    val productFeatures = rescaledDataDF.map {
      row => (row.getAs[Int]("productId"), row.getAs[SparseVector]("features").toArray)
    }
      .rdd
      .map {
        case (productId, features) => (productId, new DoubleMatrix(features))
      }
    // 两两配对商品  计算余弦相似度
    val productRecs = productFeatures.cartesian(productFeatures)
      .filter {
        case (a, b) => a._1 != b._1
      }
      // 计算余弦相似度
      .map {
        case (a, b) =>
          val simScore = consinSim(a._2, b._2)
          (a._1, (b._1, simScore))
      }
      .filter(_._2._2 > 0.4)
      .groupByKey()
      .map {
        case (productId, recs) =>
          ProductRecs(productId, recs.toList.sortWith(_._2 > _._2).map(x => Recommendation(x._1, x._2)))
      }
      .toDF()
    productRecs.write
      .option("uri", mongoConfig.uri)
      .option("collection", util.DataUtil.CONTENT_PRODUCT_RECS)
      .mode("overwrite")
      .format("com.mongodb.spark.sql")
      .save()

    spark.stop()
  }

  def consinSim(product1: DoubleMatrix, product2: DoubleMatrix): Double = {
    product1.dot(product2) / (product1.norm2() * product2.norm2())
  }

}
