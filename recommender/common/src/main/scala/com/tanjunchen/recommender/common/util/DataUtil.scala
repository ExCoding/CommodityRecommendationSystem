package com.tanjunchen.recommender.common.util

/**
 * @Author tanjunchen
 * @Date 2020/8/17 21:31
 * @Version 1.0
 */
object DataUtil {

  // 定义数据文件路径
  val PRODUCT_DATA_PATH = "data/products.csv"
  val RATING_DATA_PATH = "data/ratings.csv"

  // 定义 mongodb 中存储的表名
  val MONGODB_PRODUCT_COLLECTION = "Product"
  val MONGODB_RATING_COLLECTION = "Rating"

  // Mongo Uri
  val MONGO_DB_URI = "mongodb://192.168.17.140:27017/recommender"
  val MONGO_DB_NAME = "recommender"

  // 业务表
  // 历史热门统计表
  val RATE_MORE_PRODUCTS = "RateMoreProducts"
  // 近期热门统计表
  val RATE_MORE_RECENTLY_PRODUCTS = "RateMoreRecentlyProducts"
  // 评分很高的统计表
  val AVERAGE_PRODUCTS = "AverageProducts"

  // 离线推荐服务
  val USER_RECS = "UserRecs"
  val PRODUCT_RECS = "ProductRecs"
  val USER_MAX_RECOMMENDATION = 20

  // 实时推荐服务

  val STREAM_RECS = "StreamRecs"
  val STREAM_PRODUCT_RECS = "StreamProductRecs"
  val MAX_USER_RATING_NUM = 20
  val MAX_SIM_PRODUCTS_NUM = 20

  // kafka topic
  val KAFKA_TOPIC = "recommender"
  val KAFKA_BOOTSTRAP_SERVER = "192.168.17.140:9092"
  val KAFKA_GROUP_ID = "recommender"

  // redis url
  val REDIS_URL = "192.168.17.140"
}
