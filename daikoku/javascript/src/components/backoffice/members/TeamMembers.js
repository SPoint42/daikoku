import React, { Component } from 'react';
import { Redirect } from 'react-router-dom';
import Select from 'react-select';
import { connect } from 'react-redux';
import _ from 'lodash';
import { toastr } from 'react-redux-toastr';

import * as Services from '../../../services';
import { TeamBackOffice } from '..';
import { SwitchButton } from '../../inputs';
import { updateTeamPromise } from '../../../core/context';
import {
  Option,
  PaginatedComponent,
  AvatarWithAction,
  Can,
  manage,
  team,
  administrator,
  apiEditor,
  user,
} from '../../utils';
import { t, Translation } from '../../../locales';

import 'antd/lib/tooltip/style/index.css';

export class TeamMembersSimpleComponent extends Component {
  state = {
    addableMembers: [],
    selectedMember: null,
    loading: true,
  };

  componentDidMount() {
    this.updateMembers(this.props.currentTeam);
  }

  updateMembers = team => {
    Promise.all([Services.members(team._id), Services.addableUsersForTeam(team._id)]).then(
      ([members, addableMembers]) => {
        this.setState({
          addableMembers: addableMembers.map(m => ({
            label: (
              <div
                style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                {m.name} ({m.email}){' '}
                <img
                  style={{ borderRadius: '50%', backgroundColor: 'white', width: 34, height: 34 }}
                  src={m.picture}
                  alt="avatar"
                />
              </div>
            ),
            value: m,
          })),
          members,
          loading: false,
        });
      }
    );
  };

  isAdmin = user => {
    if (!this.props.currentTeam) {
      return false;
    }

    if (user.isDaikokuAdmin) {
      return true;
    }

    return Option(this.props.currentTeam.users.find(u => u.userId === user._id))
      .map(user => user.teamPermission)
      .fold(
        () => false,
        perm => perm === administrator
      );
  };

  userHavePemission = (user, permission) => {
    return Option(this.props.currentTeam.users.find(u => u.userId === user._id))
      .map(user => user.teamPermission)
      .fold(
        () => false,
        perm => perm === permission
      );
  };

  removeMember = member => {
    if (
      this.isAdmin(member) &&
      this.props.currentTeam.users.filter(u => u.teamPermission === administrator).length === 1
    ) {
      alert(
        t(
          'remove.member.alert',
          this.props.currentLanguage,
          false,
          "You can't delete this user, it must remain an admin in a team."
        )
      );
    } else {
      window
        .confirm(
          t(
            'remove.member.confirm',
            this.props.currentLanguage,
            false,
            'Are you sure you want to remove this member from the team ?'
          )
        )
        .then(ok => {
          if (ok) {
            const teamId = this.props.currentTeam._id;
            Services.removeMemberFromTeam(teamId, member._id).then(({ done, team }) => {
              done
                ? toastr.success(
                    'Success',
                    t(
                      'remove.member.success',
                      this.props.currentLanguage,
                      false,
                      `${member.name} is no longer member of your team`,
                      member.name
                    )
                  )
                : toastr.error('Failure');
              this.props.updateTeam(team).then(() => this.updateMembers(this.props.currentTeam));
            });
          }
        });
    }
  };

  addMember = slug => {
    const member = slug.value;
    this.setState({ selectedMember: member }, () => {
      const teamId = this.props.currentTeam._id;
      Services.addMembersToTeam(teamId, [member._id]).then(({ done, team }) => {
        this.setState({ selectedMember: null }, () => {
          done
            ? toastr.success(
                'Success',
                t(
                  'member.now.member',
                  this.props.currentLanguage,
                  false,
                  `${member.name} is now member of your team`,
                  member.name
                )
              )
            : toastr.error('Failure');
          this.props.updateTeam(team).then(() => this.updateMembers(this.props.currentTeam));
        });
      });
    });
  };

  togglePermission = (member, permission) => {
    if (this.isAdmin(this.props.connectedUser)) {
      const teamId = this.props.currentTeam._id;
      if (
        this.userHavePemission(member, administrator) &&
        this.props.currentTeam.users.filter(u => u.teamPermission === administrator).length === 1
      ) {
        alert(
          t(
            'remove.admin.alert',
            this.props.currentLanguage,
            "You can't remove this admin status, it must remain an admin in a team."
          )
        );
      } else {
        const newPermission = this.userHavePemission(member, permission) ? user : permission;
        Services.updateTeamMemberPermission(teamId, [member._id], newPermission).then(
          ({ done, team }) => {
            done
              ? toastr.success(
                  'Success',
                  t(
                    'member.new.permission.success',
                    this.props.currentLanguage,
                    false,
                    `${member.name} is now ${newPermission}`,
                    member.name,
                    newPermission
                  )
                )
              : toastr.error('Failure');
            this.props.updateTeam(team).then(() => this.updateMembers(this.props.currentTeam));
          }
        );
      }
    } else {
      window.alert(
        t(
          'not.admin.alert',
          this.props.currentLanguage,
          "Your are not an administrator. You can't do that."
        )
      );
    }
  };

