export function currentTenant(team) {
  return fetch(`/api/teams/${team}/tenant`, {
    method: 'GET',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
    },
  }).then(r => r.json());
}

export function me() {
  return fetch('/api/me', {
    method: 'GET',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
    },
  }).then(r => r.json());
}

export function myOwnTeam() {
  return fetch('/api/me/teams/own', {
    method: 'GET',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
    },
  }).then(r => r.json());
}

//Restricted api with all of property
export function oneOfMyTeam(id) {
  return fetch(`/api/me/teams/${id}`, {
    method: 'GET',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
    },
  }).then(r => r.json());
}

export function getVisibleApi(id) {
  return fetch(`/api/me/visible-apis/${id}`, {
    method: 'GET',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
    },
  }).then(r => r.json());
}

export function getTeamVisibleApi(teamId, apiId) {
  return fetch(`/api/me/teams/${teamId}/visible-apis/${apiId}`, {
    method: 'GET',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
    },
  }).then(r => r.json());
}

export function myTeams() {
  return fetch('/api/me/teams', {
    method: 'GET',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
    },
  }).then(r => r.json());
}

export function allJoinableTeams() {
  return fetch('/api/teams/joinable', {
    method: 'GET',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
    },
  }).then(r => r.json());
}

export function myVisibleApis() {
  return fetch('/api/me/visible-apis', {
    method: 'GET',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
    },
  }).then(r => r.json());
}

export function myVisibleApisOfTeam(currentTeam) {
  return fetch(`/api/teams/${currentTeam}/visible-apis`, {
    method: 'GET',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
    },
  }).then(r => r.json());
}

export function teamAllNotifications(teamId, page = 0) {
  return fetch(`/api/teams/${teamId}/notifications/all?page=${page}`, {
    method: 'GET',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
    },
  }).then(r => r.json());
}

export function teamNotifications(teamId) {
  return fetch(`/api/teams/${teamId}/notifications`, {
    method: 'GET',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
    },
  }).then(r => r.json());
}

export function teamUnreadNotificationsCount(teamId) {
  return fetch(`/api/teams/${teamId}/notifications/unread-count`, {
    method: 'GET',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
    },
  }).then(
    r => {
      if (r.status === 200) {
        return r.json();
      } else {
        return { count: 0 };
      }
    },
    () => ({ count: 0 })
  );
}

export function myAllNotifications(page = 0, pageSize = 10) {
  return fetch(`/api/me/notifications/all?page=${page}&pageSize=${pageSize}`, {
    method: 'GET',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
    },
  }).then(r => r.json());
}

export function myNotifications(page = 0, pageSize = 10) {
  return fetch(`/api/me/notifications?page=${page}&pageSize=${pageSize}`, {
    method: 'GET',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
    },
  }).then(r => r.json());
}

export function myUnreadNotificationsCount() {
  return fetch('/api/me/notifications/unread-count', {
    method: 'GET',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
    },
  }).then(
    r => {
      if (r.status === 200) {
        return r.json();
      } else {
        return { count: 0 };
      }
    },
    () => ({ count: 0 })
  );
}

export function acceptNotificationOfTeam(teamId, NotificationId) {
  return fetch(`/api/teams/${teamId}/notifications/${NotificationId}/accept`, {
    method: 'PUT',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
    },
  }).then(r => r.json());
}

export function rejectNotificationOfTeam(teamId, NotificationId) {
  return fetch(`/api/teams/${teamId}/notifications/${NotificationId}/reject`, {
    method: 'PUT',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
    },
  }).then(r => r.json());
}

export function subscribedApis(team) {
  return fetch(`/api/teams/${team}/subscribed-apis`, {
    method: 'GET',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
    },
  }).then(r => r.json());
}

export function getDocPage(api, id) {
  return fetch(`/api/apis/${api}/pages/${id}`, {
    method: 'GET',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
    },
  }).then(r => r.json());
}

export function getDocDetails(api) {
  return fetch(`/api/apis/${api}/doc`, {
    method: 'GET',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
    },
  }).then(r => r.json());
}

export function reorderDoc(team, api) {
  return fetch(`/api/teams/${team}/apis/${api}/pages/_reorder`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
    body: '{}',
  }).then(r => r.json());
}

