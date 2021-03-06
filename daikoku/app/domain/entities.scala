package fr.maif.otoroshi.daikoku.domain

import java.util.concurrent.TimeUnit

import akka.http.scaladsl.util.FastFuture
import cats.syntax.option._
import fr.maif.otoroshi.daikoku.audit.KafkaConfig
import fr.maif.otoroshi.daikoku.audit.config.{ElasticAnalyticsConfig, Webhook}
import fr.maif.otoroshi.daikoku.domain.ApiVisibility.Public
import fr.maif.otoroshi.daikoku.domain.NotificationStatus.Pending
import fr.maif.otoroshi.daikoku.domain.TeamPermission.Administrator
import fr.maif.otoroshi.daikoku.domain.json._
import fr.maif.otoroshi.daikoku.env.Env
import fr.maif.otoroshi.daikoku.login.AuthProvider
import fr.maif.otoroshi.daikoku.utils.StringImplicits._
import fr.maif.otoroshi.daikoku.utils._
import org.joda.time.DateTime
import play.api.libs.json._
import play.api.mvc.Result
import play.twirl.api.Html
import reactivemongo.bson.BSONObjectID

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}

trait CanJson[A] {
  def asJson: JsValue
}

/**
  * Entity representing the UI style of the tenant
  * @param js Javascript code injected in each page
  * @param css CSS code injected in each page
  * @param colorTheme CSS code to customize colors of the current tenant
  */
case class DaikokuStyle(
    js: String = "",
    css: String = "",
    colorTheme: String = s""":root {
  --error-color: #ff6347;
  --error-color: #ffa494;
  --success-color: #4F8A10;
  --success-color: #76cf18;

  --link-color: #7f96af;
  --link--hover-color: #8fa6bf;

  --body-bg-color: #fff;
  --body-text-color: #212529;
  --navbar-bg-color: #7f96af;
  --navbar-brand-color: #fff;
  --menu-bg-color: #fff;
  --menu-text-color: #212529;
  --menu-text-hover-bg-color: #9bb0c5;
  --menu-text-hover-color: #fff;
  --section-bg-color: #f8f9fa;
  --section-text-color: #6c757d;
  --section-bottom-color: #eee;
  --addContent-bg-color: #e9ecef;
  --addContent-text-color: #000;
  --sidebar-bg-color: #f8f9fa;

  --btn-bg-color: #fff;
  --btn-text-color: #495057;
  --btn-border-color: #97b0c7;

  --badge-tags-bg-color: #ffc107;
  --badge-tags-bg-color: #ffe1a7;
  --badge-tags-text-color: #212529;

  --pagination-text-color: #586069;
  --pagination-border-color: #586069;

  --table-bg-color: #f8f9fa;

  --apicard-visibility-color: #586069;
  --apicard-visibility-border-color: rgba(27,31,35,.15);
  --modal-selection-bg-color: rgba(27,31,35,.15);
}""",
    jsUrl: Option[String] = None,
    cssUrl: Option[String] = None,
    faviconUrl: Option[String] = None,
    fontFamilyUrl: Option[String] = None,
    title: String = "New Organization",
    description: String = "A new organization to host very fine APIs",
    unloggedHome: String = "",
    homePageVisible: Boolean = false,
    logo: String = "/assets/images/daikoku.svg",
) extends CanJson[DaikokuStyle] {
  override def asJson: JsValue = json.DaikokuStyleFormat.writes(this)
}

case class AuditTrailConfig(
    elasticConfigs: Seq[ElasticAnalyticsConfig] =
      Seq.empty[ElasticAnalyticsConfig],
    auditWebhooks: Seq[Webhook] = Seq.empty[Webhook],
    alertsEmails: Seq[String] = Seq.empty[String],
    kafkaConfig: Option[KafkaConfig] = None,
) extends CanJson[AuditTrailConfig] {
  override def asJson: JsValue = json.AuditTrailConfigFormat.writes(this)
}

case class Tenant(
    id: TenantId,
    enabled: Boolean = true,
    deleted: Boolean = false,
    name: String,
    domain: String, // = "localhost",
    style: Option[DaikokuStyle],
    defaultLanguage: Option[String],
    otoroshiSettings: Set[OtoroshiSettings],
    mailerSettings: Option[MailerSettings],
    bucketSettings: Option[S3Configuration],
    authProvider: AuthProvider,
    authProviderSettings: JsObject,
    auditTrailConfig: AuditTrailConfig = AuditTrailConfig(),
    isPrivate: Boolean = true
) extends CanJson[Tenant] {

  override def asJson: JsValue = json.TenantFormat.writes(this)
  def asJsonWithJwt(implicit env: Env): JsValue =
    json.TenantFormat.writes(this).as[JsObject] ++ json.DaikokuHeader
      .jsonHeader(id)
  def mailer(implicit env: Env): Mailer =
    mailerSettings.map(_.mailer).getOrElse(ConsoleMailer())
  def humanReadableId = name.urlPathSegmentSanitized
  def toUiPayload(env: Env): JsValue = {
    Json.obj(
      "_id" -> id.value,
      "_humanReadableId" -> name.urlPathSegmentSanitized,
      "name" -> name,
      "title" -> style
        .map(a => JsString(a.title))
        .getOrElse(JsNull)
        .as[JsValue],
      "description" -> style
        .map(a => JsString(a.description))
        .getOrElse(JsNull)
        .as[JsValue],
      "unloggedHome" -> style
        .map(a => JsString(a.unloggedHome))
        .getOrElse(JsNull)
        .as[JsValue],
      "logo" -> style.map(a => JsString(a.logo)).getOrElse(JsNull).as[JsValue],
      "mode" -> env.config.mode.name,
      "authProvider" -> authProvider.name,
      "defaultLanguage" -> defaultLanguage.fold(JsNull.as[JsValue])(
        JsString.apply),
      "homePageVisible" -> style.exists(_.homePageVisible)
    )
  }
  def colorTheme(): Html = {
    style.map { s =>
      Html(s"""<style>${s.colorTheme}</style>""")
    } getOrElse Html("")
  }
  def moareStyle(): Html = {
    style.map { s =>
      val moreCss = s.cssUrl
        .map(u => s"""<link rel="stylesheet" media="screen" href="${u}">""")
        .getOrElse("")

      val moreFontFamily = s.fontFamilyUrl
        .map(u =>
          s"""<style>
             |@font-face{
             |font-family: "custom";
             |src: url("$u")
             |}
             |</style>""".stripMargin)
        .getOrElse("")

      if (s.css.startsWith("http")) {
        Html(
          s"""<link rel="stylesheet" media="screen" href="${s.css}">\n$moreCss\n$moreFontFamily""")
      } else if (s.css.startsWith("/")) {
        Html(
          s"""<link rel="stylesheet" media="screen" href="${s.css}">\n$moreCss\n$moreFontFamily""")
      } else {
        Html(s"""<style>${s.css}</style>\n$moreCss\n$moreFontFamily""")
      }
    } getOrElse Html("")
  }
  def moareJs(): Html = {
    style.map { s =>
      val moreJs =
        s.jsUrl.map(u => s"""<script" src="${u}"></script>""").getOrElse("")
      if (s.js.startsWith("http")) {
        Html(s"""<script" src="${s.js}"></script>\n$moreJs""")
      } else if (s.js.startsWith("<script")) {
        Html(s"""${s.js}\n$moreJs""")
      } else {
        Html(s"""<script>${s.js}</script>\n$moreJs""")
      }
    } getOrElse Html("")
  }
  def favicon(): String = {
    style.flatMap(_.faviconUrl).getOrElse("/assets/images/favicon.png")
  }
}

