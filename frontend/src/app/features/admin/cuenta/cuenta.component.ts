import { Component, inject, signal, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { Sesion, mensajeError } from '../../../api';

@Component({
  selector: 'app-cuenta',
  imports: [FormsModule],
  templateUrl: './cuenta.component.html',
  styleUrl: './cuenta.component.css'
})
export class CuentaComponent implements OnInit {
  private http = inject(HttpClient);
  sesion = inject(Sesion);

  claveActual = '';
  claveNueva = '';
  error = signal('');
  aviso = signal('');

  async ngOnInit() {}

  async cambiarClave() {
    if (!this.claveActual || !this.claveNueva) return;
    try {
      await firstValueFrom(
        this.http.post('/api/sesion/clave', { actual: this.claveActual, nueva: this.claveNueva })
      );
      this.sesion.usuario.set(null);
      window.location.href = '/login';
    } catch (e) {
      this.error.set(mensajeError(e));
    }
  }
}