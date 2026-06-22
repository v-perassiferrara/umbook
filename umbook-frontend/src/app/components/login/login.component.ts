import { Component, OnInit } from '@angular/core';
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
export class LoginComponent implements OnInit {
  form: FormGroup;
  errorMsg = '';
  loading = false;
  isBlocked = false;

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

  ngOnInit(): void {
    this.checkBloqueo();
  }

  campo(name: string): AbstractControl {
    return this.form.get(name)!;
  }

  esInvalido(name: string): boolean {
    const c = this.campo(name);
    return c.invalid && c.touched;
  }

  checkBloqueo(): boolean {
    const blockedUntil = localStorage.getItem('umbook_blocked_until');
    if (blockedUntil) {
      const tiempoRestante = parseInt(blockedUntil, 10) - Date.now();
      if (tiempoRestante > 0) {
        const minutos = Math.ceil(tiempoRestante / 60000);
        this.errorMsg = `Tu acceso ha sido bloqueado temporalmente por demasiados intentos fallidos. Volvé a intentar en ${minutos} minuto(s).`;
        this.isBlocked = true;
        return true;
      } else {
        localStorage.removeItem('umbook_blocked_until');
        localStorage.removeItem('umbook_failed_attempts');
      }
    }
    this.isBlocked = false;
    return false;
  }

  onSubmit(): void {
    this.form.markAllAsTouched();
    if (this.form.invalid) return;

    if (this.checkBloqueo()) {
      return;
    }

    this.loading = true;
    this.errorMsg = '';

    this.userService.login(this.form.value).subscribe({
      next: (usuario) => {
        localStorage.removeItem('umbook_failed_attempts');
        localStorage.removeItem('umbook_blocked_until');
        localStorage.setItem('umbook_user', JSON.stringify(usuario));
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.loading = false;
        
        let intentos = parseInt(localStorage.getItem('umbook_failed_attempts') || '0', 10);
        intentos++;
        localStorage.setItem('umbook_failed_attempts', intentos.toString());

        if (intentos >= 10) {
          const blockDuration = 3600000; // 1 hora
          localStorage.setItem('umbook_blocked_until', (Date.now() + blockDuration).toString());
          this.checkBloqueo();
        } else {
          this.errorMsg = err.error || 'Error al iniciar sesión.';
        }
      }
    });
  }
}
