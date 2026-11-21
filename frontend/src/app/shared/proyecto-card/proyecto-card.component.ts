import { Component, computed, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

const MESES = ['Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio', 'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'];
const ESTADOS: Record<string, string> = { EN_EJECUCION: 'En ejecución', TERMINADO: 'Terminado', PROXIMAMENTE: 'Próximamente' };

/** Tarjeta de proyecto: fotos deslizables, estado, ubicación, descripción y fecha. */
@Component({
  selector: 'app-proyecto-card',
  imports: [RouterLink],
  templateUrl: './proyecto-card.component.html',
  styleUrl: './proyecto-card.component.css'
})
export class ProyectoCardComponent {
  proyecto = input.required<any>();
  indice = signal(0);

  /** Fotografía principal + adicionales, sin repetir. */
  fotos = computed<string[]>(() => {
    const p = this.proyecto();
    return [...new Set<string>([p.imagen, ...(p.galeria || [])].filter(Boolean))];
  });

  estadoTexto = computed(() => ESTADOS[this.proyecto().estado] || '');

  /** "2025-10-01" → "Octubre 1, 2025" (sin pasar por Date para evitar desfases de zona horaria). */
  fechaTexto = computed(() => {
    const m = /^(\d{4})-(\d{2})-(\d{2})$/.exec(this.proyecto().fecha || '');
    if (!m) return '';
    const mes = MESES[Number(m[2]) - 1];
    return mes ? `${mes} ${Number(m[3])}, ${m[1]}` : '';
  });

  ir(i: number) { this.indice.set(i); }

  mover(delta: number) {
    const n = this.fotos().length;
    if (n > 1) this.indice.set((this.indice() + delta + n) % n);
  }
}
