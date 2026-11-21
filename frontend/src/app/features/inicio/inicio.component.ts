import { Component, inject, signal, OnDestroy, OnInit } from '@angular/core';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { firstValueFrom } from 'rxjs';
import { Sitio, Tipo } from '../../modelos';
import { InicioService } from './inicio.service';
import { mensajeError } from '../../api';
import { ProyectoCardComponent } from '../../shared/proyecto-card/proyecto-card.component';
import { CasaModeloComponent } from '../../shared/casa-modelo/casa-modelo.component';

@Component({
  selector: 'app-inicio',
  imports: [ProyectoCardComponent, CasaModeloComponent],
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

  /** Duración de cada diapositiva (ms). Debe coincidir con --hero-slide-dur del CSS. */
  readonly duracion = 6500;
  private temporizador?: ReturnType<typeof setTimeout>;

  /** (Re)inicia la cuenta regresiva de la diapositiva actual; la barra de progreso de las pestañas va sincronizada. */
  private programar() {
    clearTimeout(this.temporizador);
    if (!this.reproduciendo()) return;
    this.temporizador = setTimeout(() => {
      if (document.hidden) this.programar(); else this.mover(1);
    }, this.duracion);
  }

  alternarReproduccion() {
    this.reproduciendo.update(v => !v);
    this.programar();
  }

  irA(indice: number) {
    this.slide.set(indice);
    this.programar();
  }

  /**
   * Tamaño del título en vw. Se calcula para que la palabra más larga quepa SIN partirse
   * dentro del ancho disponible (~50vw) y para que títulos muy largos no crezcan de más.
   */
  escalaTitulo(titulo: string): number {
    const palabras = this.palabras(titulo);
    const larga = Math.max(4, ...palabras.map(p => p.length));
    const porPalabra = 50 / (larga * 0.7);                       // ancho medio de una mayúscula ≈ 0.7em
    const porLargo = titulo.length > 38 ? 3.6 : titulo.length > 24 ? 4.6 : 5.6;
    return Math.round(Math.min(5.6, porPalabra, porLargo) * 100) / 100;
  }

  /** Palabras del título para la animación de revelado con máscara. */
  palabras(titulo: string) {
    return (titulo || '').split(/\s+/).filter(Boolean);
  }

  async ngOnInit() {
    await this.cargar();
    this.programar();
  }

  ngOnDestroy() {
    clearTimeout(this.temporizador);
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
    this.programar();
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