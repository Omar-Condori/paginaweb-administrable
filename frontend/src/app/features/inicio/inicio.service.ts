import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { Sitio, Tipo } from '../../modelos';

@Injectable({ providedIn: 'root' })
export class InicioService {
  private http = inject(HttpClient);

  async cargarSitio(): Promise<any> {
    return firstValueFrom(this.http.get('/api/publico/sitio'));
  }
}