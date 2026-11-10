import { Component, inject, signal, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { Sesion, mensajeError } from '../../../api';

@Component({
  selector: 'app-usuarios',
  imports: [FormsModule],
  templateUrl: './usuarios.component.html',
  styleUrl: './usuarios.component.css'
})
export class UsuariosComponent implements OnInit {
  private http = inject(HttpClient);
  sesion = inject(Sesion);

  usuarios = signal<any[]>([]);
  cargando = signal(true);
  guardando = signal(false);
  error = signal('');
  aviso = signal('');

  usuarioEdicion: any = null;

  async ngOnInit() {
    await this.cargarUsuarios();
  }

  async cargarUsuarios() {
    try {
      this.usuarios.set(await firstValueFrom(this.http.get<any[]>('/api/admin/usuarios')));
    } catch (e) {
      this.error.set(mensajeError(e));
    } finally {
      this.cargando.set(false);
    }
  }

  editarUsuario(u?: any) {
    this.usuarioEdicion = u
      ? { ...u, clave: '' }
      : { nombre: '', email: '', rol: 'OPERADOR', activo: true, version: 0, clave: '' };
  }

  async guardarUsuario() {
    const u = this.usuarioEdicion;
    if (!u) return;
    try {
      this.guardando.set(true);
      await firstValueFrom(
        u.id
          ? this.http.put(`/api/admin/usuarios/${u.id}`, u)
          : this.http.post('/api/admin/usuarios', u)
      );
      this.usuarioEdicion = null;
      await this.cargarUsuarios();
      this.aviso.set('Usuario guardado. Sus sesiones anteriores se han cerrado.');
    } catch (e) {
      this.error.set(mensajeError(e));
    } finally {
      this.guardando.set(false);
    }
  }

  async borrarUsuario(u: any) {
    if (!confirm(`¿Eliminar a ${u.nombre}?`)) return;
    try {
      await firstValueFrom(this.http.delete(`/api/admin/usuarios/${u.id}?version=${u.version}`));
      await this.cargarUsuarios();
      this.aviso.set('Usuario eliminado.');
    } catch (e) {
      this.error.set(mensajeError(e));
    }
  }
}