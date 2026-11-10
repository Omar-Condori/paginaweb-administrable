import { CarruselPaginaComponent } from '../../shared/carrusel/carrusel.component';
import { Component, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { Sitio, Tipo } from '../../modelos';
import { NosotrosService } from './nosotros.service';
import { mensajeError } from '../../api';

@Component({
  selector: 'app-nosotros',
  imports: [RouterLink, CarruselPaginaComponent],
  templateUrl: './nosotros.component.html',
  styleUrl: './nosotros.component.css'
})
export class NosotrosComponent {
  private http = inject(HttpClient);
  private service = inject(NosotrosService);

  sitio = signal<any | null>(null);
  error = signal('');
  cargando = signal(true);

  constructor() {
    void this.cargar();
  }

  async cargar() {
    this.cargando.set(true);
    this.error.set('');
    try {
      const s = await this.service.cargarSitio();
      this.sitio.set(s);
    } catch (e) {
      this.error.set('No pudimos cargar la página');
    } finally {
      this.cargando.set(false);
    }
  }

  items(tipo: string) {
    return this.sitio()?.contenidos?.filter((c: any) => c.tipo === tipo) || [];
  }

  seccion(tipo: string) {
    return this.sitio()?.secciones?.find((s: any) => s.codigo === tipo);
  }
}