object Tenant {
  val Default = TenantId("default")
}

object Team {
  val Default = TeamId("none")
}

sealed trait MailerSettings {
  def mailerType: String
  def mailer(implicit env: Env): Mailer
  def asJson: JsValue
}

case class ConsoleMailerSettings()
    extends MailerSettings
    with CanJson[ConsoleMailerSettings] {
  def mailerType: String = "console"
  def asJson: JsValue = Json.obj("type" -> "console")
  def mailer(implicit env: Env): Mailer = {
    new ConsoleMailer()
  }
}

case class MailgunSettings(domain: String,
                           key: String,
                           fromTitle: String,
                           fromEmail: String)
    extends MailerSettings
    with CanJson[MailgunSettings] {
  def mailerType: String = "mailgun"
  def asJson: JsValue = json.MailgunSettingsFormat.writes(this)
  def mailer(implicit env: Env): Mailer = {
    new MailgunSender(env.wsClient, this)
  }
}

case class MailjetSettings(apiKeyPublic: String,
                           apiKeyPrivate: String,
                           fromTitle: String,
                           fromEmail: String)
    extends MailerSettings
    with CanJson[MailjetSettings] {
  def mailerType: String = "mailjet"
  def asJson: JsValue = json.MailjetSettingsFormat.writes(this)
  def mailer(implicit env: Env): Mailer = {
    new MailjetSender(env.wsClient, this)
  }
}

// case class IdentitySettings(
//   identityThroughOtoroshi: Boolean,
//   stateHeaderName: String = "Otoroshi-State",
//   stateRespHeaderName: String = "Otoroshi-State-Resp",
//   claimHeaderName: String = "Otoroshi-Claim",
//   claimSecret: String = "secret",
// ) extends CanJson[OtoroshiSettings] {
//   def asJson: JsValue = json.IdentitySettingsFormat.writes(this)
// }

case class OtoroshiSettings(id: OtoroshiSettingsId,
                            url: String,
                            host: String,
                            clientId: String = "admin-api-apikey-id",
                            clientSecret: String = "admin-api-apikey-secret")
    extends CanJson[OtoroshiSettings] {
  def asJson: JsValue = json.OtoroshiSettingsFormat.writes(this)
}

case class ApiKeyRestrictionPath(method: String, path: String)
    extends CanJson[ApiKeyRestrictionPath] {
  def asJson: JsValue = json.ApiKeyRestrictionPathFormat.writes(this)
}

case class ApiKeyRestrictions(
    enabled: Boolean = false,
    allowLast: Boolean = true,
    allowed: Seq[ApiKeyRestrictionPath] = Seq.empty,
    forbidden: Seq[ApiKeyRestrictionPath] = Seq.empty,
    notFound: Seq[ApiKeyRestrictionPath] = Seq.empty,
) extends CanJson[ApiKeyRestrictions] {
  def asJson: JsValue = json.ApiKeyRestrictionsFormat.writes(this)
}

case class ApikeyCustomization(
    dynamicPrefix: Option[String] = None,
    clientIdOnly: Boolean = false,
    readOnly: Boolean = false,
    constrainedServicesOnly: Boolean = false,
    metadata: JsObject = play.api.libs.json.Json.obj(),
    tags: JsArray = play.api.libs.json.Json.arr(),
    restrictions: ApiKeyRestrictions = ApiKeyRestrictions()
) extends CanJson[ApikeyCustomization] {
  def asJson: JsValue = json.ApikeyCustomizationFormat.writes(this)
}

object OtoroshiTarget {
  val expressionReplacer = ReplaceAllWith("\\$\\{([^}]*)\\}")
  val logger = play.api.Logger("OtoroshiTarget")

  def processValue(value: String, context: Map[String, String]): String = {
    value match {
      case v if v.contains("${") =>
        scala.util.Try {
          OtoroshiTarget.expressionReplacer.replaceOn(value) { expression =>
            context.get(expression).getOrElse("--")
          }
        } recover {
          case e =>
            OtoroshiTarget.logger.error(
              s"Error while parsing expression, returning raw value: $value",
              e)
            value
        } get
      case _ => value
    }
  }
}

