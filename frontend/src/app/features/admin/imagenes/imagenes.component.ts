import { Component, inject, signal, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { Sesion, mensajeError } from '../../../api';

@Component({
  selector: 'app-imagenes',
  imports: [FormsModule],
  templateUrl: './imagenes.component.html',
  styleUrl: './imagenes.component.css'
})
export class ImagenesComponent implements OnInit {
  private http = inject(HttpClient);
  sesion = inject(Sesion);

  archivos = signal<any[]>([]);
  subiendo = signal(false);
  error = signal('');
  aviso = signal('');

  async ngOnInit() {
    await this.cargar();
  }

  async cargar() {
    try {
      this.archivos.set(await firstValueFrom(this.http.get<any[]>('/api/admin/archivos')));
    } catch (e) {
      this.error.set(mensajeError(e));
    }
  }

  async subir(event: Event) {
    const input = event.target as HTMLInputElement,
      file = input.files?.[0];
    if (!file) return;
    if (file.size > 8 * 1024 * 1024) {
      this.error.set('La imagen supera 8 MB.');
      input.value = '';
      return;
    }
    this.subiendo.set(true);
    this.error.set('');
    try {
      const form = new FormData();
      form.append('archivo', file);
      const a = await firstValueFrom(this.http.post<any>('/api/admin/archivos', form));
      this.archivos.update(v => [a, ...v]);
      this.aviso.set('Imagen subida correctamente.');
    } catch (e) {
      this.error.set(mensajeError(e));
    } finally {
      this.subiendo.set(false);
      input.value = '';
    }
  }

  async borrarImagen(a: any) {
    if (!confirm('¿Eliminar esta imagen de la biblioteca?')) return;
    try {
      await firstValueFrom(this.http.delete(`/api/admin/archivos/${a.nombre}`));
      this.archivos.update(v => v.filter((f: any) => f.nombre !== a.nombre));
      this.aviso.set('Imagen eliminada.');
    } catch (e) {
      this.error.set(mensajeError(e));
    }
  }
}