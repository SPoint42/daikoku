import React, { Component } from 'react';
import { Link } from 'react-router-dom';
import { connect } from 'react-redux';

import * as Services from '../../../services';
import { TeamBackOffice } from '..';
import { Table } from '../../inputs';
import { Can, read, apikey, isUserIsTeamAdmin } from '../../utils';
import { t, Translation } from '../../../locales';

export class TeamApiKeysComponent extends Component {
  state = {
    showApiKey:
      this.props.connectedUser.isDaikokuAdmin ||
      !this.props.currentTeam.showApiKeyOnlyToAdmins ||
      isUserIsTeamAdmin(this.props.connectedUser, this.props.currentTeam),
  };

  columns = [
    {
      title: t('Api Name', this.props.currentLanguage),
      style: { textAlign: 'left', alignItems: 'center', display: 'flex' },
      content: api => api.name,
    },
    {
      title: t('Version', this.props.currentLanguage),
      style: { textAlign: 'left', alignItems: 'center', display: 'flex' },
      content: api => api.currentVersion,
    },
    {
      title: t('Actions', this.props.currentLanguage),
      style: { textAlign: 'center', width: 150, alignItems: 'center', display: 'flex' },
      notFilterable: true,
      content: item => item._id,
      cell: (a, api) =>
        this.state.showApiKey && (
          <div style={{ width: 100 }}>
            <Link
              to={`/${this.props.currentTeam._humanReadableId}/settings/apikeys/${api._humanReadableId}`}
              className="btn btn-sm btn-access-negative">
              <i className="fas fa-eye mr-1" />
              <Translation i18nkey="Api keys" language={this.props.currentLanguage}>
                Api keys
              </Translation>
            </Link>
          </div>
        ),
    },
  ];

  cleanSubs = () => {
    window
      .confirm(
        t(
          'clean.archived.sub.confirm',
          this.props.currentLanguage,
          'Are you sure you want to clean archived subscriptions ?'
        )
      )
      .then(ok => {
        if (ok) {
          Services.cleanArchivedSubscriptions(this.props.currentTeam._id).then(() =>
            this.table.update()
          );
        }
      });
  };

  render() {
    return (
      <TeamBackOffice tab="ApiKeys" apiId={this.props.match.params.apiId}>
        <Can I={read} a={apikey} team={this.props.currentTeam} dispatchError={true}>
          <div className="row">
            <div className="col">
              <h1>
                <Translation i18nkey="Subscribed Apis" language={this.props.currentLanguage}>
                  Subscribed Apis
                </Translation>
              </h1>
              <Link
                to={`/${this.props.currentTeam._humanReadableId}/settings/consumption`}
                className="btn btn-sm btn-access-negative">
                <i className="fas fa-chart-bar mr-1" />
                <Translation i18nkey="See Stats" language={this.props.currentLanguage}>
                  See Stats
                </Translation>
              </Link>
              <Table
                currentLanguage={this.props.currentLanguage}
                selfUrl="apikeys"
                defaultTitle="Apikeys"
                defaultValue={() => ({})}
                itemName="apikey"
                columns={this.columns}
                fetchItems={() => Services.subscribedApis(this.props.currentTeam._id)}
                showActions={false}
                showLink={false}
                extractKey={item => item._id}
                injectTable={t => (this.table = t)}
              />
              <button className="btn btn-sm btn-danger-negative mt-1" onClick={this.cleanSubs}>
                <Translation i18nkey="clean archived apikeys" language={this.props.currentLanguage}>
                  clean archived apikeys
                </Translation>
              </button>
            </div>
          </div>
        </Can>
      </TeamBackOffice>
    );
  }
}

const mapStateToProps = state => ({
  ...state.context,
});

export const TeamApiKeys = connect(mapStateToProps)(TeamApiKeysComponent);
