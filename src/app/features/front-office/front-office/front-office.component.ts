import { Component, OnInit } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-front-office',
  templateUrl: './front-office.component.html',
  styleUrl: './front-office.component.css'
})
export class FrontOfficeComponent implements OnInit {
  isChildRoute = false;
  showChildHeader = true;

  constructor(private readonly router: Router) {}

  ngOnInit(): void {
    this.router.events.pipe(
      filter((event) => event instanceof NavigationEnd)
    ).subscribe((event: NavigationEnd) => {
      this.updateRouteState(event.urlAfterRedirects);
    });

    this.updateRouteState(this.router.url);
  }

  private updateRouteState(url: string): void {
    this.isChildRoute = url !== '/front' && url !== '/front/';
    this.showChildHeader = !this.isBlogRoute(url);
  }

  private isBlogRoute(url: string): boolean {
    return url.startsWith('/front/blog') || url.startsWith('/front/blog-analytics');
  }

  private rolePrefixFromUrl(): 'client' | 'freelancer' {
    const firstSegment = this.router.url.split('/').filter(Boolean)[0];
    return firstSegment === 'freelancer' ? 'freelancer' : 'client';
  }

  goToSubscription(): void {
    const role = this.rolePrefixFromUrl();
    this.router.navigate(['/', role, 'subscription']);
  }

  goToSubscriptionPayment(): void {
    const role = this.rolePrefixFromUrl();
    this.router.navigate(['/', role, 'subscription', 'pay']);
  }
}
