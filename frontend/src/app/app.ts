import { Component, inject, OnInit } from '@angular/core';
import { CanActivateFn, Router, RouterOutlet, Routes } from '@angular/router';
import { Sesion } from './api';
import { LayoutComponent } from './shared/layout/layout.component';

// ============ GUARD ============

// Solo permite entrar si hay un usuario con sesión; si no, redirige al login
const privado: CanActivateFn = () => {
  const sesion = inject(Sesion);
  const router = inject(Router);

  // Si ya tenemos la sesión en señal, permitir
  if (sesion.usuario()) return true;

  // Si no, redirigir al login
  return router.createUrlTree(['/login']);
};

// ============ RUTAS ADMIN ============

const adminRutas: Routes = [
  {
    path: 'admin',
    loadComponent: () => import('./features/admin/shared/admin-layout.component').then(m => m.AdminLayoutComponent),
    canActivate: [privado],
    children: [
      { path: '', redirectTo: 'panel', pathMatch: 'full' },
      { path: 'panel', loadComponent: () => import('./features/admin/dashboard/dashboard.component').then(m => m.DashboardComponent) },
      { path: 'contenido', loadComponent: () => import('./features/admin/contenidos/contenidos.component').then(m => m.ContenidosComponent) },
      { path: 'imagenes', loadComponent: () => import('./features/admin/imagenes/imagenes.component').then(m => m.ImagenesComponent) },
      { path: 'configuracion', loadComponent: () => import('./features/admin/configuracion/configuracion.component').then(m => m.ConfiguracionComponent) },
      { path: 'usuarios', loadComponent: () => import('./features/admin/usuarios/usuarios.component').then(m => m.UsuariosComponent) },
      { path: 'cuenta', loadComponent: () => import('./features/admin/cuenta/cuenta.component').then(m => m.CuentaComponent) },
    ]
  }
];

// ============ RUTAS ============

export const rutas: Routes = [
  {
    path: '',
    component: LayoutComponent,
    children: [
      { path: '',          loadComponent: () => import('./features/inicio/inicio.component').then(m => m.InicioComponent) },
      { path: 'nosotros',  loadComponent: () => import('./features/nosotros/nosotros.component').then(m => m.NosotrosComponent) },
      { path: 'proyectos', loadComponent: () => import('./features/proyectos/proyectos.component').then(m => m.ProyectosComponent) },
      { path: 'contactos', loadComponent: () => import('./features/contacto/contacto.component').then(m => m.ContactoComponent) },
    ]
  },
  { path: 'login', loadComponent: () => import('./features/login/login.component').then(m => m.LoginComponent) },
  ...adminRutas,
  { path: '**', redirectTo: '' }
];

// ============ COMPONENTE RAÍZ ============

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  template: '<router-outlet />'
})
export class App implements OnInit {
  private sesion = inject(Sesion);

  ngOnInit() {
    this.sesion.iniciar();
  }
}