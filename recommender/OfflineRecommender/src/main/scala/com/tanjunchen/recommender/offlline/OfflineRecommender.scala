package com.tanjunchen.recommender.offlline

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import com.tanjunchen.recommender.common.util


/**
 * @Author tanjunchen
 * @Date 2020/8/17 23:31
 * @Version 1.0
 */

case class ProductRating(userId: Int, productId: Int, score: Double, timestamp: Int)

case class MongoConfig(uri: String, db: String)

// 定义标准推荐对象
case class Recommendation(productId: Int, score: Double)

// 定义用户的推荐表
case class UserRecs(userId: Int, recs: Seq[Recommendation])

// 定义商品相似度表
case class ProductRecs(productId: Int, recs: Seq[Recommendation])


object OfflineRecommender {

  def main(args: Array[String]): Unit = {
    val config = Map(
      "spark.cores" -> "local[*]",
      "mongo.uri" -> "mongodb://localhost:27017/recommender",
      "mongo.db" -> "recommender"
    )
    // 创建一个spark config
    val sparkConf = new SparkConf().setMaster(config("spark.cores")).setAppName("OfflineRecommender")
    // 创建spark session
    val spark = SparkSession.builder().config(sparkConf).getOrCreate()

    import spark.implicits._
    implicit val mongoConfig = MongoConfig(config("mongo.uri"), config("mongo.db"))

    // 加载数据
    val ratingRDD = spark.read
      .option("uri", mongoConfig.uri)
      .option("collection", util.DataUtil.MONGODB_RATING_COLLECTION)
      .format("com.mongodb.spark.sql")
      .load()
      .as[ProductRating]
      .rdd
      .map(
        rating => (rating.userId, rating.productId, rating.score)
      ).cache()

    

  }
}
