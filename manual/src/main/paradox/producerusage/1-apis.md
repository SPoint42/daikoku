#Managing APIs

## Create a new API
To create a new API, you can click on the `+ API` button in the catalog page or the `Create new API` button in the Team's APIs page in your back offices' team.
After clicking on the button localized in the catalog page, you need to choose a team which is the owner.

### API informations
An API needs a name to be created.
Then, you can add a small description, which will be displayed in the corresponding catalog's page.
The API can be published or not. In the latter case, you can consider this as a draft.

### Versions and tags
These are pure informations like current version of the API and supported versions.
Tags and categories are array of item, mostly used to filter APIs.

### Visibililty and subscription
Visibility can be:

* public: everybody can see the complete API.
* public with authorization: everybody sees just a part of the API, on the catalog's page (name, tags, categories and small desc.). Everybody can ask access to an admin of owner team.
* private: Just authorized teams have access to the API.

Subscription can be: 

* Automatic: API subscriptions will be granted automatically.
* Manual: API subscriptions will require an acceptance by a team admin.


### Authorizations
The teams which have access to the API, in the case of visibility is private.

### Description
API description. Basically it can be written in markdown.
The description can be set from team asset.

### Plans
An API needs a plan to be subscribed.
Plan needs a name, possibly a description and an @ref:[otoroshi instance](../tenantusage/1-otoroshi.md).
It's possible to allow multiple APIkeys for a plan.

It's important to choose a type of plan :

* free without quotas: a plan with an unlimited number of calls per day and per month.
* free with quotas: a plan with a limited number of calls per day and per month. Quotas will be set by default and can be overwritten.
* quotas with limit: a priced plan with a limited number of calls per day and per month. Quotas will be set by default but can be overwritten. A fixed cost by month can be set. 
* quotas without limit: a priced plan with unlimited number of calls per day and per month. Quotas will be set by default but can be overwritten. A fixed cost by month can be set. The cost per additional requests can be set.
* pay per use: a plan priced on usage. A fixed cost by month can be set. The cost per additional requests can be set.

### Swagger
The swagger can be provided as a url or just some content paste on the UI.

### Testing
You can enable the testing for your API.
Click on the `Generate a dedicated testing key in Otoroshi` to choose an otoroshi instance and and service group which is used to receive the testing APIkey.

@@@warning
Make sure this service descriptor is the right one for testing and not your production system !
@@@

### Documentation
The last tabs allows you to create a paginated documentation. Like description every pages can be written with markdown or set from asset.