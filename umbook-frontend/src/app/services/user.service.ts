import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Usuario } from '../models/usuario.model';

@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly apiUrl = 'http://localhost:8080/api/usuarios';

  constructor(private http: HttpClient) {}

  registrar(datos: {
    nombre: string;
    apellido: string;
    email: string;
    nombreUsuario: string;
    contrasena: string;
    fechaNacimiento: string;
  }): Observable<Usuario> {
    return this.http.post<Usuario>(`${this.apiUrl}/registrar`, datos);
  }

  login(credenciales: { email: string; contrasena: string }): Observable<Usuario> {
    return this.http.post<Usuario>(`${this.apiUrl}/login`, credenciales);
  }

  buscar(termino: string): Observable<Usuario[]> {
    const params = new HttpParams().set('q', termino);
    return this.http.get<Usuario[]>(`${this.apiUrl}/buscar`, { params });
  }
}
