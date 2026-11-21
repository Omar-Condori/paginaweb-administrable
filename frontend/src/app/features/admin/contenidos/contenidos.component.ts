import { Component, inject, signal, OnInit, OnDestroy, ViewChild, ElementRef } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { firstValueFrom, Subscription } from 'rxjs';
import { Sesion, mensajeError } from '../../../api';
import { secciones, contenidoNuevo, estadosProyecto, Tipo } from '../../../modelos';

@Component({
  selector: 'app-contenidos',
  imports: [FormsModule],
  templateUrl: './contenidos.component.html',
  styleUrl: './contenidos.component.css'
})
export class ContenidosComponent implements OnInit, OnDestroy {
  private http = inject(HttpClient);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  sesion = inject(Sesion);

  secciones = secciones;
  estadosProyecto = estadosProyecto;
  readonly maxFotosExtra = 11;
  sitio = signal<any | null>(null);
  archivos = signal<any[]>([]);
  tipo = signal<Tipo>('CARRUSEL');
  cargando = signal(true);
  guardando = signal(false);
  subiendo = signal(false);
  error = signal('');
  aviso = signal('');

  @ViewChild('visor') visor!: ElementRef<HTMLDialogElement>;
  imagenAmpliada = signal<{ url: string; titulo: string } | null>(null);
  imagenesFallidas = signal<Set<string>>(new Set());

  marcarImagenFallida(url: string) {
    this.imagenesFallidas.update(actuales => new Set([...actuales, url]));
  }

  ampliarImagen(c: any) {
    this.imagenAmpliada.set({ url: c.imagen, titulo: c.titulo });
    this.visor.nativeElement.showModal();
  }

  cerrarImagen() {
    this.visor.nativeElement.close();
    this.imagenAmpliada.set(null);
  }

  edicion: any = null;
  seccionEdicion: any = null;
  private sub?: Subscription;

  async ngOnInit() {
    await this.cargar();
    this.sub = this.route.queryParams.subscribe(params => {
      const tipoParam = params['tipo'];
      if (tipoParam && secciones.find(s => s.tipo === tipoParam)) {
        this.tipo.set(tipoParam as Tipo);
        this.prepararSeccion();
        this.edicion = null;
      }
    });
  }

  ngOnDestroy() {
    this.sub?.unsubscribe();
  }

  async cargar() {
    this.cargando.set(true);
    this.error.set('');
    try {
      const [s, a] = await Promise.all([
        firstValueFrom(this.http.get<any>('/api/admin/sitio')),
        firstValueFrom(this.http.get<any[]>('/api/admin/archivos'))
      ]);
      this.sitio.set(s);
      this.archivos.set(a);
      this.prepararSeccion();
    } catch (e) {
      this.error.set(mensajeError(e));
    } finally {
      this.cargando.set(false);
    }
  }

  prepararSeccion() {
    const s = this.sitio()?.secciones.find((sec: any) => sec.codigo === this.tipo());
    this.seccionEdicion = s ? { ...s } : null;
  }

  ir(v: string, t?: string) {
    if (t) {
      this.router.navigate(['/admin', v], { queryParams: { tipo: t } });
    } else {
      this.router.navigate(['/admin', v]);
    }
  }

  esMarca() { return this.tipo() === 'MARCAS'; }

  esTarjeta() { return this.tipo() === 'NOSOTROS_VALORES'; }

  esCarrusel() {
    return this.tipo() === 'CARRUSEL' || this.tipo().startsWith('CARRUSEL_');
  }

  paginaActual() {
    if (this.tipo() === 'NOSOTROS' || this.tipo() === 'CARRUSEL_NOSOTROS' || this.esTarjeta()) return 'NOSOTROS';
    if (this.esMarca()) return 'TODAS LAS PÁGINAS';
    if (this.tipo() === 'CARRUSEL_PROYECTOS') return 'PROYECTOS';
    if (this.tipo() === 'CARRUSEL_CONTACTOS') return 'CONTACTOS';
    return 'INICIO';
  }

  nombreTipo() {
    return secciones.find(s => s.tipo === this.tipo())?.nombre || '';
  }

  lista() {
    return this.sitio()?.contenidos.filter((c: any) => c.tipo === this.tipo()) || [];
  }

  cantidad(t: Tipo) {
    return this.sitio()?.contenidos.filter((c: any) => c.tipo === t).length || 0;
  }

  publicados() {
    return this.sitio()?.contenidos.filter((c: any) => c.activo).length || 0;
  }

  puedeCrear() {
    return this.tipo() !== 'NOSOTROS' || this.lista().length === 0;
  }

  async operacion(fn: () => Promise<unknown>, texto: string) {
    this.guardando.set(true);
    this.error.set('');
    this.aviso.set('');
    try {
      await fn();
      this.aviso.set(texto);
    } catch (e) {
      this.error.set(mensajeError(e));
    } finally {
      this.guardando.set(false);
    }
  }

  editar(c?: any) {
    this.error.set('');
    this.edicion = c
      ? { ...c, estado: c.estado || '', fecha: c.fecha || '', galeria: [...(c.galeria || [])] }
      : { ...contenidoNuevo(this.tipo()), orden: Math.max(-1, ...this.lista().map((item: any) => item.orden)) + 1 };
  }

