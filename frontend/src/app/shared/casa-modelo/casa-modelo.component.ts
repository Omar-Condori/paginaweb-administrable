import { Component, computed, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

/** Casa modelo: carrusel de fotos; la tarjeta de texto cambia junto con la foto. */
@Component({
  selector: 'app-casa-modelo',
  imports: [RouterLink],
  templateUrl: './casa-modelo.component.html',
  styleUrl: './casa-modelo.component.css'
})
export class CasaModeloComponent {
  items = input.required<any[]>();
  seccion = input<any>();
  indice = signal(0);

  /** Índice seguro aunque la lista cambie de tamaño. */
  actual = computed(() => {
    const n = this.items().length;
    return n ? this.indice() % n : 0;
  });

  /** Fotos antiguas guardaban la imagen de la tarjeta en imagenSecundaria. */
  foto(c: any): string { return c.imagenSecundaria || c.imagen; }

  mover(delta: number) {
    const n = this.items().length;
    if (n > 1) this.indice.set((this.actual() + delta + n) % n);
  }
}
