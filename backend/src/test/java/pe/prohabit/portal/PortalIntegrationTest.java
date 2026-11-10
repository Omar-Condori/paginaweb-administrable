package pe.prohabit.portal;

import java.net.*;
import java.net.http.*;
import java.io.*;
import java.util.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import tools.jackson.databind.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT,properties={
 "spring.datasource.url=${TEST_DB_URL:jdbc:h2:mem:portal;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1}",
 "spring.datasource.username=${TEST_DB_USER:sa}","spring.datasource.password=${TEST_DB_PASSWORD:pruebas}",
 "app.admin-email=admin@test.local","app.admin-password=ClavePruebas-2026!",
 "app.seed-demo=true","app.uploads=target/uploads-test"
})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PortalIntegrationTest {
 @Value("${local.server.port}") int port;
 final ObjectMapper json=new ObjectMapper();
 Cliente admin;
 class Cliente {
  final HttpClient http=HttpClient.newBuilder().cookieHandler(new CookieManager(null,CookiePolicy.ACCEPT_ALL)).build();
  String csrf="",header="X-CSRF-TOKEN";
  HttpResponse<String> pedir(String metodo,String ruta,String cuerpo,boolean token) throws Exception {
   var b=HttpRequest.newBuilder(URI.create("http://localhost:"+port+ruta)).header("Content-Type","application/json");
   if(token&&!csrf.isEmpty())b.header(header,csrf);
   return http.send(b.method(metodo,HttpRequest.BodyPublishers.ofString(cuerpo)).build(),HttpResponse.BodyHandlers.ofString());
  }
  JsonNode get(String ruta) throws Exception {var r=pedir("GET",ruta,"",false);assertEquals(200,r.statusCode(),r.body());return json.readTree(r.body());}
  void token() throws Exception {var t=get("/api/sesion/csrf");csrf=t.path("token").asString();header=t.path("headerName").asString();}
  void login(String email,String clave) throws Exception {
   token();var r=http.send(HttpRequest.newBuilder(URI.create("http://localhost:"+port+"/api/sesion/login")).header(header,csrf).header("Content-Type","application/x-www-form-urlencoded").POST(HttpRequest.BodyPublishers.ofString("email="+URLEncoder.encode(email,java.nio.charset.StandardCharsets.UTF_8)+"&clave="+URLEncoder.encode(clave,java.nio.charset.StandardCharsets.UTF_8))).build(),HttpResponse.BodyHandlers.ofString());
   assertEquals(204,r.statusCode(),r.body());token();
  }
 }
 @BeforeAll void acceso() throws Exception {admin=new Cliente();admin.login("admin@test.local","ClavePruebas-2026!");}
 Map<String,Object> contenido(String imagen) {var c=new LinkedHashMap<String,Object>();c.put("tipo","PROYECTO");c.put("titulo","Proyecto de prueba");c.put("subtitulo","Prueba");c.put("descripcion","Contenido persistente");c.put("imagen",imagen);c.put("imagenSecundaria","");c.put("contacto","");c.put("cargo","");c.put("calificacion",5);c.put("orden",7);c.put("activo",true);c.put("version",0);return c;}
 JsonNode crear(Cliente c,Map<String,Object> data) throws Exception {var r=c.pedir("POST","/api/admin/contenidos",json.writeValueAsString(data),true);assertEquals(201,r.statusCode(),r.body());return json.readTree(r.body());}
 @Test void publicoPuedeLeerYNoAdministrar() throws Exception {
  var publico=new Cliente();var s=publico.get("/api/publico/sitio");assertTrue(s.path("contenidos").isArray());assertFalse(s.has("usuarios"));
  assertEquals(401,publico.pedir("GET","/api/admin/sitio","",false).statusCode());
  publico.token();assertEquals(401,publico.pedir("POST","/api/admin/contenidos",json.writeValueAsString(contenido("/ejemplos/parque.svg")),true).statusCode());
 }
 @Test void csrfObligatorio() throws Exception {assertEquals(403,admin.pedir("POST","/api/admin/contenidos","{}",false).statusCode());}
 @Test void crudVersionYVisibilidad() throws Exception {
  var c=contenido("/ejemplos/parque.svg");var nuevo=crear(admin,c);long id=nuevo.path("id").asLong();
  c.put("activo",false);c.put("titulo","Oculto");
  var r=admin.pedir("PUT","/api/admin/contenidos/"+id,json.writeValueAsString(c),true);assertEquals(200,r.statusCode(),r.body());
  assertEquals(409,admin.pedir("PUT","/api/admin/contenidos/"+id,json.writeValueAsString(c),true).statusCode());
  for(JsonNode item:new Cliente().get("/api/publico/sitio").path("contenidos")) assertNotEquals(id,item.path("id").asLong());
  assertEquals(204,admin.pedir("DELETE","/api/admin/contenidos/"+id+"?version=1","",true).statusCode());
  assertEquals(404,admin.pedir("DELETE","/api/admin/contenidos/"+id+"?version=1","",true).statusCode());
 }
 @Test void operadorEditaPeroNoAdministraUsuariosYSesionSeRevoca() throws Exception {
  String email="operador"+System.nanoTime()+"@test.local";
  var u=new LinkedHashMap<String,Object>(Map.of("nombre","Operador","email",email,"clave","ClaveOperador-2026!","rol","OPERADOR","activo",true,"version",0));
  var r=admin.pedir("POST","/api/admin/usuarios",json.writeValueAsString(u),true);assertEquals(201,r.statusCode(),r.body());long id=json.readTree(r.body()).path("id").asLong();assertFalse(r.body().contains("ClaveOperador"));
  var op=new Cliente();op.login(email,"ClaveOperador-2026!");op.get("/api/admin/sitio");
  assertEquals(403,op.pedir("GET","/api/admin/usuarios","",false).statusCode());
  assertEquals(403,op.pedir("POST","/api/admin/usuarios",json.writeValueAsString(u),true).statusCode());
  var c=crear(op,contenido("/ejemplos/parque.svg"));assertEquals(204,op.pedir("DELETE","/api/admin/contenidos/"+c.path("id").asLong()+"?version=0","",true).statusCode());
  u.put("activo",false);u.put("clave","");assertEquals(200,admin.pedir("PUT","/api/admin/usuarios/"+id,json.writeValueAsString(u),true).statusCode());
  assertEquals(401,op.pedir("GET","/api/admin/sitio","",false).statusCode());
  assertEquals(204,admin.pedir("DELETE","/api/admin/usuarios/"+id+"?version=1","",true).statusCode());
 }
 @Test void protegeAdministradorYSingleton() throws Exception {
  long id=admin.get("/api/sesion").path("id").asLong();
  assertEquals(400,admin.pedir("DELETE","/api/admin/usuarios/"+id+"?version=0","",true).statusCode());
  var c=contenido("/ejemplos/parque.svg");c.put("tipo","CASA_MODELO");c.put("imagenSecundaria","/ejemplos/interior.svg");
  assertEquals(400,admin.pedir("POST","/api/admin/contenidos",json.writeValueAsString(c),true).statusCode());
  c.put("tipo","TESTIMONIO");c.put("calificacion",8);assertEquals(400,admin.pedir("POST","/api/admin/contenidos",json.writeValueAsString(c),true).statusCode());
 }
 @Test void configuracionRechazaEnlacesPeligrosos() throws Exception {
  var c=admin.get("/api/admin/sitio").path("configuracion").deepCopy();
  ((tools.jackson.databind.node.ObjectNode)c).put("mapa","javascript:alert(1)");
  assertEquals(400,admin.pedir("PUT","/api/admin/configuracion",json.writeValueAsString(c),true).statusCode());
  ((tools.jackson.databind.node.ObjectNode)c).put("mapa","https://www.google.com/maps/embed?pb=prueba");
  assertEquals(200,admin.pedir("PUT","/api/admin/configuracion",json.writeValueAsString(c),true).statusCode());
 }
 HttpResponse<String> subir(byte[] bytes,String nombre) throws Exception {
  String b="PortalBoundary";var body=new ByteArrayOutputStream();body.write(("--"+b+"\r\nContent-Disposition: form-data; name=\"archivo\"; filename=\""+nombre+"\"\r\nContent-Type: image/png\r\n\r\n").getBytes());body.write(bytes);body.write(("\r\n--"+b+"--\r\n").getBytes());
  return admin.http.send(HttpRequest.newBuilder(URI.create("http://localhost:"+port+"/api/admin/archivos")).header(admin.header,admin.csrf).header("Content-Type","multipart/form-data; boundary="+b).POST(HttpRequest.BodyPublishers.ofByteArray(body.toByteArray())).build(),HttpResponse.BodyHandlers.ofString());
 }
 @Test void imagenRealSubidaPublicadaYBorradoProtegido() throws Exception {
  var out=new ByteArrayOutputStream();ImageIO.write(new BufferedImage(20,20,BufferedImage.TYPE_INT_RGB),"png",out);
  var r=subir(out.toByteArray(),"foto.png");assertEquals(201,r.statusCode(),r.body());var a=json.readTree(r.body());String url=a.path("url").asString(),nombre=a.path("nombre").asString();
  assertEquals(200,new Cliente().pedir("GET",url,"",false).statusCode());
  var c=crear(admin,contenido(url));
  assertEquals(400,admin.pedir("DELETE","/api/admin/archivos/"+nombre,"",true).statusCode());
  assertEquals(204,admin.pedir("DELETE","/api/admin/contenidos/"+c.path("id").asLong()+"?version=0","",true).statusCode());
  assertEquals(204,admin.pedir("DELETE","/api/admin/archivos/"+nombre,"",true).statusCode());
  assertEquals(404,new Cliente().pedir("GET",url,"",false).statusCode());
  assertEquals(400,subir("<script>alert(1)</script>".getBytes(),"falsa.png").statusCode());
 }
 @Test void seccionOcultaNoExponeContenidos() throws Exception {
  var s=admin.get("/api/admin/sitio");JsonNode seccion=null;for(JsonNode item:s.path("secciones")) if(item.path("codigo").asString().equals("RECREACION"))seccion=item.deepCopy();
  assertNotNull(seccion);((tools.jackson.databind.node.ObjectNode)seccion).put("activo",false);
  assertEquals(200,admin.pedir("PUT","/api/admin/secciones/RECREACION",json.writeValueAsString(seccion),true).statusCode());
  for(JsonNode item:new Cliente().get("/api/publico/sitio").path("contenidos"))assertNotEquals("RECREACION",item.path("tipo").asString());
  ((tools.jackson.databind.node.ObjectNode)seccion).put("activo",true);((tools.jackson.databind.node.ObjectNode)seccion).put("version",1);
  assertEquals(200,admin.pedir("PUT","/api/admin/secciones/RECREACION",json.writeValueAsString(seccion),true).statusCode());
 }
}
