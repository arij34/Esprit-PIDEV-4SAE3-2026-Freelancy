import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-back-office',
  templateUrl: './back-office.component.html',
  styleUrl: './back-office.component.css'
})
export class BackOfficeComponent implements OnInit {
  activeTab: string = 'dashboard';
  sidebarOpen: boolean = false;
  isMobile: boolean = false;

  constructor(private router: Router, private route: ActivatedRoute) {}

  ngOnInit() {
    this.checkMobile();
    window.addEventListener('resize', () => this.checkMobile());
    this.route.queryParams.subscribe(params => {
      if (params['tab']) {
        this.activeTab = params['tab'];
      }
    });
  }

  checkMobile() {
    this.isMobile = window.innerWidth < 1024;
  }

  onTabChange(tab: string) {
    if (tab === 'Challenge') {
      this.router.navigate(['/admin/challenges']);
      return;
    }
    this.activeTab = tab;
    if (this.isMobile) {
      this.sidebarOpen = false;
    }
  }

  onMenuClick() {
    this.sidebarOpen = true;
  }

  onToggleSidebar() {
    this.sidebarOpen = !this.sidebarOpen;
  }
}