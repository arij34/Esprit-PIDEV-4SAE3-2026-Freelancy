import { KeycloakService } from 'keycloak-angular';
import { environment } from '../../../environments/environment';

/**
 * Initializes Keycloak before Angular bootstraps.
 *
 * NOTE: `onLoad: 'login-required'` forces a redirect to Keycloak login
 * as soon as the app loads.
 */
export function initializeKeycloak(keycloak: KeycloakService) {
  return () => {
    const initPromise = keycloak.init({
      config: {
        url: environment.keycloakUrl,
        realm: environment.keycloakRealm,
        clientId: environment.keycloakClientId
      },
      initOptions: {
        // Do NOT force login on first load (so /front can be visited anonymously).
        // Protected routes (/admin, /client, /freelancer) still redirect to Keycloak via RoleGuard.
        onLoad: 'check-sso',
        silentCheckSsoRedirectUri: window.location.origin + '/assets/silent-check-sso.html',
        checkLoginIframe: false
      },
      // We enable the bearer interceptor globally and use an allowlist.
      enableBearerInterceptor: true,
      bearerPrefix: 'Bearer',
      bearerExcludedUrls: [
        // static assets
        '/assets',
        // backend public endpoints (no token required)
        '/api/public',
        // Exclude blog and analytics services from Keycloak bearer interceptor
        'http://localhost:8050',
        'http://localhost:8053'
      ]
    });

    // IMPORTANT:
    // In some setups, silent SSO check can hang forever (neither resolve nor reject)
    // which blocks APP_INITIALIZER => blank page. We enforce a short timeout.
    const timeoutPromise = new Promise<boolean>((resolve) =>
      setTimeout(() => resolve(true), 2500)
    );

    return Promise.race([initPromise, timeoutPromise]).catch((err) => {
      console.error('Keycloak init failed. Continuing without SSO.', err);
      return true;
    });
  };
}
