import { Injectable, signal } from '@angular/core';

export type Tema = 'dark' | 'light';

const CLAVE = 'prohabit-tema';

/** Tema Día / Noche global. Aplica data-theme en <html>; los estilos viven en theme.css. */
@Injectable({ providedIn: 'root' })
export class TemaService {
  /** Noche por defecto (diseño actual del sitio); se recuerda la elección del usuario. */
  tema = signal<Tema>(this.leer());

  constructor() {
    this.aplicar(this.tema());
  }

  alternar() {
    this.establecer(this.tema() === 'dark' ? 'light' : 'dark');
  }

  establecer(t: Tema) {
    this.tema.set(t);
    this.aplicar(t);
    try { localStorage.setItem(CLAVE, t); } catch { /* almacenamiento no disponible */ }
  }

  private leer(): Tema {
    try { return localStorage.getItem(CLAVE) === 'light' ? 'light' : 'dark'; } catch { return 'dark'; }
  }

  private aplicar(t: Tema) {
    document.documentElement.setAttribute('data-theme', t);
  }
}
