import { Component, computed, input, signal, OnDestroy } from '@angular/core';
import { Tipo, Sitio } from '../../modelos';

@Component({
  selector: 'app-carrusel-pagina',
  templateUrl: './carrusel.component.html',
  styleUrl: './carrusel.component.css'
})
export class CarruselPaginaComponent implements OnDestroy {
  sitio = input.required<Sitio>();
  tipo = input.required<Tipo>();
  pagina = input.required<string>();
  indice = signal(0);
  reproduciendo = signal(!matchMedia('(prefers-reduced-motion: reduce)').matches);
  fotos = computed(() => {
    const datos = this.sitio();
    if (!datos.secciones.some(s => s.codigo === this.tipo() && s.activo)) return [];
    return datos.contenidos.filter(c => c.tipo === this.tipo() && c.activo)
      .sort((a, b) => a.orden - b.orden || (a.id || 0) - (b.id || 0));
  });
  actual = computed(() => this.fotos()[this.indice() % (this.fotos().length || 1)]);
  private reloj = setInterval(() => {
    if (this.reproduciendo() && !document.hidden && this.fotos().length > 1) this.mover(1);
  }, 6500);

  mover(delta: number) {
    const total = this.fotos().length;
    this.indice.set(total ? (this.indice() + delta + total) % total : 0);
  }

  ngOnDestroy() { clearInterval(this.reloj); }
}