export function getTeamSubscriptions(api, team) {
  return fetch(`/api/apis/${api}/subscriptions/teams/${team}`, {
    method: 'GET',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
    },
  }).then(r => r.json());
}

export function getMySubscriptions(apiId) {
  return fetch(`/api/me/subscriptions/${apiId}`, {
    method: 'GET',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
    },
  }).then(r => r.json());
}

export function askForApiKey(api, teams, plan) {
  return fetch(`/api/apis/${api}/subscriptions`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ plan, teams }),
  }).then(r => r.json());
}

export function deleteApiKey(teamId, subscriptionId) {
  return fetch(`/api/teams/${teamId}/subscriptions/${subscriptionId}/_delete`, {
    method: 'DELETE',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
  }).then(r => r.json());
}

export function archiveApiKey(teamId, subscriptionId, enable) {
  return fetch(`/api/teams/${teamId}/subscriptions/${subscriptionId}/_archive?enabled=${enable}`, {
    method: 'PUT',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
  }).then(r => r.json());
}

export function cleanArchivedSubscriptions(teamId) {
  return fetch(`/api/teams/${teamId}/subscriptions/_clean`, {
    method: 'DELETE',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
  }).then(r => r.json());
}

export function member(teamId, userId) {
  return fetch(`/api/teams/${teamId}/members/${userId}`, {
    method: 'GET',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
    },
  }).then(r => r.json());
}

export function members(teamId) {
  return fetch(`/api/teams/${teamId}/members`, {
    method: 'GET',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
    },
  }).then(r => r.json());
}

export function teamHome(teamId) {
  return fetch(`/api/teams/${teamId}/home`, {
    method: 'GET',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
    },
  }).then(r => r.json());
}

export function teamApi(teamId, apiId) {
  return fetch(`/api/teams/${teamId}/apis/${apiId}`, {
    method: 'GET',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
    },
  }).then(r => r.json());
}

export function teamApis(teamId) {
  return fetch(`/api/teams/${teamId}/apis`, {
    method: 'GET',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
    },
  }).then(r => r.json());
}

//public api with restricted property
export function team(teamId) {
  return fetch(`/api/teams/${teamId}`, {
    method: 'GET',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
    },
  }).then(r => r.json());
}

export function teamFull(teamId) {
  return fetch(`/api/teams/${teamId}/_full`, {
    method: 'GET',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
    },
  }).then(r => r.json());
}

export function teams() {
  return fetch('/api/teams', {
    method: 'GET',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
    },
  }).then(r => r.json());
}

export function createTeam(team) {
  return fetch('/api/teams', {
    method: 'POST',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(team),
  }).then(r => r.json());
}

export function updateTeam(team) {
  return fetch(`/api/teams/${team._id}`, {
    method: 'PUT',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(team),
  }).then(r => r.json());
}

export function deleteTeam(teamId) {
  return fetch(`/api/teams/${teamId}`, {
    method: 'DELETE',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
    },
  }).then(r => r.json());
}

export function addableUsersForTeam(teamId) {
  return fetch(`/api/teams/${teamId}/addable-members`, {
    method: 'GET',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
    },
  }).then(r => r.json());
}

export function allOtoroshis() {
  return fetch('/api/teams/otoroshis', {
    method: 'GET',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
    },
  }).then(r => r.json());
}

export function oneOtoroshi(id) {
  return fetch(`/api/teams/otoroshis/${id}`, {
    method: 'GET',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
    },
  }).then(r => r.json());
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////

export function deleteOtoroshiSettings(id) {
  return fetch(`/api/teams/otoroshis/${id}`, {
    method: 'DELETE',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
    },
  }).then(r => r.json());
}

export function saveOtoroshiSettings(oto) {
  return fetch(`/api/teams/otoroshis/${oto._id}`, {
    method: 'PUT',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(oto),
  }).then(r => r.json());
}

export function createOtoroshiSettings(oto) {
  return fetch('/api/teams/otoroshis', {
    method: 'POST',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(oto),
  }).then(r => r.json());
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////

export function deleteTeamApi(teamId, id) {
  return fetch(`/api/teams/${teamId}/apis/${id}`, {
    method: 'DELETE',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
    },
  }).then(r => r.json());
}

