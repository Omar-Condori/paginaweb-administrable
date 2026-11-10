import { inject, Injectable, signal } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpInterceptorFn, HttpParams } from '@angular/common/http';
import { Router } from '@angular/router';
import { firstValueFrom, catchError, throwError } from 'rxjs';
import { Usuario } from './modelos';

// ============ MENSAJES DE ERROR ============

export function mensajeError(e: unknown): string {
  if (e instanceof HttpErrorResponse) {
    return e.status === 0
      ? 'No se pudo conectar con el servidor. Comprueba que el backend esté iniciado.'
      : e.error?.mensaje || (
          e.status === 413
            ? 'La imagen supera el tamaño permitido.'
            : 'No se pudo completar la operación. Revisa los datos e inténtalo de nuevo.'
        );
  }
  return e instanceof Error ? e.message : 'Ocurrió un error inesperado.';
}

// ============ SERVICIO DE SESIÓN ============

@Injectable({ providedIn: 'root' })
export class Sesion {
  private http = inject(HttpClient);

  usuario = signal<Usuario | null>(null);

  async iniciar() {
    try {
      this.usuario.set(await firstValueFrom(this.http.get<Usuario>('/api/sesion')));
    } catch {
      this.usuario.set(null);
    }
  }

  async login(email: string, clave: string) {
    const params = new HttpParams().set('email', email).set('clave', clave);
    await firstValueFrom(
      this.http.post('/api/auth/login', params, {
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
      })
    );
    this.usuario.set(await firstValueFrom(this.http.get<Usuario>('/api/sesion')));
  }

  async salir() {
    await firstValueFrom(this.http.post('/api/auth/logout', null));
    this.usuario.set(null);
  }

  esAdmin() {
    return this.usuario()?.rol === 'ADMINISTRADOR';
  }
}

// ============ INTERCEPTOR DE SEGURIDAD ============

export const seguridadInterceptor: HttpInterceptorFn = (req, next) => {
  return next(req);
};