import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { Sesion, mensajeError } from '../../api';
import { TemaToggleComponent } from '../../shared/components/tema-toggle/tema-toggle.component';

@Component({
  selector: 'app-login',
  imports: [FormsModule, RouterLink, TemaToggleComponent],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  sesion = inject(Sesion);
  router = inject(Router);

  email = '';
  clave = '';
  error = signal('');
  ocupado = signal(false);
  verClave = signal(false);

  constructor() {
    if (this.sesion.usuario()) void this.router.navigateByUrl('/admin');
  }

  toggleVerClave() {
    this.verClave.update(v => !v);
  }

  async entrar() {
    this.ocupado.set(true);
    this.error.set('');
    try {
      await this.sesion.login(this.email, this.clave);
      await Promise.resolve(); // microtask para propagar señal
      await this.router.navigateByUrl('/admin/panel');
    } catch (e) {
      this.error.set(mensajeError(e));
    } finally {
      this.ocupado.set(false);
    }
  }
}