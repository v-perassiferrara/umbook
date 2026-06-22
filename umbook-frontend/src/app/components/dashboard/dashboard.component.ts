import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { UserService } from '../../services/user.service';
import { Usuario } from '../../models/usuario.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
  form: FormGroup;
  resultados: Usuario[] = [];
  errorMsg = '';
  sinResultados = false;
  buscado = false;
  loading = false;
  usuarioActual: Usuario | null = null;
  solicitudesEnviadas: { [id: number]: boolean } = {};

  constructor(
    private fb: FormBuilder,
    private userService: UserService,
    private router: Router
  ) {
    this.form = this.fb.group({
      busqueda: ['']
    });
  }

  ngOnInit(): void {
    const data = localStorage.getItem('umbook_user');
    if (data) {
      this.usuarioActual = JSON.parse(data);
    }
  }

  inicialAvatar(): string {
    if (!this.usuarioActual) return '?';
    return (this.usuarioActual.nombre[0] + this.usuarioActual.apellido[0]).toUpperCase();
  }

  inicialUsuario(u: Usuario): string {
    return (u.nombre[0] + u.apellido[0]).toUpperCase();
  }

  buscar(): void {
    const termino = (this.form.value.busqueda ?? '').trim();

    if (!termino) {
      this.errorMsg = 'Debe ingresar un nombre o apellido para buscar.';
      return;
    }

    this.loading = true;
    this.errorMsg = '';
    this.sinResultados = false;
    this.buscado = false;

    this.userService.buscar(termino).subscribe({
      next: (usuarios) => {
        this.resultados = usuarios ?? [];
        this.sinResultados = this.resultados.length === 0;
        this.buscado = true;
        this.loading = false;
      },
      error: (err) => {
        this.errorMsg = err.error || 'Error al realizar la búsqueda.';
        this.loading = false;
      }
    });
  }

  limpiar(): void {
    this.form.reset({ busqueda: '' });
    this.resultados = [];
    this.sinResultados = false;
    this.buscado = false;
    this.errorMsg = '';
  }

  logout(): void {
    localStorage.removeItem('umbook_user');
    this.router.navigate(['/login']);
  }

  solicitarAmistad(u: Usuario): void {
    this.solicitudesEnviadas[u.id] = true;
  }
}
