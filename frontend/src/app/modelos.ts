// ============ TIPOS BASE ============

export type Tipo =
  | 'CARRUSEL'
  | 'PROYECTO'
  | 'CASA_MODELO'
  | 'RECREACION'
  | 'TESTIMONIO'
  | 'VENTAS'
  | 'NOSOTROS'
  | 'CARRUSEL_NOSOTROS'
  | 'CARRUSEL_PROYECTOS'
  | 'CARRUSEL_CONTACTOS'
  | 'NOSOTROS_VALORES'
  | 'MARCAS';

export type Rol = 'ADMINISTRADOR' | 'OPERADOR';

// ============ INTERFACES ============

export interface Contenido {
  id?: number;
  tipo: Tipo;
  titulo: string;
  subtitulo: string;
  descripcion: string;
  imagen: string;
  imagenSecundaria: string;
  contacto: string;
  cargo: string;
  calificacion: number;
  orden: number;
  activo: boolean;
  version: number;
  /** Solo proyectos */
  estado: string;
  fecha: string;
  galeria: string[];
}

export interface Seccion {
  codigo: Tipo;
  titulo: string;
  subtitulo: string;
  activo: boolean;
  version: number;
}

export interface Configuracion {
  nombre: string;
  lema: string;
  telefono: string;
  correo: string;
  direccion: string;
  horario: string;
  mapa: string;
  facebook: string;
  instagram: string;
  whatsapp: string;
  version: number;
}

export interface Sitio {
  configuracion: Configuracion;
  secciones: Seccion[];
  contenidos: Contenido[];
}

export interface Usuario {
  id?: number;
  nombre: string;
  email: string;
  rol: Rol;
  activo: boolean;
  version: number;
  clave?: string;
}

export interface Archivo {
  nombre: string;
  original: string;
  url: string;
  bytes: number;
}

// ============ CONSTANTES ============

export const secciones: { tipo: Tipo; nombre: string; detalle: string }[] = [
  { tipo: 'CARRUSEL',    nombre: 'Carrusel principal',   detalle: 'Imágenes y mensajes de bienvenida' },
  { tipo: 'PROYECTO',    nombre: 'Proyectos',            detalle: 'Tarjetas de proyectos dentro de Inicio' },
  { tipo: 'CASA_MODELO', nombre: 'Casa modelo',          detalle: 'Fondo y tarjeta con fotografía' },
  { tipo: 'RECREACION',  nombre: 'Áreas recreacionales', detalle: 'Espacios para compartir en familia' },
  { tipo: 'TESTIMONIO',  nombre: 'Testimonios',          detalle: 'Fotografía, estrellas y opinión' },
  { tipo: 'VENTAS',      nombre: 'Área de ventas',       detalle: 'Equipo y datos de contacto' },
  { tipo: 'MARCAS',      nombre: 'Marcas',              detalle: 'Logos del carrusel infinito sobre el pie de página' },
  { tipo: 'NOSOTROS',    nombre: 'Nosotros',             detalle: 'Presentación de tu empresa' },
  { tipo: 'NOSOTROS_VALORES', nombre: 'Visión y Misión', detalle: 'Tarjetas de misión, visión y valores' },
  { tipo: 'CARRUSEL_NOSOTROS', nombre: 'Carrusel de Nosotros', detalle: 'Fotografías de la página Nosotros' },
  { tipo: 'CARRUSEL_PROYECTOS', nombre: 'Carrusel de Proyectos', detalle: 'Fotografías de la página Proyectos' },
  { tipo: 'CARRUSEL_CONTACTOS', nombre: 'Carrusel de Contactos', detalle: 'Fotografías de la página Contactos' }
];

export const estadosProyecto: { valor: string; texto: string }[] = [
  { valor: 'EN_EJECUCION', texto: 'En ejecución' },
  { valor: 'TERMINADO',    texto: 'Terminado' },
  { valor: 'PROXIMAMENTE', texto: 'Próximamente' }
];

// ============ FUNCIONES ============

export function contenidoNuevo(tipo: Tipo): Contenido {
  return {
    tipo,
    titulo: '',
    subtitulo: '',
    descripcion: '',
    imagen: '',
    imagenSecundaria: '',
    contacto: '',
    cargo: '',
    calificacion: 5,
    orden: 0,
    activo: true,
    version: 0,
    estado: '',
    fecha: '',
    galeria: []
  };
}