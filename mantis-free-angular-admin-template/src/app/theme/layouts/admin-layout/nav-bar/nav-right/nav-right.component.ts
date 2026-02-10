// angular import
import { Component, output, inject, input } from '@angular/core';
import { RouterModule, Router } from '@angular/router';

// project import
import { SharedModule } from 'src/app/theme/shared/shared.module';

// third party

// icon
import { IconService } from '@ant-design/icons-angular';
import {
  BellOutline,
  SettingOutline,
  GiftOutline,
  MessageOutline,
  PhoneOutline,
  CheckCircleOutline,
  LogoutOutline,
  EditOutline,
  UserOutline,
  ProfileOutline,
  WalletOutline,
  QuestionCircleOutline,
  LockOutline,
  CommentOutline,
  UnorderedListOutline,
  ArrowRightOutline,
  GithubOutline
} from '@ant-design/icons-angular/icons';

@Component({
  selector: 'app-nav-right',
  imports: [SharedModule, RouterModule],
  templateUrl: './nav-right.component.html',
  styleUrls: ['./nav-right.component.scss']
})
export class NavRightComponent {
  private iconService = inject(IconService);
  private router = inject(Router);

  // public props
  styleSelectorToggle = input<boolean>();
  readonly Customize = output();
  windowWidth: number;
  screenFull: boolean = true;
  direction: string = 'ltr';

  // user info loaded from localStorage (set at login)
  currentUser: any = null;

  // constructor
  constructor() {
    this.windowWidth = window.innerWidth;
    this.iconService.addIcon(
      ...[
        CheckCircleOutline,
        GiftOutline,
        MessageOutline,
        SettingOutline,
        PhoneOutline,
        LogoutOutline,
        EditOutline,
        UserOutline,
        EditOutline,
        ProfileOutline,
        QuestionCircleOutline,
        LockOutline,
        CommentOutline,
        UnorderedListOutline,
        ArrowRightOutline,
        BellOutline,
        GithubOutline,
        WalletOutline
      ]
    );

    // load current user from localStorage if present
    const userJson = typeof window !== 'undefined' ? localStorage.getItem('currentUser') : null;
    if (userJson) {
      try {
        this.currentUser = JSON.parse(userJson);
      } catch (e) {
        console.error('Error parsing currentUser from localStorage', e);
      }
    }
  }

  profile = [
    {
      icon: 'edit',
      title: 'Edit Profile'
    },
    {
      icon: 'user',
      title: 'View Profile'
    },
    {
      icon: 'profile',
      title: 'Social Profile'
    },
    {
      icon: 'wallet',
      title: 'Billing'
    },
    {
      icon: 'logout',
      title: 'Logout'
    }
  ];

  setting = [
    {
      icon: 'question-circle',
      title: 'Support'
    },
    {
      icon: 'user',
      title: 'Account Settings'
    },
    {
      icon: 'lock',
      title: 'Privacy Center'
    },
    {
      icon: 'comment',
      title: 'Feedback'
    },
    {
      icon: 'unordered-list',
      title: 'History'
    }
  ];

  // helpers
  get displayName(): string {
    if (!this.currentUser) {
      return 'Admin';
    }
    const first = this.currentUser.firstName || this.currentUser.firstname || '';
    const last = this.currentUser.lastName || this.currentUser.lastname || '';
    const full = `${first} ${last}`.trim();
    return full || this.currentUser.email || 'Admin';
  }

  get roleLabel(): string {
    if (!this.currentUser || !this.currentUser.role) {
      return 'JWT User';
    }
    // map role to human label
    switch (this.currentUser.role) {
      case 'ADMIN':
        return 'Administrator';
      case 'CLIENT':
        return 'Client';
      case 'FREELANCER':
        return 'Freelancer';
      default:
        return this.currentUser.role;
    }
  }

  onProfileItemClick(item: { title: string }) {
    const title = item.title.toLowerCase();

    if (title.includes('logout')) {
      localStorage.removeItem('currentUser');
      this.router.navigate(['/login']);
      return;
    }

    // TODO: add real routes for profile/settings pages
    if (title.includes('edit profile') || title.includes('view profile') || title.includes('social profile')) {
      // example: navigate to a future profile route
      // this.router.navigate(['/profile']);
      console.log('Profile menu clicked:', item.title);
      return;
    }

    if (title.includes('billing')) {
      // this.router.navigate(['/billing']);
      console.log('Billing menu clicked');
      return;
    }
  }
}
