-- Carrusel infinito de marcas (logos) que se muestra sobre el pie de todas las páginas.
ALTER TABLE contenidos DROP CONSTRAINT IF EXISTS contenidos_tipo_check;
ALTER TABLE contenidos ADD CONSTRAINT contenidos_tipo_check CHECK (tipo IN (
    'CARRUSEL', 'PROYECTO', 'CASA_MODELO', 'RECREACION', 'TESTIMONIO', 'VENTAS', 'NOSOTROS',
    'CARRUSEL_NOSOTROS', 'CARRUSEL_PROYECTOS', 'CARRUSEL_CONTACTOS', 'NOSOTROS_VALORES', 'MARCAS'
));

INSERT INTO secciones(codigo, titulo, subtitulo, activo) VALUES
('MARCAS', 'Marcas con las que trabajamos', '', true)
ON CONFLICT (codigo) DO NOTHING;