  async guardarContenido() {
    const c = this.edicion;
    if (!c) return;
    if (this.tipo() === 'CASA_MODELO') c.imagenSecundaria = '';
    if (this.esMarca()) {
      c.subtitulo = '';
      c.descripcion = '';
      c.imagenSecundaria = '';
      c.contacto = '';
      c.cargo = '';
      c.calificacion = 5;
    }
    if (this.esCarrusel()) {
      c.descripcion = '';
      c.imagenSecundaria = '';
      c.contacto = '';
      c.cargo = '';
      c.calificacion = 5;
      c.orden = c.orden || 0;
      // Conserva el estado de publicación al editar una diapositiva.
    }
    await this.operacion(async () => {
      await firstValueFrom(
        c.id
          ? this.http.put(`/api/admin/contenidos/${c.id}`, c)
          : this.http.post('/api/admin/contenidos', c)
      );
      this.edicion = null;
      await this.cargar();
    }, 'Contenido guardado. Los cambios ya están disponibles en el sitio.');
  }

  async borrar(c: any) {
    if (!confirm(`¿Eliminar “${c.titulo}”? Esta acción no se puede deshacer.`)) return;
    await this.operacion(async () => {
      await firstValueFrom(this.http.delete(`/api/admin/contenidos/${c.id}?version=${c.version}`));
      await this.cargar();
    }, 'Contenido eliminado.');
  }

  async estado(c: any) {
    await this.operacion(async () => {
      await firstValueFrom(this.http.put(`/api/admin/contenidos/${c.id}`, { ...c, activo: !c.activo }));
      await this.cargar();
    }, c.activo ? 'Contenido oculto.' : 'Contenido publicado.');
  }

  async reordenar(c: any, dir: 'up' | 'down') {
    const items = this.lista();
    const idx = items.findIndex((i: any) => i.id === c.id);
    if (idx === -1) return;
    const targetIdx = dir === 'up' ? idx - 1 : idx + 1;
    if (targetIdx < 0 || targetIdx >= items.length) return;
    const otro = items[targetIdx];
    const nuevoOrdenC = otro.orden;
    const nuevoOrdenOtro = c.orden;
    await this.operacion(async () => {
      await Promise.all([
        firstValueFrom(this.http.put(`/api/admin/contenidos/${c.id}`, { ...c, orden: nuevoOrdenC })),
        firstValueFrom(this.http.put(`/api/admin/contenidos/${otro.id}`, { ...otro, orden: nuevoOrdenOtro }))
      ]);
      await this.cargar();
    }, 'Orden actualizado.');
  }

  async guardarSeccion() {
    const s = this.seccionEdicion;
    if (!s) return;
    await this.operacion(async () => {
      await firstValueFrom(this.http.put(`/api/admin/secciones/${s.codigo}`, s));
      await this.cargar();
    }, 'Encabezado y visibilidad actualizados.');
  }

  async subir(event: Event, campo?: 'imagen' | 'imagenSecundaria') {
    const input = event.target as HTMLInputElement,
      file = input.files?.[0];
    if (!file) return;
    if (file.size > 8 * 1024 * 1024) {
      this.error.set('La imagen supera 8 MB.');
      input.value = '';
      return;
    }
    this.subiendo.set(true);
    this.error.set('');
    try {
      const form = new FormData();
      form.append('archivo', file);
      const a = await firstValueFrom(this.http.post<any>('/api/admin/archivos', form));
      this.archivos.update(v => [a, ...v]);
      if (campo && this.edicion) this.edicion[campo] = a.url;
      this.aviso.set('Imagen subida correctamente.');
    } catch (e) {
      this.error.set(mensajeError(e));
    } finally {
      this.subiendo.set(false);
      input.value = '';
    }
  }

  /** Sube varias fotografías a la vez y las agrega a la galería del proyecto. */
  async subirGaleria(event: Event) {
    const input = event.target as HTMLInputElement;
    const files = Array.from(input.files || []);
    if (!files.length || !this.edicion) return;
    const cupo = this.maxFotosExtra - this.edicion.galeria.length;
    if (files.length > cupo) {
      this.error.set(cupo > 0 ? `Solo puedes agregar ${cupo} foto(s) más a este proyecto.` : 'Ya alcanzaste el máximo de fotografías del proyecto.');
      input.value = '';
      return;
    }
    if (files.some(f => f.size > 8 * 1024 * 1024)) {
      this.error.set('Alguna imagen supera 8 MB.');
      input.value = '';
      return;
    }
    this.subiendo.set(true);
    this.error.set('');
    try {
      for (const file of files) {
        const form = new FormData();
        form.append('archivo', file);
        const a = await firstValueFrom(this.http.post<any>('/api/admin/archivos', form));
        this.archivos.update(v => [a, ...v]);
        this.edicion.galeria.push(a.url);
      }
      this.aviso.set(files.length === 1 ? 'Foto agregada al proyecto.' : `${files.length} fotos agregadas al proyecto.`);
    } catch (e) {
      this.error.set(mensajeError(e));
    } finally {
      this.subiendo.set(false);
      input.value = '';
    }
  }

  agregarDeBiblioteca(url: string, select: HTMLSelectElement) {
    if (url && this.edicion && this.edicion.galeria.length < this.maxFotosExtra && !this.edicion.galeria.includes(url)) {
      this.edicion.galeria.push(url);
    }
    select.value = '';
  }

  quitarFoto(i: number) {
    this.edicion?.galeria.splice(i, 1);
  }

  moverFoto(i: number, delta: number) {
    const g: string[] | undefined = this.edicion?.galeria;
    const j = i + delta;
    if (!g || j < 0 || j >= g.length) return;
    [g[i], g[j]] = [g[j], g[i]];
  }

  async borrarImagen(a: any) {
    if (!confirm('¿Eliminar esta imagen de la biblioteca?')) return;
    await this.operacion(async () => {
      await firstValueFrom(this.http.delete(`/api/admin/archivos/${a.nombre}`));
      this.archivos.update(v => v.filter((f: any) => f.nombre !== a.nombre));
    }, 'Imagen eliminada.');
  }
}