export const environment = {
  production: false,

  // API Gateway (proxy Angular)
  apiUrl: '/api',
  apiBaseUrl: '',
  userApiBaseUrl: '',

  // Keycloak (local dev)
  keycloakUrl: 'http://localhost:8081',
  keycloakRealm: 'smart-platform',
  keycloakClientId: 'angular-app',

  // Challenges
  challengeTasksPath: '/tasks/challenge/{id}',
  addTaskPath: '/tasks/{id}',
  appName: 'ChallengePro',
  version: '1.0.0',
  projectApiUrl: '',

  // WebSocket skill-management (proxied through Angular dev server)
  wsUrl: ''
};