export function saveTeamApi(teamId, api) {
  return fetch(`/api/teams/${teamId}/apis/${api._id}`, {
    method: 'PUT',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(api),
  }).then(r => r.json());
}

export function createTeamApi(teamId, api) {
  return fetch(`/api/teams/${teamId}/apis`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(api),
  }).then(r => r.json());
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////

export function removeMemberFromTeam(teamId, userId) {
  return fetch(`/api/teams/${teamId}/members/${userId}`, {
    method: 'DELETE',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
    },
  }).then(r => r.json());
}

export function addMembersToTeam(teamId, members) {
  return fetch(`/api/teams/${teamId}/members`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ members }),
  }).then(r => r.json());
}

export function updateTeamMemberPermission(teamId, members, permission) {
  return fetch(`/api/teams/${teamId}/members/_permission`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ members, permission }),
  }).then(r => r.json());
}

export function createDocPage(teamId, apiId, page) {
  return fetch(`/api/teams/${teamId}/pages`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(page),
  }).then(r => r.json());
}

export function deleteDocPage(teamId, apiId, pageId) {
  return fetch(`/api/teams/${teamId}/pages/${pageId}`, {
    method: 'DELETE',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
    },
  }).then(r => r.json());
}

export function saveDocPage(teamId, apiId, page) {
  return fetch(`/api/teams/${teamId}/pages/${page._id}`, {
    method: 'PUT',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(page),
  }).then(r => r.json());
}

export function allTenants() {
  return fetch('/api/tenants', {
    method: 'GET',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
    },
  }).then(r => r.json());
}

export function oneTenant(tenant) {
  return fetch(`/api/tenants/${tenant}`, {
    method: 'GET',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
    },
  }).then(r => r.json());
}

export function createTenant(tenant) {
  return fetch('/api/tenants', {
    method: 'POST',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(tenant),
  }).then(r => r.json());
}

export function saveTenant(tenant) {
  return fetch(`/api/tenants/${tenant._id}`, {
    method: 'PUT',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(tenant),
  }).then(r => r.json());
}

export function deleteTenant(id) {
  return fetch(`/api/tenants/${id}`, {
    method: 'DELETE',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
  }).then(r => r.json());
}

export function askToJoinTeam(team) {
  return fetch(`/api/teams/${team}/join`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
  }).then(r => r.json());
}

export function askForApiAccess(teams, api) {
  return fetch(`/api/apis/${api}/access`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ teams }),
  }).then(r => r.json());
}

export function updateApiKeysVisibility(teamId, showApiKeyOnlyToAdmins) {
  return fetch(`/api/teams/${teamId}/apiKeys/visibility`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ showApiKeyOnlyToAdmins }),
  }).then(r => r.json());
}

export function fetchAuditTrail(from, to, page, size) {
  return fetch(`/api/admin/auditTrail?from=${from}&to=${to}&page=${page}&size=${size}`, {
    method: 'GET',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
  }).then(r => r.json());
}

////////////////////////////////////////////////////////////////////////////////////////////////

export function fetchAllUsers() {
  return fetch('/api/admin/users', {
    method: 'GET',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
  }).then(r => r.json());
}

export function findUserById(id) {
  return fetch(`/api/admin/users/${id}`, {
    method: 'GET',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
  }).then(r => r.json());
}

export function deleteUserById(id) {
  return fetch(`/api/admin/users/${id}`, {
    method: 'DELETE',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
  }).then(r => r.json());
}

export function deleteSelfUserById() {
  return fetch('/api/me', {
    method: 'DELETE',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
  }).then(r => r.json());
}

export function updateUserById(user) {
  return fetch(`/api/admin/users/${user._id}`, {
    method: 'PUT',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(user),
  }).then(r => r.json());
}

export function createUser(user) {
  return fetch('/api/admin/users', {
    method: 'POST',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(user),
  }).then(r => r.json());
}

export function simpleTenantList() {
  return fetch('/api/tenants/simplified', {
    method: 'GET',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
  }).then(r => r.json());
}

export function redirectToTenant(id) {
  return fetch(`/api/tenants/${id}/_redirect`, {
    method: 'GET',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
  }).then(r => r.json());
}

