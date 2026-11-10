import { CarruselPaginaComponent } from '../../shared/carrusel/carrusel.component';
import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { ProyectosService } from './proyectos.service';
import { mensajeError } from '../../api';

@Component({
  selector: 'app-proyectos',
  imports: [RouterLink, CarruselPaginaComponent],
  templateUrl: './proyectos.component.html',
  styleUrl: './proyectos.component.css'
})
export class ProyectosComponent {
  private service = inject(ProyectosService);

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