case class OtoroshiTarget(
    otoroshiSettings: OtoroshiSettingsId,
    serviceGroup: OtoroshiServiceGroupId,
    apikeyCustomization: ApikeyCustomization = ApikeyCustomization()
) extends CanJson[OtoroshiTarget] {
  def asJson: JsValue = json.OtoroshiTargetFormat.writes(this)
  def processedMetadata(context: Map[String, String]): Map[String, String] = {
    apikeyCustomization.metadata
      .asOpt[Map[String, String]]
      .getOrElse(Map.empty[String, String])
      .mapValues(v => OtoroshiTarget.processValue(v, context))
  }
  def processedTags(context: Map[String, String]): Seq[String] = {
    apikeyCustomization.tags
      .asOpt[Seq[String]]
      .getOrElse(Seq.empty[String])
      .map(v => OtoroshiTarget.processValue(v, context))
  }
}

case class OtoroshiService(name: String,
                           otoroshiSettings: OtoroshiSettingsId,
                           service: OtoroshiServiceId)
    extends CanJson[OtoroshiService] {
  def asJson: JsValue = json.OtoroshiServiceFormat.writes(this)
}

sealed trait ValueType {
  def value: String
}
case class OtoroshiServiceId(value: String)
    extends ValueType
    with CanJson[OtoroshiServiceId] {
  def asJson: JsValue = JsString(value)
}
case class OtoroshiSettingsId(value: String)
    extends ValueType
    with CanJson[OtoroshiSettingsId] {
  def asJson: JsValue = JsString(value)
}
case class UsagePlanId(value: String)
    extends ValueType
    with CanJson[UsagePlanId] {
  def asJson: JsValue = JsString(value)
}
case class UserId(value: String) extends ValueType with CanJson[UserId] {
  def asJson: JsValue = JsString(value)
}
case class TeamId(value: String) extends ValueType with CanJson[TeamId] {
  def asJson: JsValue = JsString(value)
}
case class ApiId(value: String) extends ValueType with CanJson[ApiId] {
  def asJson: JsValue = JsString(value)
}
case class ApiSubscriptionId(value: String)
    extends ValueType
    with CanJson[ApiSubscriptionId] {
  def asJson: JsValue = JsString(value)
}
case class ApiDocumentationId(value: String)
    extends ValueType
    with CanJson[ApiDocumentationId] {
  def asJson: JsValue = JsString(value)
}
case class ApiDocumentationPageId(value: String)
    extends ValueType
    with CanJson[ApiDocumentationPageId] {
  def asJson: JsValue = JsString(value)
}
case class Version(value: String) extends ValueType with CanJson[Version] {
  def asJson: JsValue = JsString(value)
}
case class TenantId(value: String) extends ValueType with CanJson[TenantId] {
  def asJson: JsValue = JsString(value)
}
case class AssetId(value: String) extends ValueType with CanJson[AssetId] {
  def asJson: JsValue = JsString(value)
}
case class OtoroshiGroup(value: String)
    extends ValueType
    with CanJson[OtoroshiGroup] {
  def asJson: JsValue = JsString(value)
}
case class OtoroshiServiceGroupId(value: String)
    extends ValueType
    with CanJson[OtoroshiServiceGroupId] {
  def asJson: JsValue = JsString(value)
}
case class NotificationId(value: String)
    extends ValueType
    with CanJson[NotificationId] {
  def asJson: JsValue = JsString(value)
}
case class UserSessionId(value: String)
    extends ValueType
    with CanJson[UserSessionId] {
  def asJson: JsValue = JsString(value)
}
case class MongoId(value: String) extends ValueType with CanJson[MongoId] {
  def asJson: JsValue = JsString(value)
}

trait BillingTimeUnit extends CanJson[BillingTimeUnit] {
  def name: String
  def asJson: JsValue = JsString(name)
}

object BillingTimeUnit {
  case object Hour extends BillingTimeUnit {
    def name: String = "Hour"
  }
  case object Day extends BillingTimeUnit {
    def name: String = "Day"
  }
  case object Month extends BillingTimeUnit {
    def name: String = "Month"
  }
  case object Year extends BillingTimeUnit {
    def name: String = "Year"
  }
  val values: Seq[BillingTimeUnit] =
    Seq(Hour, Day, Month, Year)
  def apply(name: String): Option[BillingTimeUnit] = name.toLowerCase() match {
    case "hour"   => Hour.some
    case "hours"  => Hour.some
    case "day"    => Day.some
    case "days"   => Day.some
    case "month"  => Month.some
    case "months" => Month.some
    case "year"   => Year.some
    case "years"  => Year.some
    case _        => None
  }
}

case class BillingDuration(value: Long, unit: BillingTimeUnit)
    extends CanJson[BillingDuration] {
  def asJson: JsValue = json.BillingDurationFormat.writes(this)
}

sealed trait TeamType {
  def name: String
}

object TeamType {
  case object Personal extends TeamType {
    def name: String = "Personal"
  }
  case object Organization extends TeamType {
    def name: String = "Organization"
  }
  val values: Seq[TeamType] =
    Seq(Personal, Organization)
  def apply(name: String): Option[TeamType] = name match {
    case "Organization" => Organization.some
    case "Personal"     => Personal.some
    case _              => None
  }
}

sealed trait ApiVisibility {
  def name: String
}

object ApiVisibility {
  case object Public extends ApiVisibility {
    def name: String = "Public"
  }
  case object PublicWithAuthorizations extends ApiVisibility {
    def name: String = "PublicWithAuthorizations"
  }
  case object Private extends ApiVisibility {
    def name: String = "Private"
  }
  val values: Seq[ApiVisibility] =
    Seq(Public, Private, PublicWithAuthorizations)
  def apply(name: String): Option[ApiVisibility] = name match {
    case "Public"                   => Public.some
    case "Private"                  => Private.some
    case "PublicWithAuthorizations" => PublicWithAuthorizations.some
    case _                          => None
  }
}

sealed trait UsagePlanVisibility {
  def name: String
}

object UsagePlanVisibility {
  case object Public extends UsagePlanVisibility {
    def name: String = "Public"
  }

