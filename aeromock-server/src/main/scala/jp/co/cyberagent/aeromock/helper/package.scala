package jp.co.cyberagent.aeromock

import java.net.{InetSocketAddress, URLDecoder}
import java.nio.charset.Charset
import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes
import java.security.MessageDigest
import java.util.regex.Pattern

import io.netty.handler.codec.http.{FullHttpRequest, HttpHeaders, HttpRequest}
import io.netty.handler.codec.http.multipart.{HttpPostRequestDecoder, MixedAttribute}
import io.netty.util.CharsetUtil
import jp.co.cyberagent.aeromock.config.MessageManager
import jp.co.cyberagent.aeromock.core.http.AeromockHttpRequest
import jp.co.cyberagent.aeromock.util.ResourceUtil
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.reflect.FieldUtils

import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer
import scala.reflect.ClassTag
import scalaz._
import Scalaz._
import scalaz.Validation._
import scala.language.reflectiveCalls

/**
 * Package object for various helper.
 * @author stormcat24
 */
package object helper {

  val INSECURE_URI = Pattern.compile( """.*[<>&"].*""")
  val EXTENSION = Pattern.compile( """.*\.(.+)""")
  val WITHOUT_EXTENSION = Pattern.compile("""(.+)\..+""")
  val CONTENTTYPE_CHARSET = """^(.+);\s*charset=(.*)$""".r

  type Closable = { def close(): Unit }
  def processResource[A <: Closable, B](resource: A)(f: A => B) = try {
    f(resource)
  } finally { resource.close }

  def millsToSeconds(end: Long, start: Long): BigDecimal = {
    BigDecimal(end - start) / BigDecimal(1000)
  }

  def getDifferenceSecondsFromNow(pointTime: Long): BigDecimal = millsToSeconds(System.currentTimeMillis(), pointTime)


  def tryo[T](f: => T)(implicit error: Throwable => Option[T] = {
    t: Throwable => None
  }): Option[T] = {
    try {
      Some(f)
    } catch {
      case c: Throwable => error(c)
    }
  }

  def trye[T](f: => T)(implicit onError: Throwable => Either[Throwable, T] = {
    t: Throwable => Left(t)
  }): Either[Throwable, T] = {
    try {
      Right(f)
    } catch {
      case c: Throwable => onError(c)
    }
  }

  def getExtension(value: String): Option[String] = {
    require(value != null)

    val result = EXTENSION.matcher(value)
    result.matches() match {
      case false => None
      case true => Some(result.group(1))
    }
  }

  def getObjectFqdn(target: Any): String = {
    require(target != null)

    target.getClass().getName().replaceAll( """\$$""", "")
  }

  def red(value: String) = s"\u001b[31m${value}\u001b[00m"
  def green(value: String) = s"\u001b[32m${value}\u001b[00m"
  def yellow(value: String) = s"\u001b[33m${value}\u001b[00m"
  def blue(value: String) = s"\u001b[34m${value}\u001b[00m"
  def purple(value: String) = s"\u001b[35m${value}\u001b[00m"
  def lightBlue(value: String) = s"\u001b[36m${value}\u001b[00m"
  def white(value: String) = s"\u001b[37m${value}\u001b[00m"

  def cast[S: ClassTag](value: Any): Validation[Throwable, S] = {
    val t = implicitly[ClassTag[S]].runtimeClass.asInstanceOf[Class[S]]
    fromTryCatch(t.cast(value))
  }

  object SystemHelper {

    import ValueStrategies.StringValueStrategy

    def property(key: String): Option[String] = property(StringValueStrategy(key)).map(_.right.get.toString)

    def property[A](key: ValueStrategy[A]): Option[Either[Throwable, A]] = key.convert(Option(System.getProperty(key.key)))

  }


  // implicit classes START

  implicit class StringContextHelper(val context: StringContext) {

    def message(args: AnyRef*): String = {
      val key = context.parts.iterator.toList.head
      MessageManager.getMessage(key, args.iterator.toList:_*)
    }
  }

  implicit class PathHelper(val path: Path) {

    val fileSystem = FileSystems.getDefault()

    def exists(): Boolean = Files.exists(path)

    def isDirectory(): Boolean = Files.isDirectory(path)

    def withoutExtension(): Path = {

      val matcher = WITHOUT_EXTENSION.matcher(path.toString)

      matcher.matches() match {
        case false => path
        case true => fileSystem.getPath(matcher.group(1))
      }
    }

    def getChildren(): List[Path] = {
      tryo {
        Files.newDirectoryStream(path)
      } match {
        case Some(system) => processResource(system)(_.asScala.toList)
        case None => List.empty
      }
    }

    def getRelativePath(root: Path): Path = {
      val full = path.toString.replace("/", fileSystem.getSeparator())
      val base = root.toString.replace("/", fileSystem.getSeparator())
      fileSystem.getPath(full.replace(base, ""))
    }

    def hasExtension(extension: String): Boolean = {
      require(extension != null)

      path.getFileName().toString.endsWith(s".$extension")
    }

    def +(token: String): Path = Paths.get(path.toString + token)

    def /(childPath: String): Path = {
      require(StringUtils.isNotBlank(childPath))

      val optimized = "." + fileSystem.getSeparator() + childPath.replace("/", fileSystem.getSeparator())
      path.resolve(optimized).normalize()
    }

    def /(childPath: Path): Path = {
      require(childPath != null)
      /(childPath.toString())
    }

    def filterChildren(regExp: String): List[Path] = {
      require(regExp != null)
      if (!isDirectory) {
        throw new IllegalStateException(s"${path.toString()} is not directory.")
      }

      val pattern = Pattern.compile(regExp)
      val buf = new ListBuffer[Path]
      Files.walkFileTree(path, new SimpleFileVisitor[Path] {
        override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
          val matcher = pattern.matcher(file.getFileName().toString())
          if (matcher.find()) {
            buf += file
          }
          FileVisitResult.CONTINUE
        }
      })

      buf.toList
    }

    def withHomeDirectory(): Path = {

      if (path.toString().startsWith("~")) {
        SystemHelper.property("user.home") match {
          case Some(home) => {
            val replaceExp = if (System.getProperty("os.name").contains("Windows")) {
              home.replace("""\""", """\\""")
            } else {
              home
            }
            fileSystem.getPath(path.toString().replaceFirst("^~", replaceExp))
          }
          case None => throw new IllegalStateException("can't get system property 'user.home'.")
        }
      } else {
        path
      }
    }

    def toCheckSum(): String = {
      ResourceUtil.processResrouce(Files.newInputStream(path, StandardOpenOption.READ)) { is =>
        val buf = new Array[Byte](4096)

        val md = MessageDigest.getInstance("MD5")

        var len = 0
        while ({len = is.read(buf, 0, buf.length); len >= 0}) md.update(buf, 0, len)
        md.digest().map("%02x" format _).mkString
      }
    }

    def getExtension(): Option[String] = helper.getExtension(path.getFileName.toString)
  }

  implicit class FullHttpRequestHelper(original: FullHttpRequest) {

    lazy val decoded = URLDecoder.decode(original.getUri(), "UTF-8")

    lazy val requestUri = if (decoded.contains("?")) decoded.substring(0, decoded.indexOf("?")) else decoded

    lazy val queryString = if (decoded.contains("?")) decoded.substring(decoded.indexOf("?") + 1, decoded.length()) else ""

    lazy val queryParameters = if (StringUtils.isBlank(queryString)) {
      Map.empty[String, String]
    } else {
      queryString.split("&").map(s => {
        val pair = s.split("=")
        if (pair.length > 1) (pair(0), pair(1)) else (pair(0), "")
      }).toMap
    }

    lazy val postData = {
      import org.json4s._
      import org.json4s.native.JsonMethods._

      val postData = Option(original.headers.get(HttpHeaders.Names.CONTENT_TYPE)).map {
        case CONTENTTYPE_CHARSET(ct, charset) => (ct, Charset.forName(charset))
        case s => (s, CharsetUtil.UTF_8)
      }.map {
        case ("application/json", cs) => {
          val json = original.content.toString(cs)
          if (StringUtils.isBlank(json)) {
            Map.empty[String, Any].right[String]
          } else {
            \/.fromTryCatchNonFatal(parse(json)) match {
              case \/-(j: JObject) => j.values.right[String]
              case \/-(j) => Map.empty[String, Any].right[String]
              case -\/(e) => s"Failed to parse json: $json".left[Map[String, Any]]
            }
          }

        }
        case ("application/x-www-form-urlencoded", cs) => {
          (new HttpPostRequestDecoder(original).getBodyHttpDatas().asScala.collect {
            case a: MixedAttribute => (a.getName() -> a.getValue())
          }).toMap.right[String]
        }
        case (ct, cs) if ct.startsWith("multipart/form-data") => {
          (new HttpPostRequestDecoder(original).getBodyHttpDatas().asScala.collect {
            case a: MixedAttribute => (a.getName() -> a.getValue())
          }).toMap.right[String]
        }
        case (ct, cs) => {
          // ignore, do not parse
          Map.empty[String, Any].right[String]
        }
      }

      postData match {
        case Some(\/-(d)) => d
        case Some(-\/(e)) => throw new AeromockInvalidRequestException(e)
        case _ => Map.empty[String, Any]
      }
    }

    def toAeromockRequest(routeParameters: Map[String, String]) = {
      AeromockHttpRequest(
        url = requestUri,
        queryParameters = queryParameters,
        postData = postData,
        routeParameters = routeParameters,
        method = original.getMethod
      )
    }

    lazy val extension = getExtension(requestUri)

    def toVariableMap(): Map[String, Any] = {
      import io.netty.handler.codec.http.HttpHeaders.Names

      val namesClass = classOf[Names]
      namesClass.getFields().toArray.map {
        f =>
          val headerKey = FieldUtils.readDeclaredStaticField(namesClass, f.getName, true)
          val headerValue = original.headers().get(headerKey.toString)
          (f.getName, if (headerValue == null) "" else headerValue)
      }.toMap
    }
  }

  implicit class ExternalInetSocketAddress(source: InetSocketAddress) {

    def toVariableMap(): Map[String, String] = {
      Map(
        "REMOTE_ADDR" -> source.getAddress().getHostAddress(),
        "REMOTE_HOST" -> source.getHostName()
      )
    }
  }

  object ValueStrategies {

    case class StringValueStrategy(key: String) extends ValueStrategy[String] {
      override def convert(value: Option[String]): Option[Either[Throwable, String]] = value.map(trye(_))
    }

    case class IntValueStrategy(key: String) extends ValueStrategy[Int] {
      override def convert(value: Option[String]): Option[Either[Throwable, Int]] = value.map(v => trye(v.toInt))
    }
  }

  implicit class ExternalString(source: String) {
    import ValueStrategies._

    def intStrategy: ValueStrategy[Int] = IntValueStrategy(source)
    def stringStrategy: ValueStrategy[String] = StringValueStrategy(source)
  }

  // implicit classes END

  trait ValueStrategy[ReturnType] {
    val key: String
    def convert(value: Option[String]):  Option[Either[Throwable, ReturnType]]
  }

}


