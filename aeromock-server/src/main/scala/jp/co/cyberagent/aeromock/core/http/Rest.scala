package jp.co.cyberagent.aeromock.core.http

import io.netty.handler.codec.http.HttpMethod
import org.apache.commons.lang3.StringUtils

case class Endpoint(raw: String) {

  def value: String = raw.replace("\\", "/")

  def last: String = value.split("/").last

}


class ParsedRequest(
  val url: String,
  val queryParameters: Map[String, String],
  val formData: Map[String, String],
  val method: HttpMethod = HttpMethod.GET
)

object ParsedRequest {

  def apply(url: String, queryParameters: Map[String, String],
        formData: Map[String, String], method: HttpMethod = HttpMethod.GET): ParsedRequest = {
    require(url != null)

    val s = url.trim
    val formatted = if (StringUtils.isEmpty(s) || s == "/") {
      "/index"
    } else if (s.endsWith("/")) {
      s"${s}index"
    } else {
      s
    }

    new ParsedRequest(formatted, queryParameters, formData, method)
  }

  def unapply(request: ParsedRequest) = {
    Some((request.url, request.queryParameters, request.formData, request.method))
  }

}
