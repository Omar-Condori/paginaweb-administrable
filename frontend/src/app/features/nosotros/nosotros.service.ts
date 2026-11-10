import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { Sitio } from '../../modelos';

@Injectable({ providedIn: 'root' })
export class NosotrosService {
  private http = inject(HttpClient);

  async cargarSitio(): Promise<any> {
    return firstValueFrom(this.http.get('/api/publico/sitio'));
  }
}