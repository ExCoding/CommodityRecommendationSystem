package com.tanjunchen.recommender.statistics

import com.tanjunchen.recommender.common.util
import java.text.SimpleDateFormat
import java.util.Date

import org.apache.log4j.{Level, Logger}
import org.apache.spark.SparkConf
import org.apache.spark.sql.{DataFrame, SparkSession}

/**
 * @Author tanjunchen
 * @Date 2020/8/17 22:25
 * @Version 1.0
 */

case class Rating(userId: Int, productId: Int, score: Double, timestamp: Int)

case class MongoConfig(uri: String, db: String)

object StatisticsRecommender {

  // 设置日志级别
  Logger.getLogger("org").setLevel(Level.ERROR)

  def main(args: Array[String]): Unit = {
    val config = Map(
      "spark.cores" -> "local[*]",
      "mongo.uri" -> util.DataUtil.MONGO_DB_URI,
      "mongo.db" -> util.DataUtil.MONGO_DB_NAME
    )

    // 创建一个spark config
    val sparkConf = new SparkConf().setMaster(config("spark.cores")).setAppName("StatisticsRecommender")
    // 创建spark session
    val spark = SparkSession.builder().config(sparkConf).getOrCreate()

    import spark.implicits._
    implicit val mongoConfig = MongoConfig(config("mongo.uri"), config("mongo.db"))

    // 加载数据
    val ratingDF = spark.read
      .option("uri", mongoConfig.uri)
      .option("collection", util.DataUtil.MONGODB_RATING_COLLECTION)
      .format("com.mongodb.spark.sql")
      .load()
      .as[Rating]
      .toDF()

    // 创建 ratings 临时表
    ratingDF.createOrReplaceTempView("ratings")

    // TODO: 用 spark sql 去做不同的统计推荐
    // 1. 历史热门商品，按照评分个数统计，productId，count
    val rateMoreProductsDF = spark.sql("select productId,count(productId) as " +
      "count from ratings group by productId order by count desc")
    // rateMoreProductsDF.foreach(x => println(x))
    storeDFInMongoDB(rateMoreProductsDF, util.DataUtil.RATE_MORE_PRODUCTS)

    // 2. 近期热门商品，把时间戳转换成 yyyyMM 格式进行评分个数统计，最终得到 productId, count, yearmonth
    // 创建一个日期格式化工具
    val simpleDateFormat = new SimpleDateFormat("yyyyMM")
    // 注册 UDF，将 timestamp 转化为年月格式 yyyyMM
    spark.udf.register("changeDate", (x: Int) => simpleDateFormat.format(new Date(x * 1000L)).toInt)
    // 把原始 rating 数据转换成想要的结构 productId, score, yearmonth
    val ratingOfYearMonthDF = spark.sql("select productId, score, changeDate(timestamp) as yearmonth from ratings")
    ratingOfYearMonthDF.createOrReplaceTempView("ratingOfMonth")
    val rateMoreRecentlyProductsDF = spark.sql("select productId, count(productId) as count, yearmonth from ratingOfMonth group by yearmonth, productId order by yearmonth desc, count desc")
    // 把 df 保存到 mongodb
    storeDFInMongoDB(rateMoreRecentlyProductsDF, util.DataUtil.RATE_MORE_RECENTLY_PRODUCTS)

    // 3. 优质商品统计，商品的平均评分，productId，avg
    val averageProductsDF = spark.sql("select productId, avg(score) as avg from ratings group by productId order by avg desc")
    storeDFInMongoDB(averageProductsDF, util.DataUtil.AVERAGE_PRODUCTS)
    spark.stop()
  }

  def storeDFInMongoDB(df: DataFrame, collection_name: String)(implicit mongoConfig: MongoConfig): Unit = {
    println("====== 开始写入数据 ======")
    df.write
      .option("uri", mongoConfig.uri)
      .option("collection", collection_name)
      .mode("overwrite")
      .format("com.mongodb.spark.sql")
      .save()
    println("====== 写入数据成功 ======")
  }
}