  case object Private extends UsagePlanVisibility {
    def name: String = "Private"
  }
  val values: Seq[UsagePlanVisibility] =
    Seq(Public, Private)
  def apply(name: String): Option[UsagePlanVisibility] = name match {
    case "Public"  => Public.some
    case "Private" => Private.some
    case _         => None
  }
}

sealed trait SubscriptionProcess {
  def name: String
}

object SubscriptionProcess {
  case object Automatic extends SubscriptionProcess {
    def name: String = "Automatic"
  }
  case object Manual extends SubscriptionProcess {
    def name: String = "Manual"
  }
  val values: Seq[SubscriptionProcess] = Seq(Automatic, Manual)
  def apply(name: String): Option[SubscriptionProcess] = name match {
    case "Automatic" => Automatic.some
    case "Manual"    => Manual.some
    case _           => None
  }
}

case class Currency(code: String) extends CanJson[Currency] {
  def asJson: JsValue = json.CurrencyFormat.writes(this)
}

sealed trait UsagePlan {
  def id: UsagePlanId
  def costPerMonth: BigDecimal
  def maxRequestPerSecond: Option[Long]
  def maxRequestPerDay: Option[Long]
  def maxRequestPerMonth: Option[Long]
  def allowMultipleKeys: Option[Boolean]
  def costFor(requests: Long): BigDecimal
  def currency: Currency
  def customName: Option[String]
  def customDescription: Option[String]
  def otoroshiTarget: Option[OtoroshiTarget]
  def trialPeriod: Option[BillingDuration]
  def billingDuration: BillingDuration
  def typeName: String
  def visibility: UsagePlanVisibility
  def authorizedTeams: Seq[TeamId]
  def asJson: JsValue = UsagePlanFormat.writes(this)
  def addAutorizedTeam(teamId: TeamId): UsagePlan
  def addAutorizedTeams(teamIds: Seq[TeamId]): UsagePlan
  def removeAuthorizedTeam(teamId: TeamId): UsagePlan
  def removeAllAuthorizedTeams(): UsagePlan
}

case object UsagePlan {
  case class FreeWithoutQuotas(
      id: UsagePlanId,
      currency: Currency,
      billingDuration: BillingDuration,
      customName: Option[String],
      customDescription: Option[String],
      otoroshiTarget: Option[OtoroshiTarget],
      allowMultipleKeys: Option[Boolean],
      override val visibility: UsagePlanVisibility = UsagePlanVisibility.Public,
      override val authorizedTeams: Seq[TeamId] = Seq.empty
  ) extends UsagePlan {
    override def typeName: String = "FreeWithoutQuotas"
    override def costPerMonth: BigDecimal = BigDecimal(0)
    override def maxRequestPerSecond: Option[Long] = None
    override def maxRequestPerDay: Option[Long] = None
    override def maxRequestPerMonth: Option[Long] = None
    override def costFor(requests: Long): BigDecimal = BigDecimal(0)
    override def trialPeriod: Option[BillingDuration] = None
    override def addAutorizedTeam(teamId: TeamId): UsagePlan =
      this.copy(authorizedTeams = authorizedTeams :+ teamId)
    override def removeAuthorizedTeam(teamId: TeamId): UsagePlan =
      this.copy(authorizedTeams = authorizedTeams.filter(up => up != teamId))
    override def removeAllAuthorizedTeams(): UsagePlan =
      this.copy(authorizedTeams = Seq.empty)
    override def addAutorizedTeams(teamIds: Seq[TeamId]): UsagePlan =
      this.copy(authorizedTeams = teamIds)
  }
  case class FreeWithQuotas(
      id: UsagePlanId,
      maxPerSecond: Long,
      maxPerDay: Long,
      maxPerMonth: Long,
      currency: Currency,
      billingDuration: BillingDuration,
      customName: Option[String],
      customDescription: Option[String],
      otoroshiTarget: Option[OtoroshiTarget],
      allowMultipleKeys: Option[Boolean],
      override val visibility: UsagePlanVisibility = UsagePlanVisibility.Public,
      override val authorizedTeams: Seq[TeamId] = Seq.empty
  ) extends UsagePlan {
    override def typeName: String = "FreeWithQuotas"
    override def costPerMonth: BigDecimal = BigDecimal(0)
    override def maxRequestPerSecond: Option[Long] = maxPerSecond.some
    override def maxRequestPerDay: Option[Long] = maxPerDay.some
    override def maxRequestPerMonth: Option[Long] = maxPerMonth.some
    override def costFor(requests: Long): BigDecimal = BigDecimal(0)
    override def trialPeriod: Option[BillingDuration] = None
    override def addAutorizedTeam(teamId: TeamId): UsagePlan =
      this.copy(authorizedTeams = authorizedTeams :+ teamId)
    override def removeAuthorizedTeam(teamId: TeamId): UsagePlan =
      this.copy(authorizedTeams = authorizedTeams.filter(up => up != teamId))
    override def removeAllAuthorizedTeams(): UsagePlan =
      this.copy(authorizedTeams = Seq.empty)
    override def addAutorizedTeams(teamIds: Seq[TeamId]): UsagePlan =
      this.copy(authorizedTeams = teamIds)
  }
  case class QuotasWithLimits(
      id: UsagePlanId,
      maxPerSecond: Long,
      maxPerDay: Long,
      maxPerMonth: Long,
      costPerMonth: BigDecimal,
      trialPeriod: Option[BillingDuration],
      currency: Currency,
      billingDuration: BillingDuration,
      customName: Option[String],
      customDescription: Option[String],
      otoroshiTarget: Option[OtoroshiTarget],
      allowMultipleKeys: Option[Boolean],
      override val visibility: UsagePlanVisibility = UsagePlanVisibility.Public,
      override val authorizedTeams: Seq[TeamId] = Seq.empty
  ) extends UsagePlan {
    override def typeName: String = "QuotasWithLimits"
    override def maxRequestPerSecond: Option[Long] = maxPerSecond.some
    override def maxRequestPerDay: Option[Long] = maxPerDay.some
    override def maxRequestPerMonth: Option[Long] = maxPerMonth.some
    override def costFor(requests: Long): BigDecimal = costPerMonth
    override def addAutorizedTeam(teamId: TeamId): UsagePlan =
      this.copy(authorizedTeams = authorizedTeams :+ teamId)
    override def removeAuthorizedTeam(teamId: TeamId): UsagePlan =
      this.copy(authorizedTeams = authorizedTeams.filter(up => up != teamId))
    override def removeAllAuthorizedTeams(): UsagePlan =
      this.copy(authorizedTeams = Seq.empty)
    override def addAutorizedTeams(teamIds: Seq[TeamId]): UsagePlan =
      this.copy(authorizedTeams = teamIds)
  }
  case class QuotasWithoutLimits(
      id: UsagePlanId,
      maxPerSecond: Long,
      maxPerDay: Long,
      maxPerMonth: Long,
      costPerAdditionalRequest: BigDecimal,
      costPerMonth: BigDecimal,
      trialPeriod: Option[BillingDuration],
      currency: Currency,
      billingDuration: BillingDuration,
      customName: Option[String],
      customDescription: Option[String],
      otoroshiTarget: Option[OtoroshiTarget],
      allowMultipleKeys: Option[Boolean],
      override val visibility: UsagePlanVisibility = UsagePlanVisibility.Public,
      override val authorizedTeams: Seq[TeamId] = Seq.empty
  ) extends UsagePlan {
    override def typeName: String = "QuotasWithoutLimits"
    override def maxRequestPerSecond: Option[Long] = maxPerSecond.some
    override def maxRequestPerDay: Option[Long] = maxPerDay.some
    override def maxRequestPerMonth: Option[Long] = maxPerMonth.some
    override def costFor(requests: Long): BigDecimal =
      costPerMonth + (Math.max(requests - maxPerMonth, 0) * costPerAdditionalRequest)
    override def addAutorizedTeam(teamId: TeamId): UsagePlan =
      this.copy(authorizedTeams = authorizedTeams :+ teamId)
    override def removeAuthorizedTeam(teamId: TeamId): UsagePlan =
      this.copy(authorizedTeams = authorizedTeams.filter(up => up != teamId))
    override def removeAllAuthorizedTeams(): UsagePlan =
      this.copy(authorizedTeams = Seq.empty)
    override def addAutorizedTeams(teamIds: Seq[TeamId]): UsagePlan =
      this.copy(authorizedTeams = teamIds)
  }
  case class PayPerUse(
      id: UsagePlanId,
      costPerMonth: BigDecimal,
      costPerRequest: BigDecimal,
      trialPeriod: Option[BillingDuration],
      currency: Currency,
      billingDuration: BillingDuration,
      customName: Option[String],
      customDescription: Option[String],
      otoroshiTarget: Option[OtoroshiTarget],
      allowMultipleKeys: Option[Boolean],
      override val visibility: UsagePlanVisibility = UsagePlanVisibility.Public,
      override val authorizedTeams: Seq[TeamId] = Seq.empty
  ) extends UsagePlan {
    override def typeName: String = "PayPerUse"
    override def costFor(requests: Long): BigDecimal =
      costPerMonth + (requests * costPerRequest)
    override def maxRequestPerMonth: Option[Long] = None
    override def maxRequestPerSecond: Option[Long] = None
    override def maxRequestPerDay: Option[Long] = None
    override def addAutorizedTeam(teamId: TeamId): UsagePlan =
      this.copy(authorizedTeams = authorizedTeams :+ teamId)
    override def removeAuthorizedTeam(teamId: TeamId): UsagePlan =
      this.copy(authorizedTeams = authorizedTeams.filter(up => up != teamId))
    override def removeAllAuthorizedTeams(): UsagePlan =
      this.copy(authorizedTeams = Seq.empty)
    override def addAutorizedTeams(teamIds: Seq[TeamId]): UsagePlan =
      this.copy(authorizedTeams = teamIds)
  }
}

