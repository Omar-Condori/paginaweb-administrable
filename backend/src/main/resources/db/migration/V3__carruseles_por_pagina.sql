-- Conserva los contenidos actuales y habilita carruseles independientes por página.
ALTER TABLE contenidos DROP CONSTRAINT IF EXISTS contenidos_tipo_check;
ALTER TABLE contenidos ADD CONSTRAINT contenidos_tipo_check CHECK (tipo IN (
    'CARRUSEL', 'PROYECTO', 'CASA_MODELO', 'RECREACION', 'TESTIMONIO', 'VENTAS', 'NOSOTROS',
    'CARRUSEL_NOSOTROS', 'CARRUSEL_PROYECTOS', 'CARRUSEL_CONTACTOS'
));

INSERT INTO secciones(codigo, titulo, subtitulo, activo) VALUES
('CARRUSEL_NOSOTROS', 'Carrusel de Nosotros', '', true),
('CARRUSEL_PROYECTOS', 'Carrusel de Proyectos', '', true),
('CARRUSEL_CONTACTOS', 'Carrusel de Contactos', '', true)
ON CONFLICT (codigo) DO NOTHING;
