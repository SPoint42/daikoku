package fr.maif.otoroshi.daikoku.utils

import cats.data.EitherT
import controllers.AppError
import controllers.AppError.{
  ApiNotLinked,
  OtoroshiError,
  OtoroshiSettingsNotFound
}
import fr.maif.otoroshi.daikoku.domain.UsagePlan._
import fr.maif.otoroshi.daikoku.domain._
import fr.maif.otoroshi.daikoku.env.Env
import fr.maif.otoroshi.daikoku.utils.StringImplicits._
import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.json.{JsError, JsObject, Json}
import reactivemongo.bson.BSONObjectID

import scala.concurrent.Future

class ApiService(env: Env, otoroshiClient: OtoroshiClient) {

  implicit val ec = env.defaultExecutionContext
  implicit val ev = env

  def subscribeToApi(tenant: Tenant,
                     user: User,
                     api: Api,
                     planId: String,
                     team: Team): Future[Either[AppError, JsObject]] = {
    val defaultPlanOpt =
      api.possibleUsagePlans.find(p => p.id == api.defaultUsagePlan)
    val askedUsagePlan = api.possibleUsagePlans.find(p => p.id.value == planId)
    val plan: UsagePlan = askedUsagePlan
      .orElse(defaultPlanOpt)
      .getOrElse(api.possibleUsagePlans.head)

    def createKey(api: Api, plan: UsagePlan, team: Team, group: JsObject)(
        implicit otoroshiSettings: OtoroshiSettings
    ): Future[Either[AppError, JsObject]] = {
      import cats.implicits._
      // TODO: verify if group is in authorized groups (if some)
      val groupId = (group \ "id").as[String]
      val createdAt = DateTime.now().toString()
      val clientId = IdGenerator.token(32)
      val clientSecret = IdGenerator.token(64)
      val clientName =
        s"daikoku-api-key-${api.humanReadableId}-${plan.customName
          .getOrElse(plan.typeName)
          .urlPathSegmentSanitized}-${team.humanReadableId}-${System.currentTimeMillis()}"
      val apiSubscription = ApiSubscription(
        id = ApiSubscriptionId(BSONObjectID.generate().stringify),
        tenant = tenant.id,
        apiKey = OtoroshiApiKey(clientName, clientId, clientSecret),
        plan = plan.id,
        createdAt = DateTime.now(),
        team = team.id,
        api = api.id,
        by = user.id,
        customName = None
      )
      val ctx = Map(
        "user.id" -> user.id.value,
        "user.name" -> user.name,
        "user.email" -> user.email,
        "api.id" -> api.id.value,
        "api.name" -> api.name,
        "team.id" -> team.id.value,
        "team.name" -> team.name,
        "tenant.id" -> tenant.id.value,
        "tenant.name" -> tenant.name,
        "createdAt" -> createdAt,
        "client.id" -> clientId,
        "client.name" -> clientName,
        "group.id" -> groupId
      ) ++ team.metadata.map(t => ("team.metadata." + t._1, t._2)) ++ user.metadata
        .map(
          t => ("user.metadata." + t._1, t._2)
        )
      val apiKey = ActualOtoroshiApiKey(
        clientId = clientId,
        clientSecret = clientSecret,
        clientName = clientName,
        authorizedGroup = groupId,
        throttlingQuota = 1000,
        dailyQuota = RemainingQuotas.MaxValue,
        monthlyQuota = RemainingQuotas.MaxValue,
        allowClientIdOnly =
          plan.otoroshiTarget.exists(_.apikeyCustomization.clientIdOnly),
        readOnly = plan.otoroshiTarget.exists(_.apikeyCustomization.readOnly),
        constrainedServicesOnly = plan.otoroshiTarget.exists(
          _.apikeyCustomization.constrainedServicesOnly),
        tags = plan.otoroshiTarget
          .map(_.processedTags(ctx))
          .getOrElse(Seq.empty[String]),
        restrictions = plan.otoroshiTarget
          .map(_.apikeyCustomization.restrictions)
          .getOrElse(ApiKeyRestrictions()),
        metadata = Map(
          "daikoku_created_by" -> user.email,
          "daikoku_created_from" -> "daikoku",
          "daikoku_created_at" -> createdAt,
          "daikoku_created_with_id" -> api.id.value,
          "daikoku_created_with" -> api.name,
          "daikoku_created_for_team_id" -> team.id.value,
          "daikoku_created_for_team" -> team.name,
          "daikoku_created_on_tenant" -> tenant.id.value
        ) ++ plan.otoroshiTarget
          .map(_.processedMetadata(ctx))
          .getOrElse(Map.empty[String, String])
      )
      val tunedApiKey = plan match {
        case _: FreeWithoutQuotas => apiKey
        case p: FreeWithQuotas =>
          apiKey.copy(throttlingQuota = p.maxPerSecond,
                      dailyQuota = p.maxPerDay,
                      monthlyQuota = p.maxPerMonth)
        case p: QuotasWithLimits =>
          apiKey.copy(throttlingQuota = p.maxPerSecond,
                      dailyQuota = p.maxPerDay,
                      monthlyQuota = p.maxPerMonth)
        case p: QuotasWithoutLimits =>
          apiKey.copy(throttlingQuota = p.maxPerSecond,
                      dailyQuota = p.maxPerDay,
                      monthlyQuota = p.maxPerMonth)
        case _: PayPerUse => apiKey
      }
      val r: EitherT[Future, AppError, JsObject] = for {
        _ <- EitherT(otoroshiClient.createApiKey(groupId, tunedApiKey))
        _ <- EitherT.liftF(
          env.dataStore.apiSubscriptionRepo
            .forTenant(tenant.id)
            .save(apiSubscription))
        _ <- EitherT.liftF(
          env.dataStore.teamRepo
            .forTenant(tenant.id)
            .save(team.copy(
              subscriptions = team.subscriptions :+ apiSubscription.id))
        )
        _ <- EitherT.liftF(
          env.dataStore.apiRepo
            .forTenant(tenant.id)
            .save(
              api.copy(subscriptions = api.subscriptions :+ apiSubscription.id))
        )
      } yield {
        Json.obj("creation" -> "done", "subscription" -> apiSubscription.asJson)
      }

      r.value
    }

