import { Component, DestroyRef, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterOutlet, NavigationEnd } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { filter } from 'rxjs';
import { Sesion } from '../../../api';
import { secciones } from '../../../modelos';
import { TemaToggleComponent } from '../../../shared/components/tema-toggle/tema-toggle.component';

@Component({
  selector: 'app-admin-layout',
  imports: [RouterLink, RouterOutlet, TemaToggleComponent],
  templateUrl: './admin-layout.component.html',
  styleUrl: './admin-layout.component.css'
})
export class AdminLayoutComponent {
  private router = inject(Router);
  private destroyRef = inject(DestroyRef);
  sesion = inject(Sesion);
  seccionesInicio = secciones.filter(s => ['CARRUSEL', 'PROYECTO', 'CASA_MODELO', 'RECREACION', 'TESTIMONIO', 'VENTAS'].includes(s.tipo));
  menu = signal(false);
  vista = signal('panel');
  tipoActual = signal('');
  desplegados = signal<Set<string>>(new Set());

  constructor() {
    this.sincronizar(this.router.url);
    this.router.events.pipe(
      filter((e): e is NavigationEnd => e instanceof NavigationEnd),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe(e => {
      this.sincronizar(e.urlAfterRedirects);
      this.menu.set(false);
    });
  }

  private sincronizar(url: string) {
    const arbol = this.router.parseUrl(url);
    const segmentos = arbol.root.children['primary']?.segments || [];
    this.vista.set(segmentos[1]?.path || 'panel');
    this.tipoActual.set(arbol.queryParams['tipo'] || 'CARRUSEL');
    const grupo = this.grupoActual();
    this.desplegados.set(new Set(grupo ? [grupo] : []));
  }

  grupoActual() {
    if (this.vista() === 'configuracion') return 'CONTACTOS';
    if (this.vista() !== 'contenido') return '';
    const tipo = this.tipoActual();
    if (tipo === 'NOSOTROS' || tipo === 'CARRUSEL_NOSOTROS' || tipo === 'NOSOTROS_VALORES') return 'NOSOTROS';
    if (this.seccionesInicio.some(s => s.tipo === tipo)) return 'INICIO';
    if (tipo === 'CARRUSEL_CONTACTOS') return 'CONTACTOS';
    return '';
  }

  alternarGrupo(grupo: string) {
    this.desplegados.update(actuales =>
      new Set(actuales.has(grupo) ? [] : [grupo])
    );
  }

  seleccionado(tipo: string) {
    return this.vista() === 'contenido' && this.tipoActual() === tipo;
  }

  carruselDe(grupo: string) {
    return grupo === 'PROYECTO' ? 'CARRUSEL_PROYECTOS' : 'CARRUSEL_' + grupo;
  }

  tituloVista() {
    if (this.vista() === 'contenido') return secciones.find(s => s.tipo === this.tipoActual())?.nombre || 'Contenido';
    return ({ panel: 'Resumen', imagenes: 'Imágenes', configuracion: 'Datos generales', usuarios: 'Usuarios', cuenta: 'Mi cuenta' } as Record<string, string>)[this.vista()] || 'Contenido';
  }

  ir(v: string, t?: string) {
    this.router.navigate(['/admin', v], t ? { queryParams: { tipo: t } } : {});
    this.menu.set(false);
  }

  async salir() {
    await this.sesion.salir();
    this.router.navigateByUrl('/login');
  }
}
