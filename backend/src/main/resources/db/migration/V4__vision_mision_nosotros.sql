ALTER TABLE contenidos DROP CONSTRAINT IF EXISTS contenidos_tipo_check;
ALTER TABLE contenidos ADD CONSTRAINT contenidos_tipo_check CHECK (tipo IN (
    'CARRUSEL', 'PROYECTO', 'CASA_MODELO', 'RECREACION', 'TESTIMONIO', 'VENTAS', 'NOSOTROS',
    'CARRUSEL_NOSOTROS', 'CARRUSEL_PROYECTOS', 'CARRUSEL_CONTACTOS', 'NOSOTROS_VALORES'
));

INSERT INTO secciones(codigo, titulo, subtitulo, activo) VALUES
('NOSOTROS_VALORES', 'Visión y Misión', 'Conoce nuestra visión, misión y valores', true)
ON CONFLICT (codigo) DO NOTHING;
