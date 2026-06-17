import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, AbstractControl } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { UserService } from '../../services/user.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  form: FormGroup;
  errorMsg = '';
  loading = false;

  constructor(
    private fb: FormBuilder,
    private userService: UserService,
    private router: Router
  ) {
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      contrasena: ['', Validators.required]
    });
  }

  campo(name: string): AbstractControl {
    return this.form.get(name)!;
  }

  esInvalido(name: string): boolean {
    const c = this.campo(name);
    return c.invalid && c.touched;
  }

  onSubmit(): void {
    this.form.markAllAsTouched();
    if (this.form.invalid) return;

    this.loading = true;
    this.errorMsg = '';

    this.userService.login(this.form.value).subscribe({
      next: (usuario) => {
        localStorage.setItem('umbook_user', JSON.stringify(usuario));
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.errorMsg = err.error || 'Error al iniciar sesión.';
        this.loading = false;
      }
    });
  }
}
