import {
  LOGIN,
  LOGOUT,
  IMPERSONATE,
  UPDATE_TEAM,
  UPDATE_NOTIFS,
  UPDATE_LANGUAGE,
} from './action-types';

const initialState = {
  impersonator: null,
  connectedUser: null,
  currentTeam: null,
  unreadNotificationsCount: 0,
  tenant: null,
  history: null,
  currentLanguage: 'En',
};

export function contextReducer(state = initialState, action) {
  switch (action.type) {
    case LOGIN:
      return {
        history: state.history,
        connectedUser: action.user,
        currentTeam: action.team,
        tenant: action.tenant,
        currentLanguage: action.language,
      };

    case LOGOUT:
      return { history: state.history, ...initialState };

    case IMPERSONATE:
      return { ...state, impersonator: action.impersonator };

    case UPDATE_TEAM:
      return {
        ...state,
        currentTeam: action.team,
        unreadNotificationsCount: action.unreadNotificationsCount,
      };

    case UPDATE_NOTIFS:
      return { ...state, unreadNotificationsCount: action.unreadNotificationsCount };

    case UPDATE_LANGUAGE:
      return { ...state, currentLanguage: action.language };

    default:
      return state;
  }
}
