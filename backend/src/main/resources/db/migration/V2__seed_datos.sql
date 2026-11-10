INSERT INTO configuracion(id,nombre,lema,telefono,correo,direccion,horario,mapa,facebook,instagram,whatsapp) VALUES
(1,'PROHABIT','Espacios para vivir, razones para quedarte.','','','','','','','','')
ON CONFLICT (id) DO UPDATE SET
  nombre=EXCLUDED.nombre,
  lema=EXCLUDED.lema,
  telefono=EXCLUDED.telefono,
  correo=EXCLUDED.correo,
  direccion=EXCLUDED.direccion,
  horario=EXCLUDED.horario,
  mapa=EXCLUDED.mapa,
  facebook=EXCLUDED.facebook,
  instagram=EXCLUDED.instagram,
  whatsapp=EXCLUDED.whatsapp;

INSERT INTO secciones(codigo,titulo,subtitulo,activo) VALUES
('CARRUSEL','Tu próximo hogar comienza aquí','Espacios para construir una nueva historia',true),
('PROYECTO','Nuestros Proyectos','El sueño de la casa propia',true),
('CASA_MODELO','Un hogar confortable','Casa Modelo',true),
('RECREACION','Áreas Recreacionales','Fines de Semana en Familia',true),
('TESTIMONIO','Nuestros','Clientes Satisfechos',true),
('VENTAS','Ventas','Conoce a Nuestra Área de Ventas',true),
('NOSOTROS','Nosotros','Nuestra Historia',true)
ON CONFLICT (codigo) DO UPDATE SET
  titulo=EXCLUDED.titulo,
  subtitulo=EXCLUDED.subtitulo,
  activo=EXCLUDED.activo;