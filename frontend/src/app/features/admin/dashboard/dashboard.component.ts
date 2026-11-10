import { Component, inject, signal, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { Sesion } from '../../../api';
import { secciones } from '../../../modelos';

@Component({
  selector: 'app-dashboard',
  imports: [RouterLink],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
  private http = inject(HttpClient);
  private router = inject(Router);
  sesion = inject(Sesion);

  secciones = secciones;
  sitio = signal<any | null>(null);
  archivos = signal<any[]>([]);
  cargando = signal(true);

  async ngOnInit() {
    await this.cargar();
  }

  async cargar() {
    this.cargando.set(true);
    try {
      const [s, a] = await Promise.all([
        firstValueFrom(this.http.get<any>('/api/admin/sitio')),
        firstValueFrom(this.http.get<any[]>('/api/admin/archivos'))
      ]);
      this.sitio.set(s);
      this.archivos.set(a);
    } catch (e) {
      console.error('Error cargando dashboard:', e);
    } finally {
      this.cargando.set(false);
    }
  }

  ir(v: string, t?: string) {
    this.router.navigate(['/admin', v], t ? { queryParams: { tipo: t } } : {});
  }

  publicados() {
    return this.sitio()?.contenidos?.filter((c: any) => c.activo).length || 0;
  }

  cantidad(tipo: string) {
    return this.sitio()?.contenidos?.filter((c: any) => c.tipo === tipo).length || 0;
  }
}