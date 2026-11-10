package pe.prohabit.portal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.prohabit.portal.shared.modelo.*;
import pe.prohabit.portal.admin.usuario.UsuariosServicio;
import pe.prohabit.portal.admin.contenido.ContenidoServicio;

@Component
public class Inicializador implements ApplicationRunner {
    private final UsuariosServicio usuarios;
    private final ContenidoServicio contenido;
    private final JdbcTemplate db;
    @Value("${app.admin-email}") String email;
    @Value("${app.admin-password}") String clave;
    @Value("${app.seed-demo}") boolean demo;
    public Inicializador(UsuariosServicio u,ContenidoServicio c,JdbcTemplate d) {usuarios=u;contenido=c;db=d;}
    @Override @Transactional public void run(ApplicationArguments args) {
        db.queryForObject("SELECT id FROM configuracion WHERE id=1 FOR UPDATE",Integer.class);
        if(db.queryForObject("SELECT COUNT(*) FROM usuarios",Integer.class)==0) {
            if(clave.length()<12) throw new IllegalStateException("Define ADMIN_PASSWORD con al menos 12 caracteres para crear el primer administrador.");
            usuarios.guardar(null,new UsuarioEntrada("Administrador",email,clave,Rol.ADMINISTRADOR,true,0));
        }
        if(!demo||db.queryForObject("SELECT COUNT(*) FROM inicializacion WHERE clave='demo'",Integer.class)>0) return;
        agregar(Tipo.CARRUSEL,"Un nuevo comienzo, un lugar para ti.","Construye tu próxima historia","Descubre espacios pensados para disfrutar cada etapa de tu vida.","residencial","",0);
        agregar(Tipo.CARRUSEL,"Más espacio para compartir.","La vida que imaginas","Conoce nuestros proyectos y encuentra tu próximo hogar.","parque","",1);
        agregar(Tipo.PROYECTO,"Residencial Los Olivos","Proyecto de ejemplo","Viviendas con espacios luminosos y áreas para disfrutar en familia.","residencial","",0);
        agregar(Tipo.PROYECTO,"Jardines del Valle","Proyecto de ejemplo","Un entorno abierto que conecta tu hogar con la naturaleza.","parque","",1);
        agregar(Tipo.PROYECTO,"Alameda del Sol","Proyecto de ejemplo","Diseño contemporáneo y ambientes cómodos para cada día.","interior","",2);
        agregar(Tipo.CASA_MODELO,"Espacios que se sienten como hogar","Casa modelo","Ambientes abiertos, luz natural y detalles pensados para tu bienestar. Agenda una visita y conoce la distribución de nuestra casa modelo.","residencial","interior",0);
        agregar(Tipo.RECREACION,"Piscina familiar","","Un espacio para compartir momentos al aire libre.","piscina","",0);
        agregar(Tipo.RECREACION,"Áreas verdes","","Naturaleza y tranquilidad cerca de casa.","parque","",1);
        agregar(Tipo.RECREACION,"Espacios de encuentro","","Ambientes para disfrutar juntos.","interior","",2);
        agregar(Tipo.TESTIMONIO,"Cliente de ejemplo","","Aquí podrás publicar el testimonio real de un cliente, con su fotografía y autorización.","persona","",0);
        agregar(Tipo.VENTAS,"Asesora de ejemplo","","Nuestro equipo te acompaña a conocer el proyecto.","/ejemplos/persona.svg","","contacto@example.com","Asesora",5,0,true,0);
        agregar(Tipo.NOSOTROS,"Construimos espacios para nuevas historias.","Nosotros","Reemplaza este texto con la historia, la misión y los valores de tu empresa. Cuenta qué distingue a tus proyectos y cómo acompañas a las familias a encontrar su hogar.","residencial","",0);
        db.update("INSERT INTO inicializacion(clave) VALUES ('demo')");
    }
    private void agregar(Tipo t,String titulo,String sub,String desc,String img,String secundaria,int orden) {
        contenido.guardar(null,new Contenido(null,t,titulo,sub,desc,"/ejemplos/"+img+".svg",secundaria.isEmpty()?"":"/ejemplos/"+secundaria+".svg","","",5,orden,true,0));
    }
    private void agregar(Tipo t,String titulo,String sub,String desc,String img,String secundaria,String contacto,String cargo,int calificacion,int orden,boolean activo,int version) {
        contenido.guardar(null,new Contenido(null,t,titulo,sub,desc,"/ejemplos/"+img+".svg",secundaria.isEmpty()?"":"/ejemplos/"+secundaria+".svg",contacto,cargo,calificacion,orden,activo,version));
    }
}