export function getTenantNames(ids) {
  return fetch('/api/tenants/_names', {
    method: 'POST',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(ids),
  }).then(r => r.json());
}

function fetchEntity(url) {
  return fetch(url, {
    method: 'GET',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
  }).then(r => r.json());
}

export const fetchNewTenant = () => fetchEntity('/api/entities/tenant');
export const fetchNewTeam = () => fetchEntity('/api/entities/team');
export const fetchNewApi = () => fetchEntity('/api/entities/api');
export const fetchNewUser = () => fetchEntity('/api/entities/user');
export const fetchNewOtoroshi = () => fetchEntity('/api/entities/otoroshi');

export function checkIfApiNameIsUnique(teamId, name) {
  return fetch(`/api/teams/${teamId}/apis/_names`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ name }),
  }).then(r => r.json());
}

export function getSessions() {
  return fetch('/api/admin/sessions', {
    method: 'GET',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
  }).then(r => r.json());
}

export function deleteSession(id) {
  return fetch(`/api/admin/sessions/${id}`, {
    method: 'DELETE',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
  }).then(r => r.json());
}

export function deleteSessions() {
  return fetch('/api/admin/sessions', {
    method: 'DELETE',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
  }).then(r => r.json());
}

export function search(search) {
  return fetch('/api/_search', {
    method: 'POST',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ search }),
  }).then(r => r.json());
}

export function apiKeyConsumption(clientId, teamId, from, to) {
  return fetch(`/api/teams/${teamId}/apiKey/${clientId}/consumption?from=${from}&to=${to}`, {
    method: 'GET',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
  }).then(r => r.json());
}

export function syncApiKeyConsumption(clientId, teamId) {
  return fetch(`/api/teams/${teamId}/apiKey/${clientId}/consumption/_sync`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
  }).then(r => r.json());
}

export function syncApiConsumption(apiId, teamId) {
  return fetch(`/api/teams/${teamId}/apis/${apiId}/consumption/_sync`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
  }).then(r => r.json());
}

export function syncTeamBilling(teamId) {
  return fetch(`/api/teams/${teamId}/billing/_sync`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
  }).then(r => r.json());
}

export function syncTeamIncome(teamId) {
  return fetch(`/api/teams/${teamId}/income/_sync`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
  }).then(r => r.json());
}

export function apiConsumption(apiId, planId, teamId, from, to) {
  return fetch(
    `/api/teams/${teamId}/apis/${apiId}/plan/${planId}/consumption?from=${from}&to=${to}`,
    {
      method: 'GET',
      credentials: 'include',
      headers: {
        Accept: 'application/json',
        'Content-Type': 'application/json',
      },
    }
  ).then(r => r.json());
}

export function apiGlobalConsumption(apiId, teamId, from, to) {
  return fetch(`/api/teams/${teamId}/apis/${apiId}/consumption?from=${from}&to=${to}`, {
    method: 'GET',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
  }).then(r => r.json());
}

export function getPlanInformations(clientId, teamId) {
  return fetch(`/api/teams/${teamId}/apiKey/${clientId}/informations`, {
    method: 'GET',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
  }).then(r => r.json());
}

export function getTeamConsumptions(teamId, from, to) {
  return fetch(`/api/teams/${teamId}/consumptions?from=${from}&to=${to}`, {
    method: 'GET',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
  }).then(r => r.json());
}

export function getTeamBillings(teamId, from, to) {
  return fetch(`/api/teams/${teamId}/billings?from=${from}&to=${to}`, {
    method: 'GET',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
  }).then(r => r.json());
}

export function getTeamIncome(teamId, from, to) {
  return fetch(`/api/teams/${teamId}/income?from=${from}&to=${to}`, {
    method: 'GET',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
  }).then(r => r.json());
}
export function getApiCategories() {
  return fetch('/api/categories', {
    method: 'GET',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
  }).then(r => r.json());
}

/////////////////////////////////////////////////////////////////////////////////
// Assets
/////////////////////////////////////////////////////////////////////////////////

export function getAsset(teamId, assetId) {
  return fetch(`/api/teams/${teamId}/assets/${assetId}`, {
    method: 'GET',
    credentials: 'include',
    headers: {},
  }).then(r => r.json());
}

