import { Component, inject, signal, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { Sesion, mensajeError } from '../../../api';

@Component({
  selector: 'app-configuracion',
  imports: [FormsModule],
  templateUrl: './configuracion.component.html',
  styleUrl: './configuracion.component.css'
})
export class ConfiguracionComponent implements OnInit {
  private http = inject(HttpClient);
  sesion = inject(Sesion);

  config = signal<any | null>(null);
  cargando = signal(true);
  guardando = signal(false);
  error = signal('');
  aviso = signal('');

  async ngOnInit() {
    await this.cargar();
  }

  async cargar() {
    this.cargando.set(true);
    try {
      const s = await firstValueFrom(this.http.get<any>('/api/admin/sitio'));
      this.config.set({ ...s.configuracion });
    } catch (e) {
      this.error.set(mensajeError(e));
    } finally {
      this.cargando.set(false);
    }
  }

  async guardarConfig() {
    await this.operacion(async () => {
      const c = this.config();
      if (!c) return;
      this.config.set(await firstValueFrom(this.http.put<any>('/api/admin/configuracion', c)));
    }, 'Datos generales actualizados.');
  }

  async operacion(fn: () => Promise<unknown>, texto: string) {
    this.guardando.set(true);
    this.error.set('');
    this.aviso.set('');
    try {
      await fn();
      this.aviso.set(texto);
    } catch (e) {
      this.error.set(mensajeError(e));
    } finally {
      this.guardando.set(false);
    }
  }
}