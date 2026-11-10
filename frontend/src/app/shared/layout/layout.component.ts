import { Component, inject, signal, HostListener, AfterViewInit, OnDestroy } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { firstValueFrom } from 'rxjs';
import { Sitio } from '../../modelos';
import { Sesion } from '../../api';
import { TemaToggleComponent } from '../components/tema-toggle/tema-toggle.component';

@Component({
  selector: 'app-layout',
  imports: [RouterLink, RouterLinkActive, RouterOutlet, TemaToggleComponent],
  templateUrl: './layout.component.html',
  styleUrl: './layout.component.css'
})
export class LayoutComponent implements AfterViewInit, OnDestroy {
  private http = inject(HttpClient);
  private sanitizer = inject(DomSanitizer);
  sesion = inject(Sesion);

  sitio = signal<any | null>(null);
  menu = signal(false);
  scrolled = signal(false);
  mapa: SafeResourceUrl | null = null;

  ngAfterViewInit() {
    this.checkScroll();
    this.cargarSitio();
  }

  ngOnDestroy() {}

  async cargarSitio() {
    try {
      const s = await firstValueFrom(this.http.get<Sitio>('/api/publico/sitio'));
      this.sitio.set(s);
      this.mapa = this.mapaSeguro(s.configuracion?.mapa);
    } catch (e) {
      console.error('Error cargando sitio en layout:', e);
    }
  }

  @HostListener('window:scroll')
  checkScroll() {
    const hero = document.querySelector('.hero') as HTMLElement;
    if (hero) {
      const rect = hero.getBoundingClientRect();
      // Activa el fondo cuando se desplaza más de la mitad del hero
      this.scrolled.set(rect.bottom < window.innerHeight / 2);
    } else {
      // En páginas sin hero (carrusel), el fondo azul es fijo si hay algo de scroll
      this.scrolled.set(window.scrollY > 20);
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