case class OtoroshiApiKey(clientName: String,
                          clientId: String,
                          clientSecret: String)

case class SwaggerAccess(url: String,
                         content: Option[String] = None,
                         headers: Map[String, String] =
                           Map.empty[String, String]) {
  def swaggerContent()(implicit ec: ExecutionContext,
                       env: Env): Future[JsValue] = {
    content match {
      case Some(c) => FastFuture.successful(Json.parse(c))
      case None => {
        val finalUrl =
          if (url.startsWith("/")) s"http://127.0.0.1:${env.config.port}${url}"
          else url
        env.wsClient
          .url(finalUrl)
          .withHttpHeaders(headers.toSeq: _*)
          .get()
          .map { resp =>
            Json.parse(resp.body)
          }
      }
    }
  }
}

case class ApiDocumentation(id: ApiDocumentationId,
                            tenant: TenantId,
                            pages: Seq[ApiDocumentationPageId],
                            lastModificationAt: DateTime)
    extends CanJson[ApiDocumentation] {
  override def asJson: JsValue = json.ApiDocumentationFormat.writes(this)
  def fetchPages(tenant: Tenant)(implicit ec: ExecutionContext, env: Env) = {
    env.dataStore.apiDocumentationPageRepo
      .forTenant(tenant.id)
      .findWithProjection(
        Json.obj(
          "_deleted" -> false,
          "_id" -> Json.obj(
            "$in" -> JsArray(pages.map(_.value).map(JsString.apply).toSeq))),
        Json.obj(
          "_id" -> true,
          "_humanReadableId" -> true,
          "title" -> true,
          "level" -> true,
          "lastModificationAt" -> true,
          "content" -> true,
          "contentType" -> true
        )
      )
      .map { list =>
        // TODO: fetch remote content
        pages
          .map(id => list.find(o => (o \ "_id").as[String] == id.value))
          .collect { case Some(e) => e }
      }
  }
}

