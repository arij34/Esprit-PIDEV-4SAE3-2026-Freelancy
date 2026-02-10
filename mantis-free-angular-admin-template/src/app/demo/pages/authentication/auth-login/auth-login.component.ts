import { Component } from '@angular/core';
import { RouterModule, Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AuthService } from '../auth-register/auth.service';

@Component({
  selector: 'app-auth-login',
  standalone: true,
  imports: [RouterModule, ReactiveFormsModule, CommonModule],
  templateUrl: './auth-login.component.html',
  styleUrl: './auth-login.component.scss'
})
export class AuthLoginComponent {
  loginForm: FormGroup;
  roleForm: FormGroup;
  submitted = false;
  showRoleSelection = false;
  currentUser: any = null;

  constructor(
    private formBuilder: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.loginForm = this.formBuilder.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required]]
    });
    this.roleForm = this.formBuilder.group({
      role: ['', [Validators.required]]
    });
  }

  get f() { return this.loginForm.controls; }

  onSubmit() {
    this.submitted = true;

    if (this.loginForm.invalid) {
      return;
    }

    this.authService.login(this.loginForm.value).subscribe({
      next: (user) => {
        console.log('Login User:', user);
        this.currentUser = user;

        // Check if role is missing
        if (!user.role) {
          this.showRoleSelection = true;
          return;
        }

        this.handleRedirection(user);
      },
      error: (error) => {
        const errorMessage = error.error && error.error.error ? error.error.error : error.message;
        alert('Login failed: ' + errorMessage);
      }
    });
  }

  onRoleSubmit() {
    if (this.roleForm.invalid) {
      return;
    }

    const selectedRole = this.roleForm.value.role;
    this.authService.updateRole(this.currentUser.id, selectedRole).subscribe({
      next: (updatedUser) => {
        alert('Role updated successfully!');
        this.handleRedirection(updatedUser);
      },
      error: (error) => {
        alert('Failed to update role');
      }
    });
  }

  handleRedirection(user: any) {
    // Persist user data
    localStorage.setItem('currentUser', JSON.stringify(user));

    if (user.role === 'ADMIN') {
      this.router.navigate(['/dashboard/default']);
    } else if (user.role === 'CLIENT' || user.role === 'FREELANCER') {
      // Redirect to external Nova template
      window.location.href = '/assets/nova/index.html';
    } else {
      this.router.navigate(['/dashboard/default']);
    }
  }
}
