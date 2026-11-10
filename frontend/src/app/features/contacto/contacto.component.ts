import { CarruselPaginaComponent } from '../../shared/carrusel/carrusel.component';
import { Component, inject, signal } from '@angular/core';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { firstValueFrom } from 'rxjs';
import { ContactoService } from './contacto.service';
import { mensajeError } from '../../api';

@Component({
  selector: 'app-contacto',
  imports: [CarruselPaginaComponent],
  templateUrl: './contacto.component.html',
  styleUrl: './contacto.component.css'
})
export class ContactoComponent {
  private service = inject(ContactoService);
  private sanitizer = inject(DomSanitizer);

  sitio = signal<any | null>(null);
  error = signal('');
  cargando = signal(true);
  mapa: SafeResourceUrl | null = null;

  constructor() {
    void this.cargar();
  }

  async cargar() {
    this.cargando.set(true);
    this.error.set('');
    try {
      const s = await this.service.cargarSitio();
      this.sitio.set(s);
      this.mapa = this.mapaSeguro(s.configuracion?.mapa);
    } catch (e) {
      this.error.set('No pudimos cargar la página');
    } finally {
      this.cargando.set(false);
    }
  }

  contacto(valor: string) {
    return valor.includes('@') ? 'mailto:' + valor : 'tel:' + valor.replace(/[^+\d]/g, '');
  }

  private mapaSeguro(url: string) {
    try {
      const u = new URL(url);
      return u.protocol === 'https:' &&
        ['www.google.com', 'maps.google.com', 'www.google.com.pe'].includes(u.hostname) &&
        u.pathname.startsWith('/maps/embed')
        ? this.sanitizer.bypassSecurityTrustResourceUrl(url)
        : null;
    } catch {
      return null;
    }
  }
}