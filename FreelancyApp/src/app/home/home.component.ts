import { Component, OnInit, AfterViewInit } from '@angular/core';

declare var WOW: any;

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css'],
  standalone: false
})
export class HomeComponent implements OnInit, AfterViewInit {

  constructor() { }

  ngOnInit(): void {
    // Hide preloader after a delay
    if (typeof window !== 'undefined') {
      setTimeout(() => {
        const preloader = document.querySelector('.preloader');
        if (preloader) {
          (preloader as HTMLElement).style.display = 'none';
        }
      }, 1000);
    }
  }

  ngAfterViewInit(): void {
    // Initialize WOW.js animations after view is initialized
    if (typeof window !== 'undefined' && typeof WOW !== 'undefined') {
      new WOW().init();
    }
  }
}