// "https://mozilla.github.io/pdf.js/web/compressed.tracemonkey-pldi-09.pdf"
case class ApiDocumentationPage(id: ApiDocumentationPageId,
                                tenant: TenantId,
                                deleted: Boolean = false,
                                // api: ApiId,
                                title: String,
                                //index: Double,
                                level: Int = 0,
                                lastModificationAt: DateTime,
                                content: String,
                                contentType: String = "text/markdown",
                                remoteContentEnabled: Boolean = false,
                                remoteContentUrl: Option[String] = None,
                                remoteContentHeaders: Map[String, String] =
                                  Map.empty[String, String])
    extends CanJson[ApiDocumentationPage] {
  //def humanReadableId = s"$index-$level-${title.urlPathSegmentSanitized}"
  def humanReadableId = s"$level-${title.urlPathSegmentSanitized}"
  override def asJson: JsValue = json.ApiDocumentationPageFormat.writes(this)
  def asWebUiJson: JsValue =
    json.ApiDocumentationPageFormat.writes(this).as[JsObject]
}

case class User(
    id: UserId,
    deleted: Boolean = false,
    tenants: Set[TenantId],
    origins: Set[AuthProvider],
    name: String,
    email: String,
    picture: String = "/assets/images/anonymous.jpg",
    personalToken: Option[String],
    isDaikokuAdmin: Boolean = false,
    password: Option[String] = None,
    hardwareKeyRegistrations: Seq[JsObject] = Seq.empty,
    lastTenant: Option[TenantId],
    metadata: Map[String, String] = Map.empty,
    defaultLanguage: Option[String],
    isGuest: Boolean = false
) extends CanJson[User] {
  override def asJson: JsValue = json.UserFormat.writes(this)
  def humanReadableId = email.urlPathSegmentSanitized
  def asSimpleJson: JsValue = {
    Json.obj(
      "_id" -> id.value,
      "_humanReadableId" -> email.urlPathSegmentSanitized,
      "name" -> name,
      "email" -> email,
      "picture" -> picture
    )
  }
  def toUiPayload(tenantId: TenantId): JsValue = {
    Json.obj(
      "_id" -> id.value,
      "_humanReadableId" -> email.urlPathSegmentSanitized,
      "name" -> name,
      "email" -> email,
      "picture" -> picture,
      "isDaikokuAdmin" -> isDaikokuAdmin,
      "defaultLanguage" -> defaultLanguage.fold(JsNull.as[JsValue])(
        JsString.apply),
      "isGuest" -> isGuest
      // "lastTeam" -> json.TeamIdFormat.writes(lastTeams.getOrElse(tenantId, Team.Default))
    )
  }
}

object GuestUser {
  def apply(tenantId: TenantId): User = User(
    id = UserId("anonymous"),
    tenants = Set(tenantId),
    origins = Set.empty,
    name = "anonymous",
    email = "",
    lastTenant = None,
    defaultLanguage = None,
    personalToken = None,
    isGuest = true
  )
}

object GuestUserSession {
  def apply(user: User, tenant: Tenant): UserSession = {
    val sessionMaxAge =
      tenant.authProviderSettings.\("sessionMaxAge").asOpt[Int].getOrElse(86400)
    UserSession(
      id = MongoId(BSONObjectID.generate().stringify),
      userId = user.id,
      userName = user.name,
      userEmail = user.email,
      impersonatorId = None,
      impersonatorName = None,
      impersonatorEmail = None,
      impersonatorSessionId = None,
      sessionId = UserSessionId(IdGenerator.token),
      created = DateTime.now(),
      expires = DateTime.now().plusSeconds(sessionMaxAge),
      ttl = FiniteDuration(sessionMaxAge, TimeUnit.SECONDS)
    )
  }
}

sealed trait TeamPermission {
  def name: String
}

object TeamPermission {
  case object Administrator extends TeamPermission {
    def name: String = "Administrator"
  }
  case object ApiEditor extends TeamPermission {
    def name: String = "ApiEditor"
  }
  case object TeamUser extends TeamPermission {
    def name: String = "User"
  }
  val values: Seq[TeamPermission] =
    Seq(Administrator, ApiEditor, TeamUser)
  def apply(name: String): Option[TeamPermission] = name match {
    case "Administrator" => Administrator.some
    case "ApiEditor"     => ApiEditor.some
    case "User"          => TeamUser.some
    case _               => None
  }
}

case class UserWithPermission(
    userId: UserId,
    teamPermission: TeamPermission
) extends CanJson[UserWithPermission] {
  override def asJson: JsValue = json.UserWithPermissionFormat.writes(this)
}

case class Team(
    id: TeamId,
    tenant: TenantId,
    deleted: Boolean = false,
    `type`: TeamType,
    name: String,
    description: String,
    contact: String = "contact@foo.bar",
    avatar: Option[String] = Some("/assets/images/daikoku.svg"),
    users: Set[UserWithPermission] = Set.empty,
    subscriptions: Seq[ApiSubscriptionId] = Seq.empty,
    authorizedOtoroshiGroups: Set[OtoroshiGroup] = Set.empty,
    showApiKeyOnlyToAdmins: Boolean = true,
    metadata: Map[String, String] = Map.empty
) extends CanJson[User] {
  override def asJson: JsValue = json.TeamFormat.writes(this)
  def humanReadableId = name.urlPathSegmentSanitized
  def asSimpleJson: JsValue = toUiPayload()
  def toUiPayload(): JsValue = {
    Json.obj(
      "_id" -> id.value,
      "_humanReadableId" -> humanReadableId,
      "_tenant" -> json.TenantIdFormat.writes(tenant),
      "tenant" -> json.TenantIdFormat.writes(tenant),
      "type" -> json.TeamTypeFormat.writes(`type`),
      "name" -> name,
      "description" -> description,
      "avatar" -> JsString(avatar.getOrElse("/assets/images/daikoku.svg")),
      "contact" -> contact,
      "users" -> json.SetUserWithPermissionFormat.writes(users),
      "showApiKeyOnlyToAdmins" -> showApiKeyOnlyToAdmins,
    )
  }
  def includeUser(userId: UserId): Boolean = {
    users.exists(_.userId == userId)
  }
  def admins(): Set[UserId] =
    users.filter(u => u.teamPermission == Administrator).map(_.userId)
}

