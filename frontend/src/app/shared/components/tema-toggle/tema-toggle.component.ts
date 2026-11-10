import { Component, inject, input } from '@angular/core';
import { TemaService } from '../../services/tema.service';

@Component({
  selector: 'app-tema-toggle',
  templateUrl: './tema-toggle.component.html',
  styleUrl: './tema-toggle.component.css',
  host: { '[class.flotante]': 'flotante()' }
})
export class TemaToggleComponent {
  tema = inject(TemaService);
  /** Fijo en la esquina (solo para pantallas sin encabezado, como el login). */
  flotante = input(false);
}