export function deleteAsset(teamId, assetId) {
  return fetch(`/api/teams/${teamId}/assets/${assetId}`, {
    method: 'DELETE',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
  }).then(r => r.json());
}

export function listAssets(teamId) {
  return fetch(`/api/teams/${teamId}/assets`, {
    method: 'GET',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
  }).then(r => r.json());
}

export function storeAsset(teamId, filename, title, desc, contentType, formData) {
  return fetch(`/api/teams/${teamId}/assets?filename=${filename}&title=${title}&desc=${desc}`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': contentType,
      'Asset-Content-Type': contentType,
      //'X-Thumbnail': thumbnail
    },
    body: formData,
  }).then(r => r.json());
}

export function updateAsset(teamId, assetId, contentType, formData) {
  return fetch(`/api/teams/${teamId}/assets/${assetId}/_replace`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      Accept: contentType,
      'Content-Type': contentType,
    },
    body: formData,
  }).then(r => r.json());
}

/////////////////////////////////////////////////////////////////////////////////
// Tenant Assets
/////////////////////////////////////////////////////////////////////////////////

export function getTenantAsset(assetId) {
  return fetch(`/tenant-assets/${assetId}`, {
    method: 'GET',
    credentials: 'include',
    headers: {},
  }).then(r => r.json());
}

export function deleteTenantAsset(assetId) {
  return fetch(`/tenant-assets/${assetId}`, {
    method: 'DELETE',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
  }).then(r => r.json());
}

export function updateTenantAsset(assetId, contentType, formData) {
  return fetch(`/tenant-assets/${assetId}/_replace`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      Accept: contentType,
      'Content-Type': contentType,
    },
    body: formData,
  }).then(r => r.json());
}

export function listTenantAssets(teamId) {
  if (teamId) {
    return fetch(`/tenant-assets?teamId=${teamId}`, {
      method: 'GET',
      credentials: 'include',
      headers: {
        Accept: 'application/json',
        'Content-Type': 'application/json',
      },
    }).then(r => r.json());
  } else {
    return fetch('/tenant-assets', {
      method: 'GET',
      credentials: 'include',
      headers: {
        Accept: 'application/json',
        'Content-Type': 'application/json',
      },
    }).then(r => r.json());
  }
}

export function storeTenantAsset(filename, title, desc, contentType, formData) {
  return fetch(`/tenant-assets?filename=${filename}&title=${title}&desc=${desc}`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': contentType,
      'Asset-Content-Type': contentType,
      //'X-Thumbnail': thumbnail
    },
    body: formData,
  }).then(r => r.json());
}

export function uploadExportFile(file) {
  return fetch('/api/state/import', {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/x-ndjson',
    },
    body: file,
  }).then(r => r.json());
}

export function updateSubscriptionCustomName(team, subscription, customName) {
  return fetch(`/api/teams/${team._id}/subscriptions/${subscription._id}/name`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ customName }),
  }).then(r => r.json());
}

export function storeThumbnail(id, formData) {
  return fetch(`/asset-thumbnails/${id}`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'image/png',
      'Asset-Content-Type': 'image/png',
    },
    body: formData,
  }).then(r => r.json());
}

export function createTestingApiKey(teamId, body) {
  return fetch(`/api/teams/${teamId}/testing/apikeys`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(body),
  }).then(r => r.json());
}

export function testingCall(teamId, apiId, body) {
  return fetch(`/api/teams/${teamId}/testing/${apiId}/call`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(body),
  }).then(r => r.json());
}

export function getTranslations() {
  return fetch('/api/translations', {
    method: 'GET',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
  }).then(r => r.json());
}

export function saveApiTranslations(teamId, apiId, translations) {
  return fetch(`/api/teams/${teamId}/apis/${apiId}/_translate`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(translations),
  }).then(r => r.json());
}

export function saveTenantTranslations(tenantId, translations) {
  return fetch(`/api/tenant/${tenantId}/_translate`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(translations),
  }).then(r => r.json());
}

export function saveTeamTranslations(teamId, translations) {
  return fetch(`/api/teams/${teamId}/_translate`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(translations),
  }).then(r => r.json());
}