sealed trait TestingAuth {
  def name: String
}

object TestingAuth {
  case object ApiKey extends TestingAuth {
    def name: String = "ApiKey"
  }
  case object Basic extends TestingAuth {
    def name: String = "Basic"
  }
}

case class Testing(
    enabled: Boolean = false,
    auth: TestingAuth = TestingAuth.Basic,
    name: Option[String] = None,
    username: Option[String] = None,
    password: Option[String] = None,
) extends CanJson[User] {
  override def asJson: JsValue = json.TestingFormat.writes(this)
}

case class Api(
    id: ApiId,
    tenant: TenantId,
    deleted: Boolean = false,
    team: TeamId,
    name: String,
    smallDescription: String,
    description: String,
    currentVersion: Version = Version("1.0.0"),
    supportedVersions: Set[Version] = Set(Version("1.0.0")),
    lastUpdate: DateTime,
    published: Boolean = false,
    testing: Testing = Testing(),
    documentation: ApiDocumentation,
    swagger: Option[SwaggerAccess] = Some(
      SwaggerAccess(url = "/assets/swaggers/petstore.json")),
    tags: Set[String] = Set.empty,
    categories: Set[String] = Set.empty,
    visibility: ApiVisibility,
    subscriptionProcess: SubscriptionProcess,
    possibleUsagePlans: Seq[UsagePlan],
    defaultUsagePlan: UsagePlanId,
    subscriptions: Seq[ApiSubscriptionId] = Seq.empty,
    authorizedTeams: Seq[TeamId] = Seq.empty,
    managedServices: Seq[OtoroshiService] = Seq.empty
) extends CanJson[User] {
  def humanReadableId = name.urlPathSegmentSanitized
  override def asJson: JsValue = json.ApiFormat.writes(this)
  def asSimpleJson: JsValue = Json.obj(
    "_id" -> id.asJson,
    "_humanReadableId" -> name.urlPathSegmentSanitized,
    "_tenant" -> tenant.asJson,
    "team" -> team.value,
    "name" -> name,
    "smallDescription" -> smallDescription,
    "description" -> description,
    "currentVersion" -> currentVersion.asJson,
    "supportedVersions" -> JsArray(supportedVersions.map(_.asJson).toSeq),
    "tags" -> JsArray(tags.map(JsString.apply).toSeq),
    "categories" -> JsArray(categories.map(JsString.apply).toSeq),
    "visibility" -> visibility.name,
    "possibleUsagePlans" -> JsArray(possibleUsagePlans.map(_.asJson).toSeq),
    "subscriptionProcess" -> subscriptionProcess.name
  )
  def asIntegrationJson(teams: Seq[Team]): JsValue = {
    val t = teams.find(_.id == team).get.name.urlPathSegmentSanitized
    Json.obj(
      "id" -> s"${t}/${name.urlPathSegmentSanitized}",
      "team" -> t,
      "name" -> name,
      "smallDescription" -> smallDescription,
      "currentVersion" -> currentVersion.asJson,
      "supportedVersions" -> JsArray(supportedVersions.map(_.asJson).toSeq),
      "tags" -> JsArray(tags.map(JsString.apply).toSeq),
      "categories" -> JsArray(categories.map(JsString.apply).toSeq),
      "visibility" -> visibility.name,
      "subscriptionProcess" -> subscriptionProcess.name
    )
  }
}

case class ApiSubscription(
    id: ApiSubscriptionId,
    tenant: TenantId,
    deleted: Boolean = false,
    apiKey: OtoroshiApiKey, // TODO: add the actual plan at the time of the subscription
    plan: UsagePlanId,
    createdAt: DateTime,
    team: TeamId,
    api: ApiId,
    by: UserId,
    customName: Option[String],
    enabled: Boolean = true
) extends CanJson[User] {
  override def asJson: JsValue = json.ApiSubscriptionFormat.writes(this)
  def asSimpleJson: JsValue = Json.obj(
    "_id" -> json.ApiSubscriptionIdFormat.writes(id),
    "_tenant" -> json.TenantIdFormat.writes(tenant),
    "_deleted" -> deleted,
    "plan" -> json.UsagePlanIdFormat.writes(plan),
    "team" -> json.TeamIdFormat.writes(team),
    "api" -> json.ApiIdFormat.writes(api),
    "createdAt" -> json.DateTimeFormat.writes(createdAt),
    "customName" -> customName
      .map(id => JsString(id))
      .getOrElse(JsNull)
      .as[JsValue],
    "enabled" -> JsBoolean(enabled)
  )
}

object RemainingQuotas {
  val MaxValue: Long = 10000000L
}

case class ActualOtoroshiApiKey(
    clientId: String = IdGenerator.token(16),
    clientSecret: String = IdGenerator.token(64),
    clientName: String,
    authorizedGroup: String,
    enabled: Boolean = true,
    allowClientIdOnly: Boolean = false,
    readOnly: Boolean = false,
    constrainedServicesOnly: Boolean = false,
    throttlingQuota: Long = RemainingQuotas.MaxValue,
    dailyQuota: Long = RemainingQuotas.MaxValue,
    monthlyQuota: Long = RemainingQuotas.MaxValue,
    tags: Seq[String] = Seq.empty[String],
    metadata: Map[String, String] = Map.empty[String, String],
    restrictions: ApiKeyRestrictions = ApiKeyRestrictions())
    extends CanJson[OtoroshiApiKey] {
  override def asJson: JsValue = json.ActualOtoroshiApiKeyFormat.writes(this)
}

sealed trait NotificationStatus

object NotificationStatus {
  case object Pending extends NotificationStatus with Product with Serializable
  case class Accepted(date: DateTime = DateTime.now())
      extends NotificationStatus
      with Product
      with Serializable
  case class Rejected(date: DateTime = DateTime.now())
      extends NotificationStatus
      with Product
      with Serializable
}

