import { bootstrapApplication } from '@angular/platform-browser';
import { inject, provideAppInitializer, provideZonelessChangeDetection } from '@angular/core';
import { provideRouter, withInMemoryScrolling } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { App, rutas } from './app/app';
import { Sesion, seguridadInterceptor } from './app/api';

bootstrapApplication(App, {
  providers: [
    provideZonelessChangeDetection(),
    provideRouter(
      rutas,
      withInMemoryScrolling({
        scrollPositionRestoration: 'enabled',
        anchorScrolling: 'enabled'
      })
    ),
    provideHttpClient(withInterceptors([seguridadInterceptor])),
    provideAppInitializer(() => inject(Sesion).iniciar())
  ]
}).catch(console.error);