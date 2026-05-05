import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-navigation',
  templateUrl: './navigation.component.html',
  styleUrls: ['./navigation.component.css']
})
export class NavigationComponent {
  isMenuOpen = false;
  constructor(private router: Router) {}

  get isBlogRoute(): boolean {
    const url = this.router.url || '';
    return url.startsWith('/front/blog') || url.startsWith('/front/blog-analytics') || url.startsWith('/blog');
  }


  toggleMenu(): void {
    this.isMenuOpen = !this.isMenuOpen;
  }

  logout(): void {
  }
}