sealed trait NotificationAction
sealed trait OtoroshiSyncNotificationAction extends NotificationAction {
  def message: String
  def json: JsValue
}

object NotificationAction {
  case class ApiAccess(api: ApiId, team: TeamId) extends NotificationAction

  case class TeamAccess(team: TeamId) extends NotificationAction

  case class ApiSubscriptionDemand(api: ApiId, plan: UsagePlanId, team: TeamId)
      extends NotificationAction

  case class OtoroshiSyncSubscriptionError(subscription: ApiSubscription,
                                           message: String)
      extends OtoroshiSyncNotificationAction {
    def json: JsValue =
      Json.obj("errType" -> "OtoroshiSyncSubscriptionError",
               "errMessage" -> message,
               "subscription" -> subscription.asJson)
  }
  case class OtoroshiSyncApiError(api: Api, message: String)
      extends OtoroshiSyncNotificationAction {
    def json: JsValue =
      Json.obj("errType" -> "OtoroshiSyncApiError",
               "errMessage" -> message,
               "api" -> api.asJson)
  }
  case class ApiKeyDeletionInformation(api: String, clientId: String)
      extends NotificationAction
}

sealed trait NotificationType {
  def value: String
}

object NotificationType {
  case object AcceptOrReject extends NotificationType {
    override def value: String = "AcceptOrReject"
  }
  case object AcceptOnly extends NotificationType {
    override def value: String = "AcceptOnly"
  }
}

case class Notification(
    id: NotificationId,
    tenant: TenantId,
    deleted: Boolean = false,
    team: TeamId,
    sender: User,
    date: DateTime = DateTime.now(),
    notificationType: NotificationType = NotificationType.AcceptOrReject,
    status: NotificationStatus = Pending,
    action: NotificationAction
) extends CanJson[Notification] {
  override def asJson: JsValue = json.NotificationFormat.writes(this)
}

case class UserSession(id: MongoId,
                       sessionId: UserSessionId,
                       userId: UserId,
                       userName: String,
                       userEmail: String,
                       impersonatorId: Option[UserId],
                       impersonatorName: Option[String],
                       impersonatorEmail: Option[String],
                       impersonatorSessionId: Option[UserSessionId],
                       created: DateTime,
                       ttl: FiniteDuration,
                       expires: DateTime)
    extends CanJson[UserSession] {
  override def asJson: JsValue = json.UserSessionFormat.writes(this)
  def invalidate()(implicit ec: ExecutionContext, env: Env): Future[Unit] = {
    env.dataStore.userSessionRepo.deleteById(id).map(_ => ())
  }
  def asSimpleJson: JsValue = Json.obj(
    "created" -> created.toDate.getTime,
    "expires" -> expires.toDate.getTime,
    "ttl" -> ttl.toMillis,
  )
  def impersonatorJson(): JsValue = {
    impersonatorId.map { _ =>
      Json.obj(
        "_id" -> impersonatorId.get.value,
        "name" -> impersonatorName.get,
        "email" -> impersonatorEmail.get
      )
    } getOrElse {
      JsNull
    }
  }
}

case class ApiKeyConsumption(
    id: MongoId,
    tenant: TenantId,
    team: TeamId,
    api: ApiId,
    plan: UsagePlanId,
    clientId: String,
    hits: Long,
    globalInformations: ApiKeyGlobalConsumptionInformations,
    quotas: ApiKeyQuotas,
    billing: ApiKeyBilling,
    from: DateTime,
    to: DateTime)
    extends CanJson[ApiKeyConsumption] {
  override def asJson: JsValue = json.ConsumptionFormat.writes(this)
}

case class ApiKeyGlobalConsumptionInformations(hits: Long,
                                               dataIn: Long,
                                               dataOut: Long,
                                               avgDuration: Option[Double],
                                               avgOverhead: Option[Double])
    extends CanJson[ApiKeyGlobalConsumptionInformations] {
  override def asJson: JsValue =
    json.GlobalConsumptionInformationsFormat.writes(this)
}

case class ApiKeyQuotas(authorizedCallsPerSec: Long,
                        currentCallsPerSec: Long,
                        remainingCallsPerSec: Long,
                        authorizedCallsPerDay: Long,
                        currentCallsPerDay: Long,
                        remainingCallsPerDay: Long,
                        authorizedCallsPerMonth: Long,
                        currentCallsPerMonth: Long,
                        remainingCallsPerMonth: Long)
    extends CanJson[ApiKeyQuotas] {
  override def asJson: JsValue = json.ApiKeyQuotasFormat.writes(this)
}

case class ApiKeyBilling(hits: Long, total: BigDecimal)
    extends CanJson[ApiKeyBilling] {
  override def asJson: JsValue = json.ApiKeyBillingFormat.writes(this)
}

case class PasswordReset(
    id: MongoId,
    deleted: Boolean = false,
    randomId: String,
    email: String,
    password: String,
    user: UserId,
    creationDate: DateTime,
    validUntil: DateTime,
) extends CanJson[PasswordReset] {
  override def asJson: JsValue = json.PasswordResetFormat.writes(this)
}

case class AccountCreation(
    id: MongoId,
    deleted: Boolean = false,
    randomId: String,
    email: String,
    name: String,
    avatar: String,
    password: String,
    creationDate: DateTime,
    validUntil: DateTime,
) extends CanJson[AccountCreation] {
  override def asJson: JsValue = json.AccountCreationFormat.writes(this)
}
sealed trait TranslationElement

object TranslationElement {
  case class ApiTranslationElement(api: ApiId) extends TranslationElement
  case class TenantTranslationElement(tenant: TenantId)
      extends TranslationElement
  case class TeamTranslationElement(team: TeamId) extends TranslationElement
}

case class Translation(id: MongoId,
                       tenant: TenantId,
                       element: TranslationElement,
                       language: String,
                       key: String,
                       value: String)
    extends CanJson[Translation] {
  override def asJson: JsValue = json.TranslationFormat.writes(this)
  def asUiTranslationJson: JsValue = {
    Json.obj(
      key -> value,
    )
  }
}
