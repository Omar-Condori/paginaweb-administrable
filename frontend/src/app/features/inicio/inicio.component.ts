import { Component, inject, signal, OnDestroy, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { firstValueFrom } from 'rxjs';
import { Sitio, Tipo } from '../../modelos';
import { InicioService } from './inicio.service';
import { mensajeError } from '../../api';

@Component({
  selector: 'app-inicio',
  imports: [RouterLink],
  templateUrl: './inicio.component.html',
  styleUrl: './inicio.component.css'
})
export class InicioComponent implements OnDestroy, OnInit {
  private service = inject(InicioService);
  private sanitizer = inject(DomSanitizer);

  sitio = signal<any | null>(null);
  error = signal('');
  cargando = signal(true);
  slide = signal(0);
  reproduciendo = signal(!matchMedia('(prefers-reduced-motion: reduce)').matches);
  mapa: SafeResourceUrl | null = null;

  private reloj = setInterval(() => {
    if (this.reproduciendo() && !document.hidden) this.mover(1);
  }, 6500);

  async ngOnInit() {
    await this.cargar();
  }

  ngOnDestroy() {
    clearInterval(this.reloj);
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

  items(tipo: string) {
    return this.sitio()?.contenidos?.filter((c: any) => c.tipo === tipo) || [];
  }

  seccion(tipo: string) {
    return this.sitio()?.secciones?.find((s: any) => s.codigo === tipo);
  }

  visible(tipo: string) {
    return !!this.seccion(tipo)?.activo && this.items(tipo).length > 0;
  }

  mover(delta: number) {
    const n = this.items('CARRUSEL').length;
    this.slide.set(n ? (this.slide() + delta + n) % n : 0);
  }

  estrellas(n: number) {
    return '★'.repeat(n) + '☆'.repeat(5 - n);
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