    plan.otoroshiTarget.map(_.otoroshiSettings).flatMap { id =>
      tenant.otoroshiSettings.find(_.id == id)
    } match {
      case None => Future.successful(Left(OtoroshiSettingsNotFound))
      case Some(otoSettings) =>
        implicit val otoroshiSettings: OtoroshiSettings = otoSettings
        plan.otoroshiTarget.map(_.serviceGroup) match {
          case None => Future.successful(Left(ApiNotLinked))
          case Some(groupId) =>
            otoroshiClient
              .getServiceGroup(groupId.value)
              .flatMap(group => createKey(api, plan, team, group))
        }
    }
  }

  def deleteApiKey(tenant: Tenant,
                   subscription: ApiSubscription,
                   plan: UsagePlan,
                   api: Api,
                   team: Team): Future[Either[AppError, JsObject]] = {
    def deleteKey(api: Api, team: Team, group: JsObject)(
        implicit otoroshiSettings: OtoroshiSettings
    ): Future[Either[AppError, JsObject]] = {
      import cats.implicits._

      val groupId = (group \ "id").as[String]

      val r: EitherT[Future, AppError, JsObject] = for {
        _ <- EitherT.liftF(
          otoroshiClient.deleteApiKey(groupId, subscription.apiKey.clientId))
        _ <- EitherT.liftF(
          env.dataStore.apiSubscriptionRepo
            .forTenant(tenant.id)
            .deleteByIdLogically(subscription.id))
        _ <- EitherT.liftF(
          env.dataStore.teamRepo
            .forTenant(tenant.id)
            .save(team.copy(subscriptions =
              team.subscriptions.filterNot(_ == subscription.id)))
        )
      } yield {
        Json.obj("archive" -> "done",
                 "subscriptionId" -> subscription.id.asJson)
      }

      r.value
    }

    plan.otoroshiTarget.map(_.otoroshiSettings).flatMap { id =>
      tenant.otoroshiSettings.find(_.id == id)
    } match {
      case None => Future.successful(Left(OtoroshiSettingsNotFound))
      case Some(otoSettings) =>
        implicit val otoroshiSettings: OtoroshiSettings = otoSettings
        plan.otoroshiTarget.map(_.serviceGroup) match {
          case None => Future.successful(Left(ApiNotLinked))
          case Some(groupId) =>
            otoroshiClient
              .getServiceGroup(groupId.value)
              .flatMap(group => deleteKey(api, team, group))
        }
    }
  }

  def archiveApiKey(tenant: Tenant,
                    subscription: ApiSubscription,
                    plan: UsagePlan,
                    api: Api,
                    team: Team,
                    enabled: Boolean): Future[Either[AppError, JsObject]] = {
    import cats.implicits._

    plan.otoroshiTarget.map(_.otoroshiSettings).flatMap { id =>
      tenant.otoroshiSettings.find(_.id == id)
    } match {
      case None => Future.successful(Left(OtoroshiSettingsNotFound))
      case Some(otoSettings) =>
        implicit val otoroshiSettings: OtoroshiSettings = otoSettings
        plan.otoroshiTarget.map(_.serviceGroup) match {
          case None => Future.successful(Left(ApiNotLinked))
          case Some(groupId) =>
            otoroshiClient
              .getServiceGroup(groupId.value)
              .flatMap(group => {
                val groupId = (group \ "id").as[String]

                val r: EitherT[Future, AppError, JsObject] = for {
                  apiKey <- EitherT(
                    otoroshiClient.getApikey(groupId,
                                             subscription.apiKey.clientId))
                    .leftMap(err => OtoroshiError(JsError.toJson(err)))
                  _ <- EitherT.liftF(
                    otoroshiClient.updateApiKey(groupId,
                                                apiKey.copy(enabled = enabled)))
                  _ <- EitherT.liftF(
                    env.dataStore.apiSubscriptionRepo
                      .forTenant(tenant.id)
                      .save(subscription.copy(enabled = enabled))
                  )
                } yield {
                  Json.obj("enabled" -> enabled,
                           "subscriptionId" -> subscription.id.asJson)
                }

                r.value
              })
        }
    }
  }
}
