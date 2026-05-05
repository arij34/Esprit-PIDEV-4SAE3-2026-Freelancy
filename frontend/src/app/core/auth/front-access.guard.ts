import { Injectable } from '@angular/core';
import { CanActivate, CanActivateChild, Router, UrlTree } from '@angular/router';
import { AuthService } from './auth.service';
import { KC_ROLES } from './roles';

@Injectable({
  providedIn: 'root'
})
export class FrontAccessGuard implements CanActivate, CanActivateChild {
  constructor(
    private readonly auth: AuthService,
    private readonly router: Router
  ) {}

  async canActivate(): Promise<boolean | UrlTree> {
    return this.checkAccess();
  }

  async canActivateChild(): Promise<boolean | UrlTree> {
    return this.checkAccess();
  }

  private async checkAccess(): Promise<boolean | UrlTree> {
    const isLoggedIn = await this.auth.isLoggedIn();

    if (!isLoggedIn) {
      return true;
    }

    if (this.auth.hasRole(KC_ROLES.ADMIN)) {
      return this.router.parseUrl('/admin/dashboard');
    }

    return true;
  }
}
