import { Routes } from '@angular/router';

export const adminRoutes: Routes = [
  {
    path: '',
    component: () => import('./shared/admin-layout.component').then(m => m.AdminLayoutComponent),
    children: [
      { path: '', redirectTo: 'panel', pathMatch: 'full' },
      { path: 'panel', loadComponent: () => import('./dashboard/dashboard.component').then(m => m.DashboardComponent) },
      { path: 'contenido', loadComponent: () => import('./contenidos/contenidos.component').then(m => m.ContenidosComponent) },
      { path: 'imagenes', loadComponent: () => import('./imagenes/imagenes.component').then(m => m.ImagenesComponent) },
      { path: 'configuracion', loadComponent: () => import('./configuracion/configuracion.component').then(m => m.ConfiguracionComponent) },
      { path: 'usuarios', loadComponent: () => import('./usuarios/usuarios.component').then(m => m.UsuariosComponent) },
      { path: 'cuenta', loadComponent: () => import('./cuenta/cuenta.component').then(m => m.CuentaComponent) },
    ]
  }
];