package pe.prohabit.portal.shared.modelo;

import jakarta.validation.constraints.*;

public record Configuracion(@NotBlank @Size(max=100) String nombre,
    @NotNull @Size(max=240) String lema, @NotNull @Size(max=40) String telefono,
    @jakarta.validation.constraints.Email @Size(max=180) String correo,
    @NotNull @Size(max=300) String direccion, @NotNull @Size(max=160) String horario,
    @NotNull @Size(max=500) String mapa, @NotNull @Size(max=300) String facebook,
    @NotNull @Size(max=300) String instagram,
    @jakarta.validation.constraints.Pattern(regexp="[0-9]{0,15}",message="WhatsApp debe contener solo dígitos con código de país") String whatsapp,
    @Min(0) long version) {}