  updateApiKeysVisibility = onlyForAdmins => {
    return Services.updateApiKeysVisibility(
      this.props.currentTeam._id,
      onlyForAdmins
    ).then(({ team }) => this.props.updateTeam(team));
  };

  render() {
    if (this.props.currentTeam.type === 'Personal') {
      return <Redirect to="/settings/me" />;
    }

    if (!this.state.members) {
      return null;
    }

    const filteredMembers = this.state.search
      ? this.state.members.filter(({ name, email }) =>
          [name, email].some(value => value.toLowerCase().includes(this.state.search))
        )
      : this.state.members;

    return (
      <Can I={manage} a={team} team={this.props.currentTeam} dispatchError={true}>
        <div className="row">
          <div className="col">
            <h1>
              <Translation
                i18nkey="team.members.title"
                language={this.props.currentLanguage}
                replacements={[this.props.currentTeam.name]}>
                {this.props.currentTeam.name} members
              </Translation>
            </h1>
          </div>
        </div>
        <div className="row">
          <div className="col-12 mb-3 d-flex justify-content-start">
            <Select
              placeholder={t('Add new member to the team ...', this.props.currentLanguage)}
              className="add-member-select mr-2"
              classNamePrefix="select"
              options={this.state.addableMembers}
              onChange={this.addMember}
              value={this.state.selectedMember}
              filterOption={(data, search) => _.values(data.value).some(v => v.includes(search))}
            />
            <input
              placeholder={t('Find a member', this.props.currentLanguage)}
              className="form-control"
              onChange={e => {
                this.setState({ search: e.target.value });
              }}
            />
          </div>
          <Can I={manage} a={team} team={this.props.currentTeam}>
            <div className="col-12 mb-3 d-flex justify-content-between">
              <SwitchButton
                label={t('Api keys are just visible by admins', this.props.currentLanguage)}
                onSwitch={this.updateApiKeysVisibility}
                checked={this.props.currentTeam.showApiKeyOnlyToAdmins}
                large
                noText
              />
            </div>
          </Can>
        </div>
        <PaginatedComponent
          currentLanguage={this.props.currentLanguage}
          items={_.sortBy(filteredMembers, [member => member.name.toLowerCase()])}
          count={15}
          formatter={member => {
            const isAdmin = this.userHavePemission(member, administrator);
            const isApiEditor = this.userHavePemission(member, apiEditor);
            return (
              <AvatarWithAction
                key={member._id}
                avatar={member.picture}
                infos={
                  <>
                    {this.userHavePemission(member, administrator) && (
                      <i className="fas fa-shield-alt" style={{ marginRight: '10px' }} />
                    )}
                    {this.userHavePemission(member, apiEditor) && (
                      <i className="fas fa-pencil-alt" style={{ marginRight: '10px' }} />
                    )}
                    <span className="team-member__name">{member.name}</span>
                  </>
                }
                actions={[
                  {
                    action: () => this.removeMember(member),
                    iconClass: 'fas fa-trash delete-icon',
                    tooltip: t('Remove member', this.props.currentLanguage),
                  },
                  {
                    action: [
                      {
                        action: () => this.togglePermission(member, administrator),
                        iconClass: `fas fa-shield-alt ${
                          isAdmin ? 'admin-active' : 'admin-inactive'
                        }`,
                        tooltip: `${
                          isAdmin
                            ? t('Remove administrator status', this.props.currentLanguage)
                            : t('Add administrator status', this.props.currentLanguage)
                        }`,
                      },
                      {
                        action: () => this.togglePermission(member, apiEditor),
                        iconClass: `fas fa-pencil-alt ${
                          isApiEditor ? 'admin-active' : 'admin-inactive'
                        }`,
                        tooltip: `${
                          isApiEditor
                            ? t('Remove api editor status', this.props.currentLanguage)
                            : t('Add api editor status', this.props.currentLanguage)
                        }`,
                      },
                    ],
                    iconClass: 'fas fa-user-cog',
                    tooltip: t('Manage permissions', this.props.currentLanguage),
                  },
                ]}
              />
            );
          }}
        />
      </Can>
    );
  }
}

class TeamMembersComponent extends Component {
  render() {
    return (
      <TeamBackOffice tab="Members" apiId={this.props.match.params.apiId}>
        <TeamMembersSimpleComponent {...this.props} />
      </TeamBackOffice>
    );
  }
}

const mapStateToProps = state => ({
  ...state.context,
});

const mapDispatchToProps = {
  updateTeam: team => updateTeamPromise(team),
};

export const TeamMembers = connect(mapStateToProps, mapDispatchToProps)(TeamMembersComponent);
