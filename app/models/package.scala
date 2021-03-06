import java.io.File
import java.nio.charset.{Charset, CodingErrorAction}

import org.joda.time.Interval
import play.api.libs.json._

package object models {

  trait Scale {
    def scale: Int
  }

  abstract class ChoroplethScale(override val scale: Int) extends Scale

  case object Neighbor extends ChoroplethScale(1)

  case object Boro extends ChoroplethScale(2)

  abstract class TimeScale(override val scale: Int) extends Scale

  case object Hour extends TimeScale(1)

  case object Day extends TimeScale(2)

  case object Week extends TimeScale(3)

  case object Month extends TimeScale(4)


  abstract class GeoCellScale(override val scale: Int) extends Scale

  case object Geo0001x extends GeoCellScale(1)

  case object Geo001x extends GeoCellScale(2)

  case object Geo01x extends GeoCellScale(3)

  case object Geo1x extends GeoCellScale(4)

  object Formatter {

    implicit val rectangularFormat: Format[Rectangle] = Json.format[Rectangle]

    implicit val intervalFormat: Format[Interval] = {
      new Format[Interval] {

        override def reads(json: JsValue): JsResult[Interval] = {
          JsSuccess(new Interval((json \ "start").as[Long], (json \ "end").as[Long]))
        }

        override def writes(interval: Interval): JsValue = {
          JsObject(Seq(("start", JsNumber(interval.getStartMillis)), ("end", JsNumber(interval.getEndMillis))))
        }

      }
    }

    implicit val spatialTimeScaleFormat: Format[MapTimeScale] = {
      new Format[MapTimeScale] {
        override def reads(json: JsValue): JsResult[MapTimeScale] = {
          val time = (json \ "time").as[String] match {
            case "hour" => Hour
            case "day" => Day
            case _ => ???
          }
          val geo = (json \ "map").as[String] match {
            case "boro" => Boro
            case "neighbor" => Neighbor
            case _ => ???
          }
          JsSuccess(MapTimeScale(geo, time))
        }

        override def writes(o: MapTimeScale): JsValue = {
          JsObject(Seq(
            "map" -> JsString(o.spatial match {
                                case Boro => "boro"
                                case Neighbor => "neighbor"
                              }),
            "time" -> JsString(o.time match {
                                 case Day => "day"
                                 case Hour => "hour"
                               })
          ))
        }
      }
    }

    implicit val signalQueryFormat: Format[MapQuery] = {
      new Format[MapQuery] {
        override def reads(json: JsValue): JsResult[MapQuery] = {
          val qType = (json \ "queryType").as[String] match {
            case "Signal" => QueryType.Signal
            case "AppUsage" => QueryType.AppUsage
          }
          val area = (json \ "area").as[Rectangle]
          val time = (json \ "time").as[Interval]
          val scale = (json \ "scale").as[MapTimeScale]
          JsSuccess(MapQuery(qType, scale, area, time))
        }

        override def writes(o: MapQuery): JsValue = ???
      }
    }
  }

}
